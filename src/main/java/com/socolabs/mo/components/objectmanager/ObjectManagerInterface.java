package com.socolabs.mo.components.objectmanager;

import com.socolabs.mo.components.movingobjects.MovingObject;

public interface ObjectManagerInterface {
	public MovingObject addObject(MovingObject obj);
	public MovingObject updateLocation(String ID, double lat, double lng);
}
