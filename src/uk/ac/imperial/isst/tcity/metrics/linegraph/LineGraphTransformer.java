package uk.ac.imperial.isst.tcity.metrics.linegraph;
/**
 * 
 */

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.TCityConstants;
import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class LineGraphTransformer {

	final static Logger logger = LogManager.getLogger(LineGraphTransformer.class);
	public static final String EDGE_NODE_TYPE = "edge";
	public static final String INIT = "init";
	public static final String TARGET = "goal";
	
	public LineGraphTransformer() {
		
	}
	
	public LineAndOrGraph createLineAndOrGraph (AndOrGraph graph) throws Exception {
		logger.info("Hardening. Building LineAndOrGraph for graph G(V=" + graph.getNodes().size() + ",E=" + graph.getEdges().size() + ")");
		
		if (graph.getNodes().size() == 0 || graph.getEdges().size() == 0) {
			throw new Exception("Unable to build LineAndOrGraph => input graph is empty");
		}
		
		long start = System.currentTimeMillis();
						
		
		AndOrGraph lineGraph = new AndOrGraph(); 		
		List<AndOrEdge> edges = graph.getEdges();
		
		Map<String, List<AndOrNode>> edgeNodesBySourceId = new LinkedHashMap<String, List<AndOrNode>>();
		Map<String, List<AndOrNode>> edgeNodesByTargetId = new LinkedHashMap<String, List<AndOrNode>>();
		//AndOrNode graphSource = graph.getNode(graph.getSource());
		//AndOrNode graphTarget = graph.getNode(graph.getTarget());

		//lineGraph.addNode(graphSource);
		//lineGraph.addNode(graphTarget);
		
		Map<AndOrNode, AndOrNode> nodeMapByEdgeSource = new LinkedHashMap<AndOrNode, AndOrNode>();
		Map<AndOrNode, AndOrNode> nodeMapByEdgeTarget = new LinkedHashMap<AndOrNode, AndOrNode>();
		
		Map<AndOrNode, AndOrEdge> nodeMapToOriginalEdges = new LinkedHashMap<AndOrNode, AndOrEdge>();
		
		for (AndOrEdge e : edges) {			
			AndOrNode edgeSource = graph.getNode(e.getSource());
			AndOrNode edgeTarget = graph.getNode(e.getTarget());
			
			if (edgeSource.isAtomicType() && (edgeTarget.isAtomicType() || edgeTarget.isLogicType())) {
				String newNodeId = e.getSource() + "__" + e.getTarget();
				logger.debug("New node: " + newNodeId);
				AndOrNode n = new AndOrNode(newNodeId, LineGraphTransformer.EDGE_NODE_TYPE, e.getValue(), e.getLabel());
				lineGraph.addNode(n);		
				nodeMapToOriginalEdges.put(n, e);
				
				List<AndOrNode> mappedSourceNodes = edgeNodesBySourceId.get(e.getSource()); 
				if (mappedSourceNodes == null) {
					mappedSourceNodes = new ArrayList<AndOrNode>();
					edgeNodesBySourceId.put(e.getSource(), mappedSourceNodes);
				}
				mappedSourceNodes.add(n);
				List<AndOrNode> mappedTargetNodes = edgeNodesByTargetId.get(e.getTarget()); 
				if (mappedTargetNodes == null) {
					mappedTargetNodes = new ArrayList<AndOrNode>();
					edgeNodesByTargetId.put(e.getTarget(), mappedTargetNodes);
				}
				mappedTargetNodes.add(n);
				
				nodeMapByEdgeSource.put(n, edgeSource);
				nodeMapByEdgeTarget.put(n, edgeTarget);				
			} 

			if (edgeSource.isLogicType()) {
				if (!lineGraph.containsNode(edgeSource.getId())) {
					lineGraph.addNode(edgeSource);
				}
				if (edgeTarget.isLogicType()) {
					if (!lineGraph.containsNode(edgeTarget.getId())) {
						lineGraph.addNode(edgeTarget);
					}
					lineGraph.addEdge(e);
				} else {
					List<AndOrNode> mappedTargetNodes = edgeNodesByTargetId.get(edgeTarget.getId()); 
					if (mappedTargetNodes == null) {
						mappedTargetNodes = new ArrayList<AndOrNode>();
						edgeNodesByTargetId.put(edgeTarget.getId(), mappedTargetNodes);
					}
					mappedTargetNodes.add(edgeSource);
				}
				
			}
		}
		
		
		for (AndOrNode n : lineGraph.getNodes()) {
			if (n.getType().equalsIgnoreCase(LineGraphTransformer.EDGE_NODE_TYPE)) {
				//AndOrNode edgeSource = nodeMapByEdgeSource.get(n);
				//AndOrNode edgeTarget = nodeMapByEdgeTarget.get(n);
				AndOrEdge e = nodeMapToOriginalEdges.get(n);
					
				List<AndOrNode> inputNodes = edgeNodesByTargetId.get(e.getSource());
				// Nodes pointing to this edgeNode n
				if (inputNodes != null) {
					for (AndOrNode in : inputNodes) {
						AndOrEdge edgeFromSource = new AndOrEdge(in.getId(), n.getId(), TCityConstants.NotApplicable, TCityConstants.NotApplicable);
						if (!lineGraph.containsEdge(edgeFromSource)) {
							lineGraph.addEdge(edgeFromSource);
						}
					}
				}
				List<AndOrNode> outputNodes = edgeNodesBySourceId.get(e.getTarget());
				// Nodes pointed by this edgeNode n
				if (outputNodes != null) {
					for (AndOrNode out : outputNodes) {
						AndOrEdge edgeToTarget = new AndOrEdge(n.getId(), out.getId(), TCityConstants.NotApplicable, TCityConstants.NotApplicable);
						if (!lineGraph.containsEdge(edgeToTarget)) {
							lineGraph.addEdge(edgeToTarget);
						}
					}
				}
			}
			
			if (n.isLogicType()) {
				List<AndOrNode> inNodes = edgeNodesByTargetId.get(n.getId());
				if (inNodes != null) {
					for (AndOrNode inNode : inNodes) {
						AndOrEdge edgeFromInNode = new AndOrEdge(inNode.getId(), n.getId(), TCityConstants.NotApplicable, TCityConstants.NotApplicable);
						if (!lineGraph.containsEdge(edgeFromInNode)) {
							lineGraph.addEdge(edgeFromInNode);
						}						
					}
				}
				List<AndOrNode> outNodes = edgeNodesBySourceId.get(n.getId());
				if (outNodes != null) {
					for (AndOrNode outNode : outNodes) {
						AndOrEdge edgeToOutNode = new AndOrEdge(n.getId(), outNode.getId(), TCityConstants.NotApplicable, TCityConstants.NotApplicable);
						if (!lineGraph.containsEdge(edgeToOutNode)) {
							lineGraph.addEdge(edgeToOutNode);
						}						
					}
				}
						
			}
		}
		
		List<AndOrNode> targetList = edgeNodesByTargetId.get(graph.getTarget());
		
		if (targetList == null || targetList.size() == 0) {
			logger.error("Line graph construction failed");
			return null;
		}
		
		if (targetList.size() > 1) {
			logger.warn("Line graph has more than one target (using first node"); 
		}		

		// ADD artificial target
		AndOrNode finalNode = targetList.get(0);		
		//lineGraph.setTarget(finalNode.getId());

		AndOrNode aTarget = new AndOrNode(graph.getTarget(), LineGraphTransformer.TARGET, TCityConstants.Infinite, graph.getNode(graph.getTarget()).getLabel());
		lineGraph.addNode(aTarget);
		AndOrEdge tEdge = new AndOrEdge(finalNode.getId(), aTarget.getId(), TCityConstants.NotApplicable, TCityConstants.NotApplicable);
		if (!lineGraph.containsEdge(tEdge)) {
			lineGraph.addEdge(tEdge);
		}
		lineGraph.setTarget(aTarget.getId());
		
		// ADD artificial source
		//FIXME
		List<AndOrNode> sourceList = edgeNodesBySourceId.get(graph.getSource());
		AndOrNode aSource = new AndOrNode(graph.getSource(), LineGraphTransformer.INIT, TCityConstants.Infinite, graph.getNode(graph.getSource()).getLabel());
		lineGraph.addNode(aSource);
		for (AndOrNode s : sourceList) {
			AndOrEdge sEdge = new AndOrEdge(aSource.getId(), s.getId(), TCityConstants.NotApplicable, TCityConstants.NotApplicable);
			if (!lineGraph.containsEdge(sEdge)) {
				lineGraph.addEdge(sEdge);
			}
		}
		lineGraph.setSource(aSource.getId());
		
		long end = System.currentTimeMillis();
		logger.info("[TIME] Hardening LineAndOrGraph generation took " + (end-start)+ " ms (" + (end-start+500)/1000 + " seconds)");
		
		return new LineAndOrGraph(graph, lineGraph, nodeMapToOriginalEdges);
	}
	
	
	
	
	
	
	/*
	public LineAndOrGraph createLineAndOrGraphOLD (AndOrGraph graph) throws Exception {
		logger.info("Hardening. Building LineAndOrGraph for graph G(V=" + graph.getNodes().size() + ",E=" + graph.getEdges().size() + ")");
		
		if (graph.getNodes().size() == 0 || graph.getEdges().size() == 0) {
			throw new Exception("Unable to build LineAndOrGraph => input graph is empty");
		}
		
		long start = System.currentTimeMillis();
		
		
		LineAndOrGraph lgraph = new LineAndOrGraph(); 
		
		AndOrGraph lineGraph = new AndOrGraph(); 		
		List<AndOrEdge> edges = graph.getEdges();
		
		Map<String, List<AndOrNode>> edgeNodesBySourceId = new LinkedHashMap<String, List<AndOrNode>>();
		Map<String, List<AndOrNode>> edgeNodesByTargetId = new LinkedHashMap<String, List<AndOrNode>>();
		AndOrNode graphSource = graph.getNode(graph.getSource());

		lineGraph.addNode(graphSource);
		for (AndOrEdge e : edges) {			
			String newNodeId = e.getSource() + "__" + e.getTarget();
			logger.debug("New node: " + newNodeId);
			AndOrNode n = new AndOrNode(newNodeId, LineGraphTransformer.EDGE_NODE_TYPE, e.getValue(), newNodeId);
			lineGraph.addNode(n);
			List<AndOrNode> mappedSourceNodes = edgeNodesBySourceId.get(e.getSource()); 
			if (mappedSourceNodes == null) {
				mappedSourceNodes = new ArrayList<AndOrNode>();
				edgeNodesBySourceId.put(e.getSource(), mappedSourceNodes);
			}
			mappedSourceNodes.add(n);
			List<AndOrNode> mappedTargetNodes = edgeNodesByTargetId.get(e.getTarget()); 
			if (mappedTargetNodes == null) {
				mappedTargetNodes = new ArrayList<AndOrNode>();
				edgeNodesByTargetId.put(e.getTarget(), mappedTargetNodes);
			}
			mappedTargetNodes.add(n);
			
			
			if (graphSource.equals(graph.getNode(e.getSource()))) {
				AndOrEdge initEdge = new AndOrEdge(graphSource.getId(), n.getId(), TCityConstants.NotApplicable, TCityConstants.NotApplicable);
				lineGraph.addEdge(initEdge);
			}
			
			List<AndOrNode> inputNodes = edgeNodesByTargetId.get(e.getSource());
			if (inputNodes != null) {
				for (AndOrNode in : inputNodes) {
					AndOrEdge edgeFromSource = new AndOrEdge(in.getId(), n.getId(), TCityConstants.NotApplicable, TCityConstants.NotApplicable);
					lineGraph.addEdge(edgeFromSource);
				}
			}
			List<AndOrNode> outputNodes = edgeNodesBySourceId.get(e.getTarget());
			if (outputNodes != null) {
				for (AndOrNode out : outputNodes) {
					AndOrEdge edgeToTarget = new AndOrEdge(n.getId(), out.getId(), TCityConstants.NotApplicable, TCityConstants.NotApplicable);
					lineGraph.addEdge(edgeToTarget);
				}
			}
		}
		
		lgraph.setAndOrGraph(lineGraph);
		long end = System.currentTimeMillis();
		logger.info("[TIME] Hardening LineAndOrGraph generation took " + (end-start)+ " ms (" + (end-start+500)/1000 + " seconds)");
		return lgraph;
	}
	*/
	
		
}
