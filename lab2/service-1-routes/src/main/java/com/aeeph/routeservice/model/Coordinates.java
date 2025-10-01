package com.aeeph.routeservice.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import javax.persistence.Embeddable;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import javax.xml.bind.annotation.XmlRootElement;

@Embeddable
@JacksonXmlRootElement(localName = "Coordinates")
//@XmlAccessorType(XmlAccessType.FIELD)
public class Coordinates {

    @Max(value = 716, message = "Максимальное значение для поля x - 716")
    private double x; //Максимальное значение поля: 716

    @NotNull(message = "Не заполнено поле y")
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
