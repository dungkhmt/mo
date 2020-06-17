package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.moves.OrOptMove2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

public class GreedyOrOptMove2Explorer implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private ExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    private HashMap<VRPRoute, ArrayList<IVRPMove>> mRoute2RelatedMoves;
    private TreeSet<IVRPMove> orderedMoves;

    public GreedyOrOptMove2Explorer(VRPVarRoutes vr, ExplorationSelector selector) {
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
            for (VRPRoute ar : vr.getAllRoutes()) {
                if (cr != ar) {
                    for (VRPPoint x1 = cr.getStartPoint().getNext(); x1 != cr.getEndPoint(); x1 = x1.getNext()) {
                        for (VRPPoint x2 = x1.getNext(); x2 != cr.getEndPoint(); x2 = x2.getNext()) {
                            for (VRPPoint y = ar.getStartPoint(); y != ar.getEndPoint(); y = y.getNext()) {
                                if (vr.exploreOrOptMove2(x1, x2, y)) {
                                    LexMultiValues eval = objectiveF.evaluate();
                                    if (objectiveF.compare(eval, 0) < 0) {
                                        OrOptMove2 move = new OrOptMove2(vr, x1, x2, y, eval);
                                        if (!mRoute2RelatedMoves.containsKey(cr)) {
                                            mRoute2RelatedMoves.put(cr, new ArrayList<>());
                                        }
                                        if (!mRoute2RelatedMoves.containsKey(ar)) {
                                            mRoute2RelatedMoves.put(ar, new ArrayList<>());
                                        }
                                        mRoute2RelatedMoves.get(cr).add(move);
                                        mRoute2RelatedMoves.get(ar).add(move);
                                        orderedMoves.add(move);
                                    }
                                }
                            }
                        }
                    }
                    for (VRPPoint x1 = ar.getStartPoint().getNext(); x1 != ar.getEndPoint(); x1 = x1.getNext()) {
                        for (VRPPoint x2 = x1.getNext(); x2 != ar.getEndPoint(); x2 = x2.getNext()) {
                            for (VRPPoint y = cr.getStartPoint(); y != cr.getEndPoint(); y = y.getNext()) {
                                if (vr.exploreOrOptMove2(x1, x2, y)) {
                                    LexMultiValues eval = objectiveF.evaluate();
                                    if (objectiveF.compare(eval, 0) < 0) {
                                        OrOptMove2 move = new OrOptMove2(vr, x1, x2, y, eval);
                                        if (!mRoute2RelatedMoves.containsKey(cr)) {
                                            mRoute2RelatedMoves.put(cr, new ArrayList<>());
                                        }
                                        if (!mRoute2RelatedMoves.containsKey(ar)) {
                                            mRoute2RelatedMoves.put(ar, new ArrayList<>());
                                        }
                                        mRoute2RelatedMoves.get(cr).add(move);
                                        mRoute2RelatedMoves.get(ar).add(move);
                                        orderedMoves.add(move);
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
        return "GreedyOrOptMove2Explorer";
    }
}
