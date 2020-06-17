package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting;//package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.logging.FileHandler;
//import java.util.logging.Formatter;
//import java.util.logging.Handler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;
//
//import org.apache.poi.EncryptedDocumentException;
//import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.BusInfomation;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.BusRoute;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.RouteElement;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRequest;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRoutingSolution;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.StatisticInformation;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.Vehicle;
//import localsearch.domainspecific.vehiclerouting.apps.sharedaride.Info;
//import localsearch.domainspecific.vehiclerouting.apps.sharedaride.SearchInput;
//import localsearch.domainspecific.vehiclerouting.apps.sharedaride.ShareARide;
//import localsearch.domainspecific.vehiclerouting.apps.sharedaride.SolutionShareARide;
//import localsearch.domainspecific.vehiclerouting.apps.sharedaride.Search.ALNSwithSA;
//import localsearch.domainspecific.vehiclerouting.vrp.Constants;
//import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
//import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
//import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
//import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
//import localsearch.domainspecific.vehiclerouting.vrp.constraints.timewindows.CEarliestArrivalTimeVR;
//import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
//import localsearch.domainspecific.vehiclerouting.vrp.entities.LexMultiValues;
//import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
//import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
//import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
//import localsearch.domainspecific.vehiclerouting.vrp.functions.CapacityConstraintViolationsVR;
//import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
//import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
//import localsearch.domainspecific.vehiclerouting.vrp.invariants.EarliestArrivalTimeVR;
//import localsearch.domainspecific.vehiclerouting.vrp.invariants.RelatedPointBuckets;
//
//public class BusScheduling {
//	public static final Logger LOGGER = Logger.getLogger("Logger");
//
//	public static int PEOPLE = 1;
//	public int scale = 100000;
//	public ArrayList<Point> points;
//	public ArrayList<Point> pickupPoints;
//	// public ArrayList<Point> deliveryPoints;
//	public HashMap<Point, String> type;
//	public ArrayList<Point> startPoints;
//	public ArrayList<Point> stopPoints;
//	public ArrayList<Point> rejectPoints;
//	public HashMap<Point, Integer> earliestAllowedArrivalTime;
//	public HashMap<Point, Integer> serviceDuration;
//	public HashMap<Point, Integer> lastestAllowedArrivalTime;
//	public double[] capList;
//	public HashMap<Point, ArrayList<String>> nbCus;
//
//	public int nVehicle;
//	public static int nRequest;
//	public static double MAX_DISTANCE;
//
//	public ArcWeightsManager awm;
//	public NodeWeightsManager nwm;
//	public VRManager mgr;
//	public VarRoutesVR XR;
//	public ConstraintSystemVR S;
//	public IFunctionVR objective;
//	public CEarliestArrivalTimeVR ceat;
//	public LexMultiValues valueSolution;
//	public EarliestArrivalTimeVR eat;
//	public CEarliestArrivalTimeVR cEarliest;
//	public CapacityConstraintViolationsVR capCons;
//	public AccumulatedWeightEdgesVR accDisInvr;
//	public HashMap<Point, IFunctionVR> accDisF;
//
//	int cntTimeRestart;
//	int cntInteration;
//
//	public BusScheduling(DataProcessing info) {
//		this.rejectPoints = new ArrayList<Point>();
//		this.nVehicle = info.nVehicle;
//		this.nRequest = info.nRequest;
//		points = info.points;
//		pickupPoints = info.pickupPs;
//		// deliveryPoints = new ArrayList<Point>();
//		startPoints = info.startPoints;
//		stopPoints = info.stopPoints;
//		type = info.type;
//		this.capList = info.capList;
//		this.nbCus = info.nbCus;
//		earliestAllowedArrivalTime = info.earliestAllowedArrivalTime;
//		serviceDuration = info.serviceDuration;
//		lastestAllowedArrivalTime = info.lastestAllowedArrivalTime;
//
//		awm = new ArcWeightsManager(points);
//		nwm = new NodeWeightsManager(points);
//		double max_dist = Double.MIN_VALUE;
//		for (int i = 0; i < points.size(); i++) {
//			for (int j = 0; j < points.size(); j++) {
//				double tmp_cost = info.costT[i][j];
//				awm.setWeight(points.get(i), points.get(j), tmp_cost);
//				max_dist = tmp_cost > max_dist ? tmp_cost : max_dist;
//			}
//			nwm.setWeight(points.get(i), nbCus.get(points.get(i)).size());
//		}
//		MAX_DISTANCE = max_dist;
//	}
//
//	public void stateModel() {
//		mgr = new VRManager();
//		XR = new VarRoutesVR(mgr);
//		S = new ConstraintSystemVR(mgr);
//		for (int i = 0; i < nVehicle; ++i)
//			XR.addRoute(startPoints.get(i), stopPoints.get(i));
//
//		for (int i = 0; i < nRequest; ++i) {
//			Point pickup = pickupPoints.get(i);
//			XR.addClientPoint(pickup);
//		}
//
//		// time windows
//		eat = new EarliestArrivalTimeVR(XR, awm, earliestAllowedArrivalTime,
//				serviceDuration);
//		cEarliest = new CEarliestArrivalTimeVR(eat, lastestAllowedArrivalTime);
//
//		capCons = new CapacityConstraintViolationsVR(XR, nwm, capList);
//		S.post(cEarliest);
//		objective = new TotalCostVR(XR, awm);
//		valueSolution = new LexMultiValues();
//		valueSolution.add(S.violations());
//		valueSolution.add(objective.getValue());
//
//		mgr.close();
//	}
//
//	private boolean checkSizeBus(int k, String name) {
//		String lw = name.toLowerCase();
//		if ((lw.contains("ngÃµ") || lw.contains("ngÃ¡ch") || lw
//				.contains("háº»m")) && capList[k] != 15)
//			return false;
//		else
//			return true;
//	}
//
//	public void greedyInitSolution() {
//		double currtime = System.currentTimeMillis();
//
//		for (int i = 0; i < pickupPoints.size(); i++) {
//			// printBucket();
//			Point pickup = pickupPoints.get(i);
//			if (XR.route(pickup) != Constants.NULL_POINT)
//				continue;
//
//			Point pre_pick = null;
//			double best_objective = Double.MAX_VALUE;
//			for (int r = 1; r <= XR.getNbRoutes(); r++) {
//				Point st = XR.getStartingPointOfRoute(r);
//				for (Point p = st; p != XR.getTerminatingPointOfRoute(r); p = XR
//						.next(p)) {
//					if (S.evaluateAddOnePoint(pickup, p) == 0
//							&& capCons.evaluateAddOnePoint(pickup, p) == 0
//							&& checkSizeBus(r, pickup.getName())
//							&& checkDistanceConstraint(pickup, p, st)) {
//						double cost = objective.evaluateAddOnePoint(pickup, p);
//						if (cost < best_objective) {
//							best_objective = cost;
//							pre_pick = p;
//						}
//					}
//				}
//			}
//			if (pre_pick == null)
//				rejectPoints.add(pickup);
//			else {
//				mgr.performAddOnePoint(pickup, pre_pick);
//			}
//		}
//		LOGGER.log(Level.INFO, "people reject = " + rejectPoints.size()
//				+ ", time for inserting reqs = "
//				+ (System.currentTimeMillis() - currtime) / 1000);
//	}
//
//	// public Point getFarthestReq(Point fr, Point st){
//	// int r = XR.route(st);
//	// double maxTime = -1;
//	// Point far = fr;
//	// for(Point p = XR.next(fr); p != XR.getTerminatingPointOfRoute(r); p =
//	// XR.next(p)){
//	// double w = awm.getWeight(p, st);
//	// if(maxTime < w){
//	// maxTime = w;
//	// far = p;
//	// }
//	// }
//	// return far;
//	// }
//	// public void sortReqsonRoutes(){
//	// for(int r = 1; r <= XR.getNbRoutes(); r++){
//	// Point st = XR.getStartingPointOfRoute(r);
//	// for(Point p = XR.next(st); p != XR.getTerminatingPointOfRoute(r); p =
//	// XR.next(p)){
//	// Point q = getFarthestReq(p, st);
//	//
//	// }
//	// }
//	// }
//
//	// public boolean checkDistanceConstraint(Point x, Point y, Point st){
//	// if(y == st && awm.getWeight(x, st) >= awm.getWeight(XR.next(st), st))
//	// return true;
//	// if(awm.getWeight(x, st) <= awm.getWeight(y, st) && awm.getWeight(x, st)
//	// >= awm.getWeight(XR.next(y), st))
//	// return true;
//	// return false;
//	// }
//
//	public boolean checkDistanceConstraint(Point x, Point y, Point st) {
//		double xst = awm.getWeight(x, st);
//		double yst = awm.getWeight(y, st);
//		double yx = awm.getWeight(y, x);
//		double nyst = awm.getWeight(XR.next(y), st);
//		double xny = awm.getWeight(x, XR.next(y));
//		int r = XR.route(st);
//		Point en = XR.getTerminatingPointOfRoute(r);
//		double yny = awm.getWeight(y, XR.next(y));
//		double te = eat.getEarliestArrivalTime().get(en);
//		double sttime = eat.getEarliestArrivalTime(XR.next(st));
//		if (y != st) {
//			double endtime = te + yx + xny - yny;
//			double drtime = awm.getWeight(XR.next(st), en);
//			if (endtime - sttime > 1.5 * drtime)
//				return false;
//		} else {
//			double endtime = te + xny;
//			double drtime = awm.getWeight(x, en);
//			if (endtime - sttime > 1.5 * drtime)
//				return false;
//		}
//		// if(y == st && xst < nyst)
//		// return false;
//		// if(xst > yst || xst < nyst)
//		// return false;
//		// // HashMap<Point, Double> earliestArrivalTime =
//		// eat.getEarliestArrivalTime();
//		// // double endTime =
//		// earliestArrivalTime.get(XR.getTerminatingPointOfRoute(XR.route(y)));
//		// if(y == st && xny + nyst > 1.3*xst)
//		// return false;
//		// if(y != st && yx + xst > 1.3*yst)
//		// return false;
//		return true;
//	}
//
//	public boolean checkTriangleConstraint(Point x, Point y, Point st) {
//
//		return true;
//	}
//
////	public SchoolBusRoutingSolution printSolutionJson(String dir, String typeofReq, String tarname,
////			int lv, DataProcessing info) {
////		BusRoute[] busRoutes = new BusRoute[XR.getNbRoutes()];
////		
////		double[] per = new double[XR.getNbRoutes() + 1];
////		int[] newCapList = new int[XR.getNbRoutes() + 1];
////		HashMap<String, String> hs2bus = new HashMap<String, String>();
////		
////		int i = 1;
////		
////		for (int r = 1; r <= XR.getNbRoutes(); r++) {
////			int c = 0;
////			int g = 0;
////			Point st = XR.getStartingPointOfRoute(r);
////			Point pre = st;
////			Point en = XR.getTerminatingPointOfRoute(r);
////			Point pre_back = en;
////			int endtime = 59400;
////			int nb = XR.index(XR.getTerminatingPointOfRoute(r)) + 1;
////			RouteElement[] nodes = new RouteElement[nb];
////			
////			for (Point p = XR.getStartingPointOfRoute(r); p != XR
////					.getTerminatingPointOfRoute(r); p = XR.next(p)) {
////				String str = "Bus-" + r;
////				
////				ArrayList<String> cus = nbCus.get(p);
////				String[] cusStrs = new String[cus.size()];
////				for (int k = 0; k < cus.size(); k++){
////					hs2bus.put(cus.get(k), str);
////					cusStrs[k] = cus.get(k);
////				}
////				nodes[g] = new RouteElement(p.ID, "PICKUP_POINT", (int)eat.getEarliestArrivalTime(p),
////						(int)(eat.getEarliestArrivalTime(p) + 120), cusStrs);
////				g++;
////				i++;
////				c += cus.size();
////			}
////			
////			c += nbCus.get(XR.getTerminatingPointOfRoute(r)).size();
////			String inc = "";
////			if (c < 16) {
////				inc = c + "/15";
////				per[r] = c * 100 / 15;
////				newCapList[r] = 15;
////			} else if (c >= 16 && c < 29) {
////				inc = c + "/28";
////				per[r] = c * 100 / 28;
////				newCapList[r] = 28;
////			} else {
////				inc = c + "/44";
////				per[r] = c * 100 / 44;
////				newCapList[r] = 44;
////			}
////			ArrayList<String> cus = nbCus.get(XR.getTerminatingPointOfRoute(r));
////			String[] cusStrs = new String[cus.size()];
////			for (int k = 0; k < cus.size(); k++){
////				cusStrs[k] = cus.get(k);
////			}
////			nodes[g] = new RouteElement(XR.getTerminatingPointOfRoute(r).ID, "DELIVERY_POINT",
////					(int)eat.getEarliestArrivalTime(XR.getTerminatingPointOfRoute(r)),
////					(int)(eat.getEarliestArrivalTime(XR.getTerminatingPointOfRoute(r)) + 120), cusStrs);
////			Vehicle vehicle = new Vehicle(r, newCapList[r]);
////			busRoutes[r-1] = new BusRoute(vehicle, c, nodes);
////			
////			i++;
////		}
////		
////		ArrayList<SchoolBusRequest> unsch = new ArrayList<SchoolBusRequest>();
////		for (int k = 0; k < rejectPoints.size(); k++) {
////			Point p = rejectPoints.get(k);
////			for(int m = 0; m < nbCus.get(p).size(); m++){
////				for(int n = 0; n < info.input.getRequests().length; n++){
////					SchoolBusRequest s = info.input.getRequests()[n];
////					if(s.getPickupLocationId() == p.ID)
////						unsch.add(s);
////				}
////			}
////		}
////		SchoolBusRequest[] unScheduledRequests = new SchoolBusRequest[unsch.size()];
////		for(int k = 0; k < unsch.size(); k++)
////			unScheduledRequests[k] = unsch.get(k);
////
////		
////		BusInfomation[] busInfomation = new BusInfomation[XR.getNbRoutes()];
////		int l = 0;
////		for (int r = 1; r <= XR.getNbRoutes(); r++) {
////			if(XR.getStartingPointOfRoute(r).ID != XR.next(XR.getStartingPointOfRoute(r)).ID)
////				l++;
////			String str = "Bus-" + r;
////			double t = eat.getEarliestArrivalTime(XR
////					.getTerminatingPointOfRoute(r))
////					- eat.getEarliestArrivalTime(XR.next(XR
////							.getStartingPointOfRoute(r)));
////			int h = (int) (t / 3600);
////			int m = (int) ((t - h * 3600) / 60);
////			int s = (int) (t - h * 3600 - m * 60);
////			str = h + ":" + m + ":" + s;
////
////			busInfomation[r-1] = new BusInfomation(r, newCapList[r], per[r],
////					XR.index(XR.getTerminatingPointOfRoute(r)), (int)t);
////		}
////		
////		StatisticInformation statisticInformation = new StatisticInformation(10000,
////				l, 
////				busInfomation);
////		return new SchoolBusRoutingSolution(busRoutes, 
////				unScheduledRequests, statisticInformation);
////	}
//	
//	public void printSolution(String dir, String typeofReq, String tarname,
//			int lv) {
//		double[] per = new double[XR.getNbRoutes() + 1];
//		int[] newCapList = new int[XR.getNbRoutes() + 1];
//		HashMap<String, String> hs2bus = new HashMap<String, String>();
//		String fo = dir + "solution-" + lv + ".xls";
//		XSSFWorkbook workbook = new XSSFWorkbook();
//
//		XSSFSheet reqSheet = workbook.createSheet("route");
//		Row row = reqSheet.createRow(0);
//		Cell cell = row.createCell(0);
//		cell.setCellValue("BusID");
//		cell = row.createCell(1);
//		cell.setCellValue("Ä�iá»ƒm Ä‘Ã³n");
//		cell = row.createCell(2);
//		cell.setCellValue("Thá»�i gian Ä‘Ã³n");
//		cell = row.createCell(3);
//		cell.setCellValue("Sá»‘ HS");
//		cell = row.createCell(4);
//		cell.setCellValue("Danh Sach HS");
//		cell = row.createCell(5);
//		cell.setCellValue("Lat");
//		cell = row.createCell(6);
//		cell.setCellValue("Lng");
//		cell = row.createCell(7);
//		cell.setCellValue("ThÃ¡Â»ï¿½i gian Ä‘i tháº³ng");
//		cell = row.createCell(8);
//		cell.setCellValue("Thá»�i gian ngá»“i trÃªn xe chiá»�u Ä‘i");
//		cell = row.createCell(9);
//		cell.setCellValue("Ä�iá»ƒm tráº£");
//		cell = row.createCell(10);
//		cell.setCellValue("Thá»�i gian tráº£");
//		cell = row.createCell(11);
//		cell.setCellValue("Thá»�i gian ngá»“i trÃªn xe chiá»�u vá»�");
//
//		int i = 1;
//		for (int r = 1; r <= XR.getNbRoutes(); r++) {
//			int c = 0;
//			Point st = XR.getStartingPointOfRoute(r);
//			Point pre = st;
//			Point en = XR.getTerminatingPointOfRoute(r);
//			Point pre_back = en;
//			int endtime = 59400;
//			for (Point p = XR.getStartingPointOfRoute(r); p != XR
//					.getTerminatingPointOfRoute(r); p = XR.next(p)) {
//				row = reqSheet.createRow(i);
//				String str = "Bus-" + r;
//				cell = row.createCell(0);
//				cell.setCellValue(str);
//				ArrayList<String> cus = nbCus.get(p);
//				for (int k = 0; k < cus.size(); k++)
//					hs2bus.put(cus.get(k), str);
//				cell = row.createCell(1);
//				cell.setCellValue(p.getName());
//				double t = eat.getEarliestArrivalTime(p);
//				int h = (int) (t / 3600);
//				int m = (int) ((t - h * 3600) / 60);
//				int s = (int) (t - h * 3600 - m * 60);
//				str = h + ":" + m + ":" + s;
//				cell = row.createCell(2);
//				cell.setCellValue(str);
//				cell = row.createCell(3);
//				cell.setCellValue(cus.size());
//				cell = row.createCell(4);
//				cell.setCellValue(cus.toString());
//				cell = row.createCell(5);
//				cell.setCellValue(p.getX());
//				cell = row.createCell(6);
//				cell.setCellValue(p.getY());
//				c += cus.size();
//				cell = row.createCell(7);
//				cell.setCellValue(awm.getWeight(p, st));
//				cell = row.createCell(8);
//				cell.setCellValue(eat.getEarliestArrivalTime(XR
//						.getTerminatingPointOfRoute(r)) - t - 120);
//				cell = row.createCell(9);
//				cell.setCellValue(pre_back.getName());
//
//				double t2 = endtime + awm.getWeight(en, pre_back);
//				int h2 = (int) (t2 / 3600);
//				int m2 = (int) ((t2 - h2 * 3600) / 60);
//				int s2 = (int) (t2 - h2 * 3600 - m2 * 60);
//				str = h2 + ":" + m2 + ":" + s2;
//				cell = row.createCell(10);
//				cell.setCellValue(str);
//				cell = row.createCell(11);
//				cell.setCellValue(awm.getWeight(en, pre_back));
//				endtime = (int) (endtime + awm.getWeight(en, pre_back) + 120);
//				en = pre_back;
//				pre_back = XR.prev(pre_back);
//
//				i++;
//			}
//			row = reqSheet.createRow(i);
//			String str = "Bus-" + r;
//			cell = row.createCell(0);
//			cell.setCellValue(str);
//			cell = row.createCell(1);
//			cell.setCellValue(XR.getTerminatingPointOfRoute(r).getName());
//
//			double t = eat.getEarliestArrivalTime(XR
//					.getTerminatingPointOfRoute(r));
//			int h = (int) (t / 3600);
//			int m = (int) ((t - h * 3600) / 60);
//			int s = (int) (t - h * 3600 - m * 60);
//			str = h + ":" + m + ":" + s;
//			cell = row.createCell(2);
//			cell.setCellValue(str);
//			c += nbCus.get(XR.getTerminatingPointOfRoute(r)).size();
//			String inc = "";
//			if (c < 16) {
//				inc = c + "/15";
//				per[r] = c * 100 / 15;
//				newCapList[r] = 15;
//			} else if (c >= 16 && c < 29) {
//				inc = c + "/28";
//				per[r] = c * 100 / 28;
//				newCapList[r] = 28;
//			} else {
//				inc = c + "/44";
//				per[r] = c * 100 / 44;
//				newCapList[r] = 44;
//			}
//
//			cell = row.createCell(3);
//			cell.setCellValue(inc);
//			cell = row.createCell(5);
//			cell.setCellValue(XR.getTerminatingPointOfRoute(r).getX());
//			cell = row.createCell(6);
//			cell.setCellValue(XR.getTerminatingPointOfRoute(r).getY());
//			cell = row.createCell(7);
//			cell.setCellValue(awm.getWeight(XR.getTerminatingPointOfRoute(r),
//					st));
//			cell = row.createCell(8);
//			cell.setCellValue(eat.getEarliestArrivalTime(XR
//					.getTerminatingPointOfRoute(r)) - t - 120);
//			cell = row.createCell(9);
//			cell.setCellValue(pre_back.getName());
//			cell = row.createCell(10);
//			cell.setCellValue(endtime + awm.getWeight(en, pre_back));
//			cell = row.createCell(11);
//			cell.setCellValue(awm.getWeight(en, pre_back));
//			endtime = (int) (endtime + awm.getWeight(en, pre_back) + 120);
//
//			i++;
//		}
//
//		XSSFSheet rejectSheet = workbook.createSheet("rejectPoints");
//		row = rejectSheet.createRow(0);
//		cell = row.createCell(0);
//		cell.setCellValue("Ä�iá»ƒm");
//		cell = row.createCell(1);
//		cell.setCellValue("Sá»‘ HS");
//		cell = row.createCell(2);
//		cell.setCellValue("Danh sÃ¡ch HS");
//
//		for (int k = 0; k < rejectPoints.size(); k++) {
//			Point p = rejectPoints.get(k);
//			row = rejectSheet.createRow(k + 1);
//			cell = row.createCell(0);
//			cell.setCellValue(p.getName());
//			cell = row.createCell(1);
//			cell.setCellValue(nbCus.get(p).size());
//			cell = row.createCell(2);
//			cell.setCellValue(nbCus.get(p).toString());
//		}
//
//		XSSFSheet stSheet = workbook.createSheet("Statistic");
//		row = stSheet.createRow(0);
//		cell = row.createCell(0);
//		cell.setCellValue("BusID");
//		cell = row.createCell(1);
//		cell.setCellValue("Loáº¡i xe");
//		cell = row.createCell(2);
//		cell.setCellValue("Tá»‰ lá»‡ chá»Ÿ (%)");
//		cell = row.createCell(3);
//		cell.setCellValue("Sá»‘ Ä‘iá»ƒm dá»«ng");
//		cell = row.createCell(4);
//		cell.setCellValue("Thá»�i gian di chuyá»ƒn");
//
//		for (int r = 1; r <= XR.getNbRoutes(); r++) {
//			String str = "Bus-" + r;
//			row = stSheet.createRow(r);
//			cell = row.createCell(0);
//			cell.setCellValue(str);
//			cell = row.createCell(1);
//			cell.setCellValue(newCapList[r]);
//			cell = row.createCell(2);
//			cell.setCellValue(per[r]);
//			cell = row.createCell(3);
//			cell.setCellValue(XR.index(XR.getTerminatingPointOfRoute(r)));
//			double t = eat.getEarliestArrivalTime(XR
//					.getTerminatingPointOfRoute(r))
//					- eat.getEarliestArrivalTime(XR.next(XR
//							.getStartingPointOfRoute(r)));
//			int h = (int) (t / 3600);
//			int m = (int) ((t - h * 3600) / 60);
//			int s = (int) (t - h * 3600 - m * 60);
//			str = h + ":" + m + ":" + s;
//			cell = row.createCell(4);
//			cell.setCellValue(str);
//		}
//
//		XSSFSheet req2busSheet = workbook.createSheet("request2busId");
//		row = req2busSheet.createRow(0);
//		cell = row.createCell(0);
//		cell.setCellValue("MÃ£ HS");
//		cell = row.createCell(1);
//		cell.setCellValue("BusID");
//
//		int k = 1;
//		for (String key : hs2bus.keySet()) {
//			row = req2busSheet.createRow(k);
//			cell = row.createCell(0);
//			cell.setCellValue(key);
//			cell = row.createCell(1);
//			cell.setCellValue(hs2bus.get(key));
//			k++;
//		}
//	}
//
////	public SolutionVinPro search(int maxIter, int timeLimit, SeachInputVinPro si) {
////		ALNSforVinPro alns = new ALNSforVinPro(mgr, objective, S, capCons, eat,
////				awm, si);
////		return alns.search(maxIter, timeLimit);
////	}
//
//	public static void main(String[] args) {
//		try {
//			String dir = "data/vinschool/";
//
//			int timeLimit = 36000000;
//			int nIter = 30000;
//			int lv = Constants.THCS;
//			String type = "Táº¡i Ä‘iá»ƒm";
//			String name = "Trung há»�c Harmony";
//			Handler fileHandler;
//			Formatter simpleFormater;
//			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
//			Date date = new Date();
//			// System.out.println(dateFormat.format(date));
//
//			fileHandler = new FileHandler("output/vinschool/"
//					+ dateFormat.format(date) + "_evaluateRealData-" + lv
//					+ ".txt");
//			simpleFormater = new SimpleFormatter();
//
//			LOGGER.addHandler(fileHandler);
//
//			fileHandler.setFormatter(simpleFormater);
//
//			// String description =
//			// "\n\n\t RUN WITH 13 REMOVAL AND 14 INSERTION (3,1,-5,1) \n\n";
//			// LOGGER.log(Level.INFO, description);
//
//			LOGGER.log(Level.INFO, "Read data");
//
//			DataProcessing info = new DataProcessing(30, 30, 30);
//
//			info.readDataFile(dir, type, name, lv);
//			BusScheduling bs = new BusScheduling(info);
//
//			LOGGER.log(Level.INFO, "Read data done --> Create model");
//			bs.stateModel();
//
//			LOGGER.log(Level.INFO, "Create model done --> Init solution");
//			double currTime = System.currentTimeMillis();
//			bs.greedyInitSolution();
//
//			LOGGER.log(
//					Level.INFO,
//					"Init solution done. At start search number of reject points = "
//							+ bs.rejectPoints.size() + "    violations = "
//							+ bs.S.violations() + "   cost = "
//							+ bs.objective.getValue() + ", init time = "
//							+ (System.currentTimeMillis() - currTime) / 1000);
//			SeachInputVinPro si = new SeachInputVinPro(bs.pickupPoints,
//					bs.rejectPoints, bs.capList, bs.earliestAllowedArrivalTime,
//					bs.serviceDuration, bs.lastestAllowedArrivalTime);
////			SolutionVinPro best_solution = bs.search(nIter, timeLimit, si);
////
////			LOGGER.log(Level.INFO,
////					"Search done. At end search number of reject points = "
////							+ best_solution.get_rejectPoints().size()
////							+ "   cost = " + best_solution.get_cost());
////			best_solution.copy2XR(bs.XR);
////
////			bs.rejectPoints = best_solution.get_rejectPoints();
////			bs.printSolution("output/vinschool/", type, name, lv);
//			fileHandler.close();
//		} catch (SecurityException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//}
