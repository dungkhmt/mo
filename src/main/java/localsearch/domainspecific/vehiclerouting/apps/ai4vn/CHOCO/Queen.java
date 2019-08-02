package localsearch.domainspecific.vehiclerouting.apps.ai4vn.CHOCO;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;

public class Queen {

	/**
	 * @param args
	 */
	
	
	public void test1()
	{
	
		double t0 = System.currentTimeMillis();
		CPModel m = new CPModel();
		int n = 100;
		IntegerVariable[] q = new IntegerVariable[n];
		for (int i=0; i<n ; i++)
		{
			q[i]=Choco.makeIntVar("q"+ i,0,n-1);
		}
		for (int i=0; i< n-1; i++)
		{
			m.addConstraint(Choco.allDifferent(q));
		}
	}
	public void solveQueen(){
		double t0 = System.currentTimeMillis();
		CPModel m = new CPModel();
		
		int n = 500;
		IntegerVariable[] x = new IntegerVariable[n];
		for(int i = 0; i < n; i++)
			x[i] = Choco.makeIntVar("x" + i, 0,n-1);
		
		m.addConstraint(Choco.allDifferent(x));
		for(int i = 0; i < n-1; i++)
			for(int j = i+1; j < n; j++){
				m.addConstraint(Choco.neq(Choco.plus(x[i], i), 
						Choco.plus(x[j], j)));
				m.addConstraint(Choco.neq(Choco.minus(x[i], i), 
						Choco.minus(x[j], j)));
			}
		CPSolver s = new CPSolver();
		s.read(m);
		s.solve();

		for(int i = 0; i < n; i++)
			System.out.println("x[" + i + "] = " + s.getVar(x[i])
					.getVal());
		
		double t = System.currentTimeMillis() - t0;
		s.printRuntimeStatistics();
		System.out.println("time = " + t);
	}
	
	
	public static void test2(){
		CPModel m = new CPModel();
		IntegerVariable X1 = Choco.makeIntVar("X1", 1,10);
		IntegerVariable X2 = Choco.makeIntVar("X2", 1,10);
		IntegerVariable X3 = Choco.makeIntVar("X3", 1,10);
		IntegerVariable X4 = Choco.makeIntVar("X4", 1,10);
		
		m.addConstraint(Choco.eq(
				Choco.plus(X1, X4),
				Choco.plus(X2, X3)
				));
		
		m.addConstraint(Choco.leq(X1,X3));
		
		m.addConstraint(Choco.eq(
				Choco.plus(X1, 3),
				X2
				));

		m.addConstraint(Choco.neq(X4,X2));
		
		CPSolver s = new CPSolver();
		s.read(m);
		
		s.solve();
		
		System.out.println("X1 = " + s.getVar(X1).getVal());
		System.out.println("X2 = " + s.getVar(X2).getVal());
		System.out.println("X3 = " + s.getVar(X3).getVal());
		System.out.println("X4 = " + s.getVar(X4).getVal());
		
		
		
		
		
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Queen q = new Queen();
		q.solveQueen();
		
		//q.test2();
	}

}
