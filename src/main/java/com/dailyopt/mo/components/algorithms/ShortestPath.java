package com.dailyopt.mo.components.algorithms;

import java.util.ArrayList;
import java.util.HashSet;

import com.dailyopt.mo.components.maps.graphs.Arc;
import com.dailyopt.mo.components.maps.graphs.Graph;

public class ShortestPath {
	private int N;
	private HashSet<Arc>[] A;
	double[] d;
	int[] pred;
	boolean[] found;
	int s,t;
	int[] node;
	int[] idx;
	public ShortestPath(Graph g){
		N = g.getN();
		A = g.getA();
		d = new double[N+1];
		pred = new int[N+1];
		node = new int[N+1];
		idx = new int[N+1];
		found = new boolean[N+1];
	}
	void swap(int i, int j){
	    double tmp = d[i]; d[i] = d[j]; d[j] = tmp;
	    int tmp_i = node[i]; node[i] = node[j]; node[j] = tmp_i;
	    idx[node[i]] = i; idx[node[j]] = j;
	}
	void heapify(int i, int n){
	    int L = 2*i;
	    int R = 2*i+1;
	    int min = i;
	    if(L <= n && d[L] < d[i]) min = L;
	    if(R <= n && d[R] < d[min]) min = R;
	    if(min != i){
	        swap(i,min);
	        heapify(min,n);
	    }
	}
	void buildHeap(){
	    for(int i = N/2; i >= 1; i--)
	        heapify(i,N);
	}
	void upHeap(int i){
	    int p = i;
	    while(p/2 >= 1){
	        if(d[p] < d[p/2]){
	            swap(p,p/2);
	            p = p/2;
	        }else
	            break;
	    }
	}
	int selectMin(){
	    int sel_v = node[1];
	    swap(1,N);
	    N = N-1;
	    heapify(1,N);
	    return sel_v;
	}
	void init(){
	    for(int i = 1; i <= N; i++){
	        node[i] = i;
	        idx[i] = i;
	    }
	    for(int v = 1; v <= N; v++)
	        d[v] = 100000000;
	    d[idx[s]] = 0;
	    for(Arc a: A[s]){
	        int v = a.getEndPoint();
	        double w = a.getLength();
	        d[idx[v]] = w;
	        pred[v] = s;
	    }
	    buildHeap();
	}

	public int[] solve(int s, int t){
		this.s = s;
		this.t = t;
	    for(int v = 1; v <= N; v++)if(v != s)
	        found[v] = false;
	    found[s] = true;
	    init();
	    s = selectMin();
	    // LOOP
	    while(N > 0){
	        int v = selectMin();
	        found[v] = true;// fix node v
	        for(Arc a: A[v]){
	            int u = a.getEndPoint();
	            if(found[u] == false){// 
	                double w = a.getLength();
	                if(d[idx[u]] > d[idx[v]] + w){
	                    d[idx[u]] = d[idx[v]] + w;
	                    pred[u] = v;
	                    upHeap(idx[u]);
	                }
	            }
	        }
	    }
	    if(!found[t]) return null;
	    ArrayList<Integer> L = new ArrayList<Integer>();
	    int x = t;
	    while(x != s){
	        L.add(x);
	        x = pred[x];
	    }
	    L.add(s);
	    int[] r_path = new int[L.size()];
	    for(int i = 0; i < L.size(); i++){
	    	r_path[i] = L.get(L.size()-1-i);
	    }
	    return r_path;
	}
	
}
