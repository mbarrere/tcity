package uk.ac.imperial.isst.tcity.maxsat;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class LogicGateTransformer {

	final static Logger logger = LogManager.getLogger(LogicGateTransformer.class);
	
	public LogicGateTransformer() {
		
	}
	
	public AndOrGraph flip (AndOrGraph graph) {
		List<AndOrNode> nodes = graph.getNodes();
		
		for (AndOrNode n : nodes) {			
 			if (n.isAndType()){		 		
 				n.setType("or");
 				logger.trace("Changed AND node " + n.getId() + " to OR node");
 			} else {
	 			if (n.isOrType()){		 		
	 				n.setType("and");
	 				logger.trace("Changed OR node " + n.getId() + " to AND node");
	 			}
 			}
		}
		
		return graph;
	}
}
