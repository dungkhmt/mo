package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.movingobjects.ILocation;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Vertex extends Point {
    private int id;
    private int index;
    private HashMap<GNode, Integer> gnodeIndex;
    private HashMap<GNode, ArrayList<Pair<Vertex, Double>>> mGNode2Shortcuts;

    private GNode cacheQueryNode;
    private int cacheIndex;
    private boolean isObject;

    public Vertex(int id, double lat, double lng) {
        super(lat, lng);
        this.id = id;
        gnodeIndex = new HashMap<>();
        mGNode2Shortcuts = new HashMap<>();
    }

    public void sortShortcut() {
        for (Map.Entry<GNode, ArrayList<Pair<Vertex, Double>>> e : mGNode2Shortcuts.entrySet()) {
            Collections.sort(e.getValue(), new Comparator<Pair<Vertex, Double>>() {
                @Override
                public int compare(Pair<Vertex, Double> o1, Pair<Vertex, Double> o2) {
                    if (o1.second < o2.second) {
                        return -1;
                    } else if (o1.second > o2.second) {
                        return 1;
                    }
                    return 0;
                }
            });
        }
    }

    public void addShortcut(GNode g, Vertex v, double d) {
        if (!mGNode2Shortcuts.containsKey(g)) {
            mGNode2Shortcuts.put(g, new ArrayList<>());
        }
        mGNode2Shortcuts.get(g).add(new Pair<>(v, d));
    }

    public void setGNodeIndex(GNode g, int index) {
        gnodeIndex.put(g, index);
    }

    public Iterator<Pair<Vertex, Double>> getIterOfGNode(GNode g) {
        return mGNode2Shortcuts.get(g).iterator();
    }

    public int getGNodeIndex(GNode g) {
//        if (cacheQueryNode != g) {
//            cacheQueryNode = g;
//            cacheIndex = gnodeIndex.get(g);
//        }
//        return cacheIndex;
        return gnodeIndex.get(g);
    }

    public boolean inside(GNode g) {
        return gnodeIndex.containsKey(g);
    }

    public String toString() {
        return "" + id;
    }

    public void clear() {
        cacheIndex = 0;
        cacheQueryNode = null;
        gnodeIndex.clear();
    }
}
