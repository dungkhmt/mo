package com.socolabs.mo.components.objectmanager;

/*
 * The 2D space is hashed by grid, for example, 1km x 1km
 * A cluster represents a cell of the grid, containing objects belonging to that cell (based on current location)
 * 
 */

import java.util.*;

import com.socolabs.mo.components.movingobjects.MovingObject;

public class ObjectCluster {
	private List<MovingObject> objects;
}
