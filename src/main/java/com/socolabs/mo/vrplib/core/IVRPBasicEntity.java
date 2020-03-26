package com.socolabs.mo.vrplib.core;

public interface IVRPBasicEntity {
    void addNewPoint(VRPPoint point);
    void removePoint(VRPPoint point);
    void addNewRoute(VRPRoute route);
    void removeRoute(VRPRoute route);
    VRPVarRoutes getVarRoutes();
    String name();
}
