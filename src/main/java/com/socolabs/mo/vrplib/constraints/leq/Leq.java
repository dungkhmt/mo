package com.socolabs.mo.vrplib.constraints.leq;

import com.socolabs.mo.vrplib.core.IVRPConstraint;
import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;

import java.util.ArrayList;
import java.util.HashSet;

public class Leq implements IVRPConstraint {

    private IVRPConstraint f;

    private int stt;

    public Leq(IVRPFunction f, double v) {
        this.f = new LeqFunctionConstant(f, v);
    }

    public Leq(double v, IVRPFunction f) {
        this.f = new LeqConstantFunction(v, f);
    }

    @Override
    public double getValue() {
        return f.getValue();
    }

    @Override
    public double getTmpValue() {
        return f.getTmpValue();
    }

    @Override
    public void explore() {

    }

    @Override
    public void propagate() {

    }

    @Override
    public VRPVarRoutes getVarRoutes() {
        return f.getVarRoutes();
    }

    @Override
    public HashSet<VRPPoint> getIndependentPoints() {
        return null;
    }

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
        return "Leq";
    }
}
