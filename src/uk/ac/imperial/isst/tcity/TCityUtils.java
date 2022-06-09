package uk.ac.imperial.isst.tcity;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.config.ConfigProperties;
import uk.ac.imperial.isst.tcity.config.ToolConfig;

public class TCityUtils {
	
	private static final Logger logger = LogManager.getLogger(TCityUtils.class);
	
	public static void loadConfiguration() {
		loadConfiguration(null);		
	}
	
	public static void loadConfiguration(String configFilePath) {
		ConfigProperties config = new ConfigProperties(); 
		config.loadDefaultSetings();  
		config.tryLoadDefaultFromFile(TCity.CONFIG_FILE);
		//config.printProperties();				
		ToolConfig.setupTool(config);				
		
		
		if (configFilePath != null) {
			logger.debug("=> Loading custom configuration file '" + configFilePath + "'... ");
			try { 
				long start = System.currentTimeMillis(); 								
				config.loadConfigFromFile(configFilePath);
				//config.printProperties();
				ToolConfig.setupTool(config);
				long loadTime = System.currentTimeMillis() - start;
				logger.info("Configuration loaded in " + loadTime + " ms (" + ((loadTime+500)/1000) + " seconds)");
			} catch (IOException e) {
				logger.warn("Configuration file '" + configFilePath + "' not found => using default settings.");
			}		
		}		
	}
}
