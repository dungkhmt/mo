package com.dailyopt.mo.components.algorithms.nearestlocation;

import com.dailyopt.mo.components.movingobjects.ILocation;

public interface IDistance {
    public double getDistance(ILocation a, ILocation b);
}
