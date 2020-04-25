package com.socolabs.mo.vrplib.core;

import com.socolabs.mo.vrplib.constraints.capacity.CapacityConstraint;
import com.socolabs.mo.vrplib.constraints.leq.Leq;
import com.socolabs.mo.vrplib.constraints.timewindows.TimeWindowsConstraint;
import com.socolabs.mo.vrplib.entities.InvariantSttCmp;
import com.socolabs.mo.vrplib.entities.accumulatedcalculators.AccumulatedWeightCalculator;
import com.socolabs.mo.vrplib.functions.AccumulatedPointWeightsOnPath;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.vrplib.utils.CBLSVRP;
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
        for (int step = 0; step < 100000; step++) {
            VRPPoint x = addPoints.get(rand.nextInt(addPoints.size()));
            VRPPoint y = insertedPoints.get(rand.nextInt(insertedPoints.size()));
            if (x != y) {
                System.out.println("add " + x.getLocationCode() + " after " + y.getLocationCode());
                vr.propageteOnePointMove(x, y);
            }
        }
        for (int step = 0; step < 100000; step++) {
            VRPPoint x = addPoints.get(rand.nextInt(addPoints.size()));
            VRPPoint y = addPoints.get(rand.nextInt(addPoints.size()));
            if (x != y) {
                System.out.println("swap " + x + " and " + y + " " + x.getRoute() + " " + y.getRoute());
                vr.propageteTwoPointsMove(x, y);
            }
        }

        for (int step = 0; step < 10000000; step++) {
            VRPPoint x1 = addPoints.get(rand.nextInt(addPoints.size()));
            VRPPoint x2 = addPoints.get(rand.nextInt(addPoints.size()));
            VRPPoint y = insertedPoints.get(rand.nextInt(insertedPoints.size()));
            if (x1.getIndex() < x2.getIndex() && x1.getRoute() == x2.getRoute() && y.getRoute() != x1.getRoute()) {
                System.out.println("propagateOrOptMove1 " + x1 + "; " + x2 + " and " + y + " " + x1.getRoute() + " " + y.getRoute());
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
                vr.propagateOrOptMove1(x1, x2, y);
            }
        }

    }
}