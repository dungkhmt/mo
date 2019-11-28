package com.socolabs.mo.components.algorithms.nearestlocation;

import com.socolabs.mo.components.maps.utils.GoogleMapsQuery;
import com.socolabs.mo.components.movingobjects.ILocation;

public class HaversineDistance implements IDistance {

    private static GoogleMapsQuery G = new GoogleMapsQuery();

    public HaversineDistance() {

    }

    @Override
    public double getDistance(ILocation a, ILocation b) {
        return G.computeDistanceHaversine(a.getLat(), a.getLng(), b.getLat(), b.getLng());
    }
}
