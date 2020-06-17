package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

import weka.core.Debug.Random;

import java.util.ArrayList;

public class Utils {
	public static int CAP_FLEXIBILITY = 0;
	public static int CAP_LEQ_16 = 1;
	public static int CAP_LEQ_29 = 2;
	
	public static int CAP_45 = 43;
	public static int CAP_29 = 27;
	public static int CAP_16 = 14;
	
	public static int ADD_ONE_POINT 	= 0;
	public static int REMOVE_ONE_POINT 	= 1;
	public static int ONE_POINT_MOVE 	= 2;
	public static int TWO_POINT_MOVE 	= 3;

	public static int LOG_SUCCESS 				= 0;
	public static int LOG_TIME_VIOLATION 		= 1;
	public static int LOG_CAP_VIOLATION 		= 2;
	public static int LOG_TIME_CAP_VIOLATION 	= 3;
	
	public static int NO_UPDATE = 0;
	public static int UPDATED = 1;
	public static int REMOVED = 2;
	
	int n;
	int[] bool;
	int[] A;
	public ArrayList<ArrayList<Integer>> permutations;
	
	int nbP;
	int[][] C;
	int[] X;
	int[] T;
	int[] BestWay;
	boolean[] free;
	int minSpending;
	 
	void savePermutation() {
		ArrayList<Integer> per = new ArrayList<Integer>();
	    for (int i = 1; i <= n; i++)
	        per.add(A[i]);
	    permutations.add(per);
	}
	 
	void Try(int k) {
	    for (int i = 1; i <= n; i++) {
	        if(bool[i] == 0) {
	            A[k] = i;
	            bool[i] = 1;
	            if(k == n)
	                savePermutation();
	            else
	                Try(k + 1);
	            bool[i] = 0;
	        }
	    }
	}
	
	public ArrayList<ArrayList<Integer>> getPermutations(int n){
		this.n = n;
		permutations = new ArrayList<ArrayList<Integer>>();
		A = new int[n+1];
		bool = new int[n+1];
		for(int i = 0; i <= n; i++){
			A[i] = 0;
			bool[i] = 0;
		}
		Try(1);
		return permutations;
	}
	
	public void init(int max_travel){
		X = new int[nbP+1];
		T = new int[nbP+1];
		BestWay = new int[nbP+1];
		free = new boolean[nbP+1];
	    for (int i = 0; i <= nbP; i++ )
	        free[i] = true;
	    free[1] = false;
	    X[1] = 1;
	    T[1] = 0;
	    minSpending = max_travel*nbP;
	}
	
	public void findReverses(int i){
	    for(int j = 2; j <= nbP; j++ ){
	        if(free[j]){
	            X[i] = j;
	            T[i] = T[i-1] + C[X[i-1]][j]; 
	            if ( T[i] < minSpending ){
	                free[j] = false;
	                if(i == nbP){
	                	//System.out.println(T[i]);
	                    if(T[nbP] < minSpending ){
	                        for (int k = 0; k <= nbP; k++) 
	                            BestWay[k] = X[k];
	                        minSpending = T[nbP];
	                    }
	                }
	                else 
	                	findReverses(i + 1);
	                free[j] = true;
	            }
	        }
	    }
	}
	
	public void findOpt(int i){
	    for(int j = 2; j <= nbP; j++ ){
	        if(free[j]){
	            X[i] = j;
	            T[i] = T[i-1] + C[X[i-1]][j]; 
	            if ( T[i] < minSpending ){
	                free[j] = false;
	                if(i == nbP){
	                	//System.out.println(T[i]);
	                	int minusCost = C[X[1]][X[2]];
	                	int plusCost = C[X[i-1]][X[i]];
	                    if(T[nbP] - minusCost + plusCost < minSpending ){
	                        for (int k = 0; k <= nbP; k++) 
	                            BestWay[k] = X[k];
	                        minSpending = T[nbP] - minusCost + plusCost;
	                    }
	                }
	                else 
	                	findOpt(i + 1);
	                free[j] = true;
	            }
	        }
	    }
	}
	
	public int[] getShortestPathOnRouteReverses(int nb, int maxTravel, int[][] cost){
		this.C = cost;
		this.nbP = nb;
		init(maxTravel);
		findReverses(2);
		return BestWay;
	}
	
	public int[] getShortestPathOnRoute(int nb, int maxTravel, int[][] cost){
		this.C = cost;
		this.nbP = nb;
		init(maxTravel);
		findOpt(2);
		return BestWay;
	}
	
	public Utils() {
		// TODO Auto-generated constructor stub
		
	}
	
	public static void main(String[] args){
		Utils u = new Utils();
		int k = 8;
		int maxTravel = 21;
		int[][] c = new int[k+1][k+1];
		Random r = new Random();
		for(int i = 1; i <= k; i++){
			for(int j = 1; j <= k; j++){
				if(i == j)
					c[i][j] = 0;
				else{
					int d = r.nextInt(20) + 1;
					c[i][j] = d;
				}
				System.out.print(c[i][j] + " ");
			}
			System.out.println();
			
		}
		int[] best = new int[k+1];
		best = u.getShortestPathOnRoute(k, maxTravel, c);
		for(int i = 0; i <=k ; i++)
			System.out.print(best[i] + " -> ");
		System.out.println("best cost = " + u.minSpending);
	}

}
