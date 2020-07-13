package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.moves.TwoOptMoveOneRoute;

public class GreedyTwoOptMoveOneRouteExplorer implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private GreedyExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    public GreedyTwoOptMoveOneRouteExplorer(VRPVarRoutes vr, GreedyExplorationSelector selector) {
        this.vr = vr;
        this.selector = selector;
        this.objectiveF = selector.getObjectiveFunction();
        selector.add(this);
    }

    @Override
    public IVRPMove getMove() {
        for (VRPRoute cr : vr.getAllRoutes()) {
            for (VRPPoint y = cr.getStartPoint(); y != cr.getEndPoint(); y = y.getNext()) {
                for (VRPPoint x = y.getNext(); x != cr.getEndPoint(); x = x.getNext()) {
                    if (vr.exploreTwoOptMoveOneRoute(x, y)) {
                        LexMultiValues eval = objectiveF.evaluate();
                        if (objectiveF.compare(eval, 0) < 0) {
                            return new TwoOptMoveOneRoute(vr, x, y, eval);
                        }
                    }
                }
            }
        }
        return null;
    }

    public String name() {
        return "GreedyTwoOptMoveOneRouteExplorer";
    }
}
