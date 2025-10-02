package com.aeeph.navigatorservice.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "Location")
public class Location {

  private Double x;
  private double y;
  private String name;

  public Double getX() {
    return x;
  }

  public void setX(Double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
