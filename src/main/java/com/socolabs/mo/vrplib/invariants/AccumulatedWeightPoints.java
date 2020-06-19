package com.socolabs.mo.vrplib.invariants;

import com.socolabs.mo.vrplib.core.IVRPInvariant;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IAccumulatedCalculator;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AccumulatedWeightPoints implements IVRPInvariant {

    private VRPVarRoutes vr;
    private IAccumulatedCalculator accCalculator;

    private double[] accWeightArr;
    private double[] tmpAccWeightArr;

    private ArrayList<VRPPoint> changedPoints;

    private int stt;

    public AccumulatedWeightPoints(IAccumulatedCalculator accCalculator) {
        this.vr = accCalculator.getVarRoutes();
        this.accCalculator = accCalculator;
        vr.post(this);
        init();
    }

    public double getWeightValueOfPoint(VRPPoint point) {
        return accWeightArr[point.getStt()];
    }

    public double getTmpWeightValueOfPoint(VRPPoint point) {
        return tmpAccWeightArr[point.getStt()];
    }

    private void init() {
        int maxStt = 0;
        for (VRPPoint point : vr.getAllPoints()) {
            maxStt = Math.max(maxStt, point.getStt());
        }
        accWeightArr = new double[maxStt + 1];
        tmpAccWeightArr = new double[maxStt + 1];
        for (VRPRoute route : vr.getAllRoutes()) {
            VRPPoint point = route.getStartPoint();
            VRPPoint prev = point.getPrev();
            if (prev == null) {
                accWeightArr[point.getStt()] = accCalculator.caclAccWeightAtPoint(0, point);
                prev = point;
                point = point.getNext();
                while (point != null) {
                    int stt = point.getStt();
                    accWeightArr[stt] = accCalculator.caclAccWeightAtPoint(accWeightArr[prev.getStt()], point);
                    tmpAccWeightArr[stt] = accWeightArr[stt];
                    prev = point;
                    point = point.getNext();
                }
            }
        }
//        System.arraycopy(accWeightArr, 0, tmpAccWeightArr, 0, maxStt);
        changedPoints = new ArrayList<>();
    }

    private void clearTmpData() {
        for (VRPPoint point : changedPoints) {
            int stt = point.getStt();
            tmpAccWeightArr[stt] = accWeightArr[stt];
        }
        changedPoints.clear();
    }

    @Override
    public void explore() {
//        System.out.println(accCalculator.name() + " explore: ");
        clearTmpData();
        for (VRPRoute r : vr.getChangedRoutes()) {
            VRPPoint cur = r.getStartPoint();
            VRPPoint prev = cur.getTmpPrev();
            if (prev == null) {
                int stt = cur.getStt();
                tmpAccWeightArr[stt] = accCalculator.calcTmpAccWeightAtPoint(0, cur);
                prev = cur;
                cur = cur.getTmpNext();
                changedPoints.add(prev);
            }
            while (cur != null) {
                int stt = cur.getStt();
                tmpAccWeightArr[stt] = accCalculator.calcTmpAccWeightAtPoint(tmpAccWeightArr[prev.getStt()], cur);
                changedPoints.add(cur);
                prev = cur;
                cur = cur.getTmpNext();
            }
        }

        for (VRPPoint p : vr.getRemovedPoints()) {
            tmpAccWeightArr[p.getStt()] = 0;
            changedPoints.add(p);
        }

    }

    @Override
    public void propagate() {
        for (VRPPoint point : changedPoints) {
            int stt = point.getStt();
            accWeightArr[stt] = tmpAccWeightArr[stt];
        }
    }

    @Override
    public void createPoint(VRPPoint point) {
        int stt = point.getStt();
        if (stt >= accWeightArr.length) {
            int len = accWeightArr.length;
            int newLen = (stt / len + 1) * len;
            double[] newArr = new double[newLen];
            System.arraycopy(accWeightArr, 0, newArr, 0, accWeightArr.length);
            accWeightArr = newArr;
            newArr = new double[newLen];
            System.arraycopy(tmpAccWeightArr, 0, newArr, 0, tmpAccWeightArr.length);
            tmpAccWeightArr = newArr;
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
        for (VRPRoute route : vr.getAllRoutes()) {
            VRPPoint point = route.getStartPoint();
            VRPPoint prev = point.getPrev();
            if (prev == null) {
//                System.out.println("route: " + route.getStt());
                double accWeight = accCalculator.caclAccWeightAtPoint(0, point);
//                System.out.println("point " + point.getLocationCode() + " acc = " + accWeightArr[point.getStt()] + " verifying value = " + accWeight);
                if (Math.abs(accWeightArr[point.getStt()] - accWeight) > CBLSVRP.EPS) {
                    System.out.println("EXCEPTION::" + accCalculator.name() + " -> accWeightArr != tmpAccWeightArr");
                    return false;
                }
                point = point.getNext();
                while (point != null) {
                    accWeight = accCalculator.caclAccWeightAtPoint(accWeight, point);
//                    System.out.println("point " + point.getLocationCode() + " acc = " + accWeightArr[point.getStt()] + " verifying value = " + accWeight);
                    if (Math.abs(accWeightArr[point.getStt()] - accWeight) > CBLSVRP.EPS) {
                        System.out.println("EXCEPTION::" + accCalculator.name() + " -> accWeightArr != tmpAccWeightArr");
                        return false;
                    }
                    point = point.getNext();
                }
            }
        }
        return true;
    }

    @Override
    public String name() {
        return "AccumulatedWeightPoints";
    }
}
