package com.socolabs.mo.vrplib.search;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.entities.VRPSolutionValue;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.neighborhoodexploration.*;

import java.util.ArrayList;
import java.util.Random;

public class GreedySearch {

    public final static int ONE_POINT_MOVE = 0;
    public final static int TWO_POINTS_MOVE = 1;
    public final static int TWO_OPT_MOVE = 2;
    public final static int THREE_OPT_MOVE = 3;
    public final static int OR_OPT_MOVE = 4;
    public final static int CROSS_EXCHANGE_MOVE = 5;
    public final static int INSERT_ONE_MOVE = 6;

    private Random rand = new Random(1993);

    private VRPVarRoutes vr;
    private LexMultiFunctions objectiveFunc;
    private GreedyExplorationSelector selector;

    public GreedySearch(VRPVarRoutes vr, LexMultiFunctions objectiveFunc) {
        this.vr = vr;
        this.objectiveFunc = objectiveFunc;
        selector = new GreedyExplorationSelector(vr, objectiveFunc);
    }

    public void addExplorer(int moveType) {
        switch (moveType) {
            case INSERT_ONE_MOVE:
                new BestInsertOnePointMoveExplorer(vr, selector);
                break;
            case ONE_POINT_MOVE:
                new GreedyOnePointMoveExplorer(vr, selector);
                break;
            case TWO_POINTS_MOVE:
                new GreedyTwoOptMoveOneRouteExplorer(vr, selector);
                new GreedyTwoPointsMoveExplorer(vr, selector);
                break;
            case TWO_OPT_MOVE:
                new GreedyTwoOptMove1Explorer(vr, selector);
                new GreedyTwoOptMove2Explorer(vr, selector);
                new GreedyTwoOptMove3Explorer(vr, selector);
                new GreedyTwoOptMove4Explorer(vr, selector);
                new GreedyTwoOptMove5Explorer(vr, selector);
                new GreedyTwoOptMove6Explorer(vr, selector);
                new GreedyTwoOptMove7Explorer(vr, selector);
                new GreedyTwoOptMove8Explorer(vr, selector);
                break;
            case THREE_OPT_MOVE:
                new GreedyThreeOptMove1Explorer(vr, selector);
                new GreedyThreeOptMove2Explorer(vr, selector);
                new GreedyThreeOptMove3Explorer(vr, selector);
                new GreedyThreeOptMove4Explorer(vr, selector);
                new GreedyThreeOptMove5Explorer(vr, selector);
                new GreedyThreeOptMove6Explorer(vr, selector);
                new GreedyThreeOptMove7Explorer(vr, selector);
                new GreedyThreeOptMove8Explorer(vr, selector);
                break;
            case OR_OPT_MOVE:
                new GreedyOrOptMove1Explorer(vr, selector);
                new GreedyOrOptMove2Explorer(vr, selector);
                break;
            case CROSS_EXCHANGE_MOVE:
                new GreedyCrossExchangeMove1Explorer(vr, selector);
                new GreedyCrossExchangeMove2Explorer(vr, selector);
                new GreedyCrossExchangeMove3Explorer(vr, selector);
                new GreedyCrossExchangeMove4Explorer(vr, selector);
        }
    }

    public void search(int maxIter, int maxTime, boolean intensiveSearch) {
        int iter = 0;
        long startTime = System.currentTimeMillis();
        VRPSolutionValue bestSolution = null;
        while (iter < maxIter && System.currentTimeMillis() - startTime < maxTime) {
            iter++;
            LexMultiValues oldValues = objectiveFunc.values();
            System.out.println("iter " + iter + " :: objectiveValues = " + oldValues);
            IVRPMove bestMove = selector.selectFirstImprovementMove();
            if (bestMove != null) {
                bestMove.move();
                LexMultiValues newValues = objectiveFunc.values();
                LexMultiValues deltaValues = newValues.minus(oldValues);
                if (objectiveFunc.compare(deltaValues, bestMove.evaluation()) != 0) {
                    System.out.println(name() + ":: EXCEPTION deltaValues = " + deltaValues + " - moveValues = " + bestMove.evaluation());
                    System.exit(-1);
                }
            } else {
                if (intensiveSearch) {
                    if (bestSolution == null || objectiveFunc.compare(oldValues, bestSolution.getSolutionValues()) < 0) {
                        bestSolution = new VRPSolutionValue(vr, oldValues);
                    }
                    reset();
                } else {
                    break;
                }
            }
        }
        if (bestSolution != null) {
            if (objectiveFunc.compare(objectiveFunc.values(), bestSolution.getSolutionValues()) > 0) {
                System.out.println("assign " + bestSolution.getSolutionValues());
                bestSolution.assign();
            }
        }
        System.out.println("Best values = " + objectiveFunc.values());
    }

    private void reset() {
        System.out.println("Reset ....................................");
        ArrayList<VRPPoint> refreshPoints = new ArrayList<>();
        for (VRPRoute r : vr.getAllRoutes()) {
            if (r.getNbPoints() > 0) {
                int type = rand.nextInt(10);
                if (type == 0) {
                    for (VRPPoint p = r.getStartPoint().getNext(); p != r.getEndPoint(); p = p.getNext()) {
                        refreshPoints.add(p);
                    }
                }
            }
        }
        for (VRPPoint x : refreshPoints) {
            VRPPoint y = refreshPoints.get(rand.nextInt(refreshPoints.size()));
            if (y.getRoute() != null && y != x && !y.isEndPoint()) {
                if (vr.exploreOnePointMove(x, y)) {
                    System.out.println("Reset::" + x + " - " + y);
                    vr.propagateOnePointMove(x, y);
                }
            }
        }
//        ArrayList<VRPPoint> allPoints = vr.getAllPoints();
//        for (VRPPoint x : allPoints) {
//            if (!x.isDepot()) {
//                if (rand.nextInt(10) == 0) {
//                    VRPPoint y = allPoints.get(rand.nextInt(allPoints.size()));
//                    if (y.getRoute() != null && y != x && !y.isEndPoint()) {
//                        if (vr.exploreOnePointMove(x, y)) {
//                            System.out.println("Reset::" + x + " - " + y);
//                            vr.propagateOnePointMove(x, y);
//                        }
//                    }
//                }
//            }
//        }
    }

    public String name() {
        return "GreedySearch";
    }
}
