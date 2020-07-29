package com.socolabs.mo.vrplib.functions.div;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;

import java.util.HashSet;

public class DivConstantFunction implements IVRPFunction {

    private IVRPFunction f;
    private double v;

    private double value;
    private double tmpValue;

    public DivConstantFunction(double v, IVRPFunction f) {
        this.f = f;
        this.v = v;
        f.getVarRoutes().post(this);
        if (f.getValue() != 0) {
            tmpValue = value = v / f.getValue();
        } else {
            tmpValue = value = 0;
        }
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
        if (f.getTmpValue() != 0) {
            tmpValue = v / f.getTmpValue();
        } else {
            tmpValue = 0;
        }
    }

    @Override
    public void propagate() {
        value = tmpValue;
    }

    @Override
    public void clearTmpData() {
        tmpValue = value;
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
        return f.getVarRoutes();
    }

    @Override
    public HashSet<VRPPoint> getIndependentPoints() {
        return f.getIndependentPoints();
    }

    private int stt;

    @Override
    public int getStt() {
        return this.stt;
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
        return "DivConstantFunction";
    }
}
