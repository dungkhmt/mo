package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class DistanceElementList {
	private DistanceElement[] distanceElement;
	private int scale;
	public DistanceElementList(DistanceElement[] distanceElement,
                               int scale){
		super();
		this.scale = scale;
		this.distanceElement = distanceElement;
	}
	public DistanceElementList(){
		super();
	}
	public DistanceElement[] getDistanceElement() {
		return distanceElement;
	}
	public void setDistanceElement(DistanceElement[] distanceElement) {
		this.distanceElement = distanceElement;
	}
	public int getScale() {
		return scale;
	}
	public void setScale(int scale) {
		this.scale = scale;
	}
}
