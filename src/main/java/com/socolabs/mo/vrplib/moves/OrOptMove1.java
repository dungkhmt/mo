package com.socolabs.mo.vrplib.moves;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiValues;

public class OrOptMove1 implements IVRPMove {

    private VRPVarRoutes vr;
    private VRPPoint x1;
    private VRPPoint x2;
    private VRPPoint y;
    private LexMultiValues eval;

    public OrOptMove1(VRPVarRoutes vr, VRPPoint x1, VRPPoint x2, VRPPoint y, LexMultiValues eval) {
        this.vr = vr;
        this.x1 = x1;
        this.x2 = x2;
        this.y = y;
        this.eval = eval;
    }

    @Override
    public void move() {
        vr.propagateOrOptMove1(x1, x2, y);
    }

    @Override
    public LexMultiValues evaluation() {
        return eval;
    }
}
