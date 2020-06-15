package com.socolabs.mo.vrplib.entities;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.utils.CBLSVRP;

import java.util.ArrayList;

public class LexMultiFunctions {

    public final static int MAXIMIZE = -1;
    public final static int MINIMIZE = 1;

    private ArrayList<IVRPFunction> functions;
    private ArrayList<Integer> coefs;

    public LexMultiFunctions() {
        functions = new ArrayList<>();
        coefs = new ArrayList<>();
    }

    public void add(IVRPFunction f, int coef) {
        functions.add(f);
        this.coefs.add(coef);
    }

    public LexMultiValues evaluate() {
        LexMultiValues values = new LexMultiValues();
        for (IVRPFunction f : functions) {
            values.add(f.getTmpValue() - f.getValue());
        }
        return values;
    }

    public int compare(LexMultiValues a, LexMultiValues b) {
        for (int i = 0; i < functions.size(); i++) {
            double va = a.get(i) * coefs.get(i);
            double vb = b.get(i) * coefs.get(i);
            if (Math.abs(va - vb) < CBLSVRP.EPS) {
                continue;
            }
            if (va < vb) {
                return -1;
            } else if (va > vb) {
                return 1;
            }
        }
        return 0;
    }

    public int compare(LexMultiValues a, double b) {
        for (int i = 0; i < functions.size(); i++) {
            double va = a.get(i) * coefs.get(i);
            double vb = b * coefs.get(i);
            if (Math.abs(va - vb) < CBLSVRP.EPS) {
                continue;
            }
            if (va < vb) {
                return -1;
            } else if (va > vb) {
                return 1;
            }
        }
        return 0;
    }
}
