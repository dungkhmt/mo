package com.socolabs.mo.vrplib.entities.weightmanagers;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IWeightManager;

import java.util.HashMap;

public class ServiceTimeManager implements IWeightManager {

    private VRPVarRoutes vr;
    private HashMap<VRPPoint, Integer> serviceTimeMap;

    public ServiceTimeManager(VRPVarRoutes vr, HashMap<VRPPoint, Integer> serviceTimeMap) {
        this.vr = vr;
        this.serviceTimeMap = serviceTimeMap;
    }

    @Override
    public double getWeight(VRPPoint point) {
        return serviceTimeMap.get(point);
    }

    @Override
    public double getTmpWeight(VRPPoint point) {
        return serviceTimeMap.get(point);
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
        return "ServiceTimeManager";
    }
}
