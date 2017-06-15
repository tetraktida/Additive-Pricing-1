package additive;

import java.util.ArrayList;
import java.util.Comparator;

import gurobi.GRBLinExpr;
import gurobi.GRBVar;

class SupportElement {
	private double v;
	private double q;
	
	/*
	 * Constructor.
	 */
	SupportElement(double val, double prob)
	{
		v=val;
		q=prob;
	}
	
	/*
	 * Get value.
	 */
	double getVal()
	{
		return v;
	}
	
	/*
	 * Get probability.
	 */
	double getProb()
	{
		return q;
	}
}

class Distribution {
	private ArrayList<SupportElement> support;
		
	/*
	 * Constructor.
	 */
	Distribution()
	{
		support = new ArrayList<SupportElement>();
	}
	
	/*
	 * Add an element to the support of the distribution.
	 */
	void add(double v, double q)
	{
		SupportElement e = new SupportElement(v, q);
		
		Comparator<SupportElement> cmp=new Comparator<SupportElement>() {

			    @Override
			    public int compare(SupportElement o1, SupportElement o2) {
			        // TODO Auto-generated method stub
			        if(o1.getVal()<o2.getVal()) {
			        	return -1;
			        	}
			        if(o1.getVal()==o2.getVal()){
			        	return 0;
			        }
			        return 1;
			    }
		  };
		
		support.add(e);
		
		support.sort(cmp);
	}
	
	/*
	 * Get the size of the support.
	 */
	int getSupportSize()
	{
		return support.size();
	}
	
	/*
	 * Get the i-th value in the support.
	 */
	double getVal(int i)
	{
		if(i<support.size()) {
			return support.get(i).getVal();
		}
		
		return -1;
	}
	
	/*
	 * Get the probability of the i-th value in the support.
	 */
	double getProb(int i)
	{
		if(i<support.size()) {
			return support.get(i).getProb();
		}
		
		return -1;
	}
}

public class ProductDistribution {
	private ArrayList<Distribution> distrs;
	
	private int n;
	private int m;
	private boolean iid; 
	
	/*
	 * Constructor.
	 */
	public ProductDistribution()
	{		
		distrs = new ArrayList<Distribution>();
		n = 0;
	}
	
	public void setIID(int itemsNo)
	{
		iid = true;
		n = itemsNo;
	}
	
	public double[] getIIDSupport()
	{
		double support[] = new double[m];
		
		for(int i=0; i<m; i++)
			support[i] = distrs.get(0).getVal(i);
		
		return support;
	}
	
	public double[] getIIDProb()
	{
		double prob[] = new double[m];
		
		for(int i=0; i<m; i++)
			prob[i] = distrs.get(0).getProb(i);
		
		return prob;
	}
	
	/*
	 * Add a new item with empty support to the distribution.
	 */
	void addItem()
	{
		distrs.add(new Distribution());
		n++;
	}
	
	/*
	 * Add a new element in the support of item i.
	 */
	void add(int i, double v, double q)
	{
		if(i<distrs.size()) {
			Distribution d = distrs.get(i);
			d.add(v, q);
			if(d.getSupportSize()>m)
				m++;
		}
	}
	
	/*
	Recursively enumerate the probabilities of all possible valuations and return
	them as a coefficient-vector of the objective function. 
	*/
	
	private void calcObjRec(GRBLinExpr obj, int i, Double res, GRBVar x[], int k[])
	{
		if (i<n) {
			Distribution d = distrs.get(i);

			for(int j=0;j<m;++j)
				calcObjRec(obj,i+1,res*d.getProb(j), x, k);
		} else {
			obj.addTerm(res, x[k[0]]);
			k[0] += (n+1);
		}
	}
	
	
	/*
	Return the coefficient-vector of the objective function.
	*/
	void calculateObjective(GRBLinExpr obj, GRBVar x[])
	{
		int k[] = new int[1];
		
		k[0] = n;
		
		calcObjRec(obj,0,1.0, x,k);
	}
	
	
	/*
	Return a constraint as a vector. There are two possible behaviors:

	1. cur1 >= 0 and cur2 >= 0: A truthful constraint saying that for the
	valuation # cur1 it is better to buy the lottery assigned to it that buying the
	lottery for valuation # cur2. In that case, acc[] is an n-dimensional array
	containing valuation # cur1.

	2. cur1 >= 0 and cur2 < 0: A rationality constraint saying that the lottery
	assigned to valuation # cur1 has non-negative payoff. In that case, acc[] is an
	n-dimensional array containing valuation # cur1.
	*/
	
	private void calculateConstraint(GRBLinExpr constr, int cur1, int cur2, double acc[], GRBVar x[])
	{		
		if(cur1 == cur2)
			return;
		
		if(cur1>=0) {
				for(int j=0;j<n;++j)
					constr.addTerm(-acc[j],x[cur1*(n+1)+j]);
				constr.addTerm(1.0,x[cur1*(n+1)+n]);
		}
		
		if(cur2>=0) {
				for(int j=0;j<n;++j)
					constr.addTerm(acc[j],x[cur2*(n+1)+j]);

				constr.addTerm(-1.0,x[cur2*(n+1)+n]);
		}
	}
	
	/*
	Recursively enumerate the rationality constraints for every possible valuation
	and return them as a coefficient-vector. 
	*/
	private void ratRec(ArrayList<GRBLinExpr> constraints, int cur[], int i, double acc[], boolean integer, GRBVar x[])
	{				
		if(i<n) {
			Distribution d = distrs.get(i);
			for(int j=0;j<m;++j) {
				acc[i]=d.getVal(j);
				ratRec(constraints,cur,i+1,acc, integer, x);
			}
		} else {
			
			GRBLinExpr constr = new GRBLinExpr();
			
			calculateConstraint(constr,cur[0],-1,acc, x);
			
			constraints.add(constr);
				

			++cur[0];
		}	
	}
	
	/*
	Return the coefficient-vectors of all rationality constraints.
	*/
	void calcRationalityConstraints(ArrayList<GRBLinExpr> constraints, boolean integer, GRBVar x[])
	{
		double acc[] = new double[n];
		int cur[]={0};
	
		ratRec(constraints, cur, 0, acc, integer, x);
	}
	
	
	/*
	Recursively enumerate the truthfulness constraints for every possible pair of
	valuation s and return them as a coefficient-vector.
	*/

	private void truthRec(ArrayList<GRBLinExpr> constraints, int cur[], int i, double acc[], boolean integer, GRBVar x[])
	{	
		int size = (int)Math.pow(m,n);
		
		if(i<n) {
			Distribution d = distrs.get(i);
			for(int j=0;j<m;++j)
			{
				acc[i]=d.getVal(j);
				truthRec(constraints,cur,i+1,acc,integer,x);
			}
		} else {
			for(int j=0; j < size; ++j){
				GRBLinExpr constr = new GRBLinExpr();
				calculateConstraint(constr, cur[0],j,acc,x);
				
				if(j != cur[0])
					constraints.add(constr);				
			}
			++cur[0];
		}	
	}

	/*
	Enumerate the coefficient-vectors of all truthfulness constraints.
	*/
	
	void calcTruthfulnessConstraints(ArrayList<GRBLinExpr> constraints, boolean integer, GRBVar x[])
	{
		double acc[]= new double[n];
		int current[] = {0};
		
		
		truthRec(constraints, current, 0, acc, integer,x);
	}
	
	/*
	Recursively enumerate all valuation vectors. 
	*/
	private void enumValRec(ArrayList<String> valuations, int i, double acc[])
	{				
		if(i<n) {
			Distribution d = distrs.get(i);
			
			for(int j=0;j<m;++j) {
				acc[i]=d.getVal(j);
				enumValRec(valuations,i+1,acc);
			}
		} else {
			String s = new String("(");
			for(int j=0; j<n; j++) {
	    		s+=acc[j];
	    		if(j<n-1)
	    			s+=", ";
	    	}
	    	s+=")";
	    	
	    	valuations.add(s);
		}	
	}
	
	/*
	Create a list with all valuation vectors. 
	*/
	void enumValuations(ArrayList<String> valuations){
		double acc[] = new double[n];	
		
		enumValRec(valuations, 0, acc);
	}
	
	private void enumSymValRec(ArrayList<int []> valuations, int i, int acc[], int rem)
	{		
		if(i<m-1) {
			
			for(int j=0;j<=rem;++j) {
				acc[i]=j;
				enumSymValRec(valuations,i+1,acc, rem-j);
			}
		} else {
			
			acc[i]=rem;
			
			int d[] = new int[m];
			
			for(int j=0;j<m;++j)
				d[j] = acc[j];
			
			valuations.add(d);
		}	
	}
	
	private void enumSymValRecStr(ArrayList<String> valuations, int i, int acc[], int rem)
	{		
		if(i<m-1) {
			
			for(int j=0;j<=rem;++j) {
				acc[i]=j;
				enumSymValRecStr(valuations,i+1,acc, rem-j);
			}
		} else {
			
			String s = new String();
			
			acc[i]=rem;
			
			s = s + "(";
			
			for(int j=0;j<m-1;++j)
				s+= Arithmetics.sanitizeDouble(distrs.get(0).getVal(j))+": "+acc[j] + ", ";
			
			s+= Arithmetics.sanitizeDouble(distrs.get(0).getVal(m-1))+": "+acc[m-1] + ")";
			
			valuations.add(s);
		}	
	}
	
	void enumSymetricValuationsStr(ArrayList<String> valuations){
		int acc[] = new int[m];
		
		enumSymValRecStr(valuations, 0, acc, n);
	}
	
	void enumSymetricValuations(ArrayList<int[]> valuations){
		int acc[] = new int[m];
		
		enumSymValRec(valuations, 0, acc, n);
	}
	
}
