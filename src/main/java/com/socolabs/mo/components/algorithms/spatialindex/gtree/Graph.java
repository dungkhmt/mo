package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import lombok.Getter;

import java.util.*;

@Getter
public class Graph {
    private ArrayList<Vertex> vertices;
    private ArrayList<Edge> edges;
    private HashMap<Vertex, ArrayList<Edge>> mVertex2OutEdges;
    private HashMap<Vertex, ArrayList<Edge>> mVertex2InEdges;
    private HashMap<Vertex, HashMap<Vertex, Edge>> mVertices2Edge;
    private HashMap<Vertex, HashMap<Vertex, Double>> cacheDistanceMatrices;

    private PriorityQueue<Pair<Vertex, Double>> pq;


    public Graph(ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
        this.vertices = new ArrayList<>(vertices);
        this.edges = new ArrayList<>(edges);
        mVertex2InEdges = new HashMap<>();
        mVertex2OutEdges = new HashMap<>();
        mVertices2Edge = new HashMap<>();
        for (Vertex v : vertices) {
            mVertex2OutEdges.put(v, new ArrayList<>());
            mVertex2InEdges.put(v, new ArrayList<>());
            mVertices2Edge.put(v, new HashMap<>());
        }
        for (Edge e : edges) {
            mVertex2InEdges.get(e.getEndPoint()).add(e);
            mVertex2OutEdges.get(e.getStartPoint()).add(e);
            try {
                if (e.getWeight() < mVertices2Edge.get(e.getStartPoint()).get(e.getEndPoint()).getWeight()) {
                    mVertices2Edge.get(e.getStartPoint()).put(e.getEndPoint(), e);
                }
            } catch (NullPointerException ex) {
                mVertices2Edge.get(e.getStartPoint()).put(e.getEndPoint(), e);
            }
        }
        pq = new PriorityQueue<>(new Comparator<Pair<Vertex, Double>>() {
            @Override
            public int compare(Pair<Vertex, Double> o1, Pair<Vertex, Double> o2) {
                if (o1.second - o2.second < 0) {
                    return -1;
                } else if (o1.second - o2.second > 0) {
                    return 1;
                }
                return o1.first.getId() - o2.first.getId();
            }
        });
        cacheDistanceMatrices = new HashMap<>();
    }

    public ArrayList<Edge> getEdgesOfVertex(Vertex v) {
        return mVertex2OutEdges.get(v);
    }

    public HashSet<Vertex> getAdjVertices(Vertex v) {
        HashSet<Vertex> adjVertices = new HashSet<>();
        for (Edge e : mVertex2OutEdges.get(v)) {
            adjVertices.add(e.getEndPoint());
        }
        for (Edge e : mVertex2InEdges.get(v)) {
            adjVertices.add(e.getStartPoint());
        }
        return adjVertices;
    }
    public ArrayList<Edge> getInEdgesOfVertex(Vertex v) {
        return mVertex2InEdges.get(v);
    }

    public boolean containsEdge(Vertex u, Vertex v) {
        return mVertices2Edge.get(u).containsKey(v);
    }

    public Edge getEdge(Vertex u, Vertex v) {
        return mVertices2Edge.get(u).get(v);
    }

    public int size() {
        return vertices.size();
    }

    public void calculateCacheDistanceMatrices(Vertex s, HashSet<Vertex> cacheVertices) {
        if (!cacheDistanceMatrices.containsKey(s)) {
            cacheDistanceMatrices.put(s, new HashMap<>());
        }
        HashMap<Vertex, Double> distElememtsMap = cacheDistanceMatrices.get(s);

//        long edgeCnt = 0;
        HashMap<Vertex, Integer> nbVisitedEdges = new HashMap<>();
        HashMap<Vertex, Double> dist = new HashMap<>();
        nbVisitedEdges.put(s, 1);
        dist.put(s, .0);
        pq.clear();
        pq.add(new Pair<>(s, .0));
        int cnt = 0;
        while (!pq.isEmpty()) {
            Vertex u = pq.peek().first;
            Double d = pq.peek().second;
            pq.poll();
            if (d > dist.get(u)) {
                continue;
            }
            if (cacheVertices.contains(u)) {
//                edgeCnt += nbVisitedEdges.get(u);
                distElememtsMap.put(u, d);
                cnt++;
                if (cnt == cacheVertices.size()) {
                    break;
                }
            }
            for (Edge e : getEdgesOfVertex(u)) {
                Vertex v = e.getEndPoint();
                double w = e.getWeight();
                if (!dist.containsKey(v) || dist.get(v) > d + w) {
                    dist.put(v, d + w);
                    pq.add(new Pair<>(v, d + w));
                    nbVisitedEdges.put(v, nbVisitedEdges.get(u) + 1);
                }
            }
        }
//        return edgeCnt;
    }

    public double getSPDist(Vertex s, Vertex t) {
        return cacheDistanceMatrices.get(s).get(t);
    }

    public double getShortestPathDistance(Vertex s, Vertex t) {
        HashMap<Vertex, Double> dist = new HashMap<>();
        dist.put(s, .0);
        pq.clear();
        pq.add(new Pair<>(s, .0));
        while (!pq.isEmpty()) {
            Vertex u = pq.peek().first;
            Double d = pq.peek().second;
            pq.poll();
            if (d > dist.get(u)) {
                continue;
            }
            if (u == t) {
                return d;
            }
            for (Edge e : getEdgesOfVertex(u)) {
                Vertex v = e.getEndPoint();
                double w = e.getWeight();
                if (!dist.containsKey(v) || dist.get(v) > d + w) {
                    dist.put(v, d + w);
                    pq.add(new Pair<>(v, d + w));
                }
            }
        }
        return Double.MAX_VALUE;
    }

    public Path getShortestPath(Vertex s, Vertex t) {
        HashMap<Vertex, Double> dist = new HashMap<>();
        HashMap<Vertex, Vertex> prev = new HashMap<>();
        dist.put(s, .0);
        pq.clear();
        pq.add(new Pair<>(s, .0));
        while (!pq.isEmpty()) {
            Vertex u = pq.peek().first;
            Double d = pq.peek().second;
            pq.poll();
            if (d > dist.get(u)) {
                continue;
            }
            if (u == t) {
                ArrayList<Vertex> verticesPath = new ArrayList<>();
                Vertex v = u;
                while (v != s) {
                    verticesPath.add(v);
                    v = prev.get(v);
                }
                verticesPath.add(v);
                Collections.reverse(verticesPath);
                return new Path(verticesPath, d);
            }
            for (Edge e : getEdgesOfVertex(u)) {
                Vertex v = e.getEndPoint();
                double w = e.getWeight();
                if (!dist.containsKey(v) || dist.get(v) > d + w) {
                    dist.put(v, d + w);
                    pq.add(new Pair<>(v, d + w));
                    prev.put(v, u);
                }
            }
        }
        System.out.println("WARNING::Don't travel from s to t");
        return null;
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
        Random rand = new Random(1993);
        while (tmpVertices.size() > 2) {
            Graph tmpGraph = new Graph(tmpVertices, tmpEdges);
            Collections.sort(tmpVertices, new Comparator<Vertex>() {
                @Override
                public int compare(Vertex o1, Vertex o2) {
                    return mVertex2SubVertices.get(o1).size() - mVertex2SubVertices.get(o2).size();
                }
            });
//            Collections.shuffle(tmpVertices, rand);
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
                        Vertex parentVertex = new Vertex(++n, 0, 0);
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
                            if ((e.getStartPoint() == compressEdge.getStartPoint() && e.getEndPoint() == compressEdge.getEndPoint()) ||
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
//        System.out.println("super graph " + size() + " " + edges.size());
//        for (Graph sg : subGraphs) {
//            System.out.println("sub graph " + sg.size() + " " + sg.edges.size());
//        }
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

//        System.out.println(g);


//        PriorityQueue<Graph> pq = new PriorityQueue<>(new Comparator<Graph>() {
//            @Override
//            public int compare(Graph o1, Graph o2) {
//                return o2.size() - o1.size();
//            }
//        });
//        pq.add(g);
//        while (pq.size() < 3) {
//            System.out.println("step " + pq.size());
//            Graph gr = pq.poll();
//            ArrayList<Graph> subGraphs = gr.multilevelGraphPartitioning();
//            System.out.println(" :: " + gr);
//            for (Graph s : subGraphs) {
//                System.out.println(s);
//            }
//            pq.addAll(subGraphs);
//        }
//        System.out.println("---------");
//        for (Graph gr : pq) {
//            System.out.println(gr);
//        }
    }


}
