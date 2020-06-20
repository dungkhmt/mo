package com.socolabs.mo.vrplib.functions;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;

@Getter
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
    public void clearTmpData() {

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
        return this.stt;
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
