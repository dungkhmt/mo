package com.socolabs.mo.vrplib.core;

import java.util.ArrayList;

public interface IFunctionVRP extends InvariantVRP {
    double getValue();

    double evaluateKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y);
}
