package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;

public class NWMTravelTimeObjective  implements INodeWeightManager {

    private VRPVarRoutes vr;
    private RevAccumulatedWeightPoints revAccTravelTime;

    public NWMTravelTimeObjective(VRPVarRoutes vr, RevAccumulatedWeightPoints revAccTravelTime) {
        this.vr = vr;
        this.revAccTravelTime = revAccTravelTime;
    }

    @Override
    public double getWeight(VRPPoint point) {
        if (point.isStartPoint()) {
            return revAccTravelTime.getWeightValueOfPoint(point.getNext());
        }
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        return Math.max(0, revAccTravelTime.getWeightValueOfPoint(point) - p.getDirectTravelTimeToSchool());
    }

    @Override
    public double getTmpWeight(VRPPoint point) {
        if (point.isStartPoint()) {
            return revAccTravelTime.getTmpWeightValueOfPoint(point.getTmpNext());
        }
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        return Math.max(0, revAccTravelTime.getTmpWeightValueOfPoint(point) - p.getDirectTravelTimeToSchool());
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
        return "NWMTravelTimeObjective";
    }
}



