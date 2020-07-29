package com.socolabs.mo.vrplib.functions.max;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;

import java.util.*;

public class MaxRouteFunctions implements IVRPFunction {
    private VRPVarRoutes vr;
    private IVRPFunction[] functions;
    private TreeSet<Pair<Double, IVRPFunction>> pq;
    private double value;
    private double tmpValue;

    public MaxRouteFunctions(VRPVarRoutes vr, HashMap<VRPRoute, IVRPFunction> mRoute2Function) {
        this.vr = vr;
        vr.post(this);
        int maxStt = 0;
        for (VRPRoute r : mRoute2Function.keySet()) {
            maxStt = Math.max(maxStt, r.getStt());
        }
        functions = new IVRPFunction[maxStt + 1];
        for (VRPRoute r : mRoute2Function.keySet()) {
            int stt = r.getStt();
            functions[stt] = mRoute2Function.get(r);
        }
        init();
    }

    private void init() {
        pq = new TreeSet<>(new Comparator<Pair<Double, IVRPFunction>>() {
            @Override
            public int compare(Pair<Double, IVRPFunction> o1, Pair<Double, IVRPFunction> o2) {
                if (o1.first < o2.first) {
                    return -1;
                }
                if (o2.first < o1.first) {
                    return 1;
                }
                return o1.second.getStt() - o2.second.getStt();
            }
        });

        for (IVRPFunction f : functions) {
            if (f != null) {
                pq.add(new Pair<>(f.getValue(), f));
            }
        }
        tmpValue = value = pq.last().first;
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
            IVRPFunction f = functions[route.getStt()];
            pq.remove(new Pair<>(f.getValue(), f));
            pq.add(new Pair<>(f.getTmpValue(), f));
        }
        tmpValue = pq.last().first;
        for (VRPRoute route : vr.getChangedRoutes()) {
            IVRPFunction f = functions[route.getStt()];
            pq.remove(new Pair<>(f.getTmpValue(), f));
            pq.add(new Pair<>(f.getValue(), f));
        }
    }

    @Override
    public void propagate() {
        value = tmpValue;
        for (VRPRoute route : vr.getChangedRoutes()) {
            IVRPFunction f = functions[route.getStt()];
            pq.remove(new Pair<>(f.getValue(), f));
            pq.add(new Pair<>(f.getTmpValue(), f));
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
        for (IVRPFunction f : functions) {
            if (f != null) {
                v = Math.max(v, f.getValue());
            }
        }
        if (value != v) {
            System.out.println("EXCEPTION::" + name() + " -> value != tmpValue " + value + " - " + v);
            for (IVRPFunction f : functions) {
                if (f != null) {
                    System.out.println(f.getStt() + " " + f.getValue());
                }
            }
            System.out.println(pq.last().first);
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
        return "MaxRouteFunctions";
    }
}

