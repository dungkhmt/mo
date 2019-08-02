package com.dailyopt.mo.model.routevrp;

public class Route {
	private RouteVRPInputPoint[] points;

	public RouteVRPInputPoint[] getPoints() {
		return points;
	}

	public void setPoints(RouteVRPInputPoint[] points) {
		this.points = points;
	}

	public Route(RouteVRPInputPoint[] points) {
		super();
		this.points = points;
	}

	public Route() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
