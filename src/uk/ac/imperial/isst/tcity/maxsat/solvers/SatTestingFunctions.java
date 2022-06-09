package uk.ac.imperial.isst.tcity.maxsat.solvers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class SatTestingFunctions {

	public static Double MAX_DOUBLE =  Double.MAX_VALUE;

	// TESTING FUNCTION	
	private Set<AndOrNode> computeDependentNodes(AndOrGraph graph, AndOrNode n, Map<AndOrNode, Set<AndOrNode>> deps) {
		if (deps.get(n) != null) {
			return deps.get(n);
		}
		
		Set<AndOrNode> nDeps = new LinkedHashSet<AndOrNode>(); 
		
		List<AndOrNode> pred = graph.getIncomingNodes(n.getId());
		
		for (AndOrNode p : pred) {
			if (p.isAtomicType()) {
				nDeps.add(p);
			}			
			Set<AndOrNode> pDeps = computeDependentNodes(graph, p, deps);
			nDeps.addAll(pDeps);			
		}
		deps.put(n, nDeps);
		return nDeps;
	}
	
	// TESTING FUNCTION
	@SuppressWarnings("unused")
	private Map<String, Integer> loadWeightsBasedOnDependencies(AndOrGraph graph, String targetId) {
		Map<String, Integer> weights = new LinkedHashMap<String, Integer>();
		
		Map<AndOrNode, Set<AndOrNode>> deps = new LinkedHashMap<AndOrNode, Set<AndOrNode>>();
		
		AndOrNode target = graph.getNode(targetId);		
		computeDependentNodes(graph, target, deps);
		
		List<AndOrNode> nodes = graph.getNodes();
				
		for (AndOrNode n : nodes) {			
			Double d = MAX_DOUBLE;
			System.out.println("Node " + n.getId() + " with type: " + n.getType());
			
			if (n.isAtomicType()) {
				n.setValue(String.valueOf(deps.get(n).size()));
				weights.put(n.getId(), deps.get(n).size());				
				//System.out.println("NON-INIT Node " + n.getId() + " with type: " + n.getType() + " and value: " + deps.get(n).size());
			}
			if (n.isInitType()) {
				//System.out.println("INIT Node " + n.getId() + " with type: " + n.getType() + " and value: " + d.intValue());				
				n.setValue("inf");
				weights.put(n.getId(), d.intValue());
			}
		}
		System.out.println("\n+++ Weights:\n" + weights);
		System.out.println("\n+++ Dependencies:\n"); // + deps);
		for (Entry<AndOrNode, Set<AndOrNode>> e : deps.entrySet()) {
			System.out.print("Node " + e.getKey().getId() + ": ");
			for (AndOrNode x : e.getValue()) {
				System.out.print(x.getId() + ", ");
			}
			System.out.println();			
		}
		return weights;
	}
}
