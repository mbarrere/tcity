/**
 * 
 */
package uk.ac.imperial.isst.tcity.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.imperial.isst.tcity.cpag.editor.CPAGContainer;
import uk.ac.imperial.isst.tcity.cpag.editor.GraphContainer;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class JSONWriter {

	public JSONWriter() {		
	}
		
	public void write(AndOrGraph graph, String outputFilepath) throws JsonProcessingException, FileNotFoundException, UnsupportedEncodingException {
		
		ObjectMapper mapper = new ObjectMapper();		 
		//String json = mapper.writeValueAsString(problem.getGraph());
		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(graph); //rootNode.asText();				
		 
		File outputFile = new File(outputFilepath);			
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");		
		writer.println(json);						
		writer.close();		
	}
	
	public void writeCPAG(CPAGContainer cpagContainer, String outputFilepath) throws JsonProcessingException, FileNotFoundException, UnsupportedEncodingException {
		
		ObjectMapper mapper = new ObjectMapper();		 
		//String json = mapper.writeValueAsString(problem.getGraph());
		//String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(graph); //rootNode.asText();				
		
		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cpagContainer); //rootNode.asText();
		 
		File outputFile = new File(outputFilepath);			
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");		
		writer.println(json);						
		writer.close();		
	}
	
	public void writeCPAG(GraphContainer graphContainer, String outputFilepath) throws JsonProcessingException, FileNotFoundException, UnsupportedEncodingException {
		
		ObjectMapper mapper = new ObjectMapper();		 
		//String json = mapper.writeValueAsString(problem.getGraph());
		//String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(graph); //rootNode.asText();				
		
		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(graphContainer); //rootNode.asText();
		 
		File outputFile = new File(outputFilepath);			
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");		
		writer.println(json);						
		writer.close();		
	}

}
