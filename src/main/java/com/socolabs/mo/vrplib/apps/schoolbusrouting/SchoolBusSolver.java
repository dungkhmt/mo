package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.DistanceElement;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRequest;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRoutingInput;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRoutingSolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SchoolBusSolver {

    private SchoolBusRoutingInput input;

    private HashMap<String, HashMap<String, Integer>> travelTimeMap;
    private HashMap<Integer, ArrayList<SchoolBusRequest>> mLocationId2Requests;

    public SchoolBusRoutingSolution solve(SchoolBusRoutingInput input) {
        this.input = input;
        initData();

        return null;
    }

    private void initData() {
        initTravelTime();
    }

    // khoi tao hashmap travel time giua cac locations
    private void initTravelTime() {
        HashSet<Integer> locationIdSet = new HashSet<>();
        for (SchoolBusRequest r : input.getRequests()) {
            int locationId = r.getPickupLocationId();
            locationIdSet.add(locationId);
        }
        locationIdSet.add(input.getShoolPointId());
        travelTimeMap = new HashMap<>();
        double timeScale = 1.0 + 0.01 * input.getConfigParams().getTimeScale();
        for (DistanceElement e : input.getDistances()) {
            String src = "" + e.getSrcCode();
            String dest = "" + e.getDestCode();
            if (!travelTimeMap.containsKey(src)) {
                travelTimeMap.put(src, new HashMap<>());
            }
            travelTimeMap.get(src).put(dest, (int) Math.ceil(timeScale * e.getTravelTime()));
        }
    }
}
