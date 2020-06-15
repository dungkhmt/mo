package com.socolabs.mo.components.movingobjects.taxi;

import com.socolabs.mo.components.maps.GISMap;
import com.socolabs.mo.components.maps.Path;

public class TaxiAgent extends Thread {

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

    public void run() {
        System.out.println("Start running " + taxi.getId());
        Path pickupPath = gismap.findPath(taxi.getLatLng(), from);
        Path deliveryPath = gismap.findPath(from, to);
        double totalDistance = pickupPath.getLength() + deliveryPath.getLength();
        System.out.println("request:: Taxi + " + taxi.getId() + " total distance from " + from + " to " + to + " = " + totalDistance);
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
