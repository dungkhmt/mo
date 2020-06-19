package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.util.HashSet;

public class SBTotalUsedBuses implements IVRPFunction {

    private VRPVarRoutes vr;
    private int value;
    private int tmpValue;

    public SBTotalUsedBuses(VRPVarRoutes vr) {
        this.vr = vr;
        vr.post(this);
        value = 0;
        for (VRPRoute r : vr.getAllRoutes()) {
            if (r.getNbPoints() > 0) {
                value++;
            }
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
            int nb = r.getNbPoints();
            int tmpNb = r.getTmpNbPoints();
            if (nb == 0 && tmpNb > 0) {
                tmpValue++;
            } else if (nb > 0 && tmpNb == 0) {
                tmpValue--;
            }
        }
    }

    @Override
    public void propagate() {
        value = tmpValue;
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
                v++;
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
        return "SBTotalUsedBuses";
    }
}
