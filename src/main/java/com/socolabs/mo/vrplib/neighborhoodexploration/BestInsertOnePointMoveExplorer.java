package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.moves.OnePointMove;

public class BestInsertOnePointMoveExplorer implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private GreedyExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    public BestInsertOnePointMoveExplorer(VRPVarRoutes vr, GreedyExplorationSelector selector) {
        this.vr = vr;
        this.selector = selector;
        this.objectiveF = selector.getObjectiveFunction();
        selector.add(this);
    }

    @Override
    public IVRPMove getMove() {
        OnePointMove bestMove = null;
        LexMultiValues bestEval = null;
        for (VRPRoute cr : vr.getAllRoutes()) {
            for (VRPPoint x : vr.getFreePoints()) {
                for (VRPPoint y = cr.getStartPoint(); y != cr.getEndPoint(); y = y.getNext()) {
                    if (vr.exploreOnePointMove(x, y)) {
                        LexMultiValues eval = objectiveF.evaluate();
                        if (objectiveF.compare(eval, 0) < 0) {
                            if (bestEval == null || objectiveF.compare(eval, bestEval) < 0) {
                                bestMove = new OnePointMove(vr, x, y, eval);
                                bestEval = eval;
                            }
                        }
                    }
                }
            }
        }
        return bestMove;
    }

    public String name() {
        return "BestInsertOnePointMoveExplorer";
    }
}
