package com.aeeph.navigatorservice.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "Coordinates")
public class Coordinates {

  private double x;
  private Integer y;

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public Integer getY() {
    return y;
  }

  public void setY(Integer y) {
    this.y = y;
  }

  public int distanceTo(Coordinates other) {
    return (int) (Math.abs(this.x - other.x) + Math.abs(this.y - other.y));
  }

  public int distanceLinear(Coordinates other) {
    return (int) (Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2)));
  }

  public Location toLocation(String name) {
    Location location = new Location();
    location.setName(name);
    location.setX(this.x);
    location.setY(this.y);
    return location;
  }
}
