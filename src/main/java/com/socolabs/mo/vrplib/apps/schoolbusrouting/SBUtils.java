package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.Utils;

public class SBUtils {

    public final static int BOARDING_TIME_1 = 900;
    public final static int BOARDING_TIME_2 = 1800;

    public static int getRoadBloakCap(int roadBlock) {
        if (roadBlock == Utils.CAP_FLEXIBILITY) {
            return Utils.CAP_45;
        }
        if (roadBlock == Utils.CAP_29) {
            return Utils.CAP_29;
        }
        return Utils.CAP_16;
    }
}
