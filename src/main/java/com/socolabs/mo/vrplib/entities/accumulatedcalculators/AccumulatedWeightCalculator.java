package com.socolabs.mo.vrplib.entities.accumulatedcalculators;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;

import java.util.HashMap;
import java.util.Map;

public class AccumulatedWeightCalculator implements IAccumulatedCalculator {

    private VRPVarRoutes vr;
    private HashMap<VRPPoint, Double> weightMap;
    private double[] weights;

    public AccumulatedWeightCalculator(VRPVarRoutes vr, HashMap<VRPPoint, Double> weightMap) {
        this.vr = vr;
        this.weightMap = weightMap;
        int maxStt = 0;
        for (VRPPoint p : vr.getAllPoints()) {
            maxStt = Math.max(maxStt, p.getStt());
        }
        weights = new double[maxStt + 1];
        for (Map.Entry<VRPPoint, Double> e : weightMap.entrySet()) {
            weights[e.getKey().getStt()] = e.getValue();
        }
        vr.post(this);
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + weights[point.getStt()];
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + weights[point.getStt()];
    }

    public void setWeight(VRPPoint point, double w) {
        weights[point.getStt()] = w;
    }

    @Override
    public void createPoint(VRPPoint point) {
        int stt = point.getStt();
        if (stt >=  weights.length) {
            int len =  weights.length;
            int newLen = (stt / len + 1) * len;
            double[] newArr = new double[newLen];
            System.arraycopy( weightMap, 0, newArr, 0,  weights.length);
            weights = newArr;
        }
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
        return vr;
    }

    @Override
    public String name() {
        return "AccumulatedWeightCalculator";
    }
}
