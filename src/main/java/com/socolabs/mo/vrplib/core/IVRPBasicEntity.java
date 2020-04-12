package com.socolabs.mo.vrplib.core;

public interface IVRPBasicEntity {
    void createPoint(VRPPoint point);
    void removePoint(VRPPoint point);
    void createRoute(VRPRoute route);
    void removeRoute(VRPRoute route);
    VRPVarRoutes getVarRoutes();
    String name();
}
