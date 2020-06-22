package com.socolabs.mo.vrplib.apps.schoolbusrouting.nodewieghtmanagers;

import com.socolabs.mo.vrplib.apps.schoolbusrouting.SchoolBusPickupPoint;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;

public class NWMGroupViolationAtPoint implements INodeWeightManager {

    private VRPVarRoutes vr;

    public NWMGroupViolationAtPoint(VRPVarRoutes vr) {
        this.vr = vr;
    }

    @Override
    public double getWeight(VRPPoint point) {
        if (!point.isDepot()) {
            SchoolBusPickupPoint prev = (SchoolBusPickupPoint) point.getPrev();
            SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
            if (p.getGroupId() != prev.getGroupId()) {
                return p.getGroupId() > 0 ? 1 : 0;
            }
        }
        return 0;
    }

    @Override
    public double getTmpWeight(VRPPoint point) {
        if (!point.isDepot()) {
            SchoolBusPickupPoint prev = (SchoolBusPickupPoint) point.getTmpPrev();
            SchoolBusPickupPoint p = (SchoolBusPickupPoint) point;
            if (p.getGroupId() != prev.getGroupId()) {
                return 1;
            }
        }
        return 0;
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
        return "NWMGroupViolationAtPoint";
    }
}
