package com.socolabs.mo.vrplib.functions;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;

import java.util.HashSet;

public class NumberPointsOnRoute implements IVRPFunction {

    private VRPVarRoutes vr;
    private VRPRoute route;

    public NumberPointsOnRoute(VRPVarRoutes vr, VRPRoute route) {
        this.vr = vr;
        this.route = route;
        vr.post(this);
    }

    @Override
    public double getValue() {
        return route.getNbPoints();
    }

    @Override
    public double getTmpValue() {
        return route.getTmpNbPoints();
    }

    @Override
    public void explore() {

    }

    @Override
    public void propagate() {

    }

    @Override
    public void clearTmpData() {

    }

    @Override
    public HashSet<VRPPoint> getIndependentPoints() {
        return null;
    }

    private int stt;

    @Override
    public int getStt() {
        return stt;
    }

    @Override
    public void setStt(int stt) {
        this.stt = stt;
    }

    @Override
    public boolean verify() {
        return true;
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
        return "NumberPointsOnRoute";
    }
}
