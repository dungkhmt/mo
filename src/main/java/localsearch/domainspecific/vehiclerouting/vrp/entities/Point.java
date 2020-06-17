
package localsearch.domainspecific.vehiclerouting.vrp.entities;

import java.text.DecimalFormat;
import java.util.ArrayList;

/*
 * Authors: Nguyen Thanh Hoang (thnbk56@gmail.com)
 * date: 30/08/2015
 */
public class Point {
    public int ID;
    double x, y;
    String name;
    int type;//0: school, 1: public, 2: home
    int varIndex;

    public Point(int ID, double x, double y, String name, int type, int varIndex){
        this.ID = ID;
        this.x = x; this.y = y;
        this.name = name;
        this.type = type;
        this.varIndex = varIndex;
    }
    public Point(int ID, double x, double y, int type, int varIndex){
        this.ID = ID;
        this.x = x; this.y = y;
        this.type = type;
        this.varIndex = varIndex;
    }
    public Point(int ID, int x, int y){
        this.ID = ID;
        this.x = x;
        this.y = y;
    }
    public Point(){
        this.ID = 0;this.x = 0;this.y = 0;;
    }
    public Point(int ID){
        this.ID = ID; this.x = 0; this.y = 0;
    }
    public Point(int ID, int type){
        this.ID = ID; this.type = type;
    }
    public void setID(int iD) {
        ID = iD;
    }
    public int getID() {
        return ID;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getType(){
        return type;
    }
//    public ArrayList<Integer> getBucketIDs(){
//    	return bucketIDs;
//    }
//
//    public void setBucketIDs(ArrayList<Integer> bkIDs){
//    	this.bucketIDs = bkIDs;
//    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setType(int type){
        this.type = type;
    }

    public int getVarIndex() {
        return varIndex;
    }
    public void setVarIndex(int varIndex) {
        this.varIndex = varIndex;
    }
    double degrees(Point p) {
        double X = p.x - x;
        double Y = p.y - y;
        double d = Math.toDegrees(Math.atan2(Y, X));
        if (d < 0) d += 360;
        return d;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        //this.bucketIDs = new ArrayList<Integer>();
    }

    public Point(int ID, double x, double y) {
        // TODO Auto-generated constructor stub
        this.ID = ID;
        this.x = x;
        this.y = y;
    }
    public double distance(Point p) {
        return Math.sqrt((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y));
    }
    public double mahattanDistance(Point p){
        Point p1 = new Point(x,p.y);
        return distance(p1) + p1.distance(p);
    }
    public Point clone(){
        return new Point(ID,x,y, type, varIndex);
    }
    public String toString(){
        DecimalFormat df = new DecimalFormat("#.00");
        return ID + " (" + df.format(x) + "," + df.format(y) + ")";
    }
    public static void main(String[] argn) {
        System.out.print(Math.toDegrees(Math.atan2(-0.5, 1)));
    }
}
