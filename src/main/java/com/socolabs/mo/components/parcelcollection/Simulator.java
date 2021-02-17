package com.socolabs.mo.components.parcelcollection;

import com.dailyopt.vrp.core.VRPPoint;
import com.dailyopt.vrp.core.VRPRoute;
import com.dailyopt.vrp.core.VRPVarRoutes;
import com.dailyopt.vrp.entities.accumulatedcalculators.AccumulatedEdgeCalculator;
import com.dailyopt.vrp.entities.accumulatedcalculators.AccumulatedNodeCalculator;
import com.dailyopt.vrp.functions.sum.SumAccumulatedWeightPoints;
import com.dailyopt.vrp.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Edge;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Graph;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Simulator {

    private final static double SPEED = 0.1;

    private HashMap<Vertex, HashMap<Vertex, Double>> realDistMatrix;
    private ArrayList<PCRequest> requests;
    private Vertex depot;
    private int nbVehicles;
    private int capacity;
    private IKNN kNNer;

    private VRPVarRoutes vr;
    private PCDistanceManager distanceManager;
    private HashMap<VRPRoute, VRPPoint> mRoute2StartPoint;
    private HashMap<VRPRoute, Double> mRoute2StartTimeAtCurPoint;
    private AccumulatedWeightPoints accDist;
    private SumAccumulatedWeightPoints objective;
    private AccumulatedWeightPoints accWeight;

    private HashMap<PCRequest, PCPoint> mRequest2Point;
    private HashMap<Vertex, ArrayList<PCPoint>> mVertex2AfterInsertablePoints;
    private Deque<PCRequest> requestQueue;
    private long startMoment;
    private long curMoment;

    public Simulator(ArrayList<PCRequest> requests,
                     Vertex depot,
                     int nbVehicles,
                     int capacity,
                     IKNN kNNer,
                     HashMap<Vertex, HashMap<Vertex, Double>> realDistMatrix) {
        Collections.sort(requests, new Comparator<PCRequest>() {
            @Override
            public int compare(PCRequest o1, PCRequest o2) {
                return o2.getMoment() - o1.getMoment();
            }
        });
        this.requests = requests;
        this.depot = depot;
        this.nbVehicles = nbVehicles;
        this.capacity = capacity;
        this.kNNer = kNNer;
        this.realDistMatrix = realDistMatrix;
//        kNNer.setObject(depot, true);

        vr = new VRPVarRoutes();
        distanceManager = new PCDistanceManager(vr);
        mRoute2StartPoint = new HashMap<>();
        mRoute2StartTimeAtCurPoint = new HashMap<>();
        mVertex2AfterInsertablePoints = new HashMap<>();
        mVertex2AfterInsertablePoints.put(depot, new ArrayList<>());
        for (int i = 1; i <= nbVehicles; i++) {
            PCPoint startPoint = new PCPoint(depot, vr);
            PCPoint endPoint = new PCPoint(depot, vr);
            VRPRoute route = new VRPRoute(startPoint, endPoint, i + "", vr);
            mRoute2StartPoint.put(route, startPoint);
            mRoute2StartTimeAtCurPoint.put(route, 0.);
//            mVertex2AfterInsertablePoints.get(depot).add(startPoint);
            kNNer.addPoint(startPoint);
        }
        distanceManager.setDistanceElement(depot, depot, 0);
        accDist = new AccumulatedWeightPoints(new AccumulatedEdgeCalculator(distanceManager));
        accWeight = new AccumulatedWeightPoints(new AccumulatedNodeCalculator(new PCNodeWeightManager(vr)));
        objective = new SumAccumulatedWeightPoints(accDist);
        requestQueue = new ArrayDeque<>();
        startMoment = System.currentTimeMillis();
        kNNer.setAccWeight(accWeight);
        mRequest2Point = new HashMap<>();
    }

    private boolean finish() {
        return requests.size() == 0 && requestQueue.size() == 0;
    }

    private void receiceRequests() {
        curMoment = System.currentTimeMillis() - startMoment;
        while (!requests.isEmpty() && requests.get(requests.size() - 1).getMoment() <= curMoment) {
            requestQueue.addLast(requests.get(requests.size() - 1));
            requests.remove(requests.size() - 1);
        }
    }

    private void updateStatus() {
        receiceRequests();
        ArrayList<VRPRoute> removedRoutes = new ArrayList<>();
        for (VRPRoute route : mRoute2StartPoint.keySet()) {
            VRPPoint curPoint = mRoute2StartPoint.get(route);
            double nt = mRoute2StartTimeAtCurPoint.get(route);
            if (route.getNbPoints() == 0) {
                mRoute2StartTimeAtCurPoint.put(route, 1.0 * curMoment);
                continue;
            }
            if (nt <= curMoment) {
                while (curPoint != route.getEndPoint()) {
                    PCPoint p1 = (PCPoint) curPoint;
                    PCPoint p2 = (PCPoint) curPoint.getNext();
                    double d = realDistMatrix.get(p1.getV()).get(p2.getV());
                    double dt = d / SPEED;
                    nt += dt;
                    if (nt <= curMoment) {
//                        ArrayList<PCPoint> points = mVertex2AfterInsertablePoints.get(p1.getV());
//                        points.remove(p1);
//                        if (points.size() == 0) {
//                            kNNer.setObject(p1.getV(), false);
//                        }
                        kNNer.removePoint(curPoint);
                        curPoint = curPoint.getNext();
                    } else {
                        break;
                    }
                }
                if (nt <= curMoment) {
                    removedRoutes.add(route);
//                    mRoute2StartTimeAtCurPoint.put(route, 1.0 * curMoment);
//                    mRoute2StartPoint.put(route, curPoint);
                } else {
                    mRoute2StartTimeAtCurPoint.put(route, nt);
                    mRoute2StartPoint.put(route, curPoint.getNext());
                    kNNer.removePoint(curPoint);
//                    PCPoint p1 = (PCPoint) curPoint;
//                    ArrayList<PCPoint> points = mVertex2AfterInsertablePoints.get(p1.getV());
//                    points.remove(p1);
//                    if (points.size() == 0) {
//                        kNNer.setObject(p1.getV(), false);
//                    }
                }
            }
        }
        for (VRPRoute route : removedRoutes) {
            mRoute2StartTimeAtCurPoint.remove(route);
            mRoute2StartPoint.remove(route);
            PCPoint startPoint = new PCPoint(depot, vr);
            PCPoint endPoint = new PCPoint(depot, vr);
            VRPRoute newRoute = new VRPRoute(startPoint, endPoint, route.getTruckCode() + "'", vr);
            mRoute2StartPoint.put(newRoute, startPoint);
            mRoute2StartTimeAtCurPoint.put(newRoute, 1.0 * curMoment);
//            mVertex2AfterInsertablePoints.get(depot).add(startPoint);
            kNNer.addPoint(startPoint);
        }
    }

    public void run() {
        while (!finish()) {
            updateStatus();
            ArrayList<PCRequest> nonInsertedRequests = new ArrayList<>();
            while (!requestQueue.isEmpty()) {
                PCRequest request = requestQueue.poll();
                if (!insert(request)) {
                    nonInsertedRequests.add(request);
                }
            }
            reoptimize();
            for (int i = nonInsertedRequests.size() - 1; i >= 0; i--) {
                requestQueue.addFirst(nonInsertedRequests.get(i));
            }
        }

    }

    private void reoptimize() {
//        System.out.println("reoptimize " + requests.size());
//        System.out.println(getTotalDistance() + " ");
        ArrayList<VRPPoint> points = new ArrayList<>();
        for (VRPRoute route : mRoute2StartPoint.keySet()) {
            if (mRoute2StartPoint.get(route) == route.getEndPoint()) {
                continue;
            }
            for (VRPPoint p = mRoute2StartPoint.get(route).getNext(); p != route.getEndPoint(); p = p.getNext()) {
                points.add(p);
            }
        }

        int maxTime = 5000;
        while (System.currentTimeMillis() - startMoment < curMoment + maxTime) {
            boolean stop = true;
            for (int i = 0; i < points.size(); i++) {
                for (int j = i + 1; j < points.size(); j++) {
                    VRPPoint x = points.get(i);
                    VRPPoint y = points.get(j);
                    if (!checkTwoPointsMove(x, y)) {
                        continue;
                    }
                    double eval = evaluateTwoPointsMove(x, y);
                    if (eval < 0) {
                        vr.propagateTwoPointsMove(x, y);
                        stop = false;
                    }
                }
            }
            if (stop) {
                stop = exploreTwoOptMove();
                stop |= exploreThreeOptMove();
                if (!stop) {
//                    stop = exploreOrOptMove();
//                    stop |= exploreCrossExchangeMove();
//                    if (!stop) {
                        break;
//                    }
                }
            }
        }
//        System.out.println(getTotalDistance());
    }

    private boolean checkTwoPointsMove(VRPPoint x, VRPPoint y) {
        if (x.getRoute() == y.getRoute()) {
            return true;
        }
        PCPoint xx = (PCPoint) x;
        PCPoint yy = (PCPoint) y;
        return (getWeightOfRoute(x.getRoute()) - xx.getWeight() + yy.getWeight() <= capacity) &&
                (getWeightOfRoute(y.getRoute()) - yy.getWeight() + xx.getWeight() <= capacity);
    }

    private double evaluateTwoPointsMove(VRPPoint x, VRPPoint y) {
        VRPPoint prevX = x.getPrev();
        VRPPoint nextX = x.getNext();
        VRPPoint prevY = y.getPrev();
        VRPPoint nextY = y.getNext();
        if (nextX != y && nextY != x) {
            double sumCurDist = accDist.getWeightValueOfPoint(nextX) - accDist.getWeightValueOfPoint(prevX)
                    + accDist.getWeightValueOfPoint(nextY) - accDist.getWeightValueOfPoint(prevY);
            double distPX = distanceManager.getDistance(prevY, x);
            double distNX = distanceManager.getDistance(x, nextY);
            double distPY = distanceManager.getDistance(prevX, y);
            double distNY = distanceManager.getDistance(y, nextX);
            return distNX + distNY + distPY + distPX - sumCurDist;
        } else {
            if (nextX == y) {
                double sumCurDist = accDist.getWeightValueOfPoint(nextY) - accDist.getWeightValueOfPoint(prevX);
                return distanceManager.getDistance(prevX, y) +
                        distanceManager.getDistance(y, x) +
                        distanceManager.getDistance(x, nextY) -
                        sumCurDist;
            } else {
                double sumCurDist = accDist.getWeightValueOfPoint(nextX) - accDist.getWeightValueOfPoint(prevY);
                return distanceManager.getDistance(prevY, x) +
                        distanceManager.getDistance(x, y) +
                        distanceManager.getDistance(y, nextX) -
                        sumCurDist;
            }
        }
    }

    private int getWeightOfRoute(VRPRoute r) {
        return (int) accWeight.getWeightValueOfPoint(r.getEndPoint());
    }

    private boolean checkAddOnePointMove(PCPoint x, PCPoint y) {
        return x.getWeight() + getWeightOfRoute(y.getRoute()) <= capacity;
    }

    private double evaluateAddOnePointMove(VRPPoint x, VRPPoint y) {
        VRPPoint nextY = y.getNext();
        return -(accDist.getWeightValueOfPoint(nextY) - accDist.getWeightValueOfPoint(y))
                + distanceManager.getDistance(y, x)
                + distanceManager.getDistance(x, nextY);
    }

    private boolean insert(PCRequest request) {
        PCPoint newPoint = null;
        if (mRequest2Point.containsKey(request)) {
            newPoint = mRequest2Point.get(request);
        } else {
            newPoint = new PCPoint(request.getLocation(), vr, request.getWeight());
            mRequest2Point.put(request, newPoint);
        }
        ArrayList<Pair<VRPPoint, Pair<Double, Double>>> kNNList = kNNer.getKNN(request.getLocation());
        for (Pair<VRPPoint, Pair<Double, Double>> dt : kNNList) {
            PCPoint p = (PCPoint) dt.first;
            distanceManager.setDistanceElement(request.getLocation(), p.getV(), dt.second.first);
            distanceManager.setDistanceElement(p.getV(), request.getLocation(), dt.second.second);
        }
        VRPPoint bestPoint = null;
        double bestEval = PCDistanceManager.INF;
        for (int i = 0; i < kNNList.size(); i++) {
            PCPoint insertedAfterPoint = (PCPoint) kNNList.get(i).first;
            if (!checkAddOnePointMove(newPoint, insertedAfterPoint)) {
                continue;
            }
            double eval = evaluateAddOnePointMove(newPoint, insertedAfterPoint);
            if (eval < bestEval) {
                bestEval = eval;
                bestPoint = insertedAfterPoint;
            }
        }
        if (bestPoint == null || bestEval > 1e9) {
            return false;
        }
        vr.propagateOnePointMove(newPoint, bestPoint);

//        kNNer.setObject(request.second, true);
        kNNer.addPoint(newPoint);
        return true;
    }

    public double getTotalDistance() {
        double total = 0;
        for (VRPRoute route : vr.getAllRoutes()) {
            double rd = 0;
            for (VRPPoint p = route.getStartPoint(); p != route.getEndPoint(); p = p.getNext()) {
                PCPoint p1 = (PCPoint) p;
                PCPoint p2 = (PCPoint) p.getNext();
                double d = realDistMatrix.get(p1.getV()).get(p2.getV());
                total += d;
                rd += d;
            }
//            System.out.println(route + " " + rd + " " +  + getWeightOfRoute(route));
        }
        return total;
    }

    public static void main(String[] args) throws FileNotFoundException {
        String filename = "data\\HaiBaTrungRoad-connected.txt";
        if (args.length > 0) {
            filename = args[0];
        }
        //420547.1539999988
        Scanner in = new Scanner(new File(filename));
        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Edge> edges = new ArrayList<>();
        HashMap<Integer, Vertex> mID2Vertex = new HashMap<>();
        double totalLat = 0;
        double totalLng = 0;
        while(true) {
            int id = in.nextInt();
            if (id == -1) break;
            double lat = in.nextDouble();
            double lng = in.nextDouble();
            totalLat += lat;
            totalLng += lng;
            Vertex v = new Vertex(id, lat, lng);
            mID2Vertex.put(id, v);
            vertices.add(v);
        }
        while(true) {
            int beginID = in.nextInt();
            if (beginID == -1) break;
            int endID = in.nextInt();
            long length = (long) (in.nextDouble() * 1000);
            edges.add(new Edge(mID2Vertex.get(beginID), mID2Vertex.get(endID), length));
//            edges.add(new Edge(mID2Vertex.get(endID), mID2Vertex.get(beginID), length));
        }

        Random rand = new Random(1993);
        Graph g = new Graph(vertices, edges);
        g.initDataStructure();
        HashSet<Vertex> visitedVertices = new HashSet<>();
        double avgLat = totalLat / vertices.size();
        double avgLng = totalLng / vertices.size();

        int nbRequests = 1000;
        int nbVehicles = 10;
        int capacity = 100;
        Vertex depot = null;
        double minDist = 1e9;
        double maxDist = 0;
        for (Vertex v : vertices) {
            double dist = KNNHaversine.computeDistanceHaversine(avgLat, avgLng, v.getLat(), v.getLng());
            if (dist < minDist) {
                minDist = dist;
                depot = v;
            }
            maxDist = Math.max(maxDist, dist);
        }

        visitedVertices.add(depot);
        ArrayList<PCRequest> requests = new ArrayList<>();
        while (requests.size() < nbRequests) {
            Vertex v = vertices.get(rand.nextInt(vertices.size()));
            while (visitedVertices.contains(v)) {
                v = vertices.get(rand.nextInt(vertices.size()));
            }
            requests.add(new PCRequest(rand.nextInt(1000000), rand.nextInt(5) + 1, v));
            visitedVertices.add(v);
        }
        HashMap<Vertex, HashMap<Vertex, Double>> realDistanceMatrix = new HashMap<>();
        for (Vertex v : visitedVertices) {
            long[] distArr = g.bfs(v);
            HashMap<Vertex, Double> distElements = new HashMap<>();
            for (Vertex u : visitedVertices) {
                distElements.put(u, 1.0 * distArr[u.getIndex()] / 1000);
            }
            realDistanceMatrix.put(v, distElements);
        }
        int[] kList = new int[]{5, 10, 50, 100, 200, 500};
        for (int k : kList) {
            IKNN knn = new KNNGTree(realDistanceMatrix, k);
            ArrayList<PCRequest> tmpRequests = new ArrayList<>(requests);
            Simulator simulator = new Simulator(tmpRequests, depot, nbVehicles, capacity, knn, realDistanceMatrix);
            simulator.run();
            System.out.println("GTree:: k = " + k + ", cost = " + simulator.getTotalDistance());
        }
        for (int k : kList) {
            IKNN knn = new KNNHaversine(realDistanceMatrix, k);
            ArrayList<PCRequest> tmpRequests = new ArrayList<>(requests);
            Simulator simulator = new Simulator(tmpRequests, depot, nbVehicles, capacity, knn, realDistanceMatrix);
            simulator.run();
            System.out.println("Haversine:: k = " + k + ", cost = " + simulator.getTotalDistance());
        }
    }

    private boolean checkTwoOptMove(VRPPoint x, VRPPoint y) {
        int d = (int) (accWeight.getWeightValueOfPoint(x) - accWeight.getWeightValueOfPoint(y));
        return (getWeightOfRoute(x.getRoute()) - d <= capacity) &&
                (getWeightOfRoute(y.getRoute()) + d <= capacity);
    }

    private double evaluateTwoOptMove(VRPPoint x, VRPPoint y) {
        VRPPoint nx = x.getNext();
        VRPPoint ny = y.getNext();
        return distanceManager.getDistance(x, ny) + distanceManager.getDistance(y, nx)
                - distanceManager.getDistance(x, nx) - distanceManager.getDistance(y, ny);
    }

    private boolean exploreTwoOptMove() {
        boolean ok = false;
        for (VRPRoute rx : mRoute2StartPoint.keySet()) {
            VRPPoint bx = null;
            VRPPoint by = null;
            double bestEval = 0;
            for (VRPRoute ry : mRoute2StartPoint.keySet()) {
                if (rx != ry) {
                    for (VRPPoint x = mRoute2StartPoint.get(rx); x != rx.getEndPoint(); x = x.getNext()) {
                        for (VRPPoint y = mRoute2StartPoint.get(ry); y != ry.getEndPoint(); y = y.getNext()) {
                            if (checkTwoOptMove(x, y)) {
                                double eval = evaluateTwoOptMove(x, y);
                                if (eval < bestEval) {
                                    bx = x;
                                    by = y;
                                    bestEval = eval;
                                }
                            }
                        }
                    }

                }
            }
            if (bx != null) {
                vr.propagateTwoOptMove5(bx, by);
                ok = true;
            }
        }
        return ok;
    }

    private double evaluateThreeOptMove(VRPPoint x, VRPPoint y, VRPPoint z) {
        VRPPoint nx = x.getNext();
        VRPPoint ny = y.getNext();
        VRPPoint nz = z.getNext();
        return distanceManager.getDistance(x, ny) +
                distanceManager.getDistance(z, nx) +
                distanceManager.getDistance(y, nz) -
                distanceManager.getDistance(x, nx) -
                distanceManager.getDistance(y, ny) -
                distanceManager.getDistance(z, nz);
    }

    private boolean exploreThreeOptMove() {
        boolean ok = false;
        for (VRPRoute rx : mRoute2StartPoint.keySet()) {
            VRPPoint bx = null;
            VRPPoint by = null;
            VRPPoint bz = null;
            double bestEval = 0;
            for (VRPPoint x = mRoute2StartPoint.get(rx); x != rx.getEndPoint(); x = x.getNext()) {
                for (VRPPoint y = x.getNext(); y != rx.getEndPoint(); y = y.getNext()) {
                    for (VRPPoint z = y.getNext(); z != rx.getEndPoint(); z = z.getNext()) {
                        double eval = evaluateThreeOptMove(x, y, z);
                        if (eval < bestEval) {
                            bx = x;
                            by = y;
                            bz = z;
                            bestEval = eval;
                        }
                    }
                }
            }
            if (bx != null) {
                vr.propagateThreeOptMove5(bx, by, bz);
                ok = true;
            }
        }
        return ok;
    }

    private boolean checkOrOptMove(VRPPoint x1, VRPPoint x2, VRPPoint y) {
        VRPPoint px1 = x1.getPrev();
        return accWeight.getWeightValueOfPoint(x2) - accWeight.getWeightValueOfPoint(px1)
                + getWeightOfRoute(y.getRoute()) <= capacity;
    }

    private double evaluateOrOptMove(VRPPoint x1, VRPPoint x2, VRPPoint y) {
        VRPPoint px1 = x1.getPrev();
        VRPPoint nx2 = x2.getNext();
        VRPPoint ny = y.getNext();
        return distanceManager.getDistance(y, x1) +
                distanceManager.getDistance(x2, ny) +
                distanceManager.getDistance(px1, nx2) -
                distanceManager.getDistance(px1, x1) -
                distanceManager.getDistance(x2, nx2) -
                distanceManager.getDistance(y, ny);
    }

    private boolean exploreOrOptMove() {
        boolean ok = false;
        for (VRPRoute rx : mRoute2StartPoint.keySet()) {
            if (mRoute2StartPoint.get(rx) == rx.getEndPoint()) {
                continue;
            }
            VRPPoint bx1 = null;
            VRPPoint bx2 = null;
            VRPPoint by = null;
            double bestEval = 0;
            for (VRPRoute ry : mRoute2StartPoint.keySet()) {
                if (rx != ry) {
                    for (VRPPoint x1 = mRoute2StartPoint.get(rx).getNext(); x1 != rx.getEndPoint(); x1 = x1.getNext()) {
                        for (VRPPoint x2 = x1; x2 != rx.getEndPoint(); x2 = x2.getNext()) {
                            for (VRPPoint y = mRoute2StartPoint.get(ry); y != ry.getEndPoint(); y = y.getNext()) {
                                if (checkOrOptMove(x1, x2, y)) {
                                    double eval = evaluateOrOptMove(x1, x2, y);
                                    if (eval < bestEval) {
                                        bx1 = x1;
                                        bx2 = x2;
                                        by = y;
                                        bestEval = eval;
                                    }
                                }
                            }
                        }
                    }

                }
            }
            if (by != null) {
                vr.propagateOrOptMove1(bx1, bx2, by);
                ok = true;
            }
        }
        return ok;
    }

    private boolean checkCrossExchangeMove(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2) {
        int d = (int) ((accWeight.getWeightValueOfPoint(y1) - accWeight.getWeightValueOfPoint(x1)) -
                (accWeight.getWeightValueOfPoint(y2) - accWeight.getWeightValueOfPoint(x2)));
        return (getWeightOfRoute(x1.getRoute()) - d <= capacity) &&
                (getWeightOfRoute(x2.getRoute()) + d <= capacity);
    }

    private double evaluateCrossExchangeMove(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2) {
        VRPPoint nx1 = x1.getNext();
        VRPPoint ny1 = y1.getNext();
        VRPPoint nx2 = x2.getNext();
        VRPPoint ny2 = y2.getNext();
        return distanceManager.getDistance(x1, nx2) +
                distanceManager.getDistance(y2, ny1) +
                distanceManager.getDistance(x2, nx1) +
                distanceManager.getDistance(y1, ny2) -
                distanceManager.getDistance(x1, nx1) -
                distanceManager.getDistance(x2, nx2) -
                distanceManager.getDistance(y1, ny1) -
                distanceManager.getDistance(y2, ny2);
    }

    private boolean exploreCrossExchangeMove() {
        boolean ok = false;
        for (VRPRoute r1 : mRoute2StartPoint.keySet()) {
            VRPPoint bx1 = null;
            VRPPoint bx2 = null;
            VRPPoint by1 = null;
            VRPPoint by2 = null;
            double bestEval = 0;
            for (VRPRoute r2 : mRoute2StartPoint.keySet()) {
                if (r1 != r2) {
                    for (VRPPoint x1 = mRoute2StartPoint.get(r1); x1 != r1.getEndPoint() && x1.getNext() != r1.getEndPoint(); x1 = x1.getNext()) {
                        int cnt1 = 0;
                        for (VRPPoint y1 = x1.getNext(); y1 != r1.getEndPoint() && cnt1 < 5; y1 = y1.getNext(), cnt1++) {
                            for (VRPPoint x2 = mRoute2StartPoint.get(r2); x2 != r2.getEndPoint() && x2.getNext() != r2.getEndPoint(); x2 = x2.getNext()) {
                                int cnt2 = 0;
                                for (VRPPoint y2 = x2.getNext(); y2 != r2.getEndPoint() && cnt2 < 5; y2 = y2.getNext(), cnt2++) {
                                    if (checkCrossExchangeMove(x1, y1, x2, y2)) {
                                        double eval = evaluateCrossExchangeMove(x1, y1, x2, y2);
                                        if (eval < bestEval) {
                                            bx1 = x1;
                                            bx2 = x2;
                                            by1 = y1;
                                            by2 = y2;
                                            bestEval = eval;
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
            if (by1 != null) {
                vr.propagateCrossExchangeMove1(bx1, by1, bx2, by2);
                ok = true;
            }
        }
        return ok;
    }
}
