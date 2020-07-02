package com.socolabs.mo.components.movingobjects.taxi;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TaxiSimulation {
    private TaxiState[] states;
    private TaxiOrder[] orders;
}
