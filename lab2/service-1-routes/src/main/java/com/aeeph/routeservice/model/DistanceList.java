package com.aeeph.routeservice.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "DistanceList")
@XmlAccessorType(XmlAccessType.FIELD)
public class DistanceList {

    @XmlElement(name = "Distance")
    private List<Integer> distances;

    public DistanceList() {}

    public DistanceList(List<Integer> distances) {
        this.distances = distances;
    }

    public List<Integer> getDistances() {
        return distances;
    }

    public void setDistances(List<Integer> distances) {
        this.distances = distances;
    }
}
