/**
 * 
 */
package uk.ac.imperial.isst.tcity.util.exporters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.util.JSONReader;



/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class DOTGraphExporter extends GraphExporter{

	private static final Logger logger = LogManager.getLogger(DOTGraphExporter.class);
	
	private final String EXTENSION = GraphExporterFactory.DOT_FORMAT;
	
	public static String size = "8.5,11";
	//public static String size = "7.75,10.25";
	//public static String size = "6.375,8.25";
	//public static String size = "4.2,5.94";
	//public static String size = "1.05,1.485";
	//public static String size = "21.0,29.7";
	public static String ratio = "compress";
	public static String rankdir = "TD"; //LR
	public static String nodefontsize = "24"; //"36"; 
	public static String edgefontsize = "18"; //"28";
	public static String penwidth = "2.0";
	public static String splines = "spline"; //none, line, curved, ortho, polyline, spline

	@Override
	public String getFormatExtension () {
		return this.EXTENSION;
	}
	
	private String getHeader() {
		String header = "digraph G { ";  
		//header += "rankdir=LR; "; //for nodes with shape record (organize vertically).
		//header += "rankdir=" + rankdir + "; "; //for nodes with shape record (organize vertically).
		//graph [layout = dot|neato|twopi|circo]
		//header += "graph [layout = dot]; ";
		header += "graph [layout = dot, dpi = 300]; ";
		//header += "size=\"6.5,8\"; "; 
		header += "size=\"" + size + "\"; ";
		//header += "splines=\"line\"; ";				 
		//header += "splines=" + splines + "; ";
		header += "ratio=" + ratio + "; ";
		//header += "ratio=compress; ";
		//header += "ratio=fill; ";
		//header += "ratio=0.6; ";
		//header += "nodesep=0.5; ";
		//header += "node[fontsize=12,shape=box,style=rounded];";
		//header += "node[fontsize=16,penwidth=2,shape=box];";
		header += "node[fontsize=" + nodefontsize + ",shape=box];";
		//header += "edge[weight=0.5, dir=forward,style=\"setlinewidth(1.0)\"];";		
		//header += "edge[fontsize=" + edgefontsize + ",dir=forward,style=\"setlinewidth(10.0)\"];";
		header += "edge[fontsize=" + edgefontsize + ",dir=forward, penwidth=2.0];";
		return header;
	}
	
		
	
	private String generateGraphTag(AndOrGraph graph) {
		
		String tag = "";
		boolean ignoreEdgeLabel = false;
		//DecimalFormat df = new DecimalFormat("0.00");
		
		List<AndOrEdge> edges = graph.getEdges(); 
		
		for (AndOrEdge edge : edges) {
			logger.trace("Link between " + edge.getSource() + " and " + edge.getTarget());
			String sourceId = edge.getSource().replace('-', '_');
			String targetId = edge.getTarget().replace('-', '_');
			
			tag += sourceId + "->" + targetId; // + "[];\n";			
			if (ignoreEdgeLabel) {
				tag += "[];\n";
			} else {
				tag += "[";				
				//String label = edge.getLabel();				
				String label = edge.getValue();
				label = "label=\"" + label + "\"";				
				tag += label + "];\n";
			}												
		}
		
		return tag;				
	}
	
	private String getFooter() {		
		return "}";
	}
	
	
	/**
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException * ***/
	public String writeGraphToFile (AndOrGraph graph, String outputFilepath) throws FileNotFoundException, UnsupportedEncodingException {
		logger.info("Writing AndOrGraph to DOT file");
		File outputFile = new File(outputFilepath);
				
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
		
		writer.println(this.getHeader());			
		writer.println(this.generateGraphTag(graph));
		writer.println(this.getFooter());
				
		writer.close();
		//logger.info("CoreGraphOnlyProbabilities exportation finished");
		return outputFile.getAbsolutePath();
	}
	
	public String exportGraph (String graphDescriptorFilepath, String outputFilepath) throws FileNotFoundException, UnsupportedEncodingException, Exception {
		AndOrGraph graph = new JSONReader().loadAndOrGraph(graphDescriptorFilepath);
		return this.writeGraphToFile(graph, outputFilepath);
	}
		/*
	public String exportGraph (String graphDescriptorFilepath) throws FileNotFoundException, UnsupportedEncodingException, Exception {
		AndOrGraph graph = new JSONReader().loadAndOrGraph(graphDescriptorFilepath);
		
		File graphDescriptor = new File(graphDescriptorFilepath);    	
    	String extension = this.getFormatExtension(); 
		String name = graphDescriptor.getName().substring(0, graphDescriptor.getName().lastIndexOf("."));
		String outputFilepath = graphDescriptor.getParentFile().getPath() + File.separator + name + "." + extension;
		
		return this .writeGraphToFile(graph, outputFilepath);
	}*/
	
	public void exportJSONGraphAndDisplayPNG (String graphDescriptorFilepath) throws FileNotFoundException, UnsupportedEncodingException, Exception {
			
		File graphDescriptor = new File(graphDescriptorFilepath);    	
    	String extension = "png"; 
		String name = graphDescriptor.getName().substring(0, graphDescriptor.getName().lastIndexOf("."));
		String outputFilepath = graphDescriptor.getParentFile().getPath() + File.separator + name + "." + extension;
		
		String output = new GraphImageExporter(extension).exportGraph(graphDescriptorFilepath, outputFilepath);
		GraphDisplay.showImage(output);
	}
}
