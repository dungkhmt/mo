package com.dailyopt.mo.components.routeplanner.onlinevrp.model;

public class OnlineVRPSolution {
	private ExecuteRoute[] routes;

	public OnlineVRPSolution(ExecuteRoute[] routes) {
		super();
		this.routes = routes;
	}

	public ExecuteRoute[] getRoutes() {
		return routes;
	}

	public void setRoutes(ExecuteRoute[] routes) {
		this.routes = routes;
	}

	public OnlineVRPSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
