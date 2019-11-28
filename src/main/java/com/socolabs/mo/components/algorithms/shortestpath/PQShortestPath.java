package com.socolabs.mo.components.algorithms.shortestpath;

import com.socolabs.mo.components.maps.graphs.Arc;
import com.socolabs.mo.components.maps.graphs.Graph;

import java.util.*;

public class PQShortestPath {
    private int N;
    private HashSet<Arc>[] A;
    private double shortestLength;
//    double[] d;
//    int[] pred;
//    boolean[] found;
//    int s,t;
//    int[] node;
//    int[] idx;

    public PQShortestPath(Graph g){
        N = g.getN();
        A = g.getA();
//        d = new double[N+1];
//        pred = new int[N+1];
//        node = new int[N+1];
//        idx = new int[N+1];
//        found = new boolean[N+1];
    }

    public double getShortestLength(){
        return shortestLength;
    }

    public double[] solve(int s, int[] t) {
        double[] res = new double[t.length];
        HashMap<Integer, Integer> mark = new HashMap<>();
        for (int i = 0; i < t.length; i++) {
            mark.put(t[i], i);
        }
        HashMap<Integer, Double> d = new HashMap<>();
        PriorityQueue<Pair> pq = new PriorityQueue<>(new Comparator<Pair>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                if (o1.d - o2.d < 0) {
                    return -1;
                } else if (o1.d - o2.d > 0) {
                    return 1;
                }
                return 0;
            }
        });
        pq.add(new Pair(s, 0));
        d.put(s, .0);
        while (!d.isEmpty()) {
            Pair p = pq.poll();
            int u = p.u;
            if (mark.containsKey(u)) {
                int idx = mark.get(u);
                mark.remove(u);
                res[idx] = p.d;
                if (mark.size() == 0) {
                    return res;
                }
            }
            if (d.get(u) == p.d) {
                for (Arc a : A[u]) {
                    int v = a.getEndPoint();
                    double w = a.getLength();
                    if ((!d.containsKey(v)) || (d.get(v) > p.d + w)) {
                        pq.add(new Pair(v, p.d + w));
                        d.put(v, p.d + w);
                    }
                }
            }
        }
        return null;
    }

    public int[] solve(int s, int t){
        shortestLength = 1e18;
        HashMap<Integer, Double> d = new HashMap<>();
        HashMap<Integer, Integer> prev = new HashMap<>();
        PriorityQueue<Pair> pq = new PriorityQueue<>(new Comparator<Pair>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                if (o1.d - o2.d < 0) {
                    return -1;
                } else if (o1.d - o2.d > 0) {
                    return 1;
                }
                return 0;
            }
        });
        pq.add(new Pair(s, 0));
        d.put(s, .0);
        while (!d.isEmpty()) {
            Pair p = pq.poll();
            int u = p.u;
            if (u == t) {
                shortestLength = p.d;
                ArrayList<Integer> L = new ArrayList<Integer>();
                int x = t;
                while(x != s){
                    L.add(x);
                    x = prev.get(x);
                }
                L.add(s);
                int[] r_path = new int[L.size()];
                for(int i = 0; i < L.size(); i++){
                    r_path[i] = L.get(L.size()-1-i);
                }
                return r_path;
            }
            if (d.get(u) == p.d) {
                for (Arc a : A[u]) {
                    int v = a.getEndPoint();
                    double w = a.getLength();
                    if ((!d.containsKey(v)) || (d.get(v) > p.d + w)) {
                        pq.add(new Pair(v, p.d + w));
                        d.put(v, p.d + w);
                        prev.put(v, u);
                    }
                }
            }
        }
        return null;
    }
}

class Pair {

    public int u;
    public double d;

    public Pair(int u, double d) {
        this.u = u;
        this.d = d;
    }
}