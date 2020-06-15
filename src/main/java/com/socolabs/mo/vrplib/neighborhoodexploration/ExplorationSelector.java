package com.socolabs.mo.vrplib.neighborhoodexploration;

import com.socolabs.mo.vrplib.core.IVRPInvariant;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.moves.IVRPMove;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class ExplorationSelector implements IVRPInvariant {

    private VRPVarRoutes vr;
    private LexMultiFunctions objectiveFunction;

    private ArrayList<INeighborhoodExploration> neighborhoodExplorations;

    private ArrayList<VRPRoute> changedRoutes;
    private ArrayList<VRPPoint> removedPoints;

    private int stt;

    private Random rand = new Random();

    public ExplorationSelector(VRPVarRoutes vr, LexMultiFunctions objectiveFunction) {
        this.vr = vr;
        this.objectiveFunction = objectiveFunction;
        vr.post(this);
        init();
    }

    private void init() {
        changedRoutes = new ArrayList<>();
        removedPoints = new ArrayList<>();
        for (VRPRoute r : vr.getAllRoutes()) {
            changedRoutes.add(r);
        }
        for (VRPPoint p : vr.getAllPoints()) {
            if (p.getRoute() == null) {
                removedPoints.add(p);
            }
        }
        neighborhoodExplorations = new ArrayList<>();
    }

    public void add(INeighborhoodExploration neighborhoodExplorer) {
        neighborhoodExplorations.add(neighborhoodExplorer);
    }

    public IVRPMove selectBestMove() {
        IVRPMove bestMove = null;
        for (INeighborhoodExploration neighborhoodExploration : neighborhoodExplorations) {
            IVRPMove move = neighborhoodExploration.getMove();
            if (bestMove != null) {
                bestMove = move;
            } else {
                int cmp = objectiveFunction.compare(move.evaluation(), bestMove.evaluation());
                if (cmp < 0) {
                    bestMove = move;
                } else if (cmp == 0 && rand.nextBoolean()) {
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    @Override
    public void explore() {

    }

    @Override
    public void propagate() {
        changedRoutes.clear();
        removedPoints.clear();
        changedRoutes.addAll(vr.getChangedRoutes());
        removedPoints.addAll(vr.getRemovedPoints());
    }

    @Override
    public HashSet<VRPPoint> getIndependentPoints() {
        return null;
    }

    @Override
    public int getStt() {
        return this.stt;
    }

    @Override
    public void setStt(int stt) {
        this.stt = stt;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public void createPoint(VRPPoint point) {
        removedPoints.add(point);
    }

    @Override
    public void removePoint(VRPPoint point) {
        removedPoints.remove(point);
    }

    @Override
    public void createRoute(VRPRoute route) {
        changedRoutes.add(route);
    }

    @Override
    public void removeRoute(VRPRoute route) {
        changedRoutes.remove(route);
    }

    @Override
    public VRPVarRoutes getVarRoutes() {
        return vr;
    }

    @Override
    public String name() {
        return "ExplorationManager";
    }
}
