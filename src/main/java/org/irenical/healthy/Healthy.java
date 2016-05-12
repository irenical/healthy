package org.irenical.healthy;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Check;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Self;
import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.irenical.jindy.ConfigNotFoundException;
import org.irenical.lifecycle.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Healthy implements LifeCycle {

  private static final Logger LOGGER = LoggerFactory.getLogger(Healthy.class);

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

  private final String serviceName;

  private final String serviceAddressPropertyKey;

  private final String servicePortPropertyKey;

  private ConsulClient client;

  public Healthy(LifeCycle target, String serviceName, String serviceAddressPropertyKey) {
    this(target, serviceName, serviceAddressPropertyKey, null);
  }

  public Healthy(LifeCycle target, String serviceName, String serviceAddressPropertyKey, String servicePortPropertyKey) {
    if (target == null) {
      throw new IllegalArgumentException("Target LifeCycle cannot be null");
    }
    if (serviceName == null || serviceName.trim().isEmpty()) {
      throw new IllegalArgumentException("Application name cannot be null or empty");
    }
    this.target = target;
    this.serviceName = serviceName;
    this.serviceAddressPropertyKey = serviceAddressPropertyKey;
    this.servicePortPropertyKey = servicePortPropertyKey;
  }

  @Override
  public void start() throws ConfigNotFoundException {
    client = new ConsulClient(CONFIG.getString(PROPERTY_CONSUL_HOST, DEFAULT_CONSUL_HOST),
      CONFIG.getInt(PROPERTY_CONSUL_PORT, DEFAULT_CONSUL_PORT));
    // register service into consul
    registerService();
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
      registerService();
    } catch (ConfigNotFoundException e) {
      LOGGER.error("Error updating service", e);
    }
  }

  /**
   * Registers this service into Consul.
   *
   * @throws ConfigNotFoundException
   */
  private void registerService() throws ConfigNotFoundException {
    NewService service = new NewService();
    service.setId(serviceName);
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

  /**
   * Unregisters this service from Consul.
   *
   * @throws ConfigNotFoundException
   */
  private void deRegisterService() throws ConfigNotFoundException {
    client.agentServiceDeregister(serviceName);
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
      Response<Map<String, Check>> got = client.getAgentChecks();
      Check gotCheck = got.getValue().get("service:" + serviceName);
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
      LOGGER.error("Error monitoring application health", e);
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
