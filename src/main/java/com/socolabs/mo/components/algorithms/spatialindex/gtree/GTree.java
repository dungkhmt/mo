package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.algorithms.nearestlocation.QuadTree;
import com.socolabs.mo.components.maps.GISMap;
import com.socolabs.mo.components.maps.distanceelementquery.DistanceElementQuery;
import com.socolabs.mo.components.maps.distanceelementquery.GeneralDistanceElement;
import com.socolabs.mo.components.movingobjects.IMovingObject;
import com.socolabs.mo.components.movingobjects.MovingObject;
import localsearch.domainspecific.vehiclerouting.vrp.utils.DateTimeUtils;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class GTree {

    private int NB_FANOUT = 1 << 2;
    private int MAX_LEAF_SIZE = 1 << 8;
    private final static int MAX_SHORTCUT = 1 << 20;

    private Graph G;

    private GNode root;
    private HashMap<Vertex, GNode> mVertex2LeafNode;
    private HashMap<Vertex, HashSet<Vertex>> mVertex2CacheVertices;

    private HashMap<Vertex, HashSet<IMovingObject>> mVertex2MovingObjects;
    private QuadTree quadTree;


    public GTree(Graph g, int fanout, int leafSize) {
        this.NB_FANOUT = fanout;
        this.MAX_LEAF_SIZE = leafSize;
        this.G = g;
        mVertex2LeafNode = new HashMap<>();
        mVertex2CacheVertices = new HashMap<>();
        mVertex2MovingObjects = new HashMap<>();
        quadTree = new QuadTree(new HashSet<>(g.getVertices()));
    }

    public void buildTree() {
        root = new GNode(0);
        Queue<Pair<Graph, GNode>> queue = new ArrayDeque<>();
        queue.add(new Pair<>(G, root));
//        int maxDepth = 0;
        while (!queue.isEmpty()) {
            Graph sg = queue.peek().first;
            GNode node = queue.peek().second;
//            maxDepth = Math.max(maxDepth, node.getDepth());
            queue.poll();
            if (sg.size() <= MAX_LEAF_SIZE) {
                node.setLeaf(true);
                node.setChildBorders(new HashSet<>(sg.getVertices()));
                for (Vertex v : sg.getVertices()) {
                    mVertex2LeafNode.put(v, node);
//                    if (!mVertex2CacheVertices.containsKey(v)) {
//                        mVertex2CacheVertices.put(v, new HashSet<>());
//                    }
//                    mVertex2CacheVertices.get(v).addAll(sg.getVertices());
                }
                continue;
            }
            ArrayList<Graph> subGraphs = graphPartitioning(sg);
            ArrayList<GNode> children = new ArrayList<>();
            HashSet<Vertex> borders = new HashSet<>();

            for (Graph g : subGraphs) {
                GNode c = new GNode(node.getDepth() + 1);
                c.setParent(node);
                children.add(c);
                queue.add(new Pair<>(g, c));
                for (Vertex v : g.getVertices()) {
                    if (g.getEdgesOfVertex(v).size() < G.getEdgesOfVertex(v).size() ||
                        g.getInEdgesOfVertex(v).size() < G.getInEdgesOfVertex(v).size()) {
                        borders.add(v);
                        c.addBorder(v);
                    }
                }
            }
            node.setChildren(children);
            node.setChildBorders(borders);
//            System.out.println("nb borders = " + borders.size());
//            for (Vertex v : borders) {
//                if (!mVertex2CacheVertices.containsKey(v)) {
//                    mVertex2CacheVertices.put(v, new HashSet<>());
//                }
//                mVertex2CacheVertices.get(v).addAll(borders);
//            }
        }

//        System.out.println("max depth = " + maxDepth);
//        System.exit(0);
    }

    public void buildShortCut() {
        HashSet<GNode> mark = new HashSet<>();

        class PQData {
            GNode x;
            GNode y;
            int dist;
            int mem;

            public PQData(GNode x, GNode y, int dist, int mem) {
                this.x = x;
                this.y = y;
                this.dist = dist;
                this.mem = mem;
            }
        }

        PriorityQueue<PQData> pq = new PriorityQueue<>(new Comparator<PQData>() {
            @Override
            public int compare(PQData o1, PQData o2) {
                long v1 = (long) o1.dist * o2.mem;
                long v2 = (long) o2.dist * o1.mem;
                if (v1 < v2) {
                    return -1;
                } else if (v1 > v2) {
                    return 1;
                }
                return 0;
            }
        });

        int sumCurShortCutMem = 0;
        for (GNode leaf : mVertex2LeafNode.values()) {
            if (!mark.contains(leaf)) {
                mark.add(leaf);
                for (Vertex u : leaf.getBorders()) {
                    for (Vertex v : G.getAdjVertices(u)) {
                         GNode other = mVertex2LeafNode.get(v);
                         if (!mark.contains(other) && leaf.getParent() != other.getParent()) {
                             GNode lca = getLCA(leaf, other);
                             int dist = 0;
                             GNode p = leaf.getParent();
                             GNode c = leaf;
                             while (p != lca) {
                                 dist += p.getBorders().size() * c.getBorders().size();
                                 c = p;
                                 p = p.getParent();
                             }
                             GNode q = other.getParent();
                             GNode d = other;
                             while (q != lca) {
                                 dist += q.getBorders().size() * d.getBorders().size();
                                 d = q;
                                 q = q.getParent();
                             }
                             dist += c.getBorders().size() * d.getBorders().size();
                             int mem = leaf.getBorders().size() * other.getBorders().size();
                             pq.add(new PQData(leaf, other, dist, mem));
                             sumCurShortCutMem += mem;
                             while (sumCurShortCutMem > MAX_SHORTCUT && !pq.isEmpty()) {
                                 PQData data = pq.poll();
                                 sumCurShortCutMem -= data.mem;
                             }
                         }
                    }
                }
            }
        }
        while (!pq.isEmpty()) {
            PQData data = pq.poll();
            GNode x = data.x;
            GNode y = data.y;
            x.addShortcutNode(y);
            y.addShortcutNode(x);
//            for (Vertex u : x.getBorders()) {
//                for (Vertex v : y.getBorders()) {
//                    mVertex2CacheVertices.get(u).add(v);
//                    mVertex2CacheVertices.get(v).add(u);
//                }
//            }
        }
    }

    private void initDistanceMatrices(GNode g) {
        int index = 0;
        for (Vertex v : g.getChildBorders()) {
            v.setGNodeIndex(g, index++);
        }
        double[][] distMatrix = new double[index][];
        for (GNode shortcut : g.getShortcuts()) {
            for (Vertex v : shortcut.getBorders()) {
                v.setGNodeIndex(g, index++);
            }
        }
        for (Vertex v : g.getChildBorders()) {
            distMatrix[v.getGNodeIndex(g)] = new double[index];
        }
        g.setDistMatrix(distMatrix);
        for (GNode c : g.getChildren()) {
            initDistanceMatrices(c);
        }
    }

    public void calculateDistanceMatrices() {
        System.out.println(mVertex2LeafNode.size());
        G.initDataStructure();
        initDistanceMatrices(root);
        int dem = 0;
        int cnt = 0;
        for (Vertex s : mVertex2LeafNode.keySet()) {

            HashSet<Vertex> cacheVertices = new HashSet<>();
            GNode leaf = mVertex2LeafNode.get(s);
            GNode g = leaf;
            while (g != null && g.containsChildBorder(s)) {
                cacheVertices.addAll(g.getChildBorders());
                for (GNode shortcut : g.getShortcuts()) {
                    cacheVertices.addAll(shortcut.getBorders());
                }
                g = g.getParent();
            }
            dem++;
            cnt += cacheVertices.size();
//            System.out.println(dem + " " + cnt);
            double[] dist = G.calculateCacheDistanceMatrices(s, cacheVertices);
            g = leaf;
            while (g != null && g.containsChildBorder(s)) {
                int sIndex = s.getGNodeIndex(g);
                for (Vertex v : g.getChildBorders()) {
                    int vIndex = v.getGNodeIndex(g);
                    g.setDist(sIndex, vIndex, dist[v.getIndex()]);
                }
                for (GNode shortcut : g.getShortcuts()) {
                    for (Vertex v : shortcut.getBorders()) {
                        int vIndex = v.getGNodeIndex(g);
                        g.setDist(sIndex, vIndex, dist[v.getIndex()]);
                    }
                }
                g = g.getParent();
            }
        }
//        System.exit(0);
//        for (Vertex v : mVertex2CacheVertices.keySet()) {
//            cnt += mVertex2CacheVertices.get(v).size();
//            d++;
//        }
    }

    private ArrayList<Graph> graphPartitioning(Graph g) {
        PriorityQueue<Graph> pq = new PriorityQueue<>(new Comparator<Graph>() {
            @Override
            public int compare(Graph o1, Graph o2) {
                return o2.size() - o1.size();
            }
        });
        pq.add(g);
        while (pq.size() < NB_FANOUT) {
//            System.out.println("step " + pq.size());
            Graph gr = pq.poll();
            ArrayList<Graph> subGraphs = gr.multilevelGraphPartitioning();
//            System.out.println(gr);
//            for (Graph s : subGraphs) {
//                System.out.println(s);
//            }
            pq.addAll(subGraphs);
        }
        return new ArrayList<>(pq);
    }

    public GNode getLCA(GNode u, GNode v) {
        if (u.getDepth() > v.getDepth()) {
            return getLCA(v, u);
        }
        while (u.getDepth() < v.getDepth()) {
            v = v.getParent();
        }
        while (u != v) {
            u = u.getParent();
            v = v.getParent();
        }
        return u;
    }

    private double getSPDist(Vertex s, Vertex t, GNode g) {
        return g.getDist(s.getGNodeIndex(g), t.getGNodeIndex(g));
    }

    public double getShortCutShortestPathDistance(Vertex s, Vertex t) {
        GNode ns = mVertex2LeafNode.get(s);
        GNode nt = mVertex2LeafNode.get(t);
        if (ns.hasShortCut(nt)) {
            ArrayList<Pair<Vertex, Double>> l = new ArrayList<>();
            for (Vertex v : ns.getBorders()) {
                l.add(new Pair<>(v, getSPDist(s, v, ns)));
            }
            ArrayList<Pair<Vertex, Double>> r = new ArrayList<>();
            for (Vertex v : nt.getBorders()) {
                r.add(new Pair<>(v, getSPDist(v, t, nt)));
            }
            double res = Double.MAX_VALUE;
            for (Pair<Vertex, Double> pl : l) {
                for (Pair<Vertex, Double> pr : r) {
                    res = Math.min(res, pl.second + pr.second + getSPDist(pl.first, pr.first, ns));
                }
            }
            return res;
        }
        return getShortestPathDistance(s, t);
    }

    public double getShortestPathDistance(Vertex s, Vertex t) {
        GNode ns = mVertex2LeafNode.get(s);
        GNode nt = mVertex2LeafNode.get(t);
        if (ns == nt) {
            return getSPDist(s, t, ns);
        }
        GNode lca = getLCA(ns, nt);
        ArrayList<Pair<Vertex, Double>> l = new ArrayList<>();
        for (Vertex v : ns.getBorders()) {
            l.add(new Pair<>(v, getSPDist(s, v, ns)));
        }
        GNode next = ns.getParent();
        while (next != lca) {
            ArrayList<Pair<Vertex, Double>> l2 = new ArrayList<>();
            for (Vertex v : next.getBorders()) {
                double minDist = Double.MAX_VALUE;
                for (Pair<Vertex, Double> pair : l) {
                    Vertex u = pair.first;
                    if (u == v) {
                        minDist = Math.min(minDist, pair.second);
                    } else {
                        minDist = Math.min(minDist, pair.second + getSPDist(u, v, next));
                    }
                }
                l2.add(new Pair<>(v, minDist));
            }
            l = l2;
            next = next.getParent();
        }
        ArrayList<Pair<Vertex, Double>> r = new ArrayList<>();
        for (Vertex v : nt.getBorders()) {
            r.add(new Pair<>(v, getSPDist(v, t, nt)));
        }
        GNode prev = nt.getParent();
        while (prev != lca) {
            ArrayList<Pair<Vertex, Double>> r2 = new ArrayList<>();
            for (Vertex u : prev.getBorders()) {
                double minDist = Double.MAX_VALUE;
                for (Pair<Vertex, Double> pair : r) {
                    Vertex v = pair.first;
                    if (u == v) {
                        minDist = Math.min(minDist, pair.second);
                    } else {
                        minDist = Math.min(minDist, pair.second + getSPDist(u, v, prev));
                    }
                }
                r2.add(new Pair<>(u, minDist));
            }
            r = r2;
            prev = prev.getParent();
        }
        double res = Double.MAX_VALUE;
        for (Pair<Vertex, Double> pl : l) {
            Vertex u = pl.first;
            for (Pair<Vertex, Double> pr : r) {
                Vertex v = pr.first;
                res = Math.min(res, pl.second + pr.second + getSPDist(u, v, lca));
            }
        }
        return res;
    }

    private Vertex findBorder(Vertex u, Vertex v, GNode lca, double dist) {
        while (lca != null) {
            for (Vertex b : lca.getChildBorders()) {
                if (b != u && b != v) {
                    if (Math.abs(dist - getSPDist(u, b, lca) - getSPDist(b, v, lca)) < 1e-6) {
                        return b;
                    }
                }
            }
            lca = lca.getParent();
        }
        return null;
    }

    private void shortestPathRecovery(Vertex u, Vertex v, Path path) {
        if (G.containsEdge(u, v)) {
            return;
        }
        GNode lu = mVertex2LeafNode.get(u);
        GNode lv = mVertex2LeafNode.get(v);
        GNode lca = getLCA(lu, lv);
        double dist = getSPDist(u, v, lca);
        Vertex b = findBorder(u, v, lca, dist);
        shortestPathRecovery(u, b, path);
        path.add(b);
        shortestPathRecovery(b, v, path);
    }

    public Path getShortestPath(Vertex s, Vertex t) {
        GNode ns = mVertex2LeafNode.get(s);
        GNode nt = mVertex2LeafNode.get(t);
        ArrayList<Vertex> verticesPath = new ArrayList<>();
        double len = 0;
        if (ns == nt) {
            len = getSPDist(s, t, ns);
            verticesPath.add(s);
            verticesPath.add(t);
        } else {
            GNode lca = getLCA(ns, nt);
            ArrayList<Pair<Vertex, Double>> l = new ArrayList<>();
            for (Vertex v : ns.getBorders()) {
                l.add(new Pair<>(v, getSPDist(s, v, ns)));
            }
            GNode next = ns.getParent();
            HashMap<Vertex, Vertex> prevTrace = new HashMap<>();
            while (next != lca) {
                ArrayList<Pair<Vertex, Double>> l2 = new ArrayList<>();
                for (Vertex v : next.getBorders()) {
                    Vertex chosenVertex = null;
                    double minDist = Double.MAX_VALUE;
                    for (Pair<Vertex, Double> pair : l) {
                        Vertex u = pair.first;
                        if (u == v) {
                            if (pair.second < minDist) {
                                minDist = pair.second;
                                chosenVertex = null;
                            }
                        } else {
                            double d = pair.second + getSPDist(u, v, next);
                            if (d < minDist) {
                                minDist = d;
                                chosenVertex = u;
                            }
                        }
                    }
                    l2.add(new Pair<>(v, minDist));
                    if (chosenVertex != null) {
                        prevTrace.put(v, chosenVertex);
                    }
                }
                l = l2;
                next = next.getParent();
            }
            ArrayList<Pair<Vertex, Double>> r = new ArrayList<>();
            for (Vertex v : nt.getBorders()) {
                r.add(new Pair<>(v, getSPDist(v, t, nt)));
            }
            GNode prev = nt.getParent();
            HashMap<Vertex, Vertex> nextTrace = new HashMap<>();
            while (prev != lca) {
                ArrayList<Pair<Vertex, Double>> r2 = new ArrayList<>();
                for (Vertex u : prev.getBorders()) {
                    Vertex chosenVertex = null;
                    double minDist = Double.MAX_VALUE;
                    for (Pair<Vertex, Double> pair : r) {
                        Vertex v = pair.first;
                        if (u == v) {
                            if (minDist > pair.second) {
                                minDist = pair.second;
                                chosenVertex = null;
                            }
                        } else {
                            double d = pair.second + getSPDist(u, v, prev);
                            if (minDist > d) {
                                minDist = d;
                                chosenVertex = v;
                            }
                        }
                    }
                    r2.add(new Pair<>(u, minDist));
                    if (chosenVertex != null) {
                        nextTrace.put(u, chosenVertex);
                    }
                }
                r = r2;
                prev = prev.getParent();
            }
            
            Vertex cu = null;
            Vertex cv = null;
            len = Double.MAX_VALUE;
            for (Pair<Vertex, Double> pl : l) {
                Vertex u = pl.first;
                for (Pair<Vertex, Double> pr : r) {
                    Vertex v = pr.first;
                    double d = pl.second + pr.second + getSPDist(u, v, lca);
                    if (len > d) {
                        len = d;
                        cu = u;
                        cv = v;
                    }
                }
            }

//            System.out.println("cu = " + cu + " cv = " + cv);
            while (prevTrace.containsKey(cu)) {
                verticesPath.add(cu);
                cu = prevTrace.get(cu);
            }
            verticesPath.add(cu);
            if (cu != s) {
                verticesPath.add(s);
            }
            Collections.reverse(verticesPath);
            while (nextTrace.containsKey(cv)) {
                verticesPath.add(cv);
                cv = nextTrace.get(cv);
            }
            verticesPath.add(cv);
            if (cv != t) {
                verticesPath.add(t);
            }
        }

//        System.out.println("imperfect path:");
//        for (Vertex v : verticesPath) {
//            System.out.print(v + " -> ");
//        }
//        System.out.println();
        Path path = new Path(len);
        path.add(s);
        for (int i = 1; i < verticesPath.size(); i++) {
            Vertex u = verticesPath.get(i - 1);
            Vertex v = verticesPath.get(i);
            shortestPathRecovery(u, v, path);
            path.add(v);
        }
        return path;
    }

    public void addMovingObject(IMovingObject o) {
        Vertex nearestVertex = (Vertex) quadTree.findNearestPoint(o.getLat(), o.getLng());
//        assert (nearestVertex.getId() + "" != o.getId());
        if (!mVertex2MovingObjects.containsKey(nearestVertex)) {
            mVertex2MovingObjects.put(nearestVertex, new HashSet<>());
        }
        mVertex2MovingObjects.get(nearestVertex).add(o);
        GNode leaf = mVertex2LeafNode.get(nearestVertex);
        leaf.addMovingObjectVertex(nearestVertex);
        GNode child = leaf;
        GNode parent = leaf.getParent();
        while (parent != null) {
            parent.addMovingObjectChild(child);
            child = parent;
            parent = child.getParent();
        }
    }

    public void clearMovingObjects() {
        for (Vertex v : mVertex2MovingObjects.keySet()) {
            if (mVertex2MovingObjects.get(v).size() > 0) {
                GNode leaf = mVertex2LeafNode.get(v);
                for (GNode node = leaf; node != null; node = node.getParent()) {
                    node.clearMovingObjectChildList();
                }
                mVertex2MovingObjects.get(v).clear();
            }
        }
    }

    public void removeMovingObject(IMovingObject o) {
        // to do

    }

    public ArrayList<Pair<IMovingObject, Double>> verifyKNNSearch(double lat, double lng, int k) {
        Vertex s = (Vertex) quadTree.findNearestPoint(lat, lng);
        ArrayList<Pair<IMovingObject, Double>> res = new ArrayList<>();
//        HashMap<Vertex, Double> dist = new HashMap<>();
//        dist.put(s, .0);
        double[] dist = new double[G.size() + 1];
        for (int i = 0; i < dist.length; i++) {
            dist[i] = 1e9;
        }
        dist[s.getIndex()] = 0;
        PriorityQueue<Pair<Vertex, Double>> pq = new PriorityQueue<>(new Comparator<Pair<Vertex, Double>>() {
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
        pq.clear();
        pq.add(new Pair<>(s, .0));
        while (!pq.isEmpty() && res.size() < k) {
            Vertex u = pq.peek().first;
            Double d = pq.peek().second;
            pq.poll();
//            System.out.println(lat + " " + lng + " " + u + " " + d);
            if (d > dist[u.getIndex()]) {//.get(u)) {
                continue;
            }
            if (mVertex2MovingObjects.containsKey(u)) {
                for (IMovingObject mo : mVertex2MovingObjects.get(u)) {
                    res.add(new Pair<>(mo, d));
                }
            }
            for (Edge e : G.getEdgesOfVertex(u)) {
                Vertex v = e.getEndPoint();
                double w = e.getWeight();
                if (dist[v.getIndex()] > d + w) {
                    //dist.put(v, d + w);
                    dist[v.getIndex()] = d + w;
                    pq.add(new Pair<>(v, d + w));
                }
            }
        }
        return res;
    }

    public ArrayList<Pair<IMovingObject, Double>> kNNSearch(double lat, double lng, int k) {
//        System.out.println("kNNSearch");
        Vertex s = (Vertex) quadTree.findNearestPoint(lat, lng);
        GNode leaf = mVertex2LeafNode.get(s);
        ArrayList<Pair<IMovingObject, Double>> res = new ArrayList<>();

        class PQData{
            GNode node;
            Vertex mvVertex;
            ArrayList<Pair<Vertex, Double>> borderDists;
            double minDist;

            public PQData(GNode node, Vertex mvVertex, ArrayList<Pair<Vertex, Double>> borderDists, double dist) {
                this.node = node;
                this.mvVertex = mvVertex;
                this.borderDists = borderDists;
                this.minDist = dist;
            }
        }

        PriorityQueue<PQData> pq = new PriorityQueue<>(new Comparator<PQData>() {
            @Override
            public int compare(PQData o1, PQData o2) {
                if (o1.minDist < o2.minDist) {
                    return -1;
                } else if (o1.minDist > o2.minDist) {
                    return 1;
                }
                return 0;
            }
        });

        double minDist = Double.MAX_VALUE;
        ArrayList<Pair<Vertex, Double>> curBorderDists = new ArrayList<>();
        for (Vertex v : leaf.getChildBorders()) {
            double dist = getSPDist(s, v, leaf);
            if (leaf.isMovingObjectVertex(v)) {
                pq.add(new PQData(null, v, null, dist));
            }
            if (leaf.containsBorder(v)) {
                curBorderDists.add(new Pair<>(v, dist));
                minDist = Math.min(minDist, dist);
            }
        }

        int cnt = 0;
        GNode cur = leaf;
        while (res.size() < k && (!pq.isEmpty() || cur != root)) {
            PQData data = pq.peek();
            if (pq.isEmpty() || (data != null && data.minDist > minDist)) {
                GNode parent = cur.getParent();
                for (GNode g : parent.getOccurrenceList()) {
                    if (g != cur) {
                        cnt++;
                        Pair<Double, ArrayList<Pair<Vertex, Double>>> nextData = calcBorderDists(curBorderDists, g, parent);
                        pq.add(new PQData(g, null, nextData.second, nextData.first));
                    }
                }
                Pair<Double, ArrayList<Pair<Vertex, Double>>> parentData = calcBorderDists(curBorderDists, parent, parent);
                curBorderDists = parentData.second;
                minDist = parentData.first;
                cur = parent;
//                System.out.println("kNN minDist = " + minDist);
            } else {
                pq.poll();
//                System.out.println("kNN dataDist = " + data.minDist);

                if (data.mvVertex != null) {

                    for (IMovingObject mo : mVertex2MovingObjects.get(data.mvVertex)) {
                        res.add(new Pair<>(mo, data.minDist));
                    }
                } else {
                    if (!data.node.isLeaf()) {
                        for (GNode g : data.node.getOccurrenceList()) {
                            cnt++;
                            Pair<Double, ArrayList<Pair<Vertex, Double>>> nextData = calcBorderDists(data.borderDists, g, data.node);
                            pq.add(new PQData(g, null, nextData.second, nextData.first));
                        }
                    } else {
                        for (Vertex v : data.node.getContainingMOVertices()) {
                            double md = Double.MAX_VALUE;
//                            Vertex chosenVertex = null;
                            for (Pair<Vertex, Double> pair : data.borderDists) {
//                                double old = md;
                                md = Math.min(md, pair.second + getSPDist(pair.first, v, data.node));
//                                if (old > md) {
//                                    chosenVertex = pair.first;
//                                }
                            }
                            pq.add(new PQData(null, v, null, md));
//                            System.out.println("kNN calc vertex " + chosenVertex + " -> " + v + " d = " + md);
                        }
                    }
                }
            }
        }
//        System.out.println(cnt);
        return res;
    }

    private Pair<Double, ArrayList<Pair<Vertex, Double>>> calcBorderDists(
            ArrayList<Pair<Vertex, Double>> curBorderDists, GNode next, GNode parent) {
        ArrayList<Pair<Vertex, Double>> nextBorderDists = new ArrayList<>();
        double newMinDist = Double.MAX_VALUE;
        for (Vertex v : next.getBorders()) {
            double md = Double.MAX_VALUE;
            for (Pair<Vertex, Double> pair : curBorderDists) {
                Vertex u = pair.first;
                if (u == v) {
                    md = Math.min(md, pair.second);
                } else {
                    md = Math.min(md, pair.second + getSPDist(u, v, parent));
                }
            }
            nextBorderDists.add(new Pair<>(v, md));
            newMinDist = Math.min(newMinDist, md);
        }
        return new Pair<>(newMinDist, nextBorderDists);
    }

    private double[] shortcutVertexMinDist;
    private int[] shortcutMark;
    private int shortcutNum;

    public ArrayList<Pair<IMovingObject, Double>> shortcutKNNSearch(double lat, double lng, int k) {
//        System.out.println("kNNSearch");
        Vertex s = (Vertex) quadTree.findNearestPoint(lat, lng);
        GNode leaf = mVertex2LeafNode.get(s);
        ArrayList<Pair<IMovingObject, Double>> res = new ArrayList<>();
        if (shortcutVertexMinDist == null) {
            shortcutVertexMinDist = new double[G.size()];
            shortcutMark = new int[G.size()];
            shortcutNum = 0;
        }

        class PQData{
            GNode node;
            Vertex mvVertex;
            double minDist;

            public PQData(GNode node, Vertex mvVertex, double dist) {
                this.node = node;
                this.mvVertex = mvVertex;
                this.minDist = dist;
            }
        }

        PriorityQueue<PQData> pq = new PriorityQueue<>(new Comparator<PQData>() {
            @Override
            public int compare(PQData o1, PQData o2) {
                if (Math.abs(o1.minDist - o2.minDist) < 1e-6) {
                    if (o2.mvVertex != null) {
                        return -1;
                    }
                    if (o1.mvVertex != null) {
                        return 1;
                    }
                    return o1.node.getDepth() - o2.node.getDepth();
                }
                if (o1.minDist < o2.minDist) {
                    return -1;
                } else if (o1.minDist > o2.minDist) {
                    return 1;
                }
                return 0;
            }
        });

//        HashMap<Vertex, Double> mVertex2MinDist = new HashMap<>();
        shortcutVertexMinDist[s.getIndex()] = 0;
        shortcutMark[s.getIndex()] = ++shortcutNum;
//        mVertex2MinDist.put(s, .0);
        double minDist = Double.MAX_VALUE;
        for (Vertex v : leaf.getChildBorders()) {
            double dist = getSPDist(s, v, leaf);
            if (leaf.isMovingObjectVertex(v)) {
                pq.add(new PQData(null, v, dist));
            }
            if (leaf.containsBorder(v)) {
                shortcutVertexMinDist[v.getIndex()] = dist;
                shortcutMark[v.getIndex()] = shortcutNum;
//                mVertex2MinDist.put(v, dist);
                minDist = Math.min(minDist, dist);
            }
        }
        for (GNode shortcut : leaf.getShortcuts()) {
            if (shortcut.containsMovingObject()) {
                pq.add(new PQData(shortcut,
                        null,
                        getMinDistOfNode(leaf.getBorders(), shortcut.getBorders(), leaf)));
            }
        }

        GNode cur = leaf;
        HashSet<GNode> mark = new HashSet<>();
        mark.add(leaf);
        while (res.size() < k && (!pq.isEmpty() || cur != root)) {
            PQData data = pq.peek();
            if (pq.isEmpty() || (data != null && data.minDist > minDist)) {
                GNode parent = cur.getParent();
                for (GNode g : parent.getOccurrenceList()) {
                    if (g != cur) {
                        pq.add(new PQData(g,
                                null,
                                getMinDistOfNode(cur.getBorders(), g.getBorders(), parent)));
                    }
                }
                minDist = getMinDistOfNode(cur.getBorders(), parent.getBorders(), parent);
//                System.out.println("shortcut minDist = " + minDist);
                cur = parent;
            } else {
                pq.poll();
//                System.out.println("shortcut dataDist = " + data.minDist);
                if (data.mvVertex != null) {
                    for (IMovingObject mo : mVertex2MovingObjects.get(data.mvVertex)) {
                        res.add(new Pair<>(mo, data.minDist));
                    }
                } else {
                    if (mark.contains(data.node)) {
                        continue;
                    }
//                    System.out.println(data.node);
//                    for (Vertex v : data.node.getBorders()) {
//                        System.out.println("shortcut " + v + " " + mVertex2MinDist.get(v));
//                    }

                    mark.add(data.node);
                    if (!data.node.isLeaf()) {
                        for (GNode g : data.node.getOccurrenceList()) {
                            pq.add(new PQData(g,
                                    null,
                                    getMinDistOfNode(data.node.getBorders(), g.getBorders(), data.node)));
                        }
                    } else {
                        for (GNode g : data.node.getShortcuts()) {
                            if (g.containsMovingObject()) {
                                pq.add(new PQData(g,
                                        null,
                                        getMinDistOfNode(data.node.getBorders(), g.getBorders(), data.node)));
                            }
                        }
                        for (Vertex v : data.node.getContainingMOVertices()) {
                            double md = Double.MAX_VALUE;
//                            Vertex chosenVertex = null;
                            for (Vertex u : data.node.getBorders()) {
//                                double old = md;
                                md = Math.min(md, shortcutVertexMinDist[u.getIndex()] + getSPDist(u, v, data.node));
//                                if (md < old) {
//                                    chosenVertex = u;
//                                }
                            }
                            pq.add(new PQData(null, v, md));
//                            System.out.println("shortcut calc vertex " + chosenVertex + " -> " + v + " d = " + md);

                        }
                    }
                }
            }
        }
        return res;
    }

    private double getMinDistOfNode(HashSet<Vertex> curBorders,
                                    HashSet<Vertex> nextBorders,
                                    GNode g) {
        double minDist = Double.MAX_VALUE;
        for (Vertex v : nextBorders) {
            double bestDist = Double.MAX_VALUE;
            for (Vertex u : curBorders) {
                bestDist = Math.min(bestDist, shortcutVertexMinDist[u.getIndex()] + getSPDist(u, v, g));
            }
            if (shortcutMark[v.getIndex()] != shortcutNum) {
                shortcutMark[v.getIndex()] = shortcutNum;
                shortcutVertexMinDist[v.getIndex()] = Double.MAX_VALUE;
            }
            shortcutVertexMinDist[v.getIndex()] = Math.min(shortcutVertexMinDist[v.getIndex()], bestDist);
//            if (mVertex2MinDist.containsKey(v)) {
//                mVertex2MinDist.put(v, Math.min(bestDist, mVertex2MinDist.get(v)));
//            } else {
//                mVertex2MinDist.put(v, bestDist);
//            }
            minDist = Math.min(minDist, bestDist);
        }
        return minDist;
    }

    public static void main(String[] args) throws IOException {
//        PriorityQueue<Integer> pq = new PriorityQueue<>();
//        pq.add(10);
//        pq.add(20);
//        System.out.println(pq.poll());
//        System.exit(0);
        String filename = "data\\BusHanoiCityRoad-connected.txt";
//        String filename = "data\\HaiBaTrungRoad-connected.txt";
        if (args.length > 0) {
            filename = args[0];
        }
        Scanner in = new Scanner(new File(filename));
        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Edge> edges = new ArrayList<>();
        HashMap<Integer, Vertex> mID2Vertex = new HashMap<>();
        while(true) {
            int id = in.nextInt();
            if (id == -1) break;
            double lat = in.nextDouble();
            double lng = in.nextDouble();
            Vertex v = new Vertex(id, lat, lng);
            mID2Vertex.put(id, v);
            vertices.add(v);
        }
        while(true) {
            int beginID = in.nextInt();
            if (beginID == -1) break;
            int endID = in.nextInt();
            double length = in.nextDouble();
            edges.add(new Edge(mID2Vertex.get(beginID), mID2Vertex.get(endID), length));
//            edges.add(new Edge(mID2Vertex.get(endID), mID2Vertex.get(beginID), length));
        }



        FileWriter myWriter;
        Random rand = new Random(1993);

        ArrayList<Pair<Vertex, Vertex>> queries = new ArrayList<>();
        ArrayList<Double> res1 = new ArrayList<>();
        ArrayList<Double> res2 = new ArrayList<>();
        for (int iter = 0; iter < 100000; iter++) {
            int s = rand.nextInt(vertices.size());
            int t = rand.nextInt(vertices.size());
            while (s == t) {
                s = rand.nextInt(vertices.size());
                t = rand.nextInt(vertices.size());
            }
            queries.add(new Pair<>(vertices.get(s), vertices.get(t)));
        }
        int[] FANOUTList = new int[]{4, 8, 16};
        int[] LEAFSIZEList = new int[] {32, 64, 128, 256};
        if (args.length > 1) {
            FANOUTList = new int[]{Integer.parseInt(args[1])};
            LEAFSIZEList = new int[] {Integer.parseInt(args[2])};
        }
        int bestFanout = -1;
        int bestLeafSize = -1;
        long minRunningTime = 1L << 60;
        for (int fanout : FANOUTList) {
            for (int leafSize : LEAFSIZEList) {
                Graph g = new Graph(vertices, edges);
                System.out.println("fanout = " + fanout + " leafSize = " + leafSize);
                myWriter = new FileWriter(filename + ".res", true);
                myWriter.write("fanout = " + fanout + " leafSize = " + leafSize + "\n");
                myWriter.close();

                GTree gTree = new GTree(g, fanout, leafSize);
                System.out.println("building tree");
                long startTime = System.currentTimeMillis();
                gTree.buildTree();
                long endTime = System.currentTimeMillis();
                System.out.println("building time = " + (endTime - startTime));
                myWriter = new FileWriter(filename + ".res", true);
                myWriter.write("building time = " + (endTime - startTime) + "\n");
                myWriter.close();
//        System.out.println("build shortcut");
//        gTree.buildShortCut();
//        startTime = System.currentTimeMillis();
                System.out.println("calculating dist matrices");
                gTree.calculateDistanceMatrices();
                endTime = System.currentTimeMillis();
                System.out.println("calculating time = " + (endTime - startTime));
                myWriter = new FileWriter(filename + ".res", true);
                myWriter.write("calculating time = " + (endTime - startTime) + "\n");
                myWriter.close();

                int[] occRate = new int[]{100, 200, 500, 1000};//, 500, 1000, 5000};
                int[] kPars = new int[]{1};//, 5, 10, 20, 50};//, 5, 10, 20, 50, 100};
                ArrayList<Vertex> kNNVertices = new ArrayList<>();
                for (int test = 1; test < 50000; test++) {
//            System.out.println("test " + test);
                    Vertex v = g.getVertices().get(rand.nextInt(g.getVertices().size()));
                    kNNVertices.add(v);
                }
//        HashMap<MovingObject, Vertex> mMO2Vertex = new HashMap<>();
                for (int occ : occRate) {
                    for (int k : kPars) {
                        gTree.clearMovingObjects();
                        for (Vertex v : g.getVertices()) {
                            if (rand.nextInt(occ) == 0) {
                                MovingObject o = new MovingObject(v.getId() + "", v.getLat(), v.getLng());
                                gTree.addMovingObject(o);
//                mMO2Vertex.put(o, v);

                            }
                        }
                        startTime = System.currentTimeMillis();
                        for (Vertex v : kNNVertices) {
                            ArrayList<Pair<IMovingObject, Double>> r1 = gTree.kNNSearch(v.getLat(), v.getLng(), k);
                        }
                        long t1 = System.currentTimeMillis() - startTime;
                        startTime = System.currentTimeMillis();
//                        for (Vertex v : kNNVertices) {
//                            ArrayList<Pair<IMovingObject, Double>> r2 = gTree.verifyKNNSearch(v.getLat(), v.getLng(), k);
//                        }
                        long t2 = System.currentTimeMillis() - startTime;
                        System.out.println("KNN Search::("+ "fanout = " + fanout + " leafSize = " + leafSize + ", " + occ + ", " + k + ") -> kNN time = " + t1 + " shortcut time = " + t2);
                        myWriter = new FileWriter(filename + ".res", true);
                        myWriter.write("KNN Search::(" + "fanout = " + fanout + " leafSize = " + leafSize + ", " + occ + ", " + k + ") -> kNN time = " + t1 + " shortcut time = " + t2 + "\n");
                        myWriter.close();
                    }
                }

//                int diff = 0;
//
//                startTime = System.currentTimeMillis();
//                for (Pair<Vertex, Vertex> p : queries) {
//                    Vertex s = p.first;
//                    Vertex t = p.second;
//                    Path p1 = gTree.getShortestPath(s, t);
//                }
//                long t1 = System.currentTimeMillis() - startTime;
//                startTime = System.currentTimeMillis();
//                for (Pair<Vertex, Vertex> p : queries) {
//                    Vertex s = p.first;
//                    Vertex t = p.second;
//                    Path p2 = g.getShortestPath(s, t);
////            res2.add(p2.getLength());
//                }
//                long t2 = System.currentTimeMillis() - startTime;
//                System.out.println("get path gtree = " + t1 + " ijk = " + t2);
//                myWriter = new FileWriter(filename + ".res", true);
//                myWriter.write("get path gtree = " + t1 + " ijk = " + t2 + "\n");
//                myWriter.close();
//                startTime = System.currentTimeMillis();
//                for (Pair<Vertex, Vertex> p : queries) {
//                    Vertex s = p.first;
//                    Vertex t = p.second;
//                    double p1 = gTree.getShortCutShortestPathDistance(s, t);
//                }
//                t1 = System.currentTimeMillis() - startTime;
//                startTime = System.currentTimeMillis();
//                for (Pair<Vertex, Vertex> p : queries) {
//                    Vertex s = p.first;
//                    Vertex t = p.second;
//                    double p2 = g.getShortestPathDistance(s, t);
////            res2.add(p2.getLength());
//                }
//                t2 = System.currentTimeMillis() - startTime;
////        startTime = System.currentTimeMillis();
////        for (Pair<Vertex, Vertex> p : queries) {
////            Vertex s = p.first;
////            Vertex t = p.second;
////            double p1 = gTree.getShortestPathDistance(s, t);
////        }
////        t3 = System.currentTimeMillis() - startTime;
//                System.out.println("dist gtree = " + t1 + " ijk = " + t2);
//                myWriter = new FileWriter(filename + ".res", true);
//                myWriter.write("dist gtree = " + t1 + " ijk = " + t2 + "\n");
//                myWriter.close();
//                if (t1  < minRunningTime) {
//                    bestFanout = fanout;
//                    bestLeafSize = leafSize;
//                    minRunningTime = t1;
//                }
            }
        }

//        if (args.length > 1) {
//            return;
//        }
        /*
        int bestFanout = 4;
        int bestLeafSize = 512;
        Graph g = new Graph(vertices, edges);
        System.out.println("bestFanout = " + bestFanout + " bestLeafSize = " + bestLeafSize);
        myWriter = new FileWriter(filename + ".res", true);
        myWriter.write("bestFanout = " + bestFanout + " bestLeafSize = " + bestLeafSize + "\n");
        myWriter.close();
        GTree gTree = new GTree(g, bestFanout, bestLeafSize);
        System.out.println("building tree");
        long startTime = System.currentTimeMillis();
        gTree.buildTree();
        long endTime = System.currentTimeMillis();
        System.out.println("building time = " + (endTime - startTime));
        myWriter = new FileWriter(filename + ".res", true);
        myWriter.write("building time = " + (endTime - startTime) + "\n");
        myWriter.close();
        System.out.println("build shortcut");
        gTree.buildShortCut();
        startTime = System.currentTimeMillis();
        System.out.println("calculating dist matrices");
        gTree.calculateDistanceMatrices();
        endTime = System.currentTimeMillis();
        System.out.println("calculating time = " + (endTime - startTime));
        myWriter = new FileWriter(filename + ".res", true);
        myWriter.write("calculating time = " + (endTime - startTime) + "\n");
        myWriter.close();
        int[] occRate = new int[]{100, 200, 500, 1000};//, 500, 1000, 5000};
        int[] kPars = new int[]{1, 5, 10, 20, 50};//, 5, 10, 20, 50, 100};
        ArrayList<Vertex> kNNVertices = new ArrayList<>();
        for (int test = 1; test < 100000; test++) {
//            System.out.println("test " + test);
            Vertex v = g.getVertices().get(rand.nextInt(g.getVertices().size()));
            kNNVertices.add(v);
        }
//        HashMap<MovingObject, Vertex> mMO2Vertex = new HashMap<>();
        for (int occ : occRate) {
            for (int k : kPars) {
                gTree.clearMovingObjects();
                for (Vertex v : g.getVertices()) {
                    if (rand.nextInt(occ) == 0) {
                        MovingObject o = new MovingObject(v.getId() + "", v.getLat(), v.getLng());
                        gTree.addMovingObject(o);
//                mMO2Vertex.put(o, v);

                    }
                }
                startTime = System.currentTimeMillis();
                for (Vertex v : kNNVertices) {
                    ArrayList<Pair<IMovingObject, Double>> r1 = gTree.kNNSearch(v.getLat(), v.getLng(), k);
                }
                long t1 = System.currentTimeMillis() - startTime;
                startTime = System.currentTimeMillis();
                for (Vertex v : kNNVertices) {
                    ArrayList<Pair<IMovingObject, Double>> r2 = gTree.shortcutKNNSearch(v.getLat(), v.getLng(), k);
                }
                long t2 = System.currentTimeMillis() - startTime;
                System.out.println("KNN Search::(" + occ + ", " + k + ") -> kNN time = " + t1 + " shortcut time = " + t2);
                myWriter = new FileWriter(filename + ".res", true);
                myWriter.write("KNN Search::(" + occ + ", " + k + ") -> kNN time = " + t1 + " shortcut time = " + t2 + "\n");
                myWriter.close();
            }
        }

//        myWriter.close();
//        genVNMInput();

         */
    }

    public static void genVNMInput() throws IOException {
        GISMap gismap = new GISMap();
        DistanceElementQuery input = new DistanceElementQuery();
        FileInputStream file = new FileInputStream(new File("Data_dailyopt_092019.xlsx"));

        XSSFWorkbook workbook = new XSSFWorkbook(file);

        XSSFSheet sheet = workbook.getSheet("Đơn hàng");
        HashMap<String, Integer> mNameCol2Idx = new HashMap<>();
        HashMap<Integer, Pair<Double, Double>> mLocation2LatLng = new HashMap<>();
        for (Row row : sheet) {
            if (mNameCol2Idx.size() < 2) {
                for (int idx = 0; idx < row.getHeight(); idx++) {
                    Cell cell = row.getCell(idx);
                    try {
                        if (cell.getCellType() == CellType.STRING) {
                            String nameCol = cell.getStringCellValue();
                            if (nameCol.equals("SITE_NUM")) {
                                mNameCol2Idx.put("SITE_NUM", idx);
                            } else if (nameCol.equals("latlng")) {
                                mNameCol2Idx.put("latlng", idx);
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            } else {
                Cell siteCell = row.getCell(mNameCol2Idx.get("SITE_NUM"));
                Cell latlngCell = row.getCell(mNameCol2Idx.get("latlng"));
                int siteNum = (int) siteCell.getNumericCellValue();
                String[] latlng = latlngCell.getStringCellValue().split(",");
                mLocation2LatLng.put(siteNum, new Pair<>(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1])));
            }
        }
        mLocation2LatLng.put(1000000001, new Pair<> (10.8808226,106.6339871));
        mLocation2LatLng.put(1000000002, new Pair<> (10.8428112,106.7586882));
        mLocation2LatLng.put(1000000003, new Pair<> (10.9211894,106.8611579));
        mLocation2LatLng.put(1000000004, new Pair<> (10.8401923,106.7623801));
        mLocation2LatLng.put(1000000005, new Pair<> (10.8239092,106.69073));
        mLocation2LatLng.put(1000000006, new Pair<> (10.8160676,106.6781257));
        GoogleMapsQuery GMQ = new GoogleMapsQuery();
        long departure_time = (long) DateTimeUtils.dateTime2Int("2020-08-24 08:00:00");
        GeneralDistanceElement[] elements = new GeneralDistanceElement[mLocation2LatLng.size() * mLocation2LatLng.size()];
        int i = 0;
        for (Map.Entry<Integer, Pair<Double, Double>> from : mLocation2LatLng.entrySet()) {
            for (Map.Entry<Integer, Pair<Double, Double>> to : mLocation2LatLng.entrySet()) {
                elements[i++] = new GeneralDistanceElement(
                        ""+from.getKey(),
                        from.getValue().first,
                        from.getValue().second,
                        ""+to.getKey(),
                        to.getValue().first,
                        to.getValue().second,
                        0,
                        0,
                        0
                );
            }
        }
        System.out.println("query");
        input.setElements(elements);
        gismap.calcDistanceElements(input);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter("distance_elements.json");
        gson.toJson(input, fw);
        fw.close();
        System.out.println("Done");
    }
}

//        for (int i = 1; i <= 15; i++) {
//            vertices.add(new Vertex(i));
//        }
//
//        edges.add(new Edge(vertices.get(0), vertices.get(1), 6));
//        edges.add(new Edge(vertices.get(1), vertices.get(0), 6));
//        edges.add(new Edge(vertices.get(0), vertices.get(5), 4));
//        edges.add(new Edge(vertices.get(5), vertices.get(0), 4));
//        edges.add(new Edge(vertices.get(1), vertices.get(2), 2));
//        edges.add(new Edge(vertices.get(2), vertices.get(1), 2));
//        edges.add(new Edge(vertices.get(1), vertices.get(5), 3));
//        edges.add(new Edge(vertices.get(5), vertices.get(1), 3));
//        edges.add(new Edge(vertices.get(2), vertices.get(3), 2));
//        edges.add(new Edge(vertices.get(3), vertices.get(2), 2));
//        edges.add(new Edge(vertices.get(2), vertices.get(4), 2));
//        edges.add(new Edge(vertices.get(4), vertices.get(2), 2));
//        edges.add(new Edge(vertices.get(5), vertices.get(6), 3));
//        edges.add(new Edge(vertices.get(6), vertices.get(5), 3));
//        edges.add(new Edge(vertices.get(5), vertices.get(11), 9));
//        edges.add(new Edge(vertices.get(11), vertices.get(5), 9));
//        edges.add(new Edge(vertices.get(6), vertices.get(7), 3));
//        edges.add(new Edge(vertices.get(7), vertices.get(6), 3));
//        edges.add(new Edge(vertices.get(6), vertices.get(9), 6));
//        edges.add(new Edge(vertices.get(9), vertices.get(6), 6));
//        edges.add(new Edge(vertices.get(7), vertices.get(8), 2));
//        edges.add(new Edge(vertices.get(8), vertices.get(7), 2));
//        edges.add(new Edge(vertices.get(9), vertices.get(10), 2));
//        edges.add(new Edge(vertices.get(10), vertices.get(9), 2));
//        edges.add(new Edge(vertices.get(9), vertices.get(11), 3));
//        edges.add(new Edge(vertices.get(11), vertices.get(9), 3));
//        edges.add(new Edge(vertices.get(11), vertices.get(12), 2));
//        edges.add(new Edge(vertices.get(12), vertices.get(11), 2));
//        edges.add(new Edge(vertices.get(11), vertices.get(13), 3));
//        edges.add(new Edge(vertices.get(13), vertices.get(11), 3));
//        edges.add(new Edge(vertices.get(13), vertices.get(14), 2));
//        edges.add(new Edge(vertices.get(14), vertices.get(13), 2));