package com.socolabs.mo.vrplib.apps.schoolbusrouting.nodewieghtmanagers;

import com.socolabs.mo.vrplib.apps.schoolbusrouting.SchoolBusPickupPoint;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

public class NWMTravelTimeObjective2 implements INodeWeightManager {

    private VRPVarRoutes vr;
    private RevAccumulatedWeightPoints revAccTravelTime;
    private IDistanceManager travelTimeManager;
    private boolean sumTotalTravelTime;

    public NWMTravelTimeObjective2(VRPVarRoutes vr, RevAccumulatedWeightPoints revAccTravelTime, IDistanceManager travelTimeManager, boolean sumTotalTravelTime) {
        this.vr = vr;
        this.revAccTravelTime = revAccTravelTime;
        this.travelTimeManager = travelTimeManager;
        this.sumTotalTravelTime = sumTotalTravelTime;
    }

    @Override
    public double getWeight(VRPPoint point) {
        if (point.isStartPoint()) {
            return 0;
        }
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        double vi = 0;
        if (travelTimeManager.getDistance(point.getNext(), point) > CBLSVRP.EPS) {
            vi = travelTimeManager.getDistance(point, point.getNext()) / travelTimeManager.getDistance(point.getNext(), point);
        }
        if (p.getDirectTravelTimeToSchool() > CBLSVRP.EPS) {
            return vi + revAccTravelTime.getWeightValueOfPoint(point) / p.getDirectTravelTimeToSchool();
        }
        return vi;
    }

    @Override
    public double getTmpWeight(VRPPoint point) {
        if (point.isStartPoint()) {
            return 0;
        }
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        double vi = 0;
        if (travelTimeManager.getDistance(point.getTmpNext(), point) > CBLSVRP.EPS) {
            vi = travelTimeManager.getDistance(point, point.getTmpNext()) / travelTimeManager.getDistance(point.getTmpNext(), point);
        }
        if (p.getDirectTravelTimeToSchool() > CBLSVRP.EPS) {
            return vi + revAccTravelTime.getTmpWeightValueOfPoint(point) / p.getDirectTravelTimeToSchool();
        }
        return vi;
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
        return "NWMTravelTimeObjective2";
    }
}



