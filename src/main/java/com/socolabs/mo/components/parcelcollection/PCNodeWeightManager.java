package com.socolabs.mo.components.parcelcollection;

import com.dailyopt.vrp.core.VRPPoint;
import com.dailyopt.vrp.core.VRPRoute;
import com.dailyopt.vrp.core.VRPVarRoutes;
import com.dailyopt.vrp.entities.INodeWeightManager;

public class PCNodeWeightManager implements INodeWeightManager {

    private VRPVarRoutes vr;

    public PCNodeWeightManager(VRPVarRoutes vr) {
        this.vr = vr;
        vr.post(this);
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
        return "PCNodeWeightManager";
    }

    @Override
    public double getWeight(VRPPoint point) {
        PCPoint p = (PCPoint) point;
        return p.getWeight();
    }

    @Override
    public double getTmpWeight(VRPPoint point) {
        PCPoint p = (PCPoint) point;
        return p.getWeight();
    }
}
