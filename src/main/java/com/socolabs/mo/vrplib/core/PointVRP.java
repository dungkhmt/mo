package com.socolabs.mo.vrplib.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointVRP {
    private String locationCode;
    private int id;

    private int index; // thứ tự trên hành trình đi qua của truck
    private PointVRP next;
    private PointVRP prev;
    private RouteVRP route;

    private PointVRP oldNext;
    private PointVRP oldPrev;
    private RouteVRP oldRoute;

    private VarRoutesVRP vr;

    public PointVRP(VarRoutesVRP vr, String locationCode) {
        this.vr = vr;
        this.locationCode = locationCode;
        index = 0;
        next = prev = oldNext = oldPrev = null;
        route = null;
        vr.addPoint(this);
    }

    public boolean isDepot() {
        return route != null && (this == route.getStartPoint() || this == route.getEndPoint());
    }

    public boolean isStartPoint() {
        return route != null && route.getStartPoint() == this;
    }

    public boolean isEndPoint() {
        return route != null && route.getEndPoint() == this;
    }

    public void setOldState() {
        oldNext = next;
        oldPrev = prev;
        oldRoute = route;
    }

    public void isRemovedFromRoute() {
        if (route != null) {
            prev.setNext(next);
            next.setPrev(prev);
        }
        next = prev = null;
        route = null;
    }

    public void isInsertedRightAfter(PointVRP y) {
        route = y.getRoute();
        next = y.getNext();
        prev = y;
        next.setPrev(this);
        prev.setNext(this);
    }
}
