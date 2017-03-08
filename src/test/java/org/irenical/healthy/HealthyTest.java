package org.irenical.healthy;

import org.irenical.lifecycle.LifeCycle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class HealthyTest {

  private static final String SERVICE_NAME = "testServiceName";

  private static final String SERVICE_ID = "testServiceId";

  @Test
  public void testServiceIdDefaultsToServiceName() throws Exception {

    final DummyLifeCycle dummyLifeCycle = new DummyLifeCycle();


    Healthy instance = new Healthy(dummyLifeCycle, SERVICE_NAME, null);

    assertEquals("serviceId should match provided serviceName", SERVICE_NAME, instance.getServiceId());
    assertEquals("serviceName should match provided serviceName", SERVICE_NAME, instance.getServiceName());
    assertEquals("serviceId should default to serviceName", instance.getServiceName(), instance.getServiceId());


    Healthy anotherInstance = new Healthy(dummyLifeCycle, SERVICE_ID, SERVICE_NAME, null, null);

    assertEquals("serviceId should match provided serviceId", SERVICE_ID, anotherInstance.getServiceId());
    assertEquals("serviceName should match provided serviceName", SERVICE_NAME, anotherInstance.getServiceName());
    assertNotEquals("serviceId should differ from serviceName", anotherInstance.getServiceName(), anotherInstance.getServiceId());
  }

  private static final class DummyLifeCycle implements LifeCycle {
    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public boolean isRunning() {
      return true;
    }
  }
}
