package com.socolabs.mo.vrplib.core;

import com.socolabs.mo.vrplib.constraints.capacity.CapacityConstraint;
import com.socolabs.mo.vrplib.constraints.leq.Leq;
import com.socolabs.mo.vrplib.constraints.timewindows.TimeWindowsConstraint;
import com.socolabs.mo.vrplib.entities.InvariantSttCmp;
import com.socolabs.mo.vrplib.entities.accumulatedcalculators.AccumulatedWeightCalculator;
import com.socolabs.mo.vrplib.functions.AccumulatedPointWeightsOnPath;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.vrplib.utils.CBLSVRP;
import localsearch.domainspecific.vehiclerouting.vrp.CBLSVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import lombok.Getter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

@Getter
public class VRPVarRoutes {
    private ArrayList<VRPPoint> allPoints;
    private ArrayList<VRPRoute> allRoutes;
    private ArrayList<IVRPInvariant> invariants;
    private ArrayList<IVRPBasicEntity> basicEntities;

    private HashMap<VRPRoute, VRPPoint> mChangedRouteToFirstTmpPoint;
    private HashMap<VRPRoute, ArrayList<VRPPoint>> mChangedRouteToRemovedPoints;
    private HashMap<VRPRoute, ArrayList<VRPPoint>> mChangedRouteToAddedPoints;
    private ArrayList<VRPPoint> removedPoints;
    private ArrayList<VRPPoint> addedPoints;

    private ArrayList<IVRPInvariant> dependentInvariantLst;
    private HashSet<IVRPInvariant> dependentInvariants;

    private ArrayList<IVRPInvariant> globalInvariants;
    private HashMap<VRPPoint, ArrayList<IVRPInvariant>> mPoint2LocalInvariants;

    private BitSet satisfiedConstraints;
    private ArrayList<IVRPChecker> checkers;

    private ArrayList<VRPPoint> changedPoints;

    private static InvariantSttCmp invariantCmp = new InvariantSttCmp();

    public VRPVarRoutes() {
        allRoutes = new ArrayList<>();
        allPoints = new ArrayList<>();
        invariants = new ArrayList<>();
        basicEntities = new ArrayList<>();
        mChangedRouteToFirstTmpPoint = new HashMap<>();
        mChangedRouteToRemovedPoints = new HashMap<>();
        mChangedRouteToAddedPoints = new HashMap<>();
        removedPoints = new ArrayList<>();
        addedPoints = new ArrayList<>();
        dependentInvariantLst = new ArrayList<>();
        dependentInvariants = new HashSet<>();
        globalInvariants = new ArrayList<>();
        mPoint2LocalInvariants = new HashMap<>();
        satisfiedConstraints = new BitSet();
        checkers = new ArrayList<>();
        changedPoints = new ArrayList<>();
    }

    public VRPPoint createPoint(String locationCode) {
        VRPPoint p = new VRPPoint(locationCode);
        post(p);
        return p;
    }

    public VRPRoute createRoute(String startLocation, String endLocation, String truckCode) {
        VRPPoint startPoint = createPoint(startLocation);
        VRPPoint endPoint = createPoint(endLocation);
        VRPRoute r = new VRPRoute(startPoint, endPoint, truckCode);
        startPoint.setRoute(r);
        endPoint.setRoute(r);
        post(r);
        return r;
    }

    public VRPRoute createRoute(VRPPoint startPoint, VRPPoint endPoint, String truckCode) {
        VRPRoute r = new VRPRoute(startPoint, endPoint, truckCode);
        startPoint.setRoute(r);
        endPoint.setRoute(r);
        post(r);
        return r;
    }

    public void removePoint(VRPPoint point) {
        // to do
        for (IVRPBasicEntity entity : basicEntities) {
            entity.removePoint(point);
        }
    }

    public void removeRoute(VRPRoute route) {
        for (IVRPBasicEntity entity : basicEntities) {
            entity.removeRoute(route);
        }
    }

    public void post(VRPPoint point) {
        allPoints.add(point);
        point.setStt(allPoints.size());
        for (IVRPBasicEntity entity : basicEntities) {
            entity.createPoint(point);
        }
    }

    public void post(VRPRoute route) {
        allRoutes.add(route);
        route.setStt(allRoutes.size());
        for (IVRPBasicEntity entity : basicEntities) {
            entity.createRoute(route);
        }
    }

    public void post(IVRPBasicEntity entity) {
        basicEntities.add(entity);
    }

    public void post(IVRPInvariant invariant) {
        invariants.add(invariant);
        invariant.setStt(invariants.size());
        basicEntities.add(invariant);
        HashSet<VRPPoint> independentPoints = invariant.getIndependentPoints();
        if (independentPoints == null) {
            globalInvariants.add(invariant);
        } else {
            for (VRPPoint p : independentPoints) {
                if (!mPoint2LocalInvariants.containsKey(p)) {
                    mPoint2LocalInvariants.put(p, new ArrayList<>());
                }
                mPoint2LocalInvariants.get(p).add(invariant);
            }
        }
    }

    public void post(IVRPChecker checker) {
        checkers.add(checker);
    }

    public void addSatisfiedConstraint(IVRPFunction constraint) {
        satisfiedConstraints.set(constraint.getStt(), true);
    }

    public Set<VRPRoute> getChangedRoutes() {
        return mChangedRouteToFirstTmpPoint.keySet();
    }

    // chèn x vào sau y, x chưa thuộc bất kỳ route nào
    public boolean exploreInsertPointMove(VRPPoint x, VRPPoint y) {
        // kiểm tra các điều kiện đơn giản của các checkers
        // khi chưa thay đổi các points
        VRPRoute routeY = y.getRoute();
        if (y.getNext() == null || routeY == null) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkInsertPointMove(x, y)) {
                return false;
            }
        }

        clearTmpData();

        x.setTmpNext(y.getNext());
        y.getNext().setTmpPrev(x);
        x.setTmpPrev(y);
        y.setTmpNext(x);
        x.setTmpRoute(routeY);

//        routeY.increaseTmpNbPoints(1);

        // các points được thêm vào các routes (point chưa thuộc route nào)
        addedPoints.add(x);
        
        // map route bị thay đổi và point đầu tiên bị thay đổi
        // vị trí bắt đầu thay đổi là y
        mChangedRouteToFirstTmpPoint.put(routeY, y);

        // map route các điểm bị removed khỏi route

        // map route các điểm được added thêm vào route
        ArrayList<VRPPoint> addedPoints = new ArrayList<>();
        addedPoints.add(x);
        mChangedRouteToAddedPoints.put(routeY, addedPoints);

        // lưu các points bị thay đổi (bất cứ thông tin gì)
        changedPoints.add(y);
        int index = y.getIndex();
        VRPPoint p = x;
        while (p != null) {
            index++;
            p.setTmpIndex(index);
            changedPoints.add(p);
            p = p.getTmpNext();
        }

        return explore();
    }

    public void propageteInsertPointMove(VRPPoint x, VRPPoint y) {
        boolean status = exploreInsertPointMove(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::propageteInsertPointMove !!!!");
            System.exit(-1);
        }
    }

    // chèn x sau y, khi cả 2 points này đã thuộc route nào đó
    public boolean exploreOnePointMove(VRPPoint x, VRPPoint y) {
        // x không thể là depot
        if (y.getNext() == x) {
            return true;
        }
        if (x == y || x.isDepot()) {
            return false;
        }
        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();
        if (y.getNext() == null || routeY == null) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkOnePointMove(x, y)) {
                return false;
            }
        }
        clearTmpData();

        VRPPoint prevX = x.getPrev();
        VRPPoint nextX = x.getNext();
        VRPPoint nextY = y.getNext();
        VRPPoint p;

        if (routeX != null) {
            nextX.setTmpPrev(prevX);
            prevX.setTmpNext(nextX);
        }
        x.setTmpNext(nextY);
        nextY.setTmpPrev(x);
        x.setTmpPrev(y);
        y.setTmpNext(x);
        x.setTmpRoute(routeY);

        if (routeX == null) {
            addedPoints.add(x);
        }
        if (routeX != routeY) {
            if (routeX != null) {
//                routeX.decreaseTmpNbPoints(1);
                p = nextX;
                int index = prevX.getIndex();
                while (p != null) {
                    index++;
                    p.setTmpIndex(index);
                    changedPoints.add(p);
                    p = p.getTmpNext();
                }
                changedPoints.add(prevX);
            }

//            routeY.increaseTmpNbPoints(1);
            p = x;
            int index = y.getIndex();
            while (p != null) {
                index++;
                p.setTmpIndex(index);
                changedPoints.add(p);
                p = p.getTmpNext();
            }
            changedPoints.add(y);

            mChangedRouteToFirstTmpPoint.put(routeY, y);
            ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();
            addedPointsAtRouteY.add(x);
            mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);

            if (routeX != null) {
                mChangedRouteToFirstTmpPoint.put(routeX, prevX);
                mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
            }
        } else {
            if (x.getIndex() > y.getIndex()) {
                p = y;
            } else {
                p = prevX;
            }
            mChangedRouteToFirstTmpPoint.put(routeY, p);
            int index = p.getIndex();
            while (p != null) {
                p.setTmpIndex(index);
                changedPoints.add(p);
                p = p.getTmpNext();
                index++;
            }
        }
        return explore();
    }

    public void propageteOnePointMove(VRPPoint x, VRPPoint y) {
        boolean status = exploreOnePointMove(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::propageteOnePointMove !!!!");
            System.exit(-1);
        }
    }

    // swap vị trí x và y
    public boolean exploreTwoPointsMove(VRPPoint x, VRPPoint y) {
        if (x == y) {
            return true;
        }
        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == null || routeY == null) {
            return false;
        }
        if (x.isDepot() || y.isDepot()) {
            return false;
        }
        if (routeX == routeY && x.getIndex() > y.getIndex()) {
            return exploreTwoPointsMove(y, x);
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkTwoPointsMove(x, y)) {
                return false;
            }
        }
        clearTmpData();
        VRPPoint prevX = x.getPrev();
        VRPPoint nextX = x.getNext();
        VRPPoint prevY = y.getPrev();
        VRPPoint nextY = y.getNext();
        VRPPoint p;

        if (routeX != routeY || nextX != y) {
            prevX.setTmpNext(y);
            y.setTmpPrev(prevX);
            nextX.setTmpPrev(y);
            y.setTmpNext(nextX);
            prevY.setTmpNext(x);
            x.setTmpPrev(prevY);
            nextY.setTmpPrev(x);
            x.setTmpNext(nextY);
        } else {
            prevX.setTmpNext(y);
            y.setTmpPrev(prevX);
            y.setTmpNext(x);
            x.setTmpPrev(y);
            x.setTmpNext(nextY);
            nextY.setTmpPrev(x);
        }
        x.setTmpRoute(routeY);
        y.setTmpRoute(routeX);

        if (routeX != routeY) {
            p = y;
            int index = prevX.getIndex();
            while (p != null) {
                index++;
                p.setTmpIndex(index);
                changedPoints.add(p);
                p = p.getTmpNext();
            }
            changedPoints.add(prevX);

            p = x;
            index = prevY.getIndex();
            while (p != null) {
                index++;
                p.setTmpIndex(index);
                changedPoints.add(p);
                p = p.getTmpNext();
            }
            changedPoints.add(prevY);

            mChangedRouteToFirstTmpPoint.put(routeX, prevX);
            mChangedRouteToFirstTmpPoint.put(routeY, prevY);

            ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();
            addedPointsAtRouteY.add(x);
            ArrayList<VRPPoint> addedPointsAtRouteX = new ArrayList<>();
            addedPointsAtRouteX.add(y);

            mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
            mChangedRouteToAddedPoints.put(routeX, addedPointsAtRouteX);
            mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
            mChangedRouteToRemovedPoints.put(routeY, addedPointsAtRouteX);
        } else {
            p = prevX;
            mChangedRouteToFirstTmpPoint.put(routeY, p);
            int index = p.getIndex();
            while (p != null) {
                p.setTmpIndex(index);
                changedPoints.add(p);
                p = p.getTmpNext();
                index++;
            }
        }
        return explore();
    }

    public void propageteTwoPointsMove(VRPPoint x, VRPPoint y) {
        boolean status = exploreTwoPointsMove(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::propageteTwoPointsMove !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreTwoOptMove1(VRPPoint x, VRPPoint y) {
        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == routeY || routeX == null || routeY == null) {
            return false;
        }
        if (x.isEndPoint() || y.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkTwoOptMove1(x, y)) {
                return false;
            }
        }

        clearTmpData();

        VRPPoint endX = routeX.getEndPoint();
        VRPPoint startY = routeY.getStartPoint();
        // routeX: x->y, routeY: nextX -> nextY

        ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRouteX = new ArrayList<>();

        //tmp route x
        VRPPoint u, v;
        u = x;
        v = y;
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != startY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeX);
            changedPoints.add(v);
            addedPointsAtRouteX.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(endX);
        endX.setTmpPrev(u);
        endX.setTmpIndex(idx + 1);
        changedPoints.add(endX);

        // tmp route y
        u = startY;
        v = endX.getPrev();
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != x) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeY);
            changedPoints.add(v);
            addedPointsAtRouteY.add(v);
            u = v;
            v = v.getPrev();
        }
        v = y.getNext();
        while (v != null) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }

        mChangedRouteToFirstTmpPoint.put(routeX, x);
        mChangedRouteToFirstTmpPoint.put(routeY, startY);
        mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
        mChangedRouteToAddedPoints.put(routeX, addedPointsAtRouteX);
        mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
        mChangedRouteToRemovedPoints.put(routeY, addedPointsAtRouteX);

        return explore();
    }

    public void propagateTwoOptMove1(VRPPoint x, VRPPoint y) {
        boolean status = exploreTwoOptMove1(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::propagateTwoOptMove1 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreTwoOptMove2(VRPPoint x, VRPPoint y) {
        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == routeY || routeX == null || routeY == null) {
            return false;
        }
        if (x.isEndPoint() || y.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkTwoOptMove2(x, y)) {
                return false;
            }
        }

        clearTmpData();
        VRPPoint endX = routeX.getEndPoint();
        VRPPoint startX = routeX.getStartPoint();
        VRPPoint startY = routeY.getStartPoint();
        VRPPoint nextY = y.getNext();

        ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRouteX = new ArrayList<>();

        //tmp route x
        VRPPoint u, v;
        u = startX;
        v = startY.getNext();
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != nextY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeX);
            changedPoints.add(v);
            addedPointsAtRouteX.add(v);
            u = v;
            v = v.getNext();
        }
        v = x;
        while (v != startX) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeX);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(endX);
        endX.setTmpPrev(u);
        endX.setTmpIndex(idx + 1);
        changedPoints.add(endX);

        // tmp route y
        u = startY;
        v = endX.getPrev();
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != x) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeY);
            changedPoints.add(v);
            addedPointsAtRouteY.add(v);
            u = v;
            v = v.getPrev();
        }
        v = y.getNext();
        while (v != null) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }
        mChangedRouteToFirstTmpPoint.put(routeX, startX);
        mChangedRouteToFirstTmpPoint.put(routeY, startY);
        mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
        mChangedRouteToAddedPoints.put(routeX, addedPointsAtRouteX);
        mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
        mChangedRouteToRemovedPoints.put(routeY, addedPointsAtRouteX);
        return explore();
    }

    public void propagateTwoOptMove2(VRPPoint x, VRPPoint y) {
        boolean status = exploreTwoOptMove2(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::propagateTwoOptMove2 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreTwoOptMove3(VRPPoint x, VRPPoint y) {
        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == routeY || routeX == null || routeY == null) {
            return false;
        }
        if (x.isEndPoint() || y.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkTwoOptMove3(x, y)) {
                return false;
            }
        }

        clearTmpData();

        VRPPoint endX = routeX.getEndPoint();
        VRPPoint endY = routeY.getEndPoint();
        VRPPoint startY = routeY.getStartPoint();
        // routeX: x->y, routeY: nextX -> nextY

        ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRouteX = new ArrayList<>();

        //tmp route x
        VRPPoint u, v;
        u = x;
        v = y;
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != startY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeX);
            changedPoints.add(v);
            addedPointsAtRouteX.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(endX);
        endX.setTmpPrev(u);
        endX.setTmpIndex(idx + 1);
        changedPoints.add(endX);

        // tmp route y
        u = startY;
        v = endY.getPrev();
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != y) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = x.getNext();
        while (v != endX) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeY);
            changedPoints.add(v);
            addedPointsAtRouteY.add(v);
            u = v;
            v = v.getNext();
        }
        u.setTmpNext(endY);
        endY.setTmpPrev(u);
        endY.setTmpIndex(idx + 1);
        changedPoints.add(endY);

        mChangedRouteToFirstTmpPoint.put(routeX, x);
        mChangedRouteToFirstTmpPoint.put(routeY, startY);
        mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
        mChangedRouteToAddedPoints.put(routeX, addedPointsAtRouteX);
        mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
        mChangedRouteToRemovedPoints.put(routeY, addedPointsAtRouteX);

        return explore();
    }

    public void propagateTwoOptMove3(VRPPoint x, VRPPoint y) {
        boolean status = exploreTwoOptMove3(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::propagateTwoOptMove3 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreTwoOptMove4(VRPPoint x, VRPPoint y) {
        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == routeY || routeX == null || routeY == null) {
            return false;
        }
        if (x.isEndPoint() || y.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkTwoOptMove4(x, y)) {
                return false;
            }
        }

        clearTmpData();

        VRPPoint endX = routeX.getEndPoint();
        VRPPoint endY = routeY.getEndPoint();
        VRPPoint startX = routeX.getStartPoint();
        VRPPoint startY = routeY.getStartPoint();
        VRPPoint nextY = y.getNext();
        // routeX: y->x, routeY: nextY -> nextX

        ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRouteX = new ArrayList<>();

        //tmp route x
        VRPPoint u, v;
        u = startX;
        v = startY.getNext();
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != nextY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeX);
            changedPoints.add(v);
            addedPointsAtRouteX.add(v);
            u = v;
            v = v.getNext();
        }
        v = x;
        while (v != startX) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeX);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(endX);
        endX.setTmpPrev(u);
        endX.setTmpIndex(idx + 1);
        changedPoints.add(endX);

        // tmp route y
        u = startY;
        v = endY.getPrev();
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != y) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = x.getNext();
        while (v != endX) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeY);
            changedPoints.add(v);
            addedPointsAtRouteY.add(v);
            u = v;
            v = v.getNext();
        }
        u.setTmpNext(endY);
        endY.setTmpPrev(u);
        endY.setTmpIndex(idx + 1);
        changedPoints.add(endY);

        mChangedRouteToFirstTmpPoint.put(routeX, startX);
        mChangedRouteToFirstTmpPoint.put(routeY, startY);
        mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
        mChangedRouteToAddedPoints.put(routeX, addedPointsAtRouteX);
        mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
        mChangedRouteToRemovedPoints.put(routeY, addedPointsAtRouteX);

        return explore();
    }

    public void propagateTwoOptMove4(VRPPoint x, VRPPoint y) {
        boolean status = exploreTwoOptMove4(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreTwoOptMove4 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreTwoOptMove5(VRPPoint x, VRPPoint y) {
        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == routeY || routeX == null || routeY == null) {
            return false;
        }
        if (x.isEndPoint() || y.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkTwoOptMove5(x, y)) {
                return false;
            }
        }

        clearTmpData();

        VRPPoint endX = routeX.getEndPoint();
        VRPPoint endY = routeY.getEndPoint();
        VRPPoint nextX = x.getNext();
        VRPPoint nextY = y.getNext();

        // routeX: y->x, routeY: nextY -> nextX

        ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRouteX = new ArrayList<>();

        //tmp route x
        VRPPoint u, v;
        u = x;
        v = nextY;
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != endY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeX);
            changedPoints.add(v);
            addedPointsAtRouteX.add(v);
            u = v;
            v = v.getNext();
        }
        u.setTmpNext(endX);
        endX.setTmpPrev(u);
        endX.setTmpIndex(idx + 1);
        changedPoints.add(endX);

        // tmp route y
        u = y;
        v = nextX;
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != endX) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeY);
            changedPoints.add(v);
            addedPointsAtRouteY.add(v);
            u = v;
            v = v.getNext();
        }
        u.setTmpNext(endY);
        endY.setTmpPrev(u);
        endY.setTmpIndex(idx + 1);
        changedPoints.add(endY);

        mChangedRouteToFirstTmpPoint.put(routeX, x);
        mChangedRouteToFirstTmpPoint.put(routeY, y);
        mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
        mChangedRouteToAddedPoints.put(routeX, addedPointsAtRouteX);
        mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
        mChangedRouteToRemovedPoints.put(routeY, addedPointsAtRouteX);

        return explore();
    }

    public void propagateTwoOptMove5(VRPPoint x, VRPPoint y) {
        boolean status = exploreTwoOptMove5(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreTwoOptMove5 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreTwoOptMove6(VRPPoint x, VRPPoint y) {
        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == routeY || routeX == null || routeY == null) {
            return false;
        }
        if (x.isEndPoint() || y.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkTwoOptMove6(x, y)) {
                return false;
            }
        }

        clearTmpData();

        VRPPoint endX = routeX.getEndPoint();
        VRPPoint endY = routeY.getEndPoint();
        VRPPoint nextX = x.getNext();
        VRPPoint nextY = y.getNext();
        VRPPoint startX = routeX.getStartPoint();

        // routeX: y->x, routeY: nextY -> nextX

        ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRouteX = new ArrayList<>();

        //tmp route x
        VRPPoint u, v;
        u = startX;
        v = endY.getPrev();
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != y) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeX);
            changedPoints.add(v);
            addedPointsAtRouteX.add(v);
            u = v;
            v = v.getPrev();
        }
        v = x;
        while (v != startX) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(endX);
        endX.setTmpPrev(u);
        endX.setTmpIndex(idx + 1);
        changedPoints.add(endX);

        // tmp route y
        u = y;
        v = nextX;
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != endX) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeY);
            changedPoints.add(v);
            addedPointsAtRouteY.add(v);
            u = v;
            v = v.getNext();
        }
        u.setTmpNext(endY);
        endY.setTmpPrev(u);
        endY.setTmpIndex(idx + 1);
        changedPoints.add(endY);

        mChangedRouteToFirstTmpPoint.put(routeX, startX);
        mChangedRouteToFirstTmpPoint.put(routeY, y);
        mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
        mChangedRouteToAddedPoints.put(routeX, addedPointsAtRouteX);
        mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
        mChangedRouteToRemovedPoints.put(routeY, addedPointsAtRouteX);

        return explore();
    }

    public void propagateTwoOptMove6(VRPPoint x, VRPPoint y) {
        boolean status = exploreTwoOptMove6(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreTwoOptMove6 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreTwoOptMove7(VRPPoint x, VRPPoint y) {
        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == routeY || routeX == null || routeY == null) {
            return false;
        }
        if (x.isEndPoint() || y.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkTwoOptMove7(x, y)) {
                return false;
            }
        }

        clearTmpData();

        VRPPoint endX = routeX.getEndPoint();
        VRPPoint endY = routeY.getEndPoint();
        VRPPoint nextX = x.getNext();
        VRPPoint nextY = y.getNext();
        VRPPoint startY = routeY.getStartPoint();
        // routeX: y->x, routeY: nextY -> nextX

        ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRouteX = new ArrayList<>();

        //tmp route x
        VRPPoint u, v;
        u = x;
        v = nextY;
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != endY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeX);
            changedPoints.add(v);
            addedPointsAtRouteX.add(v);
            u = v;
            v = v.getNext();
        }
        u.setTmpNext(endX);
        endX.setTmpPrev(u);
        endX.setTmpIndex(idx + 1);
        changedPoints.add(endX);

        // tmp route y
        u = startY;
        v = endX.getPrev();
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != x) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeY);
            changedPoints.add(v);
            addedPointsAtRouteY.add(v);
            u = v;
            v = v.getPrev();
        }
        v = y;
        while (v != startY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(endY);
        endY.setTmpPrev(u);
        endY.setTmpIndex(idx + 1);
        changedPoints.add(endY);

        mChangedRouteToFirstTmpPoint.put(routeX, x);
        mChangedRouteToFirstTmpPoint.put(routeY, startY);
        mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
        mChangedRouteToAddedPoints.put(routeX, addedPointsAtRouteX);
        mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
        mChangedRouteToRemovedPoints.put(routeY, addedPointsAtRouteX);

        return explore();
    }

    public void propagateTwoOptMove7(VRPPoint x, VRPPoint y) {
        boolean status = exploreTwoOptMove7(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreTwoOptMove7 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreTwoOptMove8(VRPPoint x, VRPPoint y) {
        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == routeY || routeX == null || routeY == null) {
            return false;
        }
        if (x.isEndPoint() || y.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkTwoOptMove8(x, y)) {
                return false;
            }
        }

        clearTmpData();

        VRPPoint endX = routeX.getEndPoint();
        VRPPoint endY = routeY.getEndPoint();
        VRPPoint startX = routeX.getStartPoint();
        VRPPoint startY = routeY.getStartPoint();
        // routeX: y->x, routeY: nextY -> nextX

        ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRouteX = new ArrayList<>();

        //tmp route x
        VRPPoint u, v;
        u = startX;
        v = endY.getPrev();
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != y) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeX);
            changedPoints.add(v);
            addedPointsAtRouteX.add(v);
            u = v;
            v = v.getPrev();
        }
        v = x;
        while (v != startX) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(endX);
        endX.setTmpPrev(u);
        endX.setTmpIndex(idx + 1);
        changedPoints.add(endX);

        // tmp route y
        u = startY;
        v = endX.getPrev();
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != x) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeY);
            changedPoints.add(v);
            addedPointsAtRouteY.add(v);
            u = v;
            v = v.getPrev();
        }
        v = y;
        while (v != startY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(endY);
        endY.setTmpPrev(u);
        endY.setTmpIndex(idx + 1);
        changedPoints.add(endY);

        mChangedRouteToFirstTmpPoint.put(routeX, startX);
        mChangedRouteToFirstTmpPoint.put(routeY, startY);
        mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
        mChangedRouteToAddedPoints.put(routeX, addedPointsAtRouteX);
        mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
        mChangedRouteToRemovedPoints.put(routeY, addedPointsAtRouteX);

        return explore();
    }

    public void propagateTwoOptMove8(VRPPoint x, VRPPoint y) {
        boolean status = exploreTwoOptMove8(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreTwoOptMove8 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreTwoOptMoveOneRoute(VRPPoint x, VRPPoint y) {
        if (x.getRoute() == null || x.getRoute() != y.getRoute() || x.getIndex() >= y.getIndex()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkTwoOptMoveOneRoute(x, y)) {
                return false;
            }
        }
        clearTmpData();
        VRPPoint nextY = y.getNext();
        VRPPoint u = x;
        VRPPoint v = y;
        int idx = u.getIndex();
        changedPoints.add(u);
        while (v != x) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(nextY);
        nextY.setTmpPrev(u);
        changedPoints.add(nextY);
        mChangedRouteToFirstTmpPoint.put(x.getRoute(), x);
        return explore();
    }

    public void propagateTwoOptMoveOneRoute(VRPPoint x, VRPPoint y) {
        boolean status = exploreTwoOptMoveOneRoute(x, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::propagateTwoOptMoveOneRoute !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreOrOptMove1(VRPPoint x1, VRPPoint x2, VRPPoint y) {
        if (x1.getRoute() != x2.getRoute() || x1.getIndex() >= x2.getIndex()) {
            return false;
        }
        VRPRoute routeX = x1.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == null || routeY == null || routeX == routeY || y.isEndPoint()) {
            return false;
        }
        if (x1.isStartPoint() || x2.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkOrOptMove1(x1, x2, y)) {
                return false;
            }
        }
        clearTmpData();

        VRPPoint nextX2 = x2.getNext();
        ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();

        VRPPoint u = x1.getPrev();
        VRPPoint v = nextX2;
        int idx = u.getIndex();
        changedPoints.add(u);
        u.setTmpNext(v);
        v.setTmpPrev(u);
        while (v != null) {
            idx++;
            v.setTmpIndex(idx);
            changedPoints.add(v);
            v = v.getNext();
        }

        u = y;
        v = x1;
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != nextX2) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeY);
            changedPoints.add(v);
            addedPointsAtRouteY.add(v);
            u = v;
            v = v.getNext();
        }
        v = y.getNext();
        u.setTmpNext(v);
        v.setTmpPrev(u);
        while (v != null) {
            idx++;
            v.setTmpIndex(idx);
            changedPoints.add(v);
            v = v.getNext();
        }

        mChangedRouteToFirstTmpPoint.put(routeX, x1.getPrev());
        mChangedRouteToFirstTmpPoint.put(routeY, y);
        mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
        mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
        return explore();
    }

    public void propagateOrOptMove1(VRPPoint x1, VRPPoint x2, VRPPoint y) {
        boolean status = exploreOrOptMove1(x1, x2, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreOrOptMove1 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreOrOptMove2(VRPPoint x1, VRPPoint x2, VRPPoint y) {
        if (x1.getRoute() != x2.getRoute() || x1.getIndex() >= x2.getIndex()) {
            return false;
        }
        VRPRoute routeX = x1.getRoute();
        VRPRoute routeY = y.getRoute();
        if (routeX == null || routeY == null || routeX == routeY || y.isEndPoint()) {
            return false;
        }
        if (x1.isStartPoint() || x2.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkOrOptMove2(x1, x2, y)) {
                return false;
            }
        }
        clearTmpData();

        VRPPoint nextX2 = x2.getNext();
        VRPPoint prevX1 = x1.getPrev();
        ArrayList<VRPPoint> addedPointsAtRouteY = new ArrayList<>();

        VRPPoint u = prevX1;
        VRPPoint v = nextX2;
        int idx = u.getIndex();
        changedPoints.add(u);
        u.setTmpNext(v);
        v.setTmpPrev(u);
        while (v != null) {
            idx++;
            v.setTmpIndex(idx);
            changedPoints.add(v);
            v = v.getNext();
        }

        u = y;
        v = x2;
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != prevX1) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(routeY);
            changedPoints.add(v);
            addedPointsAtRouteY.add(v);
            u = v;
            v = v.getPrev();
        }
        v = y.getNext();
        u.setTmpNext(v);
        v.setTmpPrev(u);
        while (v != null) {
            idx++;
            v.setTmpIndex(idx);
            changedPoints.add(v);
            v = v.getNext();
        }

        mChangedRouteToFirstTmpPoint.put(routeX, x1.getPrev());
        mChangedRouteToFirstTmpPoint.put(routeY, y);
        mChangedRouteToAddedPoints.put(routeY, addedPointsAtRouteY);
        mChangedRouteToRemovedPoints.put(routeX, addedPointsAtRouteY);
        return explore();
    }

    public void propagateOrOptMove2(VRPPoint x1, VRPPoint x2, VRPPoint y) {
        boolean status = exploreOrOptMove2(x1, x2, y);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreOrOptMove2 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreThreeOptMove1(VRPPoint x, VRPPoint y, VRPPoint z) {
        if (x.getRoute() == null || x.getRoute() != y.getRoute() || y.getRoute() != z.getRoute()) {
            return false;
        }
        if (x.getIndex() >= y.getIndex() || y.getIndex() >= z.getIndex()) {
            return false;
        }
        if (z.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkThreeOptMove1(x, y, z)) {
                return false;
            }
        }

        clearTmpData();

        // s --> x -> z --> ny -> nx --> y -> nz --> e
        VRPPoint u = x;
        VRPPoint v = z;
        int idx = u.getIndex();
        changedPoints.add(u);
        while (v != y) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = x.getNext();
        VRPPoint nextY = y.getNext();
        while (v != nextY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }
        v = z.getNext();
        u.setTmpNext(v);
        v.setTmpPrev(u);
        changedPoints.add(v);
        mChangedRouteToFirstTmpPoint.put(x.getRoute(), x);
        return explore();
    }

    public void propagateThreeOptMove1(VRPPoint x, VRPPoint y, VRPPoint z) {
        boolean status = exploreThreeOptMove1(x, y, z);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreThreeOptMove1 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreThreeOptMove2(VRPPoint x, VRPPoint y, VRPPoint z) {
        if (x.getRoute() == null || x.getRoute() != y.getRoute() || y.getRoute() != z.getRoute()) {
            return false;
        }
        if (x.getIndex() >= y.getIndex() || y.getIndex() >= z.getIndex()) {
            return false;
        }
        if (z.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkThreeOptMove2(x, y, z)) {
                return false;
            }
        }

        clearTmpData();

        // s --> x -> z --> ny -> nx --> y -> nz --> e
        VRPRoute route = x.getRoute();
        VRPPoint start = route.getStartPoint();
        VRPPoint end = route.getEndPoint();
        VRPPoint u = start;
        VRPPoint v = end.getPrev();
        int idx = u.getIndex();
        changedPoints.add(u);
        while (v != z) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = y;
        while (v != x) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = y.getNext();
        VRPPoint nextZ = z.getNext();
        while (v != nextZ) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }
        v = x;
        while (v != start) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(end);
        end.setTmpPrev(u);
        changedPoints.add(end);
        mChangedRouteToFirstTmpPoint.put(x.getRoute(), start);
        return explore();
    }

    public void propagateThreeOptMove2(VRPPoint x, VRPPoint y, VRPPoint z) {
        boolean status = exploreThreeOptMove2(x, y, z);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreThreeOptMove2 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreThreeOptMove3(VRPPoint x, VRPPoint y, VRPPoint z) {
        if (x.getRoute() == null || x.getRoute() != y.getRoute() || y.getRoute() != z.getRoute()) {
            return false;
        }
        if (x.getIndex() >= y.getIndex() || y.getIndex() >= z.getIndex()) {
            return false;
        }
        if (z.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkThreeOptMove3(x, y, z)) {
                return false;
            }
        }

        clearTmpData();

        // s --> x -> z --> ny -> nx --> y -> nz --> e
        VRPPoint u = x;
        VRPPoint v = y;
        int idx = u.getIndex();
        changedPoints.add(u);
        while (v != x) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = z;
        while (v != y) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = z.getNext();
        u.setTmpNext(v);
        v.setTmpPrev(u);
        changedPoints.add(v);
        mChangedRouteToFirstTmpPoint.put(x.getRoute(), x);
        return explore();
    }

    public void propagateThreeOptMove3(VRPPoint x, VRPPoint y, VRPPoint z) {
        boolean status = exploreThreeOptMove3(x, y, z);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreThreeOptMove3 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreThreeOptMove4(VRPPoint x, VRPPoint y, VRPPoint z) {
        if (x.getRoute() == null || x.getRoute() != y.getRoute() || y.getRoute() != z.getRoute()) {
            return false;
        }
        if (x.getIndex() >= y.getIndex() || y.getIndex() >= z.getIndex()) {
            return false;
        }
        if (z.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkThreeOptMove4(x, y, z)) {
                return false;
            }
        }

        clearTmpData();

        // s --> x -> z --> ny -> nx --> y -> nz --> e
        VRPRoute route = x.getRoute();
        VRPPoint start = route.getStartPoint();
        VRPPoint end = route.getEndPoint();
        VRPPoint u = start;
        VRPPoint v = end.getPrev();
        int idx = u.getIndex();
        changedPoints.add(u);
        while (v != z) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = y.getNext();
        VRPPoint nextZ = z.getNext();
        while (v != nextZ) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }
        v = x.getNext();
        VRPPoint nextY = y.getNext();
        while (v != nextY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }
        v = x;
        while (v != start) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(end);
        end.setTmpPrev(u);
        changedPoints.add(end);
        mChangedRouteToFirstTmpPoint.put(x.getRoute(), start);
        return explore();
    }

    public void propagateThreeOptMove4(VRPPoint x, VRPPoint y, VRPPoint z) {
        boolean status = exploreThreeOptMove4(x, y, z);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreThreeOptMove4 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreThreeOptMove5(VRPPoint x, VRPPoint y, VRPPoint z) {
        if (x.getRoute() == null || x.getRoute() != y.getRoute() || y.getRoute() != z.getRoute()) {
            return false;
        }
        if (x.getIndex() >= y.getIndex() || y.getIndex() >= z.getIndex()) {
            return false;
        }
        if (z.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkThreeOptMove5(x, y, z)) {
                return false;
            }
        }

        clearTmpData();

        // s --> x -> z --> ny -> nx --> y -> nz --> e
        VRPPoint nextY = y.getNext();
        VRPPoint nextZ = z.getNext();
        VRPPoint u = x;
        VRPPoint v = nextY;
        int idx = u.getIndex();
        changedPoints.add(u);
        while (v != nextZ) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }
        v = x.getNext();
        while (v != nextY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }
        v = z.getNext();
        u.setTmpNext(v);
        v.setTmpPrev(u);
        changedPoints.add(v);
        mChangedRouteToFirstTmpPoint.put(x.getRoute(), x);
        return explore();
    }

    public void propagateThreeOptMove5(VRPPoint x, VRPPoint y, VRPPoint z) {
        boolean status = exploreThreeOptMove5(x, y, z);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreThreeOptMove5 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreThreeOptMove6(VRPPoint x, VRPPoint y, VRPPoint z) {
        if (x.getRoute() == null || x.getRoute() != y.getRoute() || y.getRoute() != z.getRoute()) {
            return false;
        }
        if (x.getIndex() >= y.getIndex() || y.getIndex() >= z.getIndex()) {
            return false;
        }
        if (z.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkThreeOptMove6(x, y, z)) {
                return false;
            }
        }

        clearTmpData();

        // s --> x -> z --> ny -> nx --> y -> nz --> e
        VRPRoute route = x.getRoute();
        VRPPoint start = route.getStartPoint();
        VRPPoint end = route.getEndPoint();
        VRPPoint u = start;
        VRPPoint v = end.getPrev();
        int idx = u.getIndex();
        changedPoints.add(u);
        while (v != z) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = y;
        while (v != x) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = z;
        while (v != y) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = x;
        while (v != start) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(end);
        end.setTmpPrev(u);
        changedPoints.add(end);
        mChangedRouteToFirstTmpPoint.put(x.getRoute(), start);
        return explore();
    }

    public void propagateThreeOptMove6(VRPPoint x, VRPPoint y, VRPPoint z) {
        boolean status = exploreThreeOptMove6(x, y, z);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreThreeOptMove6 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreThreeOptMove7(VRPPoint x, VRPPoint y, VRPPoint z) {
        if (x.getRoute() == null || x.getRoute() != y.getRoute() || y.getRoute() != z.getRoute()) {
            return false;
        }
        if (x.getIndex() >= y.getIndex() || y.getIndex() >= z.getIndex()) {
            return false;
        }
        if (z.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkThreeOptMove7(x, y, z)) {
                return false;
            }
        }

        clearTmpData();

        // s --> x -> z --> ny -> nx --> y -> nz --> e
        VRPPoint nextY = y.getNext();
        VRPPoint nextZ = z.getNext();
        VRPPoint u = x;
        VRPPoint v = nextY;
        int idx = u.getIndex();
        changedPoints.add(u);
        while (v != nextZ) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }
        v = y;
        while (v != x) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = z.getNext();
        u.setTmpNext(v);
        v.setTmpPrev(u);
        changedPoints.add(v);
        mChangedRouteToFirstTmpPoint.put(x.getRoute(), x);
        return explore();
    }

    public void propagateThreeOptMove7(VRPPoint x, VRPPoint y, VRPPoint z) {
        boolean status = exploreThreeOptMove7(x, y, z);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreThreeOptMove7 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreThreeOptMove8(VRPPoint x, VRPPoint y, VRPPoint z) {
        if (x.getRoute() == null || x.getRoute() != y.getRoute() || y.getRoute() != z.getRoute()) {
            return false;
        }
        if (x.getIndex() >= y.getIndex() || y.getIndex() >= z.getIndex()) {
            return false;
        }
        if (z.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkThreeOptMove8(x, y, z)) {
                return false;
            }
        }

        clearTmpData();

        // s --> x -> z --> ny -> nx --> y -> nz --> e
        VRPRoute route = x.getRoute();
        VRPPoint start = route.getStartPoint();
        VRPPoint end = route.getEndPoint();
        VRPPoint u = start;
        VRPPoint v = end.getPrev();
        int idx = u.getIndex();
        changedPoints.add(u);
        while (v != z) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = x.getNext();
        VRPPoint nextY = y.getNext();
        while (v != nextY) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }
        v = z;
        while (v != y) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        v = x;
        while (v != start) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getPrev();
        }
        u.setTmpNext(end);
        end.setTmpPrev(u);
        changedPoints.add(end);
        mChangedRouteToFirstTmpPoint.put(x.getRoute(), start);
        return explore();
    }

    public void propagateThreeOptMove8(VRPPoint x, VRPPoint y, VRPPoint z) {
        boolean status = exploreThreeOptMove8(x, y, z);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreThreeOptMove8 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreCrossExchangeMove1(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2) {
        VRPRoute route1 = x1.getRoute();
        VRPRoute route2 = x2.getRoute();
        if (route1 == route2) {
            return false;
        }
        if (route1 == null || route1 != y1.getRoute() || x1.getIndex() >= y1.getIndex() || y1.isEndPoint()) {
            return false;
        }
        if (route2 == null || route2 != y2.getRoute() || x2.getIndex() >= y2.getIndex() || y2.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkCrossExchangeMove1(x1, y1, x2, y2)) {
                return false;
            }
        }
        clearTmpData();

        ArrayList<VRPPoint> addedPointsAtRoute1 = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRoute2 = new ArrayList<>();

        VRPPoint nextY2 = y2.getNext();
        VRPPoint nextY1 = y1.getNext();
        VRPPoint u, v;
        u = x1;
        v = x2.getNext();
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != nextY2) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(route1);
            changedPoints.add(v);
            addedPointsAtRoute1.add(v);
            u = v;
            v = v.getNext();
        }
        v = nextY1;
        while (v != null) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }

        u = x2;
        v = x1.getNext();
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != nextY1) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(route2);
            changedPoints.add(v);
            addedPointsAtRoute2.add(v);
            u = v;
            v = v.getNext();
        }
        v = nextY2;
        while (v != null) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }

        mChangedRouteToFirstTmpPoint.put(route1, x1);
        mChangedRouteToFirstTmpPoint.put(route2, x2);
        mChangedRouteToAddedPoints.put(route1, addedPointsAtRoute1);
        mChangedRouteToAddedPoints.put(route2, addedPointsAtRoute2);
        mChangedRouteToRemovedPoints.put(route1, addedPointsAtRoute2);
        mChangedRouteToRemovedPoints.put(route2, addedPointsAtRoute1);

        return explore();
    }

    public void propagateCrossExchangeMove1(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2) {
        boolean status = exploreCrossExchangeMove1(x1, y1, x2, y2);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreCrossExchangeMove1 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreCrossExchangeMove2(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2) {
        VRPRoute route1 = x1.getRoute();
        VRPRoute route2 = x2.getRoute();
        if (route1 == route2) {
            return false;
        }
        if (route1 == null || route1 != y1.getRoute() || x1.getIndex() >= y1.getIndex() || y1.isEndPoint()) {
            return false;
        }
        if (route2 == null || route2 != y2.getRoute() || x2.getIndex() >= y2.getIndex() || y2.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkCrossExchangeMove2(x1, y1, x2, y2)) {
                return false;
            }
        }
        clearTmpData();

        ArrayList<VRPPoint> addedPointsAtRoute1 = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRoute2 = new ArrayList<>();

        VRPPoint nextY2 = y2.getNext();
        VRPPoint nextY1 = y1.getNext();
        VRPPoint u, v;
        u = x1;
        v = y2;
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != x2) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(route1);
            changedPoints.add(v);
            addedPointsAtRoute1.add(v);
            u = v;
            v = v.getPrev();
        }
        v = nextY1;
        while (v != null) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }

        u = x2;
        v = x1.getNext();
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != nextY1) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(route2);
            changedPoints.add(v);
            addedPointsAtRoute2.add(v);
            u = v;
            v = v.getNext();
        }
        v = nextY2;
        while (v != null) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }

        mChangedRouteToFirstTmpPoint.put(route1, x1);
        mChangedRouteToFirstTmpPoint.put(route2, x2);
        mChangedRouteToAddedPoints.put(route1, addedPointsAtRoute1);
        mChangedRouteToAddedPoints.put(route2, addedPointsAtRoute2);
        mChangedRouteToRemovedPoints.put(route1, addedPointsAtRoute2);
        mChangedRouteToRemovedPoints.put(route2, addedPointsAtRoute1);

        return explore();
    }

    public void propagateCrossExchangeMove2(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2) {
        boolean status = exploreCrossExchangeMove2(x1, y1, x2, y2);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreCrossExchangeMove2 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreCrossExchangeMove3(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2) {
        VRPRoute route1 = x1.getRoute();
        VRPRoute route2 = x2.getRoute();
        if (route1 == route2) {
            return false;
        }
        if (route1 == null || route1 != y1.getRoute() || x1.getIndex() >= y1.getIndex() || y1.isEndPoint()) {
            return false;
        }
        if (route2 == null || route2 != y2.getRoute() || x2.getIndex() >= y2.getIndex() || y2.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkCrossExchangeMove3(x1, y1, x2, y2)) {
                return false;
            }
        }
        clearTmpData();

        ArrayList<VRPPoint> addedPointsAtRoute1 = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRoute2 = new ArrayList<>();

        VRPPoint nextY2 = y2.getNext();
        VRPPoint nextY1 = y1.getNext();
        VRPPoint u, v;
        u = x1;
        v = x2.getNext();
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != nextY2) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(route1);
            changedPoints.add(v);
            addedPointsAtRoute1.add(v);
            u = v;
            v = v.getNext();
        }
        v = nextY1;
        while (v != null) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }

        u = x2;
        v = y1;
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != x1) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(route2);
            changedPoints.add(v);
            addedPointsAtRoute2.add(v);
            u = v;
            v = v.getPrev();
        }
        v = nextY2;
        while (v != null) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }

        mChangedRouteToFirstTmpPoint.put(route1, x1);
        mChangedRouteToFirstTmpPoint.put(route2, x2);
        mChangedRouteToAddedPoints.put(route1, addedPointsAtRoute1);
        mChangedRouteToAddedPoints.put(route2, addedPointsAtRoute2);
        mChangedRouteToRemovedPoints.put(route1, addedPointsAtRoute2);
        mChangedRouteToRemovedPoints.put(route2, addedPointsAtRoute1);

        return explore();
    }

    public void propagateCrossExchangeMove3(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2) {
        boolean status = exploreCrossExchangeMove3(x1, y1, x2, y2);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreCrossExchangeMove3 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreCrossExchangeMove4(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2) {
        VRPRoute route1 = x1.getRoute();
        VRPRoute route2 = x2.getRoute();
        if (route1 == route2) {
            return false;
        }
        if (route1 == null || route1 != y1.getRoute() || x1.getIndex() >= y1.getIndex() || y1.isEndPoint()) {
            return false;
        }
        if (route2 == null || route2 != y2.getRoute() || x2.getIndex() >= y2.getIndex() || y2.isEndPoint()) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkCrossExchangeMove4(x1, y1, x2, y2)) {
                return false;
            }
        }
        clearTmpData();

        ArrayList<VRPPoint> addedPointsAtRoute1 = new ArrayList<>();
        ArrayList<VRPPoint> addedPointsAtRoute2 = new ArrayList<>();

        VRPPoint nextY2 = y2.getNext();
        VRPPoint nextY1 = y1.getNext();
        VRPPoint u, v;
        u = x1;
        v = y2;
        changedPoints.add(u);
        int idx = u.getIndex();
        while (v != x2) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(route1);
            changedPoints.add(v);
            addedPointsAtRoute1.add(v);
            u = v;
            v = v.getPrev();
        }
        v = nextY1;
        while (v != null) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }

        u = x2;
        v = y1;
        idx = u.getIndex();
        changedPoints.add(u);
        while (v != x1) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            v.setTmpRoute(route2);
            changedPoints.add(v);
            addedPointsAtRoute2.add(v);
            u = v;
            v = v.getPrev();
        }
        v = nextY2;
        while (v != null) {
            idx++;
            u.setTmpNext(v);
            v.setTmpPrev(u);
            v.setTmpIndex(idx);
            changedPoints.add(v);
            u = v;
            v = v.getNext();
        }

        mChangedRouteToFirstTmpPoint.put(route1, x1);
        mChangedRouteToFirstTmpPoint.put(route2, x2);
        mChangedRouteToAddedPoints.put(route1, addedPointsAtRoute1);
        mChangedRouteToAddedPoints.put(route2, addedPointsAtRoute2);
        mChangedRouteToRemovedPoints.put(route1, addedPointsAtRoute2);
        mChangedRouteToRemovedPoints.put(route2, addedPointsAtRoute1);

        return explore();
    }

    public void propagateCrossExchangeMove4(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2) {
        boolean status = exploreCrossExchangeMove4(x1, y1, x2, y2);
        if (status) {
            propagate();
        } else {
            System.out.println("EXCEPTION::exploreCrossExchangeMove4 !!!!");
            System.exit(-1);
        }
    }

    public boolean exploreKPointsMove(ArrayList<VRPPoint> x, ArrayList<VRPPoint> y) {
        if (x.size() != y.size()) {
            return false;
        }
        for (VRPPoint p : y) {
            if (p == null) {
                return false;
            }
            if (p != CBLSVRP.NULL_POINT) {
                if (p.getRoute() == null || p.isEndPoint()) {
                    return false;
                }
            }
        }
        HashSet<VRPPoint> points = new HashSet<>(y);
        for (VRPPoint p : x) {
            if (p == null || p.isDepot() || points.contains(p)) {
                return false;
            }
            points.add(p);
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkKPointsMove(x, y)) {
                return false;
            }
        }
        clearTmpData();

        for (int i = x.size() - 1; i >= 0; i--) {
            VRPPoint u = x.get(i);
            VRPPoint v = y.get(i);
            VRPRoute routeU = u.getRoute();
            VRPRoute routeV = v.getRoute();
            if (routeU != null) {
                VRPPoint nextU = u.getTmpNext();
                VRPPoint prevU = u.getTmpPrev();
                nextU.setTmpPrev(prevU);
                prevU.setTmpNext(nextU);
                if (mChangedRouteToFirstTmpPoint.containsKey(routeU)) {
                    if (prevU.getIndex() < mChangedRouteToFirstTmpPoint.get(routeU).getIndex()) {
                        mChangedRouteToFirstTmpPoint.put(routeU, prevU);
                    }
                } else {
                    mChangedRouteToFirstTmpPoint.put(routeU, prevU);
                }
            }
            if (v != CBLSVRP.NULL_POINT) {
                VRPPoint nextV = v.getTmpNext();
                v.setTmpNext(u);
                u.setTmpPrev(v);
                u.setTmpNext(nextV);
                nextV.setTmpPrev(u);
                if (mChangedRouteToFirstTmpPoint.containsKey(routeV)) {
                    if (v.getIndex() < mChangedRouteToFirstTmpPoint.get(routeV).getIndex()) {
                        mChangedRouteToFirstTmpPoint.put(routeV, v);
                    }
                } else {
                    mChangedRouteToFirstTmpPoint.put(routeV, v);
                }
                u.setTmpRoute(routeV);
            } else {
                u.setTmpIndex(0);
                u.setTmpNext(null);
                u.setTmpPrev(null);
                u.setTmpRoute(null);
                changedPoints.add(u);
            }
            if (routeU != routeV) {
                if (routeU != null) {
                    if (!mChangedRouteToRemovedPoints.containsKey(routeU)) {
                        mChangedRouteToRemovedPoints.put(routeU, new ArrayList<>());
                    }
                    mChangedRouteToRemovedPoints.get(routeU).add(u);
                }
                if (v != CBLSVRP.NULL_POINT) {
                    if (!mChangedRouteToAddedPoints.containsKey(routeV)) {
                        mChangedRouteToAddedPoints.put(routeV, new ArrayList<>());
                    }
                    mChangedRouteToAddedPoints.get(routeV).add(u);
                }
            }
        }
        for (VRPPoint u : mChangedRouteToFirstTmpPoint.values()) {
            int idx = u.getIndex();
            while (u != null) {
                u.setTmpIndex(idx);
                changedPoints.add(u);
                u = u.getTmpNext();
                idx++;
            }
        }
        return explore();
    }

    private void clearTmpData() {
        for (VRPPoint p : changedPoints) {
            p.initTmp();
        }
        for (VRPRoute r : mChangedRouteToFirstTmpPoint.keySet()) {
            r.initTmp();
        }
        changedPoints.clear();
        mChangedRouteToFirstTmpPoint.clear();
        mChangedRouteToRemovedPoints.clear();
        mChangedRouteToAddedPoints.clear();
        removedPoints.clear();
        addedPoints.clear();
    }

    private boolean explore() {
        dependentInvariants.clear();
        dependentInvariants.addAll(globalInvariants);
        for (VRPPoint u : changedPoints) {
            if (mPoint2LocalInvariants.containsKey(u)) {
                dependentInvariants.addAll(mPoint2LocalInvariants.get(u));
            }
        }

        dependentInvariantLst.clear();
        dependentInvariantLst.addAll(dependentInvariants);
        dependentInvariantLst.sort(invariantCmp);
        for (IVRPInvariant invariant : dependentInvariantLst) {
            invariant.explore();
            if (satisfiedConstraints.get(invariant.getStt())) {
                // check lại các ràng buộc bắt buộc phải thỏa mãn
                IVRPFunction constraint = (IVRPFunction) invariant;
                if (Math.abs(constraint.getTmpValue() - constraint.getValue()) > CBLSVRP.EPS) {
                    return false;
                }
            }
        }
        return true;
    }

    private void propagate() {
        Collections.reverse(dependentInvariantLst);
        for (IVRPInvariant invariant : dependentInvariantLst) {
            invariant.propagate();
        }
        for (VRPPoint p : changedPoints) {
            p.propagate();
        }
        for (VRPRoute route : mChangedRouteToFirstTmpPoint.keySet()) {
            route.propagate();
        }
        // xóa đi khi ko muốn mất thời gian verify lại các invariants
        if (!verify()) {
            System.out.println("ERORR");
            System.exit(-1);
        }
    }

    private boolean verify() {
        for (VRPRoute route : allRoutes) {
            VRPPoint p = route.getStartPoint();
            if (route.getStartPoint().getRoute() != route || route.getEndPoint().getRoute() != route) {
                System.out.println("EXCEPTION:: verify -> startPoint and endPoint don't have the same route " + route);
                return false;
            }
            int cnt = 0;
            while (p.getNext() != null) {
                p = p.getNext();
                if (p.getRoute() != route) {
                    System.out.println("EXCEPTION:: verify -> route of " + p + " is not " + route + "; " + p.getRoute());
                    return false;
                }
                cnt++;
            }
            if (route.getNbPoints() != cnt - 1) {
                System.out.println("EXCEPTION:: verify -> calculating the number of points on route is incorrect" + (cnt - 1) + " " + route.getNbPoints());
                return false;
            }
            if (route.getEndPoint().getIndex() - route.getStartPoint().getIndex() != cnt) {
                System.out.println("EXCEPTION:: verify -> calculating the number of points on route is incorrect" + cnt + " " + route.getEndPoint().getIndex() + " " + route.getStartPoint().getIndex());
                return false;
            }

        }
        for (IVRPInvariant invariant : invariants) {
            if (!invariant.verify()) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Scanner cin = new Scanner(new FileInputStream("data\\VRPTW"));
        int nbTrucks = cin.nextInt();
        int capacity = cin.nextInt();
        int nbCustomers = cin.nextInt();
        System.out.println(nbTrucks + " " + capacity + " " + nbCustomers);
        int[] demands = new int[nbCustomers + 1];
        int[] xs = new int[nbCustomers + 1];
        int[] ys = new int[nbCustomers + 1];
        int[] earliestTimes = new int[nbCustomers + 1];
        int[] latestTimes = new int[nbCustomers + 1];
        int[] serviceTimes = new int[nbCustomers + 1];
        for (int i = 0; i <= nbCustomers; i++) {
            int id = cin.nextInt();
            xs[i] = cin.nextInt();
            ys[i] = cin.nextInt();
            demands[i] = cin.nextInt();
            earliestTimes[i] = cin.nextInt();
            latestTimes[i] = cin.nextInt();
            serviceTimes[i] = cin.nextInt();
            System.out.println(id + " " + xs[i] + " " + ys[i] + " " + demands[i] + " " + earliestTimes[i] + " " + latestTimes[i] + " " + serviceTimes[i]);
        }

        HashMap<VRPRoute, Double> mRoute2Capacity = new HashMap<>();
        HashMap<VRPPoint, Double> nodeWeightMap = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> travelTimeMap = new HashMap<>();
        HashMap<VRPPoint, Integer> serviceTimeMap = new HashMap<>();
        HashMap<VRPPoint, Integer> earliestArrivalTimeMap = new HashMap<>();
        HashMap<VRPPoint, Integer> lastestArrivalTimeMap = new HashMap<>();
        ArrayList<VRPPoint> insertedPoints = new ArrayList<>();
        ArrayList<VRPPoint> addPoints = new ArrayList<>();

        VRPVarRoutes vr = new VRPVarRoutes();
        for (int i = 1; i <= nbTrucks; i++) {
            VRPRoute route = vr.createRoute("0", "0", "" + i);
            mRoute2Capacity.put(route, (double)capacity);
            insertedPoints.add(route.getStartPoint());
        }
        for (int i = 1; i <= nbCustomers; i++) {
            VRPPoint point = vr.createPoint("" + i);
            nodeWeightMap.put(point, (double)demands[i]);
            addPoints.add(point);
        }

        for (int i = 0; i <= nbCustomers; i++) {
            travelTimeMap.put("" + i, new HashMap<>());
            HashMap<String, Integer> mToPoint2TravelTime = travelTimeMap.get("" + i);
            for (int j = 0; j <= nbCustomers; j++) {
                double dx = xs[i] - xs[j];
                double dy = ys[i] - ys[j];
                mToPoint2TravelTime.put("" + j, (int)Math.ceil(Math.sqrt(dx * dx + dy * dy)));
            }
        }

        for (VRPPoint point : vr.getAllPoints()) {
            int id = Integer.parseInt(point.getLocationCode());
            serviceTimeMap.put(point, serviceTimes[id]);
            earliestArrivalTimeMap.put(point, earliestTimes[id]);
            lastestArrivalTimeMap.put(point, latestTimes[id]);
        }

        CapacityConstraint cc = new CapacityConstraint(vr, nodeWeightMap, mRoute2Capacity);
        TimeWindowsConstraint tw = new TimeWindowsConstraint(vr, travelTimeMap, earliestArrivalTimeMap, lastestArrivalTimeMap);
        AccumulatedWeightPoints accWP = new AccumulatedWeightPoints(new AccumulatedWeightCalculator(vr, nodeWeightMap));
        for (VRPRoute route : vr.getAllRoutes()) {
            AccumulatedPointWeightsOnPath totalWeightOfRoute = new AccumulatedPointWeightsOnPath(accWP, route.getEndPoint());
            Leq subCC = new Leq(totalWeightOfRoute, capacity);
        }

        Random rand = new Random(1993);
        for (VRPPoint point : nodeWeightMap.keySet()) {
            VRPPoint y = insertedPoints.get(rand.nextInt(insertedPoints.size()));
            System.out.println("insert " + point.getLocationCode() + " after " + y.getLocationCode());
            vr.propageteOnePointMove(point, y);
            insertedPoints.add(point);
        }
//        for (int step = 0; step < 100000; step++) {
//            VRPPoint x = addPoints.get(rand.nextInt(addPoints.size()));
//            VRPPoint y = insertedPoints.get(rand.nextInt(insertedPoints.size()));
//            if (x != y) {
//                System.out.println("add " + x.getLocationCode() + " after " + y.getLocationCode());
//                vr.propageteOnePointMove(x, y);
//            }
//        }
//        for (int step = 0; step < 100000; step++) {
//            VRPPoint x = addPoints.get(rand.nextInt(addPoints.size()));
//            VRPPoint y = addPoints.get(rand.nextInt(addPoints.size()));
//            if (x != y) {
//                System.out.println("swap " + x + " and " + y + " " + x.getRoute() + " " + y.getRoute());
//                vr.propageteTwoPointsMove(x, y);
//            }
//        }

        for (int step = 0; step < 100000000; step++) {
            VRPPoint x1 = insertedPoints.get(rand.nextInt(insertedPoints.size()));
            VRPPoint y1 = addPoints.get(rand.nextInt(addPoints.size()));
            VRPPoint x2 = insertedPoints.get(rand.nextInt(insertedPoints.size()));
            VRPPoint y2 = addPoints.get(rand.nextInt(addPoints.size()));
            if (x1.getRoute() == y1.getRoute() && x1.getIndex() < y1.getIndex() &&
                    x2.getRoute() == y2.getRoute() && x2.getIndex() < y2.getIndex() && x1.getRoute() != x2.getRoute()) {
                System.out.println("propagateCrossExchangeMove1 " + x1 + "; " + y1 + "; " + x2 + " and " + y2 + " " + x1.getRoute() + "; " + x2.getRoute());
                int olcCnt = x1.getRoute().getNbPoints() + x2.getRoute().getNbPoints();
//                VRPPoint p = x.getRoute().getStartPoint();
//                while (p != null) {
//                    System.out.print(p + " -> ");
//                    p = p.getNext();
//                }
//                System.out.println();
//                p = y.getRoute().getStartPoint();
//                while (p != null) {
//                    System.out.print(p + " -> ");
//                    p = p.getNext();
//                }
//                System.out.println();
                vr.propagateCrossExchangeMove4(x1, y1, x2, y2);
                int newCnt = x1.getRoute().getNbPoints() + x2.getRoute().getNbPoints();
                if (olcCnt != newCnt) {
                    System.out.println("debg");
                    System.exit(-1);
                }
            }
        }

    }
}