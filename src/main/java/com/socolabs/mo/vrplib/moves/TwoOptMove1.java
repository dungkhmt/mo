package com.socolabs.mo.vrplib.moves;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiValues;

public class TwoOptMove1 implements IVRPMove {

    private VRPVarRoutes vr;
    private VRPPoint x;
    private VRPPoint y;
    private LexMultiValues eval;

    public TwoOptMove1(VRPVarRoutes vr, VRPPoint x, VRPPoint y, LexMultiValues eval) {
        this.vr = vr;
        this.x = x;
        this.y = y;
        this.eval = eval;
    }

    @Override
    public void move() {
        vr.propagateTwoOptMove1(x, y);
    }

    @Override
    public LexMultiValues evaluation() {
        return eval;
    }
}
