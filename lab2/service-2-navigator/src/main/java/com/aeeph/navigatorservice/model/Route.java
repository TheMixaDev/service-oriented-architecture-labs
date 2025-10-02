package com.aeeph.navigatorservice.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.Date;

@JacksonXmlRootElement(localName = "Route")
public class Route {

  private long id;
  private String name;
  private Coordinates coordinates;
  private Date creationDate;

  @JacksonXmlProperty(localName = "from")
  private Location fromLocation;

  @JacksonXmlProperty(localName = "to")
  private Location toLocation;

  private Integer distance;

  @JacksonXmlProperty(localName = "priority")
  private Priority priority;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Coordinates getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Location getFrom() {
    return fromLocation;
  }

  public void setFrom(Location from) {
    this.fromLocation = from;
  }

  public Location getTo() {
    return toLocation;
  }

  public void setTo(Location to) {
    this.toLocation = to;
  }

  public Integer getDistance() {
    return distance;
  }

  public void setDistance(Integer distance) {
    this.distance = distance;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }
}
