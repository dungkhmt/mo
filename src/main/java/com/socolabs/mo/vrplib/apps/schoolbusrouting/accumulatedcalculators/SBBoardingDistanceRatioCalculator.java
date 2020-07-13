package com.socolabs.mo.vrplib.apps.schoolbusrouting.accumulatedcalculators;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.SchoolBusPickupPoint;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;

public class SBBoardingDistanceRatioCalculator implements IAccumulatedCalculator {

    private VRPVarRoutes vr;
    private RevAccumulatedWeightPoints revAccDistance;

    public SBBoardingDistanceRatioCalculator(RevAccumulatedWeightPoints revAccDistance) {
        this.vr = revAccDistance.getVarRoutes();
        this.revAccDistance = revAccDistance;
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        if (p.getDirectTravelTimeToSchool() > 0) {
            return Math.max(prevValue, revAccDistance.getWeightValueOfPoint(point) / p.getDirectDistanceToSchool());
        }
        return prevValue;
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        if (p.getDirectTravelTimeToSchool() > 0) {
            return Math.max(prevValue, revAccDistance.getTmpWeightValueOfPoint(point) / p.getDirectDistanceToSchool());
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
        return "SBBoardingDistanceRatioCalculator";
    }
}

