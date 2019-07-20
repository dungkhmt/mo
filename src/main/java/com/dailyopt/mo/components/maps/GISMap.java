package com.dailyopt.mo.components.maps;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import com.dailyopt.mo.components.algorithms.ShortestPath;
import com.dailyopt.mo.components.maps.graphs.Arc;
import com.dailyopt.mo.components.maps.graphs.Graph;

import java.util.*;
public class GISMap {
	private ArrayList<Point> points;
	private HashMap<Integer, Integer> mID2Index;
	
	private Graph g;
	
	public GISMap(){
		//loadMap("data/SanfranciscoRoad-connected-contracted-5.txt");
		loadMap("data/SanfranciscoRoad-connected.txt");
	}
	public String name(){
		return "GISMap";
	}
	public void loadMap(String filename){
		System.out.println(name() + "::loadMap start.....");
		try{
			Scanner in = new Scanner(new File(filename));
			points = new ArrayList<Point>();
			points.add(new Point());// add artificial point, so that mapping from index 1, 2, 3....
			mID2Index = new HashMap<Integer,Integer>();
			double t0 = System.currentTimeMillis();
			// read points
			while(true){
				int id = in.nextInt();
				if(id == -1) break;
				double lat = in.nextDouble();
				double lng = in.nextDouble();
				Point p = new Point(id,lat,lng);
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
				
			}
			double t = System.currentTimeMillis() - t0;
			System.out.println(name() + "::loadMap finished, time = " + (t*0.001) + "s");
			
			g = new Graph(n,A);
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public Path findPath(int fromID, int toID){
		System.out.println(name() + "::findPath(" + fromID + "," + toID + ")");
		
		ShortestPath app = new ShortestPath(g);
		int s = mID2Index.get(fromID);
		int t = mID2Index.get(toID);
		int[] p = app.solve(s, t);
		if(p == null) return null;
		Point[] path = new Point[p.length];
		for(int i = 0; i < path.length; i++){
			path[i] = points.get(p[i]);
		}
		System.out.println(name() + "::findPath(" + fromID + "," + toID + ") finished, path.length = " + path.length);
		return new Path(path);
	}
	public Point getNearestPoint(double lat, double lng){
		return null;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
