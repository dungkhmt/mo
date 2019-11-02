package com.socolabs.mo.model.routevrp;

public class DistanceElement {
	private String src;
	private String dest;
	private double distance;
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getDest() {
		return dest;
	}
	public void setDest(String dest) {
		this.dest = dest;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public DistanceElement(String src, String dest, double distance) {
		super();
		this.src = src;
		this.dest = dest;
		this.distance = distance;
	}
	public DistanceElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
