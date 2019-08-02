package com.dailyopt.mo.model.routevrp;

public class RouteVRPInput {
	private RouteVRPInputPoint[] points;

	public RouteVRPInputPoint[] getPoints() {
		return points;
	}

	public void setPoints(RouteVRPInputPoint[] points) {
		this.points = points;
	}

	public RouteVRPInput(RouteVRPInputPoint[] points) {
		super();
		this.points = points;
	}

	public RouteVRPInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
