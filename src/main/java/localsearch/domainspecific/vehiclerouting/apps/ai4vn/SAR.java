package localsearch.domainspecific.vehiclerouting.apps.ai4vn;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.eq.Eq;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedNodeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.ConstraintViolationsVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.IndexOnRoute;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.MaxVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.plus.Plus;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;

class PairPoint {
	public double eval;
	public Point p1;
	public Point p2;
	public PairPoint(double eval, Point p1, Point p2) {
		super();
		this.eval = eval;
		this.p1 = p1;
		this.p2 = p2;
	}


}
class Move{
	public int i;
	public Point pickup;
	public Point delivery;
	public Point p1;
	public Point p2;
	public Move(int i, Point pickup, Point delivery, Point p1, Point p2) {
		super();
		this.i = i;
		this.pickup = pickup;
		this.delivery = delivery;
		this.p1 = p1;
		this.p2 = p2;
	}
	
}
public class SAR {
	public static final String PICKUP_PEOPLE = "PICKUP_PEOPLE";
	public static final String DELIVERY_PEOPLE = "DELIVERY_PEOPLE";
	public static final String PICKUP_PARCEL = "PICKUP_PARCEL";
	public static final String DELIVERY_PARCEL = "DELIVERY_PARCEL";
	public static final String START_POINT = "START_POINT";
	public static final String END_POINT = "END_POINT";
	
	

	private int nbPeopleRequests;
	private int nbParcelRequests;
	private int nbVehicles;
	private double[][] cost;
	private int[] parcelDemand;
	private int parcelCapacity;
	private int depot;
	private int N;
	private int[] x;
	private int[] y;

	private ArrayList<Point> pickupPeople;
	private ArrayList<Point> deliveryPeople;
	private ArrayList<Point> pickupParcel;
	private ArrayList<Point> deliveryParcel;
	private ArrayList<Point> pickupPoints;
	private ArrayList<Point> deliveryPoints;
	private HashMap<Point, String> mPoint2Type;

	private ArrayList<Point> startPoints;
	private ArrayList<Point> endPoints;
	private ArrayList<Point> allPoints;
	private ArrayList<Point> clientPoints;
	private HashMap<Point, Integer> mPoint2ID;
	private NodeWeightsManager nwm;
	private ArcWeightsManager awm;

	private VRManager mgr;
	private VarRoutesVR XR;
	public IFunctionVR[] accDemand;
	public IFunctionVR[] distance;
	public ConstraintSystemVR CS;
	public IFunctionVR obj;
	public IFunctionVR totalDistance;
	public LexMultiFunctions F;

	// visualization
	String[] colors = {"red","green","blue","brown","yellow","gray","black"};
	int canvasHeight = 900;
	int canvasWidth = 1000;
	int rad = 20;

	public void readData(String filename) {
		try {
			Scanner in = new Scanner(new File(filename));
			nbPeopleRequests = in.nextInt();
			nbParcelRequests = in.nextInt();
			nbVehicles = in.nextInt();
			N = (nbPeopleRequests + nbParcelRequests) * 2;
			parcelDemand = new int[N + 1];
			x = new int[N + 1];
			y = new int[N + 1];
			depot = in.nextInt();
			x[depot] = in.nextInt();
			y[depot] = in.nextInt();
			parcelCapacity = in.nextInt();
			for (int i = 1; i <= N; i++) {
				int id = in.nextInt();
				//System.out.println("readData, i = " + i + ", id = " + id);
				x[id] = in.nextInt();
				y[id] = in.nextInt();
				parcelDemand[id] = in.nextInt();
			}
			cost = new double[N+1][N+1];
			for(int i = 0; i <= N; i++){
				for(int j = 0; j <= N; j++)
					cost[i][j] = Math.sqrt((x[i]-x[j])*(x[i]-x[j])+(y[i]-y[j])*(y[i]-y[j]));
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void mapData() {
		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
		pickupPeople = new ArrayList<Point>();
		deliveryPeople = new ArrayList<Point>();
		pickupParcel = new ArrayList<Point>();
		deliveryParcel = new ArrayList<Point>();
		pickupPoints = new ArrayList<Point>();
		deliveryPoints = new ArrayList<Point>();
		clientPoints = new ArrayList<Point>();
		
		allPoints = new ArrayList<Point>();
		mPoint2ID = new HashMap<Point, Integer>();
		mPoint2Type = new HashMap<Point, String>();
		parcelDemand = new int[N+1];
		for (int k = 1; k <= nbVehicles; k++) {
			Point s = new Point(depot);
			startPoints.add(s);
			allPoints.add(s);
			mPoint2ID.put(s, depot);

			Point t = new Point(depot);
			endPoints.add(t);
			allPoints.add(t);
			mPoint2ID.put(t, depot);
			mPoint2Type.put(s, START_POINT);
			mPoint2Type.put(t, END_POINT);
		}
		int id = 0;
		for (int i = 1; i <= nbPeopleRequests; i++) {
			id++;
			Point pickup = new Point(id);
			id++;
			Point delivery = new Point(id);
			pickupPeople.add(pickup);
			deliveryPeople.add(delivery);
			pickupPoints.add(pickup);
			deliveryPoints.add(delivery);
			allPoints.add(pickup);
			allPoints.add(delivery);
			clientPoints.add(pickup);
			clientPoints.add(delivery);
			mPoint2Type.put(pickup, PICKUP_PEOPLE);
			mPoint2Type.put(delivery, DELIVERY_PEOPLE);
		}
		for (int i = 1; i <= nbParcelRequests; i++) {
			id++;
			Point pickup = new Point(id);
			id++;
			Point delivery = new Point(id);
			pickupParcel.add(pickup);
			deliveryParcel.add(delivery);
			pickupPoints.add(pickup);
			deliveryPoints.add(delivery);

			allPoints.add(pickup);
			allPoints.add(delivery);
			clientPoints.add(pickup);
			clientPoints.add(delivery);
			mPoint2Type.put(pickup, PICKUP_PARCEL);
			mPoint2Type.put(delivery, DELIVERY_PARCEL);
		}
		for(Point p: allPoints) mPoint2ID.put(p, p.ID);
		
		nwm = new NodeWeightsManager(allPoints);
		awm = new ArcWeightsManager(allPoints);
		for (Point p : pickupParcel)
			nwm.setWeight(p, parcelDemand[mPoint2ID.get(p)]);
		for (Point p : deliveryParcel)
			nwm.setWeight(p, parcelDemand[mPoint2ID.get(p)]);

		for (Point p : startPoints)
			nwm.setWeight(p, 0);
		for (Point p : endPoints)
			nwm.setWeight(p, 0);

		for (Point p1 : allPoints) {
			int i = mPoint2ID.get(p1);
			for (Point p2 : allPoints) {
				int j = mPoint2ID.get(p2);
				awm.setWeight(p1, p2, cost[i][j]);
			}
		}
	}

	public void stateModel() {
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		for (int i = 0; i < startPoints.size(); i++) {
			XR.addRoute(startPoints.get(i), endPoints.get(i));
		}
		for (Point p : clientPoints)
			XR.addClientPoint(p);

		CS = new ConstraintSystemVR(mgr);
		AccumulatedWeightNodesVR awn = new AccumulatedWeightNodesVR(XR, nwm);
		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		accDemand = new IFunctionVR[XR.getNbRoutes()];

		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			accDemand[k - 1] = new AccumulatedNodeWeightsOnPathVR(awn,
					XR.endPoint(k));
			CS.post(new Leq(accDemand[k - 1], parcelCapacity));
		}

		distance = new IFunctionVR[XR.getNbRoutes()];
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			distance[k - 1] = new AccumulatedEdgeWeightsOnPathVR(awe,
					XR.endPoint(k));
		}

		totalDistance = new TotalCostVR(XR, awm);

		F = new LexMultiFunctions();
		F.add(new ConstraintViolationsVR(CS));
		F.add(totalDistance);

		mgr.close();
	}

	public PairPoint selectBestPointInsertPeople(Point pickup, Point delivery) {
		Point sel_p1 = null;
		Point sel_p2 = null;
		double min = Integer.MAX_VALUE;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {
				if (mPoint2Type.get(p).equals(PICKUP_PEOPLE))
					continue;
				if (CS.evaluateAddTwoPoints(pickup, p, delivery,p) <= 0) {
					double eval = totalDistance.evaluateAddTwoPoints(pickup,
							p, delivery,p);
					if (eval < min) {
						min = eval;
						sel_p1 = p;
						sel_p2 = p;
					}
				}

			}
		}
		return new PairPoint(min,sel_p1, sel_p2);
	}

	public PairPoint selectBestPointInsertParcel(Point pickup, Point delivery) {
		Point sel_p1 = null;
		Point sel_p2 = null;
		double min = Integer.MAX_VALUE;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			for (Point p1 = XR.startPoint(k); p1 != XR.endPoint(k); p1 = XR
					.next(p1)) {
				for (Point p2 = p1; p2 != XR.endPoint(k); p2 = XR.next(p2)) {
					if (mPoint2Type.get(p1).equals(PICKUP_PEOPLE))
						continue;
					if (mPoint2Type.get(p2).equals(PICKUP_PEOPLE))
						continue;
					
					if (CS.evaluateAddTwoPoints(pickup, p1, delivery, p2) <= 0) {
						double eval = totalDistance.evaluateAddTwoPoints(pickup, p1, delivery,p2);
						if (eval < min) {
							min = eval;
							sel_p1 = p1;
							sel_p2 = p2;
						}
					}
				}
			}
		}
		return new PairPoint(min,sel_p1, sel_p2);
	}
	public Move selectMove(HashSet<Integer> cand){
		int sel_i = -1;	Point sel_pickup = null; Point sel_delivery = null; Point sel_p1 = null; Point sel_p2 = null;
		double min_eval = Integer.MAX_VALUE;
		for (int i : cand) {
			Point pickup = pickupPoints.get(i);	Point delivery = deliveryPoints.get(i);
			PairPoint pp = null;
			if(mPoint2Type.get(pickup).equals(PICKUP_PEOPLE)){
				pp = selectBestPointInsertPeople(pickup, delivery);
			}else if(mPoint2Type.get(pickup).equals(PICKUP_PARCEL)){
				pp = selectBestPointInsertParcel(pickup, delivery);
			}
			if(pp != null && min_eval > pp.eval){
				//if(min_eval > pp.eval){
					min_eval = pp.eval;	sel_p1 = pp.p1;	sel_p2 = pp.p2;
					sel_i = i;
					sel_pickup = pickup; sel_delivery = delivery;
				
			}
		}
		if(sel_i < 0) return null;
		return new Move(sel_i,sel_pickup, sel_delivery, sel_p1,sel_p2);
	}
	public void greedySearch() {
		HashSet<Integer> cand = new HashSet<Integer>();
		for(int i = 0; i < pickupPoints.size(); i++){
			cand.add(i);
			System.out.println("cand.add(" + i + ")");
		}
		
		while (cand.size() > 0) {
			Move m = selectMove(cand);
			if(m != null){
				//mgr.performAddTwoPoints(sel_pickup, sel_p1, sel_delivery, sel_p2);
				System.out.println("sel_pickup = " + m.pickup.ID + ", sel_delivery = " + m.delivery.ID + ", sel_p1 = " + m.p1.ID + ", sel_p2 = " 
				+ m.p2.ID + ", XR = " + XR.toString());
				mgr.performAddOnePoint(m.delivery, m.p2);
				mgr.performAddOnePoint(m.pickup, m.p1);
				cand.remove(m.i);
			}else{
				break;
			}
		}
		System.out.println("solution "+ XR.toString());
	}
	
	public void drawCircle(PrintWriter out, int centerX, int centerY, int radius, String label){
	      out.println("ctx.beginPath();");
	      out.println("ctx.arc(" + centerX + "," + centerY + "," +  radius + ", 0, 2 * Math.PI, false);");
	      out.println("ctx.fillStyle = 'white';");
	      out.println("ctx.fill();");
	      out.println("ctx.lineWidth = 2;");
	      out.println("ctx.strokeStyle = '#003300';");
	      out.println("ctx.stroke();");
		
	      out.println("ctx.font = \"20px Arial\";");
	      out.println("ctx.fillStyle = 'black';");
	      out.println("ctx.fillText(\"" + label + "\", " + (centerX - 5) + "," +  (centerY + 5) + ");");
	}
	public void drawLine(PrintWriter out, int fromX, int fromY, int toX, int toY, String color){
		out.println("ctx.beginPath();");
		out.println("ctx.moveTo(" + fromX + "," + fromY + ")");
		out.println("ctx.lineTo(" + toX + "," + toY + ")");
		out.println("ctx.strokeStyle = '" + color + "';");
		out.println("ctx.stroke()");
	}

	public void printHTML(String filename){
		try{
			PrintWriter out = new PrintWriter(filename);
			out.println("<!DOCTYPE html>");
			out.println("<html>");
			out.println("<body>");
			out.println("<canvas id=\"myCanvas\" width=\"" + canvasWidth + "\" height=\"" + canvasHeight + "\"></canvas>");
			out.println("<script>");
			out.println("var canvas = document.getElementById(\"myCanvas\");");
			out.println("var ctx = canvas.getContext(\"2d\");");
			
			for(int k = 1; k <= XR.getNbRoutes(); k++){
				Point s = XR.startPoint(k);
				//int from = mPoint2ID.get(s);
				Point p = s;
				Point np = null;
				while(p != XR.endPoint(k)){
					np = XR.next(p);
					int from = mPoint2ID.get(p);
					int to = mPoint2ID.get(np);
					drawLine(out, x[from], y[from], x[to], y[to],colors[k-1]);
					p = XR.next(p);
				}
				int from = mPoint2ID.get(p);
				int to = mPoint2ID.get(np);
				drawLine(out, x[from], y[from], x[to], y[to],colors[k-1]);
			}
			drawCircle(out, x[0], y[0], rad,"0");
			for(int i = 1; i <= N; i++){
				drawCircle(out, x[i], y[i], 20,i+"");
			}
			out.println("</script>");
			out.println("</body>");
			out.println("</html>");
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SAR app = new SAR();
		app.readData("data/sar/sar-3-4-2.inp");
		app.mapData();
		app.stateModel();
		app.greedySearch();
		app.printHTML("sar.html");
	}

}
