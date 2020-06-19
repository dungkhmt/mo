package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import lombok.Getter;

@Getter
public class SchoolBusRoute extends VRPRoute {

    private int capacity;

    public SchoolBusRoute(VRPPoint startPoint, VRPPoint endPoint, String truckCode, int capacity, VRPVarRoutes vr) {
        super(startPoint, endPoint, truckCode, vr);
        this.capacity = capacity;
    }
}
