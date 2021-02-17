package com.socolabs.mo.components.parcelcollection;

import com.dailyopt.vrp.core.VRPPoint;
import com.dailyopt.vrp.core.VRPRoute;
import com.dailyopt.vrp.core.VRPVarRoutes;
import com.dailyopt.vrp.entities.IDistanceManager;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;

import java.util.HashMap;

public class PCDistanceManager implements IDistanceManager {

    public final static double INF = 1e10;

    private VRPVarRoutes vr;
    private HashMap<Vertex, HashMap<Vertex, Double>> distanceMatrix;

    public PCDistanceManager(VRPVarRoutes vr) {
        this.vr = vr;
        distanceMatrix = new HashMap<>();
        vr.post(this);
    }

    public void setDistanceElement(Vertex x, Vertex y, double dist) {
        distanceMatrix.get(x).put(y, dist);
    }

    @Override
    public void createPoint(VRPPoint point) {
        PCPoint p = (PCPoint) point;
        if (!distanceMatrix.containsKey(p.getV())) {
            distanceMatrix.put(p.getV(), new HashMap<>());
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
        return "PCDistanceManager";
    }

    @Override
    public double getDistance(VRPPoint x, VRPPoint y) {
        if (x == null || y == null) {
            return 0;
        }
        PCPoint px = (PCPoint) x;
        PCPoint py = (PCPoint) y;
        if (!distanceMatrix.get(px.getV()).containsKey(py.getV())) {
            return INF;
        }
        return distanceMatrix.get(px.getV()).get(py.getV());
    }

    @Override
    public double getTmpDistance(VRPPoint x, VRPPoint y) {
        if (x == null || y == null) {
            return 0;
        }
        PCPoint px = (PCPoint) x;
        PCPoint py = (PCPoint) y;
        return distanceMatrix.get(px.getV()).get(py.getV());
    }
}
