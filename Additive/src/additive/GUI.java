package additive;

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

import gurobi.GRBException;

public class GUI {
		private static JFrame frame;

		private static JCheckBox iid;
		
		private static JComboBox Precision;
		
		private static JTextPane values, obj, menu, precisionText;
		
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
		private static void solve(boolean integer) throws GRBException
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
			
			//inputTables.add((t = new JTable(d,columnNames)));
			inputTables.add((t = new JTable(data,columnNames)));
			
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
									Arithmetics.setPrecision(Precision.getSelectedIndex()+3);
									try {
										solve(false);
									} catch (GRBException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
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
									Arithmetics.setPrecision(Precision.getSelectedIndex()+3);
									try {
										solve(true);
									} catch (GRBException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
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

					Precision = new JComboBox(optionsP);
					
					GroupLayout.SequentialGroup group = layout.createSequentialGroup();
					
					precisionText = createTextPane("Precision:", 12, false);
					
					precisionText.setVisible(true);
					
					group.addComponent(solve);
					group.addComponent(solveInt);
					if(!id)
						group.addComponent(addItem);
					group.addComponent(precisionText);
					group.addComponent(Precision);
					
					leftToRight.addGroup(group);
					
					
					rows[5].addComponent(solve);
					rows[5].addComponent(solveInt);
					if(!id)
						rows[5].addComponent(addItem);
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
		    		data[i][1]+=Arithmetics.sanitizeDouble(itSol.next());
		    		if(j<q-1)
		    			data[i][1]+=", ";
		    	}
		    	data[i][1]+=")";
		    	
		    	data[i][2] = Arithmetics.sanitizeDouble(itSol.next());
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
			obj.setText("Optimal Expected Revenue: " + Arithmetics.sanitizeDouble(objValue));
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
			Precision.setVisible(false);
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
			double q;
			JTable t;
			
			int n = inputTables.size();
			
			for(int i=0; i<n; ++i) {
				
				t = inputTables.get(i);
				
				if(t.getCellEditor()!=null)
					t.getCellEditor().stopCellEditing();
				
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
					} catch (NumberFormatException ex) {
						formatError = true;
						t.setValueAt("", j, 1);
					}	
				}	
			}
			
			if (formatError) {
				JOptionPane.showMessageDialog(null, "Input Error: All values must be positive and probablities must be between 0 and 1 and sum to 1.");
			}
			
			return !formatError;
		}
}
