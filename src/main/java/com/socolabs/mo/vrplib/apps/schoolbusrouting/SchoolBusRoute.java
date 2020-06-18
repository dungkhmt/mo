package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;

public class SchoolBusRoute extends VRPRoute {

    public SchoolBusRoute(VRPPoint startPoint, VRPPoint endPoint, String truckCode, VRPVarRoutes vr) {
        super(startPoint, endPoint, truckCode, vr);
    }
}
