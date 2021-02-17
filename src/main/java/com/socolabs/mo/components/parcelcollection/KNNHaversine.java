package com.socolabs.mo.components.parcelcollection;

import com.dailyopt.vrp.core.VRPPoint;
import com.dailyopt.vrp.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;

import java.util.*;

public class KNNHaversine implements IKNN {

    private int K;
    private HashMap<Vertex, HashMap<Vertex, Double>> realDistMatrix;
    private HashMap<Vertex, ArrayList<Pair<Vertex, Pair<Double, Double>>>> mVertex2DistanceElements;
    private HashMap<Vertex, HashSet<VRPPoint>> mVertex2Points;

    public KNNHaversine(HashMap<Vertex, HashMap<Vertex, Double>> realDistMatrix, int K) {
        this.realDistMatrix = realDistMatrix;
        this.K = K;
        mVertex2DistanceElements = new HashMap<>();
        mVertex2Points = new HashMap<>();
        for (Vertex v : realDistMatrix.keySet()) {
            ArrayList<Pair<Vertex, Pair<Double, Double>>> arr = new ArrayList<>();
            for (Map.Entry<Vertex, Double> e : realDistMatrix.get(v).entrySet()) {
                double d = computeDistanceHaversine(v.getLat(), v.getLng(), e.getKey().getLat(), e.getKey().getLng());
                arr.add(new Pair<>(e.getKey(), new Pair<>(d, d)));
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

    public static double computeDistanceHaversine(double lat1, double long1,
                                           double lat2, double long2) {
        double SCALE = 1000;
        double PI = 3.14159265;
        long1 = long1 * 1.0 / SCALE;
        lat1 = lat1 * 1.0 / SCALE;
        long2 = long2 * 1.0 / SCALE;
        lat2 = lat2 * 1.0 / SCALE;

        double dlat1 = lat1 * PI / 180;
        double dlong1 = long1 * PI / 180;
        double dlat2 = lat2 * PI / 180;
        double dlong2 = long2 * PI / 180;

        double dlong = dlong2 - dlong1;
        double dlat = dlat2 - dlat1;

        double aHarv = Math.pow(Math.sin(dlat / 2), 2.0) + Math.cos(dlat1)
                * Math.cos(dlat2) * Math.pow(Math.sin(dlong / 2), 2.0);
        double cHarv = 2 * Math.atan2(Math.sqrt(aHarv), Math.sqrt(1.0 - aHarv));

        double R = 6378.137; // in km

        return R * cHarv * SCALE; // in m

    }
}
