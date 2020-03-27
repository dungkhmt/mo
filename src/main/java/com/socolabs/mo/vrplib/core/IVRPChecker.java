package com.socolabs.mo.vrplib.core;

public interface IVRPChecker {
    boolean checkInsertPointMove(VRPPoint x, VRPPoint y);
    boolean checkOnePointMove(VRPPoint x, VRPPoint y);
}
