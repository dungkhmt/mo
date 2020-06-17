package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting;

import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.LatLng;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.DateTimeUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class QueryDistance {
	public ArrayList<Integer> ids;
	public HashMap<Integer, LatLng> id2LatLng;
	
	public QueryDistance(){
		ids = new ArrayList<Integer>();
		id2LatLng = new HashMap<Integer, LatLng>();
	}
	public void readDataFile(String dir, String fileName){
    	String in = dir + fileName;
		
		try{
			File f = new File(in);
	    	FileInputStream fin = new FileInputStream(f);
	    	XSSFWorkbook myWorkBook = new XSSFWorkbook (fin); 
	    	XSSFSheet pointSheet = myWorkBook.getSheet("Sheet1");
	    	
	    	int rows = pointSheet.getLastRowNum();
	    	for(int i = 0; i <= rows; i++){
	    		Row row = pointSheet.getRow(i);
	    		Cell c1 = row.getCell(0);
	        	int pId = -1;
	        	if(c1 != null)
	        		pId = (int)c1.getNumericCellValue();
	        	ids.add(pId);
	        	double lat = -1;
				Cell c2 = row.getCell(2);
				if(c2 != null)
					lat = c2.getNumericCellValue();
				
				double lng = -1;
				Cell c3 = row.getCell(3);
				if(c3 != null)
					lng = c3.getNumericCellValue();
				LatLng latlng = new LatLng(lat, lng);
				id2LatLng.put(pId, latlng);
	    	}
	    	myWorkBook.close();
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	public void updateOutputFile(String dir, String outputfileName){
    	String in = dir + outputfileName;
    	String out = dir + outputfileName;
    	GoogleMapsQuery G = new GoogleMapsQuery();
    	String date = "2019-07-29 07:00:00";
        long departure_time = DateTimeUtils.dateTime2Int(date);
        XSSFWorkbook myWorkBook;
        XSSFSheet pointSheet;
        
		try{
			File f = new File(in);
	    	FileInputStream fin = new FileInputStream(f);
	    	myWorkBook = new XSSFWorkbook (fin); 
	    	pointSheet = myWorkBook.getSheet("map");
	    	
	    	int rows = pointSheet.getLastRowNum();
	    	for(int i = 1; i <= rows; i++){
	    		Row row = pointSheet.getRow(i);
	    		Cell c1 = row.getCell(0);
	        	int pId1 = -1;
	        	if(c1 != null)
	        		pId1 = (int)c1.getNumericCellValue();
	        	
	        	Cell c2 = row.getCell(1);
	        	int pId2 = -1;
	        	if(c2 != null)
	        		pId2 = (int)c2.getNumericCellValue();
	        	
	        	Cell c3 = row.getCell(2);
	        	int t = -1;
	        	if(c3 != null)
	        		t = (int)c3.getNumericCellValue();
	        	
	        	if(t < 0){
	        		LatLng l1 = id2LatLng.get(pId1);
					LatLng l2 = id2LatLng.get(pId2);
					int t1 = G.getTravelTime(l1.lat, l1.lng, l2.lat, l2.lng, "driving", departure_time);
					if(t1 <= 0){
						System.out.println("Getting traveltime from " + pId1 + " to " + pId2 + " failed");
						//continue;
					}
					c3.setCellValue(t1);
	        	}
	    	}
	    	
	    	FileOutputStream fos = new FileOutputStream(new File(out));
			myWorkBook.write(fos);
			myWorkBook.close();
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	public void checkOutputFileComplete(String dir, String outputfileName){
    	String in = dir + outputfileName;
    	String out = dir + outputfileName;
    	GoogleMapsQuery G = new GoogleMapsQuery();
    	String date = "2019-07-29 07:00:00";
        long departure_time = DateTimeUtils.dateTime2Int(date);
        XSSFWorkbook myWorkBook;
        XSSFSheet pointSheet;
        
		try{
			File f = new File(in);
	    	FileInputStream fin = new FileInputStream(f);
	    	myWorkBook = new XSSFWorkbook (fin); 
	    	pointSheet = myWorkBook.getSheet("map");
	    	
	    	int rows = pointSheet.getLastRowNum();
	    	int id = 400;
	    	ArrayList<Integer> lstId = new ArrayList<Integer>(ids);
	    	
	    	for(int i = 1; i <= rows; i++){
	    		Row row = pointSheet.getRow(i);
	    		Cell c1 = row.getCell(0);
	        	int pId1 = -1;
	        	if(c1 != null)
	        		pId1 = (int)c1.getNumericCellValue();
	        	if(id != pId1){
	        		for(int j = 0; j < lstId.size(); j++){
	        			if(id == lstId.get(j))
	        				continue;
	        			LatLng l1 = id2LatLng.get(id);
						LatLng l2 = id2LatLng.get(lstId.get(j));
						int t = G.getTravelTime(l1.lat, l1.lng, l2.lat, l2.lng, "driving", departure_time);
						if(t < 0){
							System.out.println("Getting traveltime from " + ids.get(i) + " to " + ids.get(j) + " failed");
							//continue;
						}
						try{
							int rownum = rows + 1;
							Row newrow = pointSheet.createRow(rownum);
							Cell cell = newrow.createCell(0);
							cell.setCellValue(ids.get(i));
							cell = newrow.createCell(1);
							cell.setCellValue(ids.get(j));
							cell = newrow.createCell(2);
							cell.setCellValue(t);
							FileOutputStream fos = new FileOutputStream(new File(outputfileName));
							myWorkBook.write(fos);
							myWorkBook.close();
						}catch(Exception e){
							System.out.println(e);
						}
	        		}
	        		id = pId1;
	        		lstId = new ArrayList<Integer>(ids);
	        	}
	        	Cell c2 = row.getCell(1);
	        	int pId2 = -1;
	        	if(c2 != null)
	        		pId2 = (int)c2.getNumericCellValue();
	        	System.out.println(lstId.indexOf(pId2));
	        	lstId.remove(lstId.indexOf(pId2));
	    	}
	    	
	    	FileOutputStream fos = new FileOutputStream(new File(out));
			myWorkBook.write(fos);
			myWorkBook.close();
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	public void queryDistance(String dir, String outFileName){
		GoogleMapsQuery G = new GoogleMapsQuery();
		String filename = dir + outFileName;
		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet mapSheet = workbook.createSheet("map");

        Row row = mapSheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("idF");
        cell = row.createCell(1);
        cell.setCellValue("idT");
        cell = row.createCell(2);
        cell.setCellValue("time");
        try {
			File file = new File(filename);
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String date = "2019-07-29 07:00:00";
        long departure_time = DateTimeUtils.dateTime2Int(date);
		for(int i = 470; i < 480; i++){
			System.out.println(i);
			for(int j = 0; j < ids.size(); j++){
				if(i == j)
					continue;
				LatLng l1 = id2LatLng.get(ids.get(i));
				LatLng l2 = id2LatLng.get(ids.get(j));
				int t = G.getTravelTime(l1.lat, l1.lng, l2.lat, l2.lng, "driving", departure_time);
				if(t < 0){
					System.out.println("Getting traveltime from " + ids.get(i) + " to " + ids.get(j) + " failed");
					//continue;
				}
				try{
					FileInputStream fis = new FileInputStream(new File(filename));
					XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
					XSSFSheet mySheet = myWorkBook.getSheet("map");
					int rownum = mySheet.getLastRowNum() + 1;
					row = mySheet.createRow(rownum);
					cell = row.createCell(0);
					cell.setCellValue(ids.get(i));
					cell = row.createCell(1);
					cell.setCellValue(ids.get(j));
					cell = row.createCell(2);
					cell.setCellValue(t);
					FileOutputStream fos = new FileOutputStream(new File(filename));
					myWorkBook.write(fos);
					myWorkBook.close();
				}catch(Exception e){
					System.out.println(e);
				}
			}
		}
	}
	
	public static void main(String[] args){
		String dir = "E:/Project/cblsvr/VinschoolProject/data/";
		String fileName = "DSDiemTimesCityTaiNha.xlsx";
		String outFileName = "DSDiemTimesCityTaiNha-distance-470-480.xlsx";
		QueryDistance qr = new QueryDistance();
		qr.readDataFile(dir, fileName);
		qr.queryDistance(dir, outFileName);
		//qr.updateOutputFile(dir, outFileName);
		//qr.checkOutputFileComplete(dir, outFileName);
		System.out.println("Done!");
	}
}
