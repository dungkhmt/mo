package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;

public class SBArrivalTimeCalculator implements IAccumulatedCalculator {

    private VRPVarRoutes vr;
    private IDistanceManager travelTimeManager;
    private INodeWeightManager serviceTimeManager;
    private int earliestPickupTime;

    public SBArrivalTimeCalculator(IDistanceManager travelTimeManager, INodeWeightManager serviceTimeManager, int earliestPickupTime) {
        this.vr = travelTimeManager.getVarRoutes();
        this.travelTimeManager = travelTimeManager;
        this.serviceTimeManager = serviceTimeManager;
        this.earliestPickupTime = earliestPickupTime;
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        VRPPoint prev = point.getPrev();
        if (prev != null) {
            if (prev.isStartPoint()) {
                return earliestPickupTime;
            } else {
                return prevValue + travelTimeManager.getDistance(prev, point) + serviceTimeManager.getWeight(prev);
            }
        }
        return 0;
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        VRPPoint prev = point.getTmpPrev();
        if (prev != null) {
            if (prev.isStartPoint()) {
                return earliestPickupTime;
            } else {
                return prevValue + travelTimeManager.getTmpDistance(prev, point) + serviceTimeManager.getTmpWeight(prev);
            }
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
        return "DMArrivalTimeCalculator";
    }
}
