package com.socolabs.mo.vrplib.core;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VRPPoint {
    private int stt;
    private String locationCode;

    private int index;
    private VRPPoint next;
    private VRPPoint prev;
    private VRPRoute route;

    private int tmpIndex;
    private VRPPoint tmpNext;
    private VRPPoint tmpPrev;
    private VRPRoute tmpRoute;

    public VRPPoint(String locationCode) {
        this.locationCode = locationCode;
        stt = index = tmpIndex = 0;
        next = prev = tmpPrev = tmpNext = null;
        route = tmpRoute = null;
    }

    public void propagate() {
        index = tmpIndex;
        next = tmpNext;
        prev = tmpPrev;
        route = tmpRoute;
    }

    public void initTmp() {
        tmpIndex = index;
        tmpNext = next;
        tmpPrev = prev;
        tmpRoute = route;
    }

    public boolean isDepot() {
        return (route != null && (route.getStartPoint() == this || route.getEndPoint() == this));
    }

    public boolean isStartPoint() {
        return (route != null && route.getStartPoint() == this);
    }

    public boolean isEndPoint() {
        return (route != null && route.getEndPoint() == this);
    }

    public String toString() {
        return "Point(" + locationCode + ", " + index + ", " + stt + ")";
    }
}
