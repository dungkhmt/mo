package com.socolabs.mo.components.collectparcels;

import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;
import lombok.Getter;

@Getter
public class Parcel {
    private Vertex location;
    private int weight;
    private int moment;
}
