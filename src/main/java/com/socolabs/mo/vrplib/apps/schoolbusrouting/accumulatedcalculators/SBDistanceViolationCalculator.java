package com.socolabs.mo.vrplib.apps.schoolbusrouting.accumulatedcalculators;

import com.socolabs.mo.vrplib.apps.schoolbusrouting.SchoolBusPickupPoint;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;

public class SBDistanceViolationCalculator  implements IAccumulatedCalculator {

    private VRPVarRoutes vr;

    public SBDistanceViolationCalculator(VRPVarRoutes vr) {
        this.vr = vr;
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        VRPPoint prev = point.getPrev();
        if (prev != null) {
            if (prev.isStartPoint()) {
                return prevValue;
            } else {
                SchoolBusPickupPoint pp = (SchoolBusPickupPoint) point;
                SchoolBusPickupPoint pprev = (SchoolBusPickupPoint) prev;
                return prevValue + Math.max(0, pp.getHaversineDistanceToSchool() - pprev.getHaversineDistanceToSchool());
            }
        }
        return 0;
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        VRPPoint prev = point.getTmpPrev();
        if (prev != null) {
            if (prev.isStartPoint()) {
                return prevValue;
            } else {
                SchoolBusPickupPoint pp = (SchoolBusPickupPoint) point;
                SchoolBusPickupPoint pprev = (SchoolBusPickupPoint) prev;
                return prevValue + Math.max(0, pp.getHaversineDistanceToSchool() - pprev.getHaversineDistanceToSchool());
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
        return "SBDistanceViolationCalculator";
    }
}
