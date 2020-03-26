package com.socolabs.mo.vrplib.core;

import com.socolabs.mo.vrplib.entities.InvariantSttCmp;
import com.socolabs.mo.vrplib.utils.CBLSVRP;
import lombok.Getter;

import java.util.*;

@Getter
public class VRPVarRoutes {
    private ArrayList<VRPPoint> allPoints;
    private ArrayList<VRPRoute> allRoutes;
    private ArrayList<IVRPInvariant> invariants;
    private ArrayList<IVRPBasicEntity> basicEntities;

    private HashMap<VRPRoute, VRPPoint> mChangedRouteToFirstTmpPoint;
    private HashMap<VRPRoute, ArrayList<VRPPoint>> mChangedRouteToRemovedPoints;
    private HashMap<VRPRoute, ArrayList<VRPPoint>> mChangedRouteToAddedPoints;
    private ArrayList<VRPPoint> removedPoints;

    private ArrayList<IVRPInvariant> dependentInvariantLst;
    private HashSet<IVRPInvariant> dependentInvariants;

    private ArrayList<IVRPInvariant> globalInvariants;
    private HashMap<VRPPoint, ArrayList<IVRPInvariant>> mPoint2LocalInvariants;

    private BitSet satisfiedConstraints;
    private ArrayList<IVRPChecker> checkers;

    private ArrayList<VRPPoint> changedPoints;

    private static InvariantSttCmp invariantCmp = new InvariantSttCmp();

    public VRPVarRoutes() {
        allRoutes = new ArrayList<>();
        allPoints = new ArrayList<>();
        invariants = new ArrayList<>();
        basicEntities = new ArrayList<>();
        mChangedRouteToFirstTmpPoint = new HashMap<>();
        mChangedRouteToRemovedPoints = new HashMap<>();
        mChangedRouteToAddedPoints = new HashMap<>();
        removedPoints = new ArrayList<>();
        dependentInvariantLst = new ArrayList<>();
        dependentInvariants = new HashSet<>();
        globalInvariants = new ArrayList<>();
        mPoint2LocalInvariants = new HashMap<>();
        satisfiedConstraints = new BitSet();
        checkers = new ArrayList<>();
        changedPoints = new ArrayList<>();
    }

    public void removePoint(VRPPoint point) {
        // to do
        for (IVRPBasicEntity entity : basicEntities) {
            entity.removePoint(point);
        }
    }

    public void removeRoute(VRPRoute route) {
        for (IVRPBasicEntity entity : basicEntities) {
            entity.removeRoute(route);
        }
    }

    public void post(VRPPoint point) {
        allPoints.add(point);
        point.setStt(allPoints.size());
        for (IVRPBasicEntity entity : basicEntities) {
            entity.addNewPoint(point);
        }
    }

    public void post(VRPRoute route) {
        allRoutes.add(route);
        route.setStt(allRoutes.size());
        for (IVRPBasicEntity entity : basicEntities) {
            entity.addNewRoute(route);
        }
    }

    public void post(IVRPBasicEntity entity) {
        basicEntities.add(entity);
    }

    public void post(IVRPInvariant invariant) {
        invariants.add(invariant);
        invariant.setStt(invariants.size());
        basicEntities.add(invariant);
        HashSet<VRPPoint> independentPoints = invariant.getIndependentPoints();
        if (independentPoints == null) {
            globalInvariants.add(invariant);
        } else {
            for (VRPPoint p : independentPoints) {
                if (!mPoint2LocalInvariants.containsKey(p)) {
                    mPoint2LocalInvariants.put(p, new ArrayList<>());
                }
                mPoint2LocalInvariants.get(p).add(invariant);
            }
        }
    }

    public void post(IVRPChecker checker) {
        checkers.add(checker);
    }

    public void addSatisfiedConstraint(IVRPConstraint constraint) {
        satisfiedConstraints.set(constraint.getStt(), true);
    }

    public Set<VRPRoute> getChangedRoutes() {
        return mChangedRouteToFirstTmpPoint.keySet();
    }

    // chèn x vào sau y
    public boolean exploreInsertPointMove(VRPPoint x, VRPPoint y) {
        // kiểm tra các điều kiện đơn giản của các checkers
        // khi chưa thay đổi các points
        VRPRoute yRoute = y.getRoute();
        if (y.getNext() == null || yRoute == null) {
            return false;
        }
        for (IVRPChecker checker : checkers) {
            if (!checker.checkInsertPointMove(x, y)) {
                return false;
            }
        }

        x.setTmpNext(y.getNext());
        y.getNext().setTmpPrev(x);
        x.setTmpPrev(y);
        y.setTmpNext(x);
        x.setTmpRoute(yRoute);

        clearTmpData();

        // map route bị thay đổi và point đầu tiên bị thay đổi
        // cân nhắc vị trí bắt đầu thay đổi là x hay y ??????
        mChangedRouteToFirstTmpPoint.put(yRoute, x);

        // map route các điểm bị removed khỏi route

        // map route các điểm được added thêm vào route
        ArrayList<VRPPoint> addedPoints = new ArrayList<>();
        addedPoints.add(x);
        mChangedRouteToAddedPoints.put(yRoute, addedPoints);

        // lưu các points bị thay đổi (bất cứ thông tin gì)
        changedPoints.add(y);
        changedPoints.add(x);
        int index = y.getIndex() + 1;
        x.setTmpIndex(index);
        VRPPoint p = x.getTmpNext();
        while (p != null) {
            index++;
            p.setTmpIndex(index);
            changedPoints.add(p);
            p = p.getTmpNext();
        }

        dependentInvariants.clear();
        dependentInvariants.addAll(globalInvariants);
        for (VRPPoint u : changedPoints) {
            if (mPoint2LocalInvariants.containsKey(u)) {
                dependentInvariants.addAll(mPoint2LocalInvariants.get(u));
            }
        }

        dependentInvariantLst.clear();
        dependentInvariantLst.addAll(dependentInvariants);
        dependentInvariantLst.sort(invariantCmp);
        for (IVRPInvariant invariant : dependentInvariantLst) {
            invariant.explore();
            if (satisfiedConstraints.get(invariant.getStt())) {
                // check lại các ràng buộc bắt buộc phải thỏa mãn
                IVRPConstraint constraint = (IVRPConstraint) invariant;
                if (Math.abs(constraint.getTmpValue() - constraint.getValue()) > CBLSVRP.EPS) {
                    return false;
                }
            }
        }
        return true;
    }

    public void propageteInsertPointMove(VRPPoint x, VRPPoint y) {
        boolean status = exploreInsertPointMove(x, y);
        if (status) {
            propagate();
        } else {
            System.out.printf("EXCEPTION::propageteInsertPointMove !!!!");
            System.exit(-1);
        }
    }

    private void clearTmpData() {
        for (VRPPoint p : changedPoints) {
            p.initTmp();
        }
        changedPoints.clear();
        mChangedRouteToFirstTmpPoint.clear();
        mChangedRouteToRemovedPoints.clear();
        mChangedRouteToAddedPoints.clear();
        removedPoints.clear();
    }

    private void propagate() {
        Collections.reverse(dependentInvariantLst);
        for (IVRPInvariant invariant : dependentInvariantLst) {
            invariant.propagate();
        }
        for (VRPPoint p : changedPoints) {
            p.propagate();
        }
        // xóa đi khi ko muốn mất thời gian verify lại các invariants
        verify();
    }

    private boolean verify() {
        for (IVRPInvariant invariant : invariants) {
            if (!invariant.verify()) {
                return false;
            }
        }
        return true;
    }
}