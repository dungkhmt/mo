package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting;//package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Random;
//
//import org.apache.poi.EncryptedDocumentException;
//import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.ConfigParam;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.DistanceElement;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRequest;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRoutingInput;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.SchoolBusRoutingSolution;
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.Vehicle;
//import localsearch.domainspecific.vehiclerouting.vrp.Constants;
//import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
//import localsearch.domainspecific.vehiclerouting.vrp.entities.Request;
//import localsearch.domainspecific.vehiclerouting.vrp.utils.ScannerInput;
//import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
//import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.LatLng;
//
//public class DataProcessing {
//	
//	public ArrayList<Point> points;
//	public int nVehicle;
//	public int n16;
//	public int n29;
//	public int n45;
//	public double[] capList;
//	public static int nRequest;
//	public HashMap<Point, ArrayList<String>> nbCus;
//	public HashMap<Point, String> type;
//	public ArrayList<Point> pickupPoints;
//	public ArrayList<Point> publicPoints;
//	//public ArrayList<Point> deliveryPoints;
//	public ArrayList<Point> targetPoints;
//	public ArrayList<Point> startPoints;
//	public ArrayList<Point> stopPoints;
//	public ArrayList<Point> pickupPs;
//	public HashMap<Point, Integer> earliestAllowedArrivalTime;
//	public HashMap<Point, Integer> serviceDuration;
//	public HashMap<Point, Integer> lastestAllowedArrivalTime;
//	//public HashMap<String, HashMap<String, Integer>> bigMap;
//	public int[][] map;
//	public int[][] map_D;
//	public int[][] costT;
//	HashMap<Integer, LatLng> p2LatLng;
//	HashMap<String, Integer> name2p;
//	HashMap<String, Integer> name2area;
//	public String[] p2name;
//	
//	public ArrayList<String> rejPickup;
//	//public ArrayList<String> rejDel;
//	public ArrayList<String> rejTarget;
//	public ArrayList<Request> reqList;
//	public HashMap<Integer, ArrayList<String>> aceIDlist;
//	
//	public SchoolBusRoutingInput input;
//	int INF_TIME = 100000000;
//	int NP = 5000;
//	
//    public DataProcessing(int n16, int n29, int n45)
//    {	
//    	this.n16 = n16;
//    	this.n29 = n29;
//    	this.n45 = n45;
//    	this.pickupPoints = new ArrayList<Point>();
//    	this.publicPoints = new ArrayList<Point>();
//    	//this.deliveryPoints = new ArrayList<Point>();
//    	this.targetPoints = new ArrayList<Point>();
//    	this.rejPickup = new ArrayList<String>();
//    	//this.rejDel = new ArrayList<String>();
//    	this.rejTarget = new ArrayList<String>();
//    	this.reqList = new ArrayList<Request>();
//    	this.levelTarget = new  ArrayList<Integer>();
//    	this.map = new int[NP][NP];
//    	this.map_D = new int[NP][NP];
//		this.p2LatLng = new HashMap<Integer, LatLng>();
//		this.name2p = new HashMap<String, Integer>();
//		this.name2area = new HashMap<String, Integer>();
//		this.p2name = new String[NP];
//		this.aceIDlist = new HashMap<Integer, ArrayList<String>>();
//		for(int i = 0; i < NP; i++){
//			p2name[i] = "";
//			for(int j = 0; j < NP; j++){
//				map[i][j] = -1;
//				map_D[i][j] = -1;
//			}
//		}
//    	
//    }
//    
//    public void readMapDataTravelTime(String dir){
//    	String mapfile = dir + "map-T.xlsx";
//    	try{
//	    	File file = new File(mapfile);
//	    	FileInputStream fin = new FileInputStream(file);
//	    	XSSFWorkbook mbook = new XSSFWorkbook (fin); 
//	    	XSSFSheet mapSheet = mbook.getSheet("map");
//	    	if(mapSheet == null){
//	    		mbook.close();
//	    		return;
//	    	}
//			int rMap = mapSheet.getLastRowNum();
//	
//			for(int i = 1; i <= rMap; i++){
//				Row row = mapSheet.getRow(i);
//				int fp = -1;
//				Cell cell = row.getCell(0);
//				if(cell != null)
//					fp = (int)(cell.getNumericCellValue());
//				int tp = -1;
//				cell = row.getCell(1);
//				if(cell != null)
//					tp = (int)(cell.getNumericCellValue());
//				int time = -1;
//				cell = row.getCell(2);
//				if(cell != null)
//					time = (int)(cell.getNumericCellValue());
//				map[fp][tp] = time;
//			}
//			
//			mbook.close();
//    	} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
//    
//    public void readMapDataTravelDistance(String dir){
//    	String mapfile = dir + "map-D.xlsx";
//    	try{
//	    	File file = new File(mapfile);
//	    	FileInputStream fin = new FileInputStream(file);
//	    	XSSFWorkbook mbook = new XSSFWorkbook (fin); 
//	    	XSSFSheet mapSheet = mbook.getSheet("map");
//	    	if(mapSheet == null){
//	    		mbook.close();
//	    		return;
//	    	}
//			int rMap = mapSheet.getLastRowNum();
//	
//			for(int i = 1; i <= rMap; i++){
//				Row row = mapSheet.getRow(i);
//				int fp = -1;
//				Cell cell = row.getCell(0);
//				if(cell != null)
//					fp = (int)(cell.getNumericCellValue());
//				int tp = -1;
//				cell = row.getCell(1);
//				if(cell != null)
//					tp = (int)(cell.getNumericCellValue());
//				int time = -1;
//				cell = row.getCell(2);
//				if(cell != null)
//					time = (int)(cell.getNumericCellValue());
//				map_D[fp][tp] = time;
//			}
//			
//			mbook.close();
//    	} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
//    
//    public void readCoordinateData(String dir){
//    	String mapfile = dir + "coordinate.xlsx";
//    	try{
//	    	File file = new File(mapfile);
//	    	FileInputStream fin = new FileInputStream(file);
//	    	XSSFWorkbook mbook = new XSSFWorkbook (fin); 
//			
//			XSSFSheet pSheet = mbook.getSheet("coordinate");
//			if(pSheet == null){
//				mbook.close();
//				return;
//			}
//			int rPoints = pSheet.getLastRowNum();
//			for(int i = 1; i <= rPoints; i++){
//				Row row = pSheet.getRow(i);
//				int pName = -1;
//				Cell cell = row.getCell(0);
//				if(cell != null)
//					pName = (int)(cell.getNumericCellValue());
//				double lat = -1;
//				cell = row.getCell(1);
//				if(cell != null)
//					lat = cell.getNumericCellValue();
//				double lng = -1;
//				cell = row.getCell(2);
//				if(cell != null)
//					lng = cell.getNumericCellValue();
//				p2LatLng.put(pName, new LatLng(lat, lng));
//			}
//			mbook.close();
//    	} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
//    
//    public void readPointsData(String dir){
//    	String mapfile = dir + "points.xlsx";
//    	try{
//	    	File file = new File(mapfile);
//	    	FileInputStream fin = new FileInputStream(file);
//	    	XSSFWorkbook mbook = new XSSFWorkbook (fin); 
//			
//			XSSFSheet nSheet = mbook.getSheet("name");
//			if(nSheet == null){
//				mbook.close();
//				return;
//			}
//			int rName = nSheet.getLastRowNum();
//			for(int i = 1; i <= rName; i++){
//				Row row = nSheet.getRow(i);
//				int pID = -1;
//				Cell cell = row.getCell(0);
//				if(cell != null)
//					pID = (int)(cell.getNumericCellValue());
//				String name = "";
//				cell = row.getCell(1);
//				if(cell != null)
//					name = cell.getStringCellValue();
//				p2name[pID] = name;
//				name2p.put(name, pID);
//			}
//			mbook.close();
//    	} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
//    
//    public void updatePointsData(String dir){
//    	XSSFWorkbook workbook = new XSSFWorkbook();
//        
//		XSSFSheet pointSheet = workbook.createSheet("name");
//		Row row = pointSheet.createRow(0);
//        Cell cell = row.createCell(0);
//        cell.setCellValue("ID");
//        cell = row.createCell(1);
//        cell.setCellValue("TÃªn Ä‘iá»ƒm");
//		
//		int k = 1;
//		for(int i = 0; i < p2name.length; i++){
//			if(!p2name[i].equals("")){
//				row = pointSheet.createRow(k);
//				cell = row.createCell(0);
//				cell.setCellValue(i);
//				cell = row.createCell(1);
//				cell.setCellValue(p2name[i]);
//				k++;
//			}
//		}
//		
//		try {
//			String filename = dir + "points.xlsx";
//			File file = new File(filename);
//            FileOutputStream outputStream = new FileOutputStream(file);
//            workbook.write(outputStream);
//            workbook.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    
//    public void updateCoordinateData(String dir){
//    	XSSFWorkbook workbook = new XSSFWorkbook();
//		
//		XSSFSheet corSheet = workbook.createSheet("coordinate");
//		Row row = corSheet.createRow(0);
//        Cell cell = row.createCell(0);
//        cell.setCellValue("Ä�iá»ƒm");
//        cell = row.createCell(1);
//        cell.setCellValue("Kinh Ä‘á»™");
//        cell = row.createCell(2);
//        cell.setCellValue("VÄ© Ä‘á»™");	
//		
//		int k = 1;
//		for(int key : p2LatLng.keySet()){
//			LatLng arg = p2LatLng.get(key);
//			if(arg != null){
//				row = corSheet.createRow(k);
//				cell = row.createCell(0);
//				cell.setCellValue(key);
//				cell = row.createCell(1);
//				cell.setCellValue(arg.lat);
//				cell = row.createCell(2);
//				cell.setCellValue(arg.lng);
//				k++;
//			}
//		}
//		
//		XSSFSheet rejectSheet = workbook.createSheet("rejectedPoints");
//		row = rejectSheet.createRow(0);
//        cell = row.createCell(0);
//        cell.setCellValue("TÃªn Ä‘iá»ƒm");
//		
//		k = 1;
//		for(int i = 0; i < rejPickup.size(); i++){
//			row = rejectSheet.createRow(k);
//			cell = row.createCell(0);
//			cell.setCellValue(rejPickup.get(i));
//			k++;
//		}
//		
//		try {
//			String filename = dir + "coordinate.xlsx";
//			File file = new File(filename);
//            FileOutputStream outputStream = new FileOutputStream(file);
//            workbook.write(outputStream);
//            workbook.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    
//    public void updateMapDataTravelTime(String dir){
//    	XSSFWorkbook workbook = new XSSFWorkbook();
//        XSSFSheet mapSheet = workbook.createSheet("map");
//
//        Row row = mapSheet.createRow(0);
//        Cell cell = row.createCell(0);
//        cell.setCellValue("Ä�iá»ƒm Ä‘i");
//        cell = row.createCell(1);
//        cell.setCellValue("Ä�iá»ƒm Ä‘áº¿n");
//        cell = row.createCell(2);
//        cell.setCellValue("Thá»�i gian");	
//		int k = 1;
//		for(int i = 0; i < map.length; i++){
//			for(int j = 0; j < map.length; j++){
//				if(map[i][j] != -1){
//					row = mapSheet.createRow(k);
//					cell = row.createCell(0);
//					cell.setCellValue(i);
//					cell = row.createCell(1);
//					cell.setCellValue(j);
//					cell = row.createCell(2);
//					cell.setCellValue(map[i][j]);
//					k++;
//				}
//			}
//		}
//		
//		try {
//			String filename = dir + "map-T.xlsx";
//			File file = new File(filename);
//            FileOutputStream outputStream = new FileOutputStream(file);
//            workbook.write(outputStream);
//            workbook.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    
//    public void readSampleData(String dir){
//    	GoogleMapsQuery G = new GoogleMapsQuery();
//    	String in = dir + "SampleData.xlsx";
//		
//		try{
//			File f = new File(in);
//	    	FileInputStream fin = new FileInputStream(f);
//	    	XSSFWorkbook myWorkBook = new XSSFWorkbook (fin); 
//	    	XSSFSheet pointSheet = myWorkBook.getSheet("DSDD");
//	    	
//	    	int rows = pointSheet.getLastRowNum();
//	    	for(int i = 2; i <= rows; i++){
//	    		Row row = pointSheet.getRow(i);
//	    		Cell pick = row.getCell(1);
//	        	String pname = "";
//	        	if(pick != null)
//	        		pname = pick.getStringCellValue();
//	        	pname = pname.trim();
//	        	LatLng llp = p2LatLng.get(name2p.get(pname));
//	        	if(llp == null){
//	            	String temp = pname;
//	            	if(!temp.contains("Ha Noi"))
//	            		temp += " Ha Noi";
//	            	llp = G.getCoordinate(temp);
//	            	if(llp == null || llp.lat > 21.205756 || llp.lat < 20.946683 || llp.lng > 106.004683 || llp.lng < 105.665946)
//						llp = G.getCoordinateWithRegion(temp);
//	            	if(llp == null || llp.lat > 21.205756 || llp.lat < 20.946683 || llp.lng > 106.004683 || llp.lng < 105.665946)
//						llp = G.getCoordinateWithComponents(temp);
//	            	if(llp == null || llp.lat > 21.205756 || llp.lat < 20.946683 || llp.lng > 106.004683 || llp.lng < 105.665946)
//						llp = G.getCoordinateWithoutBound(temp);
//	            	if(llp == null){
//		        		rejPickup.add(pname);
//		        	}
//		        	else{
//		        		int ID = p2LatLng.size();
//		        		Point p = new Point(ID, llp.lat, llp.lng, pname, Constants.PUBLIC_POINT);
//		        		pickupPoints.add(p);
//		        		publicPoints.add(p);
//		        		name2p.put(pname, ID);
//		        		p2name[ID] = pname;
//		        		p2LatLng.put(ID, llp);
//		        	}
//		    	}
//	        }
//	    	//lay thong tin request
//	    	XSSFSheet peopleSheet = myWorkBook.getSheet("DSHS");
//			rows = peopleSheet.getLastRowNum();
//			
//			for(int i = 2; i <= rows; i++)
//	        {
//				Row row = peopleSheet.getRow(i);
//	        	String reqID = "";
//	        	Cell cell = row.getCell(1);
//	        	if(cell != null)
//	        		reqID = cell.getStringCellValue();
//	        	String aceID = "";
//	        	cell = row.getCell(2);
//	        	if(cell != null)
//	        		aceID = cell.getStringCellValue();
//	        	String tar = "";
//	        	cell = row.getCell(4);
//	        	if(cell != null)
//	        		tar = cell.getStringCellValue();
//	        	String pickName = "";
//	        	cell = row.getCell(6);
//	        	if(cell != null)
//	        		pickName = cell.getStringCellValue();
//	        	if(pickName.toLowerCase().contains("Ä‘iá»ƒm har") || pickName.toLowerCase().contains("Ä‘iá»ƒm tc")){
//	        		pickName = pickName.substring(pickName.indexOf(":") + 2);
//	        	}
//	        	pickName = pickName.trim();
//
//	        	String typeofReq = "";
//	        	cell = row.getCell(8);
//	        	if(cell != null)
//	        		typeofReq = cell.getStringCellValue();
//	        	int level = 0;
//	        	Point tgP = null;
//	        	Point pickupPoint = null;
//	        	LatLng ll = null;
//	        	for(int t = 0; t < targetPoints.size(); t++){
//	        		if(targetPoints.get(t).getName().equals(tar)){
//	        			tgP = targetPoints.get(t);
//	        			level = levelTarget.get(t);
//	        		}
//	        	}
//	        	if(tgP == null){
//	        		LatLng ll1 = p2LatLng.get(name2p.get(tar));
//	        		if(ll1 == null){
//	            		String tarName = tar;
//	            		if(!tarName.contains("HÃ  Ná»™i"))
//	            			tarName += " HÃ  Ná»™i";
//	            		ll1 = G.getCoordinate(tarName);
//	            		if(ll1 == null || ll1.lat > 21.205756 || ll1.lat < 20.946683 || ll1.lng > 106.004683 || ll1.lng < 105.665946)
//	    					ll1 = G.getCoordinateWithRegion(tarName);
//	            		if(ll1 == null || ll1.lat > 21.205756 || ll1.lat < 20.946683 || ll1.lng > 106.004683 || ll1.lng < 105.665946)
//	    					ll1 = G.getCoordinateWithComponents(tarName);
//	                	if(ll1 == null || ll1.lat > 21.205756 || ll1.lat < 20.946683 || ll1.lng > 106.004683 || ll1.lng < 105.665946)
//	    					ll1 = G.getCoordinateWithoutBound(tarName);
//	                	if(ll1 == null){
//		            		rejTarget.add(tar);
//		            		if(!rejPickup.contains(pickName))
//		        				rejPickup.add(pickName);
//		            	}
//		            	else{
//		            		int ID = p2LatLng.size();
//		            		tgP = new Point(ID, ll1.lat, ll1.lng, tar, Constants.SCHOOL_POINT);
//		            		targetPoints.add(tgP);
//		            		int lv = Constants.MN;
//		            		if(tar.contains("Trung há»�c"))
//		            			lv = Constants.THCS;
//		            		if(tar.contains("Tiá»ƒu há»�c"))
//		            			lv = Constants.TH;
//		            		levelTarget.add(lv);
//		            		name2p.put(tar, ID);
//		            		p2name[ID] = tar;
//		            		p2LatLng.put(ID, ll1);
//		            	}
//	        		}
//	        	}
//
//	        	pickupPoint = getElementInList(pickName, pickupPoints);
//	        	ll = p2LatLng.get(name2p.get(pickName));
//	        	if(pickupPoint == null){
//	        		if(ll == null){
//	            		String pN = pickName;
//	            		if(!pN.contains("HÃ  Ná»™i"))
//	            			pN += " HÃ  Ná»™i";
//	            		ll = G.getCoordinate(pN);
//	            		if(ll == null || ll.lat > 21.205756 || ll.lat < 20.946683 || ll.lng > 106.004683 || ll.lng < 105.665946)
//	    					ll = G.getCoordinateWithRegion(pN);
//	            		if(ll == null || ll.lat > 21.205756 || ll.lat < 20.946683 || ll.lng > 106.004683 || ll.lng < 105.665946)
//	    					ll = G.getCoordinateWithComponents(pN);
//	                	if(ll == null || ll.lat > 21.205756 || ll.lat < 20.946683 || ll.lng > 106.004683 || ll.lng < 105.665946)
//	    					ll = G.getCoordinateWithoutBound(pN);
//	                	if(ll == null){
//		        			if(!rejPickup.contains(pickName))
//		        				rejPickup.add(pickName);
//		        		}
//		        		else{
//		        			int typeofPoint = Constants.HOME_POINT;
//		        			if(typeofReq == "Táº¡i Ä‘iá»ƒm")
//		        				typeofPoint = Constants.PUBLIC_POINT;
//		        			int ID = p2LatLng.size();
//		        			pickupPoint = new Point(ID, ll.lat, ll.lng, pickName, typeofPoint);
//		        		}
//	        		}
//	        		else
//	        			System.out.println("pickup point has coordinate and notin pick list");
//	        	}
//	        	
//	        	if(pickupPoint != null && tgP != null){
//	        		pickupPoints.add(pickupPoint);
//	        		Request req = new Request(reqID, 0, aceID, level, typeofReq, pickupPoint, null, tgP);
//	        		reqList.add(req);
//	        		int ID = pickupPoint.getID();
//	        		if(ID == 48)
//    	        		System.out.println("rt");
//	        		name2p.put(pickName, ID);
//	        		p2name[ID] = pickName;
//	        		p2LatLng.put(ID, ll);
//	        	}
//	        }
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		this.nVehicle = n16 + n29 + n45;
//
//    	updateCoordinateData(dir);
//    	updatePointsData(dir);
////    	String tarFileName = dir + "targetpoints.xlsx";
////    	try{
////    		File tarfile = new File(tarFileName);
////	    	FileInputStream fin = new FileInputStream(tarfile);
////	    	XSSFWorkbook tbook = new XSSFWorkbook (fin); 
////	    	XSSFSheet tarSheet = tbook.getSheet("tar");
////	    	int trow = tarSheet.getLastRowNum();
////	    	
////			for(i = 1; i < trow; i++){
////				Row row = tarSheet.getRow(i);
////				int idA = (int)(row.getCell(0).getNumericCellValue());
////				String nm = row.getCell(1).getStringCellValue();
////				name2area.put(nm, idA);
////			}
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
//    }
//    
//    public void calTravelTime(String dir){
//    	GoogleMapsQuery G = new GoogleMapsQuery();
//    	readPointsData(dir);
//    	readCoordinateData(dir);
//    	int i = 0;
//    	for(String key1 : name2p.keySet()){
//    		for(String key2 : name2p.keySet()){
//    			int p1 = name2p.get(key1);
//    			int p2 = name2p.get(key2);
//				LatLng l1 = p2LatLng.get(p1);
//				LatLng l2 = p2LatLng.get(p2);
//				int t = G.getTravelTime(l1.lat, l1.lng, l2.lat, l2.lng, "driving");
//				if(t < 0)
//					System.out.println("Getting traveltime from " + p1 + " to " + p2 + " failed");
//				map[p1][p2] = (int)(t + 0.5*t);
//    		}
//    		i++;
//    		System.out.println("i = " + i);
//    	}
//    	updateMapDataTravelTime(dir);
//    }
//    
//    public void updatecalTravelTime(String dir){
//    	GoogleMapsQuery G = new GoogleMapsQuery();
//    	readPointsData(dir);
//    	readCoordinateData(dir);
//    	readMapDataTravelTime(dir);
//    	int i = 0;
//    	ArrayList<String> a = new ArrayList<String>();
//    	a.add("Techcombank TimesCity (Ã„ï¿½Ã†Â°Ã¡Â»ï¿½ng sÃ¡Â»â€˜ 4)");
//    	
//    	for(String key1 : name2p.keySet()){
//    		for(String key2 : name2p.keySet()){
//    			if(a.contains(key1) || a.contains(key2)){
//	    			int p1 = name2p.get(key1);
//	    			int p2 = name2p.get(key2);
//					LatLng l1 = p2LatLng.get(p1);
//					LatLng l2 = p2LatLng.get(p2);
//					int t = G.getTravelTime(l1.lat, l1.lng, l2.lat, l2.lng, "driving");
//					map[p1][p2] = (int)(t + 0.5 * t);
//    			}
//    		}
//    		i++;
//    		System.out.println("i = " + i);
//    	}
//    	updateMapDataTravelTime(dir);
//    }
//    
//    public void readSampleData2(String dir){
//
//    	String in = dir + "Sample-C2-Diem.xlsx";
//		
//    	readPointsData(dir);
//    	readCoordinateData(dir);
//    	readMapDataTravelTime(dir);
//		
//		try{
//			File f = new File(in);
//	    	FileInputStream fin = new FileInputStream(f);
//	    	XSSFWorkbook myWorkBook = new XSSFWorkbook (fin); 
//	    	XSSFSheet pointSheet = myWorkBook.getSheet("DSDD");
//	    	
//	    	int rows = pointSheet.getLastRowNum();
//	    	for(int i = 2; i <= rows; i++){
//	    		Row row = pointSheet.getRow(i);
//	    		Cell pick = row.getCell(1);
//	        	String pname = "";
//	        	if(pick != null)
//	        		pname = pick.getStringCellValue();
//	        	//pname = pname.trim();
//	        	LatLng llp = p2LatLng.get(name2p.get(pname));
//	        	if(llp == null){
//	            	System.out.println(pname);
//	        	}
//	//        	if(llp == null){
//	//        		double lat = r.nextInt(16000) + 2092000;
//	//        		lat = lat/100000;
//	//        		double lng = r.nextInt(19000) + 10578000;
//	//        		lng = lng/100000;
//	//        		llp = new LatLng(lat, lng);
//	//        	}
//	//        	String dname = del.getContents();
//	//        	LatLng lld = G.getCoordinate(dname);
//	//        	if(lld == null || lld.lat > 21.205756 || lld.lat < 20.946683 || lld.lng > 106.004683 || lld.lng < 105.665946)
//	//				lld = G.getCoordinateWithRegion(dname);
//	//        	if(lld == null || lld.lat > 21.205756 || lld.lat < 20.946683 || lld.lng > 106.004683 || lld.lng < 105.665946)
//	//				lld = G.getCoordinateWithComponents(dname);
//	//        	if(lld == null || lld.lat > 21.205756 || lld.lat < 20.946683 || lld.lng > 106.004683 || lld.lng < 105.665946)
//	//				lld = G.getCoordinateWithoutBound(dname);
//	//        	if(lld == null){
//	//        		double lat = r.nextInt(16000) + 2092000;
//	//        		lat = lat/100000;
//	//        		double lng = r.nextInt(19000) + 10578000;
//	//        		lng = lng/100000;
//	//        		lld = new LatLng(lat, lng);
//	//        	}
//	//        	if(llp == null || lld == null){
//	//        		rejPickup.add(pname);
//	//        		rejDel.add(dname);
//	//        	}
//	        	if(llp == null){
//	        		if(!rejPickup.contains(pname))
//	        			rejPickup.add(pname);
//	        	}
//	        	else{
//	        		Point p = new Point(name2p.get(pname), llp.lat, llp.lng, pname, Constants.PUBLIC_POINT);
//	        		pickupPoints.add(p);
//	        		publicPoints.add(p);
//	        		
//	        		//Point d = new Point(deliveryPoints.size(), lld.lat, lld.lng, dname, Constants.PUBLIC_POINT);
//	        		//deliveryPoints.add(d);
//	        	}
//	    	}
//	    	//lay thong tin request
//	    	XSSFSheet peopleSheet = myWorkBook.getSheet("DSHS");
//			rows = peopleSheet.getLastRowNum();
//			
//			for(int i = 2; i <= rows; i++)
//	        {
//				Row row = peopleSheet.getRow(i);
//	        	String reqID = "";
//	        	Cell cell = row.getCell(1);
//	        	if(cell != null)
//	        		reqID = cell.getStringCellValue();
//	        	String aceID = "";
//	        	cell = row.getCell(2);
//	        	if(cell != null)
//	        		aceID = cell.getStringCellValue();
//	        	String tar = "";
//	        	cell = row.getCell(4);
//	        	if(cell != null)
//	        		tar = cell.getStringCellValue();
//	        	String pickName = "";
//	        	cell = row.getCell(6);
//	        	if(cell != null)
//	        		pickName = cell.getStringCellValue();
//	        	if(pickName.toLowerCase().contains("Ä‘iá»ƒm har") || pickName.toLowerCase().contains("Ä‘iá»ƒm tc")){
//	        		pickName = pickName.substring(pickName.indexOf(":") + 2);
//	        	}
//	        	//pickName = pickName.trim();
//	        	String typeofReq = "";
//	        	cell = row.getCell(8);
//	        	if(cell != null)
//	        		typeofReq = cell.getStringCellValue();
//	        	int level = 0;
//	        	Point tgP = null;
//	        	for(int t = 0; t < targetPoints.size(); t++){
//	        		if(targetPoints.get(t).getName().equals(tar)){
//	        			tgP = targetPoints.get(t);
//	        			level = levelTarget.get(t);
//	        		}
//	        	}
//	        	if(tgP == null){
//	        		LatLng ll1 = p2LatLng.get(name2p.get(tar));
//	        		if(ll1 == null){
//	            		System.out.println(tar);
//	        		}
//	            	if(ll1 == null){
//	            		rejTarget.add(tar);
//	            		if(!rejPickup.contains(pickName))
//	        				rejPickup.add(pickName);
//	            	}
//	            	else{
//	            		tgP = new Point(name2p.get(tar), ll1.lat, ll1.lng, tar, Constants.SCHOOL_POINT);
//	            		targetPoints.add(tgP);
//	            		int lv = Constants.MN;
//	            		if(tar.contains("Trung há»�c"))
//	            			lv = Constants.THCS;
//	            		if(tar.contains("Tiá»ƒu há»�c"))
//	            			lv = Constants.TH;
//	            		levelTarget.add(lv);
//	            		aceIDlist.put(name2p.get(tar), new ArrayList<String>());
//	            	}
//	        	}
//	        	
//	        	Point pickupPoint = getElementInList(pickName, pickupPoints);
//	        	LatLng ll = p2LatLng.get(name2p.get(pickName));
//	        	if(pickupPoint == null){
//	        		if(ll == null){
//	            		System.out.println(pickName);
//	        		}
//	        		if(ll == null){
//	        			if(!rejPickup.contains(pickName))
//	        				rejPickup.add(pickName);
//	        			//if(!rejDel.contains(delName))
//	        			//	rejDel.add(delName);
//	        		}
//	        		else{
//	        			int typeofPoint = Constants.HOME_POINT;
//	        			if(typeofReq == "Táº¡i Ä‘iá»ƒm")
//	        				typeofPoint = Constants.PUBLIC_POINT;
//	        			pickupPoint = new Point(name2p.get(pickName), ll.lat, ll.lng, pickName, typeofPoint);
//	        		}
//	        	}
//	        	
//	        	if(pickupPoint != null && tgP != null){
//	        		pickupPoints.add(pickupPoint);
//	        		Request req = new Request(reqID, 0, aceID, level, typeofReq, pickupPoint, null, tgP);
//	        		reqList.add(req);
//	        		if(!aceID.equals("")){
//		        		ArrayList<String> aceList = aceIDlist.get(tgP.getID());
//		        		aceList.add(aceID);
//		        		aceIDlist.put(tgP.getID(), aceList);
//	        		}
//	        	}
//	        }
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		this.nVehicle = n16 + n29 + n45;
//    	this.capList = new double[nVehicle + 1];
//
//    	for(int i = 1; i <= this.n16; i++)
//    		capList[i] = 14;
//    	for(int i = 1; i <= this.n29; i++)
//    		capList[i+this.n16] = 27;
//    	for(int i = 1; i <= this.n45; i++)
//    		capList[i+this.n16 + this.n29] = 43;
//    }
//    
////    public void readSampleData(String dir){
////    	GoogleMapsQuery G = new GoogleMapsQuery();
////    	Random r = new Random();
////    	
////    	WorkbookSettings ws = null;
////		Workbook workbook = null;
////		String filename = dir + "SampleData.xls";
////		String mapfile = dir + "map.xls";
////		HashMap<String, HashMap<String, Integer>> oldMap = new HashMap<String, HashMap<String, Integer>>();
////		HashMap<String, LatLng> oldPoint = new HashMap<String, LatLng>();
////		readHistoricalData(dir, oldMap, oldPoint);
////		
////		WorkbookSettings ms = null;
////		Workbook mbook = null;
////		
////		try {
////			ws = new WorkbookSettings();
////			ws.setEncoding("Cp1252");
////			FileInputStream fs = new FileInputStream(new File(filename));
////			workbook = Workbook.getWorkbook(fs, ws);
////			
////			//get static points
////			//cac diem do va diem tra co dinh
////			Sheet pointSheet = workbook.getSheet("DSDD");
////			int rows = pointSheet.getRows() - 2;
////			for(int i = 2; i <= rows + 1; i++)
////            {
////            	Cell pick = pointSheet.getCell(1, i);
////            	Cell del = pointSheet.getCell(2, i);
////            	String pname = pick.getContents();
////            	LatLng llp = oldPoint.get(pname);
////            	if(llp == null){
////	            	String temp = pname;
////	            	if(!temp.contains(" HÃƒÂ  NÃ¡Â»â„¢i"))
////	            		temp += " HÃƒÂ  NÃ¡Â»â„¢i";
////	            	llp = G.getCoordinate(temp);
////	            	if(llp == null || llp.lat > 21.205756 || llp.lat < 20.946683 || llp.lng > 106.004683 || llp.lng < 105.665946)
////						llp = G.getCoordinateWithRegion(temp);
////	            	if(llp == null || llp.lat > 21.205756 || llp.lat < 20.946683 || llp.lng > 106.004683 || llp.lng < 105.665946)
////						llp = G.getCoordinateWithComponents(temp);
////	            	if(llp == null || llp.lat > 21.205756 || llp.lat < 20.946683 || llp.lng > 106.004683 || llp.lng < 105.665946)
////						llp = G.getCoordinateWithoutBound(temp);
////            	}
//////            	if(llp == null){
//////            		double lat = r.nextInt(16000) + 2092000;
//////            		lat = lat/100000;
//////            		double lng = r.nextInt(19000) + 10578000;
//////            		lng = lng/100000;
//////            		llp = new LatLng(lat, lng);
//////            	}
//////            	String dname = del.getContents();
//////            	LatLng lld = G.getCoordinate(dname);
//////            	if(lld == null || lld.lat > 21.205756 || lld.lat < 20.946683 || lld.lng > 106.004683 || lld.lng < 105.665946)
//////					lld = G.getCoordinateWithRegion(dname);
//////            	if(lld == null || lld.lat > 21.205756 || lld.lat < 20.946683 || lld.lng > 106.004683 || lld.lng < 105.665946)
//////					lld = G.getCoordinateWithComponents(dname);
//////            	if(lld == null || lld.lat > 21.205756 || lld.lat < 20.946683 || lld.lng > 106.004683 || lld.lng < 105.665946)
//////					lld = G.getCoordinateWithoutBound(dname);
//////            	if(lld == null){
//////            		double lat = r.nextInt(16000) + 2092000;
//////            		lat = lat/100000;
//////            		double lng = r.nextInt(19000) + 10578000;
//////            		lng = lng/100000;
//////            		lld = new LatLng(lat, lng);
//////            	}
//////            	if(llp == null || lld == null){
//////            		rejPickup.add(pname);
//////            		rejDel.add(dname);
//////            	}
////            	if(llp == null){
////            		rejPickup.add(pname);
////            	}
////            	else{
////            		Point p = new Point(pickupPoints.size(), llp.lat, llp.lng, pname, Constants.PUBLIC_POINT);
////            		pickupPoints.add(p);
////            		oldPoint.put(pname, llp);
////            		//Point d = new Point(deliveryPoints.size(), lld.lat, lld.lng, dname, Constants.PUBLIC_POINT);
////            		//deliveryPoints.add(d);
////            	}
////            }
////			
////			//lay thong tin request
////			Sheet peopleSheet = workbook.getSheet("DSHS");
////			rows = peopleSheet.getRows() - 2;
////			
////			for(int i = 2; i <= rows+1; i++)
////            {
////            	String reqID = peopleSheet.getCell(1, i).getContents();
////            	String aceID = peopleSheet.getCell(2, i).getContents();
////            	String tar = peopleSheet.getCell(4, i).getContents();
////            	String pickName = peopleSheet.getCell(6, i).getContents();
////            	String delName = peopleSheet.getCell(7, i).getContents();
////            	String typeofReq = peopleSheet.getCell(8, i).getContents();
////            	int level = 0;
////            	Point tgP = null;
////            	for(int t = 0; t < targetPoints.size(); t++){
////            		if(targetPoints.get(t).getName().equals(tar)){
////            			tgP = targetPoints.get(t);
////            			level = levelTarget.get(t);
////            		}
////            	}
////            	if(tgP == null){
////            		LatLng ll1 = oldPoint.get(tar);
////            		if(ll1 == null){
////	            		String tarName = tar;
////	            		if(!tarName.contains(" HÃƒÂ  NÃ¡Â»â„¢i"))
////	            			tarName += " HÃƒÂ  NÃ¡Â»â„¢i";
////	            		ll1 = G.getCoordinate(tarName);
////	            		if(ll1 == null || ll1.lat > 21.205756 || ll1.lat < 20.946683 || ll1.lng > 106.004683 || ll1.lng < 105.665946)
////	    					ll1 = G.getCoordinateWithRegion(tarName);
////	            		if(ll1 == null || ll1.lat > 21.205756 || ll1.lat < 20.946683 || ll1.lng > 106.004683 || ll1.lng < 105.665946)
////	    					ll1 = G.getCoordinateWithComponents(tarName);
////	                	if(ll1 == null || ll1.lat > 21.205756 || ll1.lat < 20.946683 || ll1.lng > 106.004683 || ll1.lng < 105.665946)
////	    					ll1 = G.getCoordinateWithoutBound(tarName);
////            		}
////                	if(ll1 == null){
////                		rejTarget.add(tar);
////                		if(!rejPickup.contains(pickName))
////            				rejPickup.add(pickName);
////            			//if(!rejDel.contains(delName))
////            			//	rejDel.add(delName);
////                	}
////                	else{
////                		tgP = new Point(targetPoints.size(), ll1.lat, ll1.lng, tar, Constants.SCHOOL_POINT);
////                		targetPoints.add(tgP);
////                		int lv = Constants.MN;
////                		if(tar.contains("Trung hÃ¡Â»ï¿½c"))
////                			lv = Constants.THCS;
////                		if(tar.contains("TiÃ¡Â»Æ’u hÃ¡Â»ï¿½c"))
////                			lv = Constants.TH;
////                		levelTarget.add(lv);
////                		oldPoint.put(tar, ll1);
////                	}
////            	}
////            	
////            	Point pickupPoint = getElementInList(pickName, pickupPoints);
////            	LatLng ll = oldPoint.get(pickName);
////            	if(pickupPoint == null){
////            		if(ll == null){
////	            		String pN = pickName;
////	            		if(!pN.contains(" HÃƒÂ  NÃ¡Â»â„¢i"))
////	            			pN += " HÃƒÂ  NÃ¡Â»â„¢i";
////	            		ll = G.getCoordinate(pN);
////	            		if(ll == null || ll.lat > 21.205756 || ll.lat < 20.946683 || ll.lng > 106.004683 || ll.lng < 105.665946)
////	    					ll = G.getCoordinateWithRegion(pN);
////	            		if(ll == null || ll.lat > 21.205756 || ll.lat < 20.946683 || ll.lng > 106.004683 || ll.lng < 105.665946)
////	    					ll = G.getCoordinateWithComponents(pN);
////	                	if(ll == null || ll.lat > 21.205756 || ll.lat < 20.946683 || ll.lng > 106.004683 || ll.lng < 105.665946)
////	    					ll = G.getCoordinateWithoutBound(pN);
////            		}
////            		if(ll == null){
////            			if(!rejPickup.contains(pickName))
////            				rejPickup.add(pickName);
////            			//if(!rejDel.contains(delName))
////            			//	rejDel.add(delName);
////            		}
////            		else{
////            			pickupPoint = new Point(pickupPoints.size(), ll.lat, ll.lng, pickName, Constants.HOME_POINT);
////            		}
////            	}
//////            	Point delPoint = getElementInList(delName, deliveryPoints);
//////            	if(delPoint == null){
//////            		LatLng ll = G.getCoordinate(delName);
//////            		if(ll == null || ll.lat > 21.205756 || ll.lat < 20.946683 || ll.lng > 106.004683 || ll.lng < 105.665946)
//////    					ll = G.getCoordinateWithRegion(delName);
//////            		if(ll == null || ll.lat > 21.205756 || ll.lat < 20.946683 || ll.lng > 106.004683 || ll.lng < 105.665946)
//////    					ll = G.getCoordinateWithComponents(delName);
//////                	if(ll == null || ll.lat > 21.205756 || ll.lat < 20.946683 || ll.lng > 106.004683 || ll.lng < 105.665946)
//////    					ll = G.getCoordinateWithoutBound(delName);
////////            		if(ll == null){
////////            			double lat = r.nextInt(16000) + 2092000;
////////                		lat = lat/100000;
////////                		double lng = r.nextInt(19000) + 10578000;
////////                		lng = lng/100000;
////////                		ll = new LatLng(lat, lng);
////////                	}
//////            		if(ll == null){
//////            			if(!rejPickup.contains(pickName))
//////            				rejPickup.add(pickName);
//////            			if(!rejDel.contains(delName))
//////            				rejDel.add(delName);
//////            		}
//////            		else
//////            			delPoint = new Point(deliveryPoints.size(), ll.lat, ll.lng, delName, Constants.HOME_POINT);
//////            	}
////            	//if(pickupPoint != null && delPoint != null && tgP != null){
////            	if(pickupPoint != null && tgP != null){
////            		pickupPoints.add(pickupPoint);
////            		//deliveryPoints.add(delPoint);
////            		//Request req = new Request(reqID, 0, aceID, level, typeofReq, pickupPoint, delPoint, tgP);
////            		Request req = new Request(reqID, 0, aceID, level, typeofReq, pickupPoint, null, tgP);
////            		reqList.add(req);
////            		oldPoint.put(pickName, ll);
////            	}
////            }
////			
////			this.nVehicle = n16 + n29 + n45;
////	    	this.capList = new double[nVehicle + 1];
////
////	    	for(int i = 1; i <= this.n16; i++)
////	    		capList[i] = 15;
////	    	for(int i = 1; i <= this.n29; i++)
////	    		capList[i+this.n16] = 28;
////	    	for(int i = 1; i <= this.n45; i++)
////	    		capList[i+this.n16 + this.n29] = 44;
////
////	    	int i = 0;
////	    	for(String key1 : oldPoint.keySet()){
////	    		HashMap<String, Integer> arg = oldMap.get(oldPoint.get(key1));
////	    		if(arg == null)
////	    			arg = new HashMap<String, Integer>();
////	    		for(String key2 : oldPoint.keySet()){
////	    			int time = -1;
////	    			if(arg.containsKey(key2))
////	    				time = arg.get(key2);
////	    			else{
////	    				time = G.getTravelTime(oldPoint.get(key1).lat, oldPoint.get(key1).lng, oldPoint.get(key2).lat, oldPoint.get(key2).lng, "driving");
////	    			}
////	    			arg.put(key2, time);
////	    		}
////	    		oldMap.put(key1, arg);
////	    		i++;
////	    		System.out.println("i = " + i);
////	    	}
////	    	updateHistoricalData(dir, oldMap, oldPoint);		
////			
////		} catch (BiffException e) {
////			System.out.println("File not found\n" + e.toString());
////	    } catch (IOException e) {
////	        e.printStackTrace(); 
////	    }
////    }
//    private Point getMinCusElementInList(String name, ArrayList<Point> arr){
//    	int minNb = 100000000;
//    	Point p = null;
//    	for(int i = 0; i < arr.size(); i++){
//    		Point t = arr.get(i);
//    		if(name.equals(t.getName())){
//    			if(nbCus.get(t).size() < minNb){
//    				minNb = nbCus.get(t).size();
//    				p = t;
//    			}
//    		}
//    	}
//    	return p;
//    }
//    
//    private Point getElementInList(String name, ArrayList<Point> arr){
//    	
//    	for(int i = 0; i < arr.size(); i++){
//    		Point t = arr.get(i);
//    		boolean a = name.equals(t.getName());
//    		if(name.equals(t.getName()) || (name.contains("Ä�iá»ƒm HAR") && name.contains(t.getName())))
//    			return t;
//    	}
//    	return null;
//    }
//    
//    public Point getNearestPublicPoint(Point p, ArrayList<Point> arr){
//    	GoogleMapsQuery G = new GoogleMapsQuery();
//    	Point publicpoint = null;
//    	int minT = 100000000;
//    	for(int i = 0; i < arr.size(); i++){
//    		Point t = arr.get(i);
//    		if(t.getType() == Constants.PUBLIC_POINT){
//    			if(t.getName().equals(p.getName()))
//    				return t;
//    			int p1 = name2p.get(p.getName());
//    			int p2 = name2p.get(t.getName());
//    			int time = map[p1][p2];
//    			if(time < minT){
//    				minT = time;
//    				publicpoint = t;
//    			}
//    		}
//    	}
//    	return publicpoint;
//    }
//    
//    public void extract(String dir, int lv, String typeofReq, String tarName, int ear, int dur, int late){
//    	this.points = new ArrayList<Point>();
//    	//this.startPoints = new ArrayList<Point>();
//    	//this.stopPoints = new ArrayList<Point>();
//    	this.pickupPs = new ArrayList<Point>();
//    	this.nbCus = new HashMap<Point, ArrayList<String>>();
//    	this.earliestAllowedArrivalTime = new HashMap<Point, Integer>();
//    	this.serviceDuration = new HashMap<Point, Integer>();
//    	this.lastestAllowedArrivalTime = new HashMap<Point, Integer>();
//    	this.type = new HashMap<Point, String>();
//
////    	Point tar = null;
////    	for(int i = 0; i < targetPoints.size(); i++){
////    		if(targetPoints.get(i).getName().equals(tarName)){
////    			tar = targetPoints.get(i);
////    			break;
////    		}
////    	}
//    	
////    	for(int i = 0; i < this.nVehicle; i++){
////    		Point sp = new Point(name2p.get(tar.getName()), tar.getX(), tar.getY(), tar.getName(), tar.getType());
////    		startPoints.add(sp);
////    		points.add(sp);
////    		nbCus.put(sp, new ArrayList<String>());
////    		type.put(sp, "start");
////    		Point ep = new Point(name2p.get(tar.getName()), tar.getX(), tar.getY(), tar.getName(), tar.getType());
////    		stopPoints.add(ep);
////    		points.add(ep);
////    		nbCus.put(ep, new ArrayList<String>());
////    		type.put(ep, "stop");
////    		earliestAllowedArrivalTime.put(sp, 0);
////			serviceDuration.put(sp, 0);
////			lastestAllowedArrivalTime.put(sp, late);
////			
////			earliestAllowedArrivalTime.put(ep, 0);
////			serviceDuration.put(ep, 0);
////			lastestAllowedArrivalTime.put(ep,late);
////    		
////    	}
//    	ArrayList<Point> tempPoints = new ArrayList<Point>();
//    	HashMap<Point, String> tempType = new HashMap<Point, String>();
//    	HashMap<Point, ArrayList<String>> tempNbCus = new HashMap<Point, ArrayList<String>>();
//    	ArrayList<Request> tempReqList = new ArrayList<Request>();
//    	
//    	for(int i = 0; i < reqList.size(); i++){
//    		Request r = reqList.get(i);
//
//    		if((r.level == lv && r.targerPoint.getName().equals(tarName) && r.typeofReq.equals(typeofReq))
//    				|| (typeofReq.equals("Táº¡i nhÃ ") && r.typeofReq.equals(typeofReq) && r.level != Constants.MN)){
//    			Point p = r.pickupPoint;
//    			Point pickup = getMinCusElementInList(p.getName(), points);
//    			if(pickup == null){
//		    		pickup = new Point(name2p.get(p.getName()), p.getX(), p.getY(), p.getName(), p.getType());
//		    		points.add(pickup);
//		    		pickupPs.add(pickup);
//		    		ArrayList<String> cus = new ArrayList<String>();
//		    		cus.add(r.ID);
//		    		nbCus.put(pickup, cus);
//		    		type.put(pickup, r.typeofReq);
//		    		earliestAllowedArrivalTime.put(pickup, ear);
//					serviceDuration.put(pickup, dur);
//					lastestAllowedArrivalTime.put(pickup, late);
//    			}
//    			else{
//    				int nb = nbCus.get(pickup).size();
//    				if(nb < 43){
//    					ArrayList<String> cus = nbCus.get(pickup);
//    		    		cus.add(r.ID);
//    					nbCus.put(pickup, cus);
//    				}
//    				else{
//    					pickup = new Point(name2p.get(p.getName()), p.getX(), p.getY(), p.getName(), p.getType());
//    		    		points.add(pickup);
//    		    		pickupPs.add(pickup);
//    		    		ArrayList<String> cus = new ArrayList<String>();
//    		    		cus.add(r.ID);
//    		    		nbCus.put(pickup, cus);
//    		    		type.put(pickup, r.typeofReq);
//    		    		earliestAllowedArrivalTime.put(pickup, ear);
//    					serviceDuration.put(pickup, dur);
//    					lastestAllowedArrivalTime.put(pickup, late);
//    				}
//    			}
//    			reqList.remove(i);
//    			--i;
//    		}
//    		else if(r.level == Constants.TH && lv == Constants.THCS && aceIDlist.get(name2p.get(tarName)).contains(r.aceID) && r.typeofReq.equals(typeofReq)){
//    			Point p = r.pickupPoint;
//    			if(name2area.get(p.getName()) == name2area.get(tarName)){	    			
//	    			Point pickup = getElementInList(p.getName(), tempPoints);
//	    			if(pickup == null){
//			    		pickup = new Point(name2p.get(p.getName()), p.getX(), p.getY(), p.getName(), p.getType());
//			    		tempPoints.add(pickup);
//			    		tempType.put(pickup, r.typeofReq);
//			    		ArrayList<String> cus = new ArrayList<String>();
//			    		cus.add(r.ID);
//			    		tempNbCus.put(pickup, cus);
//	    			}
//	    			else{
//	    				ArrayList<String> cus = tempNbCus.get(pickup);
//			    		cus.add(r.ID);
//	    				tempNbCus.put(pickup, cus);
//	    			}
//	    			tempReqList.add(r);
//    			}    			
//    		}
//    	}
//    	
//    	for(int i = 0; i < tempPoints.size(); i++){
//    		Point p = tempPoints.get(i);
//			Point pickup = getMinCusElementInList(p.getName(), points);
//			if(pickup != null){
//				int nb = nbCus.get(pickup).size() + tempNbCus.get(p).size();
//				if(nb <= 43){
//					ArrayList<String> cus = nbCus.get(pickup);
//					cus.addAll(tempNbCus.get(p));
//					nbCus.put(pickup, cus);
//					for(int j = 0; j < tempReqList.size(); j++){
//						Request r = tempReqList.get(j);
//						if(r.pickupPoint.getName().equals(p.getName()))
//							reqList.remove(r);
//					}
//				}
//			}
//    	}
//    }
//    
//    public void createDataFile(String dir, String tarName, int lv, String typeofReq){
//    	XSSFWorkbook workbook = new XSSFWorkbook();
//    	
//    	XSSFSheet reqSheet = workbook.createSheet("requests");
//		Row row = reqSheet.createRow(0);
//        Cell cell = row.createCell(0);
//        cell.setCellValue("ID Ä‘iá»ƒm Ä‘Ã³n");
//        cell = row.createCell(1);
//        cell.setCellValue("TÃªn Ä‘iá»ƒm Ä‘Ã³n");
//        cell = row.createCell(2);
//        cell.setCellValue("Kiá»ƒu Ä‘iá»ƒm Ä‘Ã³n");	
//        cell = row.createCell(3);
//        cell.setCellValue("Kinh Ä‘á»™");
//        cell = row.createCell(4);
//        cell.setCellValue("VÄ© Ä‘á»™");
//        cell = row.createCell(5);
//        cell.setCellValue("Kiá»ƒu req");
//        cell = row.createCell(6);
//        cell.setCellValue("Sá»‘ HS");
//        cell = row.createCell(7);
//        cell.setCellValue("EarliestArr");
//        cell = row.createCell(8);
//        cell.setCellValue("ServiceDuration");
//        cell = row.createCell(7);
//        cell.setCellValue("LatestArr");
//        cell = row.createCell(10);
//        cell.setCellValue("NameList");
//		int k = 1;
//		for(int i = 0; i < points.size(); i++){
//			Point p = points.get(i);
//			row = reqSheet.createRow(k);
//			cell = row.createCell(0);
//			cell.setCellValue(p.getID());
//			cell = row.createCell(1);
//			cell.setCellValue(p.getName());
//			cell = row.createCell(2);
//			cell.setCellValue(p.getType());
//			cell = row.createCell(3);
//			cell.setCellValue(p.getX());
//			cell = row.createCell(4);
//			cell.setCellValue(p.getY());
//			cell = row.createCell(5);
//			cell.setCellValue(type.get(p));
//			ArrayList<String> cus = nbCus.get(p);
//			cell = row.createCell(6);
//			cell.setCellValue(cus.size());
//			cell = row.createCell(7);
//			cell.setCellValue(earliestAllowedArrivalTime.get(p));
//			cell = row.createCell(8);
//			cell.setCellValue(serviceDuration.get(p));
//			cell = row.createCell(9);
//			cell.setCellValue(lastestAllowedArrivalTime.get(p));
//			String str = "";
//			for(int j = 0; j < cus.size(); j++){
//				if(j != cus.size() - 1)
//					str += cus.get(j) + ",";
//				else
//					str += cus.get(j);
//			}
//			cell = row.createCell(10);
//			cell.setCellValue(str);
//			k++;
//		}
//
//        try {
//        	String name = tarName.replaceAll(" ", "-");
//        	String tp = typeofReq.replaceAll(" ", "-");
//			String filename = dir + "extractRealData-TrÆ°á»�ng-" + name + "-Ä�Ã³n-" + tp + ".xlsx";
//			File file = new File(filename);
//            FileOutputStream outputStream = new FileOutputStream(file);
//            workbook.write(outputStream);
//            workbook.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//			
//    }
//    
//    public Point getPointbyId(int id, ArrayList<Point> points){
//    	for(int i = 0; i < points.size(); i++)
//    		if(points.get(i).ID == id)
//    			return points.get(i);
//    	return null;
//    }
//    
//    public SchoolBusRoutingInput readDataFile(String dir, String typeofReq, String tarName, int lv){
//    	String name = tarName.replaceAll(" ", "-");
//    	String tp = typeofReq.replaceAll(" ", "-");
//		String fileReqname = dir + "extractRealData-TrÆ°á»�ng-" + name + "-Ä�Ã³n-" + tp + ".xlsx";
//		String tarFileName = dir + "targetpoints.xlsx";
//		
//		ArrayList<Integer> earTar = new ArrayList<Integer>();
//		ArrayList<Integer> serTar = new ArrayList<Integer>();
//		ArrayList<Integer> lateTar = new ArrayList<Integer>();
//		int maxEar = -1;
//		int minSer = 120;
//		int minLate = 100000000;
//		try{
//    		File tarfile = new File(tarFileName);
//	    	FileInputStream fin = new FileInputStream(tarfile);
//	    	XSSFWorkbook tbook = new XSSFWorkbook (fin); 
//	    	XSSFSheet tarSheet = tbook.getSheet("tar");
//	    	int trow = tarSheet.getLastRowNum();
//	    	
//			for(int i = 1; i <= trow; i++){
//				Row row = tarSheet.getRow(i);
//				int idA = (int)(row.getCell(0).getNumericCellValue());
//				String nm = row.getCell(1).getStringCellValue();
//				name2area.put(nm, idA);
//				if(nm.equals(tarName) && !typeofReq.equals("Táº¡i nhÃ ")){
//					maxEar = (int)(row.getCell(2).getNumericCellValue());
//					minSer = (int)(row.getCell(3).getNumericCellValue());
//					minLate = (int)(row.getCell(4).getNumericCellValue());
//				}
//				else{
//					earTar.add((int)(row.getCell(2).getNumericCellValue()));
//					serTar.add((int)(row.getCell(3).getNumericCellValue()));
//					lateTar.add((int)(row.getCell(4).getNumericCellValue()));
//				}
//			}
//			for(int i = 0; i < earTar.size(); i++){
//				if(maxEar < earTar.get(i))
//					maxEar = earTar.get(i);
//				if(minLate > lateTar.get(i))
//					minLate = lateTar.get(i);
//			}
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		//readHistoricalData(dir);
//		readPointsData(dir);
//		readCoordinateData(dir);
//		readMapDataTravelTime(dir);
//		readMapDataTravelDistance(dir);
//		
//    	this.points = new ArrayList<Point>();
//    	this.startPoints = new ArrayList<Point>();
//    	this.stopPoints = new ArrayList<Point>();
//    	this.pickupPs = new ArrayList<Point>();
//    	this.nbCus = new HashMap<Point, ArrayList<String>>();
//    	this.earliestAllowedArrivalTime = new HashMap<Point, Integer>();
//    	this.serviceDuration = new HashMap<Point, Integer>();
//    	this.lastestAllowedArrivalTime = new HashMap<Point, Integer>();
//    	this.type = new HashMap<Point, String>();
//
//    	this.nVehicle = n16 + n29 + n45;
//    	this.capList = new double[nVehicle+1];
//    	for(int i = 0; i < this.nVehicle; i++){
//    		int ID = name2p.get(tarName);
//    		LatLng ll = p2LatLng.get(ID);
//			Point sp = new Point(ID, ll.lat, ll.lng, tarName, Constants.SCHOOL_POINT);
//			startPoints.add(sp);
//			points.add(sp);
//			nbCus.put(sp, new ArrayList<String>());
//			type.put(sp, "start");
//			Point ep = new Point(ID, ll.lat, ll.lng, tarName, Constants.SCHOOL_POINT);
//			stopPoints.add(ep);
//			points.add(ep);
//			nbCus.put(ep, new ArrayList<String>());
//			type.put(ep, "stop");
//			earliestAllowedArrivalTime.put(sp, 0);
//			serviceDuration.put(sp, 0);
//			lastestAllowedArrivalTime.put(sp, minLate);
//			
//			earliestAllowedArrivalTime.put(ep, 0);
//			serviceDuration.put(ep, 0);
//			lastestAllowedArrivalTime.put(ep, minLate);
//			
//		}
//    	
//    	for(int i = 1; i <= this.n29; i++)
//    		capList[i] = 27;
//    	for(int i = 1; i <= this.n16; i++)
//    		capList[i + this.n29] = 14;
//    	for(int i = 1; i <= this.n45; i++)
//    		capList[i + this.n16 + this.n29] = 43;
//
//    	try{
//    		
//	    	File reqfile = new File(fileReqname);
//	    	FileInputStream reqfin = new FileInputStream(reqfile);
//	    	XSSFWorkbook rbook = new XSSFWorkbook (reqfin); 
//	    	XSSFSheet reqSheet = rbook.getSheet("requests");
//	    	if(reqSheet == null)
//	    		return null;
//			int rMap = reqSheet.getLastRowNum();
//	
//			this.nRequest = 0;
//			int sum = 0;
//			SchoolBusRequest[] requests = new SchoolBusRequest[rMap];
//			for(int i = 1; i <= rMap; i++){
//				Row row = reqSheet.getRow(i);
//				int ID = (int)(row.getCell(0).getNumericCellValue());
//				String pname = row.getCell(1).getStringCellValue();
//				int typeOfPoint = (int)(row.getCell(2).getNumericCellValue());
//				double lat = row.getCell(3).getNumericCellValue();
//				double lng = row.getCell(4).getNumericCellValue();
//				String typeOfReq = row.getCell(5).getStringCellValue();
//				int cus = (int)(row.getCell(6).getNumericCellValue());
//				int ear = (int)(row.getCell(7).getNumericCellValue());
//				int ser = (int)(row.getCell(8).getNumericCellValue());
//				int last = (int)(row.getCell(9).getNumericCellValue());
//				String[] nameList = row.getCell(10).getStringCellValue().split(",");
//				Point p = new Point(ID, lat, lng, pname, typeOfPoint);
//				points.add(p);
//				ArrayList<String> cusList = new ArrayList<String>();
//				if(typeOfPoint != 0)
//					for(int k = 0; k < nameList.length; k++)
//						cusList.add(nameList[k]);
//				nbCus.put(p, cusList);
//				type.put(p, typeOfReq);
//				earliestAllowedArrivalTime.put(p, ear);
//				serviceDuration.put(p, ser);
//				lastestAllowedArrivalTime.put(p, last);
//				nRequest++;
//				sum += nameList.length;
//				pickupPs.add(p);
//				requests[i-1] = new SchoolBusRequest(i, i,
//						p.ID, p.ID, ear, ser, last, ear, ser, last);
//	        }
//			SchoolBusRequest[] reqtemp = new SchoolBusRequest[sum];
//			int k = 0;
//			for(int i = 0; i < requests.length; i++){
//				Point p = getPointbyId(requests[i].getPickupLocationId(), pickupPs);
//				ArrayList<String> cusList = nbCus.get(p);
//				for(int j = 0; j < cusList.size(); j++){
//					reqtemp[k] = new SchoolBusRequest(k, k,
//							p.ID, name2p.get(tarName), 23400, 120, 27000, 23400, 120, 27000);
//					k++;
//				}
//			}
//			requests = reqtemp;
////			int idx = 0;
////			HashMap<Integer, Integer> id2idx = new HashMap<Integer, Integer>();
////			for(int i = 0; i < points.size(); i++){
////				if(id2idx.get(points.get(i).ID) == null){
////					id2idx.put(points.get(i).ID, idx);
////					idx++;
////				}
////			}
//			DistanceElement[] distances = new DistanceElement[points.size()*points.size()];
//			costT = new int[points.size()][points.size()];
//			for(int i = 0; i < points.size(); i++){
//				for(int j = 0; j < points.size(); j++){
//					Point f = points.get(i);
//					Point t = points.get(j);
//					costT[i][j] = map[f.ID][t.ID];
//					distances[i*points.size() + j] = new DistanceElement(f.ID,
//							t.ID, map_D[f.ID][t.ID], costT[i][j], 2);
//					//costD[i][j] = mapD[f.ID][t.ID];
//				}
//			}
//			ConfigParam params = new ConfigParam(23400, 27000, 23400, 27000, 25);
//			//create datafile
//	    	Vehicle[] vehicles = new Vehicle[nVehicle];
//	    	for(int i = 1; i <= nVehicle; i++){
//	    		vehicles[i-1] = new Vehicle(i, (int)capList[i]);
//	    	}
//	    	int shoolPointId = 0;
//	    	
//	    	input = new SchoolBusRoutingInput(vehicles,
//	    			shoolPointId, requests,
//	    			distances, params);
//	    	return input;
//
//    	} catch (IOException e) {
//			e.printStackTrace();
//		}
//    	return null;
//    }
//    
//    public void printBackSolution(String dir, String typeofReq, String tarname, int lv){
//		String name = tarname.replaceAll(" ", "-");
//    	String tp = typeofReq.replaceAll(" ", "-");
//		String filename = dir + "solution-TrÆ°á»�ng-" + name + "-Ä�Ã³n-" + tp + ".xlsx";
//		
//		readPointsData(dir);
//		readCoordinateData(dir);
//		readMapDataTravelTime(dir);
//		try {
//			
//            FileInputStream inputStream = new FileInputStream(new File(filename));
//            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
// 
//            XSSFSheet sheet = workbook.getSheetAt(0);
// 
//            int rowCount = sheet.getLastRowNum();
//
//            Row row = sheet.getRow(0);
//            Cell cell = row.createCell(7);
//            cell.setCellValue("Thá»�i gian Ä‘i tháº³ng");
//            cell = row.createCell(8);
//            cell.setCellValue("Thá»�i gian ngá»“i trÃªn xe chiá»�u Ä‘i");
//            cell = row.createCell(9);
//            cell.setCellValue("Ä�iá»ƒm tráº£");
//            cell = row.createCell(10);
//            cell.setCellValue("Thá»�i gian tráº£");
//            cell = row.createCell(11);
//            cell.setCellValue("Thá»�i gian ngá»“i trÃªn xe chiá»�u vá»�");
//
//            row = sheet.getRow(1);
//            String curID = row.getCell(0).getStringCellValue();
//            ArrayList<String> namePoint = new ArrayList<String>();
//            ArrayList<Integer> time = new ArrayList<Integer>();
//            int id = 1;
//            for (int i = 1; i < rowCount; i++) {
//                row = sheet.getRow(i);
//                cell = row.getCell(0);
//                String busID = cell.getStringCellValue();
//                
//                if(!busID.equals(curID) || i == rowCount - 1){
//                	int curT_del = 59400;
//                	String cur_name = namePoint.get(namePoint.size() - 1);
//                	Row row1 = null;
//                	for(int k = 0; k < namePoint.size() - 1; k++){
//                		row1 = sheet.getRow(id+k);
//                		int dr_t = map[name2p.get(namePoint.get(k))][name2p.get(namePoint.get(0))];
//                		cell = row1.createCell(7);
//                		cell.setCellValue((int)(dr_t));
//                		cell = row1.createCell(8);
//                		cell.setCellValue(time.get(namePoint.size() - 1) - time.get(k) - 120);
//                	}
//                	for(int k = 0; k < namePoint.size(); k++){
//                		row1 = sheet.getRow(id + k);
//                		cell = row1.createCell(9);
//                		cell.setCellValue(namePoint.get(namePoint.size() - k - 1));
//                		int t_del = map[name2p.get(namePoint.get(namePoint.size()- k - 1))][name2p.get(cur_name)];
//                		int t = curT_del + t_del;
//                		curT_del = t + 120;
//                		cur_name = namePoint.get(namePoint.size() - k - 1);
//        				int h = (t-120)/3600;
//        				int m = (t - 120 - h*3600)/60;
//        				int s = t - h*3600 - m*60 - 120;
//        				String str = h + ":" + m + ":" + s;
//                		cell = row1.createCell(10);
//                		cell.setCellValue(str);
//                		cell = row1.createCell(11);
//                		cell.setCellValue(t_del);
//                	}
//                	
//                	namePoint = new ArrayList<String>();
//                    time = new ArrayList<Integer>(); 
//                    curID = busID;
//                    id = i;
//                }
//                
//                cell = row.getCell(1);
//                namePoint.add(cell.getStringCellValue());
//                cell = row.getCell(2);
//                String[] strTime = cell.getStringCellValue().split(":");
//                int t_arr = Integer.parseInt(strTime[0])*3600 + Integer.parseInt(strTime[1])*60 + Integer.parseInt(strTime[2]);
//                time.add(t_arr);
//            }
// 
//            inputStream.close();
// 
//            FileOutputStream outputStream = new FileOutputStream(filename);
//            workbook.write(outputStream);
//            workbook.close();
//            outputStream.close();
//             
//        } catch (IOException | EncryptedDocumentException ex) {
//            ex.printStackTrace();
//        }
//    }
//    
//    public SchoolBusRoutingInput createInputFile(){
//    	String dir = "E:/Project/cblsvr/VinSchool/VinSchoolAPI/dailyoptapi/data/vinschool/";
//		int lv = Constants.THCS;
//		String type = "Táº¡i Ä‘iá»ƒm";
//		String name = "Trung há»�c Harmony";
//    	return readDataFile(dir, type, name, lv);
//    }
//    
//    public static void main(String[] args){
//    	String dir = "E:\\Project\\cblsvr\\VinSchool\\VinSchoolAPI\\dailyoptapi\\data\\vinschool\\";
//    	
//    	DataProcessing dtp = new DataProcessing(10, 10, 10);
//    	dtp.readSampleData(dir);
//    	dtp.calTravelTime(dir);
//    	//dtp.calDistance(dir);
//    	//dtp.updatecalTravelTime(dir);
//    	//dtp.printBackSolution(dir, "TÃ¡ÂºÂ¡i Ã„â€˜iÃ¡Â»Æ’m", "Trung hÃ¡Â»ï¿½c Harmony", Constants.THCS);
////    	String type = "Táº¡i Ä‘iá»ƒm";
////    	dtp.readSampleData2(dir);
////    	dtp.extract(dir, Constants.THCS, type, "Trung há»�c Harmony", 6*3600+30*60, 2*60, 7*3600+30*60);
////    	dtp.createDataFile(dir, "Trung há»�c Harmony", Constants.THCS, type);
////    	dtp.extract(Constants.TH, "TiÃ¡Â»Æ’u hÃ¡Â»ï¿½c Harmony", 6*3600+30*60, 2*60, 7*3600+30*60);
////    	dtp.createDataFile(dir, Constants.TH);
////    	dtp.extract(Constants.MN, "MÃ¡ÂºÂ§m non Harmony", 6*3600+45*60, 2*60, 7*3600+45*60);
////    	dtp.createDataFile(dir, Constants.MN);
//    	//dtp.readDataFile(dir);
//    	System.out.println("Done!");
//    }
//}
