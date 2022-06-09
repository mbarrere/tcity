/**
 * 
 */
package uk.ac.imperial.isst.tcity.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.model.Measure;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class JSONReader {

	private static final Logger logger = LogManager.getLogger(JSONReader.class);
	public static Double MAX_DOUBLE =  Double.MAX_VALUE;
	
	public JSONReader() {		
	}				
	
	public AndOrGraph loadAndOrGraph(String filename) throws Exception {		
	 	ObjectMapper mapper = new ObjectMapper();
	    File inputJSONFile = new File(filename);
	    
	    String artificialSource = GraphUtils.ARTIFICIAL_SOURCE;
	    
	    if (!inputJSONFile.exists()) {
	    	throw new IllegalSpecificationException("The specified JSON file does not exists: " + filename);
	    }
	    	 	
	 	JsonNode rootNode = mapper.readTree(inputJSONFile);
	 	
	 	//System.out.println("Content: ");	 	
	 	//String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode); //rootNode.asText();
	 	//System.out.println(jsonString);
	 		 		 	
	 	AndOrGraph graph = new AndOrGraph();
	 	
	 	String problemSource = null;
	 	String problemTarget = null;
	 	
	 	JsonNode graphNode = rootNode.path("graph");
	 	JsonNode sourceNode = graphNode.path("source");
	 	
	 	if (sourceNode.isMissingNode()) {	 			 		
	 		logger.debug("Problem source not specified. Analysing whether artificial source must be added.");	 		
	 		problemSource = artificialSource;
	 	} else {
	 		problemSource = sourceNode.textValue();
	 	}
	 	
	 	JsonNode targetNode = graphNode.path("target");
	 	if (targetNode.isMissingNode()) {
	 		throw new IllegalSpecificationException("[ERROR] Target node of the problem not specified");
	 	}else {
	 		problemTarget = targetNode.textValue();
	 	}	 	
	 		 	
	 	//Measures addition - 2019-03-12
	 	List<Measure> problemMeasures = new ArrayList<Measure>();	 	
	 	try {
 		 	
	 		JsonNode measuresNode = graphNode.path("measures");
		 	Iterator<JsonNode> measuresIter = measuresNode.iterator();
		
		 	while (measuresIter.hasNext()) {
		 		JsonNode n = measuresIter.next();
		 		
		 		Measure m = mapper.treeToValue(n, Measure.class);		 	
		 		problemMeasures.add(m);								
				logger.debug("Adding measure " + m);				
		 	}		 			 		 			 	
	 	} catch (Exception e) {				
			throw new IllegalSpecificationException(e.getMessage());
		}	 	
	 	
	 	try {
		 		 	
		 	JsonNode nodesNode = graphNode.path("nodes");
		 	Iterator<JsonNode> nodesIter = nodesNode.iterator();
		
		 	while (nodesIter.hasNext()) {
		 		JsonNode n = nodesIter.next();
		 		
		 		AndOrNode node = mapper.treeToValue(n, AndOrNode.class);
		 		logger.debug(node);
				graph.addNode(node);								
		 	}
		 	
		 	JsonNode edgesNode = graphNode.path("edges");
		 	Iterator<JsonNode> edgesIter = edgesNode.iterator();
		
		 	while (edgesIter.hasNext()) {
		 		JsonNode e = edgesIter.next();
		 		
		 		AndOrEdge edge = mapper.treeToValue(e, AndOrEdge.class);		 	
				graph.addEdge(edge);					
				logger.debug(edge);				
		 	}
		 			 	
	 	} catch (Exception e) {				
	 		e.printStackTrace();
			throw new IllegalSpecificationException(e.getMessage());
		}
	 	
	 	GraphUtils utils = new GraphUtils();
	 	problemSource = utils.unifySources(graph, artificialSource);
	 	utils.verifyInputGraph(graph);
	 	
	 	if (!graph.containsNode(problemSource)) {
	 		throw new IllegalSpecificationException("[ERROR] The source node of the problem '" + problemSource + "' does not appear in the graph specification");
	 	}
	 	
	 	if (!graph.containsNode(problemTarget)) {
	 		throw new IllegalSpecificationException("[ERROR] The target node of the problem '" + problemTarget + "' does not appear in the graph specification");
	 	}
		
	 	graph.setSource(problemSource);
	 	graph.setTarget(problemTarget);
	 	
	 	return graph;
	 	//return new ProblemSpecification(graph, problemSource, problemTarget, problemMeasures);
	 		 	
    }

}
