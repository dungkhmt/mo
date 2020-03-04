package com.socolabs.mo.vrplib.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteVRP {
    private String truckCode;
    private int id;

    private PointVRP startPoint;
    private PointVRP endPoint;

    public RouteVRP(VarRoutesVRP vr, PointVRP s, PointVRP t, String truckCode) {
        startPoint = s;
        endPoint = t;
        this.truckCode = truckCode;
        s.setNext(t);
        t.setPrev(s);
        s.setRoute(this);
        t.setRoute(this);
        vr.addRoute(this);
    }

    public int getNbPointsOnRoute() {
        return endPoint.getIndex() - startPoint.getIndex() - 1;
    }
}
