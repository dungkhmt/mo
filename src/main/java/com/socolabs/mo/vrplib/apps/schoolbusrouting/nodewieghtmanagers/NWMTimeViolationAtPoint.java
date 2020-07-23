package com.socolabs.mo.vrplib.apps.schoolbusrouting.nodewieghtmanagers;

import com.socolabs.mo.vrplib.apps.schoolbusrouting.SchoolBusPickupPoint;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;

public class NWMTimeViolationAtPoint implements INodeWeightManager {

    private VRPVarRoutes vr;
    private RevAccumulatedWeightPoints revAccTravelTime;
    private double boardingTimeScale;
    private double coef;

    public NWMTimeViolationAtPoint(VRPVarRoutes vr, RevAccumulatedWeightPoints revAccTravelTime, double boardingTimeScale) {
        this.vr = vr;
        this.revAccTravelTime = revAccTravelTime;
        this.boardingTimeScale = boardingTimeScale;
        coef = boardingTimeScale / (5 + boardingTimeScale);
    }

    @Override
    public double getWeight(VRPPoint point) {
        if (point.isStartPoint()) {
            return 0;
        }
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        VRPRoute route = point.getRoute();
        if (route != null && !p.isDepot()) {
            int nbPoints = route.getNbPoints() + 1;
//            int d = nbPoints - point.getIndex();
            double f = (point.getIndex() == 1) ? coef : 0;
            return Math.max(0, revAccTravelTime.getWeightValueOfPoint(point) //- p.getTotalTravelTimeLimit());
                    - p.getDirectTravelTimeToSchool() * (boardingTimeScale - f));
        }
        return 0;
//        return Math.max(0, revAccTravelTime.getWeightValueOfPoint(point) - p.getTotalTravelTimeLimit());
    }

    @Override
    public double getTmpWeight(VRPPoint point) {
        if (point.isStartPoint()) {
            return 0;
        }
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        VRPRoute route = point.getTmpRoute();
        if (route != null && !p.isDepot()) {
            int nbPoints = route.getTmpNbPoints() + 1;
//            int d = nbPoints - point.getTmpIndex();
            double f = (point.getTmpIndex() == 1) ? coef : 0;
            return Math.max(0, revAccTravelTime.getTmpWeightValueOfPoint(point) //- p.getTotalTravelTimeLimit());
                    - p.getDirectTravelTimeToSchool() * (boardingTimeScale - f));
        }
        return 0;
//        return Math.max(0, revAccTravelTime.getTmpWeightValueOfPoint(point) - p.getTotalTravelTimeLimit());
    }

    @Override
    public void createPoint(VRPPoint point) {

    }

    @Override
    public void removePoint(VRPPoint point) {

    }

    @Override
    public void createRoute(VRPRoute route) {

    }

    @Override
    public void removeRoute(VRPRoute route) {

    }

    @Override
    public VRPVarRoutes getVarRoutes() {
        return vr;
    }

    @Override
    public String name() {
        return "NWMTimeViolationAtPoint";
    }
}

