package uk.ac.imperial.isst.tcity.maxsat.cnf;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.isst.tcity.TCity;
import uk.ac.imperial.isst.tcity.config.ToolConfig;
import uk.ac.imperial.isst.tcity.maxsat.MaxSatSolution;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class TseitinFileExporter {
	
	private static Double MAX_DOUBLE =  TCity.MAX_DOUBLE;
	private TseitinVisitor tseitinVisitor;			
	private List<Integer> nodes;
	private List<List<Integer>> hardClauses;
	private Map<Integer,Double> weights;
	private Long executionTime;
	
	private MaxSatSolution maxSatSolution;
	
	public TseitinFileExporter(TseitinStructure ts) {
		this.tseitinVisitor = ts.getTseitinVisitor();
		this.nodes = ts.getNodes();
		this.hardClauses = ts.getHardClauses();
		this.weights = ts.getWeights();
		this.executionTime = ts.getExecutionTime();
		this.maxSatSolution = null;
	}


	public void toStream(OutputStream os) throws IOException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os));
	    //System.out.println("NODES: " + nodes);
	    w.write("nodes=[");
	    for (int i = 0; i < nodes.size(); i++) {
	    	w.write(nodes.get(i) + ((i==nodes.size()-1)?"]\n":","));
	    }	    	   
	    
	    w.write("clauses=[");
	    for (int i = 0; i < hardClauses.size(); i++) {	    
	    	List<Integer> clause = hardClauses.get(i);
	    	
	    	w.write("[");
	    	for (int j = 0; j < clause.size(); j++) {
		    	w.write(clause.get(j) + ((j==clause.size()-1)?"]":","));
		    }
		    
		    w.write((i==hardClauses.size()-1)?"]\n":",");
	    }    
	    
	    Map<Integer, Object> varNameMap = tseitinVisitor.getVarNameMap();
	    
	    
	    w.write("map={");	    
	    int mapLitCount = 0;
	    for (Integer i : varNameMap.keySet()) {
	    	mapLitCount++;
	    	w.write(i + ":" + varNameMap.get(i)  + ((mapLitCount==varNameMap.keySet().size())?"}\n":","));
	    }
	    
	    
	    w.write("graph_literals=[");	     
	    int gLitCount = 0;
	    for (Integer i : varNameMap.keySet()) {
	    	gLitCount++;
	    	w.write(i + ((gLitCount==varNameMap.keySet().size())?"]\n":","));
	    }
	    
	    
//	    w.write("tseitin_literals=[");
//	    List<Integer> tLit= this.getTseitinLiterals(); 
//	    int tLitCount = 0;
//	    for (Integer i : tLit) {
//	    	tLitCount++;
//	    	w.write(i + ((tLitCount==tLit.size())?"]\n":","));
//	    }
		
	    
	    w.write("costs={");
	    int count = 0;
	    Double maxValue = MAX_DOUBLE;
	    //for (int i = 0; i < weights.size(); i++) {
	    for (Map.Entry<Integer,Double> e : weights.entrySet()) {
	    	count++;
	    	//w.write(e.getKey() + ":" + e.getValue() + ((count==weights.size())?"]\n":","));
	    	w.write(e.getKey() + ":" + (maxValue.equals(e.getValue())?"'inf'":e.getValue()) + ((count==weights.size())?"}\n":","));
	    }	
	    
	    if (maxSatSolution != null) {
		    w.write("time_ms=[" + executionTime + "]\n");
		    	    
			if (maxSatSolution.getCost() != null && maxSatSolution.getCost().compareTo(MAX_DOUBLE)==0) {
				w.write("total_cost=[inf]\n");
			} else {		
				w.write("total_cost=[" + maxSatSolution.getCost() + "]\n");
			}
			
			//System.out.println("Total nodes: " + nodes.size());
			//System.out.print("CUT solution: ");
		
			int solCount = 0;
			w.write("solution=[");
			for (AndOrNode n : maxSatSolution.getNodes()) {	
				solCount++;
				Integer index = tseitinVisitor.getIdsMap().get(n.getId());
				w.write(index + ":" + weights.get(index) + ((solCount==maxSatSolution.getNodes().size())?"]\n":","));
			}
		}
		
	    w.flush();
	}
		
	public void toWCNF(OutputStream os, boolean useInt) throws IOException {
		// MaxSAT Evaluation 2019
		//p wcnf nbvar nbclauses top
		
//		c
//		c comment line
//		c
//		p wcnf 4 5 16
//		16 1 -2 4 0
//		16 -1 -2 3 0
//		8 -2 -4 0
//		4 -3 2 0
//		3 1 3 0
		
		
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os));
	    
		int top = ToolConfig.MAX_INT_VALUE; 
		long nbclauses = hardClauses.size() + weights.size();
				
		w.write("c\nc MPMCS4FTA\n");
		
		if (maxSatSolution != null) {
		    
			if (maxSatSolution.getCost() != null && maxSatSolution.getCost().compareTo(MAX_DOUBLE)==0) {				
				w.write("c #total_cost = inf\n");
			} else {		
				Double cost = maxSatSolution.getCost();
				if (cost % 1 == 0) {
					w.write("c #total_cost = " + cost.longValue() + "\n");
				} else {
					w.write("c #total_cost = " + cost + "\n");
				}
				
			}
			
			w.write("c #total_nodes = " + maxSatSolution.getNodes().size() + "\n");
			
			int solCount = 0;

			w.write("c #solution = [");
			for (AndOrNode n : maxSatSolution.getNodes()) {	
				solCount++;
				Integer index = tseitinVisitor.getIdsMap().get(n.getId());
				//long value = weights.get(index).longValue();
				Double value = weights.get(index);
				if (useInt) {
					w.write("(" + index + ":" + value.longValue() + ((solCount==maxSatSolution.getNodes().size())?")]\n":"),"));
				} else {
					w.write("(" + index + ":" + value + ((solCount==maxSatSolution.getNodes().size())?")]\n":"),"));
				}				
			}
			
			w.write("c #time_ms = " + executionTime + "\n");
			w.write("c\nc * WCNF SPECIFICATION *\nc\n");
		}
				
		w.write("p wcnf " + nodes.size() + " " + nbclauses + " " + top + "\n");
		
	    for (int i = 0; i < hardClauses.size(); i++) {	    
	    	List<Integer> clause = hardClauses.get(i);
	    	
	    	w.write(top + " ");
	    	for (int j = 0; j < clause.size(); j++) {
		    	w.write(clause.get(j) + " ");
		    }
	    	w.write(0 + "\n");		    
	    }    
	    
	    Double maxValue = MAX_DOUBLE;
	    
	    for (Map.Entry<Integer,Double> e : weights.entrySet()) {
	    	if (useInt) {
	    		long value = maxValue.equals(e.getValue())?top:e.getValue().longValue();
	    		if (value > 0) {
		    		// Advice from Ruben Martins
		    		// Most solvers assume soft clauses to have a value equal or higher than 1
		    		// We are generating values from 1 onwards so only Tseitin clauses with 0 weight will be discarded
		    		w.write(value + " " + e.getKey() + " 0\n");
		    	}
	    	} else {
	    		double value = maxValue.equals(e.getValue())?top:e.getValue();
	    		if (value > 0) {
		    		// Advice from Ruben Martins
		    		// Most solvers assume soft clauses to have a value equal or higher than 1
		    		// We are generating values from 1 onwards so only Tseitin clauses with 0 weight will be discarded
		    		w.write(value + " " + e.getKey() + " 0\n");
		    	}
	    	}	    		    
	    }	
	    
	    w.flush();
	}

}
