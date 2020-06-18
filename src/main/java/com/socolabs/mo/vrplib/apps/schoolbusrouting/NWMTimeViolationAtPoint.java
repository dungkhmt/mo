package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;

public class NWMTimeViolationAtPoint implements INodeWeightManager {

    private VRPVarRoutes vr;
    private RevAccumulatedWeightPoints revAccTravelTime;

    public NWMTimeViolationAtPoint(VRPVarRoutes vr, RevAccumulatedWeightPoints revAccTravelTime) {
        this.vr = vr;
        this.revAccTravelTime = revAccTravelTime;
    }

    @Override
    public double getWeight(VRPPoint point) {
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        return Math.max(0, revAccTravelTime.getWeightValueOfPoint(point) - p.getTotalTravelTimeLimit());
    }

    @Override
    public double getTmpWeight(VRPPoint point) {
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        return Math.max(0, revAccTravelTime.getTmpWeightValueOfPoint(point) - p.getTotalTravelTimeLimit());
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
        return "NWMTimeViolationAtPoint";
    }
}


