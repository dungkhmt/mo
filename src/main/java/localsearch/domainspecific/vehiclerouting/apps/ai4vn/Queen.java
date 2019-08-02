package localsearch.domainspecific.vehiclerouting.apps.ai4vn;

import localsearch.model.*;
import localsearch.constraints.alldifferent.AllDifferent;
import localsearch.functions.basic.*;
import localsearch.search.TabuSearch;
import localsearch.selectors.*;

import java.io.PrintWriter;
import java.util.*;


public class Queen {
	int n;
	LocalSearchManager ls;
	ConstraintSystem S;
	VarIntLS[] x;
	Random R;
	
	public Queen(int n){
		this.n = n;
		R = new Random();
	}
	public void stateModel(){
		LocalSearchManager ls=new LocalSearchManager();
		S = new ConstraintSystem(ls);
		x = new VarIntLS[n];
		for (int i = 0; i < n; i++){
			x[i] = new VarIntLS(ls, 0, n - 1);
		}
		
		S.post(new AllDifferent(x));
		
		IFunction[] f1=new IFunction[n];
		for (int i = 0; i < n; i++) 
			f1[i] =  new FuncPlus(x[i], i);
		S.post(new AllDifferent(f1));
		
		IFunction[] f2 = new IFunction[n];
		for (int i = 0; i < n; i++) 
			f2[i] = new FuncPlus(x[i], -i);
		S.post(new AllDifferent(f2));
		
		ls.close();
		
	}
	public void search(){
		System.out.println("n = " + n + ", init S = " + S.violations());
		int it = 0;
		MinMaxSelector mms = new MinMaxSelector(S);
		while(it < 100000 && S.violations() > 0){
			VarIntLS sel_x = mms.selectMostViolatingVariable();
			int sel_val = mms.selectMostPromissingValue(sel_x);
			
			sel_x.setValuePropagate(sel_val);
			System.out.println("Step " + it + ", S = " + S.violations());
		}
	}
	
	public void printHTML(){
		
		try{
			PrintWriter out = new PrintWriter("queen.html");
			out.println("<table border = 1>");
			for(int i = 0; i < n; i++){
				out.println("<tr>");
				for(int j = 0; j < n; j++)
					if(x[j].getValue() == i)
						out.println("<td width=20 height=20, bgcolor='red'></td>");
					else
						out.println("<td width=20 height=20, bgcolor='blue'></td>");
				out.println("</tr>");
			}
			out.println("</table>");
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public void tabuSearch(){
		TabuSearch ts = new TabuSearch();
		ts.search(S, 30, 10, 100000, 200);
		
	}
	public void printSolution(){
	}
	public static void main(String[] args) {
		Queen Q = new Queen(20);
		Q.stateModel();
		Q.search();
		//Q.tabuSearch();
		Q.printHTML();
	}

}
