package com.socolabs.mo.components.movingobjects;

public interface IMovingObject extends ILocation{
	public String getId();
	public void estimateNextPositionLatLng(int inSecond);
}
