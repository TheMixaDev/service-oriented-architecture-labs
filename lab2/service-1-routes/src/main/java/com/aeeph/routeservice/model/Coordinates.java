package com.aeeph.routeservice.model;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Embeddable
@XmlRootElement(name = "Coordinates")
@XmlAccessorType(XmlAccessType.FIELD)
public class Coordinates {

    private double x; //Максимальное значение поля: 716

    private Integer y; //Поле не может быть null

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
}
