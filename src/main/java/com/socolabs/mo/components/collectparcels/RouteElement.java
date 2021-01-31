package com.socolabs.mo.components.collectparcels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class RouteElement {
    private Route route;
    private RouteElement next;
    private RouteElement prev;
    private Parcel parcel;
    private double arrivalTime;

    public RouteElement() {
        route = null;
        prev = next = null;
        parcel = null;
        arrivalTime = 0;
    }

    public void updateArrivalTime(double delta) {
        arrivalTime += delta;
    }
}
