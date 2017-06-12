package additive;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JTable;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class AdditivePricingInstance {
	private ProductDistribution productDist;
	private int m;
	private int n;
	private boolean iid;
	
	/*
	 * Constructor.
	 */
	AdditivePricingInstance(ArrayList<JTable> inputTables) {
		JTable t;
		n = inputTables.size();
		m = 0;
		iid = false;
		
		productDist = new ProductDistribution();
				
		for(int i=0;i<n;++i) {
			t = inputTables.get(i);
			productDist.addItem();
			
			if(m < t.getRowCount()) {
				m = t.getRowCount();				
			}
			
			for(int j=0;j<m;++j) {
				productDist.add(i,Double.parseDouble(t.getValueAt(j, 0).toString()), Double.parseDouble(t.getValueAt(j, 1).toString()));
			}
		}
	}
	
	/*
	 * IID Constructor.
	 */
	AdditivePricingInstance(JTable inputTable, int itemsNo) {
		
		iid = true;
		n = itemsNo;
		
		productDist = new ProductDistribution();
		productDist.addItem();
		productDist.setIID(itemsNo);
		
		
		m = inputTable.getRowCount();

		for(int j=0;j<m;++j) {
			productDist.add(0,Double.parseDouble(inputTable.getValueAt(j, 0).toString()), Double.parseDouble(inputTable.getValueAt(j, 1).toString()));
		}
	}
	
	/*
	 * Create a list with all valuation vectors.
	 */
	void enumValuations(ArrayList<String> valuations)
	{
		if(iid)
			productDist.enumSymetricValuationsStr(valuations);
		else
			productDist.enumValuations(valuations);
		
	}
	
	int getValIndex(int i, int k, int m)
	{
		return i*(m+1)+k;
	}
	
	int getPriceIndex(int i, int m)
	{
		return i*(m+1)+m;
	}
	
	
	/*
	 * Solve this instance and return the value of the objective function and the solution in a list.	
	 */
	private double solveIID(ArrayList<Double> sol, boolean integer) throws GRBException
	{	
		
		ArrayList<int[]> valuations = new ArrayList<int[]>();
		
		
		productDist.enumSymetricValuations(valuations);
		
		int valNo = valuations.size();
		int varNo = valNo*(m+1);
		
		double constraint[] = new double[varNo];
		double support[] = productDist.getIIDSupport();
		double prob[] = productDist.getIIDProb();
		
		GRBEnv    env   = new GRBEnv("mip1.log");
	    GRBModel  lp = new GRBModel(env);
	    
	    GRBVar x[] = new GRBVar[varNo];
		
	    GRBLinExpr obj = new GRBLinExpr();
		
		

	    
		for(int i=0; i< valNo; i++) {
			int val[] = valuations.get(i);
			double res = 1;
			int remain = n;
			
			for(int j =0; j<m; j++) {
				x[getValIndex(i, j, m)] = lp.addVar(0.0, 1, 0.0, GRB.CONTINUOUS,"x"+getValIndex(i, j, m));
			}
			
			for(int j = 0; remain > 0; j++) {
				res *= Math.pow(prob[j],val[j])*Combinatorics.choose(remain, val[j]);
				remain-=val[j];
			}
			
			x[getPriceIndex(i, m)] = lp.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS,"p"+getPriceIndex(i, m));
			
			
			obj.addTerm(res,x[getPriceIndex(i, m)]);
		}
	    
		lp.setObjective(obj,  GRB.MAXIMIZE);
		
		int q = 0;
		
		int val1[] = new int[m];
		int val2[] = new int[m];
		
		for(int i=0;i<valNo;i++) {
			GRBLinExpr expr = new GRBLinExpr();
			
			int val[] = valuations.get(i);
			for(int k=0;k<m;k++)
				expr.addTerm(val[k]*support[k], x[getValIndex(i, k, m)]);
			
			expr.addTerm(-1, x[getPriceIndex(i, m)]);
		
			lp.addConstr(expr, GRB.GREATER_EQUAL, 0, "c"+ q++);
			
			for(int j=0;j<valNo;j++) {
				if(j!=i) {
					
					int v[] = valuations.get(i);
					for(int k=0;k<m;k++)
						val1[k] = v[k];
							
					v = valuations.get(j);
					for(int k=0;k<m;k++)
						val2[k] = v[k];
					
					int a=0;
					int b=0;
					
					for(int k=0;k<m+1;k++)
						constraint[getValIndex(j, k, m)] = 0;
					
					while(a < m && b < m) {
						if(val1[a]<val2[b]) {
							constraint[getValIndex(j, b, m)] += -val1[a]*support[a];
							val2[b] -= val1[a];
							a++;
						} else if(val1[a]>val2[b]) {
							constraint[getValIndex(j, b, m)] += -val2[b]*support[a];
							val1[a] -= val2[b];
							b++;
						} else {
							constraint[getValIndex(j, b, m)] += -val1[a]*support[a];
							a++;
							b++;
						}
					}
					
					/*
					for(int k=0;k<m;k++) {
						for(int l=0;l<m;l++) {
							constraint[getValIndex(j, k, m)] = -val[k]*support[k];
						}
					}
					*/
					constraint[getPriceIndex(j, m)]=1;
					
					GRBLinExpr expr2 = new GRBLinExpr(expr);
					
					for(int l = 0; l<m; l++)
						expr2.addTerm(constraint[getValIndex(j, l, m)], x[getValIndex(j, l, m)]);
					expr2.addTerm(constraint[getPriceIndex(j, m)], x[getPriceIndex(j, m)]);
					
					lp.addConstr(expr2, GRB.GREATER_EQUAL, 0, "c"+ q++);
					
	/*				for(int k=0;k<varNo;k++)
						System.out.print(constraint[k]+" ");
					System.out.println();
					*/
					
					for(int k=0;k<m+1;k++)
						constraint[getValIndex(j, k, m)] = 0;
					constraint[getPriceIndex(j, m)] = 0;
				}
			}
			
			for(int k=0;k<m;k++)
				constraint[getValIndex(i, k, m)] = 0;
			constraint[getPriceIndex(i, m)] = 0;
		}
		
		
	/*	for(int i=0;i<valNo;i++) {
			for(int k=0;k<m;k++) {
				constraint[getValIndex(i, k, m)] = 1;
				lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraint, 0, "c"+ q++));
				lp.addConstraint(new LinearSmallerThanEqualsConstraint(constraint, 1, "c"+ q++));
				constraint[getValIndex(i, k, m)] = 0;
			}
		}
	*/
		//lp.setMinProblem(false);
		
		lp.optimize();
		
		
		
		double solution[] = new double[varNo];
		
		for(int i=0; i< varNo; i++)
			solution[i] = x[i].get(GRB.DoubleAttr.X);
		
		Iterator<int[]> it = valuations.iterator();
		
		int j=0;
		
		while(it.hasNext()) {
			int a[] = it.next();
			for(int i = 0; i<m; i++) {
				//System.out.print(a[i]+": ");
			}
			
			for(int i = 0; i<m; i++) {
				//System.out.print(" " + solution[getValIndex(j, i, m)]);
				sol.add(solution[getValIndex(j, i, m)]);
			}
			
			//System.out.println(" " + solution[getPriceIndex(j, m)]);
			
			sol.add(solution[getPriceIndex(j, m)]);
			
			//System.out.println(" " + objective[getPriceIndex(j, m)]);
			j++;
		}
		
		
		
		return lp.get(GRB.DoubleAttr.ObjVal);
	}
	
	/*
	 * Solve this instance and return the value of the objective function and the solution in a list.	
	 */
	double solve(ArrayList<Double> sol, boolean integer) throws GRBException
	{	
		if(iid) {
			return solveIID(sol,integer);
		}
		
		int varNo = (int) (Math.pow(m, n)*(n+1));
		
		GRBVar x[] = new GRBVar[varNo];
		
		GRBEnv env   = new GRBEnv("mip1.log");
	    GRBModel  lp = new GRBModel(env);
	    
		for(int i=0; i< varNo; i++) {
			if((i+1)%(n+1) != 0) {
				x[i] = lp.addVar(0.0, 1, 0.0, GRB.CONTINUOUS,"x"+i);
			} else {
				x[i] = lp.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS,"p"+i);
			}
		}
		
		GRBLinExpr objective = new GRBLinExpr();	
		
		productDist.calculateObjective(objective, x);
		
		int i=0;
		
		lp.setObjective(objective,GRB.MAXIMIZE);
		
		ArrayList<ArrayList<Double>> constraints = new ArrayList<ArrayList<Double>>();
		
		ArrayList<GRBLinExpr> constr = new ArrayList<GRBLinExpr>();
		
		productDist.calcRationalityConstraints(constr, integer,x);
		productDist.calcTruthfulnessConstraints(constr, integer, x);
		
		Iterator<GRBLinExpr> it1 = constr.iterator();
		
		int j=0;
		
		while (it1.hasNext()) {
			GRBLinExpr expr = it1.next();
			
			lp.addConstr(expr, GRB.LESS_EQUAL, 0, "c"+ ++j);
		}
		
		lp.optimize();
		
		double solution[] = new double[varNo];
		
		for(i=0; i< varNo; i++)
			solution[i] = x[i].get(GRB.DoubleAttr.X);
		
		int mul = Arithmetics.getShiftMultiplier();
		
		for(i=0; i<solution.length; ++i) {
			if(integer && (i+1)%(n+1) != 0) {
				sol.add(solution[i]*mul);
			} else {
				sol.add(solution[i]);
			}
		}
		
		return lp.get(GRB.DoubleAttr.ObjVal);
	}
}
