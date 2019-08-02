package com.dailyopt.mo.components.maps;

public class Path {
	private Point[] points;
	private double length;
	
	public Path(Point[] points, double length) {
		super();
		this.points = points;
		this.length = length;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public Point[] getPoints() {
		return points;
	}

	public void setPoints(Point[] points) {
		this.points = points;
	}

	public Path(Point[] points) {
		super();
		this.points = points;
	}
	public String toString(){
		String s = "length = " + length;
		for(int i = 0; i < points.length; i++)
			s = s + ", " + points[i].getId();
		
		return s;
	}
	public Path() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
