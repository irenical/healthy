package org.irenical.healthy;

import org.irenical.jindy.ConfigNotFoundException;
import org.junit.Test;

public class HealthyTest {
  
  @Test(expected=IllegalArgumentException.class)
  public void testAssertTarget(){
    new Healthy(null, null, "my.app.port");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testAssertPort(){
    TrivialLifecycle lc = new TrivialLifecycle();
    new Healthy(lc, null, null);
  }
  
  @Test
  public void testBypassed() throws ConfigNotFoundException{
    TrivialLifecycle lc = new TrivialLifecycle();
    Healthy h = new Healthy(lc, null, "my.app.port");
    h.start();
  }

}
