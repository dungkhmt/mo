package com.dailyopt.mo.components.algorithms.nearestlocation;

import com.dailyopt.mo.components.maps.utils.GoogleMapsQuery;
import com.dailyopt.mo.components.movingobjects.ILocation;

public class HaversineDistance implements IDistance {

    private static GoogleMapsQuery G = new GoogleMapsQuery();

    public HaversineDistance() {

    }

    @Override
    public double getDistance(ILocation a, ILocation b) {
        return G.computeDistanceHaversine(a.getLat(), a.getLng(), b.getLat(), b.getLng());
    }
}
