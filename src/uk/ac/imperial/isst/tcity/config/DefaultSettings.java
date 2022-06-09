package uk.ac.imperial.isst.tcity.config;

import java.util.Properties;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */

public class DefaultSettings {
	
	public static final String dotToolPath = "/usr/local/bin/dot";
	
	private static final String pythonPath = "/usr/local/bin/python3";
	private static final String pythonSolverPath = "python/optim.py";
	
	private static final String folderOutput = "output";   
	private static final String folderView = "view";
    
	public DefaultSettings() {
		
	}
	
	public void loadProperties(Properties prop) {
		prop.setProperty(ConfigKeys.dotToolKey, dotToolPath);
		
		
		prop.setProperty(ConfigKeys.useSat4jKey, String.valueOf(true));
		prop.setProperty(ConfigKeys.useOptimKey, String.valueOf(false));
		prop.setProperty(ConfigKeys.pythonPathKey, pythonPath);
		prop.setProperty(ConfigKeys.pythonSolverPathKey, pythonSolverPath);
		
		prop.setProperty(ConfigKeys.analysisHardening, String.valueOf(false));
		prop.setProperty(ConfigKeys.analysisBayesian, String.valueOf(false));
		prop.setProperty(ConfigKeys.analysisCriticality, String.valueOf(false));
		
		prop.setProperty(ConfigKeys.debugEnabled, String.valueOf(false));
		prop.setProperty(ConfigKeys.traceEnabled, String.valueOf(false));
		
		prop.setProperty(ConfigKeys.displaySolutionKey, String.valueOf(false));
		//////
		
		
		prop.setProperty(ConfigKeys.outputSolKey, String.valueOf(false));
		prop.setProperty(ConfigKeys.outputWcnfKey, String.valueOf(false));
		prop.setProperty(ConfigKeys.outputTxtKey, String.valueOf(false));
		
		prop.setProperty(ConfigKeys.outputFolder, folderOutput);
		prop.setProperty(ConfigKeys.outputViewFolder, folderView);		
							
	}

}
