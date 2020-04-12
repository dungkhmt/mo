package com.socolabs.mo.vrplib.core;

import com.socolabs.mo.vrplib.constraints.capacity.CapacityConstraint;
import com.socolabs.mo.vrplib.constraints.leq.Leq;
import com.socolabs.mo.vrplib.constraints.timewindows.TimeWindowsConstraint;
import com.socolabs.mo.vrplib.entities.InvariantSttCmp;
import com.socolabs.mo.vrplib.entities.accumulatedcalculators.AccumulatedWeightCalculator;
import com.socolabs.mo.vrplib.functions.AccumulatedPointWeightsOnPath;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.vrplib.utils.CBLSVRP;
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

        routeY.increaseTmpNbPoints(1);

        // các points được thêm vào các routes
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
        for (IVRPChecker checker : checkers) {
            if (!checker.checkOnePointMove(x, y)) {
                return false;
            }
        }
        clearTmpData();

        VRPRoute routeX = x.getRoute();
        VRPRoute routeY = y.getRoute();

        VRPPoint prevX = x.getPrev();
        VRPPoint nextX = x.getNext();
        VRPPoint nextY = y.getNext();
        VRPPoint p;

        nextX.setTmpPrev(prevX);
        prevX.setTmpNext(nextX);
        x.setTmpNext(nextY);
        nextY.setTmpPrev(x);
        x.setTmpPrev(y);
        y.setTmpNext(x);
        x.setTmpRoute(routeY);

        if (routeX != routeY) {
            routeX.decreaseTmpNbPoints(1);
            routeY.increaseTmpNbPoints(1);

            p = nextX;
            int index = prevX.getIndex();
            while (p != null) {
                index++;
                p.setTmpIndex(index);
                changedPoints.add(p);
                p = p.getTmpNext();
            }
            changedPoints.add(prevX);

            p = x;
            index = y.getIndex();
            while (p != null) {
                index++;
                p.setTmpIndex(index);
                changedPoints.add(p);
                p = p.getTmpNext();
            }
            changedPoints.add(y);

            mChangedRouteToFirstTmpPoint.put(routeY, y);
            mChangedRouteToFirstTmpPoint.put(routeX, prevX);

            ArrayList<VRPPoint> addedPoints = new ArrayList<>();
            addedPoints.add(x);
            mChangedRouteToAddedPoints.put(routeY, addedPoints);
            mChangedRouteToRemovedPoints.put(routeX, addedPoints);
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

    private void clearTmpData() {
        for (VRPPoint p : changedPoints) {
            p.initTmp();
        }
        for (VRPRoute r : mChangedRouteToAddedPoints.keySet()) {
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
        // xóa đi khi ko muốn mất thời gian verify lại các invariants
        if (!verify()) {
            System.out.println("ERORR");
            System.exit(-1);
        }
    }

    private boolean verify() {
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
            vr.propageteInsertPointMove(point, y);
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
    }
}