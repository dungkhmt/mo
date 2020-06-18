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

    private double pickupServiceTime;
    private int totalTravelTimeLimit;
    private ArrayList<SchoolBusRequest> requests;

    public SchoolBusPickupPoint(String locationCode, VRPVarRoutes vr,
                                SchoolBusRoutingInput input, HashMap<String, HashMap<String, Integer>> travelTimeMap) {
        super(locationCode, vr);
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
    }

    public void addRequest(SchoolBusRequest r) {
        requests.add(r);
        pickupServiceTime = Math.max(pickupServiceTime, r.getServicePickupDuration());
    }

    public int size() {
        return requests.size();
    }
}
