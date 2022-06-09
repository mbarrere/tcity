package uk.ac.imperial.isst.tcity.maxsat;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class ProbLogTransformer {

	final static Logger logger = LogManager.getLogger(ProbLogTransformer.class);
	
	public ProbLogTransformer() {
		
	}
	
	public AndOrGraph toLogValues (AndOrGraph graph) {
		List<AndOrNode> nodes = graph.getNodes();
						
		for (AndOrNode node : nodes) {			
 			if (node.isAtomicType()){		 		
 				node.setOriginalValue(node.getValue());
 	 			String value = node.getValue();
 	 			Double d = 0.0;
 	 			if (value == null || 
 	 					"inf".equalsIgnoreCase(value) ||
 	 					"none".equalsIgnoreCase(value)
 	 					) {
 					//d = MAX_DOUBLE;
 	 				d = 0.0;
 				} else {					
 					d = Double.parseDouble(value);
 				} 	 			 	 			
 	 			logger.debug("Original value for node " + node.getId() + ": "  + d);
 	 						 			 	 			
 	 			Double logValue = -1 * Math.log(d);
 	 			
 	 			if (logValue.isInfinite()) {
 	 				node.setValue("inf");
 	 			} else {
 	 				DecimalFormat df = new DecimalFormat(".######");		 			
 		 			//node.setValue(logValue.toString());
 		 			node.setValue(df.format(logValue));
 	 			}		 			
 	 			
 	 			logger.debug("New value (-log(v)): "  + node.getValue()); 	 			 				
 			} 
		}
		
		return graph;
	}
	
	public AndOrGraph fromLogValues (AndOrGraph graph) {
		List<AndOrNode> nodes = graph.getNodes();
						
		for (AndOrNode node : nodes) {			
 			if (node.isAtomicType()){
 				node.setValue(node.getOriginalValue());
 	 			logger.debug("Original value SET for node " + node.getId() + ": "  + node.getValue());
 			} 
		}
		
		return graph;
	}
}
