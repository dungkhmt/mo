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
        for (Vertex v : vertices) {
            mVertex2OutEdges.put(v, new ArrayList<>());
            mVertex2InEdges.put(v, new ArrayList<>());
        }
        for (Edge e : edges) {
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
        ArrayList<Edge> tmpEdges = new ArrayList<>();
        for (Vertex v : vertices) {
            mVertex2SubVertices.put(v, new ArrayList<>());
            mVertex2SubVertices.get(v).add(v);
        }
        for (Edge e : edges) {
            tmpEdges.add(new Edge(e.getStartPoint(), e.getEndPoint(), 1));
        }
        int n = size();

        while (tmpVertices.size() > 2) {
            Graph tmpGraph = new Graph(tmpVertices, tmpEdges);
            Collections.sort(tmpVertices, new Comparator<Vertex>() {
                @Override
                public int compare(Vertex o1, Vertex o2) {
                    return mVertex2SubVertices.get(o1).size() - mVertex2SubVertices.get(o2).size();
                }
            });
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
                    if (chosenEdge != null) {
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
                if (!mCompressVertex2Edge.containsKey(v)) {
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
                            if (e == compressEdge ||
                                    (e.getStartPoint() == compressEdge.getEndPoint() && e.getEndPoint() == compressEdge.getStartPoint())) {
                                continue;
                            }
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

    public String toString() {
        String ret = "";
        for (Vertex v : vertices) {
            ret += v.getId() + ": ";
            for (Edge e : getEdgesOfVertex(v)) {
                ret += "(" + e.getEndPoint().getId() + ", " + e.getWeight() + "); ";
            }
            ret += "\n";
        }
        return ret;
    }

    public static void main(String[] args) {
        ArrayList<Vertex> vertices = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            vertices.add(new Vertex(i));
        }
        ArrayList<Edge> edges = new ArrayList<>();
        edges.add(new Edge(vertices.get(0), vertices.get(1), 6));
        edges.add(new Edge(vertices.get(1), vertices.get(0), 6));
        edges.add(new Edge(vertices.get(0), vertices.get(5), 4));
        edges.add(new Edge(vertices.get(5), vertices.get(0), 4));
        edges.add(new Edge(vertices.get(1), vertices.get(2), 2));
        edges.add(new Edge(vertices.get(2), vertices.get(1), 2));
        edges.add(new Edge(vertices.get(1), vertices.get(5), 3));
        edges.add(new Edge(vertices.get(5), vertices.get(1), 3));
        edges.add(new Edge(vertices.get(2), vertices.get(3), 2));
        edges.add(new Edge(vertices.get(3), vertices.get(2), 2));
        edges.add(new Edge(vertices.get(2), vertices.get(4), 2));
        edges.add(new Edge(vertices.get(4), vertices.get(2), 2));
        edges.add(new Edge(vertices.get(5), vertices.get(6), 3));
        edges.add(new Edge(vertices.get(6), vertices.get(5), 3));
        edges.add(new Edge(vertices.get(5), vertices.get(11), 9));
        edges.add(new Edge(vertices.get(11), vertices.get(5), 9));
        edges.add(new Edge(vertices.get(6), vertices.get(7), 3));
        edges.add(new Edge(vertices.get(7), vertices.get(6), 3));
        edges.add(new Edge(vertices.get(6), vertices.get(9), 6));
        edges.add(new Edge(vertices.get(9), vertices.get(6), 6));
        edges.add(new Edge(vertices.get(7), vertices.get(8), 2));
        edges.add(new Edge(vertices.get(8), vertices.get(7), 2));
        edges.add(new Edge(vertices.get(9), vertices.get(10), 2));
        edges.add(new Edge(vertices.get(10), vertices.get(9), 2));
        edges.add(new Edge(vertices.get(9), vertices.get(11), 3));
        edges.add(new Edge(vertices.get(11), vertices.get(9), 3));
        edges.add(new Edge(vertices.get(11), vertices.get(12), 2));
        edges.add(new Edge(vertices.get(12), vertices.get(11), 2));
        edges.add(new Edge(vertices.get(11), vertices.get(13), 3));
        edges.add(new Edge(vertices.get(13), vertices.get(11), 3));
        edges.add(new Edge(vertices.get(13), vertices.get(14), 2));
        edges.add(new Edge(vertices.get(14), vertices.get(13), 2));
        Graph g = new Graph(vertices, edges);
//        System.out.println(g);


        PriorityQueue<Graph> pq = new PriorityQueue<>(new Comparator<Graph>() {
            @Override
            public int compare(Graph o1, Graph o2) {
                return o2.size() - o1.size();
            }
        });
        pq.add(g);
        while (pq.size() < 3) {
            System.out.println("step " + pq.size());
            Graph gr = pq.poll();
            ArrayList<Graph> subGraphs = gr.multilevelGraphPartitioning();
            System.out.println(gr);
            for (Graph s : subGraphs) {
                System.out.println(s);
            }
            pq.addAll(subGraphs);
        }
        System.out.println("---------");
        for (Graph gr : pq) {
            System.out.println(gr);
        }
    }
}
