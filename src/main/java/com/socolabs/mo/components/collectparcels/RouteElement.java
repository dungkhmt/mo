package com.socolabs.mo.components.collectparcels;

import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class RouteElement implements Comparable<RouteElement> {
    private Vertex location;
    private Route route;
    private RouteElement next;
    private RouteElement prev;
    private Parcel parcel;
    private double arrivalTime;

    public RouteElement(Vertex location) {
        this.location = location;
        route = null;
        prev = next = null;
        parcel = null;
        arrivalTime = 0;
    }

    public RouteElement(Route route) {
        this.route = route;
    }

    public void updateArrivalTime(double delta) {
        arrivalTime += delta;
    }

    @Override
    public int compareTo(@NotNull RouteElement o) {
        int d = route.getRemainWeight() - o.route.getRemainWeight();
        if (d == 0) {
            d = route.getVehicle().getId() - o.route.getVehicle().getId();
        }
        if (d == 0) {
            d = location.getIndex() - o.getLocation().getIndex();
        }
        return d;
    }
}
