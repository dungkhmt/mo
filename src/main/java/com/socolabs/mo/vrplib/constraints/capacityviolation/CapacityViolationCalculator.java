package com.socolabs.mo.vrplib.constraints.capacityviolation;

import com.socolabs.mo.vrplib.core.RouteVRP;
import com.socolabs.mo.vrplib.core.VarRoutesVRP;

import java.util.HashMap;

public class CapacityViolationCalculator implements ICVCalculator {

    private double[] capacity;
    private HashMap<String, Double> mTruck2Capacity;

    public CapacityViolationCalculator(VarRoutesVRP vr, HashMap<String, Double> mTruck2Capacity) {
        this.mTruck2Capacity = mTruck2Capacity;
        int maxId = 0;
        for (RouteVRP r : vr.getAllRoutes()) {
            maxId = Math.max(maxId, r.getId());
        }
        capacity = new double[maxId + 1];
    }

    @Override
    public double getViolations(double sum, RouteVRP route) {
        double cap = capacity[route.getId()];
        if (sum > cap) {
            return 100 * (sum - cap) / cap;
        }
        return 0;
    }

    @Override
    public void propagateAddRoute(RouteVRP route) {
        if (route.getId() >= capacity.length) {
            int t = route.getId() / capacity.length + 1;
            double[] newArr = new double[t * capacity.length];
            System.arraycopy(capacity, 0, newArr, 0, capacity.length);
            capacity = newArr;
        }
        capacity[route.getId()] = mTruck2Capacity.get(route.getTruckCode());
    }
}
