package com.socolabs.mo.components.maps.graphs;

import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.movingobjects.ILocation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Node extends Point implements ILocation {
    private int id;

    public Node(int id, double lat, double lng) {
        super(lat, lng);
        this.id = id;
    }

    public Node() {
        super();
    }
}
