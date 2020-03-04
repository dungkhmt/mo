package com.socolabs.mo.vrplib.core;

import com.socolabs.mo.vrplib.utils.CBLSVRP;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Getter
public class VarRoutesVRP {
    private int pointId;
    private int routeId;

    private ArrayList<PointVRP> allPoints;
    private ArrayList<RouteVRP> allRoutes;
    private HashMap<String, PointVRP> mTruckCode2FirstPoint;
    private HashMap<String, PointVRP> mTruckCode2LastPoint;

    private ArrayList<InvariantVRP> invariants;
    private ArrayList<IcheckerVRP> checkers;

    public VarRoutesVRP() {
        pointId = routeId = 0;
        allPoints = new ArrayList<>();
        invariants = new ArrayList<>();
        checkers = new ArrayList<>();
        mTruckCode2FirstPoint = new HashMap<>();
        mTruckCode2LastPoint = new HashMap<>();
    }

    public void post(InvariantVRP invariant) {
        invariants.add(invariant);
    }

    public void post(IcheckerVRP checker) {
        checkers.add(checker);
    }

    public void remove(InvariantVRP invariant) {
        invariants.remove(invariant);
    }

    public void remove(IcheckerVRP checker) {
        checkers.remove(checker);
    }

    public void addPoint(PointVRP point) {
        pointId++;
        point.setId(pointId);
        allPoints.add(point);
        for (InvariantVRP invariant : invariants) {
            invariant.propagateAddPoint(point);
        }
    }

    public void addRoute(RouteVRP route) {
        routeId++;
        route.setId(routeId);
        allRoutes.add(route);
        String truckCode = route.getTruckCode();
        if (mTruckCode2FirstPoint.containsKey(truckCode)) {
            PointVRP endPoint = mTruckCode2LastPoint.get(truckCode);
            endPoint.setNext(route.getStartPoint());
            route.getStartPoint().setPrev(endPoint);
            mTruckCode2LastPoint.put(truckCode, route.getEndPoint());
        } else {
            route.getStartPoint().setIndex(0);
            mTruckCode2FirstPoint.put(truckCode, route.getStartPoint());
            mTruckCode2LastPoint.put(truckCode, route.getEndPoint());
        }
        updateTruck(truckCode);
        for (InvariantVRP invariant : invariants) {
            invariant.propagateAddRoute(route);
        }
    }

    public Set<String> getAllTrucks() {
        return mTruckCode2FirstPoint.keySet();
    }

    public PointVRP getFirstPoint(String truckCode) {
        return mTruckCode2FirstPoint.get(truckCode);
    }

    public PointVRP getLastPoint(String truckCode) {
        return mTruckCode2LastPoint.get(truckCode);
    }

    private void updateTruck(String truckCode) {
        int index = 1;
        for (PointVRP p = mTruckCode2FirstPoint.get(truckCode).getNext(); p != null; p = p.getNext()) {
            p.setIndex(index);
            index++;
        }
    }

    // xóa các points trong x đi và chèn x[i] sau y[i],
    // nếu x[i1] và x[i2] có y[i1] == y[i2] thì sẽ chèn y[i]->x[i1]->x[i2] (i1 < i2)
    // nếu y[i] == NULL_POINT tương ứng với xóa x[i] ra khỏi route
    public void performKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y) {
        for (IcheckerVRP checker : checkers) {
            checker.validateKPointsMove(x, y);
        }

        HashSet<String> trucks = new HashSet<>();
        for (int i = x.size() - 1; i >= 0; i--) {
            PointVRP px = x.get(i);
            PointVRP py = y.get(i);
            if (px.getRoute() != null) {
                trucks.add(px.getRoute().getTruckCode());
            }
            px.setOldState();
            px.isRemovedFromRoute();
            if (py != CBLSVRP.NULL_POINT) {
                trucks.add(py.getRoute().getTruckCode());
                py.setOldState();
                px.isInsertedRightAfter(py);
            }
        }
        for (String truckCode : trucks) {
            updateTruck(truckCode);
        }

        for (InvariantVRP invariant : invariants) {
            invariant.propagateKPointsMove(x, y);
        }
    }
}
