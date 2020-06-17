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

public class GreedyOnePointMoveExplorer implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private ExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    private HashMap<VRPRoute, ArrayList<IVRPMove>> mRoute2RelatedMoves;
    private HashMap<VRPPoint, ArrayList<IVRPMove>> mPoint2RelatedMoves;
    private TreeSet<IVRPMove> orderedMoves;

    public GreedyOnePointMoveExplorer(VRPVarRoutes vr, ExplorationSelector selector) {
        this.vr = vr;
        this.selector = selector;
        this.objectiveF = selector.getObjectiveFunction();

        mRoute2RelatedMoves = new HashMap<>();
        mPoint2RelatedMoves = new HashMap<>();
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
        for (VRPPoint p : selector.getAddedPoints()) {
            ArrayList<IVRPMove> relatedMoves = mPoint2RelatedMoves.get(p);
            if (relatedMoves != null) {
                orderedMoves.removeAll(relatedMoves);
                relatedMoves.clear();
            }
        }
        for (VRPRoute cr : changedRoutes) {
            for (VRPRoute ar : vr.getAllRoutes()) {
                if (cr != ar) {
                    for (VRPPoint y = cr.getStartPoint(); y != cr.getEndPoint(); y = y.getNext()) {
                        for (VRPPoint x = ar.getStartPoint().getNext(); x != ar.getEndPoint(); x = x.getNext()) {
                            if (vr.exploreOnePointMove(x, y)) {
                                LexMultiValues eval = objectiveF.evaluate();
                                if (objectiveF.compare(eval, 0) < 0) {
                                    OnePointMove move = new OnePointMove(vr, x, y, eval);
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
                    for (VRPPoint y = ar.getStartPoint(); y != ar.getEndPoint(); y = y.getNext()) {
                        for (VRPPoint x = cr.getStartPoint().getNext(); x != cr.getEndPoint(); x = x.getNext()) {
                            if (vr.exploreOnePointMove(x, y)) {
                                LexMultiValues eval = objectiveF.evaluate();
                                if (objectiveF.compare(eval, 0) < 0) {
                                    OnePointMove move = new OnePointMove(vr, x, y, eval);
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
            for (VRPPoint x : vr.getFreePoints()) {
                for (VRPPoint y = cr.getStartPoint(); y != cr.getEndPoint(); y = y.getNext()) {
                    if (vr.exploreOnePointMove(x, y)) {
                        LexMultiValues eval = objectiveF.evaluate();
                        if (objectiveF.compare(eval, 0) < 0) {
                            OnePointMove move = new OnePointMove(vr, x, y, eval);
                            if (!mRoute2RelatedMoves.containsKey(cr)) {
                                mRoute2RelatedMoves.put(cr, new ArrayList<>());
                            }
                            mRoute2RelatedMoves.get(cr).add(move);
                            if (!mPoint2RelatedMoves.containsKey(x)) {
                                mPoint2RelatedMoves.put(x, new ArrayList<>());
                            }
                            mPoint2RelatedMoves.get(x).add(move);
                            orderedMoves.add(move);
                        }
                    }
                }
            }
        }
        for (VRPPoint x : selector.getRemovedPoints()) {
            for (VRPRoute r : vr.getAllRoutes()) {
                if (!changedRoutes.contains(r)) {
                    for (VRPPoint y = r.getStartPoint(); y != r.getEndPoint(); y = y.getNext()) {
                        if (vr.exploreOnePointMove(x, y)) {
                            LexMultiValues eval = objectiveF.evaluate();
                            if (objectiveF.compare(eval, 0) < 0) {
                                OnePointMove move = new OnePointMove(vr, x, y, eval);
                                if (!mRoute2RelatedMoves.containsKey(r)) {
                                    mRoute2RelatedMoves.put(r, new ArrayList<>());
                                }
                                mRoute2RelatedMoves.get(r).add(move);
                                if (!mPoint2RelatedMoves.containsKey(x)) {
                                    mPoint2RelatedMoves.put(x, new ArrayList<>());
                                }
                                mPoint2RelatedMoves.get(x).add(move);
                                orderedMoves.add(move);
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
        return "GreedyOnePointMoveExplorer";
    }
}
