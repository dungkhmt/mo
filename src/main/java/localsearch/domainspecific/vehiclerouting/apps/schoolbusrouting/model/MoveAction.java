package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class MoveAction {
	private int typeOfAction;
	private int pointId_1;
	private int varIndex_1;
	private int pre_pointId;
	private int pre_varIndex;
	private int routeIndex1;
	private int pointId_2;
	private int varIndex_2;
	
	public MoveAction(int typeOfAction, int pointId_1, int varIndex_1, int pre_pointId, int pre_varIndex,
			int routeIndex1, int pointId_2, int varIndex_2){
		super();
		this.pointId_1 = pointId_1;
		this.varIndex_1 = varIndex_1;
		this.pre_pointId = pre_pointId;
		this.pre_varIndex = pre_varIndex;
		this.routeIndex1 = routeIndex1;
		this.pointId_2 = pointId_2;
		this.varIndex_2 = varIndex_2;
	}
	
	public MoveAction(){
		super();
	}

	public int getRouteIndex1() {
		return routeIndex1;
	}

	public void setRouteIndex1(int routeIndex1) {
		this.routeIndex1 = routeIndex1;
	}

	public int getTypeOfAction() {
		return typeOfAction;
	}

	public void setTypeOfAction(int typeOfAction) {
		this.typeOfAction = typeOfAction;
	}

	public int getPointId_1() {
		return pointId_1;
	}

	public void setPointId_1(int pointId_1) {
		this.pointId_1 = pointId_1;
	}


	public int getPre_pointId() {
		return pre_pointId;
	}

	public void setPre_pointId(int pre_pointId) {
		this.pre_pointId = pre_pointId;
	}

	public int getPointId_2() {
		return pointId_2;
	}

	public void setPointId_2(int pointId_2) {
		this.pointId_2 = pointId_2;
	}

	public int getVarIndex_1() {
		return varIndex_1;
	}

	public void setVarIndex_1(int varIndex_1) {
		this.varIndex_1 = varIndex_1;
	}

	public int getPre_varIndex() {
		return pre_varIndex;
	}

	public void setPre_varIndex(int pre_varIndex) {
		this.pre_varIndex = pre_varIndex;
	}

	public int getVarIndex_2() {
		return varIndex_2;
	}

	public void setVarIndex_2(int varIndex_2) {
		this.varIndex_2 = varIndex_2;
	}
}
