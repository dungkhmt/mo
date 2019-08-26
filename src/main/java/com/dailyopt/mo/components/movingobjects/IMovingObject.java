package com.dailyopt.mo.components.movingobjects;

public interface IMovingObject extends ILocation{
	public void estimateNextPositionLatLng(int inSecond);
}
