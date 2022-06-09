/**
 * 
 */
package uk.ac.imperial.isst.tcity.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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
public class GraphUtils {

	private static final Logger logger = LogManager.getLogger(GraphUtils.class);
	public static final String ARTIFICIAL_SOURCE = "_s_";
	
	public GraphUtils() {	
	}
	
	public Map<String,Object> analyseGraph(AndOrGraph g) {
		Map<String,Object> stats = new LinkedHashMap<String,Object>();

		List<AndOrNode> nodes = g.getNodes();
		stats.put("#nodes", nodes.size());
		stats.put("#edges", g.getEdges().size());

		int atomic = 0;
		int and = 0;
		int or = 0;
		int init = 0;

		for (AndOrNode n : nodes) {
			if (n.isInitType()) {
				init++;
			} else {
				if (n.isAtomicType()) {
					atomic++;
				}
				if (n.isOrType()) {
					or++;
				}
				if (n.isAndType()) {
					and++;
				}
			}
		}
		DecimalFormat df = new DecimalFormat(".##");

		logger.debug("Input graph stats: ");
		logger.debug("  #nodes: " + nodes.size());
		logger.debug("  #edges: " + g.getEdges().size());
		logger.debug("  #init: " + (init + "/" + nodes.size()));
		logger.debug("  #atomic: " + atomic + "/" + nodes.size() + " (" + df.format((atomic * 100.0) / nodes.size()) + "%)");
		logger.debug("  #and: " + and + "/" + nodes.size() + " (" + df.format((and * 100.0) / nodes.size()) + "%)");
		logger.debug("  #or: " + or + "/" + nodes.size()  + " (" + df.format((or * 100.0) / nodes.size()) + "%)");
		logger.debug("  #total: " + (init+atomic+and+or) + "/" + nodes.size() + " (" + df.format(((init+atomic+and+or) * 100.0) / nodes.size()) + "%)");
		
		stats.put("#init", init);
		stats.put("#atomic", atomic);
		stats.put("#and", and);
		stats.put("#or", or);
		stats.put("#total", (init+atomic+and+or));

		return stats;
	}
	
	public String unifySources(AndOrGraph graph, String artificialSource) throws Exception {
		List<AndOrNode> nodes = graph.getNodes();		
		List<AndOrNode> sources = new ArrayList<AndOrNode>();
		
		for (AndOrNode node : nodes) {	
			int inCount = graph.getIncomingEdges(node.getId()).size();
			if (inCount == 0) {
				sources.add(node);
			}			
		}
		int count = sources.size();
		logger.debug("Count sources: " + count);
		
		//if (count > 1 || (count==1 && !sources.get(0).isInitType())) {			
		if (count > 1) {
			AndOrNode init = new AndOrNode(artificialSource, "init", "inf", "init");
			graph.addNode(init);						
			logger.debug(count + " sources detected => unification in process. ");			
			for (int i = 0; i < count; i++) {
				AndOrNode target = sources.get(i);
				//AndOrEdge e = new AndOrEdge(init.getId(), target.getId(), TCityConstants.NotApplicable, "init-" + target.getType());
				AndOrEdge e = new AndOrEdge(init.getId(), target.getId(), TCityConstants.UNIT_VALUE, "init-" + target.getType());
				graph.addEdge(e);
				logger.debug("The following edge has been automatically added: " + e);
			}			
			logger.debug("New artificial source: " + artificialSource);			
			return artificialSource;
		} else {
			return sources.get(0).getId();						
		}
	}
	
	public String unifyTargets(AndOrGraph graph, String artificialTarget) throws Exception {
		List<AndOrNode> nodes = graph.getNodes();		
		List<AndOrNode> targets = new ArrayList<AndOrNode>();
		
		for (AndOrNode node : nodes) {	
			int outCount = graph.getOutgoingEdges(node.getId()).size();
			if (outCount == 0) {
				targets.add(node);
			}			
		}
		int count = targets.size();
		logger.debug("Count targets: " + count);
		
		if (count > 1) {			
			AndOrNode artificalTgt = new AndOrNode(artificialTarget, "impact", "inf", "impact");
			graph.addNode(artificalTgt);						
			logger.debug(count + " targets detected => unification in process. ");
			
			AndOrNode orNode = new AndOrNode("_or_" + artificialTarget,"OR","none","OR");
			graph.addNode(orNode);
			for (int i = 0; i < count; i++) {
				AndOrNode target = targets.get(i);
				AndOrEdge e = new AndOrEdge(target.getId(), orNode.getId(), TCityConstants.UNIT_VALUE, "");
				graph.addEdge(e);
				logger.debug("The following edge has been automatically added: " + e);
			}			
			AndOrEdge e = new AndOrEdge(orNode.getId(), artificalTgt.getId(), TCityConstants.UNIT_VALUE, "");
			graph.addEdge(e);
			logger.debug("New artificial target: " + artificialTarget);			
			return artificialTarget;
		} else {
			return targets.get(0).getId();						
		}
	}
	
	public void verifyInputGraph(AndOrGraph graph) throws IllegalSpecificationException {
		List<AndOrNode> nodes = graph.getNodes();		
		//boolean errorFound = false; 
		List<String> errors = new ArrayList<String>(); 
		
		for (AndOrNode node : nodes) {	
			//int inCount = graph.getIncomingEdges(node.getId()).size();
			List<AndOrEdge> incomingEdges = graph.getIncomingEdges(node.getId());
			int inCount = 0; 
			if (incomingEdges != null) {
				inCount = incomingEdges.size();
			}
			
;			if (node.isAtomicType()) {
				if (inCount != 1 && !node.isInitType()) {
					String message = "[Specification error] Atomic-type node '" + node.getId() + "' has " + inCount + " incoming edges while it should have just 1.";
					logger.error(message);
					for (AndOrEdge e : incomingEdges) {
						logger.error(e);
					}
					errors.add(message);
					//throw new IllegalSpecificationException(message);
				}
				if (inCount > 0 && node.isInitType()) {
					String message = "[Specification error] Init-type node '" + node.getId() + "' has " + inCount + " incoming edges while it should have none.";
					logger.error(message);
					for (AndOrEdge e : incomingEdges) {
						logger.error(e);
					}
					errors.add(message);
					//throw new IllegalSpecificationException(message);
				}
			}
			
			if (node.isAndType()) {
				if (inCount < 2 ) {
					String message = "[Specification error] And-type node '" + node.getId() + "' has " + inCount + " incoming edges while it should have at least 2.";
					logger.error(message);
					for (AndOrEdge e : incomingEdges) {
						logger.error(e);
					}
					errors.add(message);
					//throw new IllegalSpecificationException(message);		
				} 
			}

			if (node.isOrType()) {
				if (inCount < 2 ) {
					String message = "[Specification error] Or-type node '" + node.getId() + "' has " + inCount + " incoming edges while it should have at least 2.";
					logger.error(message);
					for (AndOrEdge e : incomingEdges) {
						logger.error(e);
					}
					errors.add(message);
					//throw new IllegalSpecificationException(message);		
				} 
			}						
		}
		
		if (!errors.isEmpty()) {
			logger.error("The following errors were found: ");
			for (String e : errors) {
				logger.error("\t" + e);
			}
			throw new IllegalSpecificationException("Specification error, see log for details.");
		}
	}
	
	public void showNodeDistribution(AndOrGraph graph) throws IllegalSpecificationException {
		List<AndOrNode> nodes = graph.getNodes();		
		
		int total = 0;
		int atomic = 0;
		int ands = 0;
		int ors = 0;
		
		for (AndOrNode node : nodes) {	
			total++;
			
			if (node.isAtomicType()) {
				atomic++;				
			}
			
			if (node.isAndType()) {
				ands++;
			}

			if (node.isOrType()) {
				ors++; 
			}						
		}
				
		logger.debug("Graph distribution #nodes: " + total);
		logger.debug("Graph distribution #atomic: " + atomic);
		logger.debug("Graph distribution #ands: " + ands);
		logger.debug("Graph distribution #ors: " + ors);
	 	//System.out.println("Graph distribution #edges: " + graph.getEdges().size());	
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
}
