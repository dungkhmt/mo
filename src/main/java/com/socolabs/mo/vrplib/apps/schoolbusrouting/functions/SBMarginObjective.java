package com.socolabs.mo.vrplib.apps.schoolbusrouting.functions;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;

import java.util.HashSet;

public class SBMarginObjective implements IVRPFunction {

    private VRPVarRoutes vr;
    private AccumulatedWeightPoints visitedNumberPoints;

    private int value;
    private int tmpValue;

    public SBMarginObjective(VRPVarRoutes vr, AccumulatedWeightPoints visitedNumberPoints) {
        this.vr = vr;
        this.visitedNumberPoints = visitedNumberPoints;
        vr.post(this);
        value = 0;
        for (VRPRoute r : vr.getAllRoutes()) {
            if (r.getNbPoints() > 0) {
                value += getViolation((int) visitedNumberPoints.getWeightValueOfPoint(r.getEndPoint()));
            }
        }
        tmpValue = value;
    }

    private int getViolation(int v) {
        if (v > 0) {
            if (v < 4) {
                return 4 - v;
            } else if (v > 5 && v < 10) {
                return 10 - v;
            } else if (v > 14 && v < 20) {
                return 20 - v;
            } else if (v > 27 && v < 33) {
                return 33 - v;
            }
        }
        return 0;
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
        tmpValue = value;
        for (VRPRoute r : vr.getChangedRoutes()) {
            int nb = getViolation((int) visitedNumberPoints.getWeightValueOfPoint(r.getEndPoint()));
            int tmpNb = getViolation((int) visitedNumberPoints.getTmpWeightValueOfPoint(r.getEndPoint()));
            tmpValue -= nb;
            tmpValue += tmpNb;
        }
    }

    @Override
    public void propagate() {
        value = tmpValue;
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
        return stt;
    }

    @Override
    public void setStt(int stt) {
        this.stt = stt;
    }

    @Override
    public boolean verify() {
        int v = 0;
        for (VRPRoute r : vr.getAllRoutes()) {
            if (r.getNbPoints() > 0) {
                v += getViolation((int) visitedNumberPoints.getWeightValueOfPoint(r.getEndPoint()));
            }
        }
        if (v != value) {
            System.out.println(name() + ":: EXCEPTION calculating total used buses !!!!");
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
        return "SBMarginObjective";
    }
}

