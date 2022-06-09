/**
 * 
 */
package uk.ac.imperial.isst.tcity.maxsat.solvers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.imperial.isst.tcity.config.ConfigKeys;
import uk.ac.imperial.isst.tcity.config.ToolConfig;
import uk.ac.imperial.isst.tcity.maxsat.MaxSatSolution;
import uk.ac.imperial.isst.tcity.maxsat.cnf.TseitinFileExporter;
import uk.ac.imperial.isst.tcity.maxsat.cnf.TseitinStructure;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class OptimSolver {    
	
	private static final Logger logger = LogManager.getLogger(OptimSolver.class);
    
	public static String SOLVER_ID = "OPTIM-LP-SOLVER";
    private String pythonSolverPath;   
    private String pythonPath; 
    
    public OptimSolver() {
		super();
		this.pythonSolverPath = ToolConfig.getProperties().getProperty(ConfigKeys.pythonSolverPathKey);
		this.pythonPath = ToolConfig.getProperties().getProperty(ConfigKeys.pythonPathKey);
	}
    
    public MaxSatSolution solve(AndOrGraph graph, Map<String,Object> stats, TseitinStructure ts) throws Exception {
    	String tempFile = ".optim-input.tmp";
		FileOutputStream pOutputFileStream = new FileOutputStream(new File(tempFile));    		
		
		TseitinFileExporter exporter = new TseitinFileExporter(ts);
		exporter.toStream(pOutputFileStream);	

		OptimSolution optimSolution = this.runSolver(tempFile);
		if (logger.isDebugEnabled()) {
			System.out.println(optimSolution);
		}
    	return this.transformOptimSolution(optimSolution, graph, ts);	
    }

	private OptimSolution runSolver(String inputFile) throws Exception{
		if (logger.isDebugEnabled()) {
			System.out.println("[*] Optim solver started! " + new Timestamp(System.currentTimeMillis()));
		}
    	
		File pythonScript = new File(this.pythonSolverPath); 
		
		String s = null;
        try {
        	ProcessBuilder pb = new ProcessBuilder(pythonPath, pythonScript.getAbsolutePath(), inputFile);  
        	Process p = pb.start();
        	
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command            
            StringBuilder inBuilder = new StringBuilder();
            while ((s = stdInput.readLine()) != null) {
            	inBuilder.append(s);
                //System.out.println(s);
            }
            //System.out.println("OUT:");
            String pythonOutput = inBuilder.toString();
            //System.out.println("Python output:"); System.out.println(pythonOutput);
            
            ObjectMapper mapper = new ObjectMapper();                       			            
            OptimSolution optimSolution = mapper.readValue(pythonOutput, OptimSolution.class);	            
            //System.out.println(optimSolution);
            
            // read any errors from the attempted command            
            StringBuilder errBuilder = new StringBuilder();
            while ((s = stdError.readLine()) != null) {
            	errBuilder.append(s);
                //System.out.println(s);
            }
            //System.out.println("ERR:");
            if (!errBuilder.toString().isEmpty()) {
            	System.out.println(errBuilder.toString());
            }            
            return optimSolution;
        }
        catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
        	throw new Exception ("Python-based optimiser did not output a proper JSON solution. Check paths and external optimiser licences. ");
        }
        catch (IOException e) {
            
            if (logger.isTraceEnabled()) {
            	System.out.println("Exception:");
            	e.printStackTrace();
            }
            throw new Exception ("IOException error. Message: " + e.getMessage());
            //OptimSolution err = new OptimSolution();
            //err.setStatus(e.getMessage());
            //return err;
        }
    }
	
	private MaxSatSolution transformOptimSolution(OptimSolution optimSolution, AndOrGraph graph, TseitinStructure ts) throws Exception {		
		MaxSatSolution solution = new MaxSatSolution(graph);
		solution.setSolverId(OptimSolver.SOLVER_ID);
		solution.setCost(optimSolution.getCost());
			
		Map<Integer, Object> varNameMap = ts.getTseitinVisitor().getVarNameMap();
		
		Map<String,Double> optimSol = optimSolution.getSolution();
		if (optimSol != null && optimSol.size() > 0) {					
			for (Entry<String,Double> e : optimSol.entrySet()) {
				String literalIndex = e.getKey().substring(2);
				Integer index = Integer.parseInt(literalIndex);
				String nodeName = String.valueOf(varNameMap.get(index));
				//long value = new Double(e.getValue()).longValue();
				//System.out.println("index: " + index + ", nodeName=" + nodeName + ", value=" + value);
				
				AndOrNode node = graph.getNode(nodeName);
				if (node != null) {
					solution.getNodes().add(node);
				} 
			}
		}
		
		return solution;
	}
}
