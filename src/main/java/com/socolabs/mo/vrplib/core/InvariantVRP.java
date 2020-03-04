package com.socolabs.mo.vrplib.core;

import java.util.ArrayList;

public interface InvariantVRP {
    VarRoutesVRP getVarRoutes();
    void propagateAddPoint(PointVRP point);
    void propagateAddRoute(RouteVRP route);
    void propagateKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y);
}
