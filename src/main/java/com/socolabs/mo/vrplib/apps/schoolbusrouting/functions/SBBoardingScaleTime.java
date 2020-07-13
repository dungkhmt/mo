package com.socolabs.mo.vrplib.apps.schoolbusrouting.functions;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.SchoolBusPickupPoint;
import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class SBBoardingScaleTime implements IVRPFunction {

    private VRPVarRoutes vr;
    private RevAccumulatedWeightPoints revAccTravelTime;
    private HashMap<VRPRoute, Double> mRoute2BoardingScale;
    private HashMap<VRPRoute, Double> tmpMRoute2BoardingScale;
    private TreeSet<Pair<Double, VRPRoute>> pq;

    private double value;
    private double tmpValue;

    public SBBoardingScaleTime(VRPVarRoutes vr, RevAccumulatedWeightPoints revAccTravelTime) {
        this.vr = vr;
        this.revAccTravelTime = revAccTravelTime;
        vr.post(this);

        value = 0;
        mRoute2BoardingScale = new HashMap<>();
        tmpMRoute2BoardingScale = new HashMap<>();
        pq = new TreeSet<>(new Comparator<Pair<Double, VRPRoute>>() {
            @Override
            public int compare(Pair<Double, VRPRoute> o1, Pair<Double, VRPRoute> o2) {
                if (o1.first < o2.first) {
                    return -1;
                }
                if (o2.first < o1.first) {
                    return 1;
                }
                return o1.second.getStt() - o2.second.getStt();
            }
        });

        for (VRPRoute r : vr.getAllRoutes()) {
            double routeVal = 0;
            for (VRPPoint p = r.getStartPoint().getNext(); p != r.getEndPoint(); p = p.getNext()) {
                SchoolBusPickupPoint pp = (SchoolBusPickupPoint) p;
                if (pp.getDirectTravelTimeToSchool() > 0) {
                    routeVal = Math.max(routeVal, revAccTravelTime.getWeightValueOfPoint(p) / pp.getDirectTravelTimeToSchool());
                }
            }
            mRoute2BoardingScale.put(r, routeVal);
            value = Math.max(value, routeVal);
            pq.add(new Pair<>(routeVal, r));
        }
        tmpValue = value;
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
        for (VRPRoute r : vr.getChangedRoutes()) {
            double routeVal = 0;
            for (VRPPoint p = r.getStartPoint().getTmpNext(); p != r.getEndPoint(); p = p.getTmpNext()) {
                SchoolBusPickupPoint pp = (SchoolBusPickupPoint) p;
                if (pp.getDirectTravelTimeToSchool() > 0) {
                    routeVal = Math.max(routeVal, revAccTravelTime.getTmpWeightValueOfPoint(p) / pp.getDirectTravelTimeToSchool());
                }
            }
            Pair<Double, VRPRoute> oldP = new Pair<>(mRoute2BoardingScale.get(r), r);
            Pair<Double, VRPRoute> newP = new Pair<>(routeVal, r);
            pq.remove(oldP);
            pq.add(newP);

            tmpMRoute2BoardingScale.put(r, routeVal);
        }
        tmpValue = pq.last().first;
    }

    @Override
    public void propagate() {
        value = tmpValue;
        for (VRPRoute r : tmpMRoute2BoardingScale.keySet()) {
            mRoute2BoardingScale.put(r, tmpMRoute2BoardingScale.get(r));
        }
        tmpMRoute2BoardingScale.clear();
    }

    @Override
    public void clearTmpData() {
        tmpValue = value;
        for (VRPRoute r : tmpMRoute2BoardingScale.keySet()) {
            pq.remove(new Pair<>(tmpMRoute2BoardingScale.get(r), r));
            pq.add(new Pair<>(mRoute2BoardingScale.get(r), r));
        }
        tmpMRoute2BoardingScale.clear();
    }

    @Override
    public HashSet<VRPPoint> getIndependentPoints() {
        return null;
    }

    private int stt;
    @Override
    public int getStt() {
        return stt;
    }

    @Override
    public void setStt(int stt) {
        this.stt = stt;
    }

    @Override
    public boolean verify() {
        double v = 0;
        for (VRPRoute r : vr.getAllRoutes()) {
            double routeVal = 0;
            for (VRPPoint p = r.getStartPoint().getNext(); p != r.getEndPoint(); p = p.getNext()) {
                SchoolBusPickupPoint pp = (SchoolBusPickupPoint) p;
                if (pp.getDirectTravelTimeToSchool() > 0) {
                    routeVal = Math.max(routeVal, revAccTravelTime.getWeightValueOfPoint(p) / pp.getDirectTravelTimeToSchool());
                }
            }
            v = Math.max(v, routeVal);
            if (Math.abs(routeVal - mRoute2BoardingScale.get(r)) > CBLSVRP.EPS) {
                System.out.println(name() + ":: EXCEPTION -> 1");
                return false;
            }
        }
        if (Math.abs(v - value) > CBLSVRP.EPS) {
            System.out.println(name() + ":: EXCEPTION -> 2");
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
        return "SBBoardingScaleTime";
    }
}