package org.irenical.healthy;

import org.irenical.lifecycle.LifeCycle;

public class TrivialLifecycle implements LifeCycle {
  
  private boolean walking = false;

  @Override
  public <ERROR extends Exception> void start() throws ERROR {
    walking = true;
  }

  @Override
  public <ERROR extends Exception> void stop() throws ERROR {
    walking = false;
  }

  @Override
  public <ERROR extends Exception> boolean isRunning() throws ERROR {
    return walking;
  }

}
