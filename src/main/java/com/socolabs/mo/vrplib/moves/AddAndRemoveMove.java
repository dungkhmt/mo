package com.socolabs.mo.vrplib.moves;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.util.ArrayList;

public class AddAndRemoveMove implements IVRPMove {

    private VRPVarRoutes vr;
    private VRPPoint x;
    private VRPPoint y;
    private VRPPoint z;
    private LexMultiValues eval;

    // chèn x sau y, xóa z khỏi route
    public AddAndRemoveMove(VRPVarRoutes vr, VRPPoint x, VRPPoint y, VRPPoint z, LexMultiValues eval) {
        this.vr = vr;
        this.x = x;
        this.y = y;
        this.z = z;
        this.eval = eval;
    }

    @Override
    public void move() {
        ArrayList<VRPPoint> lx = new ArrayList<>();
        ArrayList<VRPPoint> ly = new ArrayList<>();
        lx.add(x); ly.add(y);
        lx.add(z); ly.add(CBLSVRP.NULL_POINT);
        System.out.println("propagate " + name() + " -> " + x + "; " + y + "; " + z + " eval = " + eval);
        vr.propagateKPointsMove(lx, ly);
    }

    @Override
    public LexMultiValues evaluation() {
        return eval;
    }

    public String name() {
        return "AddAndRemoveMove";
    }
}