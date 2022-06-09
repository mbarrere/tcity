/**
 * 
 */
package uk.ac.imperial.isst.tcity.util.exporters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.config.ToolConfig;


/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class GraphImageExporter extends GraphExporter{
	
	private static final Logger logger = LogManager.getLogger(GraphImageExporter.class);
	
	private String format; // = null;GraphExporterFactory.PDF_EXPORTER;
		
	public GraphImageExporter(String format) {
		this.format = format;
	}
	
	@Override
	public String getFormatExtension () {
		return this.format;
	}
	
	
	@Override
	public String exportGraph (String graphDescriptorFilepath, String outputFilepath) throws FileNotFoundException, UnsupportedEncodingException, Exception {		
		GraphExporter exporter = GraphExporterFactory.getExporter(GraphExporterFactory.DOT_FORMAT);
		String dotFilepath = exporter.exportGraph(graphDescriptorFilepath);
		//throw new UnsupportedEncodingException("PDF exporter not implemented yet");
		
		try {
			//File graphDescriptor = new File(graphDescriptorFilepath);	    				
			File outputFile = new File(outputFilepath);
			//System.out.println("Output path: " + outputFile.getAbsolutePath());
			
			//String command = "/opt/local/bin/dot -Tpdf " + graphDescriptor.getAbsolutePath() + " -o " + outputFile.getAbsolutePath();
			String command = ToolConfig.DOT_TOOL + " -T" + this.format + " " + dotFilepath + " -o " + outputFile.getAbsolutePath();
			logger.debug("Command: " + command);
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
						
			//displayFile(outputFile.getAbsolutePath());
			return outputFile.getAbsolutePath();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}
	
	
}
