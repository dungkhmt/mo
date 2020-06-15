package com.socolabs.mo.vrplib.invariants;

import com.socolabs.mo.vrplib.core.*;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;

import java.util.ArrayList;
import java.util.HashSet;

public class RevAccumulatedWeightPoints implements IVRPInvariant {

    private VRPVarRoutes vr;
    private IAccumulatedCalculator accCalculator;

    private double[] revAccWeightArr;
    private double[] tmpRevAccWeightArr;
    private ArrayList<VRPPoint> changedPoints;

    private int stt;

    public RevAccumulatedWeightPoints(IAccumulatedCalculator accCalculator) {
        this.vr = accCalculator.getVarRoutes();
        this.accCalculator = accCalculator;
        vr.post(this);
        init();
    }

    private void init() {
        int maxStt = 0;
        for (VRPPoint point : vr.getAllPoints()) {
            maxStt = Math.max(maxStt, point.getStt());
        }
        revAccWeightArr = new double[maxStt + 1];
        tmpRevAccWeightArr = new double[maxStt + 1];
        for (VRPRoute route : vr.getAllRoutes()) {
            VRPPoint point = route.getEndPoint();
            VRPPoint next = point.getNext();
            if (next == null) {
                revAccWeightArr[point.getStt()] = accCalculator.caclAccWeightAtPoint(0, point);
                next = point;
                point = point.getPrev();
                while (point != null) {
                    int stt = point.getStt();
                    revAccWeightArr[stt] = accCalculator.caclAccWeightAtPoint(revAccWeightArr[next.getStt()], point);
                    tmpRevAccWeightArr[stt] = revAccWeightArr[stt];
                    next = point;
                    point = point.getPrev();
                }
            }
        }
        changedPoints = new ArrayList<>();
    }

    private void clearTmpData() {
        for (VRPPoint point : changedPoints) {
            int stt = point.getStt();
            tmpRevAccWeightArr[stt] = revAccWeightArr[stt];
        }
        changedPoints.clear();
    }

    @Override
    public void explore() {
        clearTmpData();
        for (VRPRoute route : vr.getChangedRoutes()) {
            VRPPoint point = route.getEndPoint();
            VRPPoint next = point.getNext();
            if (next == null) {
                changedPoints.add(point);
                tmpRevAccWeightArr[point.getStt()] = accCalculator.caclAccWeightAtPoint(0, point);
                next = point;
                point = point.getPrev();
                while (point != null) {
                    changedPoints.add(point);
                    int stt = point.getStt();
                    tmpRevAccWeightArr[stt] = accCalculator.calcTmpAccWeightAtPoint(tmpRevAccWeightArr[next.getStt()], point);
                    next = point;
                    point = point.getPrev();
                }
            }
        }
        for (VRPPoint p : vr.getRemovedPoints()) {
            tmpRevAccWeightArr[p.getStt()] = 0;
            changedPoints.add(p);
        }
    }

    @Override
    public void propagate() {
        for (VRPPoint point : changedPoints) {
            int stt = point.getStt();
            revAccWeightArr[stt] = tmpRevAccWeightArr[stt];
        }
    }

    @Override
    public HashSet<VRPPoint> getIndependentPoints() {
        return null;
    }

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
        return true;
    }

    @Override
    public void createPoint(VRPPoint point) {
        int stt = point.getStt();
        if (stt >= revAccWeightArr.length) {
            int len = revAccWeightArr.length;
            int newLen = (stt / len + 1) * len;
            double[] newArr = new double[newLen];
            System.arraycopy(revAccWeightArr, 0, newArr, 0, revAccWeightArr.length);
            revAccWeightArr = newArr;
            newArr = new double[newLen];
            System.arraycopy(tmpRevAccWeightArr, 0, newArr, 0, tmpRevAccWeightArr.length);
            tmpRevAccWeightArr = newArr;
        }
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
        return "RevAccumulatedWeightPoints";
    }
}
