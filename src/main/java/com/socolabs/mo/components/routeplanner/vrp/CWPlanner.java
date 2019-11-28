package com.socolabs.mo.components.routeplanner.vrp;

import com.socolabs.mo.components.maps.Path;
import com.socolabs.mo.controller.ApiController;
import com.dailyopt.mo.model.routevrp.*;
import com.socolabs.mo.model.routevrp.*;

import java.util.*;

public class CWPlanner {

    private RouteVRPInput input;
    // raw data input
    private int nbVehicles;
    private int nbClients;
    private int capacity;
    private double[] x;
    private double[] y; // (x[i],y[i]): toa do cua diem i = 0,1,2,...,nbClients
    private double[] demand;
    private double[][] cost;
    private int depot;// depot = 0
    private HashMap<Integer, RouteVRPInputPoint> mID2InputPoint;
    private HashMap<Integer, Double> mID2Angle;

    public CWPlanner(RouteVRPInput input) {
        this.input = input;
        mapRawData();
        genAngleOfClients();
    }

    public CWPlanner() {

    }

    public RouteVRPSolution computeRoute() {
        System.out.println("computeRoute:: nbClients = " + nbClients + ", nbVehicles = " + nbVehicles);
        ArrayList<Integer> ordered_id_arr = new ArrayList<Integer>();
        for (int id : mID2Angle.keySet()) {
            ordered_id_arr.add(id);
        }
        for (int i = 0; i < nbClients; i++) {
            for (int j = i + 1; j < nbClients; j++) {
                if (mID2Angle.get(ordered_id_arr.get(i)) > mID2Angle.get(ordered_id_arr.get(j))) {
                    int tmp = ordered_id_arr.get(i);
                    ordered_id_arr.set(i, ordered_id_arr.get(j));
                    ordered_id_arr.set(j, tmp);
                }
            }
        }
        double bestViolation = 1e18;
        double bestMinMax = 1e18;
        double bestTotalDis = 1e18;
//        Route[] bestRoutes = null;
        ArrayList<Integer> bestOrderedID = null;
        for (int i = 0; i < nbClients; i++) {
            System.out.println("ordered_id: " + ordered_id_arr);
            double total_dis = 0;
            double minVal = 1e18;
            double maxVal = 0;
            double violation = 1e18;
//            Route[] routes = new Route[nbVehicles];
            for (int k = 0, j = 0; k < nbVehicles; k++) {
                int d = j;
                if (k < nbVehicles - 1) {
                    int sum = 0;
                    while (d < nbClients && sum + demand[ordered_id_arr.get(d)] <= capacity) {
                        sum += demand[ordered_id_arr.get(d)];
                        d++;
                    }
                } else {
                    d = nbClients;
                    violation = 0;
                    for (int u = j; u < d; u++) {
                        violation += demand[ordered_id_arr.get(u)];
                    }
                    if (violation <= capacity) {
                        violation = 0;
                    } else {
                        violation -= capacity;
                    }
                }
                int[] arr = new int[d - j + 2];
                RouteVRPInputPoint[] points = new RouteVRPInputPoint[d - j + 2];
                points[0] = mID2InputPoint.get(depot);
                arr[0] = depot;
                for (int u = j, v = 1; u < d; u++, v++) {
                    arr[v] = ordered_id_arr.get(u);
                    points[v] = mID2InputPoint.get(ordered_id_arr.get(u));
                }
                arr[d - j + 1] = depot;
                points[d - j + 1] = mID2InputPoint.get(depot);
                double val = 0;
                for (int u = 0; u < points.length - 1; u++) {
                    total_dis += cost[arr[u]][arr[u + 1]];
                    val += cost[arr[u]][arr[u + 1]];
                }
                minVal = Math.min(minVal, val);
                maxVal = Math.max(maxVal, val);
//                Path[] paths = new Path[points.length-1];
//                for(int u = 0; u < paths.length; u++){
//                    paths[u] = ApiController.gismap.findPath(points[u].getLat() + "," + points[u].getLng(),
//                            points[u + 1].getLat() + "," + points[u + 1].getLng());
//                }
//                routes[k] = new Route(points, total_dis, paths);
                j = d;
            }
            if (bestViolation > violation) {
                bestViolation = violation;
                bestMinMax = maxVal - minVal;
                bestTotalDis = total_dis;
                bestOrderedID = new ArrayList<>(ordered_id_arr);
            } else if (bestViolation == violation) {
                if (maxVal - minVal < bestMinMax) {
                    bestMinMax = maxVal - minVal;
                    bestTotalDis = total_dis;
                    bestOrderedID = new ArrayList<>(ordered_id_arr);
                } else if (maxVal - minVal == bestMinMax && bestTotalDis > total_dis) {
                    bestTotalDis = total_dis;
                    bestOrderedID = new ArrayList<>(ordered_id_arr);
                }
            }
            Collections.rotate(ordered_id_arr, 1);
        }

        ordered_id_arr = bestOrderedID;
        Route[] routes = new Route[nbVehicles];
        for (int k = 0, j = 0; k < nbVehicles; k++) {
            int d = j;
            if (k < nbVehicles - 1) {
                int sum = 0;
                while (d < nbClients && sum + demand[ordered_id_arr.get(d)] <= capacity) {
                    sum += demand[ordered_id_arr.get(d)];
                    d++;
                }
            } else {
                d = nbClients;
            }
            int[] arr = new int[d - j + 2];
            RouteVRPInputPoint[] points = new RouteVRPInputPoint[d - j + 2];
            points[0] = mID2InputPoint.get(depot);
            arr[0] = depot;
            for (int u = j, v = 1; u < d; u++, v++) {
                arr[v] = ordered_id_arr.get(u);
                points[v] = mID2InputPoint.get(ordered_id_arr.get(u));
            }
            arr[d - j + 1] = depot;
            points[d - j + 1] = mID2InputPoint.get(depot);
            double val = 0;
            for (int u = 0; u < points.length - 1; u++) {
                val += cost[arr[u]][arr[u + 1]];
            }
            Path[] paths = new Path[points.length - 1];
            for (int u = 0; u < paths.length; u++) {
                paths[u] = ApiController.gismap.findPath(points[u].getLat() + "," + points[u].getLng(),
                        points[u + 1].getLat() + "," + points[u + 1].getLng());
            }
            routes[k] = new Route(points, val, paths);
            j = d;
        }
        RouteVRPSolution sol = new RouteVRPSolution(routes);
        return sol;
    }

    public RouteVRPSolution computeRoute(RouteVRPInput input) {
        this.input = input;
        mapRawData();
        genAngleOfClients();
        return computeRoute();
    }


    private double calcAngle(RouteVRPInputPoint r, RouteVRPInputPoint p) {
        double angle = (float) Math.toDegrees(Math.atan2(p.getLat() - r.getLat(), p.getLng() - r.getLng()));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    private void genAngleOfClients() {
        mID2Angle = new HashMap<>();
        RouteVRPInputPoint depot_p = mID2InputPoint.get(depot);
        for (int id = 1; id <= nbClients; id++) {
            double ang = calcAngle(depot_p, mID2InputPoint.get(id));
            mID2Angle.put(id, ang);
        }
    }

    private void mapRawData() {
        nbClients = input.getPoints().length - 1;
        double totalW = 0;
        int N = nbClients + 1;
        demand = new double[N];
        x = new double[N];
        y = new double[N];
        int idx = 0;
        depot = 0;
        mID2InputPoint = new HashMap<Integer, RouteVRPInputPoint>();

        for (int i = 0; i < input.getPoints().length; i++) {
            RouteVRPInputPoint p = input.getPoints()[i];
            if (p.getType().equals("Depot")) {
                capacity = p.getInfo();
                demand[depot] = 0;
                x[depot] = p.getLat();
                y[depot] = p.getLng();
                mID2InputPoint.put(depot, p);
            } else {
                totalW += p.getInfo();
                idx++;
                demand[idx] = p.getInfo();
                x[idx] = p.getLat();
                y[idx] = p.getLng();
                mID2InputPoint.put(idx, p);
            }
        }
        nbVehicles = (int) (totalW / capacity);
        if (nbVehicles * capacity < totalW) nbVehicles += 1;
        cost = new double[N][N];
        ArrayList<DistanceElement> l_distances = new ArrayList<DistanceElement>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                RouteVRPInputPoint pi = mID2InputPoint.get(i);
                RouteVRPInputPoint pj = mID2InputPoint.get(j);
                DistanceElement de = input.getDistanceElement(pi.getId(), pj.getId());
                if (de == null) {
                    Path P = ApiController.gismap.findPath(x[i] + "," + y[i], x[j] + "," + y[j]);
                    cost[i][j] = P.getLength();
                    System.out.println("mapRawData, compute path(" + i + "," + j + ") = " + P.toString());
                    de = new DistanceElement(pi.getId(), pj.getId(), cost[i][j]);
                } else {
                    cost[i][j] = de.getDistance();
                }
                l_distances.add(de);
            }
        }
        DistanceElement[] distances = new DistanceElement[l_distances.size()];
        for (int i = 0; i < l_distances.size(); i++)
            distances[i] = l_distances.get(i);
        input.setDistances(distances);

        // store to external files
//        try{
//            Gson gson = new Gson();
//            String json = gson.toJson(input);
//            PrintWriter out = new PrintWriter(routeVRPInputFilename);
//            out.print(json);
//            out.close();
//        }catch(Exception ex){
//            ex.printStackTrace();
//        }
    }

    public static void main(String[] argv) {

    }
}
//
//class AngleComparator implements Comparator<Integer> {
//
//    private HashMap<Integer, Double> angle;
//
//    public AngleComparator(HashMap<Integer, Double> angle) {
//        this.angle = angle;
//    }
//
//    @Override
//    public int compare(Integer o1, Integer o2) {
//        double gap = angle.get(o1) - angle.get(o2);
//        if (gap < 0) {
//            return -1;
//        } else if (gap > 0) {
//            return 1;
//        }
//        return 0;
//    }
//}