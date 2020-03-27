package com.socolabs.mo.vrplib.functions.sum;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;

import java.util.HashSet;

public class SumAccumulatedWeightPoints implements IVRPFunction {
    private VRPVarRoutes vr;
    private AccumulatedWeightPoints acc;

    private double value;
    private double tmpValue;

    public SumAccumulatedWeightPoints(AccumulatedWeightPoints acc) {
        vr = acc.getVarRoutes();
        this.acc = acc;
        init();
    }

    private void init() {
        value = 0;
        for (VRPRoute route : vr.getAllRoutes()) {
            value += acc.getWeightValueOfPoint(route.getEndPoint());
        }
        tmpValue = value;
        vr.post(this);
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
        tmpValue = value;
        for (VRPRoute route : vr.getChangedRoutes()) {
            tmpValue -= acc.getWeightValueOfPoint(route.getEndPoint());
            tmpValue += acc.getTmpWeightValueOfPoint(route.getEndPoint());
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
        return "SumAccumulatedWeightPoints";
    }
}
