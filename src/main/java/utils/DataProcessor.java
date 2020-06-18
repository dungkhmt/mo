package utils;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataProcessor {

	public static int convertStr2IntRemoveDelimiter(String s){
		try{
			s = s.replace(",", "");
			return Integer.valueOf(s);
		}catch(Exception ex){
			ex.printStackTrace();
			return 0;
		}
	}
	public static void extractOrderBrenntag(String fn, String fntrucks, String fno){
		try{
			PrintWriter out = new PrintWriter(fno);
			
			HashMap<String, String> mWarehouse2GeoPointId = new HashMap<String, String>();
			HashMap<String, String> mDistributor2GeoPointId = new HashMap<String, String>();
			HashSet<String> warehouses = new HashSet<String>();
			HashSet<String> distributors = new HashSet<String>();
			HashMap<String, Double> mGeoPoint2Lat = new HashMap<String, Double>();
			HashMap<String, Double> mGeoPoint2Lng = new HashMap<String, Double>();
			HashMap<String, List<Integer>> mOrder2Items = new HashMap<String, List<Integer>>();
			HashSet<String> orders = new HashSet<String>();
			
			//HashMap<String, String> mOrder2Warehouse = new HashMap<String, String>();
			//HashMap<String, String> mOrder2Distributor = new HashMap<String, String>();
			
			Scanner in = new Scanner(new FileInputStream(fn), "UTF-8");
			String line;// = in.nextLine();
			while(in.hasNextLine()){
				line = in.nextLine();
				String[] s= line.split("@");
				String orderId = s[0].trim();
				String weight = s[1].trim();
				if(mOrder2Items.get(orderId) == null) mOrder2Items.put(orderId, new ArrayList<Integer>());
				mOrder2Items.get(orderId).add(convertStr2IntRemoveDelimiter(weight));
				
				orders.add(orderId);
				
				String etd = s[2].trim();
				String eta = s[3].trim();
				String distributorCode = s[4].trim();
				String toGeoPointId = s[5].trim();
				String toAddr = s[6].trim();
				String s_to_lng = s[7].trim();
				String s_to_lat = s[8].trim();
				String warehouseCode = s[9].trim();
				String warehouseAddr = s[10].trim();
				String s_from_lng = s[11].trim();
				String s_from_lat = s[12].trim();
				System.out.println(orderId + "\t" + weight + "\t" + etd + "\t" + eta + "\t" + warehouseCode + 
						"\t" + s_from_lat + "\t" + s_from_lng + "\t" + distributorCode + "\t" + toGeoPointId + 
						"\t" + toAddr + "\t" + s_to_lat + "\t" + s_to_lng);
				
				warehouses.add(warehouseCode);
				distributors.add(distributorCode);
				mWarehouse2GeoPointId.put(warehouseCode, warehouseCode);
				mDistributor2GeoPointId.put(distributorCode, toGeoPointId);
				mGeoPoint2Lat.put(warehouseCode, Double.valueOf(s_from_lat));
				mGeoPoint2Lat.put(toGeoPointId, Double.valueOf(s_to_lat));
				mGeoPoint2Lng.put(warehouseCode, Double.valueOf(s_from_lng));
				mGeoPoint2Lng.put(toGeoPointId, Double.valueOf(s_to_lng));
				
			}

			in.close();
			
			// write to file
			for(String g: mGeoPoint2Lat.keySet()){
				out.println("<GeoPoint geoPointId = \"" + g + "\" latitude=\"" + mGeoPoint2Lat.get(g) + 
						"\" longitude=\"" + mGeoPoint2Lng.get(g) + "\"/>");
			}
			for(String wh: warehouses){
				out.println("<Client clientId = \"" + wh + "\" clientName=\"" + wh + "\" geoPointId=\"" + mWarehouse2GeoPointId.get(wh) + "\"/>");
			}
			for(String d: distributors){
				out.println("<Client clientId = \"" + d + "\" clientName=\"" + d + "\" geoPointId=\"" + mDistributor2GeoPointId.get(d) + "\"/>");
			}
			
			orders.clear();
			// re-read file
			in = new Scanner(new FileInputStream(fn), "UTF-8");
			
			while(in.hasNextLine()){
				line = in.nextLine();
				String[] s= line.split("@");
				String orderId = s[0].trim();
				
				String weight = s[1].trim();
				String etd = s[2].trim();
				String eta = s[3].trim();
				String distributorCode = s[4].trim();
				String toGeoPointId = s[5].trim();
				String toAddr = s[6].trim();
				String s_to_lng = s[7].trim();
				String s_to_lat = s[8].trim();
				String warehouseCode = s[9].trim();
				String warehouseAddr = s[10].trim();
				String s_from_lng = s[11].trim();
				String s_from_lat = s[12].trim();

				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
				Date parsedDate = dateFormat.parse(etd);
				Timestamp fromDatePickup = new Timestamp(parsedDate.getTime());

				parsedDate = dateFormat.parse(eta);
				Timestamp thruDatePickup = new Timestamp(parsedDate.getTime());
				
				if(orders.contains(orderId)) continue;
				int quantity = 0;
				for(int I: mOrder2Items.get(orderId)){
					quantity += I;
				}
				out.println("<PickupDeliveryOrder orderId=\"" + orderId + "\" pickupClientId=\"" + warehouseCode +
				"\" deliveryClientId=\"" + distributorCode + "\" quantity=\"" + quantity 
				+ "\" fromDatePickup=\"" + fromDatePickup + "\" thruDatePickup=\"" + thruDatePickup + 
				"\" fromDateDelivery=\"" + fromDatePickup + "\" thruDateDelivery=\"" + thruDatePickup + "\"/>");
				
				for(int I: mOrder2Items.get(orderId)){
					String orderItemId = "OI" + orderId + "S" + I;
					out.println("<PickupDeliveryOrderItem itemId = \"" + orderItemId + "\" orderId=\"" + orderId + 
							"\" weight=\"" + I + "\"/>");
								
				}
				
			}
			in.close();
			
			// read xe tai
			in = new Scanner(new FileInputStream(fntrucks), "UTF-8");
			while(in.hasNextLine()){
				line = in.nextLine();
				String[] s = line.split("&");
				String name = s[0].trim();
				String s_weight = s[1].trim();
				String s_lat = s[3].trim();
				String s_lng = s[2].trim();
				double lat = Double.valueOf(s_lat);
				double lng = Double.valueOf(s_lng);
				int weight = Integer.valueOf(s_weight);
				out.println("<Vehicle name=\"" + name + "\" description=\"" + name + "\" weight=\"" + weight + 
						"\" warehouseId = \"" + "00040" + "\" startWorkingTime=\"" + "2018-05-14 07:00:00" + "\" endWorkingTime=\"" + "2018-05-14 18:00:00" + "\"/>");
			}
			in.close();
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("abc");
		//DataProcessor.extractOrderBrenntag("C:/DungPQ/daily-opt/projects/smart-platform/don-hang.txt");
		DataProcessor.extractOrderBrenntag("C:/DungPQ/daily-opt/projects/smart-platform/text.txt",
				"C:/DungPQ/daily-opt/projects/smart-platform/xe-tai.txt",
				"C:/DungPQ/daily-opt/projects/smart-platform/data.xml");
	}

}
