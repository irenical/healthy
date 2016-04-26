package org.irenical.healthy;

public class ApplicationHealth {

  private Health health;

  private String details;

  public void setDetails(String details) {
    this.details = details;
  }

  public void setHealth(Health health) {
    this.health = health;
  }

  public String getDetails() {
    return details;
  }

  public Health getHealth() {
    return health;
  }

}
