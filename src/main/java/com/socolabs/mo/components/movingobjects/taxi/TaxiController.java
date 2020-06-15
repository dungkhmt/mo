package com.socolabs.mo.components.movingobjects.taxi;

import com.socolabs.mo.components.maps.GISMap;
import com.socolabs.mo.components.maps.Path;
import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.movingobjects.ILocation;
import com.socolabs.mo.components.objectmanager.MovingObjectManager;
import com.socolabs.mo.model.modelmap.MapWindow;
import lombok.Getter;

import java.util.HashMap;

@Getter
public class TaxiController {

    private MovingObjectManager mgr;
    private GISMap gismap;

    private HashMap<String, Taxi> mID2TaxiObject;

    public TaxiController() {
        gismap = new GISMap();
        MapWindow mw = gismap.computeCoordinateMapWindows();
        mgr = new MovingObjectManager(mw.getMinlat(), mw.getMinlng(), mw.getMaxlat(), mw.getMaxlng());
        mID2TaxiObject = new HashMap<>();
    }

    public synchronized void addNewTaxi(String id, double lat, double lng) {
        Taxi newOne = new Taxi(id, lat, lng);
        mgr.addObject(newOne);
    }

    public synchronized void request(String from, String to) {
        System.out.println("Request from " + from + " to " + to);
        Taxi t = (Taxi) mgr.findNearestMovingObject(from);

        mgr.removeObject(t);
        if (t.getStatus() != Taxi.STATUS_AVAILABLE) {
            System.out.println("EXCEPTION::request -> findNearestMovingObject");
            System.exit(-1);
        }
        t.setStatus(Taxi.STATUS_RUNNING);
        // create and run a agent
        TaxiAgent agent = new TaxiAgent(this, gismap, t, from, to);
        agent.run();
    }

    public synchronized void finishTrip(Taxi taxi) {
        mgr.addObject(taxi);
    }

    public static void main(String[] args) throws InterruptedException {

        class AddTaxi extends Thread {
            private String id;
            private double lat;
            private double lng;
            private TaxiController controller;

            public AddTaxi(TaxiController controller, String id, double lat, double lng) {
                this.id = id;
                this.lat = lat;
                this.lng = lng;
                this.controller = controller;
            }

            public void run() {
                controller.addNewTaxi(id, lat, lng);
            }
        }

        class TaxiRequest extends Thread {
            private TaxiController controller;
            private String from;
            private String to;

            public TaxiRequest(TaxiController controller, String from, String to) {
                this.controller = controller;
                this.from = from;
                this.to = to;
            }

            public void run() {
                controller.request(from, to);
            }
        }

        int nbTaxies = 10000;
        TaxiController taxiController = new TaxiController();
        GISMap gismap = taxiController.getGismap();
        AddTaxi[] taxis = new AddTaxi[nbTaxies + 1];
        for (int i = 1; i <= nbTaxies; i++) {
            Point p = gismap.getRandomPoint();
            taxis[i] = new AddTaxi(taxiController, "taxi_" + i, p.getLat(), p.getLng());
            taxis[i].start();
        }
        for (int i = 1; i <= nbTaxies; i++) {
            taxis[i].join();
        }

        int nbRequests = 1000;
        TaxiRequest[] requests = new TaxiRequest[nbRequests + 1];
        for (int i = 0; i < nbRequests; i++) {
            requests[i] = new TaxiRequest(taxiController, gismap.getRandomPoint().getLatLng(), gismap.getRandomPoint().getLatLng());
            requests[i].start();
        }

        for (int i = 0; i < nbRequests; i++) {
            requests[i].join();
        }
    }
}
