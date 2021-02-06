package com.socolabs.mo.components.algorithms.spatialindex.ROAD;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.GNode;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RNode {
    private Vertex representative;
    private Rnet rootOfShortcutTree;
    private HashMap<RNode, Double> mNode2ShortcutDist;

    private ArrayList<Pair<GNode, Double>> bases;
    private ArrayList<Rnet> containing;

    public RNode(Vertex rep, Rnet root) {
        representative = rep;
        rootOfShortcutTree = root;

    }
}
