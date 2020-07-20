package com.socolabs.mo.components.maps.distanceelementquery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
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
