package com.dailyopt.mo.components.maps.graphs;

public class Arc {
	private int endPoint;
	private double length;
	public Arc(int endPoint, double length) {
		super();
		this.endPoint = endPoint;
		this.length = length;
	}
	public int getEndPoint() {
		return endPoint;
	}
	public void setEndPoint(int endPoint) {
		this.endPoint = endPoint;
	}
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	
}
