package uk.ac.imperial.isst.tcity.config;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */

public interface ConfigKeys {

	//public static final String toolName = "tool.name";
	public static final String dotToolKey = "tools.dot";
	
	public static final String analysisCriticality = "analysis.criticality";
	public static final String analysisHardening = "analysis.hardening";
	public static final String analysisBayesian = "analysis.bayesian";
	

	
	// Solvers
	public static final String useSat4jKey = "solvers.sat4j";
	public static final String useOptimKey = "solvers.optim";
	
	// Python environment
	public static final String pythonPathKey = "python.path";
	public static final String pythonSolverPathKey = "python.solver.path";
	
	// Debug
	public static final String debugEnabled = "tool.debug";
	public static final String traceEnabled = "tool.trace";
		
	// Output	
	public static final String displaySolutionKey = "display.solution";

	
	// -----------------------
	public static final String outputSolKey = "output.sol";
	public static final String outputWcnfKey = "output.wcnf";	
	public static final String outputTxtKey = "output.txt";

	// Output folders
	public static final String outputFolder = "folders.output";
	public static final String outputViewFolder = "folders.view";	
		
	
	
}
