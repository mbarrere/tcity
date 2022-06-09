package uk.ac.imperial.isst.tcity;

import java.sql.Timestamp;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import uk.ac.imperial.isst.tcity.config.ConfigKeys;
import uk.ac.imperial.isst.tcity.config.ToolConfig;
import uk.ac.imperial.isst.tcity.cpag.editor.GraphEditor;
import uk.ac.imperial.isst.tcity.metrics.HardeningAnalysis;
import uk.ac.imperial.isst.tcity.metrics.RiskAnalysis;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.util.GraphUtils;
import uk.ac.imperial.isst.tcity.util.JSONReader;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */

public class TCity {
	
	private static final Logger logger = LogManager.getLogger(TCity.class);
	public static final String TOOL_NAME = "T-CITY";
	public static final String TOOL_VERSION = "v0.64";
	public static final String CONFIG_FILE = "tcity.conf";	
	public static final Double MAX_DOUBLE = Double.MAX_VALUE;
	
	public static void displayHelpMessage(Options options) {
		// return "Usage: java -jar " + TOOL_NAME.toLowerCase() + ".jar inputFile.json [-c configFile]";
		String commandSyntax = "java -jar tcity.jar -f inputFile.json [optional args] | -g";
		new HelpFormatter().printHelp(commandSyntax, options);
	}
		
	public static void main(String[] args) {		
		final Options options = new Options();
		try {
			Configurator.setRootLevel(Level.INFO);
			logger.info("== " + TOOL_NAME + " " + TOOL_VERSION + " ==");		
			logger.info("== Started at " + new Timestamp(System.currentTimeMillis()) + " ==");											
			//runTests();
			//metricTests(args); 
			//TCity.parseToolConfig(args);
			
			CommandLineParser parser = new DefaultParser();
			
			Option inputFileOption = new Option("f", "file", true, "(Mandatory) Input JSON file");
			options.addOption(inputFileOption);
			options.addOption(new Option("d", "debug", false, "Turn on debug mode"));
			options.addOption(new Option("t", "trace", false, "Turn on trace mode"));
			options.addOption(new Option("c", "config", true, "Load custom configuration file"));
			
			Option hardeningOption = new Option("h", "hardening", false, "Enable hardening analysis");
			options.addOption(hardeningOption);			
			Option bayesianOption = new Option("b", "bayesian", false, "Enable Bayesian risk analysis");			
			options.addOption(bayesianOption);
			
			Option displayOption = new Option("x", "display", false, "Enable graph solution display (requires Graphviz/DOT)");			
			options.addOption(displayOption);
			
			Option guiOption = new Option("g", "gui", false, "Enable graphical CPAG editor and analyser");			
			options.addOption(guiOption);
			
			CommandLine line = parser.parse(options, args);
			
			TCity.parseToolConfig(line.getOptionValue("config"));
			
			if( line.hasOption( "debug" ) ) {	
				System.out.println("debug enabled");
				//Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
				Configurator.setRootLevel(Level.DEBUG);
				logger.debug("Debug mode enabled");
			}
			
			if( line.hasOption( "trace" ) ) {
				System.out.println("trace enabled");
				Configurator.setRootLevel(Level.TRACE);
				logger.debug("Trace mode enabled");
				logger.trace("Trace mode enabled");
			}
			
			String filename = null; 
			AndOrGraph graph = null;
			
			boolean commandlineMode = true;			
			
			if ( line.hasOption( "gui" )) {				
				System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
				
		    	SwingUtilities.invokeLater(new Runnable() {
		            @Override
		            public void run() {
		            	new GraphEditor();
		            }
		        });
		    	
			} else {
				if( !line.hasOption( "file" ) && commandlineMode) {
					logger.error("Input file not specified");
				    displayHelpMessage(options);
				    System.exit(-1);
				} 
				
				filename = line.getOptionValue("file");
				//filename = "examples/hardening/scenario1.json";
				logger.info("Input file: " + filename);
				
				logger.info("=> Loading problem specification... ");				
				long start = System.currentTimeMillis(); 				
				graph = new JSONReader().loadAndOrGraph(filename);
				long loadTime = System.currentTimeMillis() - start;
				logger.info(" done in " + loadTime + " ms (" + ((loadTime+500)/1000) + " seconds).");							
				
				if (logger.isDebugEnabled()) {
					new GraphUtils().analyseGraph(graph);
				}
				
				Boolean displayFlag = Boolean.valueOf(ToolConfig.getProperties().getProperty(ConfigKeys.displaySolutionKey));
				if ( line.hasOption( "display" )) {
					displayFlag = true;
				}
				
				Boolean hardeningFlag = Boolean.valueOf(ToolConfig.getProperties().getProperty(ConfigKeys.analysisHardening));
				if( line.hasOption( "hardening" ) || hardeningFlag) {
					logger.info("(*) Hardening enabled");
					
					HardeningAnalysis hardeningAnalysis = new HardeningAnalysis(graph);
					if (displayFlag) hardeningAnalysis.exportAndDisplayInputGraph(filename);
					hardeningAnalysis.compute();
					if (displayFlag) hardeningAnalysis.exportAndDisplayLineGraph(filename);
					if (displayFlag) hardeningAnalysis.exportAndDisplayHardeningSolutionGraph(filename);
				} else {
					logger.info("(*) Hardening analysis not enabled (enable with flag -" + hardeningOption.getOpt() + ")");
				}
				
				Boolean bayesianFlag = Boolean.valueOf(ToolConfig.getProperties().getProperty(ConfigKeys.analysisBayesian));									
				if( line.hasOption( "bayesian" ) || bayesianFlag) {
					logger.info("(*) Bayesian enabled");
					RiskAnalysis riskAnalysis = new RiskAnalysis(graph);
					//RiskAnalysis.testBayesian1();
					riskAnalysis.compute();
					logger.debug("=> Risk: " + riskAnalysis.getRisk());
					if (displayFlag) riskAnalysis.exportAndDisplay(filename);
				} else {
					logger.info("(*) Bayesian risk analysis not enabled (enable with flag -" + bayesianOption.getOpt() + ")");
				}
				
				logger.info("== " + TOOL_NAME + " ended at " + new Timestamp(System.currentTimeMillis()) + " ==");
				System.exit(0);
			}
			
		} 
		catch(ParseException e ) {
			logger.error(e.getMessage());	
			displayHelpMessage(options);			
			//e.printStackTrace();		
		} 
		catch (Exception e) {
			logger.error(e.getMessage());	
			e.printStackTrace();
		}
	}
	
	
	
	public static void parseToolConfig(String configFilepath) throws Exception{
		TCityUtils.loadConfiguration(configFilepath);
		
		Boolean debugEnabled = Boolean.valueOf(ToolConfig.getProperties().getProperty(ConfigKeys.debugEnabled));
		if (debugEnabled) {
			Configurator.setRootLevel(Level.DEBUG);
		}
		Boolean traceEnabled = Boolean.valueOf(ToolConfig.getProperties().getProperty(ConfigKeys.traceEnabled));
		if (traceEnabled) {
			Configurator.setRootLevel(Level.TRACE);
		}			
	}
	
	
	
	
	
	
	
	
	public static void metricTests(String[] args)throws Exception {

		//String filepath = "examples/hardening/hardening-ex1.json";
		String filepath = "examples/hardening/scenario1.json";
		//String filepath = "examples/bn/scenario1.json";
		String[] arguments = {filepath};			
		//String[] arguments = args;			
		TCity.parseToolConfig("tcity.conf");
		//Configurator.setRootLevel(Level.INFO);
		
		String filename = null;					
		
		if (arguments.length == 0) {
			logger.error("Please indicate the JSON specification file");				
		} else {
			filename = arguments[0];
			logger.debug("Filename: " + filename);				
			
			logger.info("=> Loading problem specification... ");				
			long start = System.currentTimeMillis(); 				
			AndOrGraph graph = new JSONReader().loadAndOrGraph(filename);
			long loadTime = System.currentTimeMillis() - start;
			logger.info(" done in " + loadTime + " ms (" + ((loadTime+500)/1000) + " seconds).");
							
			new GraphUtils().analyseGraph(graph);				
			//solveWithTseitinAndDisplaySolution(problem, config);
			
			Boolean bayesianFlag = Boolean.valueOf(ToolConfig.getProperties().getProperty(ConfigKeys.analysisBayesian));
			logger.debug("=> Bayesian anaysis enabled: " + bayesianFlag);
			if (bayesianFlag) {					
				RiskAnalysis riskAnalysis = new RiskAnalysis(graph);
				//RiskAnalysis.testBayesian1();
				riskAnalysis.compute();
				logger.debug("=> Risk: " + riskAnalysis.getRisk());
				riskAnalysis.exportAndDisplay(filename);
			}
			
			Boolean hardeningFlag = Boolean.valueOf(ToolConfig.getProperties().getProperty(ConfigKeys.analysisHardening));
			logger.debug("=> Hardening anaysis enabled: " + hardeningFlag);
			if (hardeningFlag) {										
				HardeningAnalysis hardeningAnalysis = new HardeningAnalysis(graph);
				hardeningAnalysis.exportAndDisplayInputGraph(filename);
				hardeningAnalysis.compute();
				hardeningAnalysis.exportAndDisplayLineGraph(filename);
				hardeningAnalysis.exportAndDisplayHardeningSolutionGraph(filename);
			}
			
			Boolean criticalityFlag = Boolean.valueOf(ToolConfig.getProperties().getProperty(ConfigKeys.analysisCriticality));
			logger.debug("=> Criticality anaysis enabled: " + criticalityFlag);
			if (criticalityFlag) {
				logger.warn("=> Criticality anaysis not implemented yet");
			}
		}
	}
	
	public static void runTests() {
		TCityTests.testMain1();
		TCityTests.testBayesian1();
		TCityTests.testTseitin();
		TCityTests.testMPMCS();
		System.exit(0);		
	}
	
	
}
