package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.moves.OnePointMove;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

public class GreedyInsertOnePointMoveExplorer  implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private ExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    private HashMap<VRPRoute, ArrayList<IVRPMove>> mRoute2RelatedMoves;
    private HashMap<VRPPoint, ArrayList<IVRPMove>> mPoint2RelatedMoves;
    private TreeSet<IVRPMove> orderedMoves;

    public GreedyInsertOnePointMoveExplorer(VRPVarRoutes vr, ExplorationSelector selector) {
        this.vr = vr;
        this.selector = selector;
        this.objectiveF = selector.getObjectiveFunction();

        mRoute2RelatedMoves = new HashMap<>();
        mPoint2RelatedMoves = new HashMap<>();
//        orderedMoves = new TreeSet<>(new Comparator<IVRPMove>() {
//            @Override
//            public int compare(IVRPMove o1, IVRPMove o2) {
//                return objectiveF.compare(o1.evaluation(), o2.evaluation());
//            }
//        });
        selector.add(this);
    }

    @Override
    public IVRPMove getMove() {
        System.out.print(name());
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
        return "GreedyInsertOnePointMoveExplorer";
    }
}
