package com.dailyopt.mo.model.modelmap;

public class MapWindow {
	private double minlat;
	private double minlng;
	private double maxlat;
	private double maxlng;
	public double getMinlat() {
		return minlat;
	}
	public void setMinlat(double minlat) {
		this.minlat = minlat;
	}
	public double getMinlng() {
		return minlng;
	}
	public void setMinlng(double minlng) {
		this.minlng = minlng;
	}
	public double getMaxlat() {
		return maxlat;
	}
	public void setMaxlat(double maxlat) {
		this.maxlat = maxlat;
	}
	public double getMaxlng() {
		return maxlng;
	}
	public void setMaxlng(double maxlng) {
		this.maxlng = maxlng;
	}
	public MapWindow(double minlat, double minlng, double maxlat, double maxlng) {
		super();
		this.minlat = minlat;
		this.minlng = minlng;
		this.maxlat = maxlat;
		this.maxlng = maxlng;
	}
	public MapWindow() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
