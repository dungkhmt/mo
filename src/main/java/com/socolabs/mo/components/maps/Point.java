package com.socolabs.mo.components.maps;

import com.socolabs.mo.components.movingobjects.ILocation;

public class Point implements ILocation {
	protected double lat;
	protected double lng;
	public Point(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}
	public Point() {

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
