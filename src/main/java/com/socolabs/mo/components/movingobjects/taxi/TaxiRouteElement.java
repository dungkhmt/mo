package com.socolabs.mo.components.movingobjects.taxi;

import com.socolabs.mo.components.maps.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaxiRouteElement {
    private Point[] points;

    public TaxiRouteElement() {
        points = new Point[0];
    }
}
