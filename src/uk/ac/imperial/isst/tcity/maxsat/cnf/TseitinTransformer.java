package uk.ac.imperial.isst.tcity.maxsat.cnf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.model.MeasureInstance;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class TseitinTransformer {

	final static Logger logger = LogManager.getLogger(TseitinTransformer.class);
	
	private AndOrGraph graph;	
	
	public TseitinStructure transformGraphWithTseitin (AndOrGraph graph, Map<String,Object> stats) throws Exception{
		this.graph = graph;
		
		try {
			Formula formula = this.getLogicalFormula();			
			
			
			logger.trace("Logical formula: \n" + formula);

			formula = this.negateFormula(formula);
			
			logger.trace("Objective: \n" + formula);
			logger.debug("Performing Tseitin transformation...");


			long start = System.currentTimeMillis();
			// Tseitin TRANSFORMATION
			TseitinVisitor tseitinVisitor = new TseitinVisitor();
			Integer x = formula.accept(tseitinVisitor);
			long tseitinTime = System.currentTimeMillis() - start;

			stats.put("tseitin.time.ms", tseitinTime);
			stats.put("tseitin.time.sec", (tseitinTime/1000));

			logger.debug("Tseitin transformation time: " + tseitinTime + " ms (" + (tseitinTime/1000) + " seconds).");			
			
			TseitinStructure ts = new TseitinStructure(tseitinVisitor, x, formula);
			ts.setup(graph, tseitinTime);
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

	}
	
	public Formula buildFormula (AndOrGraph graph, Map<String,Object> stats) throws Exception{
		this.graph = graph;
		return this.getLogicalFormula();		

	}
	
	/*
	 * Tseitin
	 */
	
	public Formula negateFormula(Formula f) throws Exception {
		return CNF.neg(f);		
	}
	
	public Formula getLogicalFormula() throws Exception {		
		AndOrNode tgt = graph.getNode(graph.getTarget());	
		List<AndOrNode> visited = new ArrayList<AndOrNode>();					
		return this.getLogicalSentence(tgt, visited);
	}
	
	private Formula getNodeFormula(AndOrNode n) {
		Formula atomicNodeForm = CNF.var(n.getId()); 
		List<MeasureInstance> measures = n.getMeasures();
		if (measures == null || measures.isEmpty()) {
			return atomicNodeForm;
		} else {			
			List<Formula> forms = new ArrayList<Formula>();
			forms.add(atomicNodeForm);
			
			for (int i = 0; i < measures.size(); i++) {
				MeasureInstance m = measures.get(i);
				Formula f = CNF.var(m.getId());
				forms.add(f);			
			}
			
			return CNF.or(forms);
		}
	}
	
	private Formula getLogicalSentence(AndOrNode node, List<AndOrNode> visited) throws Exception {
		visited.add(node); 
		List<AndOrNode> incoming = graph.getIncomingNodes(node.getId());
		
		Formula fm = null;
		if (node.isAtomicType()) {
			if (incoming.isEmpty()) {				
				fm = getNodeFormula(node);
			} else {
				if (incoming.size() > 1) {
					//NEW 31/10/2021
					logger.warn("Atomic node '" + node.getId() + "' has more than one input => introducing a new OR node as predecessor of node '" + node.getId() + "'");
					incoming = filterNodes(incoming, visited);
					fm = CNF.and(getNodeFormula(node), getMultiLogicalSentence(incoming, "|", visited)); 
				} else {
					AndOrNode inNode = incoming.get(0);
					if (visited.contains(inNode)) {					
						fm = getNodeFormula(node);
					} else {									
						fm = CNF.and(getNodeFormula(node), getLogicalSentence(inNode, visited));
					}
				}
			}
		}
		
		if (node.isAndType()) {
			if (incoming.isEmpty()) {
				throw new Exception("[ERROR] And-type node '" + node.getId() + "' has no incoming edges");				
			} else {
				incoming = filterNodes(incoming, visited);
				fm = getMultiLogicalSentence(incoming, "&", visited); 
			}
		}
		
		if (node.isOrType()) {
			if (incoming.isEmpty()) {
				throw new Exception("[ERROR] Or-type node '" + node.getId() + "' has no incoming edges");				
			} else {
				incoming = filterNodes(incoming, visited);
				fm = getMultiLogicalSentence(incoming, "|", visited); 
			}
		}
		visited.remove(node);
		return fm;
	}
	
	private Formula getMultiLogicalSentence(List<AndOrNode> nodes, String operator, List<AndOrNode> visited) throws Exception {
		if (nodes.size() == 0) {
			return CNF.tt();
		}
		List<Formula> forms = new ArrayList<Formula>();
		
		for (int i = 0; i < nodes.size(); i++) {
			AndOrNode n = nodes.get(i);
			Formula f = getLogicalSentence(n, visited);
			forms.add(f);			
		}
		
		if ("&".equals(operator)) {
			return CNF.and(forms);
		} else {
			return CNF.or(forms);
		}		
	}
	
	private List<AndOrNode> filterNodes (List<AndOrNode> incomingNodes, List<AndOrNode> visited) {
		List<AndOrNode> filtered = new ArrayList<AndOrNode>();
		for (AndOrNode n : incomingNodes) {
			if (!visited.contains(n)) {
				filtered.add(n);
			}
		}
		return filtered;
	}
	
}
