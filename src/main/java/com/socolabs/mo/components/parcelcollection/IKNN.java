package com.socolabs.mo.components.parcelcollection;

import com.dailyopt.vrp.core.VRPPoint;
import com.dailyopt.vrp.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;

import java.util.ArrayList;

public interface IKNN {

    ArrayList<Pair<VRPPoint, Pair<Double, Double>>> getKNN(Vertex v);
    void addPoint(VRPPoint p);
    void removePoint(VRPPoint p);
    void setAccWeight(AccumulatedWeightPoints accWeight);
}
