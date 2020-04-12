package com.socolabs.mo.vrplib.entities.accumulatedcalculators;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.entities.IDistanceManager;

import java.util.HashMap;
import java.util.Map;

public class ArrivalTimeCalculator implements IAccumulatedCalculator {

    private VRPVarRoutes vr;
    private IDistanceManager timer;
    private int[]  earliestArrivalTimes;

    public ArrivalTimeCalculator(IDistanceManager timer, HashMap<VRPPoint, Integer> earliestArrivalTimeMap) {
        this.vr = timer.getVarRoutes();
        this.timer = timer;
        int maxStt = 0;
        for (VRPPoint p : vr.getAllPoints()) {
            maxStt = Math.max(maxStt, p.getStt());
        }
        earliestArrivalTimes = new int[maxStt + 1];
        for (Map.Entry<VRPPoint, Integer> e : earliestArrivalTimeMap.entrySet()) {
            int stt = e.getKey().getStt();
            earliestArrivalTimes[stt] = e.getValue();
        }
        vr.post(this);
    }

    @Override
    public double caclAccWeightAtPoint(double prevValue, VRPPoint point) {
        return Math.max(prevValue + timer.getDistance(point.getPrev(), point),  earliestArrivalTimes[point.getStt()]);
    }

    @Override
    public double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point) {
        return Math.max(prevValue + timer.getDistance(point.getTmpPrev(), point),  earliestArrivalTimes[point.getStt()]);
    }

    @Override
    public void createPoint(VRPPoint point) {
        int stt = point.getStt();
        if (stt >=  earliestArrivalTimes.length) {
            int len =  earliestArrivalTimes.length;
            int newLen = (stt / len + 1) * len;
            int[] newArr = new int[newLen];
            System.arraycopy( earliestArrivalTimes, 0, newArr, 0,  earliestArrivalTimes.length);
             earliestArrivalTimes = newArr;
        }
    }

    public void setEarliestArrivalTimes(VRPPoint point, int earliestArrivalTime) {
         earliestArrivalTimes[point.getStt()] = earliestArrivalTime;
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
        return "ArrivalTimeCalculator";
    }
}
