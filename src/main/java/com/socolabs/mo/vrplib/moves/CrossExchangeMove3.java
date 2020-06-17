package com.socolabs.mo.vrplib.moves;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiValues;

public class CrossExchangeMove3 implements IVRPMove {

    private VRPVarRoutes vr;
    private VRPPoint x1;
    private VRPPoint y1;
    private VRPPoint x2;
    private VRPPoint y2;
    private LexMultiValues eval;

    public CrossExchangeMove3(VRPVarRoutes vr, VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2, LexMultiValues eval) {
        this.vr = vr;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.eval = eval;
    }

    @Override
    public void move() {
        System.out.println("propagate " + name() + " -> " + x1 + "; " + y1 + "; " + x2 + "; " + y2 + " eval = " + eval);
        vr.propagateCrossExchangeMove3(x1, y1, x2, y2);
    }

    @Override
    public LexMultiValues evaluation() {
        return eval;
    }

    public String name() {
        return "CrossExchangeMove3";
    }
}
