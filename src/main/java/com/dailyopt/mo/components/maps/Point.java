package com.dailyopt.mo.components.maps;

public class Point {
	private int id;
	private double lat;
	private double lng;
	public Point(int id, double lat, double lng) {
		super();
		this.id = id;
		this.lat = lat;
		this.lng = lng;
	}
	public Point() {
		super();
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public String getLatLng(){
		return this.lat + "," + this.lng;
	}
	public String toString() {
		return "Point(" + getLatLng() + ")";
	}
}
