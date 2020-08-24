package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import lombok.Getter;

import java.util.*;

@Getter
public class Graph {
    private ArrayList<Vertex> vertices;
    private ArrayList<Edge> edges;
    private HashMap<Vertex, ArrayList<Edge>> mVertex2OutEdges;
    private HashMap<Vertex, ArrayList<Edge>> mVertex2InEdges;

    public Graph(ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
        this.vertices = new ArrayList<>(vertices);
        this.edges = new ArrayList<>(edges);
        mVertex2InEdges = new HashMap<>();
        mVertex2OutEdges = new HashMap<>();
        for (Edge e : edges) {
            if (!mVertex2OutEdges.containsKey(e.getStartPoint())) {
                mVertex2OutEdges.put(e.getStartPoint(), new ArrayList<>());
            }
            if (!mVertex2InEdges.containsKey(e.getEndPoint())) {
                mVertex2InEdges.put(e.getEndPoint(), new ArrayList<>());
            }
            mVertex2InEdges.get(e.getEndPoint()).add(e);
            mVertex2OutEdges.get(e.getStartPoint()).add(e);
        }
    }

    public ArrayList<Edge> getEdgesOfVertex(Vertex v) {
        return mVertex2OutEdges.get(v);
    }

    public int size() {
        return vertices.size();
    }

    public ArrayList<Graph> multilevelGraphPartitioning() {
        HashMap<Vertex, ArrayList<Vertex>> mVertex2SubVertices = new HashMap<>();
        ArrayList<Vertex> tmpVertices = new ArrayList<>(vertices);
        ArrayList<Edge> tmpEdges = new ArrayList<>(edges);
        for (Vertex v : vertices) {
            mVertex2SubVertices.put(v, new ArrayList<>());
            mVertex2SubVertices.get(v).add(v);
        }
        int n = size();

        while (tmpVertices.size() < 3) {
            Graph tmpGraph = new Graph(tmpVertices, tmpEdges);
            Collections.shuffle(tmpVertices);
            HashSet<Vertex> removedVertices = new HashSet<>();
            HashMap<Vertex, Vertex> mChildVertex2ParentVertex = new HashMap<>();
            HashMap<Vertex, Edge> mCompressVertex2Edge = new HashMap<>();
            for (Vertex v : tmpVertices) {
                if (!removedVertices.contains(v)) {
                    Edge chosenEdge = null;
                    for (Edge e : tmpGraph.getEdgesOfVertex(v)) {
                        if (!removedVertices.contains(e.getEndPoint())) {
                            if (chosenEdge == null || chosenEdge.getWeight() < e.getWeight()) {
                                chosenEdge = e;
                            }
                        }
                    }
                    removedVertices.add(chosenEdge.getStartPoint());
                    removedVertices.add(chosenEdge.getEndPoint());
                    Vertex parentVertex = new Vertex(++n);
                    mChildVertex2ParentVertex.put(chosenEdge.getStartPoint(), parentVertex);
                    mChildVertex2ParentVertex.put(chosenEdge.getEndPoint(), parentVertex);
                    mCompressVertex2Edge.put(parentVertex, chosenEdge);

                    mVertex2SubVertices.put(parentVertex, new ArrayList<>(mVertex2SubVertices.get(chosenEdge.getStartPoint())));
                    mVertex2SubVertices.get(parentVertex).addAll(mVertex2SubVertices.get(chosenEdge.getEndPoint()));
                    mVertex2SubVertices.remove(chosenEdge.getStartPoint());
                    mVertex2SubVertices.remove(chosenEdge.getEndPoint());
                }
            }

            tmpVertices.clear();
            tmpEdges.clear();
            for (Vertex v : tmpGraph.getVertices()) {
                if (!removedVertices.contains(v)) {
                    tmpVertices.add(v);
                }
            }
            for (Vertex v : mCompressVertex2Edge.keySet()) {
                tmpVertices.add(v);
            }
            for (Vertex v : tmpVertices) {
                if (!removedVertices.contains(v)) {
                    HashMap<Vertex, Double> mNewVertex2Weight = new HashMap<>();
                    for (Edge e : tmpGraph.getEdgesOfVertex(v)) {
                        if (!removedVertices.contains(e.getEndPoint())) {
                            tmpEdges.add(e);
                        } else {
                            double weight = 0;
                            if (mNewVertex2Weight.containsKey(mChildVertex2ParentVertex.get(e.getEndPoint()))) {
                                weight = mNewVertex2Weight.get(mChildVertex2ParentVertex.get(e.getEndPoint()));
                            }
                            mNewVertex2Weight.put(mChildVertex2ParentVertex.get(e.getEndPoint()), weight + e.getWeight());
                        }
                    }
                    for (Map.Entry<Vertex, Double> me : mNewVertex2Weight.entrySet()) {
                        tmpEdges.add(new Edge(v, me.getKey(), me.getValue()));
                    }
                } else {
                    Edge compressEdge = mCompressVertex2Edge.get(v);
                    HashMap<Vertex, Double> mNewVertex2Weight = new HashMap<>();
                    for (Vertex rv : new Vertex[]{compressEdge.getStartPoint(), compressEdge.getEndPoint()}) {
                        for (Edge e : tmpGraph.getEdgesOfVertex(rv)) {
                            Vertex inVertex = e.getEndPoint();
                            if (removedVertices.contains(e.getEndPoint())) {
                                inVertex = mChildVertex2ParentVertex.get(e.getEndPoint());
                            }
                            double weight = 0;
                            if (mNewVertex2Weight.containsKey(inVertex)) {
                                weight = mNewVertex2Weight.get(inVertex);
                            }
                            mNewVertex2Weight.put(inVertex, weight + e.getWeight());
                        }
                    }
                    for (Map.Entry<Vertex, Double> me : mNewVertex2Weight.entrySet()) {
                        tmpEdges.add(new Edge(v, me.getKey(), me.getValue()));
                    }
                }
            }
        }

        ArrayList<Graph> subGraphs = new ArrayList<>();
        for (Vertex v : tmpVertices) {
            HashSet<Vertex> originalVertices = new HashSet<>(mVertex2SubVertices.get(v));
            ArrayList<Edge> originalEdges = new ArrayList<>();
            for (Vertex ov : originalVertices) {
                for (Edge oe : getEdgesOfVertex(ov)) {
                    if (originalVertices.contains(oe.getEndPoint())) {
                        originalEdges.add(oe);
                    }
                }
            }
            subGraphs.add(new Graph(mVertex2SubVertices.get(v), originalEdges));
        }
        return subGraphs;
    }
}
