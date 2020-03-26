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
    private int nbPoints;

    private int tmpNbPoints;

    public void propagate() {
        nbPoints = tmpNbPoints;
    }

    public void initTmp() {
        tmpNbPoints = nbPoints;
    }

    public void increaseTmpNbPoints(int k) {
        tmpNbPoints += k;
    }

    public void decreaseTmpNbPoints(int k) {
        tmpNbPoints -= k;
    }
}
