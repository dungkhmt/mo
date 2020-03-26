package com.socolabs.mo.vrplib.entities;

import com.socolabs.mo.vrplib.core.IVRPBasicEntity;
import com.socolabs.mo.vrplib.core.VRPPoint;

public interface IAccumulatedCalculator extends IVRPBasicEntity {
    double caclAccWeightAtPoint(double prevValue, VRPPoint point);
    double calcTmpAccWeightAtPoint(double prevValue, VRPPoint point);
}
