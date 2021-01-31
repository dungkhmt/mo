package com.socolabs.mo.components.collectparcels;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Edge;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;

import java.util.*;

public class Simulator {

    public final static double VEHICLE_SPEED = 10; // 6m/s => 21.6km/h

    private NearestSearcher searcher;
    private ArrayList<Depot> depots;
    private int nbVehiclesOfDepot;
    private int capacity;
    private Deque<Parcel>[] requests;

    private ArrayList<Route> allRoutes;

    private double totalDistances = 0;

    public Simulator(NearestSearcher searcher,
                     ArrayList<Depot> depots,
                     int nbVehiclesOfDepot,
                     int capacity,
                     ArrayList<Parcel> requests) {
        this.searcher = searcher;
        this.depots = depots;
        this.nbVehiclesOfDepot = nbVehiclesOfDepot;
        this.capacity = capacity;
        int maxMoment = 0;
        for (Parcel p : requests) {
            maxMoment = Math.max(maxMoment, p.getMoment());
        }
        maxMoment++;
        this.requests = new Deque[maxMoment + 1];
        for (int i = 0; i <= maxMoment; i++) {
            this.requests[i] = new ArrayDeque<>();
        }
        for (Parcel p : requests) {
            this.requests[p.getMoment()].add(p);
        }
    }

    private void init() {
        int id = 0;
        allRoutes = new ArrayList<>();
        for (Depot depot : depots) {
            for (int i = 0; i < nbVehiclesOfDepot; i++) {
                id++;
                Vehicle vehicle = new Vehicle(id, capacity);
                RouteElement start = new RouteElement();
                RouteElement end = new RouteElement();
                Route nr = new Route(depot, vehicle, start, end, 0);
                allRoutes.add(nr);
                start.setNext(end);
                end.setPrev(start);
                start.setRoute(nr);
                end.setRoute(nr);
                searcher.add(start);
            }
        }
    }

    private void update(double t) {
        for (Route r : allRoutes) {
            RouteElement e = r.getCur();
            RouteElement next = e.getNext();
            if (next != null && e.getArrivalTime() == next.getArrivalTime()) {
                e.setArrivalTime(t);
                next.setArrivalTime(t);
                continue;
            }
            while (next != null && next.getArrivalTime() <= t) {
                searcher.remove(next);
                r.setCur(next);
                next = next.getNext();
            }
            if (next == null) {
                next = r.getEnd();
                next.setPrev(null);
                next.setArrivalTime(t);
                RouteElement end = new RouteElement();
                end.setArrivalTime(next.getArrivalTime());
                next.setNext(end);
                end.setPrev(next);
                end.setRoute(r);
                searcher.add(next);
                r.reset();
            }
        }
    }

    private boolean addParcel(Parcel p) {
        Pair<RouteElement, Pair<Double, Double>> ret = searcher.getNearestElement(p);
        RouteElement nearest = ret.first;
        if (nearest != null) {
            double prevTime = ret.second.first / VEHICLE_SPEED;
            double nextTime = ret.second.second / VEHICLE_SPEED;
            double deltaTime = (prevTime + nextTime) - (nearest.getNext().getArrivalTime() - nearest.getArrivalTime());
            RouteElement routeElement = new RouteElement(nearest.getRoute(), nearest.getNext(), nearest, p, nearest.getArrivalTime() + prevTime);
            routeElement.getPrev().setNext(routeElement);
            routeElement.getNext().setPrev(routeElement);
            RouteElement e = routeElement.getNext();
            Route r = e.getRoute();
            r.addParcel(p);
            while (e != r.getEnd()) {
                searcher.remove(e);
                e.updateArrivalTime(deltaTime);
                searcher.add(e);
                e = e.getNext();
            }
            e.updateArrivalTime(deltaTime);
            if (nearest.getParcel() == null) {
                searcher.remove(nearest);
            }
            return true;
        }
        return false;
    }

    public void run() {
        init();
        for (int t = 0; t < requests.length - 1; t++) {
            update(t);
            ArrayList<Parcel> unassigned = new ArrayList<>();
            for (Parcel p : requests[t]) {
                if (!addParcel(p)) {
                    unassigned.add(p);
                }
            }
            requests[t].clear();
            Collections.reverse(unassigned);
            for (Parcel p : unassigned) {
                requests[t + 1].addFirst(p);
            }
        }

        int t = requests.length - 1;
        ArrayList<Parcel> remain = new ArrayList<>(requests[t]);
        int i = 0;
        while (i < remain.size()) {
            update(t);
            for (; i < remain.size(); i++) {
                Parcel p = remain.get(i);
                if (!addParcel(p)) {
                    break;
                }
            }
            t++;
        }
    }
}
