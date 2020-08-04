package com.socolabs.mo.vrplib.apps.schoolbusrouting.accumulatedcalculators;

import com.socolabs.mo.vrplib.apps.schoolbusrouting.SchoolBusPickupPoint;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;

public class SBNumberVisitedPointsCalculator implements IAccumulatedCalculator {

    private VRPVarRoutes vr;

    public SBNumberVisitedPointsCalculator(VRPVarRoutes vr) {
        this.vr = vr;
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        if (!point.isDepot()) {
            return prevValue + ((SchoolBusPickupPoint) point).getRequests().size();
        } else {
            return prevValue;
        }
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        if (!point.isDepot()) {
            return prevValue + ((SchoolBusPickupPoint) point).getRequests().size();
        } else {
            return prevValue;
        }
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
        return "SBNumberVisitedPointsCalculator";
    }
}
