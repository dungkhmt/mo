package com.socolabs.mo.components.collectparcels.nearestsearcher;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.collectparcels.Parcel;
import com.socolabs.mo.components.collectparcels.RouteElement;

public interface NearestSearcher {

    void add(RouteElement e);
    void remove(RouteElement e);
    Pair<RouteElement, Pair<Double, Double>> getNearestElement(Parcel p);
    long getTotalQueryTime();
}
