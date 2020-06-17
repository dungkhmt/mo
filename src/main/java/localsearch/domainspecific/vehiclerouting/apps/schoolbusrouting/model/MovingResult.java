package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class MovingResult {
	private int status;//success = 0; timeVio = 1; capVio = 2; timeAndCapVio = 3
	private int extraViolationTime;//thoi gian den truong muon
	private int extraViolationCap;//so hoc sinh vuot qua capacity cua xe.
	private int extraTime;//thoi gian cong them khi phat sinh action
	private int extraDistance;//khoang cach phat sinh
	private AddingSuggestion[] addingSuggestions;
	
	public MovingResult(int status, int extraViolationTime,
			int extraViolationCap, int extraTime, int extraDistance,
			AddingSuggestion[] addingSuggestions){
		super();
		this.status = status;
		this.extraViolationTime = extraViolationTime;
		this.extraViolationCap = extraViolationCap;
		this.extraTime = extraTime;
		this.extraDistance = extraDistance;
		this.addingSuggestions = addingSuggestions;
	}
	public MovingResult(){
		super();
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getExtraViolationTime() {
		return extraViolationTime;
	}
	public void setExtraViolationTime(int extraViolationTime) {
		this.extraViolationTime = extraViolationTime;
	}
	public int getExtraViolationCap() {
		return extraViolationCap;
	}
	public void setExtraViolationCap(int extraViolationCap) {
		this.extraViolationCap = extraViolationCap;
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
	public AddingSuggestion[] getAddingSuggestions() {
		return addingSuggestions;
	}
	public void setAddingSuggestions(AddingSuggestion[] addingSuggestions) {
		this.addingSuggestions = addingSuggestions;
	}
	
}
