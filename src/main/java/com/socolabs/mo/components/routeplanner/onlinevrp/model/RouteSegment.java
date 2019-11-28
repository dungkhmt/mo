package com.socolabs.mo.components.routeplanner.onlinevrp.model;

import java.util.ArrayList;

import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.controller.ApiController;
import com.socolabs.mo.model.routevrp.RouteVRPInputPoint;

/*
 * sequence of map points to a service point
 */
public class RouteSegment {
	private Point[] mapPoints;
	private RouteVRPInputPoint servicePoint;
	private double length;
	
	public RouteSegment(Point[] mapPoints, RouteVRPInputPoint servicePoint,
			double length) {
		super();
		this.mapPoints = mapPoints;
		this.servicePoint = servicePoint;
		this.length = length;
	}
	public RouteSegment(ArrayList<Point> points, RouteVRPInputPoint servicePoint,
			double length) {
		super();
		mapPoints = new Point[points.size()];
		for(int i = 0; i < points.size(); i++) mapPoints[i] = points.get(i);
		this.servicePoint = servicePoint;
		this.length = length;
	}
	public RouteSegment(ArrayList<Point> points, RouteVRPInputPoint servicePoint) {
		super();
		mapPoints = new Point[points.size()];
		for(int i = 0; i < points.size(); i++) mapPoints[i] = points.get(i);
		this.servicePoint = servicePoint;
		computeLength();
	}

	public void computeLength(){
		length = 0;
		for(int i = 0; i < mapPoints.length-1; i++){
			Point p1 = mapPoints[i];
			Point p2 = mapPoints[i+1];
			length += ApiController.GMQ.computeDistanceHaversine(p1.getLat(), p1.getLng(), p2.getLat(), p2.getLng());
		}
	}
	public Point getFirstPoint(){
		if(mapPoints != null && mapPoints.length > 0) return mapPoints[0];
		return null;
	}
	public Point getLastPoint(){
		if(mapPoints != null && mapPoints.length > 0) return mapPoints[mapPoints.length-1];
		return null;
	}
	public double computeLengthFromToEnd(int fromIdx){
		return computeLengthFromTo(fromIdx, mapPoints.length-1);
	}
	public double computeLengthFromTo(int fromIdx, int toIdx){
		// return the length of the path from mapPoints[fromIdx] to mapPoints[toIdx]
		double L = 0;
		for(int i = fromIdx; i < toIdx; i++){
			Point p1 = mapPoints[fromIdx];
			Point p2 = mapPoints[toIdx];
			L += ApiController.GMQ.computeDistanceHaversine(p1.getLat(), p1.getLng(), p2.getLat(), p2.getLng());
		}
		return L;
	}
	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public RouteSegment() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RouteSegment(Point[] mapPoints, RouteVRPInputPoint servicePoint) {
		super();
		this.mapPoints = mapPoints;
		this.servicePoint = servicePoint;
	}

	public Point[] getMapPoints() {
		return mapPoints;
	}

	public void setMapPoints(Point[] mapPoints) {
		this.mapPoints = mapPoints;
	}

	public RouteVRPInputPoint getServicePoint() {
		return servicePoint;
	}

	public void setServicePoint(RouteVRPInputPoint servicePoint) {
		this.servicePoint = servicePoint;
	}

	public Point getPoint(int index){
		return mapPoints[index];
	}
}
