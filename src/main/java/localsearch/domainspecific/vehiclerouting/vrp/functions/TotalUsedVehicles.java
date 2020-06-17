package localsearch.domainspecific.vehiclerouting.vrp.functions;

import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;

import java.util.ArrayList;

public class TotalUsedVehicles implements IFunctionVR {
	private VarRoutesVR XR;
	private VRManager mgr;
	private int value;

	public TotalUsedVehicles(VarRoutesVR XR) {
		// TODO Auto-generated constructor stub
		this.XR = XR;
		mgr = XR.getVRManager();
		mgr.post(this);
	}

	@Override
	public VRManager getVRManager() {
		// TODO Auto-generated method stub
		return mgr;
	}

	@Override
	public void initPropagation() {
		// TODO Auto-generated method stub
		value = 0;
	}

	@Override
	public void propagateTwoOptMoveOneRoute(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateOnePointMove(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateTwoPointsMove(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateTwoOptMove1(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateTwoOptMove2(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateTwoOptMove3(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateTwoOptMove4(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateTwoOptMove5(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateTwoOptMove6(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateTwoOptMove7(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateTwoOptMove8(Point x, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateOrOptMove1(Point x1, Point x2, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateOrOptMove2(Point x1, Point x2, Point y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateThreeOptMove1(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateThreeOptMove2(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateThreeOptMove3(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateThreeOptMove4(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateThreeOptMove5(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateThreeOptMove6(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateThreeOptMove7(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateThreeOptMove8(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateCrossExchangeMove(Point x1, Point y1, Point x2,
                                           Point y2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateTwoPointsMove(Point x1, Point x2, Point y1, Point y2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateThreePointsMove(Point x1, Point x2, Point x3,
                                         Point y1, Point y2, Point y3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateFourPointsMove(Point x1, Point x2, Point x3, Point x4,
                                        Point y1, Point y2, Point y3, Point y4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateKPointsMove(ArrayList<Point> x, ArrayList<Point> y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateAddOnePoint(Point x, Point y) {
		// TODO Auto-generated method stub
		int r = XR.route(y);
		if(XR.isStartingPoint(y) && XR.next(x) == XR.getTerminatingPointOfRoute(r))
			value++;
	}

	@Override
	public void propagateRemoveOnePoint(Point x) {
		// TODO Auto-generated method stub
		int r = XR.oldRoute(x);
		if(XR.next(XR.getStartingPointOfRoute(r)) == XR.getTerminatingPointOfRoute(r))
			value--;
	}

	@Override
	public void propagateAddTwoPoints(Point x1, Point y1, Point x2, Point y2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateRemoveTwoPoints(Point x1, Point x2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propagateAddRemovePoints(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "TotalUsedVehicles";
	}

	@Override
	public double getValue() {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public double evaluateTwoOptMoveOneRoute(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateOnePointMove(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateTwoPointsMove(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateTwoOptMove1(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateTwoOptMove2(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateTwoOptMove3(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateTwoOptMove4(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateTwoOptMove5(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateTwoOptMove6(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateTwoOptMove7(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateTwoOptMove8(Point x, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateOrOptMove1(Point x1, Point x2, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateOrOptMove2(Point x1, Point x2, Point y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateThreeOptMove1(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateThreeOptMove2(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateThreeOptMove3(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateThreeOptMove4(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateThreeOptMove5(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateThreeOptMove6(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateThreeOptMove7(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateThreeOptMove8(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateCrossExchangeMove(Point x1, Point y1, Point x2,
                                            Point y2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateTwoPointsMove(Point x1, Point x2, Point y1, Point y2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateThreePointsMove(Point x1, Point x2, Point x3,
                                          Point y1, Point y2, Point y3) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateFourPointsMove(Point x1, Point x2, Point x3,
                                         Point x4, Point y1, Point y2, Point y3, Point y4) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateKPointsMove(ArrayList<Point> x, ArrayList<Point> y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateAddOnePoint(Point x, Point y) {
		// TODO Auto-generated method stub
		int r = XR.route(y);
		if(XR.isStartingPoint(y) && XR.next(y) == XR.getTerminatingPointOfRoute(r))
			return 1;
		return 0;
	}

	@Override
	public double evaluateRemoveOnePoint(Point x) {
		// TODO Auto-generated method stub
		int r = XR.route(x);
		if(XR.next(XR.getStartingPointOfRoute(r)) == x
				&& XR.next(x) == XR.getTerminatingPointOfRoute(r))
			return -1;
		return 0;
	}

	@Override
	public double evaluateAddTwoPoints(Point x1, Point y1, Point x2, Point y2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateRemoveTwoPoints(Point x1, Point x2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double evaluateAddRemovePoints(Point x, Point y, Point z) {
		// TODO Auto-generated method stub
		return 0;
	}

}
