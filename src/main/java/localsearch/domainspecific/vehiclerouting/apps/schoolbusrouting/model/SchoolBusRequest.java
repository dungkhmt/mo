package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class SchoolBusRequest {
	
	private int id;
	private int idPerson;
	private int pickupLocationId;
	private int groupId;
	private int deliveryLocationId;
	
	private double lat_pickup;
	private double long_pickup;
	private double lat_delivery;
	private double long_delivery;
	
	private int earlyDateTimePickup;
	private int servicePickupDuration;
	private int lateDateTimePickup;
	private int earlyDateTimeDelivery;
	private int serviceDeliveryDuration;
	private int lateDateTimeDelivery;
	private int varIndex;
	private String siblingCode;

	public SchoolBusRequest(int id, int idPerson,
			int pickupLocationId, int groupId, int deliveryLocationId,
			double lat_pickup, double long_pickup,
			double lat_delivery, double long_delivery,
			int earlyDateTimePickup, int servicePickupDuration,
			int lateDateTimePickup, int earlyDateTimeDelivery,
			int serviceDeliveryDuration, int lateDateTimeDelivery,
			int varIndex, String siblingCode){
		super();
		this.id = id;
		this.idPerson = idPerson;
		this.pickupLocationId = pickupLocationId;
		this.groupId = groupId;
		this.deliveryLocationId = deliveryLocationId;
		this.lat_pickup = lat_pickup;
		this.long_pickup = long_delivery;
		this.lat_delivery = lat_delivery;
		this.long_delivery = long_delivery;
		this.earlyDateTimePickup = earlyDateTimePickup;
		this.servicePickupDuration = servicePickupDuration;
		this.lateDateTimePickup = lateDateTimePickup;
		this.earlyDateTimeDelivery = earlyDateTimeDelivery;
		this.serviceDeliveryDuration = serviceDeliveryDuration;
		this.lateDateTimeDelivery = lateDateTimeDelivery;
		this.varIndex = varIndex;
		this.siblingCode = siblingCode;
	}
	
	public SchoolBusRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public int getId(){
		return this.id;
	}
	public void setId(int id){
		this.id = id;
	}
	public int getIdPerson(){
		return this.idPerson;
	}
	public void setIdPerson(int idPerson){
		this.idPerson = idPerson;
	}
	public int getPickupLocationId(){
		return this.pickupLocationId;
	}
	public void setPickupLocationId(int pickupLocationId){
		this.pickupLocationId = pickupLocationId;
	}
	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getDeliveryLocationId(){
		return this.deliveryLocationId;
	}
	public void setDeliveryLocationId(int deliveryLocationId){
		this.deliveryLocationId = deliveryLocationId;
	}
	public double getLat_pickup() {
		return lat_pickup;
	}

	public void setLat_pickup(double lat_pickup) {
		this.lat_pickup = lat_pickup;
	}

	public double getLong_pickup() {
		return long_pickup;
	}

	public void setLong_pickup(double long_pickup) {
		this.long_pickup = long_pickup;
	}

	public double getLat_delivery() {
		return lat_delivery;
	}

	public void setLat_delivery(double lat_delivery) {
		this.lat_delivery = lat_delivery;
	}

	public double getLong_delivery() {
		return long_delivery;
	}

	public void setLong_delivery(double long_delivery) {
		this.long_delivery = long_delivery;
	}

	public int getEarlyDateTimePickup(){
		return this.earlyDateTimePickup;
	}
	public void setEarlyDateTimePickup(int earlyDateTimePickup){
		this.earlyDateTimePickup = earlyDateTimePickup;
	}
	public int getServicePickupDuration(){
		return this.servicePickupDuration;
	}
	public void setServicePickupDuration(int servicePickupDuration){
		this.servicePickupDuration = servicePickupDuration;
	}
	public int getLateDateTimePickup(){
		return this.lateDateTimePickup;
	}
	public void setLateDateTimePickup(int lateDateTimePickup){
		this.lateDateTimePickup = lateDateTimePickup;
	}
	public int getEarlyDateTimeDelivery(){
		return this.earlyDateTimeDelivery;
	}
	public void setEarlyDateTimeDelivery(int earlyDateTimeDelivery){
		this.earlyDateTimeDelivery = earlyDateTimeDelivery;
	}
	public int getServiceDeliveryDuration(){
		return this.serviceDeliveryDuration;
	}
	public void setServiceDeliveryDuration(int serviceDeliveryDuration){
		this.serviceDeliveryDuration = serviceDeliveryDuration;
	}
	public int getLateDateTimeDelivery(){
		return this.lateDateTimeDelivery;
	}
	public void setLateDateTimeDelivery(int lateDateTimeDelivery){
		this.lateDateTimeDelivery = lateDateTimeDelivery;
	}

	public String getSiblingCode() {
		return siblingCode;
	}

	public void setSiblingCode(String siblingCode) {
		this.siblingCode = siblingCode;
	}

	public int getVarIndex() {
		return varIndex;
	}

	public void setVarIndex(int varIndex) {
		this.varIndex = varIndex;
	}

}
