package com.dailyopt.mo.components.movingobjects;

public interface IMovingObject {
	public String getId();
	public double getLat();
	public double getLng();
	public void estimateNextPositionLatLng(int inSecond);
}
