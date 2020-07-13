package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.moves.OnePointMove;

public class GreedyOnePointMoveExplorer implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private GreedyExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    public GreedyOnePointMoveExplorer(VRPVarRoutes vr, GreedyExplorationSelector selector) {
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
                        for (VRPPoint x = ar.getStartPoint().getNext(); x != ar.getEndPoint(); x = x.getNext()) {
                            if (vr.exploreOnePointMove(x, y)) {
                                LexMultiValues eval = objectiveF.evaluate();
                                if (objectiveF.compare(eval, 0) < 0) {
                                    return new OnePointMove(vr, x, y, eval);
                                }
                            }
                        }
                    }
                }
            }
            for (VRPPoint x : vr.getFreePoints()) {
                for (VRPPoint y = cr.getStartPoint(); y != cr.getEndPoint(); y = y.getNext()) {
                    if (vr.exploreOnePointMove(x, y)) {
                        LexMultiValues eval = objectiveF.evaluate();
                        if (objectiveF.compare(eval, 0) < 0) {
                            return new OnePointMove(vr, x, y, eval);
                        }
                    }
                }
            }
        }
        return null;
    }

    public String name() {
        return "GreedyOnePointMoveExplorer";
    }
}
