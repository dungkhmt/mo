package com.socolabs.mo.components.movingobjects.taxi;

import com.socolabs.mo.components.maps.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TaxiOrder {
    private String id;
    private Point pickupPoint;
    private Point deliveryPoint;
}
