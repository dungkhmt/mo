package com.dailyopt.mo.components.algorithms.nearestlocation;

import com.dailyopt.mo.components.maps.Point;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class KDTree {
    public static int MIN_NB_POINT_PER_BLOCK = 5;

    private TreeNode root;
    private ArrayList<Point> points;
    public static LatCmp latCmp = new LatCmp();
    public static LngCmp lngCmp = new LngCmp();

    public KDTree(Collection<Point> points) {
        this.points = new ArrayList<>(points);
        initTree();

    }

    public Point findNearestPoint(double lat, double lng) {
        double minDist = 1e18;
        Point nearestPoint = null;
        PriorityQueue<Pair<TreeNode, Double>> pq = new PriorityQueue<>(new Comparator<Pair<TreeNode, Double>>() {
            @Override
            public int compare(Pair<TreeNode, Double> o1, Pair<TreeNode, Double> o2) {
                if (o1.second < o2.second) {
                    return -1;
                }
                if (o1.second > o2.second) {
                    return 1;
                }
                return 0;
            }
        });
        pq.add(new Pair<>(root, root.estimateMinDist(lat, lng)));
        while (!pq.isEmpty()) {
            Pair<TreeNode, Double> td = pq.poll();
            if (td.second >= minDist) {
                break;
            }
            TreeNode t = td.first;
            if (t.getChildNodes().isEmpty()) {
                Pair<Point, Double> bestPoint = t.findNearestPoint(lat, lng);
                if (bestPoint.second < minDist) {
                    minDist = bestPoint.second;
                    nearestPoint = bestPoint.first;
                }
            } else {
                for (TreeNode c : t.getChildNodes()) {
                    pq.add(new Pair<>(c, c.estimateMinDist(lat, lng)));
                }
            }
        }
        return nearestPoint;
    }

    public Point findNearestPoint(String latlng) {
        String[] s = latlng.split(",");
        double lat = Double.valueOf(s[0].trim());
        double lng = Double.valueOf(s[1].trim());
        return findNearestPoint(lat, lng);
    }

    private void initTree() {
        System.out.println(name() + ":: initTree .....");
        double t0 = System.currentTimeMillis();
        root = buildTree(points, true);
        double t = System.currentTimeMillis() - t0;
        System.out.println(name() + ":: initTree finished, time = " + (t / 1000) + "s");
    }

    private TreeNode buildTree(ArrayList<Point> points, boolean latCoorDiv) {
        TreeNode p = new TreeNode(points);
        if (points.size() <= MIN_NB_POINT_PER_BLOCK) {
            return p;
        }
        if (latCoorDiv) {
            Collections.sort(points, latCmp);
        } else {
            Collections.sort(points, lngCmp);
        }
        ArrayList<Point> l1 = new ArrayList<>();
        ArrayList<Point> l2 = new ArrayList<>();
        int mid = points.size() / 2;
        for (int i = 0; i < points.size(); i++) {
            if (i < mid) {
                l1.add(points.get(i));
            } else {
                l2.add(points.get(i));
            }
        }
        p.addChildNode(buildTree(l1, !latCoorDiv));
        p.addChildNode(buildTree(l2, !latCoorDiv));
        return p;
    }

    public String name() {
        return "KDTree";
    }
}

class LatCmp implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
        if (o1.getLat() < o2.getLat() || (o1.getLat() == o2.getLat() && o1.getLng() < o2.getLng())) {
            return -1;
        }
        if (o2.getLat() < o1.getLat() || (o1.getLat() == o2.getLat() && o2.getLng() < o1.getLng())) {
            return 1;
        }
        return 0;
    }
}

class LngCmp implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
        if (o1.getLng() < o2.getLng() || (o1.getLng() == o2.getLng() && o1.getLat() < o2.getLat())) {
            return -1;
        }
        if (o2.getLng() < o1.getLng() || (o1.getLng() == o2.getLng() && o2.getLat() < o1.getLat())) {
            return 1;
        }
        return 0;
    }
}