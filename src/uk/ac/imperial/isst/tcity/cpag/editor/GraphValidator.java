package uk.ac.imperial.isst.tcity.cpag.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeDisplayFactory;
import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrGraphCopier;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.util.GraphUtils;

public class GraphValidator {

	public GraphValidator() {
		super();
	}

	public void checkWarnings(AndOrGraph cpag, Map<String,String> warnings) throws Exception {
		AndOrGraph graph = new AndOrGraphCopier().cleanCopy(cpag);	
		
		if (graph.getSource() == null) {
			String artificialSource = "_s_";
			String srcId = new GraphUtils().unifySources(graph, artificialSource);				
			
			if (artificialSource.equalsIgnoreCase(srcId)) {
				warnings.put("Multiple sources (initial nodes) detected", "T-CITY algorithms will create an artificial unified source when needed.");
			} 			
		}
		
		if (graph.getTarget() == null) {
			String artificialTarget = "_t_";
			String tgtId = new GraphUtils().unifyTargets(graph, artificialTarget);
			
			if (artificialTarget.equalsIgnoreCase(tgtId)) {
				warnings.put("Multiple targets (final nodes) detected", "T-CITY algorithms will create an artificial unified target when needed.");
			} 
		}
		
		for (AndOrNode n : cpag.getNodes()) {			
			String type = n.getType();
			if (NodeDisplayFactory.isPrivilegeType(type)) {
				if (n.getLabel() == null || n.getLabel().trim().isEmpty()) {
					warnings.put("Node with empty label ('" + n.getId() + "')", "Node '" + n.getId() + "' has no associated label.");
				}
			}
		}
		
		for (AndOrEdge edge : cpag.getEdges()) {			
			String value = edge.getValue();
			
			try {
				Double d = Double.parseDouble(value);
				
				if (d < 0 || d > 1.0) {
					throw new Exception();
				}
			} catch (Exception e) {
				warnings.put("The value asssociated to edge '" + edge.getSource() + "'->'" + edge.getTarget() + "' (" + value +") is not a valid probability value", "The probability value should be between 0.0 and 1.0.");
			}
			
		}
	}
	
	
	public void validate(AndOrGraph cpag, Map<String,String> problems, Map<String,String> warnings) {
		//Map<String,String> problems = new LinkedHashMap<String,String>();
		
		try {
			if (cpag.getNodes().size() == 0) {
				warnings.put("Empty graph", "The current CPAG is empty.");
				return;
			}
			
			//AndOrGraph cpag = Graph.buildCPAG(graph);
			String immediateAttacks = this.findImmediateAttacks(cpag);
			
			if (immediateAttacks != null) {
				problems.put("Subsequent attack actions found", immediateAttacks);
			}
			
			for (AndOrNode n : cpag.getNodes()) {			
				String type = n.getType();
				
				if (NodeDisplayFactory.isPrivilegeType(type)) {
					if (cpag.getIncomingNodes(n.getId()).size() > 1) {
						problems.put("Security privilege with multiple inputs ('" + n.getId() + "')", "Node '" + n.getId() + "' (" + n.getLabel() + ") has multiple inputs while it should have only one.");
					}				
				}
				
				if (NodeDisplayFactory.isLogicType(type)) {
					if (cpag.getIncomingNodes(n.getId()).size() == 1) {
						problems.put("Bad formed CPAG unit at node '" + n.getId() + "'", "Logic node '" + n.getId() + "' (" + n.getType() + ") should have two or more inputs.");
					}				
				}
				
				if (NodeDisplayFactory.isActionType(type) 
						|| NodeDisplayFactory.isLogicType(type)
						|| NodeDisplayFactory.isSplitterType(type)
						) {
					if (cpag.getOutgoingNodes(n.getId()).size() == 0) {
						problems.put("Bad formed CPAG unit at node '" + n.getId() + "'", "Node '" + n.getId() + "' (" + n.getLabel() + ") has no outgoing nodes.");
					}
					
					if (cpag.getIncomingNodes(n.getId()).size() == 0) {
						problems.put("Bad formed CPAG unit at node '" + n.getId() + "'", "Node '" + n.getId() + "' (" + n.getLabel() + ") has no incoming nodes.");
					}
				}
				
				if (NodeDisplayFactory.isActionType(type) && (cpag.getOutgoingNodes(n.getId()).size() > 1) ) {
					problems.put("Bad formed CPAG unit at node '" + n.getId() + "'", "Node '" + n.getId() + "' (" + n.getLabel() + ") has two or more outgoing nodes. A splitter gate should be used to connect the action with the outgoing nodes.");
				}
			}
			
			
			this.checkWarnings(cpag, warnings);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			problems.put("Error: CPAG validation exception.", e.getMessage());
		}
				
	}
	
	private String findImmediateAttacks (AndOrGraph cpag) {
		
		for (AndOrNode n : cpag.getNodes()) {
			
			String type = n.getType();
			if (NodeDisplayFactory.isActionType(type)) {
				AndOrNode x = searchNextImmediateAction(cpag, n);
				if (x != null) {
					return "There are no security privileges between actions (id:'" + n.getId() + "') and (id:'" + x.getId() + "')."; 
				}
			}
		}
		
		return null;		
	}
	
	private AndOrNode searchNextImmediateAction(AndOrGraph graph, AndOrNode n) {
		List<AndOrNode> visited = new ArrayList<AndOrNode>();
		return this.searchNextImmediateActionRec(graph, n, visited);
	}
	
	private AndOrNode searchNextImmediateActionRec(AndOrGraph cpag, AndOrNode n, List<AndOrNode> visited) {
		if (!visited.contains(n)) {
			visited.add(n);
			
			List<AndOrNode> outgoingNodes = cpag.getOutgoingNodes(n.getId());
			for (AndOrNode x : outgoingNodes) {
				if (NodeDisplayFactory.isActionType(x.getType())) {
					return x;
				} 				
				
				if (NodeDisplayFactory.isLogicType(x.getType())
						|| NodeDisplayFactory.isSplitterType(x.getType())) {
					AndOrNode next = this.searchNextImmediateActionRec(cpag, x, visited);
					
					if (next != null) {
						return next;
					}
				}
			}
		}
		return null;
	}
		
}
