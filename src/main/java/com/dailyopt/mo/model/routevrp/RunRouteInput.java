package com.dailyopt.mo.model.routevrp;

public class RunRouteInput {
	private String vehicleCode;
	private Route route;
	public String getVehicleCode() {
		return vehicleCode;
	}
	public void setVehicleCode(String vehicleCode) {
		this.vehicleCode = vehicleCode;
	}
	public Route getRoute() {
		return route;
	}
	public void setRoute(Route route) {
		this.route = route;
	}
	public RunRouteInput(String vehicleCode, Route route) {
		super();
		this.vehicleCode = vehicleCode;
		this.route = route;
	}
	public RunRouteInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
