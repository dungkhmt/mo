package com.socolabs.mo.vrplib.utils;

import com.socolabs.mo.vrplib.core.VRPPoint;

public class CBLSVRP {
    public final static int MAX_INT = (int) 1e9;
    public final static double EPS = 1e-6;
    public final static VRPPoint NULL_POINT = new VRPPoint("NULL_POINT");

    public static boolean equal(double x, double y) {
        return Math.abs(x - y) < EPS;
    }
}
