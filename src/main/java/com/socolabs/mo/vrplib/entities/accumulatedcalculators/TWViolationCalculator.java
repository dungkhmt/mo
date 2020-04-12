package com.socolabs.mo.vrplib.entities.accumulatedcalculators;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;

import java.util.HashMap;
import java.util.Map;

public class TWViolationCalculator implements IAccumulatedCalculator {

    private VRPVarRoutes vr;
    private AccumulatedWeightPoints arrivalTimeCalculator;
    private int[] lastestArrivalTimes;

    public TWViolationCalculator(AccumulatedWeightPoints arrivalTimeCalculator, HashMap<VRPPoint, Integer> lastestArrivalTimeMap) {
        vr = arrivalTimeCalculator.getVarRoutes();
        this.arrivalTimeCalculator = arrivalTimeCalculator;
        int maxStt = 0;
        for (VRPPoint p : vr.getAllPoints()) {
            maxStt = Math.max(maxStt, p.getStt());
        }
        lastestArrivalTimes = new int[maxStt + 1];
        for (Map.Entry<VRPPoint, Integer> e : lastestArrivalTimeMap.entrySet()) {
            int stt = e.getKey().getStt();
            lastestArrivalTimes[stt] = e.getValue();
        }
        vr.post(this);
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + Math.max(0, arrivalTimeCalculator.getWeightValueOfPoint(point) - lastestArrivalTimes[point.getStt()]);
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        return prevValue + Math.max(0, arrivalTimeCalculator.getTmpWeightValueOfPoint(point) - lastestArrivalTimes[point.getStt()]);
    }

    @Override
    public void createPoint(VRPPoint point) {
        int stt = point.getStt();
        if (stt >= lastestArrivalTimes.length) {
            int len = lastestArrivalTimes.length;
            int newLen = (stt / len + 1) * len;
            int[] newArr = new int[newLen];
            System.arraycopy(lastestArrivalTimes, 0, newArr, 0, lastestArrivalTimes.length);
            lastestArrivalTimes = newArr;
        }
    }

    public void setLatestArrivalTime(VRPPoint point, int latestArrivalTime) {
        lastestArrivalTimes[point.getStt()] = latestArrivalTime;
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
        return "TWViolationCalculator";
    }
}
