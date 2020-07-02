package com.socolabs.mo.components.movingobjects.taxi;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TaxiOrderInput {
    private Taxi taxi;
    private TaxiOrder order;
}
