package com.socolabs.mo.vrplib.entities;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.utils.CBLSVRP;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class VRPSolutionValue {
    private VRPVarRoutes vr;
    private LexMultiValues solutionValues;
    private ArrayList<VRPPoint> x;
    private ArrayList<VRPPoint> y;

    public VRPSolutionValue(VRPVarRoutes vr, LexMultiValues values) {
        this.vr = vr;
        solutionValues = values;
        x = new ArrayList<>();
        y = new ArrayList<>();
        for (VRPRoute r : vr.getAllRoutes()) {
            for (VRPPoint p = r.getStartPoint().getNext(); p != r.getEndPoint(); p = p.getNext()) {
                x.add(p);
                y.add(r.getStartPoint());
            }
        }
        for (VRPPoint p : vr.getAllPoints()) {
            if (p.getRoute() == null) {
                x.add(p);
                y.add(CBLSVRP.NULL_POINT);
            }
        }
    }

    public void assign() {
        vr.propagateKPointsMove(x, y);
    }
}
