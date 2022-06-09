package uk.ac.imperial.isst.tcity;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import uk.ac.imperial.isst.tcity.maxsat.MaxSatAnalyser;
import uk.ac.imperial.isst.tcity.maxsat.MaxSatSolution;
import uk.ac.imperial.isst.tcity.maxsat.config.MathMode;
import uk.ac.imperial.isst.tcity.maxsat.config.MaxSatConfig;
import uk.ac.imperial.isst.tcity.maxsat.config.OptimMode;
import uk.ac.imperial.isst.tcity.maxsat.config.SatMode;
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


public class TCityTests {
	private static final Logger logger = LogManager.getLogger(TCityTests.class);
	
	public static void testMain1() {
		TCityUtils.loadConfiguration("tcity.conf");
		
		String filename = "examples/fps.json";
		String outputFilepath = "examples/output.json";
		try { 
			logger.debug("Loading problem specification");
			
			long start = System.currentTimeMillis(); 				
			AndOrGraph graph = new JSONReader().loadAndOrGraph(filename);
			long loadTime = System.currentTimeMillis() - start;
			logger.info("Problem specification loaded in " + loadTime + " ms (" + ((loadTime+500)/1000) + " seconds).");
							
			Map<String,Object> stats = new GraphUtils().analyseGraph(graph);
			logger.info("Graph stats: " + stats);
			 
			new JSONWriter().write(graph, outputFilepath);
			 
			//GraphExporter exporter = GraphExporterFactory.getExporter(GraphExporterFactory.DOT_FORMAT);
			//exporter.exportGraph(filename); 
			//exporter.
			String filepath = GraphExporterFactory.getExporter(GraphExporterFactory.PNG_FORMAT).exportGraph(filename);
			GraphDisplay.showImage(filepath);
		} catch (Exception e) {
			logger.error(e.getMessage());	
			e.printStackTrace();
		}
		logger.info("== " + TCity.TOOL_NAME + " ended at " + new Timestamp(System.currentTimeMillis()) + " ==");
		System.exit(0);
	}
	

	
	public static void testBayesian1() {
		TCityUtils.loadConfiguration("tcity.conf");
		
		//String filename = "examples/bn/bn-example2-and.json";
		//String outputFilepath = "examples/bn/bn-example2-and-output.json";
		
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
		logger.info("== " + TCity.TOOL_NAME + " ended at " + new Timestamp(System.currentTimeMillis()) + " ==");
		System.exit(0);
	}
	
	
	
	public static void testTseitin() {
		TCityUtils.loadConfiguration("tcity.conf");
		
		//String filename = "examples/bn/bn-example2-and.json";
		//String outputFilepath = "examples/bn/bn-example2-and-output.json";
		
		String filename = "examples/integer/int-example1.json";
		//String outputFilepath = "examples/bn/bn-example3-output.json";
		try { 
			MaxSatConfig maxSatConfig = new MaxSatConfig();
			//maxSatConfig.setSatMode(SatMode.FALSIFY);
			
			logger.info("Loading problem specification");
			
			long start = System.currentTimeMillis(); 				
			AndOrGraph graph = new JSONReader().loadAndOrGraph(filename);
			long loadTime = System.currentTimeMillis() - start;
			logger.info("... done in " + loadTime + " ms (" + ((loadTime+500)/1000) + " seconds).");
							
			Map<String,Object> stats = new GraphUtils().analyseGraph(graph);
			logger.info("Graph stats: " + stats);
			 
			//new JSONWriter().write(graph, outputFilepath);
			
			logger.info("Starting MaxSat analysis...");
			MaxSatAnalyser analyser = new MaxSatAnalyser(maxSatConfig);
			MaxSatSolution solution = analyser.solve(graph);
			
			logger.info("MaxSat solution: ");
			solution.display();
			
			
		} catch (Exception e) {
			logger.error(e.getMessage());	
			e.printStackTrace();
		}
		logger.info("=========================");
		logger.info("== " + TCity.TOOL_NAME + " ended at " + new Timestamp(System.currentTimeMillis()) + " ==");
		logger.info("=========================");
		System.exit(0);
	}
	
	
	
	public static void testMPMCS() {				
		Configurator.setRootLevel(Level.INFO);
		
		TCityUtils.loadConfiguration("tcity.conf");				
		String filename = "examples/mpmcs/fps-example.json";
		
		try { 
			MaxSatConfig maxSatConfig = new MaxSatConfig();
			maxSatConfig.setSatMode(SatMode.SATISFY);
			maxSatConfig.setMathMode(MathMode.PROB);
			maxSatConfig.setOptimMode(OptimMode.MAX);
			
			logger.debug("Loading problem specification");
			
			long start = System.currentTimeMillis(); 				
			AndOrGraph graph = new JSONReader().loadAndOrGraph(filename);
			long loadTime = System.currentTimeMillis() - start;
			logger.info("Problem specification loaded in " + loadTime + " ms (" + ((loadTime+500)/1000) + " seconds).");
							
			//Map<String,Object> stats = analyseGraph(graph);
			//logger.info("Graph stats: " + stats);
			 
			//new JSONWriter().write(graph, outputFilepath);
			
			logger.info("Starting MaxSat analysis...");			
			long resolutionStart = System.currentTimeMillis();			
			MaxSatAnalyser analyser = new MaxSatAnalyser(maxSatConfig);
			MaxSatSolution solution = analyser.solve(graph);			
			long resolutionTime = System.currentTimeMillis() - resolutionStart;
			
			String maxSatResolutionTime = resolutionTime + " ms (" + ((resolutionTime+500)/1000) + " seconds)";			
						
			logger.info("MaxSat solution found in " + maxSatResolutionTime);
			solution.display();
			
			
//			logger.info("Starting MaxSat analysis AGAIN...");			
//			solution = analyser.solve(graph);			
//			logger.info("MaxSat solution AGAIN: ");
//			solution.display();
			
			
		} catch (Exception e) {
			logger.error(e.getMessage());	
			if (logger.isTraceEnabled()) {
				e.printStackTrace();
			}
		}
		logger.info("=========================");
		logger.info("== " + TCity.TOOL_NAME + " ended at " + new Timestamp(System.currentTimeMillis()) + " ==");		
		System.exit(0);
	}
	
	
	
}
