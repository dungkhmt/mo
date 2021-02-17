package com.socolabs.mo.components.collectparcels.nearestsearcher;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.algorithms.nearestlocation.QuadTree;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.GNode;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Graph;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Path;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;
import com.socolabs.mo.components.collectparcels.Parcel;
import com.socolabs.mo.components.collectparcels.Route;
import com.socolabs.mo.components.collectparcels.RouteElement;
import com.socolabs.mo.components.collectparcels.Vehicle;
import com.socolabs.mo.components.movingobjects.IMovingObject;

import java.io.FileWriter;
import java.util.*;

public class GTreeSearcher implements NearestSearcher{

    private int NB_FANOUT = 1 << 2;
    private int MAX_LEAF_SIZE = 1 << 8;
    private final static int MAX_SHORTCUT = 1 << 20;

    private Graph G;

    private GNode root;
    private HashMap<Vertex, GNode> mVertex2LeafNode;
    private HashMap<Vertex, HashSet<Vertex>> mVertex2CacheVertices;

    private TreeSet<RouteElement>[] mVertex2RouteElement;

    private long totalQueryTime = 0;

    public GTreeSearcher(Graph g, int fanout, int leafSize) {
        this.NB_FANOUT = fanout;
        this.MAX_LEAF_SIZE = leafSize;
        this.G = g;
        mVertex2LeafNode = new HashMap<>();
        mVertex2CacheVertices = new HashMap<>();
        System.out.println("building tree");
        buildTree();
        System.out.println("calculating dist matrices");
        calculateDistanceMatrices();
        System.out.println("Done build G-Tree");
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
    private void initDistanceMatrices(GNode g) {
        int index = 0;
        for (Vertex v : g.getChildBorders()) {
            v.setGNodeIndex(g, index++);
        }
        long[][] distMatrix = new long[index][];
        for (GNode shortcut : g.getShortcuts()) {
            for (Vertex v : shortcut.getBorders()) {
                v.setGNodeIndex(g, index++);
            }
        }
        for (Vertex v : g.getChildBorders()) {
            distMatrix[v.getGNodeIndex(g)] = new long[index];
        }
        g.setDistMatrix(distMatrix);
        for (GNode c : g.getChildren()) {
            initDistanceMatrices(c);
        }
    }

    public void calculateDistanceMatrices() {
        System.out.println(mVertex2LeafNode.size());
        G.initDataStructure();
        mVertex2RouteElement = new TreeSet[G.size() + 1];
        for (int i = 0; i < mVertex2RouteElement.length; i++) {
            mVertex2RouteElement[i] = new TreeSet<>();
        }
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
            long[] dist = G.calculateCacheDistanceMatrices(s, cacheVertices);
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

    private long getSPDist(Vertex s, Vertex t, GNode g) {
        return g.getDist(s.getGNodeIndex(g), t.getGNodeIndex(g));
    }

    public long getShortestPathDistance(Vertex s, Vertex t) {
        GNode ns = mVertex2LeafNode.get(s);
        GNode nt = mVertex2LeafNode.get(t);
        if (ns == nt) {
            return getSPDist(s, t, ns);
        }
        GNode lca = getLCA(ns, nt);
        ArrayList<Pair<Vertex, Long>> l = new ArrayList<>();
        for (Vertex v : ns.getBorders()) {
            l.add(new Pair<>(v, getSPDist(s, v, ns)));
        }
        GNode next = ns.getParent();
        while (next != lca) {
            ArrayList<Pair<Vertex, Long>> l2 = new ArrayList<>();
            for (Vertex v : next.getBorders()) {
                long minDist = Long.MAX_VALUE;
                for (Pair<Vertex, Long> pair : l) {
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
        ArrayList<Pair<Vertex, Long>> r = new ArrayList<>();
        for (Vertex v : nt.getBorders()) {
            r.add(new Pair<>(v, getSPDist(v, t, nt)));
        }
        GNode prev = nt.getParent();
        while (prev != lca) {
            ArrayList<Pair<Vertex, Long>> r2 = new ArrayList<>();
            for (Vertex u : prev.getBorders()) {
                long minDist = Long.MAX_VALUE;
                for (Pair<Vertex, Long> pair : r) {
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
        long res = Long.MAX_VALUE;
        for (Pair<Vertex, Long> pl : l) {
            Vertex u = pl.first;
            for (Pair<Vertex, Long> pr : r) {
                Vertex v = pr.first;
                res = Math.min(res, pl.second + pr.second + getSPDist(u, v, lca));
            }
        }
        return res;
    }

    private Vertex findBorder(Vertex u, Vertex v, GNode lca, long dist) {
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
        long dist = getSPDist(u, v, lca);
        Vertex b = findBorder(u, v, lca, dist);
        shortestPathRecovery(u, b, path);
        path.add(b);
        shortestPathRecovery(b, v, path);
    }

    public Path getShortestPath(Vertex s, Vertex t) {
        GNode ns = mVertex2LeafNode.get(s);
        GNode nt = mVertex2LeafNode.get(t);
        ArrayList<Vertex> verticesPath = new ArrayList<>();
        long len = 0;
        if (ns == nt) {
            len = getSPDist(s, t, ns);
            verticesPath.add(s);
            verticesPath.add(t);
        } else {
            GNode lca = getLCA(ns, nt);
            ArrayList<Pair<Vertex, Long>> l = new ArrayList<>();
            for (Vertex v : ns.getBorders()) {
                l.add(new Pair<>(v, getSPDist(s, v, ns)));
            }
            GNode next = ns.getParent();
            HashMap<Vertex, Vertex> prevTrace = new HashMap<>();
            while (next != lca) {
                ArrayList<Pair<Vertex, Long>> l2 = new ArrayList<>();
                for (Vertex v : next.getBorders()) {
                    Vertex chosenVertex = null;
                    long minDist = Long.MAX_VALUE;
                    for (Pair<Vertex, Long> pair : l) {
                        Vertex u = pair.first;
                        if (u == v) {
                            if (pair.second < minDist) {
                                minDist = pair.second;
                                chosenVertex = null;
                            }
                        } else {
                            long d = pair.second + getSPDist(u, v, next);
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
            ArrayList<Pair<Vertex, Long>> r = new ArrayList<>();
            for (Vertex v : nt.getBorders()) {
                r.add(new Pair<>(v, getSPDist(v, t, nt)));
            }
            GNode prev = nt.getParent();
            HashMap<Vertex, Vertex> nextTrace = new HashMap<>();
            while (prev != lca) {
                ArrayList<Pair<Vertex, Long>> r2 = new ArrayList<>();
                for (Vertex u : prev.getBorders()) {
                    Vertex chosenVertex = null;
                    long minDist = Long.MAX_VALUE;
                    for (Pair<Vertex, Long> pair : r) {
                        Vertex v = pair.first;
                        if (u == v) {
                            if (minDist > pair.second) {
                                minDist = pair.second;
                                chosenVertex = null;
                            }
                        } else {
                            long d = pair.second + getSPDist(u, v, prev);
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
            len = Long.MAX_VALUE;
            for (Pair<Vertex, Long> pl : l) {
                Vertex u = pl.first;
                for (Pair<Vertex, Long> pr : r) {
                    Vertex v = pr.first;
                    long d = pl.second + pr.second + getSPDist(u, v, lca);
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

    public ArrayList<Pair<IMovingObject, Long>> kNNSearch(Parcel parcel, int k) {
//        System.out.println("kNNSearch");
//        Vertex s = parcel.getLocation();
//        GNode leaf = mVertex2LeafNode.get(s);
//        ArrayList<Pair<IMovingObject, Long>> res = new ArrayList<>();
//
//        class PQData{
//            GNode node;
//            Vertex mvVertex;
//            ArrayList<Pair<Vertex, Long>> borderDists;
//            long minDist;
//
//            public PQData(GNode node, Vertex mvVertex, ArrayList<Pair<Vertex, Long>> borderDists, long dist) {
//                this.node = node;
//                this.mvVertex = mvVertex;
//                this.borderDists = borderDists;
//                this.minDist = dist;
//            }
//        }
//
//        PriorityQueue<PQData> pq = new PriorityQueue<>(new Comparator<PQData>() {
//            @Override
//            public int compare(PQData o1, PQData o2) {
//                if (o1.minDist < o2.minDist) {
//                    return -1;
//                } else if (o1.minDist > o2.minDist) {
//                    return 1;
//                }
//                return 0;
//            }
//        });
//
//        long minDist = Long.MAX_VALUE;
//        ArrayList<Pair<Vertex, Long>> curBorderDists = new ArrayList<>();
//        for (Vertex v : leaf.getChildBorders()) {
//            long dist = getSPDist(s, v, leaf);
//            if (leaf.isMovingObjectVertex(v)) {
//                pq.add(new PQData(null, v, null, dist));
//            }
//            if (leaf.containsBorder(v)) {
//                curBorderDists.add(new Pair<>(v, dist));
//                minDist = Math.min(minDist, dist);
//            }
//        }
//
//        int cnt = 0;
//        GNode cur = leaf;
//        while (res.size() < k && (!pq.isEmpty() || cur != root)) {
//            PQData data = pq.peek();
//            if (pq.isEmpty() || (data != null && data.minDist > minDist)) {
//                GNode parent = cur.getParent();
//                for (GNode g : parent.getChildren()) {
//                    if (!g.hasCandidateRouteElement(parcel.getWeight())) {
//                        continue;
//                    }
//                    if (g != cur ) {
//                        cnt++;
//                        Pair<Long, ArrayList<Pair<Vertex, Long>>> nextData = calcBorderDists(curBorderDists, g, parent);
//                        pq.add(new PQData(g, null, nextData.second, nextData.first));
//                    }
//                }
//                Pair<Long, ArrayList<Pair<Vertex, Long>>> parentData = calcBorderDists(curBorderDists, parent, parent);
//                curBorderDists = parentData.second;
//                minDist = parentData.first;
//                cur = parent;
////                System.out.println("kNN minDist = " + minDist);
//            } else {
//                pq.poll();
////                System.out.println("kNN dataDist = " + data.minDist);
//
//                if (data.mvVertex != null) {
//
////                    for (IMovingObject mo : mVertex2MovingObjects.get(data.mvVertex)) {
////                        res.add(new Pair<>(mo, data.minDist));
////                    }
//                } else {
//                    if (!data.node.isLeaf()) {
//                        for (GNode g : data.node.getOccurrenceList()) {
//                            cnt++;
//                            Pair<Long, ArrayList<Pair<Vertex, Long>>> nextData = calcBorderDists(data.borderDists, g, data.node);
//                            pq.add(new PQData(g, null, nextData.second, nextData.first));
//                        }
//                    } else {
//                        for (Vertex v : data.node.getContainingMOVertices()) {
//                            long md = Long.MAX_VALUE;
////                            Vertex chosenVertex = null;
//                            for (Pair<Vertex, Long> pair : data.borderDists) {
////                                long old = md;
//                                md = Math.min(md, pair.second + getSPDist(pair.first, v, data.node));
////                                if (old > md) {
////                                    chosenVertex = pair.first;
////                                }
//                            }
//                            pq.add(new PQData(null, v, null, md));
////                            System.out.println("kNN calc vertex " + chosenVertex + " -> " + v + " d = " + md);
//                        }
//                    }
//                }
//            }
//        }
////        System.out.println(cnt);
        return null;
    }

    private Pair<Long, ArrayList<Pair<Vertex, Long>>> calcBorderDists(
            ArrayList<Pair<Vertex, Long>> curBorderDists, GNode next, GNode parent) {
        ArrayList<Pair<Vertex, Long>> nextBorderDists = new ArrayList<>();
        long newMinDist = Long.MAX_VALUE;
        for (Vertex v : next.getBorders()) {
            long md = Long.MAX_VALUE;
            for (Pair<Vertex, Long> pair : curBorderDists) {
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

    @Override
    public void add(RouteElement e) {
        Vertex nearestVertex = e.getLocation();
        mVertex2RouteElement[nearestVertex.getIndex()].add(e);
        GNode leaf = mVertex2LeafNode.get(nearestVertex);
//        leaf.addRouteElement(e);
//        GNode child = leaf;
//        GNode parent = leaf.getParent();
//        while (parent != null) {
//            parent.addRouteElement(e);
//            child = parent;
//            parent = child.getParent();
//        }
    }

    @Override
    public void remove(RouteElement e) {
        Vertex nearestVertex = e.getLocation();
//        mVertex2RouteElement.put(nearestVertex, e);
        mVertex2RouteElement[nearestVertex.getIndex()].remove(e);
        GNode leaf = mVertex2LeafNode.get(nearestVertex);
//        leaf.removeRouteElement(e);
//        GNode child = leaf;
//        GNode parent = leaf.getParent();
//        while (parent != null) {
//            parent.removeRouteElement(e);
//            child = parent;
//            parent = child.getParent();
//        }
    }

    @Override
    public Pair<RouteElement, Pair<Long, Long>> getNearestElement(Parcel parcel) {
//        long startMoment = System.currentTimeMillis();
//
//        Vertex s = parcel.getLocation();
//        GNode leaf = mVertex2LeafNode.get(s);
//
//        class PQData{
//            GNode node;
//            Vertex mvVertex;
//            ArrayList<Pair<Vertex, Long>> borderDists;
//            long minDist;
//
//            public PQData(GNode node, Vertex mvVertex, ArrayList<Pair<Vertex, Long>> borderDists, long dist) {
//                this.node = node;
//                this.mvVertex = mvVertex;
//                this.borderDists = borderDists;
//                this.minDist = dist;
//            }
//        }
//
//        PriorityQueue<PQData> pq = new PriorityQueue<>(new Comparator<PQData>() {
//            @Override
//            public int compare(PQData o1, PQData o2) {
//                if (o1.minDist < o2.minDist) {
//                    return -1;
//                } else if (o1.minDist > o2.minDist) {
//                    return 1;
//                }
//                return 0;
//            }
//        });
//
//        long minDist = Long.MAX_VALUE;
//        ArrayList<Pair<Vertex, Long>> curBorderDists = new ArrayList<>();
//        for (Vertex v : leaf.getChildBorders()) {
//            long dist = getSPDist(s, v, leaf);
//            if (leaf.isMovingObjectVertex(v)) {
//                pq.add(new PQData(null, v, null, dist));
//            }
//            if (leaf.containsBorder(v)) {
//                curBorderDists.add(new Pair<>(v, dist));
//                minDist = Math.min(minDist, dist);
//            }
//        }
//
//        int cnt = 0;
//        GNode cur = leaf;
//        boolean found = false;
//
//        // cmp value
//        Vehicle vehicle = new Vehicle(-1, parcel.getWeight() - 1);
//        Route route = new Route(null, vehicle, null, null, 0);
//        RouteElement elementCmp = new RouteElement(route);
//
//        while (!found && (!pq.isEmpty() || cur != root)) {
////            System.out.println("here");
//            PQData data = pq.peek();
//            if (pq.isEmpty() || (data != null && data.minDist > minDist)) {
//                GNode parent = cur.getParent();
//                for (GNode g : parent.getChildren()) {
//                    if (!g.hasCandidateRouteElement(parcel.getWeight())) {
//                        continue;
//                    }
//                    if (g != cur ) {
//                        cnt++;
//                        Pair<Long, ArrayList<Pair<Vertex, Long>>> nextData = calcBorderDists(curBorderDists, g, parent);
//                        pq.add(new PQData(g, null, nextData.second, nextData.first));
//                    }
//                }
//                Pair<Long, ArrayList<Pair<Vertex, Long>>> parentData = calcBorderDists(curBorderDists, parent, parent);
//                curBorderDists = parentData.second;
//                minDist = parentData.first;
//                cur = parent;
////                System.out.println("kNN minDist = " + minDist);
//            } else {
//                pq.poll();
////                System.out.println("kNN dataDist = " + data.minDist);
//
//                if (data.mvVertex != null) {
////                    RouteElement res = mVertex2RouteElement.get(data.mvVertex);
//                    RouteElement res = mVertex2RouteElement[data.mvVertex.getIndex()].higher(elementCmp);
//                    long prevDist = data.minDist;
//                    long nextDist = getShortestPathDistance(data.mvVertex, res.getNext().getLocation());
//                    totalQueryTime += System.currentTimeMillis() - startMoment;
//                    return new Pair<>(res, new Pair<>(prevDist, nextDist));
//                } else {
//                    if (!data.node.isLeaf()) {
//                        for (GNode g : data.node.getChildren()) {
//                            cnt++;
//                            if (!g.hasCandidateRouteElement(parcel.getWeight())) {
//                                continue;
//                            }
//                            Pair<Long, ArrayList<Pair<Vertex, Long>>> nextData = calcBorderDists(data.borderDists, g, data.node);
//                            pq.add(new PQData(g, null, nextData.second, nextData.first));
//                        }
//                    } else {
//                        Iterator<RouteElement> revItr = data.node.getElements().descendingIterator();
//                        while (revItr.hasNext()) {
//                            RouteElement e = revItr.next();
//                            if (e.getRoute().getRemainWeight() < parcel.getWeight()) {
//                                break;
//                            }
//                            Vertex v = e.getLocation();
//                            long md = Long.MAX_VALUE;
////                            Vertex chosenVertex = null;
//                            for (Pair<Vertex, Long> pair : data.borderDists) {
////                                long old = md;
//                                md = Math.min(md, pair.second + getSPDist(pair.first, v, data.node));
////                                if (old > md) {
////                                    chosenVertex = pair.first;
////                                }
//                            }
//                            pq.add(new PQData(null, v, null, md));
//                        }
//                    }
//                }
//            }
//        }
//        totalQueryTime += System.currentTimeMillis() - startMoment;
        return new Pair<>(null, new Pair<>(0L, 0L));
    }

    @Override
    public long getTotalQueryTime() {
        return totalQueryTime;
    }
}

