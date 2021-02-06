package com.socolabs.mo.components.algorithms.spatialindex.ROAD;

import com.socolabs.mo.components.algorithms.spatialindex.gtree.Graph;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

public class ROAD {

    private Graph originalGraph;
    private Rnet rootRnet;
    private HashMap<Vertex, RNode> mVertex2RNode;

    public ROAD(Graph g) {
        originalGraph = g;
        buildRnet();
    }

    private void buildRnet() {
        rootRnet = new Rnet(originalGraph, originalGraph, null);
        mVertex2RNode = new HashMap<>();
//        for (Vertex v : originalGraph.getVertices()) {
//            mVertex2RNode.put(v, new RNode())
//        }
        Queue<Rnet> queue = new ArrayDeque<>();
        queue.add(rootRnet);
        while (!queue.isEmpty()) {
            Rnet r = queue.poll();
            for (Vertex v : r.getBorders()) {
                if (!mVertex2RNode.containsKey(v)) {
                    mVertex2RNode.put(v, new RNode(v, r));
                }
            }
            queue.addAll(r.getChildren());
        }

    }
}
