package com.socolabs.mo.components.maps.utils;

import lombok.Getter;

@Getter
public class LatLng {

	/**
	 * @param args
	 */
	public String code;
	public double lat;
	public double lng;
	public LatLng(double lat, double lng){ this.lat = lat; this.lng = lng;}
	public String toString(){ return lat + "," + lng;}
}
