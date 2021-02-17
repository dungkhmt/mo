package com.socolabs.mo.components.parcelcollection;

import com.dailyopt.vrp.core.VRPPoint;
import com.dailyopt.vrp.core.VRPVarRoutes;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;
import lombok.Getter;

@Getter
public class PCPoint extends VRPPoint {

    private Vertex v;
    private int weight;

    public PCPoint(Vertex v, VRPVarRoutes vr) {
        super(v.toString());
        this.v = v;
        this.weight = 0;
        vr.post(this);
    }

    public PCPoint(Vertex v, VRPVarRoutes vr, int weight) {
        super(v.toString());
        this.v = v;
        this.weight = weight;
        vr.post(this);
    }
}
