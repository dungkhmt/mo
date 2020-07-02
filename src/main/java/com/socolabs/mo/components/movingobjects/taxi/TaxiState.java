package com.socolabs.mo.components.movingobjects.taxi;

import com.socolabs.mo.components.maps.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TaxiState {
    private String id;
    private String status;
    private Point position;
    private TaxiRouteElement route;
}
