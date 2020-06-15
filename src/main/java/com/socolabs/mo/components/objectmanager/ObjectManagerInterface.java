package com.socolabs.mo.components.objectmanager;

import com.socolabs.mo.components.movingobjects.IMovingObject;
import com.socolabs.mo.components.movingobjects.MovingObject;

public interface ObjectManagerInterface {
	public IMovingObject addObject(IMovingObject obj);
	public IMovingObject removeObject(IMovingObject obj);
	public IMovingObject updateLocation(String ID, double lat, double lng);
}
