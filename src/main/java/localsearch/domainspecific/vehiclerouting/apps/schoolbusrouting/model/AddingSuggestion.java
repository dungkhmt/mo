package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class AddingSuggestion {
	private int route;//route duoc add
	private int pre_pointId;//vi tri add
	private int extraTime;//thoi gian phat sinh khi add
	private int extraDistance;//quang duong phat sinh khi add
	private int extraBus;//so bus phat sinh khi add
	private int nbAffectedPupils;//so hoc sinh bi anh huong
	
	public AddingSuggestion(int route, int pre_pointId,
			int extraTime, int extraDistance, int extraBus,
			int nbAffectedPupils){
		super();
		this.route = route;
		this.pre_pointId = pre_pointId;
		this.extraTime = extraTime;
		this.extraDistance = extraDistance;
		this.extraBus = extraBus;
		this.nbAffectedPupils = nbAffectedPupils;
	}
	public AddingSuggestion(){
		super();
	}
	public int getRoute() {
		return route;
	}
	public void setRoute(int route) {
		this.route = route;
	}
	public int getPre_pointId() {
		return pre_pointId;
	}
	public void setPre_pointId(int pre_pointId) {
		this.pre_pointId = pre_pointId;
	}
	public int getExtraTime() {
		return extraTime;
	}
	public void setExtraTime(int extraTime) {
		this.extraTime = extraTime;
	}
	public int getExtraDistance() {
		return extraDistance;
	}
	public void setExtraDistance(int extraDistance) {
		this.extraDistance = extraDistance;
	}
	public int getExtraBus() {
		return extraBus;
	}
	public void setExtraBus(int extraBus) {
		this.extraBus = extraBus;
	}
	public int getNbAffectedPupils() {
		return nbAffectedPupils;
	}
	public void setNbAffectedPupils(int nbAffectedPupils) {
		this.nbAffectedPupils = nbAffectedPupils;
	}
}
