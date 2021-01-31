package com.socolabs.mo.components.collectparcels;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;

public interface NearestSearcher {

    void add(RouteElement e);
    void remove(RouteElement e);
    Pair<RouteElement, Pair<Double, Double>> getNearestElement(Parcel p);
}
