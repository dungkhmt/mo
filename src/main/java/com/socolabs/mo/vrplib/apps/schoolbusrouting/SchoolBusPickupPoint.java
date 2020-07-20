package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRequest;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRoutingInput;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Getter
public class SchoolBusPickupPoint extends VRPPoint {

    private int groupId;
    private double pickupServiceTime;
    private int totalTravelTimeLimit;
    private int directTravelTimeToSchool;
    private double directDistanceToSchool;
    private double haversineDistanceToSchool;
    private ArrayList<SchoolBusRequest> requests;
    private SchoolBusRoutingInput input;

    public SchoolBusPickupPoint(String locationCode, VRPVarRoutes vr,
                                SchoolBusRoutingInput input,
                                HashMap<String, HashMap<String, Integer>> travelTimeMap,
                                HashMap<String, HashMap<String, Double>> distanceMap) {
        super(locationCode, vr);
        this.input = input;
        requests = new ArrayList<>();
        pickupServiceTime = 0;

        int directTravelTime = travelTimeMap.get(locationCode).get("" + input.getShoolPointId());
        if (directTravelTime <= SBUtils.BOARDING_TIME_1) {
            totalTravelTimeLimit = (int) ((1 + 0.01 * input.getConfigParams().getBoardingTimeScale1()) * directTravelTime);
        } else if (directTravelTime <= SBUtils.BOARDING_TIME_2) {
            totalTravelTimeLimit = (int) ((1 + 0.01 * input.getConfigParams().getBoardingTimeScale2()) * directTravelTime);
        } else {
            // thay đổi khi sử dụng boardingTimeScale3
            totalTravelTimeLimit = (int) ((1 + 0.05 * input.getConfigParams().getBoardingTimeScale2()) * directTravelTime);
        }
        directTravelTimeToSchool = directTravelTime;
        directDistanceToSchool = distanceMap.get(locationCode).get("" + input.getShoolPointId());
        groupId = 0;
    }

    public double computeDistanceHaversine(double lat1, double long1,
                                           double lat2, double long2) {
        double SCALE = 1;
        double PI = 3.14159265;
        long1 = long1 * 1.0 / SCALE;
        lat1 = lat1 * 1.0 / SCALE;
        long2 = long2 * 1.0 / SCALE;
        lat2 = lat2 * 1.0 / SCALE;

        double dlat1 = lat1 * PI / 180;
        double dlong1 = long1 * PI / 180;
        double dlat2 = lat2 * PI / 180;
        double dlong2 = long2 * PI / 180;

        double dlong = dlong2 - dlong1;
        double dlat = dlat2 - dlat1;

        double aHarv = Math.pow(Math.sin(dlat / 2), 2.0) + Math.cos(dlat1)
                * Math.cos(dlat2) * Math.pow(Math.sin(dlong / 2), 2.0);
        double cHarv = 2 * Math.atan2(Math.sqrt(aHarv), Math.sqrt(1.0 - aHarv));

        double R = 6378.137; // in km

        return R * cHarv * SCALE; // in km

    }

    public void addRequest(SchoolBusRequest r) {
        requests.add(r);
        pickupServiceTime = Math.max(pickupServiceTime, r.getServicePickupDuration());
        groupId = Math.max(groupId, r.getGroupId());
        if (requests.size() == 1) {
            haversineDistanceToSchool = computeDistanceHaversine(r.getLat_pickup(), r.getLong_pickup(),
                    input.getLat_school(), input.getLong_school());
        }
    }

    public int size() {
        return requests.size();
    }

    public String getStudentLst() {
        String st = "[";
        int i = 0;
        for (SchoolBusRequest re : requests) {
            if (i > 0) {
                st += ", ";
            }
            st += re.getIdPerson();
            i++;
        }
        return st + "]";
    }

    public double getLat() {
        if (requests.size() > 0) {
            return requests.get(0).getLat_pickup();
        } else {
            return input.getLat_school();
        }
    }

    public double getLng() {
        if (requests.size() > 0) {
            return requests.get(0).getLong_pickup();
        } else {
            return input.getLong_school();
        }
    }
}
