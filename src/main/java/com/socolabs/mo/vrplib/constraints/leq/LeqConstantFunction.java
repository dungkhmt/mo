package com.socolabs.mo.vrplib.constraints.leq;

import com.socolabs.mo.vrplib.core.IVRPConstraint;
import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;

import java.util.HashSet;

public class LeqConstantFunction implements IVRPConstraint {

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
        return true;
    }

    @Override
    public String name() {
        return "LeqConstantFunction";
    }
}
