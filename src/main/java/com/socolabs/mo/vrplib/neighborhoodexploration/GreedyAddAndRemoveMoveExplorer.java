package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.AddAndRemoveMove;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.util.ArrayList;

public class GreedyAddAndRemoveMoveExplorer implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private GreedyExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    public GreedyAddAndRemoveMoveExplorer(VRPVarRoutes vr, GreedyExplorationSelector selector) {
        this.vr = vr;
        this.selector = selector;
        this.objectiveF = selector.getObjectiveFunction();
        selector.add(this);
    }

    @Override
    public IVRPMove getMove() {
        for (VRPRoute cr : vr.getAllRoutes()) {
            for (VRPPoint x : vr.getFreePoints()) {
                for (VRPPoint y = cr.getStartPoint(); y != cr.getEndPoint(); y = y.getNext()) {
                    for (VRPPoint z = cr.getStartPoint().getNext(); z != cr.getEndPoint(); z = z.getNext()) {
                        if (y != z) {
                            ArrayList<VRPPoint> lx = new ArrayList<>();
                            ArrayList<VRPPoint> ly = new ArrayList<>();
                            lx.add(x); ly.add(y);
                            lx.add(z); ly.add(CBLSVRP.NULL_POINT);
                            if (vr.exploreKPointsMove(lx, ly)) {
                                LexMultiValues eval = objectiveF.evaluate();
                                if (objectiveF.compare(eval, 0) < 0) {
                                    return new AddAndRemoveMove(vr, x, y, z, eval);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String name() {
        return "GreedyAddAndRemoveMoveExplorer";
    }
}
