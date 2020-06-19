package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.entities.IDistanceManager;

public class DMRoadBlockViolationCalculator implements IAccumulatedCalculator {

    private VRPVarRoutes vr;
    private IDistanceManager roadBlockManager;

    public DMRoadBlockViolationCalculator(IDistanceManager roadBlockManager) {
        vr = roadBlockManager.getVarRoutes();
        this.roadBlockManager = roadBlockManager;
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        VRPPoint prev = point.getPrev();
        if (prev != null) {
            if (prev.isStartPoint()) {
                return prevValue;
            }
        }
        int roadBlock = (int) roadBlockManager.getDistance(prev, point);
        int roadBlockCap = SBUtils.getRoadBloakCap(roadBlock);
        SchoolBusRoute schoolBusRoute = (SchoolBusRoute) point.getRoute();
        if (schoolBusRoute != null) {
            return prevValue + Math.max(0, roadBlockCap - schoolBusRoute.getCapacity());
        }
        return prevValue;
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        VRPPoint prev = point.getTmpPrev();
        if (prev != null) {
            if (prev.isStartPoint()) {
                return prevValue;
            }
        }
        int roadBlock = (int) roadBlockManager.getTmpDistance(prev, point);
        int roadBlockCap = SBUtils.getRoadBloakCap(roadBlock);
        SchoolBusRoute schoolBusRoute = (SchoolBusRoute) point.getRoute();
        if (schoolBusRoute != null) {
            return prevValue + Math.max(0, roadBlockCap - schoolBusRoute.getCapacity());
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
        return "DMRoadBlockViolationCalculator";
    }
}
