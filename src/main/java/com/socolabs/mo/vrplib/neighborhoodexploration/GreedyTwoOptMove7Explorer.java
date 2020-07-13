package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.moves.TwoOptMove7;

public class GreedyTwoOptMove7Explorer implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private GreedyExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    public GreedyTwoOptMove7Explorer(VRPVarRoutes vr, GreedyExplorationSelector selector) {
        this.vr = vr;
        this.selector = selector;
        this.objectiveF = selector.getObjectiveFunction();
        selector.add(this);
    }

    @Override
    public IVRPMove getMove() {
        for (VRPRoute cr : vr.getAllRoutes()) {
            for (VRPRoute ar : vr.getAllRoutes()) {
                if (cr != ar) {
                    for (VRPPoint y = cr.getStartPoint(); y != cr.getEndPoint(); y = y.getNext()) {
                        for (VRPPoint x = ar.getStartPoint(); x != ar.getEndPoint(); x = x.getNext()) {
                            if (vr.exploreTwoOptMove7(x, y)) {
                                LexMultiValues eval = objectiveF.evaluate();
                                if (objectiveF.compare(eval, 0) < 0) {
                                    return new TwoOptMove7(vr, x, y, eval);
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
        return "GreedyTwoOptMove7Explorer";
    }
}
