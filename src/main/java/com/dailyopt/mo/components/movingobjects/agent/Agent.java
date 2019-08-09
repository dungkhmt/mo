package com.dailyopt.mo.components.movingobjects.agent;

import java.io.IOException;
import java.util.Random;

import com.dailyopt.mo.components.api.performservicetruck.PerformServiceTruckInput;
import com.dailyopt.mo.components.api.updateplannedroutetruck.UpdatePlannedRouteTruckInput;
import com.dailyopt.mo.components.api.updateservicepoints.UpdateServicePointsInput;
import com.dailyopt.mo.components.maps.Path;
import com.dailyopt.mo.components.maps.Point;
import com.dailyopt.mo.components.movingobjects.truck.PositionTypeAction;
import com.dailyopt.mo.controller.ApiController;
import com.dailyopt.mo.model.routevrp.Route;
import com.dailyopt.mo.model.routevrp.RouteVRPInputPoint;
import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Agent extends Thread {
	OkHttpClient client = new OkHttpClient();

	public static final String urlAdd = "http://localhost:8080/add";
	public static final String urlCreateTruck = "http://localhost:8080/createTruck";
	public static final String urlUpdateLocation = "http://localhost:8080/updateLocation";
	public static final String urlShortestPath = "http://localhost:8080/findShortestPath";
	// public static final String urlUpdateServicePoints =
	// "http://localhost:8080/updateServicePoints";
	public static final String urlUpdatePlannededRouteTruck = "http://localhost:8080/updatePlannededRouteTruck";
	public static final String urlPerformServiceTruck = "http://localhost:8080/performServiceTruck";

	public static final double EPS = 0.1;

	private Random rand = new Random();
	private Thread t = null;
	private String name;
	private String moveFromPoint = "";
	private String moveToPoint = "";
	private Route route;
	private int nextServicePointIndex;

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public Agent(String name) {
		System.out.println("Agent " + name + " created...");
		this.name = name;
	}

	public static final MediaType JSON = MediaType
			.get("application/json; charset=utf-8");

	String execPost(String url, String json) throws IOException {
		RequestBody body = RequestBody.create(JSON, json);
		Request request = new Request.Builder().url(url).post(body).build();
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}

	public void start() {
		System.out.println(name + ":: start running...");
		if (t == null) {
			t = new Thread(this, name);
			t.start();
		}
	}

	public void createObject() {
		String json = "{\"id\":\"" + this.name + "\",\"lat\":" + 0
				+ ",\"lng\":" + 0 + "}";
		try {
			String rs = execPost(urlCreateTruck, json);
			System.out.println(name + "::createObject , json = " + json
					+ ", rs = " + rs);

			Thread.sleep(1000);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void updatePlannedRoute() {
		// String json = "{\"id\":\"" + this.name + "\",\"lat\":" + 0 +
		// ",\"lng\":" + 0 + "}";
		UpdatePlannedRouteTruckInput input = new UpdatePlannedRouteTruckInput(
				this.name, route);
		String json = ApiController.gson.toJson(input);
		try {
			String rs = execPost(urlUpdatePlannededRouteTruck, json);
			System.out.println(name + "::updatePlannedRoute , json = " + json
					+ ", rs = " + rs);

			Thread.sleep(1000);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean samePosition(double lat1, double lng1, double lat2,
			double lng2) {
		double d = ApiController.GMQ.computeDistanceHaversine(lat1, lng1, lat2,
				lng2);
		return d < EPS;
	}

	public String name() {
		return "Agent[" + this.name + "]";
	}

	public void runSpecifiedRouteFromServer() {
		createObject();
		updatePlannedRoute();
		nextServicePointIndex = 1;
		RouteVRPInputPoint next_p = route.getPoints()[nextServicePointIndex];
		Point p = route.getPaths()[0].getPoints()[0];
		double lat = p.getLat();
		double lng = p.getLng();
		String type = "ACTION_PASS";
		try {
			while (true) {
				String json = "{\"id\":\"" + this.name + "\",\"lat\":" + lat
						+ ",\"lng\":" + lng + "}";

				String rs = execPost(urlUpdateLocation, json);
				//System.out.println(name()
				//		+ "::runSpecifiedRouteFromServer, rs = " + rs);
				
				PositionTypeAction pta = ApiController.gson.fromJson(rs,
						PositionTypeAction.class);
				if (pta.getAction().equals("ACTION_DELIVERY")) {
					String json1 = "{\"truckId\":\"" + this.name + "\"}";
					String rs1 = execPost(urlPerformServiceTruck, json1);
					System.out.println(name()
							+ "::runSpecifiedRouteFromServer, PERFORM SERVICE");
				}
				Thread.sleep(300);
				lat = pta.getLat();
				lng = pta.getLng();
				type = pta.getType();
				if(pta.getAction().equals("ACTION_STOP")){
					System.out.println(name()
							+ "::runSpecifiedRouteFromServer, STOP");
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void runSpecifiedRoute() {
		createObject();
		updatePlannedRoute();
		nextServicePointIndex = 1;
		RouteVRPInputPoint next_p = route.getPoints()[nextServicePointIndex];
		try {
			for (int i = 0; i < route.getPaths().length; i++) {

				Path p = route.getPaths()[i];
				for (int j = 0; j < p.getPoints().length; j++) {
					Point pj = p.getPoints()[j];

					String json1 = "{\"id\":\"" + this.name + "\",\"lat\":"
							+ pj.getLat() + ",\"lng\":" + pj.getLng() + "}";

					String rs1 = execPost(urlUpdateLocation, json1);
					double d = ApiController.GMQ.computeDistanceHaversine(
							pj.getLat(), pj.getLng(), next_p.getLat(),
							next_p.getLng());
					System.out.println(name + "::updateLocation step (" + i
							+ "," + j + "), distance to next service point = "
							+ d + ", json1 = " + json1 + ", rs1 = " + rs1);
					/*
					 * if(samePosition(pj.getLat(), pj.getLng(),
					 * next_p.getLat(),next_p.getLng())){ String objectId =
					 * this.name; nextServicePointIndex++; int cnt =
					 * route.getPoints().length - nextServicePointIndex;
					 * RouteVRPInputPoint[] points = new
					 * RouteVRPInputPoint[cnt]; for(int k = 0; k < cnt; k++)
					 * points[k] = route.getPoints()[k+nextServicePointIndex];
					 * UpdateServicePointsInput input = new
					 * UpdateServicePointsInput(objectId, points); String json2
					 * = ApiController.gson.toJson(input); String rs2 =
					 * execPost(urlUpdateServicePoints, json2);
					 * System.out.println(name +
					 * "::updateLocation, CALL updateServicePoints, nextServicePointIndex = "
					 * + nextServicePointIndex + ", json2 = " + json2 +
					 * ", rs2 = " + rs2); }
					 */

					Thread.sleep(1000);
				}
				PerformServiceTruckInput inp = new PerformServiceTruckInput(
						this.name);
				String json2 = ApiController.gson.toJson(inp);
				String rs2 = execPost(urlPerformServiceTruck, json2);
				System.out.println(name + "::updateLocation, PERFORM SERVICE");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void run() {
		runSpecifiedRouteFromServer();
		// runSpecifiedRoute();
		// runRouteFromPointToPoint();
	}

	public void runRouteFromPointToPoint() {
		createObject();

		try {
			String json = "{\"fromID\":0,\"toID\":0,\"fromPoint\":\""
					+ moveFromPoint + "\",\"toPoint\":\"" + moveToPoint + "\"}";

			String rs = execPost(urlShortestPath, json);
			Gson gson = new Gson();
			Path p = gson.fromJson(rs, Path.class);
			for (int i = 0; i < p.getPoints().length; i++) {
				String json1 = "{\"id\":\"" + this.name + "\",\"lat\":"
						+ p.getPoints()[i].getLat() + ",\"lng\":"
						+ p.getPoints()[i].getLng() + "}";

				String rs1 = execPost(urlUpdateLocation, json1);
				System.out.println(name + "::updateLocation step " + i
						+ ", json1 = " + json1 + ", rs1 = " + rs1);
				Thread.sleep(1000);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		/*
		 * for(int i = 0; i < 1000; i++){ int a = rand.nextInt(100); int b =
		 * rand.nextInt(100); String json = "{\"id\":\"" + this.name +
		 * "\",\"lat\":" + a + ",\"lng\":" + b + "}"; try{ String rs =
		 * execPost(urlUpdateLocation, json); System.out.println(name +
		 * "::updateLocation step " + i + ", json = " + json + ", rs = " + rs);
		 * Thread.sleep(1000);
		 * 
		 * }catch(Exception ex){ ex.printStackTrace(); } }
		 */
	}

	public String getMoveFromPoint() {
		return moveFromPoint;
	}

	public void setMoveFromPoint(String moveFromPoint) {
		this.moveFromPoint = moveFromPoint;
	}

	public String getMoveToPoint() {
		return moveToPoint;
	}

	public void setMoveToPoint(String moveToPoint) {
		this.moveToPoint = moveToPoint;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
