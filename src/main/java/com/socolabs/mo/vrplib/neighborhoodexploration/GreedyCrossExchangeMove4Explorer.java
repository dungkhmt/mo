package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.CrossExchangeMove4;
import com.socolabs.mo.vrplib.moves.IVRPMove;

public class GreedyCrossExchangeMove4Explorer implements INeighborhoodExploration {
    private VRPVarRoutes vr;
    private GreedyExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    public GreedyCrossExchangeMove4Explorer(VRPVarRoutes vr, GreedyExplorationSelector selector) {
        this.vr = vr;
        this.selector = selector;
        this.objectiveF = selector.getObjectiveFunction();
        selector.add(this);
    }

    @Override
    public IVRPMove getMove() {
        for (VRPRoute cr : vr.getAllRoutes()) {
            if (cr.getNbPoints() > 1) {
                for (VRPRoute ar : vr.getAllRoutes()) {
                    if (ar.getNbPoints() > 1) {
                        for (VRPPoint x1 = cr.getStartPoint().getNext(); x1.getNext() != cr.getEndPoint(); x1 = x1.getNext()) {
                            for (VRPPoint y1 = x1.getNext(); y1 != cr.getEndPoint(); y1 = y1.getNext()) {
                                for (VRPPoint x2 = ar.getStartPoint().getNext(); x2.getNext() != ar.getEndPoint(); x2 = x2.getNext()) {
                                    for (VRPPoint y2 = x2.getNext(); y2 != ar.getEndPoint(); y2 = y2.getNext()) {
                                        if (vr.exploreCrossExchangeMove4(x1, y1, x2, y2)) {
                                            LexMultiValues eval = objectiveF.evaluate();
                                            if (objectiveF.compare(eval, 0) < 0) {
                                                return new CrossExchangeMove4(vr, x1, y1, x2, y2, eval);
                                            }
                                        }
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
        return "GreedyCrossExchangeMove4Explorer";
    }
}
