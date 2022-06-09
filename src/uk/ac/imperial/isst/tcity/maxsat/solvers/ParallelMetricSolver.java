/**
 * 
 */
package uk.ac.imperial.isst.tcity.maxsat.solvers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.config.ToolConfig;
import uk.ac.imperial.isst.tcity.maxsat.MaxSatSolution;
import uk.ac.imperial.isst.tcity.maxsat.cnf.TseitinStructure;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class ParallelMetricSolver {
	
	private static final Logger logger = LogManager.getLogger(ParallelMetricSolver.class);
	
	public ParallelMetricSolver() {		
	}
	
	public MaxSatSolution solve(AndOrGraph graph, Map<String,Object> stats, TseitinStructure ts) throws InterruptedException, Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(2);

		Callable<MaxSatSolution> maxSatSolver = null;
		if (ToolConfig.USE_MAX_SAT) {
	        maxSatSolver = () -> {	            
	        	Sat4jSolver solver = new Sat4jSolver();
	        	MaxSatSolution m = solver.solve(graph, stats, ts);	        	
	        	logger.debug("[*] MAX-SAT solver finished! " + new Timestamp(System.currentTimeMillis()));	        		        	
	        	return m;            
	        };
		}

        Callable<MaxSatSolution> optimSolver = null;
        if (ToolConfig.USE_OPTIM) {
	        optimSolver = () -> {	        	
	        	MaxSatSolution m = new OptimSolver().solve(graph, stats, ts);	    			        	
	        	logger.debug("[*] Optim solver finished! " + new Timestamp(System.currentTimeMillis()));	        		        	
	        	return m;
	        };
        }
        
        MaxSatSolution solution = null;
        List<Callable<MaxSatSolution>> solvers = new ArrayList<Callable<MaxSatSolution>>();
        List<String> solverNames = new ArrayList<String>();
		try {
			if (ToolConfig.USE_MAX_SAT) {
				solvers.add(maxSatSolver);
				solverNames.add("MaxSAT");
			} 
			if (ToolConfig.USE_OPTIM) {
				solvers.add(optimSolver);
				solverNames.add("OptimLP");				
			}
			
			logger.info("|+| Solvers: " + solverNames);
			
			if (solvers.isEmpty()) {
				throw new Exception("ERROR: no solver has been enabled");
			}
			solution = executorService.invokeAny(solvers);			
			logger.info("Solution found by: " + solution.getSolverId() + " at " + new Timestamp(System.currentTimeMillis()));
			
		} catch (ExecutionException e) {			
			throw e;
		} catch (Exception e) {			
			throw e;
		}        
        
		executorService.shutdown();
		try {
		    if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {		    		    	
		    	executorService.shutdownNow();		    	
		    } 
		} catch (InterruptedException e) {
			executorService.shutdownNow();
		}
        		
        return solution;
	}	
}
