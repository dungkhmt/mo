package com.socolabs.mo.vrplib.entities.distancemanagers;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;

import java.util.HashMap;

public class TravelTimeManager implements IDistanceManager {

    private VRPVarRoutes vr;
    private HashMap<String, HashMap<String, Integer>> travelTimeMap;
    private int[][] travelTimeMatrix;

    public TravelTimeManager(VRPVarRoutes vr, HashMap<String, HashMap<String, Integer>> travelTimeMap) {
        this.vr = vr;
        this.travelTimeMap = travelTimeMap;
        int maxStt = 0;
        for (VRPPoint p : vr.getAllPoints()) {
            maxStt = Math.max(maxStt, p.getStt());
        }
        travelTimeMatrix = new int[maxStt + 1][maxStt + 1];
        for (VRPPoint x : vr.getAllPoints()) {
            for (VRPPoint y : vr.getAllPoints()) {
                travelTimeMatrix[x.getStt()][y.getStt()] = travelTimeMap.get(x.getLocationCode()).get(y.getLocationCode());
            }
        }
    }

    @Override
    public double getDistance(VRPPoint x, VRPPoint y) {
        if (x == null) {
            return 0;
        }
        return travelTimeMatrix[x.getStt()][y.getStt()];//travelTimeMap.get(x.getLocationCode()).get(y.getLocationCode());
    }

    @Override
    public double getTmpDistance(VRPPoint x, VRPPoint y) {
        if (x == null) {
            return 0;
        }
        return travelTimeMatrix[x.getStt()][y.getStt()];//travelTimeMap.get(x.getLocationCode()).get(y.getLocationCode());
    }

    public void setTravelTimeArc(VRPPoint x, VRPPoint y, int travelTime) {
        // to do
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
        return vr;
    }

    @Override
    public String name() {
        return "TravelTimeManager";
    }
}
