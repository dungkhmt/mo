package com.socolabs.mo.vrplib.checkers;

import com.socolabs.mo.vrplib.core.IcheckerVRP;
import com.socolabs.mo.vrplib.core.PointVRP;
import com.socolabs.mo.vrplib.core.VarRoutesVRP;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.util.ArrayList;
import java.util.HashSet;

public class CommonChecker implements IcheckerVRP {

    private VarRoutesVRP vr;
//    private ArrayList<IcheckerVRP> localCheckers;

    public CommonChecker(VarRoutesVRP vr) {
        this.vr = vr;
        vr.post(this);
//        localCheckers = new ArrayList<>();
    }

//    public void addChecker(IcheckerVRP checker) {
//        localCheckers.add(checker);
//    }

    @Override
    public boolean checkKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y) {
        if (x.size() != y.size()) {
            return false;
        }
        HashSet<PointVRP> ySet = new HashSet<>(y);
        for (int i = 0; i < x.size(); i++) {
            PointVRP px = x.get(i);
            if (px == CBLSVRP.NULL_POINT) {
                return false;
            }
            if (px.isDepot()) {
                return false;
            }
            if (ySet.contains(px)) {
                return false;
            }
            PointVRP py = y.get(i);
            if (py != CBLSVRP.NULL_POINT) {
                if (py.getRoute() == null) {
                    return false;
                }
                if (py.isEndPoint()) {
                    return false;
                }
            }
        }
//        for (IcheckerVRP checker : localCheckers) {
//            if (!checker.checkKPointsMove(x, y)) {
//                return false;
//            }
//        }
        return true;
    }

    @Override
    public void validateKPointsMove(ArrayList<PointVRP> x, ArrayList<PointVRP> y) {
        if (x.size() != y.size()) {
            System.out.println(name() + "::ERROR::checkKPointsMove -> size of x is difference from size of y!!!");
            System.exit(-1);
        }
        HashSet<PointVRP> ySet = new HashSet<>(y);
        for (int i = 0; i < x.size(); i++) {
            PointVRP px = x.get(i);
            if (px == CBLSVRP.NULL_POINT) {
                System.out.println(name() + "::ERROR::checkKPointsMove -> x[" + i + "] is null point!!!");
                System.exit(-1);
            }
            if (px.isDepot()) {
                System.out.println(name() + "::ERROR::checkKPointsMove -> x[" + i + "] is depot!!!");
                System.exit(-1);
            }
            if (ySet.contains(px)) {
                System.out.println(name() + "::ERROR::checkKPointsMove -> x[" + i + "] is in y!!!");
                System.exit(-1);
            }
            PointVRP py = y.get(i);
            if (py != CBLSVRP.NULL_POINT) {
                if (py.getRoute() == null) {
                    System.out.println(name() + "::ERROR::checkKPointsMove -> y[" + i + "] doesn't belong to any route!!!");
                    System.exit(-1);
                }
                if (py.isEndPoint()) {
                    System.out.println(name() + "::ERROR::checkKPointsMove -> y[" + i + "] is end point!!!");
                    System.exit(-1);
                }
            }
        }
//        for (IcheckerVRP checker : localCheckers) {
//            checker.validateKPointsMove(x, y);
//        }
    }

    public String name() {
        return "CommonChecker";
    }
}
