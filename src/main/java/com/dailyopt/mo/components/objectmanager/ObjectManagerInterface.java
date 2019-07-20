package com.dailyopt.mo.components.objectmanager;

import com.dailyopt.mo.components.movingobjects.MovingObject;

public interface ObjectManagerInterface {
	public MovingObject addObject(MovingObject obj);
	public MovingObject updateLocation(String ID, double lat, double lng);
}
