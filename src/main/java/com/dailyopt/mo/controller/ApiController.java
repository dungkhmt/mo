package com.dailyopt.mo.controller;

import java.util.List;

import com.dailyopt.mo.components.maps.GISMap;
import com.dailyopt.mo.components.maps.Path;
import com.dailyopt.mo.components.maps.Point;
import com.dailyopt.mo.components.movingobjects.MovingObject;
import com.dailyopt.mo.components.objectmanager.ObjectManager;
import com.dailyopt.mo.model.AddModel;
import com.dailyopt.mo.model.modelFindPath.ShortestPathInput;
import com.google.gson.Gson;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class ApiController {
	private static Integer count = 0; 
    private static ObjectManager mgr = new ObjectManager();
    private static GISMap gismap = new GISMap();
	
	@PostMapping("/add")
    public Integer home(@RequestBody AddModel addModel) {
    	synchronized(count){
    		count = count + 1;
    		System.out.println(count);
    	}
        return addModel.getA()+addModel.getB();
    }
    
	public String name(){
		return "ApiController";
	}
	@PostMapping("/getObjects")
	public String getObjects(){
		List<MovingObject> l_objects = mgr.getObjects();
		Gson gson = new Gson();
		MovingObject[] objects = new MovingObject[l_objects.size()];
		for(int i = 0; i < l_objects.size(); i++)
			objects[i] = l_objects.get(i);
		String json = gson.toJson(objects);
		return json;
	}
	@PostMapping("/createObject")
    public String createObject(@RequestBody MovingObject obj) {
    	synchronized(mgr){
    		count = count + 1;
    		MovingObject c_obj = mgr.addObject(obj);
    		System.out.println(name() + "::createObject " + obj.getId() + ", count = " + count);
    		return "{\"id\":\"" + c_obj.getId() + "\",\"lat\":" + c_obj.getLat() + ",\"lng\":" + c_obj.getLng() + "}";
    	}
        //return "{}";
    }
    
	@PostMapping("/updateLocation")
    public String updateLocation(@RequestBody MovingObject obj) {
    	synchronized(mgr){
    		count = count + 1;
    		System.out.println(name() + "::updateLocation, object " + obj.getId() + ", count = " + count);
    		MovingObject n_obj = mgr.updateLocation(obj.getId(), obj.getLat(), obj.getLng());
    		return "{\"id\":\"" + n_obj.getId() + "\",\"lat\":" + n_obj.getLat() + ",\"lng\":" + n_obj.getLng() + "}";
    	}
        //return "{}";
    }
    
	@PostMapping("/findShortestPath")
    public String findShortestPath(@RequestBody ShortestPathInput input) {
		Path path = gismap.findPath(input.getFromID(), input.getToID());
		if(path == null){
			System.out.println(name() + "::findShortestPath, findPath(" + input.getFromID() + "," + input.getToID() + "), NOT FOUND");;
			return "{}";
		}
		Gson gson = new Gson();
		String json = gson.toJson(path);
		return json;
	}
		
}
