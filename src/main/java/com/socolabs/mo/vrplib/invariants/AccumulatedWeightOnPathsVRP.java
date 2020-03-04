package com.socolabs.mo.vrplib.invariants;

import com.socolabs.mo.vrplib.core.*;
import com.socolabs.mo.vrplib.utils.CBLSVRP;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Getter
public class AccumulatedWeightOnPathsVRP implements InvariantVRP {

    private double[] arrivalAccWeight;
    private double[] departureAccWeight;
    private VarRoutesVRP vr;
    private IWeightCaculatorVRP weightCaculator;

    public AccumulatedWeightOnPathsVRP(VarRoutesVRP vr, IWeightCaculatorVRP weightCaculator) {
        this.vr = vr;
        this.weightCaculator = weightCaculator;
        vr.post(this);
        init();
    }

    private void init() {
        int maxId = 0;
        for (PointVRP p : vr.getAllPoints()) {
            maxId = Math.max(maxId, p.getId());
        }
        arrivalAccWeight = new double[maxId + 1];
        departureAccWeight = new double[maxId + 1];
        for (String truckCode : vr.getAllTrucks()) {
            updateTruck(truckCode);
        }
    }

    public double getArrivalAccWeightAtPoint(PointVRP point) {
        int id = point.getId();
        if (id >= arrivalAccWeight.length) {
            return 0;
        } else {
            return arrivalAccWeight[id];
        }
    }

    public double getDepatureAccWeightAtPoint(PointVRP point) {
        int id = point.getId();
        if (id >= departureAccWeight.length) {
            return 0;
        } else {
            return departureAccWeight[id];
        }
    }

    public double getTotalWeightOfRoute(RouteVRP route) {
        int startId = route.getStartPoint().getId();
        int endId = route.getEndPoint().getId();
        return departureAccWeight[endId] - arrivalAccWeight[startId];
    }

    private void updateTruck(String truckCode) {
        PointVRP p = vr.getFirstPoint(truckCode);
        PointVRP nextP = p.getNext();
        while (nextP != null) {
            int i = p.getId();
            int j = nextP.getId();
            arrivalAccWeight[j] = departureAccWeight[i] + weightCaculator.getEdgeWeight(p, nextP, p.getRoute());
            departureAccWeight[j] = arrivalAccWeight[j] + weightCaculator.getPointWeight(nextP, p.getRoute());
        }
    }

    @Override
    public VarRoutesVRP getVarRoutes() {
        return vr;
    }

    @Override
    public void propagateAddPoint(PointVRP point) {
        if (point.getId() >= arrivalAccWeight.length) {
            extendArrays(point.getId());
        }
    }

    @Override
    public void propagateAddRoute(RouteVRP route) {
        int maxIndex = Math.max(route.getStartPoint().getId(), route.getEndPoint().getId());
        if (maxIndex >= arrivalAccWeight.length) {
            extendArrays(maxIndex);
        }
        updateTruck(route.getTruckCode());
    }

    @Override
    public void propagateKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y) {
        HashSet<String> trucks = new HashSet<>();
        int maxIndex = 0;
        for (PointVRP p : x) {
            if (p.getOldRoute() != null) {
                trucks.add(p.getOldRoute().getTruckCode());
            } else {
                maxIndex = Math.max(maxIndex, p.getIndex());
            }
        }
        for (PointVRP p : y) {
            if (p != CBLSVRP.NULL_POINT) {
                trucks.add(p.getOldRoute().getTruckCode());
            } else {
                maxIndex = Math.max(maxIndex, p.getIndex());
            }
        }
        if (maxIndex >= arrivalAccWeight.length) {
            extendArrays(maxIndex);
        }
        for (String truckCode : trucks) {
            updateTruck(truckCode);
        }
    }

    private void extendArrays(int i) {
        int t = i / arrivalAccWeight.length + 1;
        t *= arrivalAccWeight.length;
        double[] newArrival = new double[t];
        double[] newDepature = new double[t];
        System.arraycopy(arrivalAccWeight, 0, newArrival, 0, arrivalAccWeight.length);
        System.arraycopy(departureAccWeight, 0, newDepature, 0, departureAccWeight.length);
        arrivalAccWeight = newArrival;
        departureAccWeight = newDepature;
    }
}
