package com.dailyopt.mo.components.algorithms.nearestlocation;

import com.dailyopt.mo.components.maps.Point;
import com.dailyopt.mo.components.maps.utils.GoogleMapsQuery;

import java.util.ArrayList;

public class Block {
    public static GoogleMapsQuery G = new GoogleMapsQuery();

    private double latUpper;
    private double latLower;
    private double lngUpper;
    private double lngLower;
    private ArrayList<Point> points;

    public Block(ArrayList<Point> points) {
        this.points = points;
        latLower = latUpper = points.get(0).getLat();
        lngLower = lngUpper = points.get(0).getLng();
        for (Point p : points) {
            latLower = Math.min(latLower, p.getLat());
            latUpper = Math.max(latUpper, p.getLat());
            lngLower = Math.min(lngLower, p.getLng());
            lngUpper = Math.max(lngUpper, p.getLng());
        }
    }

    public Point findNearestPoint(double lat, double lng) {
        double bestDist = 1e18;
        Point bestPoint = null;
        for (Point p : points) {
            double dist = G.computeDistanceHaversine(lat, lng, p.getLat(), p.getLng());
            if (dist < bestDist) {
                bestDist = dist;
                bestPoint = p;
            }
        }
        return bestPoint;
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

        return 0;
    }
}