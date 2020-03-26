package com.socolabs.mo.vrplib.entities.distancemanagers;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.IWeightManager;

public class ServiceTravelTimeManager implements IDistanceManager {

    private VRPVarRoutes vr;
    private IDistanceManager travelTimeManager;
    private IWeightManager serviceTimeManager;

    public ServiceTravelTimeManager(IDistanceManager travelTimeManager, IWeightManager serviceTimeManager) {
        this.vr = travelTimeManager.getVarRoutes();
        this.travelTimeManager = travelTimeManager;
        this.serviceTimeManager = serviceTimeManager;
    }

    @Override
    public double getDistance(VRPPoint x, VRPPoint y) {
        return travelTimeManager.getDistance(x, y) + serviceTimeManager.getWeight(x);
    }

    @Override
    public double getTmpDistance(VRPPoint x, VRPPoint y) {
        return travelTimeManager.getTmpDistance(x, y) + serviceTimeManager.getTmpWeight(x);
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
        return "ServiceTravelTimeManager";
    }

}
