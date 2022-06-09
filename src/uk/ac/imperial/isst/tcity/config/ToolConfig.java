package uk.ac.imperial.isst.tcity.config;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.TCity;

public class ToolConfig {
	
	private static final Logger logger = LogManager.getLogger(ToolConfig.class);

	public static String DOT_TOOL = "/usr/local/bin/dot";
	
	////////////
	
	public static boolean USE_MAX_SAT = true;
	public static boolean USE_OPTIM = false;		
	public static boolean DEBUG = false;
	public static boolean FULL_DEBUG = false;
	public static boolean OUTPUT_WCNF = false;
	public static boolean OUTPUT_SOL = true;	
	public static boolean OUTPUT_TXT = false;
	public static boolean WEIGHTS_AS_PROBABILITIES = true;
	public static boolean MSCS = true;
	public static final int DECIMAL_PLACES = 7;
	public static final int MAX_INT_VALUE = 2000000000;
	
	public static String OUTPUT_FOLDER = "output";
	public static String VIEW_FOLDER = "view";	
	
	private static Properties configProperties;
	
	public static void setupTool(ConfigProperties config) {
		try {		
			configProperties = config.getProperties();
			DOT_TOOL = config.getProperties().getProperty(ConfigKeys.dotToolKey);
			
			DEBUG = Boolean.valueOf(config.getProperties().getProperty(ConfigKeys.debugEnabled));
			FULL_DEBUG = Boolean.valueOf(config.getProperties().getProperty(ConfigKeys.traceEnabled));
			/////
			
			USE_MAX_SAT = Boolean.valueOf(config.getProperties().getProperty(ConfigKeys.useSat4jKey));
			USE_OPTIM = Boolean.valueOf(config.getProperties().getProperty(ConfigKeys.useOptimKey));
			
			OUTPUT_WCNF = Boolean.valueOf(config.getProperties().getProperty(ConfigKeys.outputWcnfKey));
			OUTPUT_SOL = Boolean.valueOf(config.getProperties().getProperty(ConfigKeys.outputSolKey));
			OUTPUT_TXT = Boolean.valueOf(config.getProperties().getProperty(ConfigKeys.outputTxtKey));
			
			OUTPUT_FOLDER = config.getProperties().getProperty(ConfigKeys.outputFolder);
			VIEW_FOLDER = config.getProperties().getProperty(ConfigKeys.outputViewFolder);			
			
			
		} catch (Exception e) {
			logger.error("Error in configuration file => ending " + TCity.TOOL_NAME + " now.");
			System.exit(-1);
		}				
	}

	public static Properties getProperties() {		
		return configProperties;		
	}
}
