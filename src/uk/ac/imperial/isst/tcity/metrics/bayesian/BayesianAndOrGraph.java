package uk.ac.imperial.isst.tcity.metrics.bayesian;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.jayes.BayesNet;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class BayesianAndOrGraph {

	private AndOrGraph graph;
	private BayesNet bayesNet;
	
	private Map<AndOrNode,List<Double>> probabilities;
	
	
	public AndOrGraph getAndOrGraph() {
		return graph;
	}
	
	public BayesNet getBayesNet() {
		return bayesNet;
	}
	
	public void setAndOrGraph(AndOrGraph graph) {
		this.graph = graph;
		
	}
	public void setBayesNet(BayesNet bayesNet) {
		this.bayesNet = bayesNet;
		this.probabilities = new LinkedHashMap<AndOrNode,List<Double>>();
	}
	
	public void addNodeBeliefs(AndOrNode n, List<Double> beliefs) {
		this.probabilities.put(n, beliefs);
	}
	
	public Double getUnconditionalProbability(AndOrNode n) {
		/*
		Double value = this.probabilities.get(n).get(0); 				
		BigDecimal bd = new BigDecimal(value).setScale(14, RoundingMode.HALF_UP);
	    double roundedValue = bd.doubleValue();
	    System.out.println("Rounded Double value: "+roundedValue);
		return roundedValue;
		*/
		return this.probabilities.get(n).get(0);
	}
	
	
		
	
}
