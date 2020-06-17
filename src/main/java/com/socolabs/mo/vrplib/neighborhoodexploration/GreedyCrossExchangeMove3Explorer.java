package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.CrossExchangeMove3;
import com.socolabs.mo.vrplib.moves.IVRPMove;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

public class GreedyCrossExchangeMove3Explorer implements INeighborhoodExploration {
    private VRPVarRoutes vr;
    private ExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    private HashMap<VRPRoute, ArrayList<IVRPMove>> mRoute2RelatedMoves;
    private TreeSet<IVRPMove> orderedMoves;

    public GreedyCrossExchangeMove3Explorer(VRPVarRoutes vr, ExplorationSelector selector) {
        this.vr = vr;
        this.selector = selector;
        this.objectiveF = selector.getObjectiveFunction();

        mRoute2RelatedMoves = new HashMap<>();
        orderedMoves = new TreeSet<>(new Comparator<IVRPMove>() {
            @Override
            public int compare(IVRPMove o1, IVRPMove o2) {
                return objectiveF.compare(o1.evaluation(), o2.evaluation());
            }
        });
        selector.add(this);
    }

    @Override
    public IVRPMove getMove() {
        System.out.print(name() + ":: move size = " + orderedMoves.size());
        ArrayList<VRPRoute> changedRoutes = selector.getChangedRoutes();
        for (VRPRoute r : changedRoutes) {
            ArrayList<IVRPMove> relatedMoves = mRoute2RelatedMoves.get(r);
            if (relatedMoves != null) {
                orderedMoves.removeAll(relatedMoves);
                relatedMoves.clear();
            }
        }
        for (VRPRoute cr : changedRoutes) {
            if (cr.getNbPoints() > 1) {
                for (VRPRoute ar : vr.getAllRoutes()) {
                    if (ar.getNbPoints() > 1) {
                        for (VRPPoint x1 = cr.getStartPoint().getNext(); x1.getNext() != cr.getEndPoint(); x1 = x1.getNext()) {
                            for (VRPPoint y1 = x1.getNext(); y1 != cr.getEndPoint(); y1 = y1.getNext()) {
                                for (VRPPoint x2 = ar.getStartPoint().getNext(); x2.getNext() != ar.getEndPoint(); x2 = x2.getNext()) {
                                    for (VRPPoint y2 = x2.getNext(); y2 != ar.getEndPoint(); y2 = y2.getNext()) {
                                        if (vr.exploreCrossExchangeMove3(x1, y1, x2, y2)) {
                                            LexMultiValues eval = objectiveF.evaluate();
                                            if (objectiveF.compare(eval, 0) < 0) {
                                                CrossExchangeMove3 move = new CrossExchangeMove3(vr, x1, y1, x2, y2, eval);
                                                orderedMoves.add(move);
                                                if (!mRoute2RelatedMoves.containsKey(cr)) {
                                                    mRoute2RelatedMoves.put(cr, new ArrayList<>());
                                                }
                                                if (!mRoute2RelatedMoves.containsKey(ar)) {
                                                    mRoute2RelatedMoves.put(ar, new ArrayList<>());
                                                }
                                                mRoute2RelatedMoves.get(cr).add(move);
                                                mRoute2RelatedMoves.get(ar).add(move);
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
        System.out.println(" --> new size = " + orderedMoves.size());
        if (orderedMoves.size() > 0) {
            return orderedMoves.first();
        }
        return null;
    }

    public String name() {
        return "GreedyCrossExchangeMove3Explorer";
    }
}
