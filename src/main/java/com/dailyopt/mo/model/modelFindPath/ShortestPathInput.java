package com.dailyopt.mo.model.modelFindPath;

public class ShortestPathInput {
	private int fromID;
	private int toID;
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
