package com.socolabs.mo.components.maps.distanceelementquery;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GeneralDistanceElement {
    private String fromId;
    private double fromLat;
    private double fromLng;
    private String toId;
    private double toLat;
    private double toLng;
    private double distance;
    private int travelTimeTruck;
    private int travelTimeMotobike;
}
