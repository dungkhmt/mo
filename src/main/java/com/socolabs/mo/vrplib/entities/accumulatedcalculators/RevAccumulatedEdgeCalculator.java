package com.socolabs.mo.vrplib.entities.accumulatedcalculators;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.entities.IDistanceManager;

public class RevAccumulatedEdgeCalculator implements IAccumulatedCalculator {

    private IDistanceManager distancer;

    public RevAccumulatedEdgeCalculator(IDistanceManager distancer) {
        this.distancer = distancer;
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + distancer.getDistance(point, point.getNext());
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + distancer.getTmpDistance(point, point.getTmpNext());
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
        return distancer.getVarRoutes();
    }

    @Override
    public String name() {
        return "RevAccumulatedEdgeCalculator";
    }
}
