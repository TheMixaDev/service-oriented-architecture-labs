package com.aeeph.routeservice.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Embeddable
@JacksonXmlRootElement(localName = "Location")
public class Location {

  @NotNull(message = "Не заполнено поле x")
  private Double x; // Поле не может быть null

  private double y;

  @NotNull(message = "Не заполнено поле name")
  @Size(min = 1, message = "Поле name не может быть пустым")
  @Size(max = 100, message = "Максимальная длина name — 100")
  private String name; // Строка не может быть пустой, Поле не может быть null

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
