package com.socolabs.mo.components.collectparcels.nearestsearcher;

import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Edge;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Graph;
import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;
import com.socolabs.mo.components.collectparcels.Parcel;
import com.socolabs.mo.components.collectparcels.Route;
import com.socolabs.mo.components.collectparcels.RouteElement;
import com.socolabs.mo.components.collectparcels.Vehicle;
import lombok.Getter;

import java.util.*;

@Getter
public class DijkstraSearcher implements NearestSearcher {

    private Graph G;
    private TreeSet<RouteElement>[] elementOnVertex;
    private PriorityQueue<Pair<Vertex, Long>> pq;

    private long totalQueryTime = 0;

    public DijkstraSearcher(Graph g) {
        G = g;
        G.initDataStructure();
        elementOnVertex = new TreeSet[G.size() + 1];
        for (int i = 0; i < elementOnVertex.length; i++) {
            elementOnVertex[i] = new TreeSet<>();
        }
        pq = new PriorityQueue<>(new Comparator<Pair<Vertex, Long>>() {
            @Override
            public int compare(Pair<Vertex, Long> o1, Pair<Vertex, Long> o2) {
                if (o1.second - o2.second < 0) {
                    return -1;
                } else if (o1.second - o2.second > 0) {
                    return 1;
                }
                return o1.first.getId() - o2.first.getId();
            }
        });
    }

    @Override
    public void add(RouteElement e) {
//        if (e.getLocation().getId() == 183892) {
//            System.out.println("add 183892");
////            Route r = null;
////            System.out.println(r.getRemainWeight());
////            System.exit(-1);
//        }
        elementOnVertex[e.getLocation().getIndex()].add(e);
    }

    @Override
    public void remove(RouteElement e) {
//        if (e.getLocation().getId() == 183892) {
//            System.out.println("remove 183892");
////            Route r = null;
////            System.out.println(r.getRemainWeight());
////            System.exit(-1);
//        }
        if (!elementOnVertex[e.getLocation().getIndex()].contains(e)) {
            System.out.println(e.getLocation() + " " + e.getParcel().getLocation());
            System.out.println(elementOnVertex[e.getLocation().getIndex()].size());
            for (RouteElement ee : elementOnVertex[e.getLocation().getIndex()]) {
                System.out.println("debug " + ee.getLocation());
            }
            System.out.println("here " + e.getPrev().getLocation());
            Route r = null;
            System.out.println(r.getRemainWeight());
            System.exit(-1);
        }
        elementOnVertex[e.getLocation().getIndex()].remove(e);

    }

    @Override
    public Pair<RouteElement, Pair<Long, Long>> getNearestElement(Parcel p) {
        long startMomment = System.currentTimeMillis();

        Vertex s = p.getLocation();
        RouteElement nearest = null;
        long bestDist = 1L << 50;
        double[] dist = new double[G.size() + 1];
        for (int i = 0; i < dist.length; i++) {
            dist[i] = 1L << 50;
        }
        dist[s.getIndex()] = 0;

        pq.clear();
        pq.add(new Pair<>(s, 0L));

        // cmp value
        Vehicle vehicle = new Vehicle(-1, p.getWeight() - 1);
        Route route = new Route(null, vehicle, null, null, 0);
        RouteElement elementCmp = new RouteElement(route);

        while (!pq.isEmpty()) {
            Vertex u = pq.peek().first;
            long d = pq.peek().second;
            pq.poll();
//            System.out.println(lat + " " + lng + " " + u + " " + d);
            if (d > bestDist) {
                break;
            }
            if (d > dist[u.getIndex()]) {//.get(u)) {
                continue;
            }
            if (!elementOnVertex[u.getIndex()].isEmpty()) {
                RouteElement chosen = elementOnVertex[u.getIndex()].higher(elementCmp);
                if (chosen != null) {
//                    System.out.println("ijk " + u + " chosen " + chosen.getParcel());
                    if (nearest == null || (nearest != null && nearest.compareTo(chosen) > 0)) {
                        nearest = chosen;
                        bestDist = d;
                    }
                }
            }
            for (Edge e : G.getEdgesOfVertex(u)) {
                Vertex v = e.getEndPoint();
                long w = e.getWeight();
                if (dist[v.getIndex()] > d + w) {
                    //dist.put(v, d + w);
                    dist[v.getIndex()] = d + w;
                    pq.add(new Pair<>(v, d + w));
                }
            }
        }
        if (nearest == null) {
            return new Pair<RouteElement, Pair<Long, Long>>(nearest, new Pair<Long, Long>(0L, 0L));
        }
        long nextDist = G.getShortestPathDistance(s, nearest.getNext().getLocation());

//        assert nearest != nearest.getRoute().getEnd();
//        long nextDist = 0;
//        try {
//            nextDist = G.getShortestPathDistance(s, nearest.getNext().getLocation());
//        } catch (Exception e) {
//            System.out.println(nearest.getNext());
//            System.exit(-1);
//        }

        totalQueryTime += System.currentTimeMillis() - startMomment;
        return new Pair<>(nearest, new Pair<>(bestDist, nextDist));
    }
}
