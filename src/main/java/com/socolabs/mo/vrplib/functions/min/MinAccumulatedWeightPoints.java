package com.socolabs.mo.vrplib.functions.min;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.vrplib.utils.CBLSVRP;
import localsearch.domainspecific.vehiclerouting.vrp.CBLSVR;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

public class MinAccumulatedWeightPoints implements IVRPFunction {
    private VRPVarRoutes vr;
    private AccumulatedWeightPoints acc;
    private TreeSet<Pair<Double, VRPRoute>> pq;
    private double value;
    private double tmpValue;

    public MinAccumulatedWeightPoints(AccumulatedWeightPoints acc) {
        vr = acc.getVarRoutes();
        this.acc = acc;
        init();
    }

    private void init() {
        value = 0;
        pq = new TreeSet<>(new Comparator<Pair<Double, VRPRoute>>() {
            @Override
            public int compare(Pair<Double, VRPRoute> o1, Pair<Double, VRPRoute> o2) {
                if (o1.first < o2.first) {
                    return 1;
                }
                if (o2.first < o1.first) {
                    return -1;
                }
                return o1.second.getStt() - o2.second.getStt();
            }
        });
        for (VRPRoute route : vr.getAllRoutes()) {
            if (acc.getWeightValueOfPoint(route.getEndPoint()) > CBLSVRP.EPS) {
                pq.add(new Pair<>(acc.getWeightValueOfPoint(route.getEndPoint()), route));
            }
//            value += acc.getWeightValueOfPoint(route.getEndPoint());
        }
        if (pq.size() > 0) {
            tmpValue = value = pq.last().first;
        } else {
            tmpValue = value = 0;
        }
        vr.post(this);
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public double getTmpValue() {
        return tmpValue;
    }

    @Override
    public void explore() {
        tmpValue = 0;
        for (VRPRoute route : vr.getChangedRoutes()) {
            pq.remove(new Pair<>(acc.getWeightValueOfPoint(route.getEndPoint()), route));
            if (acc.getTmpWeightValueOfPoint(route.getEndPoint()) > CBLSVRP.EPS) {
                pq.add(new Pair<>(acc.getTmpWeightValueOfPoint(route.getEndPoint()), route));
            }
        }
        tmpValue = pq.last().first;
        for (VRPRoute route : vr.getChangedRoutes()) {
            pq.remove(new Pair<>(acc.getTmpWeightValueOfPoint(route.getEndPoint()), route));
            if (acc.getWeightValueOfPoint(route.getEndPoint()) > CBLSVRP.EPS) {
                pq.add(new Pair<>(acc.getWeightValueOfPoint(route.getEndPoint()), route));
            }
        }
    }

    @Override
    public void propagate() {
        value = tmpValue;
        for (VRPRoute route : vr.getChangedRoutes()) {
            pq.remove(new Pair<>(acc.getWeightValueOfPoint(route.getEndPoint()), route));
            if (acc.getTmpWeightValueOfPoint(route.getEndPoint()) > CBLSVRP.EPS) {
                pq.add(new Pair<>(acc.getTmpWeightValueOfPoint(route.getEndPoint()), route));
            }
        }
    }

    @Override
    public void clearTmpData() {
        tmpValue = value;
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
        double v = 0;
        for (VRPRoute r : vr.getAllRoutes()) {
            if (acc.getWeightValueOfPoint(r.getEndPoint()) > CBLSVRP.EPS) {
                v = Math.min(v, acc.getWeightValueOfPoint(r.getEndPoint()));
            }
        }
        if (value != tmpValue) {
            System.out.println("EXCEPTION::" + name() + " -> value != tmpValue");
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
        return "MaxAccumulatedWeightPoints";
    }
}
