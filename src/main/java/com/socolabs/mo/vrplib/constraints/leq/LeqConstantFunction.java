package com.socolabs.mo.vrplib.constraints.leq;

import com.socolabs.mo.vrplib.core.*;

import java.util.HashSet;

public class LeqConstantFunction implements IVRPFunction {

    private IVRPFunction f;
    private double v;

    private double value;
    private double tmpValue;

    public LeqConstantFunction(double v, IVRPFunction f) {
        this.f = f;
        this.v = v;
        f.getVarRoutes().post(this);
        value = Math.max(0, f.getValue() - v);
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public double getTmpValue() {
        return tmpValue;
    }

    @Override
    public void explore() {
        tmpValue = Math.max(0, f.getTmpValue() - v);
    }

    @Override
    public void propagate() {
        value = tmpValue;
    }

    @Override
    public void addNewPoint(VRPPoint point) {

    }

    @Override
    public void removePoint(VRPPoint point) {

    }

    @Override
    public void addNewRoute(VRPRoute route) {

    }

    @Override
    public void removeRoute(VRPRoute route) {

    }

    @Override
    public VRPVarRoutes getVarRoutes() {
        return f.getVarRoutes();
    }

    @Override
    public HashSet<VRPPoint> getIndependentPoints() {
        return f.getIndependentPoints();
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
        if (value != tmpValue) {
            System.out.println("EXCEPTION::" + name() + " -> value != tmpValue");
            return false;
        }
        return true;
    }

    @Override
    public String name() {
        return "LeqConstantFunction";
    }
}
