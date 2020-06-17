package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.LexMultiValues;
import com.socolabs.mo.vrplib.moves.IVRPMove;
import com.socolabs.mo.vrplib.moves.ThreeOptMove3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

public class GreedyThreeOptMove3Explorer implements INeighborhoodExploration {

    private VRPVarRoutes vr;
    private ExplorationSelector selector;
    private LexMultiFunctions objectiveF;

    private HashMap<VRPRoute, ArrayList<IVRPMove>> mRoute2RelatedMoves;
    private TreeSet<IVRPMove> orderedMoves;

    public GreedyThreeOptMove3Explorer(VRPVarRoutes vr, ExplorationSelector selector) {
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
            for (VRPPoint x = cr.getStartPoint().getNext(); x != cr.getEndPoint(); x = x.getNext()) {
                for (VRPPoint y = x.getNext(); y != cr.getEndPoint(); y = y.getNext()) {
                    for (VRPPoint z = y.getNext(); z != cr.getEndPoint(); z = z.getNext()) {
                        if (vr.exploreThreeOptMove3(x, y, z)) {
                            LexMultiValues eval = objectiveF.evaluate();
                            if (objectiveF.compare(eval, 0) < 0) {
                                ThreeOptMove3 move = new ThreeOptMove3(vr, x, y, z, eval);
                                if (!mRoute2RelatedMoves.containsKey(cr)) {
                                    mRoute2RelatedMoves.put(cr, new ArrayList<>());
                                }
                                mRoute2RelatedMoves.get(cr).add(move);
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
        return "GreedyThreeOptMove3Explorer";
    }
}