package com.socolabs.mo.vrplib.core;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VRPPoint {
    private int stt;
    private String location;

    private int index;
    private VRPPoint next;
    private VRPPoint prev;
    private VRPRoute route;

    private int tmpIndex;
    private VRPPoint tmpNext;
    private VRPPoint tmpPrev;
    private VRPRoute tmpRoute;

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
        return !(route != null && (route.getStartPoint() == this || route.getEndPoint() == this));
    }
}
