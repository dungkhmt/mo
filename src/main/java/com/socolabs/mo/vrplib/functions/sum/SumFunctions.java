package com.socolabs.mo.vrplib.functions.sum;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;

import java.util.ArrayList;
import java.util.HashSet;

public class SumFunctions implements IVRPFunction {

    private VRPVarRoutes vr;
    private ArrayList<IVRPFunction> functions;

    private double value;
    private double tmpValue;

    public SumFunctions(ArrayList<IVRPFunction> functions) {
        vr = functions.get(0).getVarRoutes();
        this.functions = functions;
        vr.post(this);
        init();
    }

    private void init() {
        value = 0;
        for (IVRPFunction f : functions) {
            value += f.getValue();
        }
        tmpValue = value;
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
        tmpValue = 0;
        for (IVRPFunction f : functions) {
            tmpValue += f.getTmpValue();
        }
    }

    @Override
    public void propagate() {
        value = tmpValue;
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
        if (value != tmpValue) {
            System.out.println("EXCEPTION::" + name() + " -> value != tmpValue");
            return false;
        }
        return true;
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
        return vr;
    }

    @Override
    public String name() {
        return "SumFunctions";
    }
}
