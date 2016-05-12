package org.irenical.healthy;

import com.ecwid.consul.transport.TransportException;
import org.apache.commons.configuration.ConversionException;
import org.irenical.jindy.ConfigFactory;
import org.irenical.jindy.ConfigNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    new Healthy(null, "myapp", null, null);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testAssertApplication() throws ConfigNotFoundException {
    TrivialLifecycle lc = new TrivialLifecycle();
    Healthy h = new Healthy(lc, null, null);
    h.start();
  }
  
  @Test(expected=ConfigNotFoundException.class)
  public void testAssertPortValue() throws ConfigNotFoundException {
    TrivialLifecycle lc = new TrivialLifecycle();
    Healthy h = new Healthy(lc, "myapp", null, "my.app.port");
    h.start();
  }
  
  @Test(expected=ConversionException.class)
  public void testInvalidPortValue() throws ConfigNotFoundException {
    ConfigFactory.getConfig().setProperty("my.app.port", "a");
    TrivialLifecycle lc = new TrivialLifecycle();
    Healthy h = new Healthy(lc, "myapp", null, "my.app.port");
    h.start();
  }
  
  @Test(expected=TransportException.class)
  public void testNoConsul() throws ConfigNotFoundException {
    ConfigFactory.getConfig().setProperty("my.app.port", 1337);
    TrivialLifecycle lc = new TrivialLifecycle();
    Healthy h = new Healthy(lc, "myapp", null, "my.app.port");
    h.start();
  }

}
