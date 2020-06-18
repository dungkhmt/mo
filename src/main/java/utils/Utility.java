package utils;


public class Utility {
	public static final double EPS = 0.000001;
	public static double getInducedValue(int x1, double y1, int x2, double y2, int x){
		if(x1 > x2){
			int tmp = x1; x1 = x2; x2 = tmp;
			double tmpf = y1; y1 = y2; y2 = tmpf;
		}
		if(x <= x1) return y1;
		if(x >= x2) return y2;
		return y1 + (y2-y1)*(x-x1)/(x2-x1);
	}
	
	
	public static void main(String[] args){
		//System.out.println(distance("2015-12-22","2015-12-21"));
	}
}
