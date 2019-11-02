package com.socolabs.mo.model.modelFindPath;

public class ShortestPathInput {
	private int fromID;
	private int toID;
	private String fromPoint;// lat,lng
	private String toPoint;// lat,lng
	
	public ShortestPathInput(int fromID, int toID, String fromPoint,
			String toPoint) {
		super();
		this.fromID = fromID;
		this.toID = toID;
		this.fromPoint = fromPoint;
		this.toPoint = toPoint;
	}
	public String getFromPoint() {
		return fromPoint;
	}
	public void setFromPoint(String fromPoint) {
		this.fromPoint = fromPoint;
	}
	public String getToPoint() {
		return toPoint;
	}
	public void setToPoint(String toPoint) {
		this.toPoint = toPoint;
	}
	public int getFromID() {
		return fromID;
	}
	public void setFromID(int fromID) {
		this.fromID = fromID;
	}
	public int getToID() {
		return toID;
	}
	public void setToID(int toID) {
		this.toID = toID;
	}
	public ShortestPathInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ShortestPathInput(int fromID, int toID) {
		super();
		this.fromID = fromID;
		this.toID = toID;
	}
	
}
