package com.socolabs.mo.vrplib.core;

import java.util.ArrayList;

public interface IcheckerVRP {

    boolean checkKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y);
    void validateKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y);
}
