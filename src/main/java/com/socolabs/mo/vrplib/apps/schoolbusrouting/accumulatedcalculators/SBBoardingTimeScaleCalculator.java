package com.socolabs.mo.vrplib.apps.schoolbusrouting.accumulatedcalculators;

import com.socolabs.mo.vrplib.apps.schoolbusrouting.SchoolBusPickupPoint;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;

public class SBBoardingTimeScaleCalculator implements IAccumulatedCalculator {

    private VRPVarRoutes vr;
    private RevAccumulatedWeightPoints revAccTravelTime;

    public SBBoardingTimeScaleCalculator(RevAccumulatedWeightPoints revAccTravelTime) {
        this.vr = revAccTravelTime.getVarRoutes();
        this.revAccTravelTime = revAccTravelTime;
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        if (p.getDirectTravelTimeToSchool() > 0 && p.getIndex() == 1) {
            return Math.max(prevValue, (revAccTravelTime.getWeightValueOfPoint(point) - point.getRoute().getNbPoints() * 120) / p.getDirectTravelTimeToSchool());
        }
        return prevValue;
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        if (p.getDirectTravelTimeToSchool() > 0 && p.getTmpIndex() == 1) {
            return Math.max(prevValue, (revAccTravelTime.getTmpWeightValueOfPoint(point) - point.getTmpRoute().getTmpNbPoints() * 120) / p.getDirectTravelTimeToSchool());
        }
        return prevValue;
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
        return "SBBoardingTimeScaleCalculator";
    }
}

