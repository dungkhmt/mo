package com.socolabs.mo.vrplib.constraints.capacityviolation;

import com.socolabs.mo.vrplib.core.RouteVRP;

public interface ICVCalculator {
    double getViolations(double sum, RouteVRP route);
    void propagateAddRoute(RouteVRP route);
}
