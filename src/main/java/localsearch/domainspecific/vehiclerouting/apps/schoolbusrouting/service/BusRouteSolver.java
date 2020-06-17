package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.service;

import com.google.gson.Gson;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.*;
import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.timewindows.CEarliestArrivalTimeVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.LexMultiValues;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.CapacityConstraintViolationsVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalUsedVehicles;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.EarliestArrivalTimeVR;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.*;

public class BusRouteSolver {
	public static final Logger LOGGER = Logger.getLogger("Logger");
	
	public SchoolBusRoutingInput input;
	public Vehicle[] vehicles;
	public SchoolBusRequest[] requests;
			
	public ArrayList<Point> points;
	public ArrayList<Point> pickupPoints;
	public ArrayList<Point> deliveryPoints;

	public ArrayList<Point> startPoints;
	public ArrayList<Point> stopPoints;
	public ArrayList<Point> rejectPoints;
	public HashMap<Point, Integer> earliestAllowedArrivalTime;
	public HashMap<Point, Integer> serviceDuration;
	public HashMap<Point, Integer> lastestAllowedArrivalTime;
	public double[] capList;
	public HashMap<Point, ArrayList<Integer>> point2nameList;
	public HashMap<Point, ArrayList<Integer>> point2idReqList;
	public HashMap<Point, Integer> point2groupId;//point thuoc cum uu tien nao
	public HashMap<Integer, ArrayList<Point>> groupId2points;
	public HashMap<Integer, HashSet<Integer>> groupId2vhIds;//cac point cua moi cum uu tien dang thuoc nhung roue nao
	
	public double[][] distance;
	public double[][] travelTime;
	public int[][] roadBlock;
	public HashSet<Integer> s_pointId;
	public HashMap<Integer, Integer> mPointId2Index;

	public int nVehicle;
	public int nRequest;
	public static double MAX_DISTANCE;

	public ArcWeightsManager awmT;
	public ArcWeightsManager awmD;
	public NodeWeightsManager nwm;
	public VRManager mgr;
	public VarRoutesVR XR;
	public ConstraintSystemVR S;
	public IFunctionVR objective;
	public IFunctionVR distanceObj;
	public IFunctionVR totalBuses;
	public CEarliestArrivalTimeVR ceat;
	public LexMultiValues valueSolution;
	public EarliestArrivalTimeVR eat;
	public CEarliestArrivalTimeVR cEarliest;
	public CapacityConstraintViolationsVR capCons;
	public AccumulatedWeightEdgesVR accDisInvr;
	public HashMap<Point, IFunctionVR> accDisF;
	
	public MovingResult[] movingResults;
	public HashMap<Integer, Integer> vhId2flag;//flag trang thai cap nhat/xoa/ko thay doi cua moi bus
	public HashMap<Integer, Integer> vhId2busID;//tra lai busID cho UI
	public HashMap<Integer, String> vhId2busName;//tra lai busName cho UI
	public HashMap<Integer, Integer> vhId2startTime;//tra lai start pickup time cho UI
	public HashMap<Integer, Integer> vhId2endTime;//tra lai start delivery cho UI
	public HashMap<Integer, Integer> vhId2resolved;//trang thai chot tuyen/ chua chot
	public HashMap<Integer, Integer> vhId2Index;//index trong input.vehicles
	public ArrayList<String> srcdest;//chua cac phan tu co dang "src-dest" de check su ton tai trong distance matrix
	
	public HashMap<Integer, ArrayList<Integer>> point2cusIndex;
	public int[] reqsMarked;
	public int nbFixedPoint;
	
	public int sVio;
	public double capVio;
	public String fileName;
	
	public HashMap<Integer, Integer> hs2vhId;
	public HashMap<Integer, Integer> hs2pointId;
	public HashMap<Integer, Integer> hs2varIndex;
	public HashMap<Integer, Integer> hs2reqId;

	public BusRouteSolver(SchoolBusRoutingInput input) {
		// TODO Auto-generated constructor stub
		this.input = input;
		this.sVio = 0;
		this.capVio = 0;
	}
	
	public BusRouteSolver() {
		// TODO Auto-generated constructor stub	
	}
	
	public String name(){
		return "BusRouteSolver";
	}
	
	public void initLogFile(String fileName){
		this.fileName = fileName;
		try{
			Handler fileHandler;
			String logFile = fileName + "-result.json";
			fileHandler = new FileHandler(logFile);
			Formatter simpleFormater = new SimpleFormatter();
			
			LOGGER.addHandler(fileHandler);
	    	
			fileHandler.setFormatter(simpleFormater);
		}catch(Exception e){
			
		}
	}
	
	public void closeLog(){
		Handler[] fileHandlers = LOGGER.getHandlers();
		for(int i = 0; i < fileHandlers.length; i++)
			fileHandlers[i].close();
	}
	
	public int getOldNbRequestsOnRoute(int vhId){		
		SchoolBusRoutingSolution lastSolution = input.getCurrentSolution();
		if(lastSolution != null){
			for(int i = 0; i < lastSolution.getBusRoutes().length; i++){
				if(lastSolution.getBusRoutes()[i].getVehicle().getId() == vhId)
					return lastSolution.getBusRoutes()[i].getNbPersons();
			}
		}
		return -1;
	}
	public void checkNbRequestsUpdated(int vhId, int nbPersons){
		SchoolBusRoutingSolution lastSolution = input.getCurrentSolution();
		if(lastSolution != null){
			for(int i = 0; i < lastSolution.getBusRoutes().length; i++){
				if(lastSolution.getBusRoutes()[i].getVehicle().getId() == vhId){
					int oldnb = lastSolution.getBusRoutes()[i].getNbPersons();
					if(oldnb != nbPersons && oldnb > 0 && nbPersons > 2)
						vhId2flag.put(vhId, Utils.UPDATED);
					//truong hop phat sinh req moi nhung user chua an cap nhat phan tuyen, thi chay updateCurrrentSolution,
					//khi ay flag = update, khi thuc hien add/remove thi se bi hieu nham. 
					int oldFlag = lastSolution.getBusRoutes()[i].getUpdateFlag();
					if(vhId2flag.get(vhId) == Utils.NO_UPDATE
						&& oldFlag != Utils.NO_UPDATE)
						vhId2flag.put(vhId, oldFlag);
				}
			}
		}
		
		
	}
	
	/**
	 * 
	 * @param id: point id
	 * @param arr: list considered points
	 * @return: point has minimum number of pupils 
	 */
	public Point getMinCusElementInList(int id, ArrayList<Point> arr){
    	int minNb = 100000000;
    	Point p = null;
    	for(int i = 0; i < arr.size(); i++){
    		Point t = arr.get(i);
    		if(id == t.getID()){
    			if(point2nameList.get(t).size() < minNb){
    				minNb = point2nameList.get(t).size();
    				p = t;
    			}
    		}
    	}
    	return p;
    }
	
	public double getDistance(int src, int dest) {
		if (mPointId2Index.get(src) == null
				|| mPointId2Index.get(dest) == null) {
			 System.out.println(name() + "::getDistance, src " + src +
			 " OR dest " + dest + " NOT COMPLETE, INPUT ERROR??????");
			 LOGGER.log(Level.WARNING, name() + "::getDistance, src " + src +
					 " OR dest " + dest + " NOT COMPLETE, INPUT ERROR??????");
			 return -1;
		}

		int is = mPointId2Index.get(src);
		int id = mPointId2Index.get(dest);
//		if (src != dest && distance[is][id] <= 0) {
//			 System.out.println(name() + "::getDistance, from src " + src +
//			 " to dest " + dest + " invalid, INPUT ERROR??????");
//			 LOGGER.log(Level.WARNING, name() + "::getDistance, from src " + src +
//					 " to dest " + dest + " invalid, INPUT ERROR??????");
//		}
		return distance[is][id];
	}
	
	public double getTravelTime(int src, int dest) {
		if (mPointId2Index.get(src) == null
				|| mPointId2Index.get(dest) == null) {
			 System.out.println(name() + "::getTravelTime, src " + src +
			 " OR dest " + dest + " NOT COMPLETE, INPUT ERROR??????");
			 LOGGER.log(Level.WARNING, name() + "::getTravelTime, src " + src +
					 " OR dest " + dest + " NOT COMPLETE, INPUT ERROR??????");
			 return -1;
		}

		int is = mPointId2Index.get(src);
		int id = mPointId2Index.get(dest);
//		if (src != dest && travelTime[is][id] <= 0) {
//			 System.out.println(name() + "::getTravelTime, from src " + src +
//			 " to dest " + dest + " invalid, INPUT ERROR??????");
//			 LOGGER.log(Level.WARNING, name() + "::getTravelTime, from src " + src +
//					 " to dest " + dest + " invalid, INPUT ERROR??????");
//		}
		return travelTime[is][id];
	}
	
	public int getRoadBlock(int src, int dest) {
		if (mPointId2Index.get(src) == null
				|| mPointId2Index.get(dest) == null) {
			 System.out.println(name() + "::getRoadBlock, src " + src +
			 " OR dest " + dest + " NOT COMPLETE, INPUT ERROR??????");
			 LOGGER.log(Level.WARNING, name() + "::getRoadBlock, src " + src +
					 " OR dest " + dest + " NOT COMPLETE, INPUT ERROR??????");
			 return Utils.CAP_29;
		}

		int is = mPointId2Index.get(src);
		int id = mPointId2Index.get(dest);
		return roadBlock[is][id];
	}
	
	/**
	 * 
	 * @param p: root point
	 * @param pickupPoints: list considered points
	 * @return: nearest point from root point
	 */
	public Point getNearestPoint(Point p, ArrayList<Point> pickupPoints){
		if(XR.route(p) == Constants.NULL_POINT)
			return null;
		int r = XR.route(p);
		Point st = XR.getStartingPointOfRoute(r);
		Point nearestPoint = null;
		double best_objective = Double.MAX_VALUE;
		
		for(int i = 0; i < pickupPoints.size(); i++){
			Point pickup = pickupPoints.get(i);
			if(XR.route(pickup) != Constants.NULL_POINT)
				continue;
//			System.out.println("travel time: " + p.getID() + " -> " + XR.getTerminatingPointOfRoute(XR.route(p)).getID() + " = " 
//					+ awmT.getWeight(p, XR.getTerminatingPointOfRoute(XR.route(p)))
//					+ ", " + p.getID() + " -> " + pickup.getID() + " = " + awmT.getWeight(p, pickup)
//					+ ", " + pickup.getID() + " -> " + XR.getTerminatingPointOfRoute(XR.route(p)).getID() + " = " 
//					+ awmT.getWeight(pickup, XR.getTerminatingPointOfRoute(XR.route(p))));
//			System.out.println("S = " + S.evaluateAddOnePoint(pickup, p));
//			System.out.println("Cap con = " + capCons.evaluateAddOnePoint(pickup, p));
//			System.out.println("Size bus = " + checkSizeBus(r, getRoadBlock(p.getID(), pickup.getID())));
//			System.out.println("Distance constraint = " + checkDistanceConstraint(pickup, p, st));
			if (S.evaluateAddOnePoint(pickup, p) == 0
					&& capCons.evaluateAddOnePoint(pickup, p) == 0
					&& checkSizeBus(r, getRoadBlock(p.getID(), pickup.getID()))
					&& checkDistanceConstraint(pickup, p, st)) {
				double cost = objective.evaluateAddOnePoint(pickup, p);
				if(cost < best_objective){
					best_objective = cost;
					nearestPoint = pickup;
				}
			}
		}
		return nearestPoint;
	}
	
	/**
	 * 
	 * @param p: root point
	 * @param pickupPoints: list considered points
	 * @return: farthest point from root point
	 */
	public Point getFarthestPoint(Point p, ArrayList<Point> pickupPoints){
		if(XR.route(p) == Constants.NULL_POINT)
			return null;
		int r = XR.route(p);
		Point st = XR.getStartingPointOfRoute(r);
		Point farthestPoint = null;
		double bad_objective = Double.MIN_VALUE;
		
		for(int i = 0; i < pickupPoints.size(); i++){
			Point pickup = pickupPoints.get(i);
			if(XR.route(pickup) != Constants.NULL_POINT)
				continue;
			if (S.evaluateAddOnePoint(pickup, p) == 0
					&& capCons.evaluateAddOnePoint(pickup, p) == 0
					&& checkSizeBus(r, getRoadBlock(p.getID(), pickup.getID()))
					&& checkDistanceConstraint(pickup, p, st)) {
				double cost = objective.evaluateAddOnePoint(pickup, p);
				if(cost > bad_objective){
					bad_objective = cost;
					farthestPoint = pickup;
				}
			}
		}
		return farthestPoint;
	}
	
	public Point getPointById(int id, int varIndex, ArrayList<Point> arr){
		for(int i = 0; i < arr.size(); i++){
			Point p = arr.get(i);
			if(p.getID() == id && p.getVarIndex() == varIndex)
				return p;
		}
		return null;
	}
	
	public ArrayList<Point> getPointById(int id, ArrayList<Point> arr){
		ArrayList<Point> result = new ArrayList<Point>();
		for(int i = 0; i < arr.size(); i++){
			Point p = arr.get(i);
			if(p.getID() == id)
				result.add(p);
		}
		return result;
	}
	
	/**
	 * lay tat ca cac anh/em co cung siblingCode tai 1 diem
	 * @param siblingCode: ma anh em can lay
	 * @param reqIndexAtPoint: list cac hoc sinh tai 1 diem
	 * @return
	 */
	public ArrayList<Integer> getSiblingIndexByCode(String siblingCode, ArrayList<Integer> reqIndexAtPoint){
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(int i = 0; i < reqIndexAtPoint.size(); i++){
			if(requests[reqIndexAtPoint.get(i)].getSiblingCode().equals(siblingCode))
				result.add(reqIndexAtPoint.get(i));
		}
		return result;
	}
	
	public int getMaxVarIndex(ArrayList<Point> arr){
		int maxIndex = 0;
		for(int i = 0; i < arr.size(); i++){
			if(maxIndex < arr.get(i).getVarIndex())
				maxIndex = arr.get(i).getVarIndex();
		}
		return maxIndex;
	}
	
	public int getBusIDfromSolution(int vhId, SchoolBusRoutingSolution solution){
		if(solution == null)
			return 0;
		for(int i = 0; i < solution.getBusRoutes().length; i++)
			if(solution.getBusRoutes()[i].getVehicle().getId() == vhId)
				return solution.getBusRoutes()[i].getBusID();
		return 0;
	}
	
	public String getBusNamefromSolution(int vhId, SchoolBusRoutingSolution solution){
		if(solution == null)
			return "";
		for(int i = 0; i < solution.getBusRoutes().length; i++)
			if(solution.getBusRoutes()[i].getVehicle().getId() == vhId)
				return solution.getBusRoutes()[i].getBusName();
		return "";
	}
	
	public int getStartTimefromSolution(int vhId, SchoolBusRoutingSolution solution){
		if(solution == null)
			return input.getConfigParams().getEarliestDateTimePickupAtPoint();
		for(int i = 0; i < solution.getBusRoutes().length; i++)
			if(solution.getBusRoutes()[i].getVehicle().getId() == vhId
			&& solution.getBusRoutes()[i].getStartingDateTimePickup() > 0)
				return solution.getBusRoutes()[i].getStartingDateTimePickup();
		return input.getConfigParams().getEarliestDateTimePickupAtPoint();
	}
	
	public int getEndTimefromSolution(int vhId, SchoolBusRoutingSolution solution){
		if(solution == null)
			return input.getConfigParams().getEarliestDateTimePickupAtSchool();
		for(int i = 0; i < solution.getBusRoutes().length; i++)
			if(solution.getBusRoutes()[i].getVehicle().getId() == vhId
			&& solution.getBusRoutes()[i].getStartingDateTimeDelivery() > 0)
				return solution.getBusRoutes()[i].getStartingDateTimeDelivery();
		return input.getConfigParams().getEarliestDateTimePickupAtSchool();
	}
	
	public AddingSuggestion[] getAddingSuggestions(Point pickup){
		ArrayList<AddingSuggestion> suggestions = new ArrayList<AddingSuggestion>();
		for (int r = 1; r <= XR.getNbRoutes(); r++) {
			boolean newBus = false;
			Point st = XR.getStartingPointOfRoute(r);
			if(XR.next(st) == XR.getTerminatingPointOfRoute(r) && newBus)
				continue;
			for (Point p = st; p != XR.getTerminatingPointOfRoute(r); p = XR
					.next(p)) {
				if (S.evaluateAddOnePoint(pickup, p) == 0
						&& capCons.evaluateAddOnePoint(pickup, p) == 0
						&& checkSizeBus(r, getRoadBlock(p.getID(), pickup.getID()))) {
					AddingSuggestion sg = new AddingSuggestion();
					int extraBus = (int)(totalBuses.evaluateAddOnePoint(pickup, p));
					if(extraBus > 0)
						newBus = true;
					sg.setExtraTime((int)objective.evaluateAddOnePoint(pickup, p));
					sg.setExtraBus(extraBus);
					sg.setRoute(vhId2busID.get(vehicles[r-1].getId()));
					sg.setPre_pointId(p.getID());
					suggestions.add(sg);
				}
			}
		}
		AddingSuggestion[] addingSuggestions = new AddingSuggestion[suggestions.size()];
		for(int i = 0; i < suggestions.size() && i < 5; i++)
			addingSuggestions[i] = suggestions.get(i);
		return addingSuggestions;
	}
	
	public void getDistanceMatrix(){
		try{
			String url = "http://103.56.158.242:8080/DailyOptAPI/distanceMatrix";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			//add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("accept", "application/json");
			con.setRequestProperty("Content-Type", "application/json");

			// Send post request
			con.setDoOutput(true);
			Gson gson = new Gson();
			DistanceElementList del = new DistanceElementList();
			del.setDistanceElement(input.getDistances());
			del.setScale(input.getConfigParams().getTimeScale());
			String strIn = gson.toJson(del);
	        OutputStream os = con.getOutputStream();
	        os.write(strIn.getBytes());
	        os.flush();
	        
			con.connect();
			int responseCode = con.getResponseCode();
			System.out.println("responseCode" + responseCode);
			if(responseCode != 200){
				LOGGER.log(Level.INFO, "Cannot get s_point id");
				System.exit(0);
			}
			
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			DistanceMatrixElement disMatrixId = gson.fromJson(response.toString(), DistanceMatrixElement.class);
			s_pointId = disMatrixId.getS_pointId();
			mPointId2Index = disMatrixId.getmPointId2Index();
			travelTime = disMatrixId.getTravelTime();
			distance = disMatrixId.getDistance();
			roadBlock = disMatrixId.getRoadBlock();
		}catch(Exception e){
			System.out.println("exception " + e);
		}
	}
	
	/**
	 * greedy init solution
	 * choose the best-objective point for insertion.
	 */
	public void greedyInitSolution() {
		//high-priority scheduling for group points
		for(int groupId : groupId2points.keySet()){
			ArrayList<Point> pInGroup = groupId2points.get(groupId);
			for (int i = 0; i < pInGroup.size(); i++) {
				Point pickup = pInGroup.get(i);
					
				if (XR.route(pickup) != Constants.NULL_POINT)
					continue;

				Point pre_pick = null;
				double best_objective = Double.MAX_VALUE;
				double bestAddUsedBuses = Integer.MAX_VALUE;

				HashSet<Integer> routes = groupId2vhIds.get(groupId);
				for(int r : routes){
					if(vhId2resolved.get(vehicles[r-1].getId()) == 1)
						continue;
					Point st = XR.getStartingPointOfRoute(r);
					for(Point p = st; p != XR.getTerminatingPointOfRoute(r);
                        p = XR.next(p)) {
						if (S.evaluateAddOnePoint(pickup, p) <= sVio
								&& capCons.evaluateAddOnePoint(pickup, p) <= capVio
								&& checkSizeBus(r, getRoadBlock(p.getID(), pickup.getID()))
								&& checkDistanceConstraint(pickup, p, st)) {
							double cost = objective.evaluateAddOnePoint(pickup, p);
							double addBuses = totalBuses.evaluateAddOnePoint(pickup, p);
							if (addBuses < bestAddUsedBuses || 
								(addBuses == bestAddUsedBuses && cost < best_objective)) {
								best_objective = cost;
								bestAddUsedBuses = addBuses;
								pre_pick = p;
							}
						}
					}
				}
				if(pre_pick != null){
					mgr.performAddOnePoint(pickup, pre_pick);
				}
				else{
					for (int r = 1; r <= XR.getNbRoutes(); r++) {
						if(vhId2resolved.get(vehicles[r-1].getId()) == 1)
							continue;
						Point st = XR.getStartingPointOfRoute(r);
						for (Point p = st; p != XR.getTerminatingPointOfRoute(r); p = XR
								.next(p)) {
							if (S.evaluateAddOnePoint(pickup, p) <= sVio
									&& capCons.evaluateAddOnePoint(pickup, p) <= capVio
									&& checkSizeBus(r, getRoadBlock(p.getID(), pickup.getID()))
									&& checkDistanceConstraint(pickup, p, st)) {
								double cost = objective.evaluateAddOnePoint(pickup, p);
								double addBuses = totalBuses.evaluateAddOnePoint(pickup, p);
								if (addBuses < bestAddUsedBuses || 
									(addBuses == bestAddUsedBuses && cost < best_objective)) {
									best_objective = cost;
									bestAddUsedBuses = addBuses;
									pre_pick = p;
								}
							}
						}
					}
					if (pre_pick != null){
						mgr.performAddOnePoint(pickup, pre_pick);
						routes.add(XR.route(pre_pick));
						groupId2vhIds.put(groupId, routes);
					}
				}
			}
		}
		
		for (int i = 0; i < pickupPoints.size(); i++) {
			Point pickup = pickupPoints.get(i);
				
			if (XR.route(pickup) != Constants.NULL_POINT)
				continue;

			Point pre_pick = null;
			double best_objective = Double.MAX_VALUE;
			double bestAddUsedBuses = Integer.MAX_VALUE;
			for (int r = 1; r <= XR.getNbRoutes(); r++) {
				if(vhId2resolved.get(vehicles[r-1].getId()) == 1)
					continue;
				Point st = XR.getStartingPointOfRoute(r);
				for (Point p = st; p != XR.getTerminatingPointOfRoute(r); p = XR
						.next(p)) {
					if (S.evaluateAddOnePoint(pickup, p) <= sVio
							&& capCons.evaluateAddOnePoint(pickup, p) <= capVio
							&& checkSizeBus(r, getRoadBlock(p.getID(), pickup.getID()))
							&& checkDistanceConstraint(pickup, p, st)) {
						double cost = objective.evaluateAddOnePoint(pickup, p);
						double addBuses = totalBuses.evaluateAddOnePoint(pickup, p);
						if (addBuses < bestAddUsedBuses || 
							(addBuses == bestAddUsedBuses && cost < best_objective)) {
							best_objective = cost;
							bestAddUsedBuses = addBuses;
							pre_pick = p;
						}
					}
				}
			}
			if (pre_pick == null)
				rejectPoints.add(pickup);
			else {
				mgr.performAddOnePoint(pickup, pre_pick);
				int groupId = point2groupId.get(pickup);
				if(groupId > 0){
					HashSet<Integer> routes = groupId2vhIds.get(groupId);
					routes.add(XR.route(pre_pick));
					groupId2vhIds.put(groupId, routes);
				}
			}
		}
		nbFixedPoint = pickupPoints.size() - rejectPoints.size();
	}
	
	/**
	 * greedy update solution
	 * choose the best-objective point for insertion.
	 */
	public void greedyUpdateSolution() {
		for (int i = 0; i < rejectPoints.size(); i++) {
			Point pickup = rejectPoints.get(i);
			if (XR.route(pickup) != Constants.NULL_POINT)
				continue;

			Point pre_pick = null;
			double best_objective = Double.MAX_VALUE;
			double bestAddUsedBuses = Integer.MAX_VALUE;
			
			int groupId = point2groupId.get(pickup);
			if(groupId > 0){
				HashSet<Integer> routes = groupId2vhIds.get(groupId);
				for(int r : routes){
					if(vhId2resolved.get(vehicles[r-1].getId()) == 1)
						continue;
					Point st = XR.getStartingPointOfRoute(r);
					for (Point p = st; p != XR.getTerminatingPointOfRoute(r); p = XR
							.next(p)) {
						if (S.evaluateAddOnePoint(pickup, p) <= sVio
								&& capCons.evaluateAddOnePoint(pickup, p) <= capVio
								&& checkSizeBus(r, getRoadBlock(p.getID(), pickup.getID()))
								&& checkDistanceConstraint(pickup, p, st)) {
							double cost = objective.evaluateAddOnePoint(pickup, p);
							double addBuses = totalBuses.evaluateAddOnePoint(pickup, p);
							if (addBuses < bestAddUsedBuses || 
								(addBuses == bestAddUsedBuses && cost < best_objective)) {
								best_objective = cost;
								bestAddUsedBuses = addBuses;
								pre_pick = p;
							}
						}
					}
				}
				if (pre_pick != null){
					mgr.performAddOnePoint(pickup, pre_pick);
					rejectPoints.remove(pickup);
					i--;
					vhId2flag.put(vehicles[XR.route(pre_pick) - 1].getId(), Utils.UPDATED);
				}
			}
			if(pre_pick == null){
				for (int r = 1; r <= XR.getNbRoutes(); r++) {
					if(vhId2resolved.get(vehicles[r-1].getId()) == 1)
						continue;
					Point st = XR.getStartingPointOfRoute(r);
					for (Point p = st; p != XR.getTerminatingPointOfRoute(r); p = XR
							.next(p)) {
						if (S.evaluateAddOnePoint(pickup, p) <= sVio
								&& capCons.evaluateAddOnePoint(pickup, p) <= capVio
								&& checkSizeBus(r, getRoadBlock(p.getID(), pickup.getID()))
								&& checkDistanceConstraint(pickup, p, st)) {
							double cost = objective.evaluateAddOnePoint(pickup, p);
							double addBuses = totalBuses.evaluateAddOnePoint(pickup, p);
							if (addBuses < bestAddUsedBuses || 
								(addBuses == bestAddUsedBuses && cost < best_objective)) {
								best_objective = cost;
								bestAddUsedBuses = addBuses;
								pre_pick = p;
							}
						}
					}
				}
				if (pre_pick != null){
					mgr.performAddOnePoint(pickup, pre_pick);
					rejectPoints.remove(pickup);
					i--;
					vhId2flag.put(vehicles[XR.route(pre_pick) - 1].getId(), Utils.UPDATED);
				}
			}
		}
	}
	
	public boolean isRemoveRoute(int vhId){
		if(input.getCurrentSolution() == null)
			return false;
		SchoolBusRoutingSolution currentSolution = input.getCurrentSolution();
		BusRoute[] busRoutes = currentSolution.getBusRoutes();
		for(int i = 0; i < busRoutes.length; i++)
			if(busRoutes[i].getVehicle().getId() == vhId)
				return true;
		return false;
	}
	
	public void printSolution(String dir, String note) {
		double[] per = new double[XR.getNbRoutes() + 1];
		int[] newCapList = new int[XR.getNbRoutes() + 1];
		HashMap<Integer, String> hs2bus = new HashMap<Integer, String>();
		String fo = dir + "solution-" + note + ".xlsx";
		XSSFWorkbook workbook = new XSSFWorkbook();

		XSSFSheet reqSheet = workbook.createSheet("route");
		Row row = reqSheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("BusID");
		cell = row.createCell(1);
		cell.setCellValue("Diem don");
		cell = row.createCell(2);
		cell.setCellValue("Thoi gian don");
		cell = row.createCell(3);
		cell.setCellValue("So HS");
		cell = row.createCell(4);
		cell.setCellValue("Danh Sach HS");
		cell = row.createCell(5);
		cell.setCellValue("Lat");
		cell = row.createCell(6);
		cell.setCellValue("Lng");
		cell = row.createCell(7);
		cell.setCellValue("Thoi gian di thang");
		cell = row.createCell(8);
		cell.setCellValue("Thoi gian ngoi tren xe chieu di");
		cell = row.createCell(9);
		cell.setCellValue("Diem tra");
		cell = row.createCell(10);
		cell.setCellValue("Thoi gian tra");
		cell = row.createCell(11);
		cell.setCellValue("Thoi gian ngoi tren xe chieu ve");

		int i = 1;
		for (int r = 1; r <= XR.getNbRoutes(); r++) {
			int c = 0;
			Point st = XR.getStartingPointOfRoute(r);
			Point en = XR.getTerminatingPointOfRoute(r);
			Point pre_back = en;
			int endtime = 59400;
			for (Point p = XR.getStartingPointOfRoute(r); p != XR
					.getTerminatingPointOfRoute(r); p = XR.next(p)) {
				row = reqSheet.createRow(i);
				String str = "Bus-" + r;
				cell = row.createCell(0);
				cell.setCellValue(str);
				ArrayList<Integer> cus = point2nameList.get(p);
				for (int k = 0; k < cus.size(); k++)
					hs2bus.put(cus.get(k), str);
				cell = row.createCell(1);
				cell.setCellValue(p.getID());
				double t = eat.getEarliestArrivalTime(p);
				int h = (int) (t / 3600);
				int m = (int) ((t - h * 3600) / 60);
				int s = (int) (t - h * 3600 - m * 60);
				str = h + ":" + m + ":" + s;
				cell = row.createCell(2);
				cell.setCellValue(str);
				cell = row.createCell(3);
				cell.setCellValue(cus.size());
				cell = row.createCell(4);
				cell.setCellValue(cus.toString());
				cell = row.createCell(5);
				cell.setCellValue(p.getX());
				cell = row.createCell(6);
				cell.setCellValue(p.getY());
				c += cus.size();
				cell = row.createCell(7);
				cell.setCellValue(awmT.getWeight(p, st));
				cell = row.createCell(8);
				cell.setCellValue(eat.getEarliestArrivalTime(XR
						.getTerminatingPointOfRoute(r)) - t - 120);
				cell = row.createCell(9);
				cell.setCellValue(pre_back.getID());

				double t2 = endtime + awmT.getWeight(en, pre_back);
				int h2 = (int) (t2 / 3600);
				int m2 = (int) ((t2 - h2 * 3600) / 60);
				int s2 = (int) (t2 - h2 * 3600 - m2 * 60);
				str = h2 + ":" + m2 + ":" + s2;
				cell = row.createCell(10);
				cell.setCellValue(str);
				cell = row.createCell(11);
				cell.setCellValue(awmT.getWeight(en, pre_back));
				endtime = (int) (endtime + awmT.getWeight(en, pre_back) + 120);
				en = pre_back;
				pre_back = XR.prev(pre_back);

				i++;
			}
			row = reqSheet.createRow(i);
			String str = "Bus-" + r;
			cell = row.createCell(0);
			cell.setCellValue(str);
			cell = row.createCell(1);
			cell.setCellValue(XR.getTerminatingPointOfRoute(r).getID());

			double t = eat.getEarliestArrivalTime(XR
					.getTerminatingPointOfRoute(r));
			int h = (int) (t / 3600);
			int m = (int) ((t - h * 3600) / 60);
			int s = (int) (t - h * 3600 - m * 60);
			str = h + ":" + m + ":" + s;
			cell = row.createCell(2);
			cell.setCellValue(str);
			c += point2nameList.get(XR.getTerminatingPointOfRoute(r)).size();
			String inc = "";
			if (c < 16) {
				inc = c + "/15";
				per[r] = c * 100 / 15;
				newCapList[r] = 15;
			} else if (c >= 16 && c < 29) {
				inc = c + "/28";
				per[r] = c * 100 / 28;
				newCapList[r] = 28;
			} else {
				inc = c + "/44";
				per[r] = c * 100 / 44;
				newCapList[r] = 44;
			}

			cell = row.createCell(3);
			cell.setCellValue(inc);
			cell = row.createCell(5);
			cell.setCellValue(XR.getTerminatingPointOfRoute(r).getX());
			cell = row.createCell(6);
			cell.setCellValue(XR.getTerminatingPointOfRoute(r).getY());
			cell = row.createCell(7);
			cell.setCellValue(awmT.getWeight(XR.getTerminatingPointOfRoute(r),
					st));
			cell = row.createCell(8);
			cell.setCellValue(eat.getEarliestArrivalTime(XR
					.getTerminatingPointOfRoute(r)) - t - 120);
			cell = row.createCell(9);
			cell.setCellValue(pre_back.getID());
			cell = row.createCell(10);
			cell.setCellValue(endtime + awmT.getWeight(en, pre_back));
			cell = row.createCell(11);
			cell.setCellValue(awmT.getWeight(en, pre_back));
			endtime = (int) (endtime + awmT.getWeight(en, pre_back) + 120);

			i++;
		}

		XSSFSheet rejectSheet = workbook.createSheet("rejectPoints");
		row = rejectSheet.createRow(0);
		cell = row.createCell(0);
		cell.setCellValue("Diem");
		cell = row.createCell(1);
		cell.setCellValue("So HS");
		cell = row.createCell(2);
		cell.setCellValue("Danh Sach HS");

		for (int k = 0; k < rejectPoints.size(); k++) {
			Point p = rejectPoints.get(k);
			row = rejectSheet.createRow(k + 1);
			cell = row.createCell(0);
			cell.setCellValue(p.getID());
			cell = row.createCell(1);
			cell.setCellValue(point2nameList.get(p).size());
			cell = row.createCell(2);
			cell.setCellValue(point2nameList.get(p).toString());
		}

		XSSFSheet stSheet = workbook.createSheet("Statistic");
		row = stSheet.createRow(0);
		cell = row.createCell(0);
		cell.setCellValue("BusID");
		cell = row.createCell(1);
		cell.setCellValue("Loai xe");
		cell = row.createCell(2);
		cell.setCellValue("Ti le cho (%)");
		cell = row.createCell(3);
		cell.setCellValue("So diem dung");
		cell = row.createCell(4);
		cell.setCellValue("Thoi gian di chuyen");

		for (int r = 1; r <= XR.getNbRoutes(); r++) {
			String str = "Bus-" + r;
			row = stSheet.createRow(r);
			cell = row.createCell(0);
			cell.setCellValue(str);
			cell = row.createCell(1);
			cell.setCellValue(newCapList[r]);
			cell = row.createCell(2);
			cell.setCellValue(per[r]);
			cell = row.createCell(3);
			cell.setCellValue(XR.index(XR.getTerminatingPointOfRoute(r)));
			double t = eat.getEarliestArrivalTime(XR
					.getTerminatingPointOfRoute(r))
					- eat.getEarliestArrivalTime(XR.next(XR
							.getStartingPointOfRoute(r)));
			int h = (int) (t / 3600);
			int m = (int) ((t - h * 3600) / 60);
			int s = (int) (t - h * 3600 - m * 60);
			str = h + ":" + m + ":" + s;
			cell = row.createCell(4);
			cell.setCellValue(str);
		}

		XSSFSheet req2busSheet = workbook.createSheet("request2busId");
		row = req2busSheet.createRow(0);
		cell = row.createCell(0);
		cell.setCellValue("Ma HS");
		cell = row.createCell(1);
		cell.setCellValue("BusID");

		int k = 1;
		for (int key : hs2bus.keySet()) {
			row = req2busSheet.createRow(k);
			cell = row.createCell(0);
			cell.setCellValue(key);
			cell = row.createCell(1);
			cell.setCellValue(hs2bus.get(key));
			k++;
		}
		
		try{
			FileOutputStream outputStream = new FileOutputStream(fo);
		    workbook.write(outputStream);
		    workbook.close();
		    outputStream.close();
		} catch (IOException | EncryptedDocumentException ex) {
		    ex.printStackTrace();
		}
	}
	
	public void sortCusBySibling(){
		point2cusIndex = new HashMap<Integer, ArrayList<Integer>>();
		for(int i = 0; i < requests.length; i++){
			int pointId = requests[i].getPickupLocationId();
			if(point2cusIndex.get(pointId) == null){
				ArrayList<Integer> siblingList = new ArrayList<Integer>();
				siblingList.add(i);
				point2cusIndex.put(pointId, siblingList);
			}
			else{
				ArrayList<Integer> siblingList = point2cusIndex.get(pointId);
				siblingList.add(i);
				point2cusIndex.put(pointId, siblingList);
			}
		}
		
		reqsMarked = new int[requests.length];
		for(int i = 0; i < requests.length; i++)
			reqsMarked[i] = 0;
	}
	
	public void biningRequests(){
		HashMap<Integer, ArrayList<Integer>> point2bining = new HashMap<Integer, ArrayList<Integer>>();
		HashMap<Integer, ArrayList<Integer>> point2clusters = new HashMap<Integer, ArrayList<Integer>>();
		HashMap<String, Integer> siblingCode2nb = new HashMap<String, Integer>();
		HashMap<String, Integer> siblingCode2pointId = new HashMap<String, Integer>();
		
		for(int i = 0; i < requests.length; i++){
			if(requests[i].getSiblingCode() == null
				|| requests[i].getSiblingCode().equals("")){
				continue;
			}
			if(siblingCode2nb.get(requests[i].getSiblingCode()) == null){
				String siblingCode = requests[i].getSiblingCode();
				siblingCode2nb.put(siblingCode, 1);
				siblingCode2pointId.put(siblingCode, requests[i].getPickupLocationId());
			}
			else{
				String siblingCode = requests[i].getSiblingCode();
				int nbSibling = siblingCode2nb.get(siblingCode);
				siblingCode2nb.put(siblingCode, nbSibling+1);
				if(requests[i].getPickupLocationId() != siblingCode2pointId.get(siblingCode))
					System.out.println(" i = " + i + ", pick location of i = " + requests[i].getPickupLocationId()
							+ "pick location pre = " + siblingCode2pointId.get(siblingCode)
							+ ", siblingCode = " + siblingCode);
			}
		}
		HashMap<String, Integer> siblingCode2marks = new HashMap<String, Integer>();
		for(String key : siblingCode2nb.keySet())
			siblingCode2marks.put(key, 0);

		for(int i = 0; i < requests.length; i++){
			int pointId = requests[i].getPickupLocationId();
			String siblingCode = requests[i].getSiblingCode();
			if(point2clusters.get(pointId) == null){
				ArrayList<Integer> clusters = new ArrayList<Integer>();
				int nbSibling = 1;
				if(siblingCode2nb.get(siblingCode) != null){
					nbSibling = siblingCode2nb.get(siblingCode);
					siblingCode2marks.put(siblingCode, 1);
				}
				clusters.add(nbSibling);
				point2clusters.put(pointId, clusters);
			}
			else{
				ArrayList<Integer> clusters = point2clusters.get(pointId);

				if(siblingCode2marks.get(siblingCode) != null
					&& siblingCode2marks.get(siblingCode) == 0){
					int nbSibling = siblingCode2nb.get(siblingCode);
					siblingCode2marks.put(siblingCode, 1);
					clusters.add(nbSibling);
					point2clusters.put(pointId, clusters);
				}
				else if(siblingCode2marks.get(siblingCode) == null){
					clusters.add(1);
					point2clusters.put(pointId, clusters);
				}
			}
		}
//		int nbPoints = 0;
//		int sum = 0;
//		for(Integer key : point2clusters.keySet()){
//			System.out.println("point " + key + ", cluster: " + point2clusters.get(key).toString());
//			nbPoints++;
//			ArrayList<Integer> clusters = point2clusters.get(key);
//			for(int i = 0; i < clusters.size(); i++)
//				sum+= clusters.get(i);
//		}
//		System.out.println("nb points = " + nbPoints);
//		System.out.println("sum = " + sum);
		Utils u = new Utils();
		for(Integer p : point2clusters.keySet()){
			ArrayList<Integer> cluster = point2clusters.get(p);
			int max_cap = getRoadBlock(input.getShoolPointId(), p);
			if(max_cap == Utils.CAP_FLEXIBILITY)
				max_cap = Utils.CAP_45;
			else if(max_cap == Utils.CAP_LEQ_29)
				max_cap = Utils.CAP_29;
			else
				max_cap = Utils.CAP_16;
			int n = cluster.size();
			int minBin = n;
			int[] bin = new int[n+1];
			for(int i = 1; i <= n; i++)
				bin[i] = 0;
			ArrayList<ArrayList<Integer>> permutations = u.getPermutations(n);
			for(int i = 0; i < permutations.size(); i++){
				ArrayList<Integer> per = permutations.get(i);
				for(int j = 0; j < per.size(); j++)
					bin[per.get(j)] += cluster.get(j);
				boolean valid = true;
				int nbBin = 0;
				for(int j = 0; j < bin.length; j++){
					if(bin[j] >= max_cap){
						valid = false;
						break;
					}
					if(bin[j] > 0)
						nbBin++;
				}
				if(!valid)
					continue;
				if(nbBin <= minBin){
					minBin = nbBin;
					point2bining.put(p, per);
				}
			}
		}
		for(Integer key : point2bining.keySet()){
			System.out.println("point " + key + ", cluster: " + point2bining.get(key).toString());
		}
	}
	
	public void mapData(){
		this.rejectPoints = new ArrayList<Point>();
		this.nbFixedPoint = 0;
		
		vehicles = input.getVehicles();
		requests = input.getRequests();
		this.nVehicle = vehicles.length;
		
		this.points = new ArrayList<Point>();
    	this.startPoints = new ArrayList<Point>();
    	this.stopPoints = new ArrayList<Point>();
    	this.pickupPoints = new ArrayList<Point>();
    	//this.deliveryPoints = new ArrayList<Point>();
    	this.point2nameList = new HashMap<Point, ArrayList<Integer>>();
    	this.point2idReqList = new HashMap<Point, ArrayList<Integer>>();
    	this.point2groupId = new HashMap<Point, Integer>();
    	this.groupId2vhIds = new HashMap<Integer, HashSet<Integer>>();
    	this.groupId2points = new HashMap<Integer, ArrayList<Point>>();
    	this.earliestAllowedArrivalTime = new HashMap<Point, Integer>();
    	this.serviceDuration = new HashMap<Point, Integer>();
    	this.lastestAllowedArrivalTime = new HashMap<Point, Integer>();

    	this.capList = new double[nVehicle + 1];
    	
    	//get distance matrix
		//HashSet<Integer> s_pointId = getDistanceMatrix();
//    	getDistanceMatrix();
//		if(s_pointId == null || s_pointId.size() == 0){
//			LOGGER.log(Level.INFO, "Cannot get s_point id");
//			System.exit(0);
//		}
    	//get distance matrix
		HashSet<Integer> s_pointId = new HashSet<Integer>();
		for (int i = 0; i < input.getDistances().length; i++) {
			DistanceElement e = input.getDistances()[i];
			int srcId = e.getSrcCode();
			int destId = e.getDestCode();
			s_pointId.add(srcId);
			s_pointId.add(destId);
		}
    	mPointId2Index = new HashMap<Integer, Integer>();
		int idx = -1;
		for (int id : s_pointId) {
			idx++;
			mPointId2Index.put(id, idx);
		}
		distance = new double[s_pointId.size()][s_pointId.size()];
		travelTime = new double[s_pointId.size()][s_pointId.size()];
		roadBlock = new int[s_pointId.size()][s_pointId.size()];
		srcdest = new ArrayList<String>();
		
		for (int i = 0; i < input.getDistances().length; i++) {
			DistanceElement e = input.getDistances()[i];
			int srcId = e.getSrcCode();
			int destId = e.getDestCode();
			int is = mPointId2Index.get(srcId);
			int id = mPointId2Index.get(destId);
			double d = e.getDistance();
			double scale = (double)input.getConfigParams().getTimeScale()/100;
			//scale = scale + 0.30;
			distance[is][id] = d + scale * d;
			double t = e.getTravelTime();
			travelTime[is][id] = t + scale * t;
			int r = e.getRoadBlock();
			roadBlock[is][id] = r;
			String sd = srcId + "-" + destId;
			srcdest.add(sd);
			
//			if((srcId != destId) && (d <= 0 || t <= 0)){
//				 System.out.println(name() + "::travelTime or distance, src " + srcId +
//						 "-dest " + destId + " INVALID");
//				 LOGGER.log(Level.WARNING, name() + "::travelTime or distance, src " + srcId +
//						 "-dest " + destId + "  INVALID");
//			}
		}
//		for (int src : s_pointId) {
//			for (int dest : s_pointId) {
//				String sd = src + "-" + dest;
//				if(src != dest && !srcdest.contains(sd)){
//					 System.out.println(name() + "::cannot get information, src " + src +
//							 "-dest " + dest);
//					 LOGGER.log(Level.WARNING, name() + "::cannot get information, src " + src +
//							 "-dest " + dest);
//				}
//			}
//		}
		
		//get map information
    	double lat_target = input.getLat_school();
    	double long_target = input.getLong_school();
    	
    	//get vehicles and school point information
    	for(int i = 1; i <= this.nVehicle; i++){
    		int idStartPoint = input.getShoolPointId();
			Point sp = new Point(idStartPoint, lat_target, long_target, Constants.START_POINT, 0);
			startPoints.add(sp);
			points.add(sp);
			this.point2nameList.put(sp, new ArrayList<Integer>());
			this.point2idReqList.put(sp, new ArrayList<Integer>());
			int idStopPoint = input.getShoolPointId();
			Point ep = new Point(idStopPoint, lat_target, long_target, Constants.STOP_POINT, 0);
			stopPoints.add(ep);
			points.add(ep);
			this.point2nameList.put(ep, new ArrayList<Integer>());
			this.point2idReqList.put(ep, new ArrayList<Integer>());
			earliestAllowedArrivalTime.put(sp, 0);
			serviceDuration.put(sp, 0);
			lastestAllowedArrivalTime.put(sp, input.getConfigParams()
					.getLatestDateTimeDeliveryAtSchool());
			
			earliestAllowedArrivalTime.put(ep, 0);
			serviceDuration.put(ep, 0);
			lastestAllowedArrivalTime.put(ep, input.getConfigParams()
					.getLatestDateTimeDeliveryAtSchool());
			capList[i] = vehicles[i-1].getCap();
			
		}
    	
    	//get request information
//    	this.siblingCode2nb = new HashMap<String, Integer>();
//    	for(int i = 0; i < requests.length; i++){
//    		SchoolBusRequest r = requests[i];
//    		if(r.getSiblingCode() != null){
//	    		if(siblingCode2nb.get(r.getSiblingCode()) == null)
//	    			siblingCode2nb.put(r.getSiblingCode(), 1);
//	    		else{
//	    			int nb = siblingCode2nb.get(r.getSiblingCode()) + 1;
//	    			siblingCode2nb.put(r.getSiblingCode(), nb);
//	    		}
//    		}
//    	}
    	
    	sortCusBySibling();
    	
    	for(int i = 0; i < requests.length; i++){
    		if(reqsMarked[i] == 1)
    			continue;
    		SchoolBusRequest r = requests[i];
    		//Point pickup = getMinCusElementInList(r.getPickupLocationId(), pickupPoints);
    		ArrayList<Point> currPoints = getPointById(r.getPickupLocationId(), pickupPoints);
			if(currPoints.size() == 0){
				Point pickup = new Point(r.getPickupLocationId(), r.getLat_pickup(),
						r.getLong_pickup(), Constants.CLIENT_POINT, 1);
	    		points.add(pickup);
	    		pickupPoints.add(pickup);
	    		
	    		int groupId = r.getGroupId();
	    		point2groupId.put(pickup, groupId);
	    		if(groupId > 0){
	    			ArrayList<Point> pInGroup = groupId2points.get(groupId);
	    			if(pInGroup == null)
	    				pInGroup = new ArrayList<Point>();
	    			pInGroup.add(pickup);
	    			groupId2points.put(groupId, pInGroup);
	    			HashSet<Integer> routes = new HashSet<Integer>();
	    			groupId2vhIds.put(groupId, routes);
	    		}
	    		
	    		ArrayList<Integer> cus = new ArrayList<Integer>();
	    		ArrayList<Integer> reqId = new ArrayList<Integer>();
	    		ArrayList<Integer> siblingList = getSiblingIndexByCode(r.getSiblingCode(),
						point2cusIndex.get(r.getPickupLocationId()));
	    		for(int k = 0; k < siblingList.size(); k++){
	    			cus.add(requests[siblingList.get(k)].getIdPerson());
	    			reqId.add(requests[siblingList.get(k)].getId());
	    			reqsMarked[siblingList.get(k)] = 1;
	    		}
	    		point2nameList.put(pickup, cus);
	    		point2idReqList.put(pickup, reqId);
	    		earliestAllowedArrivalTime.put(pickup, input.getConfigParams()
	    				.getEarliestDateTimePickupAtPoint());
				serviceDuration.put(pickup, 120);
				lastestAllowedArrivalTime.put(pickup, input.getConfigParams()
						.getLatestDateTimeDeliveryAtSchool());
			}
			else{
				boolean isAdded = false;
				for(int j = 0; j < currPoints.size(); j++){
					ArrayList<Integer> cus = point2nameList.get(currPoints.get(j));
					ArrayList<Integer> reqId = point2idReqList.get(currPoints.get(j));
					int max_cap = getRoadBlock(input.getShoolPointId(), currPoints.get(j).getID());
					if(max_cap == Utils.CAP_FLEXIBILITY)
						max_cap = Utils.CAP_45;
					else if(max_cap == Utils.CAP_LEQ_29)
						max_cap = Utils.CAP_29;
					else
						max_cap = Utils.CAP_16;
					//max capacity is 43
					ArrayList<Integer> siblingList = getSiblingIndexByCode(r.getSiblingCode(),
							point2cusIndex.get(r.getPickupLocationId()));
					int nbAdded = siblingList.size() > 1 ? siblingList.size() : 1;
					if(cus.size() + nbAdded <= max_cap){
			    		for(int k = 0; k < siblingList.size(); k++){
			    			cus.add(requests[siblingList.get(k)].getIdPerson());
			    			reqId.add(requests[siblingList.get(k)].getId());
			    			reqsMarked[siblingList.get(k)] = 1;
			    		}
			    		point2nameList.put(currPoints.get(j), cus);
			    		point2idReqList.put(currPoints.get(j), reqId);
			    		isAdded = true;
			    		break;
					}
				}
				if(isAdded == false){
					int varIndex = getMaxVarIndex(currPoints) + 1;
					Point pickup = new Point(r.getPickupLocationId(), r.getLat_pickup(), r.getLong_pickup(),
							Constants.CLIENT_POINT, varIndex);
		    		points.add(pickup);
		    		pickupPoints.add(pickup);
		    		int groupId = r.getGroupId();
		    		point2groupId.put(pickup, groupId);
		    		if(groupId > 0){
		    			ArrayList<Point> pInGroup = groupId2points.get(groupId);
		    			if(pInGroup == null)
		    				pInGroup = new ArrayList<Point>();
		    			pInGroup.add(pickup);
		    			groupId2points.put(groupId, pInGroup);
		    			HashSet<Integer> routes = new HashSet<Integer>();
		    			groupId2vhIds.put(groupId, routes);
		    		}
		    		ArrayList<Integer> cus = new ArrayList<Integer>();
		    		ArrayList<Integer> reqId = new ArrayList<Integer>();
		    		ArrayList<Integer> siblingList = getSiblingIndexByCode(r.getSiblingCode(),
							point2cusIndex.get(r.getPickupLocationId()));
		    		for(int k = 0; k < siblingList.size(); k++){
		    			cus.add(requests[siblingList.get(k)].getIdPerson());
		    			reqId.add(requests[siblingList.get(k)].getId());
		    			reqsMarked[siblingList.get(k)] = 1;
		    		}
		    		point2nameList.put(pickup, cus);
		    		point2idReqList.put(pickup, reqId);
		    		earliestAllowedArrivalTime.put(pickup, input.getConfigParams()
		    				.getEarliestDateTimePickupAtPoint());
					serviceDuration.put(pickup, r.getServicePickupDuration());
					lastestAllowedArrivalTime.put(pickup, input.getConfigParams()
							.getLatestDateTimeDeliveryAtSchool());
				}
			}
			reqsMarked[i] = 1;
    	}
    	this.nRequest = pickupPoints.size();
    	awmT = new ArcWeightsManager(points);
    	awmD = new ArcWeightsManager(points);
		nwm = new NodeWeightsManager(points);
		
//		GoogleMapsQuery G = new GoogleMapsQuery();
//		double scale = (double)input.getConfigParams().getTimeScale()/100;
//		String date = "2019-07-29 07:00:00";
//        long departure_time = DateTimeUtils.dateTime2Int(date);
        
		double max_dist = Double.MIN_VALUE;
		String fileOut = this.fileName + "-missingData.json";
		try{
			FileOutputStream fo = new FileOutputStream(fileOut);
			PrintWriter out = new PrintWriter(fo);
			out.close();
		}catch(Exception e){
			System.out.println(e);
		}
		HashMap<String, Integer> isCheck = new HashMap<String, Integer>();
		boolean isMiss = false;
		for (int i = 0; i < points.size(); i++){
			for (int j = 0; j < points.size(); j++){
//				if(points.get(i).getID() == 1833 && points.get(j).getID() == 2434)
//					System.out.println("ffdgdf");
				double tmp_cost = getTravelTime(points.get(i).getID(),
						points.get(j).getID());
					
				double dis_cost = getDistance(points.get(i).getID(),
						points.get(j).getID());
				
				int src = points.get(i).getID();
				int dest = points.get(j).getID();
				
				String sd = src + "-" + dest;
				if((src != dest && !srcdest.contains(sd)
						&& (isCheck.get(sd) == null || isCheck.get(sd) == 0))
					|| (src != dest && (tmp_cost < 0 || dis_cost < 0)
					&& (isCheck.get(sd) == null || isCheck.get(sd) == 0))){
					isCheck.put(sd, 1);
					isMiss = true;
					try{
						FileOutputStream fo = new FileOutputStream(fileOut, true);
						PrintWriter out = new PrintWriter(fo);
						out.println(sd);
						out.close();
					}catch(Exception e){
						
					}
				}
				
				
				awmD.setWeight(points.get(i), points.get(j), dis_cost);
				//double time = (double)((dis_cost*3600)/28000);
				awmT.setWeight(points.get(i), points.get(j), tmp_cost);
				//max_dist = time > max_dist ? time : max_dist;
				max_dist = tmp_cost > max_dist ? tmp_cost : max_dist;
			}
			nwm.setWeight(points.get(i), point2nameList.get(points.get(i)).size());
		}
		if(isMiss){
			System.out.println(name() + "::travel time info, check missingDistance file please!");
			LOGGER.log(Level.WARNING, name() + "::travel time info, check missingDistance file please!");
			closeLog();
			System.exit(0);
		}
		MAX_DISTANCE = max_dist;
	}
	
	public void mapDataForUpdating(){
		this.rejectPoints = new ArrayList<Point>();
		this.nbFixedPoint = 0;
		
		vehicles = input.getVehicles();
		requests = input.getRequests();
		this.nVehicle = vehicles.length;
		
		this.points = new ArrayList<Point>();
    	this.startPoints = new ArrayList<Point>();
    	this.stopPoints = new ArrayList<Point>();
    	this.pickupPoints = new ArrayList<Point>();
    	//this.deliveryPoints = new ArrayList<Point>();
    	this.point2nameList = new HashMap<Point, ArrayList<Integer>>();
    	this.point2idReqList = new HashMap<Point, ArrayList<Integer>>();
    	this.point2groupId = new HashMap<Point, Integer>();
    	this.groupId2vhIds = new HashMap<Integer, HashSet<Integer>>();
    	this.groupId2points = new HashMap<Integer, ArrayList<Point>>();
    	this.earliestAllowedArrivalTime = new HashMap<Point, Integer>();
    	this.serviceDuration = new HashMap<Point, Integer>();
    	this.lastestAllowedArrivalTime = new HashMap<Point, Integer>();

    	this.capList = new double[nVehicle + 1];
    	
    	//get distance matrix
		//HashSet<Integer> s_pointId = getDistanceMatrix();
//    	getDistanceMatrix();
//		if(s_pointId == null || s_pointId.size() == 0){
//			LOGGER.log(Level.INFO, "Cannot get s_point id");
//			System.exit(0);
//		}
    	//get distance matrix
		HashSet<Integer> s_pointId = new HashSet<Integer>();
		for (int i = 0; i < input.getDistances().length; i++) {
			DistanceElement e = input.getDistances()[i];
			int srcId = e.getSrcCode();
			int destId = e.getDestCode();
			s_pointId.add(srcId);
			s_pointId.add(destId);
		}
    	mPointId2Index = new HashMap<Integer, Integer>();
		int idx = -1;
		for (int id : s_pointId) {
			idx++;
			mPointId2Index.put(id, idx);
		}
		distance = new double[s_pointId.size()][s_pointId.size()];
		travelTime = new double[s_pointId.size()][s_pointId.size()];
		roadBlock = new int[s_pointId.size()][s_pointId.size()];
		srcdest = new ArrayList<String>();
		
		for (int i = 0; i < input.getDistances().length; i++) {
			DistanceElement e = input.getDistances()[i];
			int srcId = e.getSrcCode();
			int destId = e.getDestCode();
			int is = mPointId2Index.get(srcId);
			int id = mPointId2Index.get(destId);
			double d = e.getDistance();
			double scale = (double)input.getConfigParams().getTimeScale()/100;
			//scale = scale + 0.30;
			distance[is][id] = d + scale * d;
			double t = e.getTravelTime();
			travelTime[is][id] = t + scale * t;
			int r = e.getRoadBlock();
			roadBlock[is][id] = r;
			String sd = srcId + "-" + destId;
			srcdest.add(sd);
			
//			if((srcId != destId) && (d <= 0 || t <= 0)){
//				 System.out.println(name() + "::travelTime or distance, src " + srcId +
//						 "-dest " + destId + " INVALID");
//				 LOGGER.log(Level.WARNING, name() + "::travelTime or distance, src " + srcId +
//						 "-dest " + destId + "  INVALID");
//			}
		}
//		for (int src : s_pointId) {
//			for (int dest : s_pointId) {
//				String sd = src + "-" + dest;
//				if(src != dest && !srcdest.contains(sd)){
//					 System.out.println(name() + "::cannot get information, src " + src +
//							 "-dest " + dest);
//					 LOGGER.log(Level.WARNING, name() + "::cannot get information, src " + src +
//							 "-dest " + dest);
//				}
//			}
//		}
		
		//get map information
    	double lat_target = input.getLat_school();
    	double long_target = input.getLong_school();
    	
    	//get vehicles and school point information
    	for(int i = 1; i <= this.nVehicle; i++){
    		int idStartPoint = input.getShoolPointId();
			Point sp = new Point(idStartPoint, lat_target, long_target, Constants.START_POINT, 0);
			startPoints.add(sp);
			points.add(sp);
			this.point2nameList.put(sp, new ArrayList<Integer>());
			this.point2idReqList.put(sp, new ArrayList<Integer>());
			int idStopPoint = input.getShoolPointId();
			Point ep = new Point(idStopPoint, lat_target, long_target, Constants.STOP_POINT, 0);
			stopPoints.add(ep);
			points.add(ep);
			this.point2nameList.put(ep, new ArrayList<Integer>());
			this.point2idReqList.put(ep, new ArrayList<Integer>());
			earliestAllowedArrivalTime.put(sp, 0);
			serviceDuration.put(sp, 0);
			lastestAllowedArrivalTime.put(sp, input.getConfigParams()
					.getLatestDateTimeDeliveryAtSchool());
			
			earliestAllowedArrivalTime.put(ep, 0);
			serviceDuration.put(ep, 0);
			lastestAllowedArrivalTime.put(ep, input.getConfigParams()
					.getLatestDateTimeDeliveryAtSchool());
			capList[i] = vehicles[i-1].getCap();
			
		}
    	
    	//get request information
//    	this.siblingCode2nb = new HashMap<String, Integer>();
//    	for(int i = 0; i < requests.length; i++){
//    		SchoolBusRequest r = requests[i];
//    		if(r.getSiblingCode() != null){
//	    		if(siblingCode2nb.get(r.getSiblingCode()) == null)
//	    			siblingCode2nb.put(r.getSiblingCode(), 1);
//	    		else{
//	    			int nb = siblingCode2nb.get(r.getSiblingCode()) + 1;
//	    			siblingCode2nb.put(r.getSiblingCode(), nb);
//	    		}
//    		}
//    	}
    	
    	sortCusBySibling();
    	
    	for(int i = 0; i < requests.length; i++){
    		SchoolBusRequest r = requests[i];
    		if(r.getVarIndex() < 1)
    			continue;
    		
    		//Point pickup = getMinCusElementInList(r.getPickupLocationId(), pickupPoints);
    		Point currPoint = getPointById(r.getPickupLocationId(), r.getVarIndex(), pickupPoints);
			if(currPoint == null){
				Point pickup = new Point(r.getPickupLocationId(), r.getLat_pickup(),
						r.getLong_pickup(), Constants.CLIENT_POINT, r.getVarIndex());
	    		points.add(pickup);
	    		pickupPoints.add(pickup);
	    		
	    		int groupId = r.getGroupId();
	    		point2groupId.put(pickup, groupId);
	    		if(groupId > 0){
	    			ArrayList<Point> pInGroup = groupId2points.get(groupId);
	    			if(pInGroup == null)
	    				pInGroup = new ArrayList<Point>();
	    			pInGroup.add(pickup);
	    			groupId2points.put(groupId, pInGroup);
	    			HashSet<Integer> routes = new HashSet<Integer>();
	    			groupId2vhIds.put(groupId, routes);
	    		}
	    		
	    		ArrayList<Integer> cus = new ArrayList<Integer>();
	    		cus.add(r.getIdPerson());
	    		ArrayList<Integer> reqId = new ArrayList<Integer>();
	    		reqId.add(r.getId());

	    		point2nameList.put(pickup, cus);
	    		point2idReqList.put(pickup, reqId);
	    		earliestAllowedArrivalTime.put(pickup, input.getConfigParams()
	    				.getEarliestDateTimePickupAtPoint());
				serviceDuration.put(pickup, 120);
				lastestAllowedArrivalTime.put(pickup, input.getConfigParams()
						.getLatestDateTimeDeliveryAtSchool());
			}
			else{
				ArrayList<Integer> cus = point2nameList.get(currPoint);
				ArrayList<Integer> reqId = point2idReqList.get(currPoint);
				cus.add(r.getIdPerson());
    			reqId.add(r.getId());
    			point2nameList.put(currPoint, cus);
	    		point2idReqList.put(currPoint, reqId);
				
			}
    	}
    	this.nRequest = pickupPoints.size();
    	awmT = new ArcWeightsManager(points);
    	awmD = new ArcWeightsManager(points);
		nwm = new NodeWeightsManager(points);
		
//		GoogleMapsQuery G = new GoogleMapsQuery();
//		double scale = (double)input.getConfigParams().getTimeScale()/100;
//		String date = "2019-07-29 07:00:00";
//        long departure_time = DateTimeUtils.dateTime2Int(date);
        
		double max_dist = Double.MIN_VALUE;
		String fileOut = this.fileName + "-missingData.json";
		try{
			FileOutputStream fo = new FileOutputStream(fileOut);
			PrintWriter out = new PrintWriter(fo);
			out.close();
		}catch(Exception e){
			System.out.println(e);
		}
		HashMap<String, Integer> isCheck = new HashMap<String, Integer>();
		boolean isMiss = false;
		for (int i = 0; i < points.size(); i++){
			for (int j = 0; j < points.size(); j++){
//				if(points.get(i).getID() == 1833 && points.get(j).getID() == 2434)
//					System.out.println("ffdgdf");
				double tmp_cost = getTravelTime(points.get(i).getID(),
						points.get(j).getID());
					
				double dis_cost = getDistance(points.get(i).getID(),
						points.get(j).getID());
				
				int src = points.get(i).getID();
				int dest = points.get(j).getID();
				
				String sd = src + "-" + dest;
				if((src != dest && !srcdest.contains(sd)
						&& (isCheck.get(sd) == null || isCheck.get(sd) == 0))
					|| (src != dest && (tmp_cost < 0 || dis_cost < 0)
					&& (isCheck.get(sd) == null || isCheck.get(sd) == 0))){
					isCheck.put(sd, 1);
					isMiss = true;
					try{
						FileOutputStream fo = new FileOutputStream(fileOut, true);
						PrintWriter out = new PrintWriter(fo);
						out.println(sd);
						out.close();
					}catch(Exception e){
						
					}
				}
				
				
				awmD.setWeight(points.get(i), points.get(j), dis_cost);
				//double time = (double)((dis_cost*3600)/28000);
				awmT.setWeight(points.get(i), points.get(j), tmp_cost);
				//max_dist = time > max_dist ? time : max_dist;
				max_dist = tmp_cost > max_dist ? tmp_cost : max_dist;
			}
			nwm.setWeight(points.get(i), point2nameList.get(points.get(i)).size());
		}
		if(isMiss){
			System.out.println(name() + "::travel time info, check missingDistance file please!");
			LOGGER.log(Level.WARNING, name() + "::travel time info, check missingDistance file please!");
			closeLog();
			System.exit(0);
		}
		MAX_DISTANCE = max_dist;
	}
	
	public void createMapHs2busId(){
		hs2vhId = new HashMap<Integer, Integer>();
		hs2pointId = new HashMap<Integer, Integer>();
		hs2varIndex = new HashMap<Integer, Integer>();
		hs2reqId = new HashMap<Integer, Integer>();
		if(input.getCurrentSolution() == null){
			LOGGER.log(Level.WARNING, name() + "::rebuildSolution: cannot get current solution");
			return;
		}
		SchoolBusRoutingSolution solution = input.getCurrentSolution();
		
		//sVio = solution.getStatisticInformation().getTimeViolation();
		//capVio = solution.getStatisticInformation().getCapViolation();
		
		for(int i = 0; i < solution.getBusRoutes().length; i++){
			RouteElement[] nodes = solution.getBusRoutes()[i].getNodes();
			int r = solution.getBusRoutes()[i].getVehicle().getId();
			for(int j = 0; j < nodes.length - 1; j++){
				int[] hsList = nodes[j].getHsList();
				for(int k = 0; k < hsList.length; k++){
					hs2vhId.put(hsList[k], r);
					hs2pointId.put(hsList[k], nodes[j].getLocationId());
					hs2varIndex.put(hsList[k], nodes[j].getVarIndex());
					hs2reqId.put(hsList[k], nodes[j].getHsList()[k]);
				}
			}
		}
	}
	
	public void mapDataForMoving(){
		createMapHs2busId();
		this.rejectPoints = new ArrayList<Point>();
		this.nbFixedPoint = 0;
		
		vehicles = input.getVehicles();
		requests = input.getRequests();
		this.nVehicle = vehicles.length;
		
		this.points = new ArrayList<Point>();
    	this.startPoints = new ArrayList<Point>();
    	this.stopPoints = new ArrayList<Point>();
    	this.pickupPoints = new ArrayList<Point>();
    	//this.deliveryPoints = new ArrayList<Point>();
    	this.point2nameList = new HashMap<Point, ArrayList<Integer>>();
    	this.point2idReqList = new HashMap<Point, ArrayList<Integer>>();
    	this.point2groupId = new HashMap<Point, Integer>();
    	this.groupId2vhIds = new HashMap<Integer, HashSet<Integer>>();
    	this.groupId2points = new HashMap<Integer, ArrayList<Point>>();
    	this.earliestAllowedArrivalTime = new HashMap<Point, Integer>();
    	this.serviceDuration = new HashMap<Point, Integer>();
    	this.lastestAllowedArrivalTime = new HashMap<Point, Integer>();

    	this.capList = new double[nVehicle + 1];
    	
    	//get distance matrix
		//HashSet<Integer> s_pointId = getDistanceMatrix();
//    	getDistanceMatrix();
//		if(s_pointId == null || s_pointId.size() == 0){
//			LOGGER.log(Level.INFO, "Cannot get s_point id");
//			System.exit(0);
//		}
    	//get distance matrix
		HashSet<Integer> s_pointId = new HashSet<Integer>();
		for (int i = 0; i < input.getDistances().length; i++) {
			DistanceElement e = input.getDistances()[i];
			int srcId = e.getSrcCode();
			int destId = e.getDestCode();
			s_pointId.add(srcId);
			s_pointId.add(destId);
		}
    	mPointId2Index = new HashMap<Integer, Integer>();
		int idx = -1;
		for (int id : s_pointId) {
			idx++;
			mPointId2Index.put(id, idx);
		}
		distance = new double[s_pointId.size()][s_pointId.size()];
		travelTime = new double[s_pointId.size()][s_pointId.size()];
		roadBlock = new int[s_pointId.size()][s_pointId.size()];
		srcdest = new ArrayList<String>();
		
		for (int i = 0; i < input.getDistances().length; i++) {
			DistanceElement e = input.getDistances()[i];
			int srcId = e.getSrcCode();
			int destId = e.getDestCode();
			int is = mPointId2Index.get(srcId);
			int id = mPointId2Index.get(destId);
			double d = e.getDistance();
			double scale = (double)input.getConfigParams().getTimeScale()/100;
			//scale = scale + 0.30;
			distance[is][id] = d + scale * d;
			double t = e.getTravelTime();
			travelTime[is][id] = t + scale * t;
			int r = e.getRoadBlock();
			roadBlock[is][id] = r;
			String sd = srcId + "-" + destId;
			srcdest.add(sd);
			
//			if((srcId != destId) && (d <= 0 || t <= 0)){
//				 System.out.println(name() + "::travelTime or distance, src " + srcId +
//						 "-dest " + destId + " INVALID");
//				 LOGGER.log(Level.WARNING, name() + "::travelTime or distance, src " + srcId +
//						 "-dest " + destId + "  INVALID");
//			}
		}
//		for (int src : s_pointId) {
//			for (int dest : s_pointId) {
//				String sd = src + "-" + dest;
//				if(src != dest && !srcdest.contains(sd)){
//					 System.out.println(name() + "::cannot get information, src " + src +
//							 "-dest " + dest);
//					 LOGGER.log(Level.WARNING, name() + "::cannot get information, src " + src +
//							 "-dest " + dest);
//				}
//			}
//		}
		
		//get map information
    	double lat_target = input.getLat_school();
    	double long_target = input.getLong_school();
    	
    	//get vehicles and school point information
    	for(int i = 1; i <= this.nVehicle; i++){
    		int idStartPoint = input.getShoolPointId();
			Point sp = new Point(idStartPoint, lat_target, long_target, Constants.START_POINT, 0);
			startPoints.add(sp);
			points.add(sp);
			this.point2nameList.put(sp, new ArrayList<Integer>());
			this.point2idReqList.put(sp, new ArrayList<Integer>());
			int idStopPoint = input.getShoolPointId();
			Point ep = new Point(idStopPoint, lat_target, long_target, Constants.STOP_POINT, 0);
			stopPoints.add(ep);
			points.add(ep);
			this.point2nameList.put(ep, new ArrayList<Integer>());
			this.point2idReqList.put(ep, new ArrayList<Integer>());
			earliestAllowedArrivalTime.put(sp, 0);
			serviceDuration.put(sp, 0);
			lastestAllowedArrivalTime.put(sp, input.getConfigParams()
					.getLatestDateTimeDeliveryAtSchool());
			
			earliestAllowedArrivalTime.put(ep, 0);
			serviceDuration.put(ep, 0);
			lastestAllowedArrivalTime.put(ep, input.getConfigParams()
					.getLatestDateTimeDeliveryAtSchool());
			capList[i] = vehicles[i-1].getCap();
			
		}
    	
    	//get request information
//    	this.siblingCode2nb = new HashMap<String, Integer>();
//    	for(int i = 0; i < requests.length; i++){
//    		SchoolBusRequest r = requests[i];
//    		if(r.getSiblingCode() != null){
//	    		if(siblingCode2nb.get(r.getSiblingCode()) == null)
//	    			siblingCode2nb.put(r.getSiblingCode(), 1);
//	    		else{
//	    			int nb = siblingCode2nb.get(r.getSiblingCode()) + 1;
//	    			siblingCode2nb.put(r.getSiblingCode(), nb);
//	    		}
//    		}
//    	}
    	
    	sortCusBySibling();
    	
    	for(int i = 0; i < requests.length; i++){
    		SchoolBusRequest r = requests[i];
    		if(r.getVarIndex() < 1)
    			continue;
    		if(hs2vhId.get(r.getIdPerson()) == null)
    			r.setVarIndex(0);
    		//Point pickup = getMinCusElementInList(r.getPickupLocationId(), pickupPoints);
    		Point currPoint = getPointById(r.getPickupLocationId(), r.getVarIndex(), pickupPoints);
			if(currPoint == null){
				Point pickup = new Point(r.getPickupLocationId(), r.getLat_pickup(),
						r.getLong_pickup(), Constants.CLIENT_POINT, r.getVarIndex());
				
	    		points.add(pickup);
	    		pickupPoints.add(pickup);
	    		
	    		int groupId = r.getGroupId();
	    		point2groupId.put(pickup, groupId);
	    		if(groupId > 0){
	    			ArrayList<Point> pInGroup = groupId2points.get(groupId);
	    			if(pInGroup == null)
	    				pInGroup = new ArrayList<Point>();
	    			pInGroup.add(pickup);
	    			groupId2points.put(groupId, pInGroup);
	    			HashSet<Integer> routes = new HashSet<Integer>();
	    			groupId2vhIds.put(groupId, routes);
	    		}
	    		
	    		ArrayList<Integer> cus = new ArrayList<Integer>();
	    		cus.add(r.getIdPerson());
	    		ArrayList<Integer> reqId = new ArrayList<Integer>();
	    		reqId.add(r.getId());

	    		point2nameList.put(pickup, cus);
	    		point2idReqList.put(pickup, reqId);
	    		earliestAllowedArrivalTime.put(pickup, input.getConfigParams()
	    				.getEarliestDateTimePickupAtPoint());
				serviceDuration.put(pickup, 120);
				lastestAllowedArrivalTime.put(pickup, input.getConfigParams()
						.getLatestDateTimeDeliveryAtSchool());
			}
			else{
				ArrayList<Integer> cus = point2nameList.get(currPoint);
				ArrayList<Integer> reqId = point2idReqList.get(currPoint);
				cus.add(r.getIdPerson());
    			reqId.add(r.getId());
    			point2nameList.put(currPoint, cus);
	    		point2idReqList.put(currPoint, reqId);
				
			}
    	}
    	this.nRequest = pickupPoints.size();
    	awmT = new ArcWeightsManager(points);
    	awmD = new ArcWeightsManager(points);
		nwm = new NodeWeightsManager(points);
		
//		GoogleMapsQuery G = new GoogleMapsQuery();
//		double scale = (double)input.getConfigParams().getTimeScale()/100;
//		String date = "2019-07-29 07:00:00";
//        long departure_time = DateTimeUtils.dateTime2Int(date);
        
		double max_dist = Double.MIN_VALUE;
		String fileOut = this.fileName + "-missingData.json";
		try{
			FileOutputStream fo = new FileOutputStream(fileOut);
			PrintWriter out = new PrintWriter(fo);
			out.close();
		}catch(Exception e){
			System.out.println(e);
		}
		HashMap<String, Integer> isCheck = new HashMap<String, Integer>();
		boolean isMiss = false;
		for (int i = 0; i < points.size(); i++){
			for (int j = 0; j < points.size(); j++){
//				if(points.get(i).getID() == 1833 && points.get(j).getID() == 2434)
//					System.out.println("ffdgdf");
				double tmp_cost = getTravelTime(points.get(i).getID(),
						points.get(j).getID());
					
				double dis_cost = getDistance(points.get(i).getID(),
						points.get(j).getID());
				
				int src = points.get(i).getID();
				int dest = points.get(j).getID();
				
				String sd = src + "-" + dest;
				if((src != dest && !srcdest.contains(sd)
						&& (isCheck.get(sd) == null || isCheck.get(sd) == 0))
					|| (src != dest && (tmp_cost < 0 || dis_cost < 0)
					&& (isCheck.get(sd) == null || isCheck.get(sd) == 0))){
					isCheck.put(sd, 1);
					isMiss = true;
					try{
						FileOutputStream fo = new FileOutputStream(fileOut, true);
						PrintWriter out = new PrintWriter(fo);
						out.println(sd);
						out.close();
					}catch(Exception e){
						
					}
				}
				
				
				awmD.setWeight(points.get(i), points.get(j), dis_cost);
				//double time = (double)((dis_cost*3600)/28000);
				awmT.setWeight(points.get(i), points.get(j), tmp_cost);
				//max_dist = time > max_dist ? time : max_dist;
				max_dist = tmp_cost > max_dist ? tmp_cost : max_dist;
			}
			nwm.setWeight(points.get(i), point2nameList.get(points.get(i)).size());
		}
		if(isMiss){
			System.out.println(name() + "::travel time info, check missingDistance file please!");
			LOGGER.log(Level.WARNING, name() + "::travel time info, check missingDistance file please!");
			closeLog();
			System.exit(0);
		}
		MAX_DISTANCE = max_dist;
	}
	
	
	public void mapDataForMoving2(){
		createMapHs2busId();
		this.rejectPoints = new ArrayList<Point>();
		this.nbFixedPoint = 0;
		
		vehicles = input.getVehicles();
		requests = input.getRequests();
		this.nVehicle = vehicles.length;
		
		this.points = new ArrayList<Point>();
    	this.startPoints = new ArrayList<Point>();
    	this.stopPoints = new ArrayList<Point>();
    	this.pickupPoints = new ArrayList<Point>();
    	//this.deliveryPoints = new ArrayList<Point>();
    	this.point2nameList = new HashMap<Point, ArrayList<Integer>>();
    	this.point2idReqList = new HashMap<Point, ArrayList<Integer>>();
    	this.point2groupId = new HashMap<Point, Integer>();
    	this.groupId2vhIds = new HashMap<Integer, HashSet<Integer>>();
    	this.groupId2points = new HashMap<Integer, ArrayList<Point>>();
    	this.earliestAllowedArrivalTime = new HashMap<Point, Integer>();
    	this.serviceDuration = new HashMap<Point, Integer>();
    	this.lastestAllowedArrivalTime = new HashMap<Point, Integer>();

    	this.capList = new double[nVehicle + 1];
    	
    	//get distance matrix
		//HashSet<Integer> s_pointId = getDistanceMatrix();
//    	getDistanceMatrix();
//		if(s_pointId == null || s_pointId.size() == 0){
//			LOGGER.log(Level.INFO, "Cannot get s_point id");
//			System.exit(0);
//		}
    	//get distance matrix
		HashSet<Integer> s_pointId = new HashSet<Integer>();
		for (int i = 0; i < input.getDistances().length; i++) {
			DistanceElement e = input.getDistances()[i];
			int srcId = e.getSrcCode();
			int destId = e.getDestCode();
			s_pointId.add(srcId);
			s_pointId.add(destId);
		}
    	mPointId2Index = new HashMap<Integer, Integer>();
		int idx = -1;
		for (int id : s_pointId) {
			idx++;
			mPointId2Index.put(id, idx);
		}
		distance = new double[s_pointId.size()][s_pointId.size()];
		travelTime = new double[s_pointId.size()][s_pointId.size()];
		roadBlock = new int[s_pointId.size()][s_pointId.size()];
		srcdest = new ArrayList<String>();
		
		for (int i = 0; i < input.getDistances().length; i++) {
			DistanceElement e = input.getDistances()[i];
			int srcId = e.getSrcCode();
			int destId = e.getDestCode();
			int is = mPointId2Index.get(srcId);
			int id = mPointId2Index.get(destId);
			double d = e.getDistance();
			double scale = (double)input.getConfigParams().getTimeScale()/100;
			//scale = scale + 0.30;
			distance[is][id] = d + scale * d;
			double t = e.getTravelTime();
			travelTime[is][id] = t + scale * t;
			int r = e.getRoadBlock();
			roadBlock[is][id] = r;
			String sd = srcId + "-" + destId;
			srcdest.add(sd);
			
//			if((srcId != destId) && (d <= 0 || t <= 0)){
//				 System.out.println(name() + "::travelTime or distance, src " + srcId +
//						 "-dest " + destId + " INVALID");
//				 LOGGER.log(Level.WARNING, name() + "::travelTime or distance, src " + srcId +
//						 "-dest " + destId + "  INVALID");
//			}
		}
//		for (int src : s_pointId) {
//			for (int dest : s_pointId) {
//				String sd = src + "-" + dest;
//				if(src != dest && !srcdest.contains(sd)){
//					 System.out.println(name() + "::cannot get information, src " + src +
//							 "-dest " + dest);
//					 LOGGER.log(Level.WARNING, name() + "::cannot get information, src " + src +
//							 "-dest " + dest);
//				}
//			}
//		}
		
		//get map information
    	double lat_target = input.getLat_school();
    	double long_target = input.getLong_school();
    	
    	//get vehicles and school point information
    	for(int i = 1; i <= this.nVehicle; i++){
    		int idStartPoint = input.getShoolPointId();
			Point sp = new Point(idStartPoint, lat_target, long_target, Constants.START_POINT, 0);
			startPoints.add(sp);
			points.add(sp);
			this.point2nameList.put(sp, new ArrayList<Integer>());
			this.point2idReqList.put(sp, new ArrayList<Integer>());
			int idStopPoint = input.getShoolPointId();
			Point ep = new Point(idStopPoint, lat_target, long_target, Constants.STOP_POINT, 0);
			stopPoints.add(ep);
			points.add(ep);
			this.point2nameList.put(ep, new ArrayList<Integer>());
			this.point2idReqList.put(ep, new ArrayList<Integer>());
			earliestAllowedArrivalTime.put(sp, 0);
			serviceDuration.put(sp, 0);
			lastestAllowedArrivalTime.put(sp, input.getConfigParams()
					.getLatestDateTimeDeliveryAtSchool());
			
			earliestAllowedArrivalTime.put(ep, 0);
			serviceDuration.put(ep, 0);
			lastestAllowedArrivalTime.put(ep, input.getConfigParams()
					.getLatestDateTimeDeliveryAtSchool());
			capList[i] = vehicles[i-1].getCap();
			
		}
    	
    	//get request information
//    	this.siblingCode2nb = new HashMap<String, Integer>();
//    	for(int i = 0; i < requests.length; i++){
//    		SchoolBusRequest r = requests[i];
//    		if(r.getSiblingCode() != null){
//	    		if(siblingCode2nb.get(r.getSiblingCode()) == null)
//	    			siblingCode2nb.put(r.getSiblingCode(), 1);
//	    		else{
//	    			int nb = siblingCode2nb.get(r.getSiblingCode()) + 1;
//	    			siblingCode2nb.put(r.getSiblingCode(), nb);
//	    		}
//    		}
//    	}
    	
    	sortCusBySibling();
    	
    	for(int key : hs2vhId.keySet()){
    		int varIndex = hs2varIndex.get(key);
    		int pointId = hs2pointId.get(key);
    		int vhId = hs2vhId.get(key);
    		int reqId = hs2reqId.get(key);
    		
    		if(hs2varIndex.get(key) < 1)
    			continue;
    		//Point pickup = getMinCusElementInList(r.getPickupLocationId(), pickupPoints);
    		Point currPoint = getPointById(pointId, varIndex, pickupPoints);
			if(currPoint == null){
				Point pickup = new Point(pointId, 0,
						0, Constants.CLIENT_POINT, varIndex);
				
	    		points.add(pickup);
	    		pickupPoints.add(pickup);
	    		
	    		ArrayList<Integer> cus = new ArrayList<Integer>();
	    		cus.add(key);
	    		ArrayList<Integer> reqIdList = new ArrayList<Integer>();
	    		reqIdList.add(reqId);

	    		point2nameList.put(pickup, cus);
	    		point2idReqList.put(pickup, reqIdList);
	    		earliestAllowedArrivalTime.put(pickup, input.getConfigParams()
	    				.getEarliestDateTimePickupAtPoint());
				serviceDuration.put(pickup, 120);
				lastestAllowedArrivalTime.put(pickup, input.getConfigParams()
						.getLatestDateTimeDeliveryAtSchool());
			}
			else{
				ArrayList<Integer> cus = point2nameList.get(currPoint);
				ArrayList<Integer> reqIdList = point2idReqList.get(currPoint);
				cus.add(key);
				reqIdList.add(reqId);
    			point2nameList.put(currPoint, cus);
	    		point2idReqList.put(currPoint, reqIdList);
				
			}
    	}
    	this.nRequest = pickupPoints.size();
    	awmT = new ArcWeightsManager(points);
    	awmD = new ArcWeightsManager(points);
		nwm = new NodeWeightsManager(points);
		
//		GoogleMapsQuery G = new GoogleMapsQuery();
//		double scale = (double)input.getConfigParams().getTimeScale()/100;
//		String date = "2019-07-29 07:00:00";
//        long departure_time = DateTimeUtils.dateTime2Int(date);
        
		double max_dist = Double.MIN_VALUE;
		String fileOut = this.fileName + "-missingData.json";
		try{
			FileOutputStream fo = new FileOutputStream(fileOut);
			PrintWriter out = new PrintWriter(fo);
			out.close();
		}catch(Exception e){
			System.out.println(e);
		}
		HashMap<String, Integer> isCheck = new HashMap<String, Integer>();
		boolean isMiss = false;
		for (int i = 0; i < points.size(); i++){
			for (int j = 0; j < points.size(); j++){
//				if(points.get(i).getID() == 1833 && points.get(j).getID() == 2434)
//					System.out.println("ffdgdf");
				double tmp_cost = getTravelTime(points.get(i).getID(),
						points.get(j).getID());
					
				double dis_cost = getDistance(points.get(i).getID(),
						points.get(j).getID());
				
				int src = points.get(i).getID();
				int dest = points.get(j).getID();
				
				String sd = src + "-" + dest;
				if((src != dest && !srcdest.contains(sd)
						&& (isCheck.get(sd) == null || isCheck.get(sd) == 0))
					|| (src != dest && (tmp_cost < 0 || dis_cost < 0)
					&& (isCheck.get(sd) == null || isCheck.get(sd) == 0))){
					isCheck.put(sd, 1);
					isMiss = true;
					try{
						FileOutputStream fo = new FileOutputStream(fileOut, true);
						PrintWriter out = new PrintWriter(fo);
						out.println(sd);
						out.close();
					}catch(Exception e){
						
					}
				}
				
				
				awmD.setWeight(points.get(i), points.get(j), dis_cost);
				//double time = (double)((dis_cost*3600)/28000);
				awmT.setWeight(points.get(i), points.get(j), tmp_cost);
				//max_dist = time > max_dist ? time : max_dist;
				max_dist = tmp_cost > max_dist ? tmp_cost : max_dist;
			}
			nwm.setWeight(points.get(i), point2nameList.get(points.get(i)).size());
		}
		if(isMiss){
			System.out.println(name() + "::travel time info, check missingDistance file please!");
			LOGGER.log(Level.WARNING, name() + "::travel time info, check missingDistance file please!");
			closeLog();
			System.exit(0);
		}
		MAX_DISTANCE = max_dist;
	}
	
	public void insertingDataToPoint(){    	
    	for(int i = 0; i < requests.length; i++){
    		SchoolBusRequest r = requests[i];
    		if(r.getVarIndex() != 0)
    			continue;
    		
    		//Point pickup = getMinCusElementInList(r.getPickupLocationId(), pickupPoints);
    		ArrayList<Point> currPoints = getPointById(r.getPickupLocationId(), pickupPoints);
			if(currPoints.size() == 0){
				Point pickup = new Point(r.getPickupLocationId(), r.getLat_pickup(),
						r.getLong_pickup(), Constants.CLIENT_POINT, 1);
	    		points.add(pickup);
	    		pickupPoints.add(pickup);
	    		
	    		int groupId = r.getGroupId();
	    		point2groupId.put(pickup, groupId);
	    		if(groupId > 0){
	    			ArrayList<Point> pInGroup = groupId2points.get(groupId);
	    			if(pInGroup == null)
	    				pInGroup = new ArrayList<Point>();
	    			pInGroup.add(pickup);
	    			groupId2points.put(groupId, pInGroup);
	    			HashSet<Integer> routes = new HashSet<Integer>();
	    			groupId2vhIds.put(groupId, routes);
	    		}
	    		
	    		ArrayList<Integer> cus = new ArrayList<Integer>();
	    		ArrayList<Integer> reqId = new ArrayList<Integer>();
	    		ArrayList<Integer> siblingList = getSiblingIndexByCode(r.getSiblingCode(),
						point2cusIndex.get(r.getPickupLocationId()));
	    		for(int k = 0; k < siblingList.size(); k++){
	    			cus.add(requests[siblingList.get(k)].getIdPerson());
	    			reqId.add(requests[siblingList.get(k)].getId());
	    			reqsMarked[siblingList.get(k)] = 1;
	    		}
	    		point2nameList.put(pickup, cus);
	    		point2idReqList.put(pickup, reqId);
	    		earliestAllowedArrivalTime.put(pickup, input.getConfigParams()
	    				.getEarliestDateTimePickupAtPoint());
				serviceDuration.put(pickup, 120);
				lastestAllowedArrivalTime.put(pickup, input.getConfigParams()
						.getLatestDateTimeDeliveryAtSchool());
			}
			else{
				boolean isAdded = false;
				for(int j = 0; j < currPoints.size(); j++){
					ArrayList<Integer> cus = point2nameList.get(currPoints.get(j));
					ArrayList<Integer> reqId = point2idReqList.get(currPoints.get(j));
					int max_cap = getRoadBlock(input.getShoolPointId(), currPoints.get(j).getID());
					if(max_cap == Utils.CAP_FLEXIBILITY)
						max_cap = Utils.CAP_45;
					else if(max_cap == Utils.CAP_LEQ_29)
						max_cap = Utils.CAP_29;
					else
						max_cap = Utils.CAP_16;
					//max capacity is 43
					ArrayList<Integer> siblingList = getSiblingIndexByCode(r.getSiblingCode(),
							point2cusIndex.get(r.getPickupLocationId()));
					int nbAdded = siblingList.size() > 1 ? siblingList.size() : 1;
					if(cus.size() + nbAdded <= max_cap){
						if(XR.route(currPoints.get(j)) != Constants.NULL_POINT
							&& capCons.getSumWeights(currPoints.get(j)) + nbAdded <= max_cap){
				    		for(int k = 0; k < siblingList.size(); k++){
				    			cus.add(requests[siblingList.get(k)].getIdPerson());
				    			reqId.add(requests[siblingList.get(k)].getId());
				    			reqsMarked[siblingList.get(k)] = 1;
				    		}
				    		point2nameList.put(currPoints.get(j), cus);
				    		point2idReqList.put(currPoints.get(j), reqId);
				    		isAdded = true;
				    		break;
						}
					}
				}
				if(isAdded == false){
					int varIndex = getMaxVarIndex(currPoints) + 1;
					Point pickup = new Point(r.getPickupLocationId(), r.getLat_pickup(), r.getLong_pickup(),
							Constants.CLIENT_POINT, varIndex);
		    		points.add(pickup);
		    		pickupPoints.add(pickup);
		    		int groupId = r.getGroupId();
		    		point2groupId.put(pickup, groupId);
		    		if(groupId > 0){
		    			ArrayList<Point> pInGroup = groupId2points.get(groupId);
		    			if(pInGroup == null)
		    				pInGroup = new ArrayList<Point>();
		    			pInGroup.add(pickup);
		    			groupId2points.put(groupId, pInGroup);
		    			HashSet<Integer> routes = new HashSet<Integer>();
		    			groupId2vhIds.put(groupId, routes);
		    		}
		    		ArrayList<Integer> cus = new ArrayList<Integer>();
		    		ArrayList<Integer> reqId = new ArrayList<Integer>();
		    		ArrayList<Integer> siblingList = getSiblingIndexByCode(r.getSiblingCode(),
							point2cusIndex.get(r.getPickupLocationId()));
		    		for(int k = 0; k < siblingList.size(); k++){
		    			cus.add(requests[siblingList.get(k)].getIdPerson());
		    			reqId.add(requests[siblingList.get(k)].getId());
		    			reqsMarked[siblingList.get(k)] = 1;
		    		}
		    		point2nameList.put(pickup, cus);
		    		point2idReqList.put(pickup, reqId);
		    		earliestAllowedArrivalTime.put(pickup, input.getConfigParams()
		    				.getEarliestDateTimePickupAtPoint());
					serviceDuration.put(pickup, r.getServicePickupDuration());
					lastestAllowedArrivalTime.put(pickup, input.getConfigParams()
							.getLatestDateTimeDeliveryAtSchool());
				}
			}
			reqsMarked[i] = 1;
    	}
    	this.nRequest = pickupPoints.size();
    	awmT = new ArcWeightsManager(points);
    	awmD = new ArcWeightsManager(points);
		nwm = new NodeWeightsManager(points);
		
//		GoogleMapsQuery G = new GoogleMapsQuery();
//		double scale = (double)input.getConfigParams().getTimeScale()/100;
//		String date = "2019-07-29 07:00:00";
//        long departure_time = DateTimeUtils.dateTime2Int(date);
        
		double max_dist = Double.MIN_VALUE;
		String fileOut = this.fileName + "-missingData.json";
		try{
			FileOutputStream fo = new FileOutputStream(fileOut);
			PrintWriter out = new PrintWriter(fo);
			out.close();
		}catch(Exception e){
			System.out.println(e);
		}
		HashMap<String, Integer> isCheck = new HashMap<String, Integer>();
		boolean isMiss = false;
		for (int i = 0; i < points.size(); i++){
			for (int j = 0; j < points.size(); j++){
//				if(points.get(i).getID() == 1833 && points.get(j).getID() == 2434)
//					System.out.println("ffdgdf");
				double tmp_cost = getTravelTime(points.get(i).getID(),
						points.get(j).getID());
					
				double dis_cost = getDistance(points.get(i).getID(),
						points.get(j).getID());
				
				int src = points.get(i).getID();
				int dest = points.get(j).getID();
				
				String sd = src + "-" + dest;
				if((src != dest && !srcdest.contains(sd)
						&& (isCheck.get(sd) == null || isCheck.get(sd) == 0))
					|| (src != dest && (tmp_cost < 0 || dis_cost < 0)
					&& (isCheck.get(sd) == null || isCheck.get(sd) == 0))){
					isCheck.put(sd, 1);
					isMiss = true;
					try{
						FileOutputStream fo = new FileOutputStream(fileOut, true);
						PrintWriter out = new PrintWriter(fo);
						out.println(sd);
						out.close();
					}catch(Exception e){
						
					}
				}
				
				
				awmD.setWeight(points.get(i), points.get(j), dis_cost);
				//double time = (double)((dis_cost*3600)/28000);
				awmT.setWeight(points.get(i), points.get(j), tmp_cost);
				//max_dist = time > max_dist ? time : max_dist;
				max_dist = tmp_cost > max_dist ? tmp_cost : max_dist;
			}
			nwm.setWeight(points.get(i), point2nameList.get(points.get(i)).size());
		}
		if(isMiss){
			System.out.println(name() + "::travel time info, check missingDistance file please!");
			LOGGER.log(Level.WARNING, name() + "::travel time info, check missingDistance file please!");
			closeLog();
			System.exit(0);
		}
		MAX_DISTANCE = max_dist;
	}

	public void stateModel() {
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		S = new ConstraintSystemVR(mgr);
		vhId2flag = new HashMap<Integer, Integer>();
		vhId2busID = new HashMap<Integer, Integer>();
		vhId2busName = new HashMap<Integer, String>();
		vhId2startTime = new HashMap<Integer, Integer>();
		vhId2endTime = new HashMap<Integer, Integer>();
		vhId2resolved = new HashMap<Integer, Integer>();
		vhId2Index = new HashMap<Integer, Integer>();
		
		SchoolBusRoutingSolution solution = null;
		if(input.getCurrentSolution() != null)
			solution = input.getCurrentSolution();
		
		for (int i = 0; i < nVehicle; ++i){
			XR.addRoute(startPoints.get(i), stopPoints.get(i));
			int vhId = vehicles[i].getId();
			vhId2flag.put(vhId, Utils.NO_UPDATE);
			vhId2busID.put(vhId, getBusIDfromSolution(vhId, solution));
			vhId2startTime.put(vhId, getStartTimefromSolution(vhId, solution));
			vhId2endTime.put(vhId, getEndTimefromSolution(vhId, solution));
			vhId2busName.put(vhId, getBusNamefromSolution(vhId, solution));
			vhId2resolved.put(vhId, 0);
			vhId2Index.put(vhId, i+1);
		}

		for (int i = 0; i < nRequest; ++i) {
			Point pickup = pickupPoints.get(i);
			XR.addClientPoint(pickup);
		}

		// time windows
		eat = new EarliestArrivalTimeVR(XR, awmT, earliestAllowedArrivalTime,
				serviceDuration);
		cEarliest = new CEarliestArrivalTimeVR(eat, lastestAllowedArrivalTime);
		S.post(cEarliest);
		
		//capacity
		capCons = new CapacityConstraintViolationsVR(XR, nwm, capList);

		objective = new TotalCostVR(XR, awmT);
		distanceObj = new TotalCostVR(XR, awmD);
		totalBuses = new TotalUsedVehicles(XR);
		valueSolution = new LexMultiValues();
		valueSolution.add(S.violations());
		valueSolution.add(totalBuses.getValue());
		valueSolution.add(objective.getValue());

		mgr.close();
	}

	/**\
	 * 
	 * @param k: route need check
	 * @param roadBlock: road block constraint at one point
	 * @return: possible/impossible access
	 */
	public boolean checkSizeBus(int k, int roadBlock) {
		if(roadBlock == Utils.CAP_LEQ_29 && capList[k] > Utils.CAP_29)
			return false;
		else if(roadBlock == Utils.CAP_LEQ_16 && capList[k] > Utils.CAP_16)
			return false;
		return true;
	}

	public SchoolBusRoutingSolution randomSolve(SchoolBusRoutingInput input){
		this.input = input;
		int timeLimit = 300000;
		int nIter = 300000;

		mapData();
		System.out.println("Read data done --> Create model");
		stateModel();

		//rebuildUnresolvedSolution();
		System.out.println("Create model done --> Init solution");
		double currTime = System.currentTimeMillis();
		greedyInitSolution();

		System.out.println("Init solution done. At start search number of reject points = "
						+ rejectPoints.size() + "    violations = "
						+ S.violations() + "   cost = "
						+ objective.getValue() + ", nbBuses = "
						+ totalBuses.getValue() + ", init time = "
						+ (System.currentTimeMillis() - currTime) / 1000);

		SolutionVinPro best_solution = alnsSearch(nIter, timeLimit);

		SchoolBusRoutingSolution lastSolution = input.getCurrentSolution();
		if(lastSolution != null){
			int nbBuses = lastSolution.getStatisticInformation().getNumberBuses();
			if(nbBuses < best_solution.get_nbBuses()){
				System.out.println("Search done. At end search "
						+ "   cost = " + lastSolution.getStatisticInformation().getTotalDistance()
						+ "   nbBuses = " + lastSolution.getStatisticInformation().getNumberBuses());
				LOGGER.log(Level.INFO, "Create solution done!");
				return lastSolution;
			}
			else if(nbBuses == best_solution.get_nbBuses()){
				double cost = lastSolution.getStatisticInformation().getTotalDistance();
				if(cost < best_solution.get_cost()){
					System.out.println("Search done. At end search "
							+ "   cost = " + lastSolution.getStatisticInformation().getTotalDistance()
							+ "   nbBuses = " + lastSolution.getStatisticInformation().getNumberBuses());
					LOGGER.log(Level.INFO, "Create solution done!");
					return lastSolution;
				}
			}
			
		}
		System.out.println("Search done. At end search number of reject points = "
						+ best_solution.get_rejectPoints().size()
						+ "   cost = " + best_solution.get_cost()
						+ "   nbBuses = " + best_solution.get_nbBuses());
		best_solution.copy2XR(XR);

		for (int r = 1; r <= XR.getNbRoutes(); r++) {
			if(vhId2resolved.get(vehicles[r-1].getId()) == 1)
				vhId2flag.put(vehicles[r-1].getId(), Utils.NO_UPDATE);
			else{
				int nb = XR.index(XR.getTerminatingPointOfRoute(r)) + 1;
				if(nb <= 2){
					if(isRemoveRoute(vehicles[r-1].getId()))
						vhId2flag.put(vehicles[r-1].getId(), Utils.REMOVED);
					else 
						continue;
				}
				else
					vhId2flag.put(vehicles[r-1].getId(), Utils.UPDATED);
			}
			vhId2busID.put(vehicles[r-1].getId(),
					getBusIDfromSolution(vehicles[r-1].getId(),
							input.getCurrentSolution()));
			vhId2busName.put(vehicles[r-1].getId(),
					getBusNamefromSolution(vehicles[r-1].getId(),
							input.getCurrentSolution()));
			
			vhId2startTime.put(vehicles[r-1].getId(),
					getStartTimefromSolution(vehicles[r-1].getId(),
							input.getCurrentSolution()));
			vhId2endTime.put(vehicles[r-1].getId(),
					getEndTimefromSolution(vehicles[r-1].getId(),
							input.getCurrentSolution()));
			
		}

		rejectPoints = best_solution.get_rejectPoints();
		LOGGER.log(Level.INFO, "Create solution done!");
		printSolution("E:/Project/DO/Vinschool/VinschoolProject/screen/sampleData/", "Imperia");
		return createFormatedSolution();
	}
	
	/**
	 * re-build solution from input
	 */
	public void rebuildSolution(){
		if(input.getCurrentSolution() == null){
			LOGGER.log(Level.WARNING, name() + "::rebuildSolution: cannot get current solution");
			return;
		}
		SchoolBusRoutingSolution solution = input.getCurrentSolution();
		
		//sVio = solution.getStatisticInformation().getTimeViolation();
		//capVio = solution.getStatisticInformation().getCapViolation();
		
		for(int i = 0; i < solution.getBusRoutes().length; i++){
			RouteElement[] nodes = solution.getBusRoutes()[i].getNodes();
			int r = solution.getBusRoutes()[i].getVehicle().getId();
//			vhId2busID.put(r, solution.getBusRoutes()[i].getBusID());
//			vhId2busName.put(r, solution.getBusRoutes()[i].getBusName());
//			vhId2startTime.put(r, solution.getBusRoutes()[i].getStartingDateTimePickup());
//			vhId2endTime.put(r, solution.getBusRoutes()[i].getStartingDateTimeDelivery());
			vhId2resolved.put(r, solution.getBusRoutes()[i].getIsResolved());
			Point pre_p = XR.getStartingPointOfRoute(vhId2Index.get(r));
			for(int j = 0; j < nodes.length - 1; j++){
				Point p = getPointById(nodes[j].getLocationId(),
						nodes[j].getVarIndex(), pickupPoints);
				if(p == null){
					System.out.println(name() + "::buildingSolution: cannot get point "
							+ nodes[j].getLocationId()
							+ " in pickup points");
//					LOGGER.log(Level.WARNING, name() + "::buildingSolution: cannot get point "
//							+ nodes[j].getLocationId()
//							+ " in pickup points");
				}
				else{
					if(S.evaluateAddOnePoint(p, pre_p) == 0
							&& capCons.evaluateAddOnePoint(p, pre_p) == 0){
						mgr.performAddOnePoint(p, pre_p);
						pre_p = p;
					}
					else{
						if(solution.getBusRoutes()[i].getIsResolved() == 1){
							mgr.performAddOnePoint(p, pre_p);
							pre_p = p;
						}
					}
				}
			}
		}
		
		sVio = S.violations();
		capVio = capCons.getValue();
		
		for(int i = 0; i < pickupPoints.size(); i++){
			if(XR.route(pickupPoints.get(i)) == Constants.NULL_POINT)
				rejectPoints.add(pickupPoints.get(i));
		}
	}
	
	/**
	 * re-build solution from input
	 */
	public void rebuildUnresolvedSolution(){
		if(input.getCurrentSolution() == null)
			return;
		SchoolBusRoutingSolution solution = input.getCurrentSolution();
		
		//sVio = solution.getStatisticInformation().getTimeViolation();
		//capVio = solution.getStatisticInformation().getCapViolation();
		
		for(int i = 0; i < solution.getBusRoutes().length; i++){
			if(solution.getBusRoutes()[i].getIsResolved() == 1){
				RouteElement[] nodes = solution.getBusRoutes()[i].getNodes();
				int vhId = solution.getBusRoutes()[i].getVehicle().getId();
				vhId2busID.put(vhId, solution.getBusRoutes()[i].getBusID());
				vhId2busName.put(vhId, solution.getBusRoutes()[i].getBusName());
				vhId2startTime.put(vhId, solution.getBusRoutes()[i].getStartingDateTimePickup());
				vhId2endTime.put(vhId, solution.getBusRoutes()[i].getStartingDateTimeDelivery());
				Point pre_p = XR.getStartingPointOfRoute(vhId2Index.get(vhId));
				for(int j = 0; j < nodes.length - 1; j++){
					Point p = getPointById(nodes[j].getLocationId(),
							nodes[j].getVarIndex(), pickupPoints);
					if(p == null){
						System.out.println(name() + "::buildingSolution: cannot get point "
								+ nodes[j].getLocationId()
								+ " in pickup points");
//						LOGGER.log(Level.WARNING, name() + "::buildingSolution: cannot get point "
//								+ nodes[j].getLocationId()
//								+ " in pickup points");
					}
					else{
						//if(capCons.evaluateAddOnePoint(p, pre_p) + capCons.getValue() <= capVio){
							mgr.performAddOnePoint(p, pre_p);
							pre_p = p;
						//}
					}
				}
				vhId2resolved.put(vhId, 1);
			}
			else{
				int r = solution.getBusRoutes()[i].getVehicle().getId();
				vhId2resolved.put(r, 0);
			}
		}
		sVio = S.violations();
		capVio = capCons.getValue();
		
//		for(int i = 0; i < pickupPoints.size(); i++){
//			if(XR.route(pickupPoints.get(i)) == Constants.NULL_POINT)
//				rejectPoints.add(pickupPoints.get(i));
//		}
	}
	
	/**
	 * create nearest neighborhood solution
	 * Step 1: the starting point of route is the farthest point.
	 * Step 2: choose the nearest point for insertion.
	 */
	public void computeNearestNeighborhoodSolution(){
		while(true){
			Point p = null;
			Point st = null;
			for(int r = XR.getNbRoutes(); r >= 1; r--){
				Point startingPoint = XR.getStartingPointOfRoute(r);
				if(XR.next(startingPoint) != XR.getTerminatingPointOfRoute(r))
					continue;
				p = getFarthestPoint(XR.getStartingPointOfRoute(r),
						pickupPoints);
				if(p != null){
					st = XR.getStartingPointOfRoute(r);
					break;
				}
			}
			if(p == null || st == null)
				break;
			
			mgr.performAddOnePoint(p, st);
			while(true){
				Point pickup = getNearestPoint(p, pickupPoints);
				if(pickup == null)
					break;
				mgr.performAddOnePoint(pickup, p);
				p = pickup;
			}
		}
		
		for(int i = 0; i < pickupPoints.size(); i++){
			Point pickup = pickupPoints.get(i);
			if(XR.route(pickup) == Constants.NULL_POINT)
				rejectPoints.add(pickup);
		}
	}

	// public boolean checkDistanceConstraint(Point x, Point y, Point st){
	// if(y == st && awm.getWeight(x, st) >= awm.getWeight(XR.next(st), st))
	// return true;
	// if(awm.getWeight(x, st) <= awm.getWeight(y, st) && awm.getWeight(x, st)
	// >= awm.getWeight(XR.next(y), st))
	// return true;
	// return false;
	// }

	public boolean checkDistanceConstraint(Point x, Point y, Point st) {
		if(x.getID() == 2685 && XR.route(y) == 5)
			System.out.println("d");
		double xst = awmT.getWeight(x, st);
		double yst = awmT.getWeight(y, st);
		double yx = awmT.getWeight(y, x);
		double nyst = awmT.getWeight(XR.next(y), st);
		double xny = awmT.getWeight(x, XR.next(y));
		int r = XR.route(st);
		Point en = XR.getTerminatingPointOfRoute(r);
		double yny = awmT.getWeight(y, XR.next(y));
		double te = eat.getEarliestArrivalTime().get(en);
		double sttime = eat.getEarliestArrivalTime(XR.next(st));
		int scale1 = input.getConfigParams().getBoardingTimeScale1();
		int scale2 = input.getConfigParams().getBoardingTimeScale2();
		int scale3 = input.getConfigParams().getBoardingTimeScale2()/2;
		double boardingTimeScale1 = (double)(1 + (double)scale1/100);
		double boardingTimeScale2 = (double)(1 + (double)scale2/100);
		double boardingTimeScale3 = (double)(1 + (double)scale3/100);
		if (y != st) {
			if(yx + xst >= boardingTimeScale1*yst && yst <= 900)
				return false;
			if(yx + xst >= boardingTimeScale2*yst && yst > 900)
				return false;
			if(xst >= boardingTimeScale1 * yst && yst <= 900)
				return false;
			double endtime = te + yx + xny - yny;
			double drtime = awmT.getWeight(XR.next(st), en);
			if (drtime <= 900 && endtime - sttime > boardingTimeScale1 * drtime)
				return false;
			if (drtime <= 1800 && drtime > 900 && endtime - sttime > boardingTimeScale2 * drtime)
				return false;
			if (drtime > 1800 && endtime - sttime > boardingTimeScale3 * drtime)
				return false;
		} else {
			double endtime = te + xny;
			double drtime = awmT.getWeight(x, en);
			if (drtime <= 900 && endtime - sttime > boardingTimeScale1 * drtime)
				return false;
			if (drtime <= 1800 && drtime > 900 && endtime - sttime > boardingTimeScale2 * drtime)
				return false;
			if (drtime > 1800 && endtime - sttime > boardingTimeScale3 * drtime)
				return false;
		}
		// if(y == st && xst < nyst)
		// return false;
		// if(xst > yst || xst < nyst)
		// return false;
		// // HashMap<Point, Double> earliestArrivalTime =
		// eat.getEarliestArrivalTime();
		// // double endTime =
		// earliestArrivalTime.get(XR.getTerminatingPointOfRoute(XR.route(y)));
		// if(y == st && xny + nyst > 1.3*xst)
		// return false;
		// if(y != st && yx + xst > 1.3*yst)
		// return false;
		return true;
	}

	public boolean checkTriangleConstraint(Point x, Point y, Point st) {

		return true;
	}
	
	public RouteElement[] createReverseRoute(int r){
		Utils u = new Utils();
		int nb = XR.index(XR.getTerminatingPointOfRoute(r));
		int[][] c = new int[nb+1][nb+1];
		
		HashMap<Integer, Point> id2Point = new HashMap<Integer, Point>();
		int id = 0;
		for(Point p = XR.getStartingPointOfRoute(r); p != XR.getTerminatingPointOfRoute(r); p = XR.next(p)){
			id++;
			id2Point.put(id, p);
		}

		int maxTravel = -1;
		for(int i = 1; i <= nb; i++){
			for(int j = 1; j <= nb; j++){
				int d = (int)getTravelTime(id2Point.get(i).getID(), id2Point.get(j).getID());
				c[i][j] = d;
				if(d > maxTravel)
					maxTravel = d;
			}
		}
		int[] bestRoute = new int[nb+1];
		bestRoute = u.getShortestPathOnRouteReverses(nb, maxTravel, c);

		int h = 0;
		int endTime = input.getConfigParams().getEarliestDateTimePickupAtSchool();
		Point en = XR.getTerminatingPointOfRoute(r);
		RouteElement[] reverses = new RouteElement[nb];
		reverses[h] = new RouteElement(en.getID(), en.getVarIndex(), "PICKUP_POINT",
				endTime, endTime, 0, 0, new int[0], new int[0]);
		h++;
		for(int i = 2; i <= nb; i++){
			Point p = id2Point.get(bestRoute[i]);
			ArrayList<Integer> cusBack = point2nameList.get(p);
			int[] cusArrBack = new int[cusBack.size()];
			for (int k = 0; k < cusBack.size(); k++)
				cusArrBack[k] = cusBack.get(k);
			
			ArrayList<Integer> reqIdBack = point2idReqList.get(p);
			int[] reqIdArrBack = new int[reqIdBack.size()];
			for (int k = 0; k < reqIdBack.size(); k++)
				reqIdArrBack[k] = reqIdBack.get(k);
			//System.out.println(awmT.getWeight(en, p));
			reverses[h] = new RouteElement(p.getID(), p.getVarIndex(),
					"DELIVERY_POINT", 
					(int)(endTime + awmT.getWeight(en, p)),
					(int)(endTime + awmT.getWeight(en, p) + 120), 
					(int)awmT.getWeight(XR.getTerminatingPointOfRoute(r), p),
					(int)(endTime - input.getConfigParams()
						.getEarliestDateTimePickupAtSchool() + awmT.getWeight(en, p)),
					cusArrBack, reqIdArrBack);
			h++;
			endTime += awmT.getWeight(en, p) + 120;
			en = p;
		}
		
		return reverses;
	}

	/**
	 * create solution with json format.
	 * @return
	 */
	public SchoolBusRoutingSolution createFormatedSolution() {
		ArrayList<BusRoute> brArr = new ArrayList<BusRoute>();
		int[] newCapList = new int[XR.getNbRoutes() + 1];
		//HashMap<Integer, Integer> hs2bus = new HashMap<Integer, Integer>();
		int nbBuses = 0;
		int totalPupils = 0;
		double totalDistance = 0;
		for (int r = 1; r <= XR.getNbRoutes(); r++) {
			int nb = XR.index(XR.getTerminatingPointOfRoute(r)) + 1;
			Vehicle vehicle = new Vehicle(vehicles[r-1].getId(), vehicles[r-1].getCap());
			
			if(nb <= 2){
				if(isRemoveRoute(vehicle.getId()))
					vhId2flag.put(vehicle.getId(), Utils.REMOVED);
				if(vhId2flag.get(vehicle.getId()) == Utils.NO_UPDATE)
					continue;
			}
			
			double d = 0;
			int nbPers = 0;
			Point st = XR.getStartingPointOfRoute(r);
			Point en = XR.getTerminatingPointOfRoute(r);
			for(Point p = XR.next(st); p != en; p = XR.next(p))
				nbPers += point2nameList.get(p).size();
			nbPers += point2nameList.get(XR.getTerminatingPointOfRoute(r)).size();
			
			//bus co it hon 2 hs thi xoa
			if(nbPers > 0 && nbPers <= 2){
				Point x = st;
				Point next_x = XR.next(st);
				while(next_x != en){
					x = next_x;
					next_x = XR.next(x);
					//if(!removeAllowed.get(x))
						//continue;
					rejectPoints.add(x);

					mgr.performRemoveOnePoint(x);
				}
//				for(Point p = XR.next(st); p != en; p = XR.next(p)){
//					mgr.performRemoveOnePoint(p);
//					rejectPoints.add(p);
//				}
				if(isRemoveRoute(vehicle.getId()))
					vhId2flag.put(vehicle.getId(), Utils.REMOVED);
				else{
					vhId2flag.put(vehicle.getId(), Utils.NO_UPDATE);
					continue;
				}
			}
			int g = 0;
			RouteElement[] nodes = new RouteElement[nb-1];
			int extraTime = 0;

//			int eArrivalSchool = input.getConfigParams().getEarliestDatetimeArrivalSchool();
//			int arrTime = (int)(eat.getEarliestArrivalTime(en));
//			if(arrTime < eArrivalSchool)
//				extraTime = eArrivalSchool - arrTime;
			
			int startPickupTime = (int)eat.getEarliestArrivalTime(XR.next(st));
			extraTime = vhId2startTime.get(vehicle.getId()) - startPickupTime;
			
			
			Utils u = new Utils();
			int nbP = XR.index(XR.getTerminatingPointOfRoute(r));
			int[][] c = new int[nbP+1][nbP+1];
			
			HashMap<Integer, Point> id2Point = new HashMap<Integer, Point>();
			int id = 0;
			for(Point p = XR.getStartingPointOfRoute(r); p != XR.getTerminatingPointOfRoute(r); p = XR.next(p)){
				id++;
				id2Point.put(id, p);
			}

			int maxTravel = -1;
			for(int i = 1; i <= nbP; i++){
				for(int j = 1; j <= nbP; j++){
					int dt = (int)getTravelTime(id2Point.get(i).getID(), id2Point.get(j).getID());
					c[i][j] = dt;
					if(dt > maxTravel)
						maxTravel = dt;
				}
			}
//			int[] bestRoute = new int[nbP+1];
//			bestRoute = u.getShortestPathOnRoute(nbP, maxTravel, c);
			
//			for(int i = 2; i <= nbP; i++){
//				Point p = id2Point.get(bestRoute[i]);
			for(Point p = XR.next(st); p != XR
					.getTerminatingPointOfRoute(r); p = XR.next(p)) {			
				ArrayList<Integer> cus = point2nameList.get(p);
				ArrayList<Integer> reqId = point2idReqList.get(p);
				int[] cusArr = new int[cus.size()];
				int[] reqIdArr = new int[reqId.size()];
				for (int k = 0; k < cus.size(); k++){
					cusArr[k] = cus.get(k);
				}
				for (int k = 0; k < reqId.size(); k++){
					reqIdArr[k] = reqId.get(k);
				}
				nodes[g] = new RouteElement(p.getID(), p.getVarIndex(), "PICKUP_POINT",
						(int)eat.getEarliestArrivalTime(p) + extraTime,
						(int)(eat.getEarliestArrivalTime(p) + 120 + extraTime), 
						(int)awmT.getWeight(p, en),
						(int)(eat.getEarliestArrivalTime(en) 
						- eat.getEarliestArrivalTime(p) - 120),
						cusArr, reqIdArr);
				g++;
				d += getDistance(p.getID(), XR.next(p).getID());
			}

			ArrayList<Integer> cus = point2nameList.get(XR.getTerminatingPointOfRoute(r));
			ArrayList<Integer> reqId = point2idReqList.get(XR.getTerminatingPointOfRoute(r));
			int[] cusArr = new int[cus.size()];
			int[] reqIdArr = new int[reqId.size()];
			for (int k = 0; k < cus.size(); k++){
				cusArr[k] = cus.get(k);
			}
			for (int k = 0; k < reqId.size(); k++){
				reqIdArr[k] = reqId.get(k);
			}
			nodes[g] = new RouteElement(XR.getTerminatingPointOfRoute(r).getID(),
					XR.getTerminatingPointOfRoute(r).getVarIndex(), "DELIVERY_POINT",
					(int)eat.getEarliestArrivalTime(en) + extraTime,
					(int)(eat.getEarliestArrivalTime(en) + 120 + extraTime),
					0, 0, cusArr, reqIdArr);
		
			totalPupils += nbPers;
			
			double fillingRate = 0;
			if (nbPers < 16) {
				fillingRate = nbPers * 100 / 15;
				newCapList[r] = 15;
			} else if (nbPers >= 16 && nbPers < 29) {
				fillingRate = nbPers * 100 / 28;
				newCapList[r] = 28;
			} else {
				fillingRate = nbPers * 100 / 44;
				newCapList[r] = 44;
			}
			vehicle.setCap(newCapList[r]);
			//fillingRate = nbPers * 100 / vehicle.getCap();
			
			RouteElement[] reverses = new RouteElement[nb-1];
			//reverses = createReverseRoute(r);
			
			int h = 0;
			//int endTime = input.getConfigParams().getEarliestDateTimePickupAtSchool();
			int endTime = vhId2endTime.get(vehicle.getId());
			RouteElement[] basicReverses = new RouteElement[nb-1];
			basicReverses[h] = new RouteElement(en.getID(), en.getVarIndex(), "PICKUP_POINT",
					endTime, endTime, 0, 0, new int[0], new int[0]);
			h++;
			for(Point p = XR.prev(en); p != XR
					.getStartingPointOfRoute(r); p = XR.prev(p)) {			
				ArrayList<Integer> cusBack = point2nameList.get(p);
				int[] cusArrBack = new int[cusBack.size()];
				for (int k = 0; k < cusBack.size(); k++)
					cusArrBack[k] = cusBack.get(k);

				ArrayList<Integer> reqIdBack = point2idReqList.get(p);
				int[] reqIdArrBack = new int[reqIdBack.size()];
				for (int k = 0; k < reqIdBack.size(); k++)
					reqIdArrBack[k] = reqIdBack.get(k);
				
				basicReverses[h] = new RouteElement(p.getID(), p.getVarIndex(),
						"DELIVERY_POINT", 
						(int)(endTime + awmT.getWeight(en, p)),
						(int)(endTime + awmT.getWeight(en, p) + 120), 
						(int)awmT.getWeight(XR.getTerminatingPointOfRoute(r), p),
						(int)(endTime - input.getConfigParams()
							.getEarliestDateTimePickupAtSchool() + awmT.getWeight(en, p)),
						cusArrBack, reqIdArrBack);
				h++;
				endTime += awmT.getWeight(en, p) + 120;
				en = p;
			}
			
			double t = eat.getEarliestArrivalTime(XR
					.getTerminatingPointOfRoute(r))
					- eat.getEarliestArrivalTime(XR.next(XR
							.getStartingPointOfRoute(r)));
			
			checkNbRequestsUpdated(vehicle.getId(), nbPers);
			
			BusRoute br = new BusRoute(vehicle, vhId2busID.get(vehicle.getId()),
					vhId2busName.get(vehicle.getId()), nbPers,
					fillingRate, vhId2resolved.get(vehicle.getId()), XR.index(XR.getTerminatingPointOfRoute(r)),
					vhId2startTime.get(vehicle.getId()), vhId2endTime.get(vehicle.getId()),
					(int)t, d, vhId2flag.get(vehicle.getId()), nodes, basicReverses, reverses);
			brArr.add(br);
			if(XR.getStartingPointOfRoute(r).ID != XR.next(XR.getStartingPointOfRoute(r)).ID)
				nbBuses++;
			totalDistance += d;
		}
		BusRoute[] busRoutes = new BusRoute[brArr.size()];
		for(int i = 0; i < brArr.size(); i++){
			BusRoute br = brArr.get(i);
			if(br.getNbStops() > 1
				|| (br.getNbStops() == 1 && br.getUpdateFlag() == Utils.REMOVED))
				busRoutes[i] = brArr.get(i);
		}
		ArrayList<SchoolBusRequest> unsch = new ArrayList<SchoolBusRequest>();
		for (int k = 0; k < rejectPoints.size(); k++) {
			Point p = rejectPoints.get(k);
			ArrayList<Integer> cus = point2nameList.get(p);
			for(int n = 0; n < input.getRequests().length; n++){
				SchoolBusRequest s = input.getRequests()[n];
				if(cus.contains(s.getIdPerson())){
					s.setVarIndex(p.getVarIndex());
					unsch.add(s);
				}
			}
		}
		SchoolBusRequest[] unScheduledRequests = new SchoolBusRequest[unsch.size()];
		for(int k = 0; k < unsch.size(); k++)
			unScheduledRequests[k] = unsch.get(k);
		
		StatisticInformation statisticInformation = new StatisticInformation(totalPupils, S.violations(), (int)capCons.getValue(),
				totalDistance, nbBuses, movingResults);
		
		return new SchoolBusRoutingSolution(busRoutes,
				unScheduledRequests, statisticInformation);
	}

	public SolutionVinPro alnsSearch(int maxIter, int timeLimit) {
		ALNSforVinPro alns = new ALNSforVinPro(this, nbFixedPoint);
		return alns.search(maxIter, timeLimit);
	}
	
	public SchoolBusRoutingSolution nearestNeighborhoodSolver(SchoolBusRoutingInput input){
		this.input = input;
		int timeLimit = 36000000;
		int nIter = 10000;

		mapData();
		System.out.println("Read data done --> Create model");
		stateModel();

		System.out.println("Create model done --> Init solution");
		double currTime = System.currentTimeMillis();
		computeNearestNeighborhoodSolution();

		System.out.println("Init solution done. At start search number of reject points = "
						+ rejectPoints.size() + "    violations = "
						+ S.violations() + "   cost = "
						+ objective.getValue() + ", nbBuses = "
						+ totalBuses.getValue() + ", init time = "
						+ (System.currentTimeMillis() - currTime) / 1000);

		for (int r = 1; r <= XR.getNbRoutes(); r++) {
			int nb = XR.index(XR.getTerminatingPointOfRoute(r)) + 1;
			if(nb <= 2)
				continue;
			vhId2flag.put(vehicles[r-1].getId(), Utils.UPDATED);
		}

		LOGGER.log(Level.INFO, "Create nearest solution done!");
		//printSolution("E:/Project/cblsvr/VinschoolProject/data/data-test-output/", "TimeCity-TaiNha-PTLC-nearest");
		return createFormatedSolution();
	}
	
	public void movingRequests(){		
		MoveAction[] moveActions = input.getMoveActions();
		movingResults = new MovingResult[moveActions.length];
		for(int i = 0; i < moveActions.length; i++)
			movingResults[i] = new MovingResult();
		for(int i = 0; i < moveActions.length; i++){
			Point p = getPointById(moveActions[i].getPointId_1(),
					moveActions[i].getVarIndex_1(), points);

			//add p into after pre_p
			if(moveActions[i].getTypeOfAction() == Utils.ADD_ONE_POINT){
				Point pre_p = getPointById(moveActions[i].getPre_pointId(),
						moveActions[i].getPre_varIndex(), points);
				if(pre_p == null){
					if(moveActions[i].getRouteIndex1() > 0){
						for(int key : vhId2busID.keySet()){
							if(vhId2busID.get(key) == moveActions[i].getRouteIndex1()){
								int index = vhId2Index.get(key);
								pre_p = XR.getStartingPointOfRoute(index);
							}
						}
					}
				}
				if(pre_p == null){
					System.out.println(name() + "::movingRequests: cannot get point "
							+ moveActions[i].getPre_pointId()
							+ " in pickup points");
					LOGGER.log(Level.WARNING, name() + "::add one point: cannot get pre point "
							+ moveActions[i].getPre_pointId()
							+ " in pickup points");
					closeLog();
					System.exit(0);
				}
				if(XR.route(pre_p) == Constants.NULL_POINT){
					System.out.println(name() + "::movingRequests: point "
							+ pre_p.getID() + " is not in any route");
					LOGGER.log(Level.WARNING, name() + "::add one point: point "
							+ pre_p.getID() + " is not in any route");
					closeLog();
					System.exit(0);
				}
				
				if(p == null){
					p = getPointById(moveActions[i].getPointId_1(), 
							0, points);
					if(p == null){
						System.out.println(name() + "::movingRequests: cannot get point "
								+ moveActions[i].getPointId_1()
								+ " in pickup points");
						LOGGER.log(Level.WARNING, name() + "::movingRequests: cannot get point "
								+ moveActions[i].getPointId_1()
								+ " in pickup points");
						closeLog();
						System.exit(0);
					}
					p.setVarIndex(moveActions[i].getVarIndex_1());
				}
				
				if(XR.route(p) != Constants.NULL_POINT){
					Point q = getPointById(moveActions[i].getPointId_1(),
							0, points);
					if(XR.route(pre_p) == XR.route(p) && q != null){
						ArrayList<Integer> cusP = point2nameList.get(p);
						ArrayList<Integer> cusQ = point2nameList.get(q);
						for(int k = 0; k < cusQ.size(); k++)
							cusP.add(cusQ.get(k));
						point2nameList.put(p, cusP);
						point2nameList.put(q, new ArrayList<Integer>());
						
						ArrayList<Integer> reqIdP = point2idReqList.get(p);
						ArrayList<Integer> reqIdQ = point2idReqList.get(q);
						for(int k = 0; k < reqIdQ.size(); k++)
							reqIdP.add(reqIdQ.get(k));
						point2idReqList.put(p, reqIdP);
						point2idReqList.put(q, new ArrayList<Integer>());
						
						movingResults[i].setStatus(Utils.LOG_SUCCESS);
						movingResults[i].setExtraTime(0);
						movingResults[i].setExtraDistance(0);
						vhId2flag.put(vehicles[XR.route(pre_p) - 1].getId(), Utils.UPDATED);
						for(int t = 0; t < rejectPoints.size(); t++){
							if(rejectPoints.get(t).getID() == q.getID()){
								rejectPoints.remove(t);
								break;
							}
						}
						return;
					}
					else{
						System.out.println(name() + "::add one point: point "
								+ p.getID() + " is not the new pickup point");
						LOGGER.log(Level.WARNING, name() + "::add one point: point "
								+ p.getID() + " is not new pickup point");
						closeLog();
						System.exit(0);
					}
				}

				
				movingResults[i].setStatus(Utils.LOG_SUCCESS);
				
				int timeVio = S.evaluateAddOnePoint(p, pre_p);
				int capVio = (int)capCons.evaluateAddOnePoint(p, pre_p);
				if(timeVio > 0){
					movingResults[i].setStatus(Utils.LOG_TIME_VIOLATION);
					movingResults[i].setExtraViolationTime(timeVio);
				}
				if(capVio > 0){
					movingResults[i].setStatus(Utils.LOG_CAP_VIOLATION);
					movingResults[i].setExtraViolationCap(capVio);
				}
				if(timeVio > 0 && capVio > 0)
					movingResults[i].setStatus(Utils.LOG_TIME_CAP_VIOLATION);
				
				movingResults[i].setExtraTime((int)objective.evaluateAddOnePoint(p, pre_p));
				movingResults[i].setExtraDistance((int)distanceObj.evaluateAddOnePoint(p, pre_p));
				movingResults[i].setAddingSuggestions(getAddingSuggestions(p));
	
				mgr.performAddOnePoint(p, pre_p);
				
				timeVio = (int)(eat.getEarliestArrivalTime(XR.getTerminatingPointOfRoute(XR.route(pre_p)))
						- lastestAllowedArrivalTime.get(XR.getTerminatingPointOfRoute(XR.route(pre_p))));
				if(timeVio > 0)
					movingResults[i].setExtraViolationTime(timeVio);
				else
					timeVio = 0;
				movingResults[i].setExtraViolationTime(timeVio);
				vhId2flag.put(vehicles[XR.route(pre_p) - 1].getId(), Utils.UPDATED);
				for(int t = 0; t < rejectPoints.size(); t++){
					if(rejectPoints.get(t).getID() == p.getID()){
						rejectPoints.remove(t);
						break;
					}
				}	
			}
			else if(moveActions[i].getTypeOfAction() == Utils.REMOVE_ONE_POINT){
				if(XR.route(p) == Constants.NULL_POINT){
					System.out.println(name() + "::movingRequests: point "
							+ p.getID() + " is not in any route");
					LOGGER.log(Level.WARNING, name() + "::remove one point: point "
							+ p.getID() + " is not in any route");
					closeLog();
					System.exit(0);
				}
				int nb = XR.index(XR.getTerminatingPointOfRoute(XR.route(p))) + 1;
				if(nb <= 3)
					vhId2flag.put(vehicles[XR.route(p) - 1].getId(), Utils.REMOVED);
				else
					vhId2flag.put(vehicles[XR.route(p) - 1].getId(), Utils.UPDATED);
				movingResults[i].setStatus(Utils.LOG_SUCCESS);
				movingResults[i].setExtraTime((int)objective.evaluateRemoveOnePoint(p));
				movingResults[i].setExtraDistance((int)distanceObj.evaluateRemoveOnePoint(p));
				mgr.performRemoveOnePoint(p);
				rejectPoints.add(p);
			}
//			else if(moveActions[i].getTypeOfAction() == Utils.ONE_POINT_MOVE){
//				Point pre_p = getPointById(moveActions[i].getPre_pointId(),
//						moveActions[i].getPre_varIndex(), points);
//				if(pre_p == null){
//					System.out.println(name() + "::movingRequests: cannot get point "
//							+ moveActions[i].getPre_pointId()
//							+ " in pickup points");
//					LOGGER.log(Level.WARNING, name() + "::movingRequests: cannot get point "
//							+ moveActions[i].getPre_pointId()
//							+ " in pickup points");
//				}
//				if(XR.route(p) == Constants.NULL_POINT){
//					System.out.println(name() + "::movingRequests: point "
//							+ p.getID() + " is not any route");
//					LOGGER.log(Level.WARNING, name() + "::movingRequests: point "
//							+ p.getID() + " is not any route");
//				}
//				if(XR.route(pre_p) == Constants.NULL_POINT){
//					System.out.println(name() + "::movingRequests: point "
//							+ pre_p.getID() + " is not in any route");
//					LOGGER.log(Level.WARNING, name() + "::movingRequests: point "
//							+ pre_p.getID() + " is not in any route");
//				}
//
//				int timeVio = S.evaluateOnePointMove(p, pre_p);
//				int capVio = (int)capCons.evaluateOnePointMove(p, pre_p);
//				if(timeVio > 0){
//					movingResults[i].setStatus(Utils.LOG_TIME_VIOLATION);
//					movingResults[i].setExtraViolationTime(timeVio);
//				}
//				if(capVio > 0){
//					movingResults[i].setStatus(Utils.LOG_CAP_VIOLATION);
//					movingResults[i].setExtraViolationCap(capVio);
//				}
//				if(timeVio > 0 && capVio > 0)
//					movingResults[i].setStatus(Utils.LOG_TIME_CAP_VIOLATION);
//				movingResults[i].setExtraTime((int)objective.evaluateOnePointMove(p, pre_p));
//				movingResults[i].setExtraDistance((int)distanceObj.evaluateOnePointMove(p, pre_p));
//				
//				if(timeVio == 0 && capVio == 0){
//					vhId2flag.put(vehicles[XR.route(p) - 1].getId(), Utils.UPDATED);
//					vhId2flag.put(vehicles[XR.route(pre_p) - 1].getId(), Utils.UPDATED);
//					mgr.performOnePointMove(p, pre_p);
//					movingResults[i].setStatus(Utils.LOG_SUCCESS);
//				}
//			}
//			else if(moveActions[i].getTypeOfAction() == Utils.TWO_POINT_MOVE){
//				Point p_2 = getPointById(moveActions[i].getPointId_2(),
//						moveActions[i].getVarIndex_2(), points);
//				if(p_2 == null){
//					System.out.println(name() + "::movingRequests: cannot get point "
//							+ moveActions[i].getPointId_2()
//							+ " in pickup points");
//					LOGGER.log(Level.WARNING, name() + "::movingRequests: cannot get point "
//							+ moveActions[i].getPointId_2()
//							+ " in pickup points");
//				}
//				if(XR.route(p_2) == Constants.NULL_POINT){
//					System.out.println(name() + "::movingRequests: point "
//							+ p_2.getID() + " is not any route");
//					LOGGER.log(Level.WARNING, name() + "::movingRequests: point "
//							+ p_2.getID() + " is not any route");
//				}
//
//				ArrayList<Point> x = new ArrayList<Point>();
//				x.add(p);
//				ArrayList<Point> y = new ArrayList<Point>();
//				y.add(p_2);
//				int timeVio = S.evaluateKPointsMove(x, y);
//				int capVio = (int)capCons.evaluateKPointsMove(x, y);
//				if(timeVio > 0){
//					movingResults[i].setStatus(Utils.LOG_TIME_VIOLATION);
//					movingResults[i].setExtraViolationTime(timeVio);
//				}
//				if(capVio > 0){
//					movingResults[i].setStatus(Utils.LOG_CAP_VIOLATION);
//					movingResults[i].setExtraViolationCap(capVio);
//				}
//				if(timeVio > 0 && capVio > 0)
//					movingResults[i].setStatus(Utils.LOG_TIME_CAP_VIOLATION);
//				movingResults[i].setExtraTime((int)objective.evaluateKPointsMove(x, y));
//				movingResults[i].setExtraDistance((int)distanceObj.evaluateKPointsMove(x, y));
//				
//				if(timeVio == 0 && capVio == 0){
//					vhId2flag.put(vehicles[XR.route(p) - 1].getId(), Utils.UPDATED);
//					vhId2flag.put(vehicles[XR.route(p_2) - 1].getId(), Utils.UPDATED);
//					mgr.performTwoPointsMove(p, p_2);
//					movingResults[i].setStatus(Utils.LOG_SUCCESS);
//				}
//			}
			else{
				System.out.println(name() + "::movingRequests: type of moving request is invalid");
				LOGGER.log(Level.WARNING, name() + "::movingRequests: type of moving request is invalid");
				closeLog();
				System.exit(0);
			}
		}
	}
	
	public SchoolBusRoutingSolution updateSolutionByMoveAction(SchoolBusRoutingInput input){
		this.input = input;

		mapDataForMoving();
		System.out.println("Read data done --> Create model");
		stateModel();
		rebuildSolution();
		System.out.println("Create model done --> Update solution");
		movingRequests();
		//System.out.println("Update solution done!");
		LOGGER.log(Level.INFO, "Update solution done!");
		return createFormatedSolution();
	}
	
	public SchoolBusRoutingSolution updateCurrentSolution(SchoolBusRoutingInput input){
		this.input = input;

		mapDataForUpdating();
		System.out.println("Read data done --> Create model");
		stateModel();
		rebuildSolution();
		System.out.println("Create model done --> Update solution");
		greedyUpdateSolution();
		//System.out.println("Update solution done!");
		LOGGER.log(Level.INFO, "Update solution done!");
		return createFormatedSolution();
	}
	
	public void test(){
		for(int i = 0; i < requests.length; i++){
			if(requests[i].getSiblingCode().equals(""))
				continue;
			for(int j = 0; j < requests.length; j++){
				if(requests[i].getSiblingCode().equals(""))
					continue;
				if(requests[i].getSiblingCode().equals(requests[j].getSiblingCode())
					&&  requests[i].getPickupLocationId() == requests[j].getPickupLocationId()){
					ArrayList<Point> currPoints = getPointById(requests[i].getPickupLocationId(), pickupPoints);
					for(int k = 0; k < currPoints.size(); k++){
						ArrayList<Integer> name = point2nameList.get(currPoints.get(k));
						if((name.contains(requests[i].getIdPerson()) && !name.contains(requests[j].getIdPerson()))
							|| (name.contains(requests[j].getIdPerson()) && !name.contains(requests[i].getIdPerson())))
							System.out.println("point id = " + requests[i].getPickupLocationId()
							 + ", varIndex = " + currPoints.get(k).getVarIndex()
							 + ", siblingCode = " + requests[i].getSiblingCode()
							 + ", person1 = " + requests[i].getIdPerson()
							 + ", person2 = " + requests[j].getIdPerson());
					}
					
				}
			}
		}
		int sum = 0;
		for(Point p : point2nameList.keySet())
			sum += point2nameList.get(p).size();
		if(sum != requests.length)
			System.out.println("sum = " + sum);
		System.out.println("Test done!");
	}
	public void test(SchoolBusRoutingInput input){
		this.input = input;

		mapData();
		test();
	}
	public static void main(String[] args){
	
	}
}
