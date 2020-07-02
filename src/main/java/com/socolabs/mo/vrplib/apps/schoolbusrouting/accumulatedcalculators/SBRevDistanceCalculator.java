package com.socolabs.mo.vrplib.apps.schoolbusrouting.accumulatedcalculators;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;

public class SBRevDistanceCalculator  implements IAccumulatedCalculator {

    private VRPVarRoutes vr;
    private IDistanceManager distanceManager;

    public SBRevDistanceCalculator(IDistanceManager distanceManager) {
        this.vr = distanceManager.getVarRoutes();
        this.distanceManager = distanceManager;
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + distanceManager.getDistance(point, point.getNext());
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + distanceManager.getTmpDistance(point, point.getTmpNext());
    }

    @Override
    public void createPoint(VRPPoint point) {

    }

    @Override
    public void removePoint(VRPPoint point) {

    }

    @Override
    public void createRoute(VRPRoute route) {

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
        return "SBRevDistanceCalculator";
    }
}
