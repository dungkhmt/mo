package com.socolabs.mo.components.movingobjects;

public interface IMovingObject{
	public double getLat();
	public double getLng();
	public String getId();
	public void estimateNextPositionLatLng(int inSecond);
}
