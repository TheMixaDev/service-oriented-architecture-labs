package com.aeeph.routeservice.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.Date;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;

@Entity
@JacksonXmlRootElement(localName = "Route")
public class Route {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long
      id; // Значение поля должно быть больше 0, Значение этого поля должно быть уникальным,
          // Значение этого поля должно генерироваться автоматически

  @NotNull(message = "Не заполнено поле name")
  @Size(min = 1, message = "Поле name не может быть пустым")
  private String name; // Поле не может быть null, Строка не может быть пустой

  @NotNull(message = "Не заполнено поле coordinates")
  @Valid
  @Embedded
  private Coordinates coordinates; // Поле не может быть null

  @Temporal(TemporalType.TIMESTAMP)
  private Date
      creationDate; // Поле не может быть null, Значение этого поля должно генерироваться
                    // автоматически

  @NotNull(message = "Не заполнено поле from")
  @Valid
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "x", column = @Column(name = "from_x")),
    @AttributeOverride(name = "y", column = @Column(name = "from_y")),
    @AttributeOverride(name = "name", column = @Column(name = "from_name"))
  })
  @JacksonXmlProperty(localName = "from")
  private Location fromLocation; // Поле не может быть null

  @NotNull(message = "Не заполнено поле to")
  @Valid
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "x", column = @Column(name = "to_x")),
    @AttributeOverride(name = "y", column = @Column(name = "to_y")),
    @AttributeOverride(name = "name", column = @Column(name = "to_name"))
  })
  @JacksonXmlProperty(localName = "to")
  private Location toLocation; // Поле не может быть null

  @Min(value = 2, message = "Значение поля distance должно быть больше 1")
  private Integer distance; // Поле может быть null, Значение поля должно быть больше 1

  @Enumerated(EnumType.STRING)
  @JacksonXmlProperty(localName = "priority")
  private Priority priority;

  @PrePersist
  protected void onCreate() {
    creationDate = new Date();
  }

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

  public Location getFromLocation() {
    return fromLocation;
  }

  public void setFromLocation(Location from) {
    this.fromLocation = from;
  }

  public Location getToLocation() {
    return toLocation;
  }

  public void setToLocation(Location to) {
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
