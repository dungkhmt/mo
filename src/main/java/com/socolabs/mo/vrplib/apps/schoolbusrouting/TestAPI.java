package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRoutingInput;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRoutingSolution;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.service.BusRouteSolver;
import org.springframework.web.bind.annotation.*;
import utils.DateTimeUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

@CrossOrigin
@RestController
public class TestAPI {

	public static final String SECONDARY_ROOT_DIR = "E:/Project/cblsvr/VinschoolProject/data/data-extract/vinschool/";

	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public void testBining(HttpServletRequest request,
			@RequestBody SchoolBusRoutingInput input) {
		BusRouteSolver brSolver = new BusRouteSolver();
		brSolver.test(input);
	}
	
	@RequestMapping(value = "/school-bus-random-routing", method = RequestMethod.POST)
	public SchoolBusRoutingSolution computeSchoolBusRandomSolution(HttpServletRequest request, String fileName,
			@RequestBody SchoolBusRoutingInput input) {
		SchoolBusRoutingSolution solution = null;
//		if(getAccessPermission() != null){
//			BusRouteSolver brSolver = new BusRouteSolver();
//			brSolver.initLogFile(fileName);
//			solution = brSolver.randomSolve(input);
//			brSolver.closeLog();
//		}
		BusRouteSolver brSolver = new BusRouteSolver();
		brSolver.initLogFile(fileName);
		solution = brSolver.randomSolve(input);
		brSolver.closeLog();
//		ObjectMapper mapper = new ObjectMapper();
//	    try{
//	        // Writing to a file   
//	    	String out = SECONDARY_ROOT_DIR + "output/random-solution.json";
//	        mapper.writeValue(new File(out), solution);
//
//	    } catch (IOException e) {  
//	        e.printStackTrace();  
//	    } 
		return solution;
	}
	
	@RequestMapping(value = "/school-bus-random-routing-post", method = RequestMethod.POST)
	public SchoolBusRoutingSolution computeSchoolBusRandomSolutionPost(HttpServletRequest request,
			@RequestBody SchoolBusRoutingInput input) {
		
		BusRouteSolver brSolver = new BusRouteSolver();
		String fileName = "E:/Project/cblsvr/VinschoolProject/";
		brSolver.initLogFile(fileName);
		SchoolBusRoutingSolution solution = brSolver.randomSolve(input);
		brSolver.closeLog();
		
		ObjectMapper mapper = new ObjectMapper();
	    try{
	        // Writing to a file   
	    	String out = SECONDARY_ROOT_DIR + "output/random-solution.json";
	        mapper.writeValue(new File(out), solution);

	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } 
		return solution;
	}
	
	@RequestMapping(value = "/school-bus-nearestneighborhood-routing", method = RequestMethod.POST)
	public SchoolBusRoutingSolution computeSchoolBusNearestNeighborhoodSolution(HttpServletRequest request,
			@RequestBody SchoolBusRoutingInput input) {
		
		BusRouteSolver brSolver = new BusRouteSolver();
		SchoolBusRoutingSolution solution = brSolver.nearestNeighborhoodSolver(input);
//		ObjectMapper mapper = new ObjectMapper();
//	    try {  
//
//	        // Writing to a file
//	    	String out = SECONDARY_ROOT_DIR + "output/nearestneighborhood-solution.json";
//	        mapper.writeValue(new File(out), solution);
//	        brSolver.printSolution(SECONDARY_ROOT_DIR, "Greenbay_TieuHoc");
//
//	    } catch (IOException e) {  
//	        e.printStackTrace();  
//	    } 
		return solution;
	}
	
	@RequestMapping(value = "/school-bus-update-solution-by-move-action", method = RequestMethod.POST)
	public SchoolBusRoutingSolution updateSchoolBusSolutionByMoveAction(HttpServletRequest request, String fileName,
			@RequestBody SchoolBusRoutingInput input) {
		
		BusRouteSolver brSolver = new BusRouteSolver();
		brSolver.initLogFile(fileName);
		SchoolBusRoutingSolution solution = brSolver.updateSolutionByMoveAction(input);
		brSolver.closeLog();
		return solution;
	}
	
	@RequestMapping(value = "/school-bus-update-solution-by-move-action-post", method = RequestMethod.POST)
	public SchoolBusRoutingSolution updateSchoolBusSolutionByMoveActionPost(HttpServletRequest request,
			@RequestBody SchoolBusRoutingInput input) {
		
		BusRouteSolver brSolver = new BusRouteSolver();
		String fileName = "E:/Project/cblsvr/VinschoolProject/";
		brSolver.initLogFile(fileName);
		SchoolBusRoutingSolution solution = brSolver.updateSolutionByMoveAction(input);
		brSolver.closeLog();
		return solution;
	}
	
	
	@RequestMapping(value = "/school-bus-update-current-solution", method = RequestMethod.POST)
	public SchoolBusRoutingSolution updateCurrentSolution(HttpServletRequest request, String fileName,
			@RequestBody SchoolBusRoutingInput input) {
		
		BusRouteSolver brSolver = new BusRouteSolver();
		brSolver.initLogFile(fileName);
		SchoolBusRoutingSolution solution = brSolver.updateCurrentSolution(input);
		brSolver.closeLog();
		
//		ObjectMapper mapper = new ObjectMapper();
//	    try{
//	        // Writing to a file   
//	    	String out = SECONDARY_ROOT_DIR + "output/random-solution.json";
//	        mapper.writeValue(new File(out), solution);
//
//	    } catch (IOException e) {  
//	        e.printStackTrace();  
//	    } 
		return solution;
	}
	
	@RequestMapping(value = "/school-bus-update-current-solution-post", method = RequestMethod.POST)
	public SchoolBusRoutingSolution updateCurrentSolutionPost(HttpServletRequest request,
			@RequestBody SchoolBusRoutingInput input) {
		
		BusRouteSolver brSolver = new BusRouteSolver();
		String fileName = "E:/Project/cblsvr/VinschoolProject/";
		brSolver.initLogFile(fileName);
		SchoolBusRoutingSolution solution = brSolver.updateCurrentSolution(input);
		brSolver.closeLog();
		
//		ObjectMapper mapper = new ObjectMapper();
//	    try{
//	        // Writing to a file   
//	    	String out = SECONDARY_ROOT_DIR + "output/random-solution.json";
//	        mapper.writeValue(new File(out), solution);
//
//	    } catch (IOException e) {  
//	        e.printStackTrace();  
//	    } 
		return solution;
	}
	
	
	@RequestMapping(value = "/school-bus-update-random-solution-post", method = RequestMethod.POST)
	public SchoolBusRoutingSolution updateRandomSolutionPost(HttpServletRequest request,
			@RequestBody SchoolBusRoutingInput input) {
		
		BusRouteSolver solverTemp = new BusRouteSolver();
		String fileName = "E:/Project/cblsvr/VinschoolProject/";
		solverTemp.initLogFile(fileName);
		SchoolBusRoutingSolution solutionTemp = solverTemp.updateCurrentSolution(input);
		solverTemp.closeLog();
		input.setCurrentSolution(solutionTemp);
		BusRouteSolver brSolver = new BusRouteSolver();
		SchoolBusRoutingSolution solution = brSolver.randomSolve(input);
		brSolver.closeLog();
		
//		ObjectMapper mapper = new ObjectMapper();
//	    try{
//	        // Writing to a file   
//	    	String out = SECONDARY_ROOT_DIR + "output/random-solution.json";
//	        mapper.writeValue(new File(out), solution);
//
//	    } catch (IOException e) {  
//	        e.printStackTrace();  
//	    } 
		return solution;
	}
//	@RequestMapping(value = "/school-bus-createInput", method = RequestMethod.POST)
//	public SchoolBusRoutingInput computeSchoolBusSolution(HttpServletRequest request,
//			@RequestBody TestInput input) {
//		
////		BusRouteSolver brSolver = new BusRouteSolver();
////		return brSolver.solve(input);
//		
//		String dir = "E:/Project/cblsvr/VinSchool/VinSchoolAPI/dailyoptapi/data/vinschool/";
//
//		int timeLimit = 36000000;
//		int nIter = 30000;
//		int lv = Constants.THCS;
//		String type = "Táº¡i Ä‘iá»ƒm";
//		String name = "Trung há»�c Harmony";
//
//		DataProcessing info = new DataProcessing(30, 30, 30);
//
//		return info.readDataFile(dir, type, name, lv);
////		BusScheduling bs = new BusScheduling(info);
////		bs.stateModel();
////
////		double currTime = System.currentTimeMillis();
////		bs.greedyInitSolution();
////		// bs.printSolution("output/vinschool/", "TÃ¡ÂºÂ¡i Ã„â€˜iÃ¡Â»Æ’m",
////		// "Trung hÃ¡Â»ï¿½c Harmony", lv);
////	
////		SeachInputVinPro si = new SeachInputVinPro(bs.pickupPoints,
////				bs.rejectPoints, bs.capList, bs.earliestAllowedArrivalTime,
////				bs.serviceDuration, bs.lastestAllowedArrivalTime);
////		SolutionVinPro best_solution = bs.search(nIter, timeLimit, si);
////
////		best_solution.copy2XR(bs.XR);
////
////		bs.rejectPoints = best_solution.get_rejectPoints();
////		bs.printSolution("output/vinschool/", type, name, lv);
////		
////		return bs.printSolutionJson("output/vinschool/", type, name, lv, info);
//	}
	
//	public String getAccessPermission(){
//		try{
//			String url = "http://103.56.158.242:8080/DailyOptAPI/accessPermission";
//			URL obj = new URL(url);
//			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//
//			//add request header
//			con.setRequestMethod("POST");
//			con.setRequestProperty("accept", "application/json");
//			con.setRequestProperty("Content-Type", "application/json");
//
//			// Send post request
//			con.setDoOutput(true);
//			
//			String input = "{ \"a\": \"DailyOptAPI\", \"b\": \"sml\"}";
//	        OutputStream os = con.getOutputStream();
//	        os.write(input.getBytes());
//	        os.flush();
//	        
//			con.connect();
//			int responseCode = con.getResponseCode();
//			if(responseCode != 200)
//				return null;
//			
//			BufferedReader in = new BufferedReader(
//			        new InputStreamReader(con.getInputStream()));
//			String inputLine;
//			StringBuffer response = new StringBuffer();
//
//			while ((inputLine = in.readLine()) != null) {
//				response.append(inputLine);
//			}
//			in.close();
//			//print result
//			return response.toString();
//		}catch(Exception e){
//			System.out.println("exception " + e);
//		}
//		return null;
//	}

	public static void main(String[] args) throws FileNotFoundException {
//		String[] files = new String[]{
//				"D:\\Workspace\\VinSchool\\20200731\\VSC\\Gardenia_nha.json",
//				"D:\\Workspace\\VinSchool\\20200731\\VSC\\Greenbay_nha.json"
//		};
//		for (int i = 0; i < files.length; i++) {
//			String inputFile = files[i];
//			System.out.println(inputFile);
//			Gson g1 = new Gson();
//			BufferedReader in1 = new BufferedReader(new FileReader(inputFile));
//			SchoolBusRoutingInput input1 = g1.fromJson(in1, SchoolBusRoutingInput.class);
//			SBUtils.recreateTravelTimeMatrix(input1, inputFile.split(".json")[0] + "_new.json");
//		}
//		System.exit(0);

		double currentTimemill = System.currentTimeMillis()/1000;
		String currentDate = DateTimeUtils.unixTimeStamp2DateTime((long)currentTimemill);
		String date = "2020-09-01 23:59:00";
		double t = DateTimeUtils.dateTime2Int(date);
		double t0 = DateTimeUtils.dateTime2Int(currentDate);
		if(t0 > t){
			System.out.println("Outdate!");
			return;
		}
		TestAPI T =new TestAPI();

		String jsonInFileName = args[0] + "-input.json";
		String jsonOutFileName = args[0] + "-output.json";
		SchoolBusRoutingInput input = null;
		try{
			Gson g = new Gson();
			BufferedReader in = new BufferedReader(new FileReader(jsonInFileName));
			input = g.fromJson(in, SchoolBusRoutingInput.class);
			SchoolBusRoutingSolution solution;

			solution = T.computeSchoolBusRandomSolution(null, args[0], input);

//			if(input.getMoveActions() == null
//				|| input.getMoveActions().length == 0){
//				if(input.getRequestSolutionType().equals("new"))
//				else if(input.getRequestSolutionType().equals("update"))
//					solution = T.updateCurrentSolution(null, args[0], input);
//				else{
//					solution = T.updateCurrentSolution(null, args[0], input);
//				}
//			}
//			else
//				solution = T.updateSchoolBusSolutionByMoveAction(null, args[0], input);
			String out = g.toJson(solution);
			BufferedWriter writer = new BufferedWriter(new FileWriter(jsonOutFileName));
		    writer.write(out);
		     
		    writer.close();
		}catch(Exception e){
			
		}
		
//		if(T.getAccessPermission() != null){
//			try{
//				Gson g = new Gson();
//				BufferedReader in = new BufferedReader(new FileReader(jsonInFileName));
//				input = g.fromJson(in, SchoolBusRoutingInput.class);
//				SchoolBusRoutingSolution solution;
//				if(input.getMoveActions() == null
//					|| input.getMoveActions().length == 0){
//					if(input.getRequestSolutionType().equals("new"))
//						solution = T.computeSchoolBusRandomSolution(null, args[0], input);
//					else
//						solution = T.updateCurrentSolution(null, args[0], input);
//				}
//				else
//					solution = T.updateSchoolBusSolutionByMoveAction(null, args[0], input);
//				String out = g.toJson(solution);
//				BufferedWriter writer = new BufferedWriter(new FileWriter(jsonOutFileName));
//			    writer.write(out);
//			     
//			    writer.close();
//			}catch(Exception e){
//				
//			}
//		}
//		else{
//			Gson g = new Gson();
//			try{
//				BufferedReader in = new BufferedReader(new FileReader(jsonInFileName));
//				input = g.fromJson(in, SchoolBusRoutingInput.class);
//				String out = g.toJson(input);
//				BufferedWriter writer = new BufferedWriter(new FileWriter(jsonOutFileName));
//			    writer.write(out);
//			     
//			    writer.close();
//			}catch(Exception e){
//				
//			}
//		}
			
	}

}
