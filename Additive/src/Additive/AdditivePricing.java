/**
 * 
 */
package Additive;


import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;


/**
 * @author dimitris
 *
 */

class Arithmetic {
	private static int mult = 0;
	private static String pad;
	private static int shift = 5;
	private static int prec = 100000;
	
	static int getShiftMultiplier()
	{
		if(mult == 0) {
			mult = 1;
			for(int i=0; i<shift; ++i) {
				mult *= 10;
			}
		}
		return mult;
	}
	
	static void setShift(int k)
	{
		if(k >= 0) {
			shift = k;
			mult = 0;
			pad = null;
		}
	}
	
	static void setPrecision(int p)
	{
		if(p >= 0) {
			prec = 1;
			for(int i=0; i<p; ++i) {
				prec *= 10;
			}
		}
	}
	
	static String getPadding()
	{
		if(pad == null) {
			pad = "";
			for(int i=0; i<shift; ++i) {
				pad = "0" + pad;
			}
		}
		return pad;
	}
	
	/*
	 * Remove trailing decimal 0s and fix infinite number of decimal 9s problem.
	 */
	static String sanitizeDouble(double num, boolean div)
	{
		
		num = Math.floor(Math.abs(num)*prec)/prec;
		
		String number = Double.toString(num);
		
		String split[] = number.split("E");
		
		if(split.length>1) {
			int move = Integer.parseInt(split[1]);
			
			number = split[0];
			for(int i=0; i<move; i++)
				number = number + "0";
			
			number = number.replaceFirst("[.]", "");
			
			number = number.substring(0, move+1) + "." + number.substring(move+1, number.length()); 
		}
		
		int n;
		
		if(div) {
			number = Arithmetic.getPadding() + number;
			n = number.length();
			
			int pos = number.indexOf('.');
			
			number = number.substring(0, pos - shift)
					+ "."
					+ number.substring(pos - shift,pos)
					+ number.substring(pos+1, n);
						
			number = number.replaceFirst("0*", "");
			
			if(number.charAt(0) == '.')
				number = '0' + number;
		}
		
		
		number = number + "A";
		
		number = number.replaceFirst("0+A", "B");
		
		if(number.contains("B")) {
			return number.replaceFirst("[.]B", "").replace("B", "");
		}
		
		number = number.replaceFirst("[.]A", "");
		number = number.replaceFirst("99+A", "");
		
		
		if(!number.contains("A") && number.contains(".")) {
			if(number.charAt(number.length()-1) != '.') {
				number = number.substring(0,number.length()-1) + ((char)(1+number.charAt(number.length()-1)));
			}
			else {
				number = "A" + number;
				String s[] = number.split("9*[.]");
				
				if(s[0].length() == 1) {
					number = "1" + number.replaceAll("9", "0"); 
				} else {
					number =  number.substring(0,s[0].length()-1)
							+ ((char)(1+number.charAt(s[0].length()-1)))
							+ number.substring(s[0].length(), number.length()).replaceAll("9", "0").replace('.','\0');
				}
					
			}
		}

		return number.replaceFirst("A", "").replace(" ","");
	}
}

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
				
		support.add(e);
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

class ProductDistribution {
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
	them as a coefficient-vector of the objective function (negative because we
	solve a minimization LPs). 
	*/
	
	private void calcObjRec(ArrayList<Double> obj, int i, Double res)
	{
		Double zero=0.0;
		
		if (i<n) {
			Distribution d = distrs.get(i);

			for(int j=0;j<m;++j)
				calcObjRec(obj,i+1,res*d.getProb(j));
		} else {
			for(int j=0;j<n;++j)
				obj.add(zero);

			obj.add(-res);	
		}
	}
	
	/*
	Return the coefficient-vector of the objective function (negative because Matlab
	solves minimization LPs).
	*/
	void calculateObjective(ArrayList<Double> obj)
	{
		calcObjRec(obj,0,1.0);
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
	private void calculateConstraint(ArrayList<Double> constr, int cur1, int cur2, double acc[])
	{		
		Double zero=0.0;
		
		int size = (int)Math.pow(m,n);
		
		if(cur1 == cur2)
			return;
		
		for(int i=0;i<size;++i) {
			if(i!=cur1 && i!=cur2) {
				for(int j=0;j<=n;++j)
					constr.add(zero);
			} else if(i==cur1) {
				for(int j=0;j<n;++j)
					constr.add(-acc[j]);
				constr.add(1.0);
			} else {
				for(int j=0;j<n;++j)
					constr.add(acc[j]);

				constr.add(-1.0);
			}
		}
	}
	
	/*
	Recursively enumerate the rationality constraints for every possible valuation
	and return them as a coefficient-vector. 
	*/
	private void ratRec(ArrayList<ArrayList<Double>> constraints, int cur[], int i, double acc[], boolean integer)
	{				
		if(i<n) {
			Distribution d = distrs.get(i);
			for(int j=0;j<m;++j) {
				if(integer) {
					acc[i]=d.getVal(j)*Arithmetic.getShiftMultiplier();
				} else {
					acc[i]=d.getVal(j);
				}
				ratRec(constraints,cur,i+1,acc, integer);
			}
		} else {
			
			ArrayList<Double> constr = new ArrayList<Double>();
			
			calculateConstraint(constr,cur[0],-1,acc);
			
			if(constr.isEmpty()==false)
				constraints.add(constr);

			++cur[0];
		}	
	}

	/*
	Return the coefficient-vectors of all rationality constraints.
	*/
	void calcRationalityConstraints(ArrayList<ArrayList<Double>> constraints, boolean integer)
	{
		double acc[] = new double[n];
		int cur[]={0};
				
		ratRec(constraints, cur, 0, acc, integer);
	}
	
	/*
	Recursively enumerate the truthfulness constraints for every possible pair of
	valuation s and return them as a coefficient-vector.
	*/
	private void truthRec(ArrayList<ArrayList<Double>> constraints, int cur[], int i, double acc[], boolean integer)
	{	
		int size = (int)Math.pow(m,n);
		
		if(i<n) {
			Distribution d = distrs.get(i);
			for(int j=0;j<m;++j)
			{
				if(integer) {
					acc[i]=d.getVal(j)*Arithmetic.getShiftMultiplier();
				} else {
					acc[i]=d.getVal(j);
				}
				truthRec(constraints,cur,i+1,acc,integer);
			}
		} else {
			ArrayList<Double> constr;
			for(int j=0; j < size; ++j){
				constr = new ArrayList<Double>();
				calculateConstraint(constr, cur[0],j,acc);
				
				if(constr.isEmpty()==false)
					constraints.add(constr);				
			}
			++cur[0];
		}	
	}

	/*
	Enumerate the coefficient-vectors of all truthfulness constraints.
	*/
	void calcTruthfulnessConstraints(ArrayList<ArrayList<Double>> constraints, boolean integer)
	{
		double acc[]= new double[n];
		int current[] = {0};
		
		
		truthRec(constraints, current, 0, acc, integer);
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
				s+= Arithmetic.sanitizeDouble(distrs.get(0).getVal(j),false)+": "+acc[j] + ", ";
			
			s+= Arithmetic.sanitizeDouble(distrs.get(0).getVal(m-1),false)+": "+acc[m-1] + ")";
			
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

class AdditivePricingInstance {
	private static LinearProgramSolver solver = SolverFactory.newDefault();
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
	
	int choose(int n, int k)
	{
		int a=1;
		int b=1;
		for(int i=1;i<=k;i++) {
			a*=(n-i+1);
			b*=i;
		}
		
		return a/b;
	}
	
	/*
	 * Solve this instance and return the value of the objective function and the solution in a list.	
	 */
	private double solveIID(ArrayList<Double> sol, boolean integer)
	{	
		
		ArrayList<int[]> valuations = new ArrayList<int[]>();
		
		
		productDist.enumSymetricValuations(valuations);
		
		int valNo = valuations.size();
		int varNo = valNo*(m+1);
		
		double objective[] = new double[varNo];
		double constraint[] = new double[varNo];
		double support[] = productDist.getIIDSupport();
		double prob[] = productDist.getIIDProb();
		
		for(int j=0;j<varNo;j++)
			objective[j] = 0;
		
		
		for(int i=0; i< valNo; i++) {
			int val[] = valuations.get(i);
			double res = 1;
			int remain = n;
			
			for(int j = 0; remain > 0; j++) {
				res *= Math.pow(prob[j],val[j])*choose(remain, val[j]);
				remain-=val[j];
			}
			
			objective[getPriceIndex(i, m)] = res;
		}
		
		LinearProgram lp = new LinearProgram(objective);
		
		int q = 0;
		
		int val1[] = new int[m];
		int val2[] = new int[m];
		
		for(int i=0;i<valNo;i++) {
			int val[] = valuations.get(i);
			for(int k=0;k<m;k++)
				constraint[getValIndex(i, k, m)] = val[k]*support[k];
			constraint[getPriceIndex(i, m)]=-1;
		
			lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraint, 0, "c"+ q++));
			
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
					lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraint, 0, "c"+ q++));
					
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
		
		
		for(int i=0;i<valNo;i++) {
			for(int k=0;k<m;k++) {
				constraint[getValIndex(i, k, m)] = 1;
				lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraint, 0, "c"+ q++));
				lp.addConstraint(new LinearSmallerThanEqualsConstraint(constraint, 1, "c"+ q++));
				constraint[getValIndex(i, k, m)] = 0;
			}
		}
		
		lp.setMinProblem(false);
		
		double solution[] = solver.solve(lp);
		
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
		
		
		
		return lp.evaluate(solution);
	}
	/*
	 * Solve this instance and return the value of the objective function and the solution in a list.	
	 */
	double solve(ArrayList<Double> sol, boolean integer)
	{	
		if(iid) {
			return solveIID(sol,integer);
		}
		
		ArrayList<Double> obj = new ArrayList<Double>();
		
		productDist.calculateObjective(obj);
		
		Iterator<Double> it = obj.iterator();
		int varNo = obj.size();
		double objective[] = new double[varNo];
		int i=0;
				
		while(it.hasNext())
			objective[i++]=it.next();
				
		LinearProgram lp = new LinearProgram(objective);
		
		ArrayList<ArrayList<Double>> constraints = new ArrayList<ArrayList<Double>>();
		
		productDist.calcRationalityConstraints(constraints, integer);
		productDist.calcTruthfulnessConstraints(constraints, integer);
		
		Iterator<ArrayList<Double>> it1 = constraints.iterator();
		
		Iterator<Double> it2;
		double constr[]= new double[varNo];
		
		int j=0;
		
		while (it1.hasNext()){
			it2 = it1.next().iterator();
			i=0;
			while(it2.hasNext())
				constr[i++] = it2.next();
				
			lp.addConstraint(new LinearSmallerThanEqualsConstraint(constr, 0, "c"+ ++j)); 	
			
		}	
				
		int size=(int)Math.pow(m,n);

		for(int k=0;k<varNo;++k) {
			constr[k]=0;
			if(integer && ((k+1)%(n+1) != 0))
				lp.setBinary(k);
		}
		
		for(i=0;i<size;++i){
			for(int k=0;k<n;++k) {
				
				if(integer) {
					constr[k+(n+1)*i]=Arithmetic.getShiftMultiplier();
				} else {
					constr[k+(n+1)*i]=1;
				}
				
				lp.addConstraint(new LinearSmallerThanEqualsConstraint(constr, Arithmetic.getShiftMultiplier(), "c"+ ++j));
				constr[k+(n+1)*i]=0;
			}
		}
		
		for(i=0;i<varNo;++i){
			constr[i]=1;
			
			lp.addConstraint(new LinearBiggerThanEqualsConstraint(constr, 0, "c"+ ++j));
			
			constr[i]=0;
		}
		
		lp.setMinProblem(true); 
						
		double solution[] = solver.solve(lp);

		int mul = Arithmetic.getShiftMultiplier();
		
		for(i=0; i<solution.length; ++i) {
			if(integer && (i+1)%(n+1) != 0) {
				sol.add(solution[i]*mul);
			} else {
				sol.add(solution[i]);
			}
		}
		
		return -lp.evaluate(solution);
	}
}

public class AdditivePricing {
	
	private static JFrame frame;

	private static JCheckBox iid;
	
	private static JComboBox Shift, Precision;
	
	private static JTextPane values, obj, menu, scaleText, precisionText;
	
	private static ArrayList<JTextPane> captions;
	
	private static JTextField numI, numV;

	private static JButton setn, setm, solve, solveInt, addItem, reset, edit;
	
	private static ArrayList<JButton> removeItem;
	
	private static ArrayList<JTable> inputTables;
	
	private static GroupLayout layout;
	
	private static GroupLayout.SequentialGroup leftToRightInternal, topToBottom;
	
	private static GroupLayout.ParallelGroup leftToRight;
		
	private static JScrollPane scrollTable;
	
	private static ArrayList<JScrollPane> scrollTablesInternal;
	
	private static int n, m;
			
	private static GroupLayout.ParallelGroup columns[] = new GroupLayout.ParallelGroup[3];
	
	private static GroupLayout.ParallelGroup rows[] = new GroupLayout.ParallelGroup[6];

	/*
	 * Create the part of the frame corresponding to Items
	 */
	private static void createItemsSection()
	{
	    JTextPane items = createTextPane("Number of Items: ", 12, true);
		
		numI = new JTextField("",3);
		
		setn = new JButton("Set");		
		setn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try {
					if((n=Integer.parseInt(numI.getText()))>0) {
						setn.setEnabled(false);
						numI.setEditable(false);
						values.setVisible(true);
						numV.setVisible(true);
						setm.setVisible(true);
						frame.pack();
					    
					} else {
						JOptionPane.showMessageDialog(null, "Input Error: Not a positive integer.");
						numI.setText("");
					}
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(null, "Input Error: Not a positive integer.");
					numI.setText("");
				}
				
				
			}
		});
		
		
		
		iid = new JCheckBox();
		
		GroupLayout.SequentialGroup r = layout.createSequentialGroup();
		
		JTextPane iidt = createTextPane("IID Instance: ", 12, true);
		
		r.addComponent(setn);
		r.addComponent(iidt);
		r.addComponent(iid);
		
	    rows[0].addComponent(items);
	    rows[0].addComponent(numI);
	    rows[0].addComponent(setn);
	    rows[0].addComponent(iid);
	    rows[0].addComponent(iidt);
	    
		columns[0].addComponent(items);
		columns[1].addComponent(numI);
		columns[2].addGroup(r);
		
		columns[0].addComponent(items);
		columns[1].addComponent(numI);
		columns[2].addComponent(setn);
	}
	
	/*
	 * Solve this instance and show the results.
	 */
	private static void solve(boolean integer)
	{
		double value;
		ArrayList<Double> solution = new ArrayList<Double>();
		
		AdditivePricingInstance instance;
		
		if(iid.isSelected()) {
			instance = new AdditivePricingInstance(inputTables.get(0),n);
		} else {
			instance = new AdditivePricingInstance(inputTables);
		}
		
		value = instance.solve(solution,integer);
		
		JTable resultTable = createResultTable(solution, instance);
		
		showResults(value, resultTable);
		
		if(!iid.isSelected()) {
			solve.setEnabled(false);
			solveInt.setEnabled(false);
			addItem.setEnabled(false);
			Shift.setEnabled(false);
			Precision.setEnabled(false);
		
		
			Iterator<JButton> it = removeItem.iterator();
			
			while(it.hasNext())
				it.next().setEnabled(false);
		}
		
	}
	
	/*
	 * Remove the i-th item from the input section.
	 */
	private static void removeTable(int i)
	{
		scrollTablesInternal.get(i).getViewport().removeAll();
		
		
		removeItem.get(n-1).setVisible(false);
		removeItem.remove(n-1);
		
		captions.get(n-1).setVisible(false);
		captions.remove(n-1);
		
		for(int j=i; j<n-1;++j){
			removeItem.get(j).setText("Remove Item " + (j+1));
			captions.get(j).setText("Item " + (j+1));
			scrollTablesInternal.get(j).getViewport().add(inputTables.get(j+1));
		}
		
		inputTables.remove(i);
		scrollTablesInternal.get(n-1).getViewport().removeAll();
		scrollTablesInternal.get(n-1).setVisible(false);
		scrollTablesInternal.remove(n-1);
		
		if(n == 2) {
			removeItem.get(0).setEnabled(false);
		}
			
	}
	
	/*
	 * Add an input table for the IID case.
	 */
	private static void addIIDTable()
	{
		String columnNames[] = {"Value", "Probability"};
		
		if(inputTables.size()>0)
			return;
		
		JTextPane cap;
		Object data[][] = new Object[m][2];
		
		String d[][] = {{"1",".5"},{"2",".5"}};
		
		for(int j=0;j<m;++j) {
			data[j][0]=new String();
			data[j][1]=new String();
		}
		
		
		JTable t;
		JScrollPane sp;
		
		inputTables.add((t = new JTable(data,columnNames)));
		
		t.setVisible(true);
		t.setPreferredScrollableViewportSize(t.getPreferredSize());
		scrollTablesInternal.add((sp = new JScrollPane(t)));
			
		captions.add((cap = createTextPane("Distribution", 12, true)));
		
		rows[2].addComponent(cap);
		rows[3].addComponent(sp);
		
		columns[0].addComponent(cap);
		columns[0].addComponent(sp);
			

	}
	
	
	/*
	 * Add a table for an item in the input section.
	 */
	private static void addTable()
	{
		String columnNames[] = {"Value", "Probability"};
		
		int i = inputTables.size();
		
		JTextPane cap;
		Object data[][] = new Object[m][2];
		
		for(int j=0;j<m;++j) {
			data[j][0]=new String();
			data[j][1]=new String();
		}
		
		
		String d[][] = {{"1",".9"},{"2",".1"}};
		
		JTable t;
		JScrollPane sp;
		
		inputTables.add((t = new JTable(d,columnNames)));
		//inputTables.add((t = new JTable(data,columnNames)));
		
		t.setVisible(true);
		t.setPreferredScrollableViewportSize(t.getPreferredSize());
		scrollTablesInternal.add((sp = new JScrollPane(t)));
			
		captions.add((cap = createTextPane("Item " + (i+1), 12, true)));
		
		rows[2].addComponent(cap);
		rows[3].addComponent(sp);
		
		JButton b;
		
		removeItem.add(b = new JButton("Remove Item " + (i+1)));
		
		b.setName(i+"");
		b.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
					removeTable(Integer.parseInt(((JButton)e.getSource()).getName()));
					--n;
					frame.pack();
					frame.setLocationRelativeTo(null);
				    frame.setVisible(true);
			}
		});
		
		rows[4].addComponent(b);
				
		if(i<3) {
			columns[i].addComponent(b);
			columns[i].addComponent(cap);
			columns[i].addComponent(sp);
			
			if(i == 1) {
				removeItem.get(0).setEnabled(true);
			}
			
		} else {
			GroupLayout.ParallelGroup group = layout.createParallelGroup();
			
			group.addComponent(cap);
			group.addComponent(sp);
			group.addComponent(b);
			
			leftToRightInternal.addGroup(group);
		}
	}
	
	

	
	/*
	 * Create the part of the frame corresponding to Input
	 */
	private static void createInputSection()
	{	
		try {
			if((m = Integer.parseInt(numV.getText()))>0) {

				numV.setEditable(false);
				setm.setEnabled(false);
				iid.setEnabled(false);
				
				boolean id = iid.isSelected();
				
				inputTables = new ArrayList<JTable>();
				scrollTablesInternal = new ArrayList<JScrollPane>();
				captions = new ArrayList<JTextPane>();
				removeItem = new ArrayList<JButton>();
				
				if(id)
					solve = new JButton("Solve IID");
				else
					solve = new JButton("Solve");
				
				solve.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
							if(validate()) {
								Arithmetic.setPrecision(Precision.getSelectedIndex()+3);
								Arithmetic.setShift(Shift.getSelectedIndex());
								solve(false);
							}
					}
				});
				
				
				if(id)
					solveInt = new JButton("Solve Integer IID");
				else
					solveInt = new JButton("Solve Integer");
				solveInt.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
							if(validate()) {
								Arithmetic.setPrecision(Precision.getSelectedIndex()+3);
								Arithmetic.setShift(Shift.getSelectedIndex());
								solve(true);
							}
					}
				});
				
				if(!id) {
				
					addItem = new JButton("Add Item");
					addItem.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
								addTable();
								++n;
								frame.pack();
							    frame.setLocationRelativeTo(null);
							    frame.setVisible(true);
						}
					});
				}
				
				String[] options = { "0", "1", "2", "3", "4", "5", "6", "7" };
				String[] optionsP = { "3 decimals", "4 decimals", "5 decimals", "6 decimals", "7 decimals", "8 decimals"};

				Shift = new JComboBox(options);
				Precision = new JComboBox(optionsP);
				
				GroupLayout.SequentialGroup group = layout.createSequentialGroup();
				
				scaleText = createTextPane(" Scale:", 12, false);
				
				scaleText.setVisible(true);
				
				precisionText = createTextPane("Precision:", 12, false);
				
				precisionText.setVisible(true);
				
				group.addComponent(solve);
				group.addComponent(solveInt);
				if(!id)
					group.addComponent(addItem);
				group.addComponent(scaleText);
				group.addComponent(Shift);
				group.addComponent(precisionText);
				group.addComponent(Precision);
				
				leftToRight.addGroup(group);
				
				
				rows[5].addComponent(solve);
				rows[5].addComponent(solveInt);
				if(!id)
					rows[5].addComponent(addItem);
				rows[5].addComponent(scaleText);
				rows[5].addComponent(Shift);
				rows[5].addComponent(precisionText);
				rows[5].addComponent(Precision);
				
				if(!id)
					for(int i=0;i<n;++i) {
						addTable();
					}
				else
					addIIDTable();
										
				frame.pack();
				frame.setLocationRelativeTo(null);
			    frame.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(null, "Input Error: Not a positive integer.");
				numV.setText("");
			}
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(null, "Input Error: Not a positive integer.");
			numV.setText("");
		}
	}
	
	
	/*
	 * Create the part of the frame corresponding to Values
	 */
	private static void createValuesSection()
	{
		values = createTextPane("Number of Values: ", 12, false);

	    numV = new JTextField("",3);
	    numV.setVisible(false);
	    
	    setm = new JButton("Set");
	    setm.setVisible(false);
		setm.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				createInputSection();			
			}
		});
		
		columns[0].addComponent(values);
		columns[1].addComponent(numV);
		columns[2].addComponent(setm);
		
		rows[1].addComponent(values);
	    rows[1].addComponent(numV);
	    rows[1].addComponent(setm);
	}
	
	
	/*
	 * Create the results section
	 */
	
	private static void createResultsSection()
	{
		obj = createTextPane("", 15, false);
		menu = createTextPane("", 15, false);
		scrollTable = new JScrollPane();
		scrollTable.setVisible(false);
		reset = new JButton("Reset");
		reset.setVisible(false);
		reset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				hideResults();
				initializeFrame();
			   
			}
		});
		edit = new JButton("Edit");
		edit.setVisible(false);
		edit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				hideResults();
			}
		});
		GroupLayout.ParallelGroup r = layout.createParallelGroup().addComponent(reset).addComponent(edit);
		GroupLayout.SequentialGroup c = layout.createSequentialGroup().addComponent(reset).addComponent(edit);
		
		leftToRight.addComponent(obj);
		leftToRight.addComponent(menu);
		leftToRight.addComponent(scrollTable);
		leftToRight.addGroup(c);
		
		topToBottom.addComponent(obj);
		topToBottom.addComponent(menu);
		topToBottom.addComponent(scrollTable);
		topToBottom.addGroup(r);
	}
	
	
	/*
	 * Create the arrangement of items in the frame.
	 */
	private static void createLayout()
	{
		JPanel panel = new JPanel();
		layout = new GroupLayout(panel);
	    layout.setAutoCreateContainerGaps(true);
	    layout.setAutoCreateGaps(true);
	    panel.setLayout(layout);
		
	    frame = new JFrame("Create Instance");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
		rows[0] = layout.createParallelGroup();
		rows[1] = layout.createParallelGroup();
		rows[2] = layout.createParallelGroup();
		rows[3] = layout.createParallelGroup();
		rows[4] = layout.createParallelGroup();
		rows[5] = layout.createParallelGroup();
				
		columns[0] = layout.createParallelGroup();
		columns[1] = layout.createParallelGroup();
		columns[2] = layout.createParallelGroup();
			
		leftToRightInternal = layout.createSequentialGroup();
			
		leftToRightInternal.addGroup(columns[0]);
		leftToRightInternal.addGroup(columns[1]);
		leftToRightInternal.addGroup(columns[2]);
	    	    
	    GroupLayout.SequentialGroup topToBottomInternal = layout.createSequentialGroup();
	    
	    topToBottomInternal.addGroup(rows[0]);
	    topToBottomInternal.addGroup(rows[1]);
	    topToBottomInternal.addGroup(rows[2]);
	    topToBottomInternal.addGroup(rows[3]);
	    topToBottomInternal.addGroup(rows[4]);
	    topToBottomInternal.addGroup(rows[5]);
	    	    
	    topToBottom = layout.createSequentialGroup();
	    leftToRight = layout.createParallelGroup();
	    
	    topToBottom.addGroup(topToBottomInternal);
	    	
	    leftToRight.addGroup(leftToRightInternal);
	    	    
	    layout.setHorizontalGroup(leftToRight);
	    layout.setVerticalGroup(topToBottom);
	    
	    frame.add(panel);
	}
	
	
	/*
	 * Draw a frame.
	 */
	
	static void draw()
	{
		createLayout();
		createItemsSection();
		createValuesSection();
		createResultsSection();
	        
	    frame.pack();
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);
	}
	
	
	/*
	 * Create a JTable containing the menu.
	 */
	static JTable createResultTable(ArrayList<Double> sol, AdditivePricingInstance instance)
	{	
		ArrayList<String> valuations = new ArrayList<String>(); 

		String columnNames[] = {"Valuation", "Lottery", "Price"};
		
		instance.enumValuations(valuations);
		
		Iterator<String> it = valuations.iterator();
		Iterator<Double> itSol = sol.iterator();
		
		Object data[][]=new Object[valuations.size()][3];
		
		for(int i=0;it.hasNext();++i) {
			data[i][0]=it.next();
			data[i][1]="(";
			
			int q=n;
			if(iid.isSelected())
				q=m;
				
	    	for(int j=0; j<q; j++) {
	    		data[i][1]+=Arithmetic.sanitizeDouble(itSol.next(), true);
	    		if(j<q-1)
	    			data[i][1]+=", ";
	    	}
	    	data[i][1]+=")";
	    	
	    	data[i][2] = Arithmetic.sanitizeDouble(itSol.next(), true);
	    }
		
		JTable t = new JTable(data,columnNames);
		
		t.setEnabled(false);
				
		t.setVisible(true);
		
		return t;
	}
	

	/*
	 * Create a text pane.
	 */
	static JTextPane createTextPane(String caption, int size, boolean visible)
	{
		JTextPane t = new JTextPane();
		
		t = new JTextPane();
		t.setFont(new Font("default", Font.BOLD, size));
		t.setEditable(false);
		t.setText(caption);
		t.setVisible(visible);
		t.setOpaque(false);
		
		return t;
		
	}
	
	/*
	 * Show the results section in the frame
	 */
	private static void showResults(double objValue, JTable t)
	{
		obj.setText("Optimal Expected Revenue: " + Arithmetic.sanitizeDouble(objValue,true));
		obj.setVisible(true);
		menu.setText("Menu (" + t.getRowCount() + " entries)");
		menu.setVisible(true);
		scrollTable.setVisible(true);
		scrollTable.getViewport().add(t,null);
		reset.setVisible(true);
		edit.setVisible(true);
		frame.pack();
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);
	}
	

	/*
	 * Hide the results section in the frame
	 */
	private static void hideResults()
	{
		boolean id= iid.isSelected();
		
		edit.setVisible(false);
		reset.setVisible(false);
		scrollTable.getViewport().removeAll();
		scrollTable.setVisible(false);
		solve.setEnabled(true);
		solveInt.setEnabled(true);
		if(!id)
			addItem.setEnabled(true);
		Shift.setEnabled(true);
		Precision.setEnabled(true);
		obj.setVisible(false);
		menu.setVisible(false);
		frame.pack();
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);
	    
	    if(!id) {
	    	Iterator<JButton> it = removeItem.iterator();
		
	    	while(it.hasNext())
	    		it.next().setEnabled(true);
	    }
	}
	

	/*
	 * Bring frame to initial status
	 */
	private static void initializeFrame()
	{
		int n = inputTables.size();
		
		for(int i=0; i<n; i++) {
			inputTables.get(i).setVisible(false);
			scrollTablesInternal.get(i).removeAll();
			scrollTablesInternal.get(i).setVisible(false);
			captions.get(i).setVisible(false);
		}
		
		setn.setEnabled(true);
		numI.setEditable(true);
		iid.setEnabled(true);
		numI.setText("");
		
		numV.setEditable(true);
		numV.setVisible(false);
		numV.setText("");
		setm.setEnabled(true);
		setm.setVisible(false);
		values.setVisible(false);
		solve.setVisible(false);
		solveInt.setVisible(false);
		Shift.setVisible(false);
		Precision.setVisible(false);
		scaleText.setVisible(false);
		precisionText.setVisible(false);
		
		if(!iid.isSelected()) {
			addItem.setVisible(false);
		
			Iterator<JButton> it = removeItem.iterator();
		
			while(it.hasNext())
				it.next().setVisible(false);
		}
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	

	/*
	 * Check validity of input.
	 */
	public static boolean validate()
	{
		String v, p;
		Boolean formatError=false;
		double sum,q;
		JTable t;
		
		int n = inputTables.size();
		
		for(int i=0; i<n; ++i) {
			
			t = inputTables.get(i);
			
			if(t.getCellEditor()!=null)
				t.getCellEditor().stopCellEditing();
			
			sum=0;
			
			for(int j=0; j<m; ++j){
				v = t.getValueAt(j, 0).toString();
				p = t.getValueAt(j, 1).toString();
				
				try{
					if(Double.parseDouble(v)<0){
						formatError = true;
						t.setValueAt("", j, 0);
					}
				} catch (NumberFormatException ex) {
					formatError = true;
					t.setValueAt("", j, 0);
				}
				
				try{
					q=Double.parseDouble(p);
					if(q < 0 || q > 1) {
						formatError = true;
						t.setValueAt("", j, 1);
					}
					sum+=q;
				} catch (NumberFormatException ex) {
					formatError = true;
					t.setValueAt("", j, 1);
				}	
			}
			
			/*if(Arithmetic.sanitizeDouble(sum,false).equals("1")) {
				formatError = true;
				for(int j=0; j<m; ++j)
					t.setValueAt("", j, 1);
			}*/
				
		}
		
		if (formatError) {
			JOptionPane.showMessageDialog(null, "Input Error: All values must be positive and probablities must be between 0 and 1 and sum to 1.");
		}
		
		return !formatError;
	}
		
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		draw();
	}

}
