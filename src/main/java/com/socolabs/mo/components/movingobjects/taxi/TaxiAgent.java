package com.socolabs.mo.components.movingobjects.taxi;

import com.google.gson.Gson;
import com.socolabs.mo.components.maps.GISMap;
import com.socolabs.mo.components.maps.Path;
import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.maps.utils.GoogleMapsQuery;
import com.socolabs.mo.model.modelFindPath.ShortestPathInput;
import lombok.Getter;

import java.io.IOException;

@Getter
public class TaxiAgent extends Thread {
    public static final String taxiServer = "http://localhost:8080/";
    public static final double speed = 0.02; // m/s

    private TaxiController controller;
    private GISMap gismap;
    private Taxi taxi;
    private String from;
    private String to;

    public TaxiAgent(TaxiController controller, GISMap gisMap, Taxi taxi, String from, String to) {
        this.controller = controller;
        this.gismap = gisMap;
        this.taxi = taxi;
        this.from = from;
        this.to = to;
    }

    private TaxiRouteElement routeElement;
    private GoogleMapsQuery G;

    private TaxiState taxiState;

    public TaxiAgent(Taxi taxi, TaxiRouteElement routeElement, GoogleMapsQuery G) {
        this.taxi = taxi;
        this.routeElement = routeElement;
        this.G = G;
        taxiState = new TaxiState(taxi.getId(), "RUNNING", taxi, routeElement);
    }

    private TaxiOrderInput taxiOrderInput;
    private Gson gson;

    public TaxiAgent(TaxiOrderInput taxiOrderInput) {
        this.taxi = taxiOrderInput.getTaxi();
        this.taxiOrderInput = taxiOrderInput;
        this.G = new GoogleMapsQuery();
        gson = new Gson();
    }

    private void update() {
//        System.out.println("update");
        String json = gson.toJson(taxiState);
        String ouput = TaxiUtils.execPost(taxiServer + "update-taxi-state", json);
//        System.out.println(taxiServer + "update-taxi-state");
    }

    public void run() {
        System.out.println("running " + taxi.getId());
            ShortestPathInput pickupInput = new ShortestPathInput(0, 0, taxi.getLatLng(), taxiOrderInput.getOrder().getPickupPoint().getLatLng());
            String pickupRoute = TaxiUtils.execPost(taxiServer + "findShortestPath", gson.toJson(pickupInput));
            Path pickupPath = gson.fromJson(pickupRoute, Path.class);
            ShortestPathInput deliveryInput = new ShortestPathInput(0, 0, taxiOrderInput.getOrder().getPickupPoint().getLatLng(), taxiOrderInput.getOrder().getDeliveryPoint().getLatLng());
            String deliveryRoute = TaxiUtils.execPost(taxiServer + "findShortestPath", gson.toJson(deliveryInput));
            Path deliveryPath = gson.fromJson(deliveryRoute, Path.class);
            Point[] points = new Point[pickupPath.getPoints().length + deliveryPath.getPoints().length];
            int pos = 0;
            for (Point p : pickupPath.getPoints()) {
                points[pos++] = p;
            }
            int r = pos;
            for (Point p : deliveryPath.getPoints()) {
                points[r++] = p;
            }
            taxiState = new TaxiState(taxi.getId(), "PICKUP_RUNNING", taxi, new TaxiRouteElement(points));
            update();

            double[] distances = new double[points.length];
            for (int i = 1; i < points.length; i++) {
                distances[i] = G.computeDistanceHaversine(points[i - 1].getLat(), points[i - 1].getLng(),
                        points[i].getLat(), points[i].getLng());
            }
            int l = 1;
            double totalDist = 0;
            double prevDist = 0;
            long curTime = System.currentTimeMillis();
            while (l < points.length) {
                totalDist = speed * (System.currentTimeMillis() - curTime) / 1000;
                while (l < points.length && prevDist + distances[l] < totalDist) {
                    prevDist += distances[l];
                    l++;
                }
                if (l == points.length) {
                    taxi.setLat(points[l - 1].getLat());
                    taxi.setLng(points[l - 1].getLng());
                    break;
                }
                double rate = (totalDist - prevDist) / distances[l];
//            System.out.println("rate = " + rate + " p1 " + points[l - 1] + " p2 " + points[l]);
                taxi.setLat(points[l - 1].getLat() + rate * (points[l].getLat() - points[l - 1].getLat()));
                taxi.setLng(points[l - 1].getLng() + rate * (points[l].getLng() - points[l - 1].getLng()));
                Point[] newRoute = new Point[points.length - l + 1];
                newRoute[0] = taxi;
                int ii = 1;
                for (int j = l; j < points.length; j++) {
                    newRoute[ii++] = points[j];
                }
                String status = (l < pos) ? "PICKUP_RUNNING" : "DELIVERY_RUNNING";
                taxiState = new TaxiState(taxi.getId(), status, taxi, new TaxiRouteElement(newRoute));
                update();
                System.out.println("taxi(" + taxi.getId() + ") -> " + taxi);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            taxiState = new TaxiState(taxi.getId(), "AVAILABLE", taxi, new TaxiRouteElement());
            update();
//        Path pickupPath = gismap.findPath(taxi.getLatLng(), from);
//        Path deliveryPath = gismap.findPath(from, to);
//        double totalDistance = pickupPath.getLength() + deliveryPath.getLength();
//        System.out.println("request:: Taxi + " + taxi.getId() + " total distance from " + from + " to " + to + " = " + totalDistance);
//        try {
//            Thread.sleep((int) (totalDistance / taxi.getSpeed()));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        String[] s = to.split(",");
//        double lat = Double.valueOf(s[0].trim());
//        double lng = Double.valueOf(s[1].trim());
//        taxi.setLat(lat);
//        taxi.setLng(lng);
//        controller.finishTrip(taxi);
    }
}
