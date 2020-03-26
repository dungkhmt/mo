package com.socolabs.mo.vrplib.entities.nodeweightmanagers;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;

import java.util.HashMap;
import java.util.Map;

public class ServiceTimeManager implements INodeWeightManager {

    private VRPVarRoutes vr;
    private int[] serviceTimes;

    public ServiceTimeManager(VRPVarRoutes vr, HashMap<VRPPoint, Integer> serviceTimeMap) {
        this.vr = vr;
        int maxStt = 0;
        for (VRPPoint p : serviceTimeMap.keySet()) {
            maxStt = Math.max(maxStt, p.getStt());
        }
        serviceTimes = new int[maxStt + 1];
        for (Map.Entry<VRPPoint, Integer> e : serviceTimeMap.entrySet()) {
            int stt = e.getKey().getStt();
            serviceTimes[stt] = e.getValue();
        }
        vr.post(this);
    }

    @Override
    public double getWeight(VRPPoint point) {
        return serviceTimes[point.getStt()];
    }

    @Override
    public double getTmpWeight(VRPPoint point) {
        return serviceTimes[point.getStt()];
    }

    @Override
    public void addNewPoint(VRPPoint point) {
        int stt = point.getStt();
        if (stt >= serviceTimes.length) {
            int len = serviceTimes.length;
            int newLen = (stt / len + 1) * len;
            int[] newArr = new int[newLen];
            System.arraycopy(serviceTimes, 0, newArr, 0, serviceTimes.length);
            serviceTimes = newArr;
        }
    }

    public void setServiceTime(VRPPoint point, int serviceTime) {
        serviceTimes[point.getStt()] = serviceTime;
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
        return "ServiceTimeManager";
    }
}
