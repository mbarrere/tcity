package uk.ac.imperial.isst.tcity.model.merge;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.TCityConstants;
import uk.ac.imperial.isst.tcity.cpag.editor.Node;
import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeDisplayFactory;
import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrGraphCopier;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class GraphMerger {
	
	private static final Logger logger = LogManager.getLogger(GraphMerger.class);
	
		
	public AndOrGraph merge (AndOrGraph g1, AndOrGraph g2) throws Exception {
		
		logger.debug("Merge started...");
		
		AndOrGraph g = new AndOrGraphCopier().cleanCopy(g1);		
		List<AndOrNode> g2Nodes = new ArrayList<AndOrNode>(g2.getNodes());
		
		// Extend G with G2
		
		Map<String,AndOrNode> sameLabelNodesIdMap = new LinkedHashMap<String,AndOrNode>(); // {old key, new key}
		Map<String,String> newIdMap = new LinkedHashMap<String,String>();
		
		for (AndOrNode n : g2Nodes) {
			
			boolean nodeMerged = false;
			
			AndOrNode sameLabelNode = g.findNodeByLabel(n.getLabel());
			if (sameLabelNode != null) {
				logger.debug("Same label found: " + n.getLabel());
				
				// Valid for privilege nodes only
				String type = n.getType();
				if (NodeDisplayFactory.isPrivilegeType(type) 
						//|| NodeDisplayFactory.isActionType(type)
						//|| NodeDisplayFactory.isSplitterType(type)
						) {
					logger.debug("=> merging security privileges... ");
					sameLabelNodesIdMap.put(n.getId(), sameLabelNode);
					nodeMerged = true;
				}
				
			} 
			
			if (!nodeMerged) {			
				AndOrNode x = g.getNode(n.getId());
				if (x != null) {
					logger.debug("Nodes with the same id found. ID: " + n.getId());
					// repeated ID
					String newId = String.valueOf(Node.ID_COUNTER++);
					newIdMap.put(n.getId(), newId);
					n.setId(newId);
				} 
				g.addNode(n);
			}
		}
		
		for (AndOrEdge e : g2.getEdges()) {
			String edgeSrcId = e.getSource();
			String edgeTgtId = e.getTarget();
			
			// Nodes with the same labels
			if (sameLabelNodesIdMap.containsKey(edgeSrcId)) {
				AndOrNode newSrc = sameLabelNodesIdMap.get(edgeSrcId);
				e.setSource(newSrc.getId());
			}
			if (sameLabelNodesIdMap.containsKey(edgeTgtId)) {
				AndOrNode newTgt = sameLabelNodesIdMap.get(edgeTgtId);				
				e.setTarget(newTgt.getId());
			}
			
			// Nodes with the same ids
			if (newIdMap.containsKey(e.getSource())) {
				e.setSource(newIdMap.get(e.getSource()));
			}
			if (newIdMap.containsKey(e.getTarget())) {
				e.setTarget(newIdMap.get(e.getTarget()));
			}
			
			if (!g.containsEdge(e)) {
				g.addEdge(e);
			}
		}
				
		
		
		
		//if (logger.isDebugEnabled()) { new GraphUtils().analyseGraph(g); }
		
		// Place an OR node before security privileges with more than one input
		this.unifyDisjunctiveConditions(g);
				
		// Collapse same logical nodes
		//this.collapseLogicalNodesCubic(g);
		this.collapseLogicalNodesQuad(g);	
		
		// Remove redundant OR nodes
		this.removeRedundantDisjunctions(g);
		
		return g;
	}

	
	
	public void collapseLogicalNodesQuad (AndOrGraph g) throws Exception {
		AndOrNode target = g.getNode(g.getTarget()); 
		
		List<AndOrNode> nodes = g.getNodes();	
		List<AndOrNode> targets = new ArrayList<AndOrNode>();
		
		if (target == null) {
			logger.debug("Unifying target for logical collapsing process...");
			for (AndOrNode node : nodes) {					
				if (g.getOutgoingEdges(node.getId()).size() == 0) {
					targets.add(node);
				}			
			}			
		} else {
			targets.add(target);
		}
		List<AndOrNode> visited = new ArrayList<AndOrNode>();
		
		for (AndOrNode t : targets) {
			this.collapseLogicalNodesQuadRec(g, t, visited);
		}
	}
	
	private void collapseLogicalNodesQuadRec (AndOrGraph g, AndOrNode n, List<AndOrNode> visited) throws Exception {
		
		if (!visited.contains(n)) {
			logger.debug("Analysing node: " + n.getId());
			visited.add(n);
			
			List<AndOrNode> incomingNodes = new ArrayList<AndOrNode>(g.getIncomingNodes(n.getId())); 			
			while (!incomingNodes.isEmpty()) {
				AndOrNode x = incomingNodes.remove(0);
				this.collapseLogicalNodesQuadRec(g, x, visited);
			}
			
			/* 
			// concurrent modification exception
			List<AndOrNode> incomingNodes = g.getIncomingNodes(n.getId()); 			
			for (AndOrNode x : incomingNodes) {
				this.collapseLogicalNodesQuadRec(g, x, visited);
			}
			*/
				
			if (n.isLogicType()) {
				List<AndOrNode> gLogicNodes = g.getLogicTypeNodes();
				
				List<AndOrNode> nodesToEliminate = new ArrayList<AndOrNode>();
				List<AndOrEdge> edgesToEliminate = new ArrayList<AndOrEdge>();
				
				for (AndOrNode x : gLogicNodes) {
					if (!n.equals(x) && 
						( (n.isAndType() && x.isAndType()) ||  (n.isOrType() && x.isOrType())) &&
						samePreconditions(g, n, g, x) ) {
						
						logger.debug("Merge: eliminating node " + n.getId());
						
						List<AndOrEdge> incomingEdges = g.getIncomingEdges(n.getId());
						List<AndOrEdge> outgoingEdges = g.getOutgoingEdges(n.getId());
						
						for (AndOrEdge e : incomingEdges) {
							//AndOrEdge newEdge = new AndOrEdge (e.getSource(), x.getId(), e.getValue(), e.getLabel());
							//g.addEdge(newEdge);
							//g.removeEdge(e);
							edgesToEliminate.add(e);
						}

						for (AndOrEdge e : outgoingEdges) {
							//g.removeEdge(e);
							edgesToEliminate.add(e);
							AndOrEdge newEdge = new AndOrEdge (x.getId(), e.getTarget(), e.getValue(), e.getLabel());
							if (!g.containsEdge(newEdge)) {
								g.addEdge(newEdge);
							}								
						}	
						
						//g.removeNode(n);
						nodesToEliminate.add(n);
												
						break;
					}
				}
				
				for (AndOrEdge e : edgesToEliminate) {
					g.removeEdge(e);		
				}
				
				for (AndOrNode v : nodesToEliminate) {
					g.removeNode(v);		
				}
			}
		}
		
	}
	
	
	
	
	public void unifyDisjunctiveConditions (AndOrGraph g) throws Exception {
		
		List<AndOrNode> atomicNodes = g.getAtomicTypeNodes();
		
		List<AndOrNode> nodesToUpdate = new ArrayList<AndOrNode>();
		
		for (AndOrNode n : atomicNodes) {
			if (g.getIncomingEdges(n.getId()).size() > 1) {
				nodesToUpdate.add(n);
			}			
		}
		
		List<AndOrEdge> edgesToEliminate = new ArrayList<AndOrEdge>();
		List<AndOrEdge> edgesToAdd = new ArrayList<AndOrEdge>();
		
		for (AndOrNode n : nodesToUpdate) {
			logger.debug("** Node to update: " + n.getId());
			//Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			//String orNodeId = "or_" + timestamp.getTime();
			//logger.debug("Now: " + timestamp.getTime());
			
			//String uniqueID = UUID.randomUUID().toString();
			//logger.debug("UniqueID: " + uniqueID);
						
			String orNodeId = String.valueOf(Node.ID_COUNTER++);
			AndOrNode orNode = new AndOrNode (orNodeId, "or", "none", "or");
			logger.debug("Adding new OR node: " + orNode.getId());
			
			
			g.addNode(orNode);
		
			for (AndOrEdge e : g.getIncomingEdges(n.getId())) {
				logger.debug("Incoming edge to '" + n.getId() + "': " + e.getSource());
				AndOrEdge newEdge = new AndOrEdge (e.getSource(), orNode.getId(), e.getValue(), e.getLabel());
				edgesToAdd.add(newEdge);				
				
				edgesToEliminate.add(e);
			}
			
			AndOrEdge newEdge = new AndOrEdge (orNode.getId(), n.getId(), TCityConstants.UNIT_VALUE, "or-" + n.getId());
			edgesToAdd.add(newEdge);
						
			for (AndOrEdge e : edgesToEliminate) {
				logger.debug("Removing edge (" + e.getSource() + "," + e.getTarget() + ")");
				g.removeEdge(e);		
			}
			
			for (AndOrEdge e : edgesToAdd) {
				if (!g.containsEdge(e)) {
					g.addEdge(e);
				}		
			}	
			
			edgesToEliminate.clear();
			edgesToAdd.clear();
		}
		
	}
	
	private boolean samePreconditions (AndOrGraph g1, AndOrNode x1, AndOrGraph g2, AndOrNode x2) {
		List<AndOrNode> inNodesInG1 = g1.getIncomingNodes(x1.getId());
		List<AndOrNode> inNodesInG2 = g2.getIncomingNodes(x2.getId());
		
		Set<AndOrNode> s1 = new LinkedHashSet<>(inNodesInG1);
		Set<AndOrNode> s2 = new LinkedHashSet<>(inNodesInG2);
		
		if (s1.equals(s2)) {
			return true;
		} 
		
		return false;						
	}
	
	
	public void removeRedundantDisjunctions (AndOrGraph g) throws Exception {
		
		List<AndOrNode> logicNodes = g.getLogicTypeNodes();		
		List<AndOrNode> nodesToRemove = new ArrayList<AndOrNode>();
		
		for (AndOrNode n : logicNodes) {
			logger.debug("** Checking if OR node (for removal): " + n.getId());
			if (n.isOrType() && (g.getIncomingEdges(n.getId()).size() == 1)) {				
				nodesToRemove.add(n);				
			}						
		}
		
		List<AndOrEdge> edgesToEliminate = new ArrayList<AndOrEdge>();
		List<AndOrEdge> edgesToAdd = new ArrayList<AndOrEdge>();
		
		for (AndOrNode n : nodesToRemove) {
			logger.debug("** OR node to remove: " + n.getId());
									
			AndOrEdge incomingEdge = g.getIncomingEdges(n.getId()).get(0);
			
			edgesToEliminate.add(incomingEdge);
			
			for (AndOrEdge e : g.getOutgoingEdges(n.getId())) {
				logger.debug("Outgoing edge from '" + n.getId() + "': " + e.getTarget());
				AndOrEdge newEdge = new AndOrEdge (incomingEdge.getSource(), e.getTarget(), incomingEdge.getValue(), incomingEdge.getLabel());
				edgesToAdd.add(newEdge);				
				edgesToEliminate.add(e);
			}
									
			for (AndOrEdge e : edgesToEliminate) {
				logger.debug("Removing edge (" + e.getSource() + "," + e.getTarget() + ")");
				g.removeEdge(e);		
			}
			
			for (AndOrEdge e : edgesToAdd) {
				if (!g.containsEdge(e)) {
					g.addEdge(e);
				}		
			}	
			
			edgesToEliminate.clear();
			edgesToAdd.clear();
		}
		
		for (AndOrNode v : nodesToRemove) {
			g.removeNode(v);		
		}
		
	}
	
	
	////////// OLD ///////////

	///////// UNUSED ////////

	
	@SuppressWarnings("unused")
	private AndOrGraph mergeSimple (AndOrGraph g1, AndOrGraph g2) throws Exception {
		//AndOrGraph g = new AndOrGraph();
		AndOrGraph g = new AndOrGraphCopier().cleanCopy(g1);
		
		List<AndOrNode> g2Nodes = g2.getNodes();
		
		
		for (AndOrNode n : g2Nodes) {
			String idExtension = "";
			if (g.containsNode(n.getId())) {
				if (n.isLogicType() || n.getType().equalsIgnoreCase("attack")) {
					
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					idExtension += "_" + timestamp.getTime();
				    //System.out.println(timestamp);                  // 2021-03-24 17:12:03.311
				    //System.out.println(timestamp.getTime());        // 1616577123311
					//g2.updateNodeId(n, n.getId() + idExtension);
					n.setId(n.getId() + idExtension);
					g.addNode(n);					
				}			
			} else {
				g.addNode(n);
			}
		}
		
		for (AndOrEdge e : g2.getEdges()) {
			try {
				if (!g.containsEdge(e)) {
					g.addEdge(e);
				}
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}		
		
		
		return g;
	}
	
	

	public AndOrGraph mergeOld (AndOrGraph g1, AndOrGraph g2) throws Exception {
		//AndOrGraph g = new AndOrGraph();
		AndOrGraph g = new AndOrGraphCopier().cleanCopy(g1);
		
		List<AndOrNode> g2Nodes = g2.getNodes();
		
		List<AndOrNode> intersection = new ArrayList<AndOrNode>(); 
		List<AndOrNode> difference = new ArrayList<AndOrNode>();
		Stack<AndOrNode> stack = new Stack<AndOrNode>();
		
		for (AndOrNode n : g2Nodes) {
			if (!n.isLogicType()) {
				
				if (!g.containsNode(n.getId())) {
					difference.add(n);
					g.addNode(n);
					
				} else {
					intersection.add(n);
					stack.push(n);
				}				
			}
		}
		
		while (!stack.isEmpty()) {
			AndOrNode n = stack.pop();
			if (!n.isLogicType()) {
				List<AndOrNode> outNodesInG2 = g2.getOutgoingNodes(n.getId());
				
				for (AndOrNode x2 : outNodesInG2) {
					if (x2.isLogicType()) {
						
						List<AndOrNode> outNodesInG1 = g1.getOutgoingNodes(n.getId());
						boolean samePre = false;
						AndOrNode foundNode = null;
						for (AndOrNode x1 : outNodesInG1) {
							if (x1.isLogicType()) {
								if (this.samePreconditions(g1, x1, g2, x2)) {
									samePre = true;
									foundNode = x1;
									break;
								}
							}
						}
						if (samePre) {
							System.out.println("Same preconditions for node " + n.getId() + " and nodes x1=" + foundNode.getId() + ", x2=" + x2.getId());
						} else {
							System.out.println("Different preconditions for node " + n.getId());
						}
							
					}
				}
			}
		}
		
		for (AndOrNode n : difference) {
			g.addEdges(g2.getIncomingEdges(n.getId()));
			g.addEdges(g2.getOutgoingEdges(n.getId()));
		}
		
		return g;
	}
	
		
	//unused
	public void collapseLogicalNodesCubic (AndOrGraph g) throws Exception {
		
		boolean updated = true; 		
		
		while (updated) {
			
			updated = false;
		
			List<AndOrNode> gLogicNodes = g.getLogicTypeNodes();
			List<AndOrNode> auxLogicNodes = g.getLogicTypeNodes();	
			
			List<AndOrNode> nodesToEliminate = new ArrayList<AndOrNode>();
			List<AndOrEdge> edgesToEliminate = new ArrayList<AndOrEdge>();
			
			for (AndOrNode n : gLogicNodes) {			
				auxLogicNodes.remove(n);
				logger.debug("Node n (for1): " + n.getId());
				for (AndOrNode x : auxLogicNodes) {
					if ( (n.isAndType() && x.isAndType()) ||  (n.isOrType() && x.isOrType()) ) {
						logger.debug("Node x (for2): " + x.getId());
						if (samePreconditions(g, n, g, x)) {
							// eliminate node n
							logger.debug("Merge: eliminating node " + n.getId());
							
							List<AndOrEdge> incomingEdges = g.getIncomingEdges(n.getId());
							List<AndOrEdge> outgoingEdges = g.getOutgoingEdges(n.getId());
							
							for (AndOrEdge e : incomingEdges) {
								//AndOrEdge newEdge = new AndOrEdge (e.getSource(), x.getId(), e.getValue(), e.getLabel());
								//g.addEdge(newEdge);
								//g.removeEdge(e);
								edgesToEliminate.add(e);
							}
	
							for (AndOrEdge e : outgoingEdges) {
								//g.removeEdge(e);
								edgesToEliminate.add(e);
								AndOrEdge newEdge = new AndOrEdge (x.getId(), e.getTarget(), e.getValue(), e.getLabel());
								if (!g.containsEdge(newEdge)) {
									g.addEdge(newEdge);
								}								
							}	
							
							//g.removeNode(n);
							nodesToEliminate.add(n);
							
							updated = true;
							break;
						}
					}
				}
				
				if (updated) {
					break;
				}
			}
			
			for (AndOrEdge e : edgesToEliminate) {
				g.removeEdge(e);		
			}
			
			for (AndOrNode n : nodesToEliminate) {
				g.removeNode(n);		
			}
		}
	}
	
		
	public AndOrGraph mergeOld2 (AndOrGraph g1, AndOrGraph g2) throws Exception {
		
		AndOrGraph g = new AndOrGraphCopier().cleanCopy(g1);		
		List<AndOrNode> g2Nodes = new ArrayList<AndOrNode>(g2.getNodes());
		
		for (AndOrNode n : g2Nodes) {
			String idExtension = "";
			if (g.containsNode(n.getId())) {
				if (n.isLogicType() || n.getType().equalsIgnoreCase("attack")) {
					
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					idExtension += "_" + timestamp.getTime();
				    //System.out.println(timestamp);                  // 2021-03-24 17:12:03.311
				    //System.out.println(timestamp.getTime());        // 1616577123311					
					n = g2.updateNodeId(n, n.getId() + idExtension);					
					g.addNode(n);					
				}			
			} else {
				g.addNode(n);
			}
		}
		
		for (AndOrEdge e : g2.getEdges()) {
			try {
				if (!g.containsEdge(e)) {
					g.addEdge(e);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}		
		
		// Place an OR node before security privileges with more than one input
		this.unifyDisjunctiveConditions(g);
				
		// Collapse same logical nodes
		//this.collapseLogicalNodesCubic(g);
		this.collapseLogicalNodesQuad(g);		
		
		return g;
	}
}
