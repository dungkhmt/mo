package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRequest;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRoutingInput;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
public class SchoolBusPickupPoint extends VRPPoint {

    private int groupId;
    private double pickupServiceTime;
    private int totalTravelTimeLimit;
    private int directTravelTimeToSchool;
    private ArrayList<SchoolBusRequest> requests;
    private SchoolBusRoutingInput input;

    public SchoolBusPickupPoint(String locationCode, VRPVarRoutes vr,
                                SchoolBusRoutingInput input, HashMap<String, HashMap<String, Integer>> travelTimeMap) {
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
        groupId = 0;
    }

    public void addRequest(SchoolBusRequest r) {
        requests.add(r);
        pickupServiceTime = Math.max(pickupServiceTime, r.getServicePickupDuration());
        groupId = Math.max(groupId, r.getGroupId());
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
