package com.socolabs.mo.vrplib.search;

import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.neighborhoodexploration.*;

public class GreedySearch {

    private VRPVarRoutes vr;
    private LexMultiFunctions objectiveFunc;

    public GreedySearch(VRPVarRoutes vr, LexMultiFunctions objectiveFunc) {
        this.vr = vr;
        this.objectiveFunc = objectiveFunc;
    }

    public void search(int maxIter, int maxTime) {
        ExplorationSelector selector = new ExplorationSelector(vr, objectiveFunc);
        new GreedyCrossExchangeMove1Explorer(vr, selector);
        new GreedyCrossExchangeMove2Explorer(vr, selector);
        new GreedyCrossExchangeMove3Explorer(vr, selector);
        new GreedyCrossExchangeMove4Explorer(vr, selector);
        new GreedyOnePointMoveExplorer(vr, selector);
        new GreedyOrOptMove1Explorer(vr, selector);
        new GreedyOrOptMove2Explorer(vr, selector);
        new GreedyThreeOptMove1Explorer(vr, selector);
        new GreedyThreeOptMove2Explorer(vr, selector);
        new GreedyThreeOptMove3Explorer(vr, selector);
        new GreedyThreeOptMove4Explorer(vr, selector);
        new GreedyThreeOptMove5Explorer(vr, selector);
        new GreedyThreeOptMove6Explorer(vr, selector);
        new GreedyThreeOptMove7Explorer(vr, selector);
        new GreedyThreeOptMove8Explorer(vr, selector);
        new GreedyTwoOptMove1Explorer(vr, selector);
        new GreedyTwoOptMove2Explorer(vr, selector);
        new GreedyTwoOptMove3Explorer(vr, selector);
        new GreedyTwoOptMove4Explorer(vr, selector);
        new GreedyTwoOptMove5Explorer(vr, selector);
        new GreedyTwoOptMove6Explorer(vr, selector);
        new GreedyTwoOptMove7Explorer(vr, selector);
        new GreedyTwoOptMove8Explorer(vr, selector);
        new GreedyTwoOptMoveOneRouteExplorer(vr, selector);
        new GreedyTwoPointsMoveExplorer(vr, selector);


        int iter = 0;
        long startTime = System.currentTimeMillis();
        while (iter < maxIter && System.currentTimeMillis() - startTime < maxTime) {
            iter++;
            LexMultiValues oldValues = objectiveFunc.values();
            System.out.println("iter " + iter + " :: objectiveValues = " + oldValues);
            IVRPMove bestMove = selector.selectBestMove();
            if (bestMove != null) {
                bestMove.move();
                LexMultiValues newValues = objectiveFunc.values();
                LexMultiValues deltaValues = newValues.minus(oldValues);
                if (objectiveFunc.compare(deltaValues, bestMove.evaluation()) != 0) {
                    System.out.println(name() + ":: EXCEPTION deltaValues = " + deltaValues + " - moveValues = " + bestMove.evaluation());
                    System.exit(-1);
                }
            } else {
                break;
            }
        }
        System.out.println("Best values = " + objectiveFunc.values());
    }

    public String name() {
        return "GreedySearch";
    }
}
