package com.socolabs.mo.vrplib.functions;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;

import java.util.ArrayList;
import java.util.HashSet;

public class AccumulatedPointWeightsOnPath implements IVRPFunction {

    private AccumulatedWeightPoints accWeightPoints;
    private VRPPoint t;

    private int stt;

    public AccumulatedPointWeightsOnPath(AccumulatedWeightPoints accWeightPoints, VRPPoint t) {
        this.accWeightPoints = accWeightPoints;
        this.t = t;
    }

    @Override
    public double getValue() {
        return accWeightPoints.getWeightValueOfPoint(t);
    }

    @Override
    public double getTmpValue() {
        return accWeightPoints.getTmpWeightValueOfPoint(t);
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
        return accWeightPoints.getVarRoutes();
    }

    @Override
    public HashSet<VRPPoint> getIndependentPoints() {
        HashSet<VRPPoint> points = new HashSet<>();
        points.add(t);
        return points;
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
        return "AccumulatedPointWeightsOnPath";
    }
}
