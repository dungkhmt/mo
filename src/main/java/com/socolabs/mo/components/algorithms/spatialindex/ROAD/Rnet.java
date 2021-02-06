package com.socolabs.mo.components.algorithms.spatialindex.ROAD;

import com.socolabs.mo.components.algorithms.spatialindex.gtree.Edge;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Graph;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;

@Getter
public class Rnet {
    private int MIN_NB_VERTICES = 64;

    private HashSet<Vertex> borders;
    private ArrayList<Rnet> children;

    private ArrayList<Graph> subGraphs;
    private Graph originalGraph;
    private Graph g;

    private Rnet parent;

    public Rnet(Graph g, Graph originalGraph, Rnet parent) {
        this.g = g;
        this.originalGraph = originalGraph;
        this.parent = parent;
        borders = new HashSet<>();
        for (Vertex u : g.getVertices()) {
            for (Edge e : originalGraph.getEdgesOfVertex(u)) {
                Vertex v = e.getEndPoint();
                if (!g.containsVertex(v)) {
                    borders.add(u);
                    break;
                }
            }
        }
        partition();
        children = new ArrayList<>();
        for (Graph sg : subGraphs) {
            children.add(new Rnet(sg, originalGraph, this));
//            children.add(new Rnet(sg, originalGraph, this));
        }
    }

    private void partition() {
        subGraphs = new ArrayList<>();
        if (g.size() <= MIN_NB_VERTICES) {
            return;
        }
        ArrayList<Graph> subGraphs = g.multilevelGraphPartitioning();
        Graph g1 = subGraphs.get(0);
        Graph g2 = subGraphs.get(1);
        if (g1.size() < g2.size()) {
            Graph tmp = g1;
            g1 = g2;
            g2 = tmp;
        }
        HashSet<Vertex> commonborders = new HashSet<>();
        for (Vertex u : g1.getVertices()) {
            for (Edge e : originalGraph.getEdgesOfVertex(u)) {
                Vertex v = e.getEndPoint();
                if (g2.containsVertex(v)) {
                    commonborders.add(u);
                    break;
                }
            }
        }

        ArrayList<Edge> g2Edges = new ArrayList<>(g2.getEdges());
        ArrayList<Vertex> g2Vertices = new ArrayList<>(g2.getVertices());
        g2Vertices.addAll(commonborders);
        for (Vertex u : commonborders) {
            for (Edge e : originalGraph.getEdgesOfVertex(u)) {
                Vertex v = e.getEndPoint();
                if (g2.containsVertex(v) || commonborders.contains(v)) {
                    g2Edges.add(e);
                }
            }
            for (Edge e : originalGraph.getInEdgesOfVertex(u)) {
                Vertex v = e.getStartPoint();
                if (g2.containsVertex(v)) {
                    g2Edges.add(e);
                }
            }
        }

        subGraphs.add(g1);
        subGraphs.add(new Graph(g2Vertices, g2Edges));

    }
}
