package com.socolabs.mo.components.maps;

import java.io.File;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Scanner;

import com.socolabs.mo.components.algorithms.nearestlocation.KDTree;
import com.socolabs.mo.components.algorithms.nearestlocation.QuadTree;
import com.socolabs.mo.components.algorithms.shortestpath.PQShortestPath;
import com.socolabs.mo.components.maps.distanceelementquery.DistanceElement;
import com.socolabs.mo.components.maps.distanceelementquery.DistanceElementQuery;
import com.socolabs.mo.components.maps.distanceelementquery.GeneralDistanceElement;
import com.socolabs.mo.components.maps.distanceelementquery.LatLngInput;
import com.socolabs.mo.components.maps.graphs.Arc;
import com.socolabs.mo.components.maps.graphs.Graph;
import com.socolabs.mo.components.maps.graphs.Node;
import com.socolabs.mo.components.maps.graphs.Segment;
import com.socolabs.mo.components.maps.utils.GoogleMapsQuery;
import com.socolabs.mo.components.maps.utils.LatLng;
import com.socolabs.mo.components.movingobjects.ILocation;
import com.socolabs.mo.model.modelmap.MapWindow;
import com.google.gson.Gson;

import java.util.*;
public class GISMap {
//	private KDTree kdTree;
	private QuadTree quadTree;
	private ArrayList<Node> points;
	private HashMap<Integer, Integer> mID2Index;
	public static GoogleMapsQuery G = new GoogleMapsQuery();
	
	private Graph g;
	private Random rand = new Random(1000);

	public GISMap(){
		//loadMap("data/SanfranciscoRoad-connected-contracted-5.txt");
		try {
			loadMap("data/HCMRoad-connected.txt");
		} catch (Exception e) {

		}
	}

	public GISMap(String dataMapPath){
		//loadMap("data/SanfranciscoRoad-connected-contracted-5.txt");
		try {
			loadMap(dataMapPath);
		} catch (Exception e) {

		}
	}

	public Point getRandomPoint() {
		return (Point) points.get(rand.nextInt(points.size()));
	}

	public String name(){
		return "GISMap";
	}
	public String computeCoordinateWindows(){
		String json = "{";
		double minLat = Integer.MAX_VALUE;
		double minLng = Integer.MAX_VALUE;
		double maxLat = 1-minLat;
		double maxLng = 1-minLng;
		for(int i = 1; i < points.size(); i++){
			Node p = points.get(i);
			if(p.getLat() > maxLat) maxLat = p.getLat();
			if(p.getLat() < minLat) minLat = p.getLat();
			if(p.getLng() > maxLng) maxLng = p.getLng();
			if(p.getLng() < minLng) minLng = p.getLng();
		}
		json = json + "\"minlat\":" + minLat
				+ ",\"maxlat\":" + maxLat
				+ ",\"minlng\":" + minLng
				+ ",\"maxlng\":" + maxLng
				+ "}";
		
		MapWindow mw = new MapWindow(minLat, minLng, maxLat, maxLng);
		Gson gson = new Gson();
		json = gson.toJson(mw);
		System.out.println("computeCoordinateWindows, " + minLat + "," + minLng + "  " + maxLat + "," + maxLng + ", json = " + json);
		return json;
	}

	public MapWindow computeCoordinateMapWindows(){
		double minLat = Integer.MAX_VALUE;
		double minLng = Integer.MAX_VALUE;
		double maxLat = 1-minLat;
		double maxLng = 1-minLng;
		for(int i = 1; i < points.size(); i++){
			Node p = points.get(i);
			if(p.getLat() > maxLat) maxLat = p.getLat();
			if(p.getLat() < minLat) minLat = p.getLat();
			if(p.getLng() > maxLng) maxLng = p.getLng();
			if(p.getLng() < minLng) minLng = p.getLng();
		}
		MapWindow mw = new MapWindow(minLat, minLng, maxLat, maxLng);
		System.out.println("computeCoordinateMapWindows, " + minLat + "," + minLng + "  " + maxLat + "," + maxLng);
		return mw;
	}

	public Node findNearestPoint(String latlng){
		return (Node) quadTree.findNearestPoint(latlng); //kdTree.findNearestPoint(latlng);

//		String[] s = latlng.split(",");
//		double lat = Double.valueOf(s[0].trim());
//		double lng = Double.valueOf(s[1].trim());
//		double minD = Integer.MAX_VALUE;
//		Point sel_p = null;
//		for(int i = 1; i < points.size(); i++){
//			double d = G.computeDistanceHaversine(lat, lng, points.get(i).getLat(), points.get(i).getLng());
//			if(d < minD){
//				minD = d;
//				sel_p = points.get(i);
//			}
//		}
//
//		Point kd_point = kdTree.findNearestPoint(lat, lng);
//		if (kd_point != sel_p) {
//			System.out.println("??????????????????????????????????????????????????????????????????????????????????????????? kd:: " + kd_point + "; sel:: " + sel_p);
//		}
//		return sel_p;
	}
	public void loadMap(String filename){
		System.out.println(name() + "::loadMap start.....");
		try{
			Scanner in = new Scanner(new File(filename));
			points = new ArrayList<Node>();
			points.add(new Node());// add artificial point, so that mapping from index 1, 2, 3....
			mID2Index = new HashMap<Integer,Integer>();
			double t0 = System.currentTimeMillis();
			// read points
			while(true){
				int id = in.nextInt();
				if(id == -1) break;
				double lat = in.nextDouble();
				double lng = in.nextDouble();
				Node p = new Node(id,lat,lng);
				mID2Index.put(id,points.size());
				points.add(p);
				//System.out.println(name() + "::loadMap, id = " + id + ", latlng = (" + lat + "," + lng + "), sz = " + points.size());
			}
			//System.exit(-1);
			int n = points.size()-1;
			HashSet<Arc>[] A = new HashSet[n+1];
			for(int i = 0; i <= n; i++)
				A[i] = new HashSet<Arc>();
			// read arcs
			while(true){
				int beginID = in.nextInt();
				if(beginID == -1) break;
				int endID = in.nextInt();
				double length = in.nextDouble();
				if(mID2Index.get(beginID) == null || mID2Index.get(endID) == null){
					System.out.println(name() + "::loadMap, BUG data??? --> exit");
					System.exit(-1);
					continue;
				}
				int beginIndex = mID2Index.get(beginID);
				int endIndex = mID2Index.get(endID);
				Arc a = new Arc(endIndex,length);
				//System.out.println(name() + "::loadMap (" + beginID + "," + endID + "), beginIndex = " + beginIndex 
				//		+ ", endIndex = " + endIndex + ", n = " + n);
				
				A[beginIndex].add(a);
//				Arc b = new Arc(beginIndex, length);
//				A[endIndex].add(b);
			}
			List<ILocation> ps = new ArrayList<>();
			for (int i = 1; i < points.size(); i++) {
				ps.add(points.get(i));
			}
			double t = System.currentTimeMillis() - t0;
			System.out.println(name() + "::loadMap finished, time = " + (t*0.001) + "s");
//			kdTree = new KDTree(ps);
			quadTree = new QuadTree(ps);

//			Random rand = new Random();
//			double latLen = quadTree.getLatUpper() - quadTree.getLatLower();
//			double lngLen = quadTree.getLngUpper() - quadTree.getLngLower();
//			double kdTime = 0;
//			double quadTime = 0;
//			for (int test = 0; test < 1000000; test++) {
//				double lat = rand.nextDouble() * latLen + quadTree.getLatLower();
//				double lng = rand.nextDouble() * lngLen + quadTree.getLngLower();
////				Point bestPoint = null;
////				double minDist = 1e18;
////				for (Point q : points) {
////					double dist = G.computeDistanceHaversine(lat, lng, q.getLat(), q.getLng());
////					if (dist < minDist) {
////						minDist = dist;
////						bestPoint = q;
////					}
////				}
//				System.out.println(test + ": " + lat + " " + lng + " " );
//				t0 = System.currentTimeMillis();
//				Point kd = kdTree.findNearestPoint(lat, lng);
//				kdTime += System.currentTimeMillis() - t0;
//				t0 = System.currentTimeMillis();
//				Point qu = quadTree.findNearestPoint(lat, lng);
//				quadTime += System.currentTimeMillis() - t0;
//				if (kd.getLat() != qu.getLat() || kd.getLng() != qu.getLng()) {
////				if (kd != qu) {
//					System.out.println("kd " + kd + " qu " + qu);
//					System.exit(-1);
//				}
//			}
//			System.out.println("kdTime = " + (kdTime / 1000) + " quadTime = " + (quadTime / 1000));
			g = new Graph(n,A);
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void calcDistanceElements(DistanceElementQuery input) {
		HashMap<String, HashMap<String, GeneralDistanceElement>> mFrom2To2Element = new HashMap<>();
		HashMap<String, Node> mLatLgng2Point = new HashMap<>();
		HashMap<String, String> mId2LatLng = new HashMap<>();
		for (GeneralDistanceElement e : input.getElements()) {
			if (!mFrom2To2Element.containsKey(e.getFromId())) {
				mFrom2To2Element.put(e.getFromId(), new HashMap<>());
			}
			mFrom2To2Element.get(e.getFromId()).put(e.getToId(), e);
			String fromLatLng = e.getFromLat() + "," + e.getFromLng();
			String toLatLng = e.getToLat() + "," + e.getToLng();
			mId2LatLng.put(e.getFromId(), fromLatLng);
			mId2LatLng.put(e.getToId(), toLatLng);
			if (!mLatLgng2Point.containsKey(fromLatLng)) {
				mLatLgng2Point.put(fromLatLng, findNearestPoint(fromLatLng));
			}
			if (!mLatLgng2Point.containsKey(toLatLng)) {
				mLatLgng2Point.put(toLatLng, findNearestPoint(toLatLng));
			}
		}
		PQShortestPath shortestPath = new PQShortestPath(g);
		int cnt = 0;
		for (Map.Entry<String, HashMap<String, GeneralDistanceElement>> firstEntry : mFrom2To2Element.entrySet()) {
			int startId = mID2Index.get(mLatLgng2Point.get(mId2LatLng.get(firstEntry.getKey())).getId());
			int[] endIds = new int[firstEntry.getValue().size()];
			int l = 0;
			for (Map.Entry<String, GeneralDistanceElement> secondEntry : firstEntry.getValue().entrySet()) {
				endIds[l++] = mID2Index.get(mLatLgng2Point.get(mId2LatLng.get(secondEntry.getKey())).getId());
			}
			cnt++;
			System.out.println(cnt + " " + mFrom2To2Element.size() + " " + startId);
//			for (int aa : endIds) {
//				System.out.println("  - " + aa);
//			}
			double[] distances = shortestPath.solve(startId, endIds);
			l = 0;
			for (Map.Entry<String, GeneralDistanceElement> secondEntry : firstEntry.getValue().entrySet()) {
				secondEntry.getValue().setDistance(distances[l++]);
//				endIds[l++] = mLatLgng2Point.get(mId2LatLng.get(secondEntry.getKey())).getId();
			}
		}
	}

	public Segment[] getSegmentOnWindow(double latLower, double lngLower, double latUpper, double lngUpper) {
		Collection<ILocation> pointsOnWindow = quadTree.getPointsOnWindow(latLower, lngLower, latUpper, lngUpper);
		int len = 0;
		for (ILocation p : pointsOnWindow) {
			Node node = (Node) p;
			len += g.getA()[node.getId()].size();
		}
		Segment[] segments = new Segment[len];
		int i = 0;
		for (ILocation p : pointsOnWindow) {
			Node node = (Node) p;
			for (Arc a : g.getA()[node.getId()]) {
				segments[i] = new Segment(i, node.getLatLng(), points.get(a.getEndPoint()).getLatLng());
				i++;
			}
		}
		return segments;
	}

	public DistanceElement[] getDistanceElements(LatLngInput input) {
		HashMap<String, Integer> mCode2Id = new HashMap<>();
		for (LatLng latLng : input.getLatLngList()) {
			Node point = findNearestPoint(latLng.getLat() +"," + latLng.getLng());
			System.out.println("latlng Code: " + latLng.getCode() + " id: " + point.getId());
			mCode2Id.put(latLng.getCode(), point.getId());
		}
		int[] t = new int[mCode2Id.size()];
		HashMap<String, Integer> mCode2Tid = new HashMap<>();
		for (int i = 0; i < input.getLatLngList().length; i++) {
			System.out.println(input.getLatLngList()[i].getCode());
			t[i] = mCode2Id.get(input.getLatLngList()[i].getCode());
			mCode2Tid.put(input.getLatLngList()[i].getCode(), i);
		}
		for (String code : mCode2Tid.keySet()) {
			System.out.println("code: " + code + " " + mCode2Tid.get(code));
		}
		double[][] dist = new double[t.length][];
		PQShortestPath shortestPath = new PQShortestPath(g);
		for (int i = 0; i < t.length; i++) {
			dist[i] = shortestPath.solve(t[i], t);
		}
		ArrayList<DistanceElement> distanceElements = new ArrayList<>();
		for (String from : mCode2Id.keySet()) {
			int i = mCode2Tid.get(from);
			for (String to : mCode2Id.keySet()) {
				int j = mCode2Tid.get(to);
				distanceElements.add(new DistanceElement(from, to, dist[i][j]));
			}
		}
		DistanceElement[] arr = new DistanceElement[distanceElements.size()];
		distanceElements.toArray(arr);
		return arr;
	}

	public Path findPath(String fromLatLng, String toLatLng){
		Node fromPoint = findNearestPoint(fromLatLng);
		Node toPoint = findNearestPoint(toLatLng);
		return findPath(fromPoint.getId(), toPoint.getId());
	}
	public Path findPath(int fromID, int toID){
		//System.out.println(name() + "::findPath(" + fromID + "," + toID + ")");

		PQShortestPath app = new PQShortestPath(g);//ShortestPath(g);
		int s = mID2Index.get(fromID);
		int t = mID2Index.get(toID);
		int[] p = app.solve(s, t);
		if(p == null) return null;
		Node[] path = new Node[p.length];
		for(int i = 0; i < path.length; i++){
			path[i] = points.get(p[i]);
		}
		//System.out.println(name() + "::findPath(" + fromID + "," + toID + ") finished, path.length = " + path.length);
		Path rs = new Path(path);
		rs.setLength(app.getShortestLength());
		return rs;
	}
	public Point getNearestPoint(double lat, double lng){
		return null;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
