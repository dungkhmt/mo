package com.socolabs.mo.vrplib.entities;

import com.socolabs.mo.vrplib.core.IVRPInvariant;

import java.util.Comparator;

public class InvariantSttCmp implements Comparator<IVRPInvariant> {
    @Override
    public int compare(IVRPInvariant o1, IVRPInvariant o2) {
        return o1.getStt() - o2.getStt();
    }
}
