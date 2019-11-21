package com.dailyopt.mo.components.algorithms.nearestlocation;

import com.dailyopt.mo.components.maps.Point;
import com.dailyopt.mo.components.maps.utils.GoogleMapsQuery;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@Getter
@Setter
public class Block {
    public static GoogleMapsQuery G = new GoogleMapsQuery();

    private double latUpper;
    private double latLower;
    private double lngUpper;
    private double lngLower;
    private Collection<Point> points;

    public Block(Collection<Point> points) {
        this.points = points;
        Point fp = points.iterator().next();
        latLower = latUpper = fp.getLat();
        lngLower = lngUpper = fp.getLng();
        for (Point p : points) {
            latLower = Math.min(latLower, p.getLat());
            latUpper = Math.max(latUpper, p.getLat());
            lngLower = Math.min(lngLower, p.getLng());
            lngUpper = Math.max(lngUpper, p.getLng());
        }
    }

    public Block(Collection<Point> points, double latLower, double lngLower, double latUpper, double lngUpper) {
        this.points = points;
        this.latLower = latLower;
        this.latUpper = latUpper;
        this.lngLower = lngLower;
        this.lngUpper = lngUpper;
    }

    public void add(Point p) {
        points.add(p);
    }

    public void remove(Point p) {
        points.remove(p);
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public int size() {
        return points.size();
    }

    public boolean isInRange(Point p) {
        double lat = p.getLat();
        double lng = p.getLng();
        return latLower <= lat && lat <= latUpper && lngLower <= lng && lng <= lngUpper;
    }

    public boolean contains(Point p) {
        if (isInRange(p)) {
            return points.contains(p);
        }
        return false;
    }

    public Pair<Point, Double> findNearestPoint(double lat, double lng) {
        double bestDist = 1e18;
        Point bestPoint = null;
        for (Point p : points) {
            double dist = G.computeDistanceHaversine(lat, lng, p.getLat(), p.getLng());
            if (dist < bestDist) {
                bestDist = dist;
                bestPoint = p;
            }
        }
        return new Pair<>(bestPoint, bestDist);
    }

    public double estimateMinDist(double lat, double lng) {
        if (lat >= latLower && lat <= latUpper && lng >= lngLower && lng <= lngUpper) {
            return 0;
        }
        if (lat <= latLower && lng <= lngLower) {
            return G.computeDistanceHaversine(lat, lng, latLower, lngLower);
        }
        if (lat <= latLower && lng >= lngUpper) {
            return G.computeDistanceHaversine(lat, lng, latLower, lngUpper);
        }
        if (lat >= latUpper && lng <= lngLower) {
            return G.computeDistanceHaversine(lat, lng, latUpper, lngLower);
        }
        if (lat >= latUpper && lng >= lngUpper) {
            return G.computeDistanceHaversine(lat, lng, latUpper, lngUpper);
        }
        if (lat < latLower && lng >= lngLower && lng <= lngUpper) {
            return G.computeDistanceHaversine(lat, lng, latLower, lng);
        }
        if (lat > latUpper && lng >= lngLower && lng <= lngUpper) {
            return G.computeDistanceHaversine(lat, lng, latUpper, lng);
        }
        if (lng < lngLower && lat >= latLower && lat <= latUpper) {
            return G.computeDistanceHaversine(lat, lng, lat, lngLower);
        }
        if (lng > lngUpper && lat >= latLower && lat <= latUpper) {
            return G.computeDistanceHaversine(lat, lng, lat, lngUpper);
        }
        return 0;
    }
}
