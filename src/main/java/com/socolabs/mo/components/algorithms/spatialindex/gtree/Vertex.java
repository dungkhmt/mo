package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.movingobjects.ILocation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vertex extends Point {
    private int id;

    public Vertex(int id, double lat, double lng) {
        super(lat, lng);
        this.id = id;
    }

    public String toString() {
        return "" + id;
    }
}
