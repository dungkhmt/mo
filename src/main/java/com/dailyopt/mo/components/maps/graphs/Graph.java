package com.dailyopt.mo.components.maps.graphs;

import java.util.HashSet;

public class Graph {
	private int n;
	private HashSet<Arc>[] A;// A[v] is the set of outgoing arcs from node v
	public Graph(int n, HashSet<Arc>[] a) {
		super();
		this.n = n;
		A = a;
	}
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}
	public HashSet<Arc>[] getA() {
		return A;
	}
	public void setA(HashSet<Arc>[] a) {
		A = a;
	}
	
	
}
