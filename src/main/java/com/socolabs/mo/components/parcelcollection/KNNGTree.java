package com.socolabs.mo.components.parcelcollection;

import com.dailyopt.vrp.core.VRPPoint;
import com.dailyopt.vrp.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;

import java.util.*;

public class KNNGTree implements IKNN {

    private int K;
    private HashMap<Vertex, HashMap<Vertex, Double>> realDistMatrix;
    private HashMap<Vertex, ArrayList<Pair<Vertex, Pair<Double, Double>>>> mVertex2DistanceElements;
    private HashMap<Vertex, HashSet<VRPPoint>> mVertex2Points;

    public KNNGTree(HashMap<Vertex, HashMap<Vertex, Double>> realDistMatrix, int K) {
        this.realDistMatrix = realDistMatrix;
        this.K = K;
        mVertex2DistanceElements = new HashMap<>();
        mVertex2Points = new HashMap<>();
        for (Vertex v : realDistMatrix.keySet()) {
            ArrayList<Pair<Vertex, Pair<Double, Double>>> arr = new ArrayList<>();
            for (Map.Entry<Vertex, Double> e : realDistMatrix.get(v).entrySet()) {
                arr.add(new Pair<>(e.getKey(), new Pair<>(e.getValue(), realDistMatrix.get(e.getKey()).get(v))));
            }
            Collections.sort(arr, new Comparator<Pair<Vertex, Pair<Double, Double>>>() {
                @Override
                public int compare(Pair<Vertex, Pair<Double, Double>> o1, Pair<Vertex, Pair<Double, Double>> o2) {
                    if (o1.second.first < o2.second.first) {
                        return -1;
                    } else if (o1.second.first > o2.second.first) {
                        return 1;
                    }
                    return 0;
                }
            });
            mVertex2DistanceElements.put(v, arr);
            v.setObject(false);
        }
    }

    @Override
    public ArrayList<Pair<VRPPoint, Pair<Double, Double>>> getKNN(Vertex v) {
        ArrayList<Pair<VRPPoint, Pair<Double, Double>>> ret = new ArrayList<>();
        for (Pair<Vertex, Pair<Double, Double>> p : mVertex2DistanceElements.get(v)) {
            if (mVertex2Points.containsKey(p.first)) {
                for (VRPPoint point : mVertex2Points.get(p.first)) {
                    ret.add(new Pair<>(point, p.second));
                }
            }
            if (ret.size() >= K) {
                break;
            }
        }
        return ret;
    }

    @Override
    public void addPoint(VRPPoint p) {
        PCPoint pc = (PCPoint) p;
        if (!mVertex2Points.containsKey(pc.getV())) {
            mVertex2Points.put(pc.getV(), new HashSet<>());
        }
        mVertex2Points.get(pc.getV()).add(p);
    }

    @Override
    public void removePoint(VRPPoint p) {
        PCPoint pc = (PCPoint) p;
        mVertex2Points.get(pc.getV()).remove(p);
    }

    @Override
    public void setAccWeight(AccumulatedWeightPoints accWeight) {

    }

}
