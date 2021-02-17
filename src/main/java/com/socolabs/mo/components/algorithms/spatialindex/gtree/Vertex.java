package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.movingobjects.ILocation;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@Setter
public class Vertex extends Point {
    private int id;
    private int index;
    private HashMap<GNode, Integer> gnodeIndex;
//    private HashMap<GNode, ArrayList<GNodeShortcut>> mGNode2Shortcuts;
//    private HashMap<GNode, ArrayList<GNodeShortcut>> mExpandingGnode2Shortcuts;
//    private HashMap<GNode, ArrayList<GNodeShortcut>> mChildGnode2Shortcuts;

    private HashSet<GNode> visitedNodes;

    private int nbMark = 0;
    private long minDist;
    private boolean isObject;
    private boolean isAdded;

    public Vertex(int id, double lat, double lng) {
        super(lat, lng);
        this.id = id;
        gnodeIndex = new HashMap<>();
//        mGNode2Shortcuts = new HashMap<>();
//        mExpandingGnode2Shortcuts = new HashMap<>();
//        mChildGnode2Shortcuts = new HashMap<>();
        visitedNodes = new HashSet<>();
    }

//    public void sortShortcut() {
//        for (Map.Entry<GNode, ArrayList<GNodeShortcut>> e : mGNode2Shortcuts.entrySet()) {
//            Collections.sort(e.getValue());
//        }
//        for (Map.Entry<GNode, ArrayList<GNodeShortcut>> e : mExpandingGnode2Shortcuts.entrySet()) {
//            Collections.sort(e.getValue());
//        }
//        for (Map.Entry<GNode, ArrayList<GNodeShortcut>> e : mChildGnode2Shortcuts.entrySet()) {
//            Collections.sort(e.getValue());
//        }
//    }
//
//    public void addSiblingShortcut(GNode g, GNode sibling, Vertex v, long d) {
//        if (!mGNode2Shortcuts.containsKey(g)) {
//            mGNode2Shortcuts.put(g, new ArrayList<>());
//        }
//        mGNode2Shortcuts.get(g).add(new GNodeShortcut(sibling, v, d));
//    }
//
//    public void addFatherShortcut(GNode f, Vertex v, long d) {
//        if (!mExpandingGnode2Shortcuts.containsKey(f)) {
//            mExpandingGnode2Shortcuts.put(f, new ArrayList<>());
//        }
//        mExpandingGnode2Shortcuts.get(f).add(new GNodeShortcut(f, v, d));
//    }
//
//    public void addChildShortcut(GNode g, GNode child, Vertex v, long d) {
//        if (!mChildGnode2Shortcuts.containsKey(g)) {
//            mChildGnode2Shortcuts.put(g, new ArrayList<>());
//        }
//        mChildGnode2Shortcuts.get(g).add(new GNodeShortcut(child, v, d));
//    }

    public boolean updateMinDist(GNode g, int nbMark, long dist) {
        if (nbMark != this.nbMark) {
            this.nbMark = nbMark;
            minDist = 1L << 50;
            visitedNodes.clear();
            isAdded = false;
        }

        if (minDist > dist) {
            visitedNodes.clear();
            minDist = dist;
        }
        if (minDist == dist && !visitedNodes.contains(g)) {
            visitedNodes.add(g);
            return true;
        }
        return false;
    }

    public void setGNodeIndex(GNode g, int index) {
        gnodeIndex.put(g, index);
    }

//    public Iterator<GNodeShortcut> getIterOfSiblingGNode(GNode g) {
//        return mGNode2Shortcuts.get(g).iterator();
//    }
//
//    public Iterator<GNodeShortcut> getIterOfExpandingGNode(GNode g) {
//        return mExpandingGnode2Shortcuts.get(g).iterator();
//    }
//
//    public Iterator<GNodeShortcut> getIterOfChildGNode(GNode g) {
//        ArrayList<GNodeShortcut> arr = mChildGnode2Shortcuts.get(g);
//        if (arr != null) {
//            return arr.iterator();
//        } else {
//            return null;
//        }
//    }

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
        gnodeIndex.clear();
    }
}

//class GNodeShortcut implements Comparable<GNodeShortcut> {
//    GNode node;
//    Vertex v;
//    long dist;
//
//    public GNodeShortcut(GNode node, Vertex v, long dist) {
//        this.node = node;
//        this.v = v;
//        this.dist = dist;
//    }
//
//    @Override
//    public int compareTo(@NotNull GNodeShortcut o) {
//        if (dist < o.dist) {
//            return -1;
//        } else if (dist > o.dist) {
//            return 1;
//        }
//        return 0;
//    }
//}