package com.socolabs.mo.model.routevrp;

public class RouteVRPInput {
	private RouteVRPInputPoint[] points;
	private DistanceElement[] distances;
	
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

	public DistanceElement[] getDistances() {
		return distances;
	}

	public void setDistances(DistanceElement[] distances) {
		this.distances = distances;
	}

	public RouteVRPInput(RouteVRPInputPoint[] points,
			DistanceElement[] distances) {
		super();
		this.points = points;
		this.distances = distances;
	}
	public DistanceElement getDistanceElement(String src, String dest){
		if(distances == null) return null;
		for(int i = 0; i < distances.length; i++){
			if(distances[i].getSrc().equals(src) && distances[i].getDest().equals(dest)){
				return distances[i];
			}
		}
		return null;
	}
}
