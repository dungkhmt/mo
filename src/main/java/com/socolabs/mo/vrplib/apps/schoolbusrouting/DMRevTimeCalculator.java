package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.entities.IDistanceManager;

public class DMRevTimeCalculator implements IAccumulatedCalculator {

    private VRPVarRoutes vr;
    private IDistanceManager travelTimeManager;

    public DMRevTimeCalculator(IDistanceManager travelTimeManager) {
        this.vr = travelTimeManager.getVarRoutes();
        this.travelTimeManager = travelTimeManager;
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + travelTimeManager.getDistance(point, point.getNext());
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + travelTimeManager.getTmpDistance(point, point.getTmpNext());
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
        return "DMRevTimeCalculator";
    }
}
