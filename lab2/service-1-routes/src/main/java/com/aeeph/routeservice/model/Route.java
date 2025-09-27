package com.aeeph.routeservice.model;

import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@Entity
@XmlRootElement(name = "Route")
@XmlAccessorType(XmlAccessType.FIELD)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически

    @NotNull
    @Size(min = 1)
    private String name; //Поле не может быть null, Строка не может быть пустой

    @NotNull
    @Embedded
    private Coordinates coordinates; //Поле не может быть null

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически

    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="x", column=@Column(name="from_x")),
            @AttributeOverride(name="y", column=@Column(name="from_y")),
            @AttributeOverride(name="name", column=@Column(name="from_name"))
    })
    private Location fromLocation; //Поле не может быть null

    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="x", column=@Column(name="to_x")),
            @AttributeOverride(name="y", column=@Column(name="to_y")),
            @AttributeOverride(name="name", column=@Column(name="to_name"))
    })
    private Location toLocation; //Поле не может быть null

    @Min(2)
    private Integer distance; //Поле может быть null, Значение поля должно быть больше 1

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

    @XmlElement(name = "from")
    public Location getFrom() {
        return fromLocation;
    }

    public void setFrom(Location from) {
        this.fromLocation = from;
    }

    @XmlElement(name = "to")
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
}
