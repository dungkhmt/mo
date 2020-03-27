package com.socolabs.mo.vrplib.constraints.leq;

import com.socolabs.mo.vrplib.core.*;

import java.util.ArrayList;
import java.util.HashSet;

public class Leq implements IVRPFunction {

    private IVRPFunction f;

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
