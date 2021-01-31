package com.socolabs.mo.components.collectparcels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Route {
    private Depot depot;
    private Vehicle vehicle;
    private RouteElement cur;
    private RouteElement end;
    private int weight;

    public void reset() {
        weight = 0;
    }

    public void addParcel(Parcel p) {
        weight += p.getWeight();
        assert weight <= vehicle.getCapacity();
    }
}
