package com.socolabs.mo.vrplib.functions;

import com.socolabs.mo.vrplib.core.*;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightOnPathsVRP;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TotalCostVRP implements IFunctionVRP {

    private double value;
    private VarRoutesVRP vr;
    private IWeightCaculatorVRP weightCaculator;
    private AccumulatedWeightOnPathsVRP weightOnPaths;

    public TotalCostVRP(VarRoutesVRP vr, IWeightCaculatorVRP weightCaculator) {
        this.vr = vr;
        this.weightCaculator = weightCaculator;
        weightOnPaths = new AccumulatedWeightOnPathsVRP(vr, weightCaculator);
        vr.post(this);
        init();
    }

    public TotalCostVRP(AccumulatedWeightOnPathsVRP weightOnPaths) {
        this.vr = weightOnPaths.getVarRoutes();
        this.weightCaculator = weightOnPaths.getWeightCaculator();
        this.weightOnPaths = weightOnPaths;
        vr.post(this);
        init();
    }

    private void init() {
        value = 0;
        for (String truckCode : vr.getAllTrucks()) {
            value += weightOnPaths.getDepatureAccWeightAtPoint(vr.getLastPoint(truckCode));
        }
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public double evaluateKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y) {
        double eval = 0;
        HashSet<PointVRP> removedPoints = new HashSet<>();
        HashMap<PointVRP, PointVRP> mYi2LastX = new HashMap<>();
        for (PointVRP p : x) {
            RouteVRP r = p.getRoute();
            if (r != null) {
                PointVRP next = p.getNext();
                PointVRP prev = p.getPrev();
                while (removedPoints.contains(next)) {
                    next = next.getNext();
                }
                while (removedPoints.contains(prev)) {
                    prev = prev.getPrev();
                }
                eval += weightCaculator.getEdgeWeight(prev, next, r) -
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
                RouteVRP r = py.getRoute();
                PointVRP next = py.getNext();
                PointVRP prev = py;
                while (removedPoints.contains(next)) {
                    next = next.getNext();
                }
                if (mYi2LastX.containsKey(py)) {
                    prev = mYi2LastX.get(py);
                }
                mYi2LastX.put(py, px);
                eval += weightCaculator.getEdgeWeight(prev, px, r) +
                            weightCaculator.getPointWeight(px, r) +
                            weightCaculator.getEdgeWeight(px, next, r) -
                            weightCaculator.getEdgeWeight(prev, next, r);
            }
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

    }

    @Override
    public void propagateKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y) {

    }
}
