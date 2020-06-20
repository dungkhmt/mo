package com.socolabs.mo.vrplib.core;

import java.util.ArrayList;
import java.util.HashSet;

public interface IVRPInvariant extends IVRPBasicEntity {
    void explore();
    void propagate();
    void clearTmpData();

    HashSet<VRPPoint> getIndependentPoints();
    int getStt();
    void setStt(int stt);

    boolean verify();

}
