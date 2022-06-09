/**
 * 
 */
package uk.ac.imperial.isst.tcity.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AndOrGraph {

	private String source;
	private String target;
	
	private List<AndOrNode> nodes;
	private List<AndOrEdge> edges;
	private Map<String, List<AndOrEdge>> inEdges;
	private Map<String, List<AndOrEdge>> outEdges;
	
	private Map<String, AndOrNode> nodeMapById;
	
	private Map<String, List<AndOrNode>> inNodes;
	private Map<String, List<AndOrNode>> outNodes;
	
	public AndOrGraph() {
		nodes = new ArrayList<AndOrNode>();
		edges = new ArrayList<AndOrEdge>();
		inEdges = new LinkedHashMap<String, List<AndOrEdge>>();
		outEdges = new LinkedHashMap<String, List<AndOrEdge>>();
		
		nodeMapById = new LinkedHashMap<String,AndOrNode>();
		
		inNodes = new LinkedHashMap<String, List<AndOrNode>>();
		outNodes = new LinkedHashMap<String, List<AndOrNode>>();
	}
	
	private void addNode(String nodeId, String type, String value, String label) throws Exception {
		if (nodeMapById.containsKey(nodeId)) {
			throw new Exception("[Error] Node " + nodeId + " alreadt exists");			
		}
		AndOrNode n = new AndOrNode(nodeId, type, value, label);
		nodes.add(n);
		nodeMapById.put(nodeId, n);
		
		inEdges.put(nodeId, new ArrayList<AndOrEdge>());
		outEdges.put(nodeId, new ArrayList<AndOrEdge>());
		inNodes.put(nodeId, new ArrayList<AndOrNode>());
		outNodes.put(nodeId, new ArrayList<AndOrNode>());
	}
	
	public void addNode(AndOrNode n) throws Exception {
		if (nodeMapById.containsKey(n.getId())) {
			throw new Exception("[Error] Node " + n.getId() + " already exists");			
		}
		
		nodes.add(n);
		nodeMapById.put(n.getId(), n);
		
		inEdges.put(n.getId(), new ArrayList<AndOrEdge>());
		outEdges.put(n.getId(), new ArrayList<AndOrEdge>());
		inNodes.put(n.getId(), new ArrayList<AndOrNode>());
		outNodes.put(n.getId(), new ArrayList<AndOrNode>());
	}
	
	public void removeNode(AndOrNode n) throws Exception {
		if (!nodeMapById.containsKey(n.getId())) {
			throw new Exception("[Error] Node " + n.getId() + " to remove does not exist");			
		}
		
		nodes.remove(n);		
		nodeMapById.remove(n.getId());
		
		inEdges.remove(n.getId());
		outEdges.remove(n.getId());
		inNodes.remove(n.getId());
		outNodes.remove(n.getId());
	}
	
	public void addNode(String nodeId, String type, String value) throws Exception {
		this.addNode(nodeId, type, value, null);
	}
	
	public void addNodes(List<AndOrNode> nodeList) throws Exception {
		if (nodeList != null) {
			for (AndOrNode n : nodeList) {
				this.addNode(n);
			}
		}
	}
	
	public AndOrNode updateNodeId (AndOrNode n, String newId) throws Exception {

		AndOrNode node = new AndOrNode(newId, n.getType(), n.getValue(), n.getLabel());
		this.addNode(node);
		
		List<AndOrEdge> edgesToEliminate = new ArrayList<AndOrEdge>();
		
		List<AndOrEdge> incomingEdges = this.getIncomingEdges(n.getId());
		List<AndOrEdge> outgoingEdges = this.getOutgoingEdges(n.getId());
		
		for (AndOrEdge e : incomingEdges) {
			AndOrEdge newEdge = new AndOrEdge (e.getSource(), node.getId(), e.getValue(), e.getLabel());
			this.addEdge(newEdge);
			//g.removeEdge(e);
			edgesToEliminate.add(e);
		}

		for (AndOrEdge e : outgoingEdges) {
			//g.removeEdge(e);
			edgesToEliminate.add(e);
			AndOrEdge newEdge = new AndOrEdge (node.getId(), e.getTarget(), e.getValue(), e.getLabel());
			if (!this.containsEdge(newEdge)) {
				this.addEdge(newEdge);
			}
			
		}	
		
		
		for (AndOrEdge e : edgesToEliminate) {
			this.removeEdge(e);		
		}
		
		this.removeNode(n);
		
		return node;
	}
	
	public AndOrNode findNodeByLabel(String label) {
		if (label == null || label.trim().isEmpty()) {
			return null;
		}
		
		for (AndOrNode n : nodes) {
			if ( label.equalsIgnoreCase(String.valueOf(((AndOrNode)n).getLabel())) ) {
				return n;
			};  
		}
		return null;
	}
	
	public void addEdge(AndOrEdge e) throws Exception {
		String sourceId = e.getSource();
		String targetId = e.getTarget();
		
		if (!nodeMapById.containsKey(sourceId)) {			
			throw new Exception("[Error] Adding edge from a non-existing node " + sourceId);
		}
		if (!nodeMapById.containsKey(targetId)) {			
			throw new Exception("[Error] Adding edge to a non-existing node " + sourceId);
		}
		
		if (edges.contains(e)) {
			throw new Exception("[Error] Edge (" + sourceId + "," + targetId + ") already exists");
		}
		
		AndOrNode s = this.nodeMapById.get(sourceId);
		AndOrNode t = this.nodeMapById.get(targetId);
				
		edges.add(e);
		inEdges.get(targetId).add(e);
		outEdges.get(sourceId).add(e);
		inNodes.get(targetId).add(s);
		outNodes.get(sourceId).add(t);
	}
	
	public void removeEdge(AndOrEdge e) throws Exception {
		String sourceId = e.getSource();
		String targetId = e.getTarget();
		
		if (!nodeMapById.containsKey(sourceId)) {			
			throw new Exception("[Error] Removing edge from a non-existing node " + sourceId);
		}
		if (!nodeMapById.containsKey(targetId)) {			
			throw new Exception("[Error] Removing edge to a non-existing node " + sourceId);
		}
		
		if (!edges.contains(e)) {
			throw new Exception("[Error] Edge (" + sourceId + "," + targetId + ") to remove does not exist");
		}
		
		AndOrNode s = this.nodeMapById.get(sourceId);
		AndOrNode t = this.nodeMapById.get(targetId);
				
		edges.remove(e);
		inEdges.get(targetId).remove(e);
		outEdges.get(sourceId).remove(e);
		inNodes.get(targetId).remove(s);
		outNodes.get(sourceId).remove(t);
	}
	
	private void addEdge(String sourceId, String targetId, String value, String label) throws Exception {
		AndOrEdge e = new AndOrEdge(sourceId, targetId, value, label);
		this.addEdge(e);		
	}
	
	public void addEdge(String sourceId, String targetId) throws Exception {
		this.addEdge(sourceId, targetId, null, null);
	}
	
	public void addEdges(List<AndOrEdge> edgeList) throws Exception {
		if (edgeList != null) {
			for (AndOrEdge e : edgeList) {
				this.addEdge(e);
			}
		}
	}
	
	public boolean containsEdge(AndOrEdge e) {
		return edges.contains(e);
	}
	
	public List<AndOrEdge> getIncomingEdges(String nodeId) {
		return inEdges.get(nodeId);
	}
	
	public List<AndOrEdge> getOutgoingEdges(String nodeId) {
		return outEdges.get(nodeId);
	}

	public List<AndOrNode> getIncomingNodes(String nodeId) {
		return inNodes.get(nodeId);
	}
	
	public List<AndOrNode> getOutgoingNodes(String nodeId) {
		return outNodes.get(nodeId);
	}
	
	public boolean containsNode(String nodeId) {
		return this.nodeMapById.get(nodeId) != null;
	}
	
	public AndOrNode getNode(String nodeId) {
		return this.nodeMapById.get(nodeId);
	}
	
	public AndOrEdge getEdge(String sourceId, String targetId) {
		AndOrEdge e = new AndOrEdge(sourceId, targetId, null, null);
		return this.edges.get(this.edges.indexOf(e));
	}

	public List<AndOrNode> getNodes() {
		return this.nodes;
	}
	
	@JsonIgnore
	public List<AndOrNode> getLogicTypeNodes() {
		List<AndOrNode> logicNodes = new ArrayList<AndOrNode>();
		
		for (AndOrNode n : this.nodes) {
			if (n.isLogicType()) {
				logicNodes.add(n);
			}
		}
		return logicNodes;
	}
	
	@JsonIgnore
	public List<AndOrNode> getAtomicTypeNodes() {
		List<AndOrNode> atomicNodes = new ArrayList<AndOrNode>();
		
		for (AndOrNode n : this.nodes) {
			if (n.isAtomicType()) {
				atomicNodes.add(n);
			}
		}
		return atomicNodes;
	}
	
	
	public List<AndOrEdge> getEdges() {
		return this.edges;
	}
	
	@Override
	public String toString() {
		return "AndOrGraph [nodes=" + nodes + ", edges=" + edges + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AndOrGraph other = (AndOrGraph) obj;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}

	
	//For JSON output
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}
