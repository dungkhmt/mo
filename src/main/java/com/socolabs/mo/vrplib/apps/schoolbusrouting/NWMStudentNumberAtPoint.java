package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;

public class NWMStudentNumberAtPoint implements INodeWeightManager {

    private VRPVarRoutes vr;

    public NWMStudentNumberAtPoint(VRPVarRoutes vr) {
        this.vr = vr;
    }

    @Override
    public double getWeight(VRPPoint point) {
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        return p.size();
    }

    @Override
    public double getTmpWeight(VRPPoint point) {
        SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
        return p.size();
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
        return "NWMStudentNumberAtPoint";
    }
}
