package org.irenical.healthy;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.irenical.jindy.ConfigNotFoundException;
import org.irenical.lifecycle.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Check;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Self;

/**
 * An health checker that takes a standard LifeCycle and uses the result of its isRunning method
 * to register a service and its status to Consul
 */
public class Healthy implements LifeCycle {

  private static final Logger LOGGER = LoggerFactory.getLogger(Healthy.class);

  private static final String CHECK_PREFIX = "service:";

  private static final String PROPERTY_CONSUL_HOST = "consul.host";

  private static final String DEFAULT_CONSUL_HOST = "localhost";

  private static final String PROPERTY_CONSUL_PORT = "consul.port";

  private static final int DEFAULT_CONSUL_PORT = 8500;

  private static final String PROPERTY_HEALTHCHECK_INTERVAL_MILLIS = "healthy.interval.millis";

  private static final int DEFAULT_APPLICATION_HEALTHCHECK_INTERVAL_MILLIS = 10000;

  private static final int MIN_APPLICATION_HEALTHCHECK_INTERVAL_MILLIS = 1000;

  private static final Config CONFIG = ConfigFactory.getConfig();

  private final ScheduledExecutorService checkExecutor = new ScheduledThreadPoolExecutor(1);

  private final LifeCycle target;

  private final String serviceId;

  private final String serviceName;

  private final String serviceAddressPropertyKey;

  private final String servicePortPropertyKey;

  private ConsulClient client;

  /**
   * Build a Healthy instance for the given target LifeCycle with a serviceName
   * and the consul property for the serviceAddress value.
   *
   * Defaults to using the serviceName as the serviceId
   *
   * @param target the target LifeCycle for the health checks
   * @param serviceName the service name to report to consul
   * @param serviceAddressPropertyKey the Jindy property where the service address value is located (optional)
   */
  public Healthy(LifeCycle target, String serviceName, String serviceAddressPropertyKey) {
    this(target, serviceName, serviceAddressPropertyKey, null);
  }

  /**
   * Build a Healthy instance for the given target LifeCycle with a serviceName
   * and the consul properties for the serviceAddress and servicePort values.
   *
   * Defaults to using the serviceName as the serviceId
   *
   * @param target the target LifeCycle for the health checks
   * @param serviceName the service name to report to consul
   * @param serviceAddressPropertyKey the Jindy property where the service address value is located (optional)
   * @param servicePortPropertyKey the Jindy property where the port value is located (optional)
   */
  public Healthy(LifeCycle target, String serviceName, String serviceAddressPropertyKey,
                 String servicePortPropertyKey) {

    // Default to using the serviceName as the serviceId (the same as consuls' default behaviour)
    this(target, serviceName, serviceName, serviceAddressPropertyKey, servicePortPropertyKey);
  }

  /**
   * Build a Healthy instance for the given target LifeCycle with a serviceName and serviceId
   * and the consul properties for the serviceAddress and servicePort values.
   *
   * @param target the target LifeCycle for the health checks
   * @param serviceId the service id to report to consul
   * @param serviceName the service name to report to consul
   * @param serviceAddressPropertyKey the Jindy property where the service address value is located (optional)
   * @param servicePortPropertyKey the Jindy property where the port value is located (optional)
   */
  public Healthy(LifeCycle target, String serviceId, String serviceName, String serviceAddressPropertyKey,
                 String servicePortPropertyKey) {

    if (target == null) {
      throw new IllegalArgumentException("Target LifeCycle cannot be null");
    }
    if (serviceName == null || serviceName.trim().isEmpty()) {
      throw new IllegalArgumentException("Service name cannot be null or empty");
    }

    this.target = target;
    this.serviceId = serviceId;
    this.serviceName = serviceName;
    this.serviceAddressPropertyKey = serviceAddressPropertyKey;
    this.servicePortPropertyKey = servicePortPropertyKey;
  }

  @Override
  public void start() throws ConfigNotFoundException {
    client = new ConsulClient(CONFIG.getString(PROPERTY_CONSUL_HOST, DEFAULT_CONSUL_HOST),
        CONFIG.getInt(PROPERTY_CONSUL_PORT, DEFAULT_CONSUL_PORT));
    // monitor service and schedule next health checks
    monitor();
    // setup property change listeners
    setupListeners();
  }

  @Override
  public void stop() throws ConfigNotFoundException {
    deRegisterService();
  }

  @Override
  public boolean isRunning() throws ConfigNotFoundException {
    if (client == null) {
      return false;
    }
    Response<Self> agentSelf = client.getAgentSelf();
    return agentSelf != null && agentSelf.getValue() != null;
  }

  private void setupListeners() {
    if (serviceAddressPropertyKey != null) {
      CONFIG.listen(serviceAddressPropertyKey, this::onServicePropertyChanged);
    }
    if (servicePortPropertyKey != null) {
      CONFIG.listen(servicePortPropertyKey, this::onServicePropertyChanged);
    }
  }

  private void onServicePropertyChanged(String propertyKey) {
    try {
      deRegisterService();
    } catch (ConfigNotFoundException e) {
      LOGGER.error("Error updating service", e);
    }
  }

  /**
   * Registers this service into Consul.
   *
   * @throws ConfigNotFoundException
   */
  private Check getCheck() throws ConfigNotFoundException {
    Check result = client.getAgentChecks().getValue().get(CHECK_PREFIX + serviceId);
    if (result == null) {
      NewService service = new NewService();
      service.setId(serviceId);
      service.setName(serviceName);
      if (serviceAddressPropertyKey != null) {
        service.setAddress(CONFIG.getString(serviceAddressPropertyKey));
      }
      if (servicePortPropertyKey != null) {
        service.setPort(CONFIG.getMandatoryInt(servicePortPropertyKey));
      }

      NewService.Check check = new NewService.Check();
      check.setTtl((2 * getHealthCheckInterval()) + "ms");
      service.setCheck(check);

      client.agentServiceRegister(service);
    }
    return client.getAgentChecks().getValue().get(CHECK_PREFIX + serviceId);
  }

  /**
   * Unregisters this service from Consul.
   *
   * @throws ConfigNotFoundException
   */
  private void deRegisterService() throws ConfigNotFoundException {
    client.agentServiceDeregister(serviceId);
  }

  /**
   * Schedules the next service health check
   */
  private void scheduleHealthCheck() {
    checkExecutor.schedule(this::monitor, getHealthCheckInterval(), TimeUnit.MILLISECONDS);
  }

  private int getHealthCheckInterval() {
    int interval = CONFIG.getInt(PROPERTY_HEALTHCHECK_INTERVAL_MILLIS, DEFAULT_APPLICATION_HEALTHCHECK_INTERVAL_MILLIS);
    if (interval < MIN_APPLICATION_HEALTHCHECK_INTERVAL_MILLIS) {
      interval = MIN_APPLICATION_HEALTHCHECK_INTERVAL_MILLIS;
    }
    return interval;
  }

  /**
   * Checks service health state and update Consul service
   */
  private void monitor() {
    try {
      Check gotCheck = getCheck();
      String checkId = gotCheck.getCheckId();
      ApplicationHealth health = healthCheck();
      switch (health.getHealth()) {
      case HEALTHY:
        client.agentCheckPass(checkId, health.getDetails());
        break;
      case WARNING:
        LOGGER.warn("Reporting WARN Status (" + health.getDetails() + ")");
        client.agentCheckWarn(checkId, health.getDetails());
        break;
      case CRITICAL:
        LOGGER.error("Reporting CRITICAL Status (" + health.getDetails() + ")");
        client.agentCheckFail(checkId, health.getDetails());
        break;
      }
    } catch (Exception e) {
      LOGGER.error("Error monitoring service health", e);
    } finally {
      scheduleHealthCheck();
    }
  }

  private ApplicationHealth healthCheck() {
    ApplicationHealth result = new ApplicationHealth();
    result.setDetails("Default health check ran at " + ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
    if (target.isRunning()) {
      result.setHealth(Health.HEALTHY);
    } else {
      result.setHealth(Health.CRITICAL);
    }
    return result;
  }

}
