package com.socolabs.mo.vrplib.core;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VRPRoute {
    private int stt;
    private int truckCode;

    private VRPPoint startPoint;
    private VRPPoint endPoint;
    private int nbPoint;

    private int tmpNbPoint;
}
