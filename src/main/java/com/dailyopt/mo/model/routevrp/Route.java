package com.dailyopt.mo.model.routevrp;

import com.dailyopt.mo.components.maps.Path;


public class Route {
	private RouteVRPInputPoint[] points;
	private double length;
	private Path[] paths;// paths[i]: detail itinerary (point on maps) from points[i] -> points[i+1]
	
	

	public Route(RouteVRPInputPoint[] points, double length, Path[] paths) {
		super();
		this.points = points;
		this.length = length;
		this.paths = paths;
	}



	public RouteVRPInputPoint[] getPoints() {
		return points;
	}



	public void setPoints(RouteVRPInputPoint[] points) {
		this.points = points;
	}



	public double getLength() {
		return length;
	}



	public void setLength(double length) {
		this.length = length;
	}



	public Path[] getPaths() {
		return paths;
	}



	public void setPaths(Path[] paths) {
		this.paths = paths;
	}



	public Route() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
