package com.socolabs.mo.vrplib.entities.accumulatedcalculators;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;

import java.util.HashMap;
import java.util.Map;

public class AccumulatedNodeCalculator implements IAccumulatedCalculator {

    private VRPVarRoutes vr;
    private INodeWeightManager nodeWeightManager;

    public AccumulatedNodeCalculator(INodeWeightManager nodeWeightManager) {
        this.vr = nodeWeightManager.getVarRoutes();
        this.nodeWeightManager = nodeWeightManager;
        vr.post(this);
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + nodeWeightManager.getWeight(point);
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + nodeWeightManager.getTmpWeight(point);
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
        return "AccumulatedNodeCalculator::" + nodeWeightManager.name();
    }
}
