package com.socolabs.mo.vrplib.moves;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiValues;

public class TwoOptMove4 implements IVRPMove {

    private VRPVarRoutes vr;
    private VRPPoint x;
    private VRPPoint y;
    private LexMultiValues eval;

    public TwoOptMove4(VRPVarRoutes vr, VRPPoint x, VRPPoint y, LexMultiValues eval) {
        this.vr = vr;
        this.x = x;
        this.y = y;
        this.eval = eval;
    }

    @Override
    public void move() {
        System.out.println("propagate " + name() + " -> " + x + "; " + y + " eval = " + eval);
        vr.propagateTwoOptMove4(x, y);
    }

    @Override
    public LexMultiValues evaluation() {
        return eval;
    }

    public String name() {
        return "TwoOptMove4";
    }
}
