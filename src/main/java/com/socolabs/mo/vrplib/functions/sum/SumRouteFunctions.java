package com.socolabs.mo.vrplib.functions.sum;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.utils.CBLSVRP;
import localsearch.domainspecific.vehiclerouting.vrp.CBLSVR;

import java.util.HashMap;
import java.util.HashSet;

public class SumRouteFunctions implements IVRPFunction {

    private VRPVarRoutes vr;
    private IVRPFunction[] functions;

    private double value;
    private double tmpValue;

    public SumRouteFunctions(VRPVarRoutes vr, HashMap<VRPRoute, IVRPFunction> mRoute2Function) {
        this.vr = vr;
        vr.post(this);
        int maxStt = 0;
        for (VRPRoute r : mRoute2Function.keySet()) {
            maxStt = Math.max(maxStt, r.getStt());
        }
        value = 0;
        functions = new IVRPFunction[maxStt + 1];
        for (VRPRoute r : mRoute2Function.keySet()) {
            int stt = r.getStt();
            functions[stt] = mRoute2Function.get(r);
            value += functions[stt].getValue();
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
        tmpValue = value;
        for (VRPRoute r : vr.getChangedRoutes()) {
            int idx = r.getStt();
            if (idx >= 0 && idx < functions.length) {
                if (functions[idx] != null) {
                    tmpValue -= functions[idx].getValue();
                    tmpValue += functions[idx].getTmpValue();
                }
            }
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
        return this.stt;
    }

    @Override
    public void setStt(int stt) {
        this.stt = stt;
    }

    @Override
    public boolean verify() {
        double v = 0;
        for (int i = 0; i < functions.length; i++) {
            if (functions[i] != null) {
                v += functions[i].getValue();
            }
        }
        if (Math.abs(v - value) > CBLSVRP.EPS) {
            System.out.println(name() + ":: EXCEPTION calculating sum of route functions !!!!");
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
        return "SumRouteFunctions";
    }
}
