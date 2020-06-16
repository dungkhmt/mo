package com.socolabs.mo.vrplib.moves;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiValues;

public class ThreeOptMove4 implements IVRPMove {

    private VRPVarRoutes vr;
    private VRPPoint x;
    private VRPPoint y;
    private VRPPoint z;
    private LexMultiValues eval;

    public ThreeOptMove4(VRPVarRoutes vr, VRPPoint x, VRPPoint y, VRPPoint z, LexMultiValues eval) {
        this.vr = vr;
        this.x = x;
        this.y = y;
        this.z = z;
        this.eval = eval;
    }

    @Override
    public void move() {
        vr.propagateThreeOptMove4(x, y, z);
    }

    @Override
    public LexMultiValues evaluation() {
        return eval;
    }
}