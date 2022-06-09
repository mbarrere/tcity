package uk.ac.imperial.isst.tcity.metrics.linegraph;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class LineAndOrGraph {

	private AndOrGraph originalGraph;
	private AndOrGraph graph;
	private Map<AndOrNode, AndOrEdge> nodeEdgeMapping = new LinkedHashMap<AndOrNode, AndOrEdge>();
	
	
	public LineAndOrGraph(AndOrGraph originalGraph, AndOrGraph graph, Map<AndOrNode, AndOrEdge> nodeEdgeMapping) {
		super();
		this.originalGraph = originalGraph;
		this.graph = graph;
		this.nodeEdgeMapping = nodeEdgeMapping;
	}

	public AndOrGraph getOriginalGraph() {
		return originalGraph;
	}
	
	public void setOriginalGraph(AndOrGraph originalGraph) {
		this.originalGraph = originalGraph;
	}
	
	public AndOrGraph getAndOrGraph() {
		return graph;
	}
	
	public void setAndOrGraph(AndOrGraph graph) {
		this.graph = graph;
	}
	
	public Map<AndOrNode, AndOrEdge> getNodeEdgeMapping() {
		return nodeEdgeMapping;
	}
	
	public void setNodeEdgeMapping(Map<AndOrNode, AndOrEdge> nodeEdgeMapping) {
		this.nodeEdgeMapping = nodeEdgeMapping;
	}
		
	
}
