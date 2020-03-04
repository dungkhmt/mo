package com.socolabs.mo.vrplib.constraints;

import com.socolabs.mo.vrplib.constraints.capacityviolation.BoundaryViolationCalculator;
import com.socolabs.mo.vrplib.constraints.capacityviolation.CapacityViolationCalculator;
import com.socolabs.mo.vrplib.constraints.capacityviolation.ICVCalculator;
import com.socolabs.mo.vrplib.core.*;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightOnPathsVRP;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CapacityConstraintVRP implements IFunctionVRP {

    private VarRoutesVRP vr;
    private IWeightCaculatorVRP weightCaculator;
    private AccumulatedWeightOnPathsVRP weightOnPaths;

    private double violations;
    private ICVCalculator cvCalculator;

    public CapacityConstraintVRP(VarRoutesVRP vr,
                                 IWeightCaculatorVRP weightCaculator,
                                 HashMap<String, Double> mTruck2Capacity) {
        this.vr = vr;
        this.weightCaculator = weightCaculator;
        weightOnPaths = new AccumulatedWeightOnPathsVRP(vr, weightCaculator);
        cvCalculator = new CapacityViolationCalculator(vr, mTruck2Capacity);
        vr.post(this);
        init();
    }

    public CapacityConstraintVRP(VarRoutesVRP vr,
                                 IWeightCaculatorVRP weightCaculator,
                                 HashMap<String, Double> mTruck2LowerWeight,
                                 HashMap<String, Double> mTruck2UpperWeight) {
        this.vr = vr;
        this.weightCaculator = weightCaculator;
        weightOnPaths = new AccumulatedWeightOnPathsVRP(vr, weightCaculator);
        cvCalculator = new BoundaryViolationCalculator(vr, mTruck2LowerWeight, mTruck2UpperWeight);
        vr.post(this);
        init();
    }

    public CapacityConstraintVRP(AccumulatedWeightOnPathsVRP weightOnPaths,
                                 HashMap<String, Double> mTruck2Capacity) {
        this.vr = weightOnPaths.getVarRoutes();
        this.weightCaculator = weightOnPaths.getWeightCaculator();
        this.weightOnPaths = weightOnPaths;
        cvCalculator = new CapacityViolationCalculator(vr, mTruck2Capacity);
        vr.post(this);
        init();
    }

    public CapacityConstraintVRP(AccumulatedWeightOnPathsVRP weightOnPaths,
                                 HashMap<String, Double> mTruck2LowerWeight,
                                 HashMap<String, Double> mTruck2UpperWeight) {
        this.vr = weightOnPaths.getVarRoutes();
        this.weightCaculator = weightOnPaths.getWeightCaculator();
        this.weightOnPaths = weightOnPaths;
        cvCalculator = new BoundaryViolationCalculator(vr, mTruck2LowerWeight, mTruck2UpperWeight);
        vr.post(this);
        init();
    }

    private void init() {
        violations = 0;
        for (RouteVRP r : vr.getAllRoutes()) {
            violations += cvCalculator.getViolations(weightOnPaths.getTotalWeightOfRoute(r), r);
        }
    }

    private double getViolations(double sumWeight, RouteVRP route) {
        return cvCalculator.getViolations(sumWeight, route);
    }

    @Override
    public double getValue() {
        return violations;
    }

    @Override
    public double evaluateKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y) {
        double eval = 0;
        HashSet<RouteVRP> changedRoutes = new HashSet<>();
        for (int i = 0; i < x.size(); i++) {
            PointVRP px = x.get(i);
            PointVRP py = y.get(i);
            if (px.getRoute() != null) {
                changedRoutes.add(px.getRoute());
            }
            if (py != CBLSVRP.NULL_POINT) {
                changedRoutes.add(py.getRoute());
            }
        }
        HashSet<PointVRP> removedPoints = new HashSet<>();
        HashMap<PointVRP, PointVRP> mYi2LastX = new HashMap<>();
        for (RouteVRP r : changedRoutes) {
            double sumWeight = weightOnPaths.getTotalWeightOfRoute(r);
            eval -= cvCalculator.getViolations(sumWeight, r);
            for (PointVRP p : x) {
                if (p.getRoute() == r) {
                    PointVRP next = p.getNext();
                    PointVRP prev = p.getPrev();
                    while (removedPoints.contains(next)) {
                        next = next.getNext();
                    }
                    while (removedPoints.contains(prev)) {
                        prev = prev.getPrev();
                    }
                    sumWeight += weightCaculator.getEdgeWeight(prev, next, r) -
                            weightCaculator.getEdgeWeight(prev, p, r) -
                            weightCaculator.getEdgeWeight(p, next, r) -
                            weightCaculator.getPointWeight(p, r);
                    removedPoints.add(p);
                }
            }
            for (int i = 0; i < x.size(); i++) {
                PointVRP px = x.get(i);
                PointVRP py = y.get(i);
                if (py != CBLSVRP.NULL_POINT) {
                    PointVRP next = py.getNext();
                    PointVRP prev = py;
                    while (removedPoints.contains(next)) {
                        next = next.getNext();
                    }
                    if (mYi2LastX.containsKey(py)) {
                        prev = mYi2LastX.get(py);
                    }
                    mYi2LastX.put(py, px);
                    sumWeight += weightCaculator.getEdgeWeight(prev, px, r) +
                            weightCaculator.getPointWeight(px, r) +
                            weightCaculator.getEdgeWeight(px, next, r) -
                            weightCaculator.getEdgeWeight(prev, next, r);
                }
            }
            eval += cvCalculator.getViolations(sumWeight, r);
        }
        return eval;
    }

    @Override
    public VarRoutesVRP getVarRoutes() {
        return vr;
    }

    @Override
    public void propagateAddPoint(PointVRP point) {

    }

    @Override
    public void propagateAddRoute(RouteVRP route) {
        cvCalculator.propagateAddRoute(route);
    }

    @Override
    public void propagateKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y) {

    }
}
