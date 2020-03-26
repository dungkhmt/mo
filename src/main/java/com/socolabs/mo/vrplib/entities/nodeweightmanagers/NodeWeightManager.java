package com.socolabs.mo.vrplib.entities.nodeweightmanagers;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;

import java.util.HashMap;
import java.util.Map;

public class NodeWeightManager implements INodeWeightManager {

    private VRPVarRoutes vr;
    private double[] nodeWeights;

    public NodeWeightManager(VRPVarRoutes vr, HashMap<VRPPoint, Double> nodeWeightMap) {
        this.vr = vr;
        int maxStt = 0;
        for (VRPPoint p : nodeWeightMap.keySet()) {
            maxStt = Math.max(maxStt, p.getStt());
        }
        nodeWeights = new double[maxStt + 1];
        for (Map.Entry<VRPPoint, Double> e : nodeWeightMap.entrySet()) {
            int stt = e.getKey().getStt();
            nodeWeights[stt] = e.getValue();
        }
        vr.post(this);
    }

    @Override
    public double getWeight(VRPPoint point) {
        return nodeWeights[point.getStt()];
    }

    @Override
    public double getTmpWeight(VRPPoint point) {
        return nodeWeights[point.getStt()];
    }

    @Override
    public void addNewPoint(VRPPoint point) {
        int stt = point.getStt();
        if (stt >= nodeWeights.length) {
            int len = nodeWeights.length;
            int newLen = (stt / len + 1) * len;
            double[] newArr = new double[newLen];
            System.arraycopy(nodeWeights, 0, newArr, 0, nodeWeights.length);
            nodeWeights = newArr;
        }
    }

    public void setNodeWeight(VRPPoint point, double nodeWeight) {
        nodeWeights[point.getStt()] = nodeWeight;
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
        return "NodeWeightManager";
    }
}
