package org.irenical.healthy;

import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigNotFoundException;
import org.irenical.jindy.archaius.ConsulConfigStrategy;

public class IrenicalConsulConfigStrategy implements ConsulConfigStrategy {

  @Override
  public boolean bypassConsul(Config config) {
    String environment = config.getString(Healthy.PROPERTY_ENVIRONMENT);
    String stack = config.getString(Healthy.PROPERTY_STACK);
    String cluster = config.getString(Healthy.PROPERTY_CLUSTER);
    return environment == null || (stack == null && cluster == null);
  }

  @Override
  public String getBasePath(Config config) {
    try {
      String stack = config.getString(Healthy.PROPERTY_STACK, config.getString(Healthy.PROPERTY_CLUSTER));
      if (stack == null) {
        throw new ConfigNotFoundException("Either 'stack' or 'cluster' property must be defined");
      }
      String app = config.getMandatoryString(Healthy.PROPERTY_APPLICATION_NAME);
      return stack + "/" + app;
    } catch (ConfigNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

}
