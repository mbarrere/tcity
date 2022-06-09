package uk.ac.imperial.isst.tcity.maxsat;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.config.ToolConfig;
import uk.ac.imperial.isst.tcity.maxsat.cnf.TseitinStructure;
import uk.ac.imperial.isst.tcity.maxsat.cnf.TseitinTransformer;
import uk.ac.imperial.isst.tcity.maxsat.config.MathMode;
import uk.ac.imperial.isst.tcity.maxsat.config.MaxSatConfig;
import uk.ac.imperial.isst.tcity.maxsat.config.OptimMode;
import uk.ac.imperial.isst.tcity.maxsat.config.SatMode;
import uk.ac.imperial.isst.tcity.maxsat.solvers.ParallelMetricSolver;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.util.GraphUtils;

public class MaxSatAnalyser {
	
	final static Logger logger = LogManager.getLogger(MaxSatAnalyser.class);
	
	private MaxSatConfig config;
	
	public MaxSatAnalyser(MaxSatConfig config) {
		this.config = config;
	}
	
	
	public MaxSatConfig getConfig() {
		return config;
	}

	public void setConfig(MaxSatConfig config) {
		this.config = config;
	}

	public MaxSatSolution solve (AndOrGraph graph) throws Exception{
		
		boolean supportedConfig = false;
		
		if (this.config.getSatMode().equals(SatMode.SATISFY) &&
			this.config.getMathMode().equals(MathMode.PROB) &&
			this.config.getOptimMode().equals(OptimMode.MAX)) 
			{
				supportedConfig = true;
				// Fault Tree Analysis: Identifying Maximum Probability Minimal Cut Sets with MaxSAT 
				// https://doi.org/10.1109/DSN-S50200.2020.00029
				logger.debug("Launching MPMCS4FTA resolution process");	
				graph = new LogicGateTransformer().flip(graph);
				graph = new ProbLogTransformer().toLogValues(graph);
			}
		
		if (this.config.getSatMode().equals(SatMode.FALSIFY) &&
				this.config.getMathMode().equals(MathMode.PROB) &&
				this.config.getOptimMode().equals(OptimMode.MAX)) 
			{
					supportedConfig = true;
					logger.debug("Launching LDA4CPS resolution process");			
			}
		
		if (this.config.getSatMode().equals(SatMode.FALSIFY) &&
				this.config.getMathMode().equals(MathMode.LINEAR) &&
				this.config.getOptimMode().equals(OptimMode.MIN)) 
			{
					supportedConfig = true;
					logger.debug("Launching META4ICS resolution process");			
			}
		
		if (!supportedConfig) {
			throw new Exception("MaxSat configuration not supported yet");
		}
		
		
		Map<String,Object> stats = this.analyseGraph(graph);

		logger.debug("----------------------------------");
		logger.info("Problem source: " + graph.getSource());
		logger.info("Problem target: " + graph.getTarget());
				
		logger.debug("----------------------------------");

		logger.info("=> Performing Tseitin transformation... ");
		
		long start = System.currentTimeMillis();
		TseitinStructure ts = new TseitinTransformer().transformGraphWithTseitin(graph, stats);
		long tsTime = System.currentTimeMillis() - start;
		logger.info(" done in " + tsTime + " ms (" + ((tsTime+500)/1000) + " seconds).");
		
		
		//testing
		//Sat4jSolver solver = new Sat4jSolver();
		//MaxSatSolution solution = solver.solve(graph, stats, ts);
		
		ParallelMetricSolver parallelSolver = new ParallelMetricSolver();
		MaxSatSolution solution = parallelSolver.solve(graph, stats, ts);
		
		if (this.config.getSatMode().equals(SatMode.SATISFY) &&
				this.config.getMathMode().equals(MathMode.PROB) &&
				this.config.getOptimMode().equals(OptimMode.MAX)) 
				{					
					logger.debug("[MPMCS mode] Reverting graph changes and updating solution");	
					graph = new LogicGateTransformer().flip(graph);
					graph = new ProbLogTransformer().fromLogValues(graph);
					
					/*
					 
					 // MSCS 		
						metric.setCost(GraphUtils.round(Math.exp(-1 * metric.getCost()), CpsMetricAnalyser.DECIMAL_PLACES));
				 		
						for (AndOrNode node : problem.getGraph().getNodes()) {
							if (node.isAtomicType()) {	 			
								if (node.isGoalOrFunction() || node.isInitType()) { 					
				 					node.setValue("none"); 					
				 				} else {
				 					node.setValue(node.getOriginalValue());
				 				}
					 		} else {
					 			if (node.isAndType()){		 		
					 				node.setType("or");
					 			} else {
						 			if (node.isOrType()){		 		
						 				node.setType("and");
						 			}
					 			}
					 		}
							
						}
		
					 */
					
					solution.setCost(GraphUtils.round(Math.exp(-1 * solution.getCost()), ToolConfig.DECIMAL_PLACES));										
				}
		//FIXME 
		// re-adapt solution for the other cases (uses classes instead of if-then cases
		
		//solution.display();
		//end testing
		
		
		/*
		long metricStart = System.currentTimeMillis();
		SecurityMetric m = null;
		try {
			// Optim solver requires further work when dealing with multiple measures
			if (problem.involvesMultipleMeasures()) {
				CpsMetricAnalyser.USE_OPTIM = false;
			}
			m = new ParallelMetricSolver().solve(problem, stats, ts, config);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println("== " + TOOL_NAME + " ended at " + new Timestamp(System.currentTimeMillis()) + " ==");
			System.exit(0);
		}
		long metricTime = System.currentTimeMillis() - metricStart;

		logger.info("\n==================================");		
		logger.info("=> BEST solution found by " + m.getSolverId() + " for:");
		logger.info("Source: " + graph.getSource());
		logger.info("Target: " + graph.getTarget());
		
		//updateNodeLabelsWithMeasureValues(m, problem);
		
		// MAX_PROBS 
 		//if (CpsMetricAnalyser.WEIGHTS_AS_PROBABILITIES) {
		if (CpsMetricAnalyser.MSCS) {
 			updateProblemSpecAndMetric(m,problem);
 			//System.out.println("Joint probability of failure: " + GraphUtils.round(Math.exp(-1 * cost), CpsMetricAnalyser.DECIMAL_PLACES));
 			//System.out.println("CUT cost: " + cost);
 		}
 		
		m.display();
			
		ts.setSecurityMetric(m);
		ts.setExecutionTime(metricTime);
		
		System.out.println("[*] Metric computation time: " + metricTime + " ms (" + ((metricTime+500)/1000) + " seconds).");
		System.out.println("==================================");
		*/
		
		//ProblemSolution sol = new ProblemSolution(problem, m);		
		//if (OUTPUT_SOL) { outputSolution(ts, problem, m, CpsMetricAnalyser.VIEW_FOLDER, sol); }
		//if (OUTPUT_WCNF) { outputWCNF(ts, problem, m); }		
		//if (OUTPUT_TXT) { outputTxt(ts, problem, m); }
		//return sol;
				
		return solution;
	}
	
	private Map<String,Object> analyseGraph(AndOrGraph g) {
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
}
