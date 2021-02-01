package com.socolabs.mo.components.collectparcels;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Edge;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Graph;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;
import com.socolabs.mo.components.collectparcels.nearestsearcher.DijkstraSearcher;
import com.socolabs.mo.components.collectparcels.nearestsearcher.GTreeSearcher;
import com.socolabs.mo.components.collectparcels.nearestsearcher.NearestSearcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;

public class Simulator {

    public final static double VEHICLE_SPEED = 30; // 6m/s => 21.6km/h

    private NearestSearcher searcher;
    private ArrayList<Depot> depots;
    private int nbVehiclesOfDepot;
    private int capacity;
    private ArrayList<Parcel>[] requests;

    private ArrayList<Route> allRoutes;

    private double totalDistances = 0;

    private int maxRemainWeight = 0;

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
        this.requests = new ArrayList[maxMoment + 1];
        for (int i = 0; i <= maxMoment; i++) {
            this.requests[i] = new ArrayList<>();
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
                RouteElement start = new RouteElement(depot.getLocation());
                RouteElement end = new RouteElement(depot.getLocation());
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
        maxRemainWeight = 0;
        for (Route r : allRoutes) {
            RouteElement e = r.getCur();
            RouteElement next = e.getNext();
            if (next != null && e.getArrivalTime() == next.getArrivalTime()) {
                e.setArrivalTime(t);
                next.setArrivalTime(t);
                maxRemainWeight = Math.max(maxRemainWeight, r.getRemainWeight());
                continue;
            }
            while (next != null && next.getArrivalTime() <= t) {
                if (next != r.getEnd()) {
                    searcher.remove(next);
                }
                r.setCur(next);
                next.setPrev(null);
                next = next.getNext();
            }
            if (next == null) {
                next = r.getEnd();
                next.setPrev(null);
                next.setArrivalTime(t);
                RouteElement end = new RouteElement(r.getDepot().getLocation());
                end.setArrivalTime(next.getArrivalTime());
                next.setNext(end);
                end.setPrev(next);
                end.setRoute(r);
                r.setEnd(end);
                r.reset();
                searcher.add(next);
            }
            maxRemainWeight = Math.max(maxRemainWeight, r.getRemainWeight());
        }
    }

    private boolean addParcel(Parcel p) {
        if (maxRemainWeight < p.getWeight()) {
            return false;
        }
        Pair<RouteElement, Pair<Double, Double>> ret = searcher.getNearestElement(p);
        RouteElement nearest = ret.first;
        if (nearest != null) {
            Route r = nearest.getRoute();
            if (nearest.getParcel() == null) {
                searcher.remove(nearest);
            }
            for (RouteElement e = r.getCur().getNext(); e != r.getEnd(); e = e.getNext()) {
                searcher.remove(e);
            }
            double prevTime = ret.second.first / VEHICLE_SPEED;
            double nextTime = ret.second.second / VEHICLE_SPEED;
            double deltaTime = (prevTime + nextTime) - (nearest.getNext().getArrivalTime() - nearest.getArrivalTime());
            RouteElement routeElement = new RouteElement(p.getLocation(), nearest.getRoute(), nearest.getNext(), nearest, p, nearest.getArrivalTime() + prevTime);
            routeElement.getPrev().setNext(routeElement);
            routeElement.getNext().setPrev(routeElement);
            RouteElement e = routeElement.getNext();
            r.addParcel(p);

//            System.out.println("new weight = " + r.getWeight());
            while (e != null) {
                e.updateArrivalTime(deltaTime);
                e = e.getNext();
            }
            System.out.println("arrival Time = " + r.getEnd().getArrivalTime() + " " + ret.second.first + " + " + ret.second.second);
            for (e = r.getCur().getNext(); e != r.getEnd(); e = e.getNext()) {
//                if (p.getLocation().getId() == 183892 && e.getLocation().getId() == 183892) {
//                    System.exit(-1);
//                }
                searcher.add(e);
            }
            return true;
        }
        return false;
    }

    public void run() {
        init();
        int cnt = 0;
        for (int t = 0; t < requests.length - 1; t++) {
            System.out.println("processing:: t = " + t + "; nb parcels = " + requests[t].size() + "; cnt = " + cnt);
            update(t);
            Collections.sort(requests[t]);
            boolean ok = true;
            for (Parcel p : requests[t]) {
                if ((!ok) || (!addParcel(p))) {
                    requests[t + 1].add(p);
                    ok = false;
                } else {
                    cnt++;
                }
            }
            requests[t].clear();
        }

        int t = requests.length - 1;
        ArrayList<Parcel> remain = new ArrayList<>(requests[t]);
        int i = 0;
        while (i < remain.size()) {
            System.out.println("processing:: t = " + t + "; nb parcels = " + (remain.size() - i) + "; cnt = " + cnt);
            update(t);
            for (; i < remain.size(); i++) {
                Parcel p = remain.get(i);
                if (!addParcel(p)) {
                    break;
                }
            }
            t++;
        }

        System.out.println("Total Query Time = " + searcher.getTotalQueryTime());
    }

    public static void main(String[] args) throws FileNotFoundException {
        String filename = "data\\BusHanoiCityRoad-connected.txt";
//        String filename = "data\\HaiBaTrungRoad-connected.txt";
        if (args.length > 0) {
            filename = args[0];
        }
        Scanner in = new Scanner(new File(filename));
        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Edge> edges = new ArrayList<>();
        HashMap<Integer, Vertex> mID2Vertex = new HashMap<>();
        while(true) {
            int id = in.nextInt();
            if (id == -1) break;
            double lat = in.nextDouble();
            double lng = in.nextDouble();
            Vertex v = new Vertex(id, lat, lng);
            mID2Vertex.put(id, v);
            vertices.add(v);
        }
        while(true) {
            int beginID = in.nextInt();
            if (beginID == -1) break;
            int endID = in.nextInt();
            double length = in.nextDouble();
            edges.add(new Edge(mID2Vertex.get(beginID), mID2Vertex.get(endID), length));
//            edges.add(new Edge(mID2Vertex.get(endID), mID2Vertex.get(beginID), length));
        }



        FileWriter myWriter;
        Random rand = new Random(1993);
        ArrayList<Depot> depots = new ArrayList<>();
        HashSet<Vertex> depotLocations = new HashSet<>();
        int nbDepots = 5;
        for (int i = 0; i < nbDepots; i++) {
            Vertex v = vertices.get(rand.nextInt(vertices.size()));
            while (depotLocations.contains(v)) {
                v = vertices.get(rand.nextInt(vertices.size()));
            }
            depotLocations.add(v);
            depots.add(new Depot(v));
        }

        int capacity = 1000;
        int nbVehicles = 2;
        int nbRequests = 1000;
        ArrayList<Parcel> requests = new ArrayList<>();
        while (requests.size() < nbRequests) {
            Vertex v = vertices.get(rand.nextInt(vertices.size()));
            while (depotLocations.contains(v)) {
                v = vertices.get(rand.nextInt(vertices.size()));
            }
            int weight = rand.nextInt(200) + 1;
            int moment = rand.nextInt(20000);
            requests.add(new Parcel(v, weight, moment));
            depotLocations.add(v);
        }
//        NearestSearcher searcher = new DijkstraSearcher(new Graph(vertices, edges));
        NearestSearcher searcher = new GTreeSearcher(new Graph(vertices, edges), 16, 256);
        Simulator simulator = new Simulator(searcher, depots, nbVehicles, capacity, requests);
        simulator.run();
    }
}
