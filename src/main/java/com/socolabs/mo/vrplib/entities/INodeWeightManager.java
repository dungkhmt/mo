package com.socolabs.mo.vrplib.entities;

import com.socolabs.mo.vrplib.core.IVRPBasicEntity;
import com.socolabs.mo.vrplib.core.VRPPoint;

public interface INodeWeightManager extends IVRPBasicEntity {
    double getWeight(VRPPoint point);
    double getTmpWeight(VRPPoint point);
}