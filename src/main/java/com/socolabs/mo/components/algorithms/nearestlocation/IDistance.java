package com.socolabs.mo.components.algorithms.nearestlocation;

import com.socolabs.mo.components.movingobjects.ILocation;

public interface IDistance {
    public double getDistance(ILocation a, ILocation b);
}
