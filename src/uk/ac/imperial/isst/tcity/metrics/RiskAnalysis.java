package uk.ac.imperial.isst.tcity.metrics;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.metrics.bayesian.BayesianAndOrGraph;
import uk.ac.imperial.isst.tcity.metrics.bayesian.BayesianAndOrGraphExporter;
import uk.ac.imperial.isst.tcity.metrics.bayesian.BayesianTransformer;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.util.GraphUtils;
import uk.ac.imperial.isst.tcity.util.JSONReader;
import uk.ac.imperial.isst.tcity.util.JSONWriter;
import uk.ac.imperial.isst.tcity.util.exporters.GraphDisplay;
import uk.ac.imperial.isst.tcity.util.exporters.GraphExporterFactory;

public class RiskAnalysis {

	final static Logger logger = LogManager.getLogger(RiskAnalysis.class);
	
	private AndOrGraph graph;
	private BayesianAndOrGraph bgraph;
	
	public RiskAnalysis(AndOrGraph graph) {
		this.graph = graph;
		this.bgraph = null;
	}
	
	public void compute() throws Exception {
		 			
		AndOrNode sourceNode = this.graph.getNode(this.graph.getSource()); 
		if (sourceNode == null) {
			throw new Exception ("The source node with id '" + graph.getSource() + "' does not exist.");
		}
		AndOrNode targetNode = this.graph.getNode(this.graph.getTarget());
		if (targetNode == null) {
			throw new Exception ("The target node with id '" + graph.getTarget() + "' does not exist.");
		}		
		
		if (!this.graph.getIncomingNodes(sourceNode.getId()).isEmpty()) {
			throw new Exception ("Risk analysis error: the source node has incoming nodes/edges");
		}
		try {	
			this.bgraph = new BayesianTransformer().createBayesianAndOrGraph(this.graph, sourceNode, targetNode);			
			logger.info("Unconditional probability for target '" + targetNode.getId() + "': " + bgraph.getUnconditionalProbability(targetNode));
			
		} catch (Exception e) {
			logger.error(e.getMessage());	
			//e.printStackTrace();
			throw e;
		}			
	} 
	
	public void computeWithEvidence(Map<String,Boolean> evidenceMap) {
		try { 			
			AndOrNode sourceNode = this.graph.getNode(this.graph.getSource());
			AndOrNode targetNode = this.graph.getNode(this.graph.getTarget());
			
			this.bgraph = new BayesianTransformer().createBayesianAndOrGraphWithEvidence(this.graph, sourceNode, targetNode, evidenceMap);			
			logger.info("Unconditional probability for target '" + targetNode.getId() + "': " + bgraph.getUnconditionalProbability(targetNode));
			
		} catch (Exception e) {
			logger.error(e.getMessage());	
			e.printStackTrace();
		}			
	} 
	
	public Double getRisk () throws Exception {
		if (this.bgraph == null) {
			throw new Exception ("Bayesian network must be computed first");
		} else {
			AndOrNode targetNode = this.graph.getNode(this.graph.getTarget());					
			return bgraph.getUnconditionalProbability(targetNode);
		}
	}
	
	public Map<String,Double> getRiskMap () throws Exception {
		if (this.bgraph == null) {
			throw new Exception ("Bayesian network must be computed first");
		} else {
			Map<String,Double> riskMap = new LinkedHashMap<String,Double>();
			for (AndOrNode n : this.graph.getNodes()) {
				riskMap.put(n.getId(), bgraph.getUnconditionalProbability(n));
			}
			return riskMap;
		}
	}
	
	
	public void exportAndDisplay(String inputFilepath) throws Exception{
		if (this.bgraph == null) {
			throw new Exception ("Bayesian network must be computed first");
		} else {
			try {
				String bayesianFilepath = new BayesianAndOrGraphExporter().exportGraph(bgraph, GraphExporterFactory.PNG_FORMAT, inputFilepath);
				GraphDisplay.showImage(bayesianFilepath);								
			} catch (Exception e) {
				logger.error(e.getMessage());	
				e.printStackTrace();
			}
		}
	}
	
	public static void testBayesian1() {
		//TCityUtils.loadConfiguration("tcity.conf");
		
		String filename = "examples/bn/bn-example3.json";
		String outputFilepath = "examples/bn/bn-example3-output.json";
		try { 
			logger.info("Loading problem specification");
			
			long start = System.currentTimeMillis(); 				
			AndOrGraph graph = new JSONReader().loadAndOrGraph(filename);
			long loadTime = System.currentTimeMillis() - start;
			logger.info("... done in " + loadTime + " ms (" + ((loadTime+500)/1000) + " seconds).");
							
			Map<String,Object> stats = new GraphUtils().analyseGraph(graph);
			logger.info("Graph stats: " + stats);
			 
			new JSONWriter().write(graph, outputFilepath);
			 
			//String filepath = GraphExporterFactory.getExporter(GraphExporterFactory.PNG_FORMAT).exportGraph(filename);
			//GraphDisplay.showImage(filepath);
			
			AndOrNode sourceNode = graph.getNode(graph.getSource());
			AndOrNode targetNode = graph.getNode(graph.getTarget());
			
			BayesianAndOrGraph bgraph = new BayesianTransformer().createBayesianAndOrGraph(graph, sourceNode, targetNode);
			String bayesianFilepath = new BayesianAndOrGraphExporter().exportGraph(bgraph, GraphExporterFactory.PNG_FORMAT, filename);
			GraphDisplay.showImage(bayesianFilepath);
			
			logger.info("Unconditional probability for target '" + targetNode.getId() + "': " + bgraph.getUnconditionalProbability(targetNode));
		} catch (Exception e) {
			logger.error(e.getMessage());	
			e.printStackTrace();
		}				
	}
}
