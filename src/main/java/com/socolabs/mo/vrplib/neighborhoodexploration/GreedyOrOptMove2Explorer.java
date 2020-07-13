package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.moves.OrOptMove2;

public class GreedyOrOptMove2Explorer implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private GreedyExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    public GreedyOrOptMove2Explorer(VRPVarRoutes vr, GreedyExplorationSelector selector) {
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
                    for (VRPPoint x1 = cr.getStartPoint().getNext(); x1 != cr.getEndPoint(); x1 = x1.getNext()) {
                        for (VRPPoint x2 = x1.getNext(); x2 != cr.getEndPoint(); x2 = x2.getNext()) {
                            for (VRPPoint y = ar.getStartPoint(); y != ar.getEndPoint(); y = y.getNext()) {
                                if (vr.exploreOrOptMove2(x1, x2, y)) {
                                    LexMultiValues eval = objectiveF.evaluate();
                                    if (objectiveF.compare(eval, 0) < 0) {
                                        return new OrOptMove2(vr, x1, x2, y, eval);
                                    }
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
        return "GreedyOrOptMove2Explorer";
    }
}
