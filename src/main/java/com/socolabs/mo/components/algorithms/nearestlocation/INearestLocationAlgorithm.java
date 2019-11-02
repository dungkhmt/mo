package com.socolabs.mo.components.algorithms.nearestlocation;

import com.socolabs.mo.components.movingobjects.ILocation;

import java.util.ArrayList;

public interface INearestLocationAlgorithm {
    public void updateLocation(ILocation p);
    public void addLocation(ILocation p);
    public void removeLocation(ILocation p);
    public ILocation findNearestLocation(ILocation p);
    public ArrayList<ILocation> findKNearestLocations(ILocation p);
}
