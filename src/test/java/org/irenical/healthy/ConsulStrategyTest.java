package org.irenical.healthy;

import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConsulStrategyTest {

  private Config config;

  private IrenicalConsulConfigStrategy strategy;

  @Before
  public void pre() {
    config = ConfigFactory.getConfig();
    strategy = new IrenicalConsulConfigStrategy();
  }

  @After
  public void post() {
    config.clear();
  }

  @Test
  public void testBypass() {
    Assert.assertTrue(strategy.bypassConsul(config));
    config.setProperty(Healthy.PROPERTY_ENVIRONMENT, "development");
    config.setProperty(Healthy.PROPERTY_STACK, "java");
    Assert.assertFalse(strategy.bypassConsul(config));
  }

  @Test(expected = RuntimeException.class)
  public void testConfigFailed() {
    Config config = ConfigFactory.getConfig();
    IrenicalConsulConfigStrategy strategy = new IrenicalConsulConfigStrategy();
    strategy.getBasePath(config);
  }

  @Test(expected = RuntimeException.class)
  public void testConfigStackFailed() {
    Config config = ConfigFactory.getConfig();
    config.setProperty(Healthy.PROPERTY_APPLICATION_NAME, "myapp");
    IrenicalConsulConfigStrategy strategy = new IrenicalConsulConfigStrategy();
    strategy.getBasePath(config);
  }

  @Test(expected = RuntimeException.class)
  public void testConfigApplicationFailed() {
    Config config = ConfigFactory.getConfig();
    config.setProperty(Healthy.PROPERTY_STACK, "web");
    IrenicalConsulConfigStrategy strategy = new IrenicalConsulConfigStrategy();
    strategy.getBasePath(config);
  }
  
  @Test
  public void testPath() {
    config.setProperty(Healthy.PROPERTY_APPLICATION_NAME, "myapp");
    config.setProperty(Healthy.PROPERTY_STACK, "web");
    IrenicalConsulConfigStrategy strategy = new IrenicalConsulConfigStrategy();
    Assert.assertEquals(strategy.getBasePath(config),"web/myapp");
  }

}
