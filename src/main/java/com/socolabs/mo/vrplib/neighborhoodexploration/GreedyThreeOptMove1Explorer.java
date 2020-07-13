package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.moves.ThreeOptMove1;

public class GreedyThreeOptMove1Explorer implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private GreedyExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    public GreedyThreeOptMove1Explorer(VRPVarRoutes vr, GreedyExplorationSelector selector) {
        this.vr = vr;
        this.selector = selector;
        this.objectiveF = selector.getObjectiveFunction();
        selector.add(this);
    }

    @Override
    public IVRPMove getMove() {
        for (VRPRoute cr : vr.getAllRoutes()) {
            for (VRPPoint x = cr.getStartPoint().getNext(); x != cr.getEndPoint(); x = x.getNext()) {
                for (VRPPoint y = x.getNext(); y != cr.getEndPoint(); y = y.getNext()) {
                    for (VRPPoint z = y.getNext(); z != cr.getEndPoint(); z = z.getNext()) {
                        if (vr.exploreThreeOptMove1(x, y, z)) {
                            LexMultiValues eval = objectiveF.evaluate();
                            if (objectiveF.compare(eval, 0) < 0) {
                                return new ThreeOptMove1(vr, x, y, z, eval);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    public String name() {
        return "GreedyThreeOptMove1Explorer";
    }
}