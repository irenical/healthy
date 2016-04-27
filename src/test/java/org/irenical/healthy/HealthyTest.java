package org.irenical.healthy;

import org.apache.commons.configuration.ConversionException;
import org.irenical.jindy.ConfigFactory;
import org.irenical.jindy.ConfigNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ecwid.consul.transport.TransportException;

public class HealthyTest {
  
  @Before
  public void before(){
  }
  
  @After
  public void after(){
    ConfigFactory.getConfig().clear();
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testAssertTarget(){
    new Healthy(null, null, "my.app.port");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testAssertPort(){
    TrivialLifecycle lc = new TrivialLifecycle();
    new Healthy(lc, null, null);
  }
  
  @Test(expected=ConfigNotFoundException.class)
  public void testAssertApplication() throws ConfigNotFoundException {
    TrivialLifecycle lc = new TrivialLifecycle();
    Healthy h = new Healthy(lc, null, "my.app.port");
    h.start();
  }
  
  @Test(expected=ConfigNotFoundException.class)
  public void testAssertPortValue() throws ConfigNotFoundException {
    ConfigFactory.getConfig().setProperty("application", "myapp");
    TrivialLifecycle lc = new TrivialLifecycle();
    Healthy h = new Healthy(lc, null, "my.app.port");
    h.start();
  }
  
  @Test(expected=ConversionException.class)
  public void testInvalidPortValue() throws ConfigNotFoundException {
    ConfigFactory.getConfig().setProperty("application", "myapp");
    ConfigFactory.getConfig().setProperty("my.app.port", "a");
    TrivialLifecycle lc = new TrivialLifecycle();
    Healthy h = new Healthy(lc, null, "my.app.port");
    h.start();
  }
  
  @Test(expected=TransportException.class)
  public void testNoConsul() throws ConfigNotFoundException {
    ConfigFactory.getConfig().setProperty("application", "myapp");
    ConfigFactory.getConfig().setProperty("my.app.port", 1337);
    TrivialLifecycle lc = new TrivialLifecycle();
    Healthy h = new Healthy(lc, null, "my.app.port");
    h.start();
  }

}
