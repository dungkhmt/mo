package com.socolabs.mo.vrplib.constraints.capacity;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;
import com.socolabs.mo.vrplib.entities.nodeweightmanagers.NodeWeightManager;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CapacityConstraint implements IVRPFunction {

    private VRPVarRoutes vr;
    private INodeWeightManager nodeWeightManager;
    private HashMap<VRPRoute, Double> mRoute2Capacity;
    private double[] capacities;

    private double violations;
    private double tmpViolations;
    private double[] totalWeightOfRoutes;
    private double[] tmpTotalWeightOfRoutes;

    private HashSet<VRPRoute> changedRoutes;

    public CapacityConstraint(VRPVarRoutes vr, INodeWeightManager nodeWeightManager, HashMap<VRPRoute, Double> mRoute2Capacity) {
        this.vr = vr;
        this.nodeWeightManager = nodeWeightManager;
        this.mRoute2Capacity = mRoute2Capacity;
        init();
    }

    public CapacityConstraint(VRPVarRoutes vr, HashMap<VRPPoint, Double> nodeWeightMap, HashMap<VRPRoute, Double> mRoute2Capacity) {
        this.vr = vr;
        this.nodeWeightManager = new NodeWeightManager(vr, nodeWeightMap);
        this.mRoute2Capacity = mRoute2Capacity;
        init();
    }

    private void init() {
        vr.post(this);
        int maxStt = 0;
        for (VRPRoute route : mRoute2Capacity.keySet()) {
            maxStt = Math.max(maxStt, route.getStt());
        }
        violations = 0;
        capacities = new double[maxStt + 1];
        totalWeightOfRoutes = new double[maxStt + 1];
        tmpTotalWeightOfRoutes = new double[maxStt + 1];
        for (Map.Entry<VRPRoute, Double> e : mRoute2Capacity.entrySet()) {
            VRPRoute route = e.getKey();
            int stt = route.getStt();
            capacities[stt] = e.getValue();
            totalWeightOfRoutes[stt] = 0;
            VRPPoint p = route.getStartPoint();
            while (p != null) {
                totalWeightOfRoutes[stt] += nodeWeightManager.getWeight(p);
                p = p.getNext();
            }
            violations += getViolation(capacities[stt], totalWeightOfRoutes[stt]);
            tmpTotalWeightOfRoutes[stt] = totalWeightOfRoutes[stt];
        }
        tmpViolations = violations;
        changedRoutes = new HashSet<>();
    }

    private double getViolation(double capacity, double totalWeight) {
        return Math.max(0, (totalWeight - capacity) / capacity * 100);
    }

    public void clearTmpData() {
        tmpViolations = violations;
        for (VRPRoute route : changedRoutes) {
            int stt = route.getStt();
            tmpTotalWeightOfRoutes[stt] = totalWeightOfRoutes[stt];
        }
        changedRoutes.clear();
    }

    @Override
    public double getValue() {
        return violations;
    }

    @Override
    public double getTmpValue() {
        return tmpViolations;
    }

    @Override
    public void explore() {
//        clearTmpData();
        for (Map.Entry<VRPRoute, ArrayList<VRPPoint>> e : vr.getMChangedRouteToAddedPoints().entrySet()) {
            VRPRoute route = e.getKey();
            ArrayList<VRPPoint> addedPoints = e.getValue();
            changedRoutes.add(route);
            int stt = route.getStt();
            for (VRPPoint point : addedPoints) {
                tmpTotalWeightOfRoutes[stt] += nodeWeightManager.getTmpWeight(point);
            }
        }
        for (Map.Entry<VRPRoute, ArrayList<VRPPoint>> e : vr.getMChangedRouteToRemovedPoints().entrySet()) {
            VRPRoute route = e.getKey();
            ArrayList<VRPPoint> removedPoints = e.getValue();
            changedRoutes.add(route);
            int stt = route.getStt();
            for (VRPPoint point : removedPoints) {
                tmpTotalWeightOfRoutes[stt] -= nodeWeightManager.getTmpWeight(point);
            }
        }
        for (VRPRoute route : changedRoutes) {
            int stt = route.getStt();
            tmpViolations -= getViolation(capacities[stt], totalWeightOfRoutes[stt]);
            tmpViolations += getViolation(capacities[stt], tmpTotalWeightOfRoutes[stt]);
        }
    }

    @Override
    public void propagate() {
        for (VRPRoute route : changedRoutes) {
            int stt = route.getStt();
            totalWeightOfRoutes[stt] = tmpTotalWeightOfRoutes[stt];
        }
        violations = tmpViolations;
    }

    @Override
    public HashSet<VRPPoint> getIndependentPoints() {
        return null;
    }

    private int stt;
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
        double verifiyingViolations = 0;
        for (Map.Entry<VRPRoute, Double> e : mRoute2Capacity.entrySet()) {
            VRPRoute route = e.getKey();
            double totalWeight = 0;
            VRPPoint p = route.getStartPoint();
            while (p != null) {
                totalWeight += nodeWeightManager.getWeight(p);
                p = p.getNext();
            }
            if (Math.abs(totalWeight - totalWeightOfRoutes[route.getStt()]) > CBLSVRP.EPS) {
                p = route.getStartPoint();
                System.out.println(route);
                while (p != null) {
                    System.out.println(p + " -> " + nodeWeightManager.getWeight(p));
                    totalWeight += nodeWeightManager.getWeight(p);
                    p = p.getNext();
                }
                System.out.println(totalWeightOfRoutes[route.getStt()]);
                System.out.println("EXCEPTION::" + name() + " -> calculating weight of routes is incorrect");
                return false;
            }
            verifiyingViolations += getViolation(mRoute2Capacity.get(route), totalWeight);
        }
        if (Math.abs(verifiyingViolations - violations) > CBLSVRP.EPS) {
            System.out.println("EXCEPTION::" + name() + " -> verifiyingViolations != violations");
            return false;
        }
        return true;
    }

    @Override
    public void createPoint(VRPPoint point) {

    }

    @Override
    public void removePoint(VRPPoint point) {

    }

    @Override
    public void createRoute(VRPRoute route) {
        int stt = route.getStt();
        if (stt > capacities.length) {
            int len = capacities.length;
            int newLen = (stt / len + 1) * len;
            double[] newArr = new double[newLen];
            System.arraycopy(capacities, 0, newArr, 0, capacities.length);
            capacities = newArr;
            newArr = new double[newLen];
            System.arraycopy(totalWeightOfRoutes, 0, newArr, 0, totalWeightOfRoutes.length);
            totalWeightOfRoutes = newArr;
            newArr = new double[newLen];
            System.arraycopy(tmpTotalWeightOfRoutes, 0, newArr, 0, tmpTotalWeightOfRoutes.length);
            tmpTotalWeightOfRoutes = newArr;
        }
    }

    public void setCapacity(VRPRoute route, double capacity) {
        capacities[route.getStt()] = capacity;
        mRoute2Capacity.put(route, capacity);
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
        return "CapacityConstraint";
    }
}
