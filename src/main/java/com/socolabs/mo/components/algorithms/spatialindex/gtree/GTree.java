package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.maps.GISMap;
import com.socolabs.mo.components.maps.distanceelementquery.DistanceElementQuery;
import com.socolabs.mo.components.maps.distanceelementquery.GeneralDistanceElement;
import localsearch.domainspecific.vehiclerouting.vrp.utils.DateTimeUtils;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.Direction;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class GTree {

    private final static int NB_FANOUT = 1 << 2;
    private final static int MAX_LEAF_SIZE = 1 << 8;
    private final static int NB_SHORTCUT = 1 << 20;

    private Graph G;

    private GNode root;
    private HashMap<Vertex, GNode> mVertex2LeafNode;

    private HashMap<Vertex, HashSet<Vertex>> mVertex2CacheVertices;

    public GTree(Graph g) {
        this.G = g;
        mVertex2LeafNode = new HashMap<>();
        mVertex2CacheVertices = new HashMap<>();
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
                    if (!mVertex2CacheVertices.containsKey(v)) {
                        mVertex2CacheVertices.put(v, new HashSet<>());
                    }
                    mVertex2CacheVertices.get(v).addAll(sg.getVertices());
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
            for (Vertex v : borders) {
                if (!mVertex2CacheVertices.containsKey(v)) {
                    mVertex2CacheVertices.put(v, new HashSet<>());
                }
                mVertex2CacheVertices.get(v).addAll(borders);
            }
        }

//        System.out.println("max depth = " + maxDepth);
//        System.exit(0);
    }

    public void calculateDistanceMatrices() {
        for (Vertex v : mVertex2CacheVertices.keySet()) {
            G.calculateCacheDistanceMatrices(v, mVertex2CacheVertices.get(v));
        }
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

    public double getShortestPathDistance(Vertex s, Vertex t) {
        GNode ns = mVertex2LeafNode.get(s);
        GNode nt = mVertex2LeafNode.get(t);
        if (ns == nt) {
            return G.getSPDist(s, t);
        }
        GNode lca = getLCA(ns, nt);
        ArrayList<Pair<Vertex, Double>> l = new ArrayList<>();
        for (Vertex v : ns.getBorders()) {
            l.add(new Pair<>(v, G.getSPDist(s, v)));
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
                        minDist = Math.min(minDist, pair.second + G.getSPDist(u, v));
                    }
                }
                l2.add(new Pair<>(v, minDist));
            }
            l = l2;
            next = next.getParent();
        }
        ArrayList<Pair<Vertex, Double>> r = new ArrayList<>();
        for (Vertex v : nt.getBorders()) {
            r.add(new Pair<>(v, G.getSPDist(v, t)));
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
                        minDist = Math.min(minDist, pair.second + G.getSPDist(u, v));
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
                res = Math.min(res, pl.second + pr.second + G.getSPDist(u, v));
            }
        }
        return res;
    }

    private Vertex findBorder(Vertex u, Vertex v, GNode lca, double dist) {
        while (lca != null) {
            for (Vertex b : lca.getChildBorders()) {
                if (b != u && b != v) {
                    if (Math.abs(dist - G.getSPDist(u, b) - G.getSPDist(b, v)) < 1e-6) {
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
//        System.out.println(" recovery " + u + " -> " + v);
//        if (u.getId() == 8586 && v.getId() == 4039) {
//            System.out.println("DEBUG");
//            for (Edge e : G.getEdgesOfVertex(u)) {
//                System.out.println(e.getEndPoint());
//            }
//            System.out.println(" check containing edge");
//            for (Map.Entry<Vertex, Edge> me : G.getMVertices2Edge().get(u).entrySet()) {
//                System.out.println(me.getKey());
//            }
//        }
        double dist = G.getSPDist(u, v);
        GNode lu = mVertex2LeafNode.get(u);
        GNode lv = mVertex2LeafNode.get(v);
//        if (u.getId() == 13045 && v.getId() == 4672) {
//            System.out.println("check border " + u + " -> " + lu.isBorder(u) + " " + v + " -> " + lv.isBorder(v));
//            while (lu != lv) {
//                System.out.println("lu ");
//                for (Vertex border : lu.getBorders()) {
//                    System.out.print(border + "; ");
//                }
//                System.out.println("\n lv");
//                for (Vertex border : lv.getBorders()) {
//                    System.out.print(border + "; ");
//                }
//                System.out.println("");
//                lu = lu.getParent();
//                lv = lv.getParent();
//            }
//            System.out.println("lca");
//            for (Vertex border : lu.getChildBorders()) {
//                System.out.print(border + "; ");
//            }
//        }
//        lu = mVertex2LeafNode.get(u);
//        lv = mVertex2LeafNode.get(v);
        if (lu == lv) {
//            System.out.println("lu == lv");
            for (Vertex t : lu.getChildBorders()) {
                if (t != u && t != v) {
                    double d1 = G.getSPDist(u, t);
                    double d2 = G.getSPDist(t, v);
//                    if (t.getId() == 16435) {
//                        System.out.println(t + " d1 = " + d1 + " d2 = " + d2 + " dist = " + dist);
//                    }
                    if (Math.abs(dist - d1 - d2) < 1e-6) {
//                        System.out.println(" - " + u + " " + t + " " + v);
                        shortestPathRecovery(u, t, path);
                        path.add(t);
                        shortestPathRecovery(t, v, path);
                        return;
                    }
                }
            }
        }
        lu = lu.getParent();
        GNode lca = getLCA(lu, lv);
        Vertex b = findBorder(u, v, lca, dist);
//        System.out.println(" + " + u + " " + b + " " + v);
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
            len = G.getSPDist(s, t);
            verticesPath.add(s);
            verticesPath.add(t);
        } else {
            GNode lca = getLCA(ns, nt);
            ArrayList<Pair<Vertex, Double>> l = new ArrayList<>();
            for (Vertex v : ns.getBorders()) {
                l.add(new Pair<>(v, G.getSPDist(s, v)));
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
                            double d = pair.second + G.getSPDist(u, v);
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
                r.add(new Pair<>(v, G.getSPDist(v, t)));
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
                            double d = pair.second + G.getSPDist(u, v);
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
                    double d = pl.second + pr.second + G.getSPDist(u, v);
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

    public static void main(String[] args) throws IOException {
        String filename = "data\\HaiBaTrungRoad-connected.txt";
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

        Graph g = new Graph(vertices, edges);
        GTree gTree = new GTree(g);
        System.out.println("building tree");
        long startTime = System.currentTimeMillis();
        gTree.buildTree();
        long endTime = System.currentTimeMillis();
        System.out.println("building time = " + (endTime - startTime));

        startTime = System.currentTimeMillis();
        System.out.println("calculating dist matrices");
        gTree.calculateDistanceMatrices();
        endTime = System.currentTimeMillis();
        System.out.println("calculating time = " + (endTime - startTime));

        Random rand = new Random(1993);
        long t1 = 0;
        long t2 = 0;
        int diff = 0;
        ArrayList<Pair<Vertex, Vertex>> queries = new ArrayList<>();
        for (int iter = 0; iter < 50000; iter++) {
            int s = rand.nextInt(vertices.size());
            int t = rand.nextInt(vertices.size());
            while (s == t) {
                s = rand.nextInt(vertices.size());
                t = rand.nextInt(vertices.size());
            }
            queries.add(new Pair<>(vertices.get(s), vertices.get(t)));
        }
        startTime = System.currentTimeMillis();
        for (Pair<Vertex, Vertex> p : queries) {
            Vertex s = p.first;
            Vertex t = p.second;
//            System.out.println(vertices.get(s) + " " + vertices.get(t));
//            Path p2 = g.getShortestPath(vertices.get(s), vertices.get(t));
//            System.out.println("path: ");
//            for (Vertex x : p2.getVertices()) {
//                System.out.print(x + " ->");
//            }
//            System.out.println();
//            double res1 = gTree.getShortestPathDistance(vertices.get(s), vertices.get(t));
            Path p1 = gTree.getShortestPath(s, t);
//            ArrayList<Vertex> verticesPath = p1.getVertices();
//            if (vertices.get(s) != verticesPath.get(0) || vertices.get(t) != verticesPath.get(verticesPath.size() - 1)) {
//                System.out.println("ERROR:: 1");
//                System.exit(-1);
//            }
//            double recalcRes = 0;
//            for (int i = 1; i < verticesPath.size(); i++) {
//                if (!g.containsEdge(verticesPath.get(i - 1), verticesPath.get(i))) {
//                    System.out.println("ERROR:: 2");
//                    System.exit(-1);
//                }
//                recalcRes += g.getEdge(verticesPath.get(i - 1), verticesPath.get(i)).getWeight();
//            }
//            double res1 = p1.getLength();
//            if (Math.abs(recalcRes - res1) > 1e-6) {
//                System.out.println("ERROR:: 2");
//                System.exit(-1);
//            }
//            System.out.println("r1 = " + res1 + " time = " + (endTime - startTime));
//            startTime = System.currentTimeMillis();
//            double res2 = g.getShortestPathDistance(vertices.get(s), vertices.get(t));
//            endTime = System.currentTimeMillis();
//            t2 += endTime - startTime;
//            System.out.println("r2 = " + res2 + " time = " + (endTime - startTime));
//            if (Math.abs(res1 - res2) > 1e-6) {
//                diff++;
//            }
        }
        endTime = System.currentTimeMillis();
        t1 = endTime - startTime;
        startTime = System.currentTimeMillis();
        for (Pair<Vertex, Vertex> p : queries) {
            Vertex s = p.first;
            Vertex t = p.second;
            Path p2 = g.getShortestPath(s, t);
        }
        endTime = System.currentTimeMillis();
        t2 = endTime - startTime;
        System.out.println("t1 = " + t1 + " t2 = " + t2 + " diff = " + diff);
//        genVNMInput();
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