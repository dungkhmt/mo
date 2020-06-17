package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class ConfigParams {
	private int earliestDateTimePickupAtPoint;
	private int latestDateTimeDeliveryAtSchool;
	private int earliestDateTimePickupAtSchool;
	private int latestDateTimeDeliveryAtPoint;
	private int timeScale;
	private int boardingTimeScale1;
	private int boardingTimeScale2;
	private int boardingTimeScale3;
	private int earliestDatetimeArrivalSchool;
	
	public ConfigParams(int earliestDateTimePickupAtPoint,
			int latestDateTimeDeliveryAtSchool,
			int earliestDateTimePickupAtSchool,
			int latestDateTimeDeliveryAtPoint,
			int timeScale, int boardingTimeScale1, int boardingTimeScale2, 
			int boardingTimeScale3, int earliestDatetimeArrivalSchool) {
		super();
		this.earliestDateTimePickupAtPoint = earliestDateTimePickupAtPoint;
		this.latestDateTimeDeliveryAtSchool = latestDateTimeDeliveryAtSchool;
		this.earliestDateTimePickupAtSchool = earliestDateTimePickupAtSchool;
		this.latestDateTimeDeliveryAtPoint = latestDateTimeDeliveryAtPoint;
		this.timeScale = timeScale;
		this.boardingTimeScale1 = boardingTimeScale1;
		this.boardingTimeScale2 = boardingTimeScale2;
		this.boardingTimeScale3 = boardingTimeScale3;
		this.earliestDatetimeArrivalSchool = earliestDatetimeArrivalSchool;
	}
	
	public ConfigParams(){
		super();
	}
	
	public int getEarliestDatetimeArrivalSchool() {
		return earliestDatetimeArrivalSchool;
	}

	public void setEarliestDatetimeArrivalSchool(int earliestDatetimeArrivalSchool) {
		this.earliestDatetimeArrivalSchool = earliestDatetimeArrivalSchool;
	}

	public int getEarliestDateTimePickupAtPoint(){
		return this.earliestDateTimePickupAtPoint;
	}
	public void setEarliestDateTimePickupAtPoint(int earliestDateTimePickupAtPoint){
		this.earliestDateTimePickupAtPoint = earliestDateTimePickupAtPoint;
	}

	public int getLatestDateTimeDeliveryAtSchool() {
		return latestDateTimeDeliveryAtSchool;
	}

	public void setLatestDateTimeDeliveryAtSchool(int latestDateTimeDeliveryAtSchool) {
		this.latestDateTimeDeliveryAtSchool = latestDateTimeDeliveryAtSchool;
	}

	public int getEarliestDateTimePickupAtSchool() {
		return earliestDateTimePickupAtSchool;
	}

	public void setEarliestDateTimePickupAtSchool(int earliestDateTimePickupAtSchool) {
		this.earliestDateTimePickupAtSchool = earliestDateTimePickupAtSchool;
	}

	public int getLatestDateTimeDeliveryAtPoint(){
		return this.latestDateTimeDeliveryAtPoint;
	}
	public void setLatestDateTimeDeliveryAtPoint(int latestDateTimeDeliveryAtPoint){
		this.latestDateTimeDeliveryAtPoint = latestDateTimeDeliveryAtPoint;
	}
	public int getTimeScale(){
		return this.timeScale;
	}
	public void setTimeScale(int timeScale){
		this.timeScale = timeScale;
	}

	public int getBoardingTimeScale1() {
		return boardingTimeScale1;
	}

	public void setBoardingTimeScale1(int boardingTimeScale1) {
		this.boardingTimeScale1 = boardingTimeScale1;
	}

	public int getBoardingTimeScale2() {
		return boardingTimeScale2;
	}

	public void setBoardingTimeScale2(int boardingTimeScale2) {
		this.boardingTimeScale2 = boardingTimeScale2;
	}

	public int getBoardingTimeScale3() {
		return boardingTimeScale3;
	}

	public void setBoardingTimeScale3(int boardingTimeScale3) {
		this.boardingTimeScale3 = boardingTimeScale3;
	}
	
}
