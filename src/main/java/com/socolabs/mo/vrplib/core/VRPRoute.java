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

    // khống tính 2 point startPoint và endPoint
//    private int nbPoints;
//
//    private int tmpNbPoints;

    public VRPRoute(VRPPoint startPoint, VRPPoint endPoint, String truckCode) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.truckCode = truckCode;
        startPoint.setNext(endPoint);
        endPoint.setPrev(startPoint);
        startPoint.setRoute(this);
        endPoint.setRoute(this);
        endPoint.setIndex(1);
        startPoint.initTmp();
        endPoint.initTmp();
    }

    public VRPRoute(VRPPoint startPoint, VRPPoint endPoint, String truckCode, VRPVarRoutes vr) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.truckCode = truckCode;
        startPoint.setNext(endPoint);
        endPoint.setPrev(startPoint);
        startPoint.setRoute(this);
        endPoint.setRoute(this);
        endPoint.setIndex(1);
        startPoint.initTmp();
        endPoint.initTmp();
        vr.post(this);
    }

    public void propagate() {
//        nbPoints = tmpNbPoints;
    }

    public void initTmp() {
//        tmpNbPoints = nbPoints;
    }

    public int getNbPoints() {
        return endPoint.getIndex() - startPoint.getIndex() - 1;
    }

    public int getTmpNbPoints() {
        return endPoint.getTmpIndex() - startPoint.getTmpIndex() - 1;
    }
//    public void increaseTmpNbPoints(int k) {
//        tmpNbPoints += k;
//    }
//
//    public void decreaseTmpNbPoints(int k) {
//        tmpNbPoints -= k;
//    }

    public String toString() {
        return "route(" + truckCode + ", " + stt + ")";
    }
}
