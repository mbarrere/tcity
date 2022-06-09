/**
 * 
 */
package uk.ac.imperial.isst.tcity.maxsat;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class MaxSatSolution {
	
	private static final Logger logger = LogManager.getLogger(MaxSatSolution.class);

	private AndOrGraph graph;
	private List<AndOrNode> nodes;	
	private Double cost;
	private String solverId;
	
	public boolean hasSolution() {
		return this.nodes != null && this.nodes.size() > 0;
	}
	
	public MaxSatSolution(AndOrGraph graph, List<AndOrNode> nodes, Double cost) {
		super();
		this.graph = graph;
		this.nodes = nodes;
		this.cost = cost;
	}
	
	public MaxSatSolution(AndOrGraph graph) {
		super();
		this.graph = graph;
		this.nodes = new ArrayList<AndOrNode>();
		this.cost = -1.0;
	}

	public AndOrGraph getGraph() {
		return graph;
	}

	public void setGraph(AndOrGraph graph) {
		this.graph = graph;
	}

	public List<AndOrNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<AndOrNode> nodes) {
		this.nodes = nodes;
	}

	public Double getCost() {
		return cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	public String getSolverId() {
		return solverId;
	}

	public void setSolverId(String solverId) {
		this.solverId = solverId;
	}
	
	public void display() {		
		logger.info("=== MaxSat solution ===");
		logger.info("Cost: " + this.getCost());		
		//logger.info("Nodes: " + this.getNodes());
		if (this.getNodes() == null || this.getNodes().size() == 0) {
			logger.info("Nodes: []");
		} else {			
			String s = "[";
			for (int i = 0; i < this.getNodes().size(); i++) {
				AndOrNode n = this.getNodes().get(i);
				s += n.getId() + " (" + n.getValue() + ")"; 
				s += (i<(this.getNodes().size()-1))?", ":"]";
			}
			logger.info("Nodes: " + s);
		}
	}
}
