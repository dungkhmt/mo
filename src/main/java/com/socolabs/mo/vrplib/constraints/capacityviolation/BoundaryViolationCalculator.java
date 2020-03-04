package com.socolabs.mo.vrplib.constraints.capacityviolation;

import com.socolabs.mo.vrplib.core.RouteVRP;
import com.socolabs.mo.vrplib.core.VarRoutesVRP;

import java.util.HashMap;

public class BoundaryViolationCalculator implements ICVCalculator {

    private double[] lower;
    private double[] upper;

    private HashMap<String, Double> mTruck2LowerWeight;
    private HashMap<String, Double> mTruck2UpperWeight;


    public BoundaryViolationCalculator(VarRoutesVRP vr, HashMap<String, Double> mTruck2LowerWeight, HashMap<String, Double> mTruck2UpperWeight) {
        this.mTruck2LowerWeight = mTruck2LowerWeight;
        this.mTruck2UpperWeight = mTruck2UpperWeight;
        int maxId = 0;
        for (RouteVRP r : vr.getAllRoutes()) {
            maxId = Math.max(maxId, r.getId());
        }
        lower = new double[maxId + 1];
        upper = new double[maxId + 1];
    }

    @Override
    public double getViolations(double sum, RouteVRP route) {
        int id = route.getId();
        double vi = Math.max(lower[id] - sum, sum - upper[id]);
        if (vi > 0) {
            return 100 * vi / (upper[id] - lower[id]);
        }
        return 0;
    }

    @Override
    public void propagateAddRoute(RouteVRP route) {
        if (route.getId() >= lower.length) {
            int t = route.getId() / lower.length + 1;
            double[] newArr = new double[t * lower.length];
            System.arraycopy(lower, 0, newArr, 0, lower.length);
            lower = newArr;
            newArr = new double[t * upper.length];
            System.arraycopy(upper, 0, newArr, 0, upper.length);
            upper = newArr;
        }
        lower[route.getId()] = mTruck2LowerWeight.get(route.getTruckCode());
        upper[route.getId()] = mTruck2UpperWeight.get(route.getTruckCode());
    }
}
