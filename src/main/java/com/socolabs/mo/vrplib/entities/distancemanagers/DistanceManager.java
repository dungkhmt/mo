package com.socolabs.mo.vrplib.entities.distancemanagers;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.util.HashMap;

public class DistanceManager  implements IDistanceManager {

    private VRPVarRoutes vr;
    private HashMap<String, HashMap<String, Double>> distanceMap;
    private double[][] travelTimeMatrix;

    public DistanceManager(VRPVarRoutes vr, HashMap<String, HashMap<String, Double>> distanceMap) {
        this.vr = vr;
        this.distanceMap = distanceMap;
        int maxStt = 0;
        for (VRPPoint p : vr.getAllPoints()) {
            maxStt = Math.max(maxStt, p.getStt());
        }
        travelTimeMatrix = new double[maxStt + 1][maxStt + 1];
        for (VRPPoint x : vr.getAllPoints()) {
            for (VRPPoint y : vr.getAllPoints()) {
                try {
                    travelTimeMatrix[x.getStt()][y.getStt()] = distanceMap.get(x.getLocationCode()).get(y.getLocationCode());
                } catch (NullPointerException e) {
                    travelTimeMatrix[x.getStt()][y.getStt()] = CBLSVRP.MAX_INT;
                }
            }
        }
    }

    @Override
    public double getDistance(VRPPoint x, VRPPoint y) {
        if (x == null || y == null) {
            return 0;
        }
        return travelTimeMatrix[x.getStt()][y.getStt()];//distanceMap.get(x.getLocationCode()).get(y.getLocationCode());
    }

    @Override
    public double getTmpDistance(VRPPoint x, VRPPoint y) {
        if (x == null || y == null) {
            return 0;
        }
        return travelTimeMatrix[x.getStt()][y.getStt()];//distanceMap.get(x.getLocationCode()).get(y.getLocationCode());
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
        return "DistanceManager";
    }
}
