package com.dailyopt.mo.components.algorithms.nearestlocation;

import com.dailyopt.mo.components.movingobjects.ILocation;

public class EuclideanDistance implements IDistance{

    public EuclideanDistance() {

    }

    @Override
    public double getDistance(ILocation a, ILocation b) {
        double latDelta = a.getLat() - b.getLat();
        double lngDelta = a.getLng() - b.getLng();
        return Math.sqrt(latDelta * latDelta + lngDelta * lngDelta);
    }
}
