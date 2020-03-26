package com.socolabs.mo.vrplib.entities.distancemanagers;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.IWeightManager;

import java.util.HashMap;

public class TravelTimeManager implements IDistanceManager {

    private VRPVarRoutes vr;
    private HashMap<String, HashMap<String, Integer>> travelTimeMap;

    public TravelTimeManager(VRPVarRoutes vr, HashMap<String, HashMap<String, Integer>> travelTimeMap) {
        this.vr = vr;
        this.travelTimeMap = travelTimeMap;
    }

    @Override
    public double getDistance(VRPPoint x, VRPPoint y) {
        return travelTimeMap.get(x.getLocation()).get(y.getLocation());
    }

    @Override
    public double getTmpDistance(VRPPoint x, VRPPoint y) {
        return travelTimeMap.get(x.getLocation()).get(y.getLocation());
    }

    @Override
    public void addNewPoint(VRPPoint point) {

    }

    @Override
    public void removePoint(VRPPoint point) {

    }

    @Override
    public void addNewRoute(VRPRoute route) {

    }

    @Override
    public void removeRoute(VRPRoute route) {

    }

    @Override
    public VRPVarRoutes getVarRoutes() {
        return vr;
    }

    @Override
    public String name() {
        return "TravelTimeManager";
    }
}
