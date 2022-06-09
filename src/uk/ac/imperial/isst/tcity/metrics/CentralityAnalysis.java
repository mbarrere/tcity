package uk.ac.imperial.isst.tcity.metrics;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.maxsat.MaxSatAnalyser;
import uk.ac.imperial.isst.tcity.maxsat.MaxSatSolution;
import uk.ac.imperial.isst.tcity.maxsat.config.MathMode;
import uk.ac.imperial.isst.tcity.maxsat.config.MaxSatConfig;
import uk.ac.imperial.isst.tcity.maxsat.config.OptimMode;
import uk.ac.imperial.isst.tcity.maxsat.config.SatMode;
import uk.ac.imperial.isst.tcity.metrics.linegraph.HardeningAndOrGraphExporter;
import uk.ac.imperial.isst.tcity.metrics.linegraph.LineAndOrGraph;
import uk.ac.imperial.isst.tcity.metrics.linegraph.LineAndOrGraphExporter;
import uk.ac.imperial.isst.tcity.metrics.linegraph.LineGraphTransformer;
import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.util.exporters.GraphDisplay;
import uk.ac.imperial.isst.tcity.util.exporters.GraphExporterFactory;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */

public class CentralityAnalysis {

	final static Logger logger = LogManager.getLogger(CentralityAnalysis.class);
	
	private AndOrGraph graph;
	private LineAndOrGraph lineGraph;
	private MaxSatSolution solution; 
	
	public CentralityAnalysis(AndOrGraph graph) {
		this.graph = graph;
		this.lineGraph = null;
		this.solution = null;
	}
	
	public void compute() {
		try {
			logger.info("Starting hardening analysis");			
			//AndOrNode sourceNode = this.graph.getNode(this.graph.getSource());
			//AndOrNode targetNode = this.graph.getNode(this.graph.getTarget());			
			this.lineGraph = new LineGraphTransformer().createLineAndOrGraph(this.graph);
			
			MaxSatConfig maxSatConfig = new MaxSatConfig();
			maxSatConfig.setSatMode(SatMode.FALSIFY);
			maxSatConfig.setMathMode(MathMode.LINEAR);
			maxSatConfig.setOptimMode(OptimMode.MIN);
			
			logger.debug("Loading problem specification");
						
			logger.info("Starting HARDENING analysis with MaxSAT...");			
			long resolutionStart = System.currentTimeMillis();			
			MaxSatAnalyser analyser = new MaxSatAnalyser(maxSatConfig);
			this.solution = analyser.solve(lineGraph.getAndOrGraph());			
			long resolutionTime = System.currentTimeMillis() - resolutionStart;
			
			String maxSatResolutionTime = resolutionTime + " ms (" + ((resolutionTime+500)/1000) + " seconds)";			
						
			logger.info("MaxSat solution found in " + maxSatResolutionTime);
			solution.display();
			
		} catch (Exception e) {
			logger.error(e.getMessage());	
			e.printStackTrace();
		}			
	} 
	
	
	public void exportAndDisplayLineGraph(String inputFilepath) throws Exception{
		//new DOTGraphExporter().exportJSONGraphAndDisplayPNG(inputFilepath);		
		//	String bayesianFilepath = new BayesianAndOrGraphExporter().exportGraph(bgraph, GraphExporterFactory.PNG_FORMAT, inputFilepath);
		//	GraphDisplay.showImage(bayesianFilepath);								
		if (this.lineGraph == null) {
			throw new Exception ("Line (dual) graph must be computed first");
		} else {
			try {
				String lgraphFilepath = new LineAndOrGraphExporter().exportGraph(this.lineGraph, GraphExporterFactory.PNG_FORMAT, inputFilepath, this.solution);
				GraphDisplay.showImage(lgraphFilepath);								
			} catch (Exception e) {
				logger.error(e.getMessage());	
				e.printStackTrace();
			}
		}
		
	}
	
	public void exportAndDisplayInputGraph(String inputFilepath) throws Exception{	
		try {
			String hardeningGraphFilepath = new HardeningAndOrGraphExporter().exportGraph(this.graph, GraphExporterFactory.PNG_FORMAT, inputFilepath);
			GraphDisplay.showImage(hardeningGraphFilepath);								
		} catch (Exception e) {
			logger.error(e.getMessage());	
			e.printStackTrace();
		}		
	}
	
	public void exportAndDisplayHardeningSolutionGraph(String inputFilepath) throws Exception{	
		if (this.lineGraph == null) {
			throw new Exception ("Line (dual) graph must be computed first");
		} else {
			try {
				List<AndOrEdge> solutionEdges = new ArrayList<AndOrEdge>();
				for (AndOrNode solNode : this.solution.getNodes()) {
					solutionEdges.add(this.lineGraph.getNodeEdgeMapping().get(solNode));
				}
				String hardeningGraphFilepath = new HardeningAndOrGraphExporter().exportSolutionGraph(this.graph, GraphExporterFactory.PNG_FORMAT, inputFilepath, solutionEdges);
				GraphDisplay.showImage(hardeningGraphFilepath);								
			} catch (Exception e) {
				logger.error(e.getMessage());	
				e.printStackTrace();
			}		
		}
	}
}
