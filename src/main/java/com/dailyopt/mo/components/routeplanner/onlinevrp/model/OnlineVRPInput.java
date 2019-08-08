package com.dailyopt.mo.components.routeplanner.onlinevrp.model;

import com.dailyopt.mo.model.routevrp.RouteVRPInputPoint;

public class OnlineVRPInput {
	private ExecuteRoute[] routes;
	private RouteVRPInputPoint[] newRequests;
	
	
	public OnlineVRPInput(ExecuteRoute[] routes,
			RouteVRPInputPoint[] newRequests) {
		super();
		this.routes = routes;
		this.newRequests = newRequests;
	}
	public ExecuteRoute[] getRoutes() {
		return routes;
	}
	public void setRoutes(ExecuteRoute[] routes) {
		this.routes = routes;
	}
	public RouteVRPInputPoint[] getNewRequests() {
		return newRequests;
	}
	public void setNewRequests(RouteVRPInputPoint[] newRequests) {
		this.newRequests = newRequests;
	}
	
}
