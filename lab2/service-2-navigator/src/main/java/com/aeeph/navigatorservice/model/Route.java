package com.aeeph.navigatorservice.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "Route")
@XmlAccessorType(XmlAccessType.FIELD)
public class Route {

    private long id;
    private String name;
    private Coordinates coordinates;
    private Date creationDate;
    
    @XmlElement(name = "from")
    private Location fromLocation;
    
    @XmlElement(name = "to")
    private Location toLocation;
    
    private Integer distance;

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
}
