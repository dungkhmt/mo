package com.socolabs.mo.vrplib.core;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VRPRoute {
    private int stt;
    private String truckCode;

    private VRPPoint startPoint;
    private VRPPoint endPoint;
    private int nbPoints;

    private int tmpNbPoints;

    public VRPRoute(VRPPoint startPoint, VRPPoint endPoint, String truckCode) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.truckCode = truckCode;
        startPoint.setNext(endPoint);
        endPoint.setPrev(startPoint);
        startPoint.setRoute(this);
        endPoint.setRoute(this);
        startPoint.initTmp();
        endPoint.initTmp();
    }

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
