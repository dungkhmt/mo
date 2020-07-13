package com.socolabs.mo.vrplib.apps.schoolbusrouting.nodewieghtmanagers;

import com.socolabs.mo.vrplib.apps.schoolbusrouting.SchoolBusPickupPoint;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;

public class NWMDistanceViolationAtPoint  implements INodeWeightManager {

    private VRPVarRoutes vr;
    private RevAccumulatedWeightPoints revAccDistance;
    private double boardingTimeScale;

    public NWMDistanceViolationAtPoint(VRPVarRoutes vr, RevAccumulatedWeightPoints revAccDistance, double boardingTimeScale) {
        this.vr = vr;
        this.revAccDistance = revAccDistance;
        this.boardingTimeScale = boardingTimeScale;
    }

    @Override
    public double getWeight(VRPPoint point) {
        if (point.isStartPoint()) {
            return 0;
        }
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        VRPRoute route = point.getRoute();
        if (route != null) {
            int nbPoints = route.getNbPoints() + 1;
            int d = nbPoints - point.getIndex();
            return Math.max(0, revAccDistance.getWeightValueOfPoint(point) //- p.getTotalTravelTimeLimit());
                    - p.getDirectDistanceToSchool() * (boardingTimeScale + 0.05 * d));
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
        if (route != null) {
            int nbPoints = route.getTmpNbPoints() + 1;
            int d = nbPoints - point.getTmpIndex();
            return Math.max(0, revAccDistance.getTmpWeightValueOfPoint(point) //- p.getTotalTravelTimeLimit());
                    - p.getDirectDistanceToSchool() * (boardingTimeScale + 0.05 * d));
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



