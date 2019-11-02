package com.socolabs.mo.components.maps.utils;

import java.io.File;
import java.util.Scanner;

public class OSMProcessor {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			Scanner in = new Scanner(new File("D:/research/data-open-street-map/vietnam-latest.osm"));
			int cnt = 0;
			while(in.hasNext()){
				String line = in.nextLine();
				cnt++;
				if(cnt < 24000000){
					if(cnt%10000 == 0) System.out.println(cnt + ": " + line);
				}else{
					if(line.contains("<way") || line.contains("</way"))
						System.out.println(cnt + ": " + line);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
