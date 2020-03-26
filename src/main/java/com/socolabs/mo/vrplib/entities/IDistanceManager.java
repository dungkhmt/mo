package com.socolabs.mo.vrplib.entities;

import com.socolabs.mo.vrplib.core.IVRPBasicEntity;
import com.socolabs.mo.vrplib.core.VRPPoint;

public interface IDistanceManager extends IVRPBasicEntity {
    double getDistance(VRPPoint x, VRPPoint y);
    double getTmpDistance(VRPPoint x, VRPPoint y);
}
