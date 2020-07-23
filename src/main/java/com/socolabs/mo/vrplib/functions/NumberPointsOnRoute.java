package com.socolabs.mo.vrplib.functions;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;

import java.util.HashSet;

public class NumberPointsOnRoute implements IVRPFunction {

    @Override
    public double getValue() {
        return 0;
    }

    @Override
    public double getTmpValue() {
        return 0;
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

    @Override
    public int getStt() {
        return 0;
    }

    @Override
    public void setStt(int stt) {

    }

    @Override
    public boolean verify() {
        return false;
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
        return null;
    }

    @Override
    public String name() {
        return null;
    }
}
