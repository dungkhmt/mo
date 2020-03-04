package com.socolabs.mo.vrplib.core;

public interface IWeightCaculatorVRP {

    double getEdgeWeight(PointVRP x, PointVRP y, RouteVRP r);
    double getPointWeight(PointVRP p, RouteVRP r);
}
