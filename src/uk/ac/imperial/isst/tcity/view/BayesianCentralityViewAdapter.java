package uk.ac.imperial.isst.tcity.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.imperial.isst.tcity.lab.centrality.CentralityMeasure;
import uk.ac.imperial.isst.tcity.metrics.RiskAnalysis;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.util.GraphUtils;

public class BayesianCentralityViewAdapter {
	
	private AndOrGraph cpag;
	private AndOrGraphViewer cpagViewer;
	private Map<String,Double> bayesianMap;
	private Map<String,Double> centralityMap;
	private int weightDecimals = 3; 
	private Map<String,Object> infoMap;
	
	private boolean autoLayout = true;
	private JSlider slider;
	
	private Boolean activeForensics = true;
	private Map<String,Boolean> forensicMap;
	
	private boolean showEdgeValues = true;
	
	
	public BayesianCentralityViewAdapter(AndOrGraph cpag, Map<String,Double> bayesianMap, Map<String,Double> centralityMap) {
		this.cpag = cpag;
		this.cpagViewer = new AndOrGraphViewer(cpag);
		this.bayesianMap = bayesianMap;
		this.centralityMap = centralityMap;
		this.forensicMap = new LinkedHashMap<String,Boolean>();
	}
	
	public void display () {
		this.cpagViewer.setTitle("CPAG (Bayesian - Centrality)");
		this.cpagViewer.display();
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("Attacker profile");
		frame.setSize(800, 800);
		frame.setLayout(new GridLayout(4, 1));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Set the panel to add buttons
		JPanel sliderPanel = new JPanel(new BorderLayout());
		sliderPanel.setPreferredSize(new Dimension(300, 50));
		
		
		// Add status label to show the status of the slider
		//JLabel status = new JLabel("Slide the slider and you can get its value!", JLabel.CENTER);
		JLabel status = new JLabel("Bayesian (" + 0.5 + ") - Centrality (" + 0.5 + ")", JLabel.CENTER);
		
		// Set the slider
		//JSlider slider = new JSlider();	
		this.slider = new JSlider();
		slider.setMinorTickSpacing(10);
		slider.setPaintTicks(true);
		
		// Set the labels to be painted on the slider
		slider.setPaintLabels(true);
		
		// Add positions label in the slider
		Hashtable<Integer, JLabel> position = new Hashtable<Integer, JLabel>();
		position.put(0, new JLabel("More likely"));
		//position.put(50, new JLabel("Medium"));
		position.put(100, new JLabel("More central"));
		
		// Set the label to be drawn
		slider.setLabelTable(position);
		
		// Add change listener to the slider
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int sliderValue = ((JSlider)e.getSource()).getValue(); //between 0 and 100
				//status.setText("Value of the slider is: " + sliderValue);
				
				double centralityWeight = (sliderValue * 1.0) / 100;				
				double bayesianWeight = 1.0 - centralityWeight;
				bayesianWeight = GraphUtils.round(bayesianWeight, weightDecimals);
				
				status.setText("Bayesian (" + bayesianWeight + ") - Centrality (" + centralityWeight + ")");
				//for (AndOrNode n : cpag.getNodes()) {}
				
				updateGraphView(bayesianWeight, centralityWeight);
			}
		});
		
		// Add the slider to the panel
		sliderPanel.add(slider);
		
		// AUTO-LAYOUT
		JButton buttonLayout = new JButton("Disable auto layout");
		buttonLayout.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
			            if (autoLayout) {
			            	autoLayout = false;
			            	cpagViewer.disableAutoLayout();
			            	buttonLayout.setText("Enable auto layout");
			            } else {
			            	autoLayout = true;
			            	cpagViewer.enableAutoLayout();
			            	buttonLayout.setText("Disable auto layout");
			            }
			        }  
			    });  
		
		JPanel layoutPanel = new JPanel();
		layoutPanel.add(buttonLayout);
		
		// SHOW EDGE VALUES
		JButton buttonEdges = new JButton("Hide edge values");
		buttonEdges.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
			            if (showEdgeValues) {
			            	showEdgeValues = false;
			            	cpagViewer.updateEdgeAttributes(false, false);
			            	buttonEdges.setText("Show edge values");
			            } else {
			            	showEdgeValues = true;
			            	cpagViewer.updateEdgeAttributes(true, true);
			            	buttonEdges.setText("Hide edge values");
			            }
			        }  
			    });  
		
		JPanel edgesPanel = new JPanel();
		edgesPanel.add(buttonEdges);
		
		// Set the window to be visible as the default to be false
		//frame.add(panel1);
	    frame.add(sliderPanel);
		frame.add(status);
		frame.add(layoutPanel);
		frame.add(edgesPanel);
		
		frame.pack();
		frame.setVisible(true);
		
		
		if (this.activeForensics) {
					
			/////////// FORENSICS ////////////////
			
			List<JButton> buttons = new ArrayList<JButton>();
			
			//Color okColor = Color.getHSBColor(0.4f, 0.6f, 0.7f);
			Color okColor =  new Color(77, 211, 96); 
			Color compromisedColor = Color.RED;
			//Color compromisedColor = new Color(242, 71, 83);
			
			//String statusLabel = "Status (";		
			//String statusOK = ") => OK";
			String statusOK = " status => OK";
			//String statusCompromised = ") => compromised!";
			String statusCompromised = " status => compromised!";
			for (AndOrNode n : this.cpag.getNodes()) {			
				if (!n.isLogicType()) {
	        		if (!cpag.getSource().equalsIgnoreCase(n.getId())) {
	        			//JButton nodeButton = new JButton(statusLabel + n.getId() + statusOK);
	        			String nodeLabel = "[" + n.getId() + "]";
	        			JButton nodeButton = new JButton(nodeLabel + statusOK);
	        			nodeButton.setBackground(okColor);
	        			nodeButton.setOpaque(true);
	        			buttons.add(nodeButton);        			
	        			nodeButton.addActionListener(new ActionListener(){  
	        				public void actionPerformed(ActionEvent e){  
	        							Boolean compromised = forensicMap.get(n.getId());
	        							if (compromised == null) {
	        								compromised = false;
	        							}
	        				            if (!compromised) {
	        				            	forensicMap.put(n.getId(), true);
	        				            	//nodeButton.setText(statusLabel + n.getId() + statusCompromised);
	        				            	nodeButton.setText(nodeLabel + statusCompromised);
	        				            	nodeButton.setBackground(compromisedColor);        				            		        				            	
	        				            } else {
	        				            	forensicMap.put(n.getId(), false);
	        				            	//nodeButton.setText(statusLabel + n.getId() + statusOK);
	        				            	nodeButton.setText(nodeLabel + statusOK);
	        				            	nodeButton.setBackground(okColor);	        				            	
	        				            }
	        				            
	        				            try {
											forensicEvidenceUpdated();
										} catch (Exception e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
	        				        }  
	        				    });          			
	        		}        		        		        		        	
	        	}        	
			}
			
			JPanel forensicPanel = new JPanel(new GridLayout(buttons.size()+1, 1));		
			forensicPanel.add(new JLabel("Node status", JLabel.CENTER));		
			for (JButton b : buttons) {
				forensicPanel.add(b);
			}
			JFrame forensicFrame = new JFrame("Forensic analysis");
			//forensicFrame.setAlwaysOnTop(true);
			//forensicFrame.setSize(800, 400);
			forensicFrame.setLayout(new GridLayout(1, 1));
			forensicFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);				
			//JLabel forensicTopMessage = new JLabel("Node status", JLabel.CENTER);
			//forensicFrame.add(forensicTopMessage);				
			forensicFrame.add(forensicPanel);
			//forensicFrame.add(new JPanel());	
			forensicFrame.pack();
			forensicFrame.setVisible(true);
		}
		
		this.updateGraphView(0.5, 0.5);
		//this.cpagViewer.disableAutoLayout();
	}
	
	
	private void updateGraphView(double bayesianWeight, double centralityWeight) {
		infoMap = new LinkedHashMap<String,Object>();
		
		for (Entry<String,Double> entry : centralityMap.entrySet()) {	
			
			Double prob = bayesianMap.get(entry.getKey());		
			
			//if (prob != null) { // source node s might not be present in the Bayesian map 
				Double vUni = entry.getValue();
				
				//ystem.out.println("Node " + entry.getKey() + ", prob: " + prob + ", vUni: " + vUni);
				
				//Double unifiedValue = (prob * vElim) + ((1-prob) * vSat); 
				Double unifiedValue = (bayesianWeight * prob) + (centralityWeight * vUni);		
				unifiedValue = GraphUtils.round(unifiedValue, weightDecimals);			
				
				infoMap.put(entry.getKey(), unifiedValue);
				//bnCentralityMap.put(e.getKey(), unifiedValue);
				//status.setText("Value of the slider is: " + sliderValue);				
				//System.out.println("Node " + entry.getKey() + ", \t\tBN-centrality = " + unifiedValue + ", \t\tBayes = " + bayesianMap.get(entry.getKey()) + ", \t\tAndOrCentrality = " + centralityMap.get(entry.getKey()));			
			//}
		}
		//System.out.println("-------------------"); 
		
		this.cpagViewer.addInfoToNodes(infoMap);
		
		for (Entry<String,Boolean> e : forensicMap.entrySet()) {
			if (e.getValue().booleanValue() == true) {
				cpagViewer.updateNodeInfo(e.getKey(), "COMPROMISED");
				//cpagViewer.updateNodeInfo(e.getKey(), "*COMP!");
			}
		}
	}
	
	
	
	private void forensicEvidenceUpdated() throws Exception {
		Map<String, Boolean> fixedVariables = new LinkedHashMap<String, Boolean>();
		
		//fixedVariables.put(this.cpag.getSource(), true);
		
		for (Entry<String,Boolean> e : forensicMap.entrySet()) {
			if (e.getValue().booleanValue() == true) {
				fixedVariables.put(e.getKey(), true);
			}
		}
		
		CentralityMeasure centralityAnalyser = new CentralityMeasure(false, false); 			
		//CentralityMeasure centralityAnalyser = new CentralityMeasure(true, true);
		this.centralityMap = centralityAnalyser.compute(this.cpag, fixedVariables);						
		//Map<String,Double> valueMapByElimination = centralityAnalyser.getValueMapByElimination();
		//Map<String,Double> valueMapBySatisfaction = centralityAnalyser.getValueMapBySatisfaction();
		
		//PENDING: add Bayesian evidence and recompute
		RiskAnalysis riskAnalysis = new RiskAnalysis(cpag);
		riskAnalysis.computeWithEvidence(fixedVariables);
		this.bayesianMap = riskAnalysis.getRiskMap();
		
		
		int sliderValue = this.slider.getValue(); //between 0 and 100			
		double centralityWeight = (sliderValue * 1.0) / 100;				
		double bayesianWeight = 1.0 - centralityWeight;
		bayesianWeight = GraphUtils.round(bayesianWeight, weightDecimals);				
		updateGraphView(bayesianWeight, centralityWeight);
		
		
		
		
		//slider.setValue(100);
		//updateGraphView(0,1);
	}
	
}
