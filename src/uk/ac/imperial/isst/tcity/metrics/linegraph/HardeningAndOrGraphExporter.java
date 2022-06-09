/**
 * 
 */
package uk.ac.imperial.isst.tcity.metrics.linegraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.config.ToolConfig;
import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.util.exporters.GraphExporterFactory;



/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class HardeningAndOrGraphExporter {

	private static final Logger logger = LogManager.getLogger(HardeningAndOrGraphExporter.class);
	
	private final String extension = GraphExporterFactory.PNG_FORMAT;
	
	public static String size = "8.5,11";
	//public static String size = "7.75,10.25";
	//public static String size = "6.375,8.25";
	//public static String size = "4.2,5.94";
	//public static String size = "1.05,1.485";
	//public static String size = "21.0,29.7";
	public static String ratio = "compress";
	public static String rankdir = "TD"; //LR
	public static String nodefontsize = "22"; //"36"; 
	public static String edgefontsize = "18"; //"28";
	public static String penwidth = "2.0";
	public static String splines = "spline"; //none, line, curved, ortho, polyline, spline

	public String getFormatExtension () {
		return this.extension;
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
	
		
	
	private String generateGraphTag(AndOrGraph graph, List<AndOrEdge> solutionEdges) {
				
		if (solutionEdges == null) {
			solutionEdges = new ArrayList<AndOrEdge>();
		} 
		
		String tag = "";
		boolean ignoreEdgeLabel = false;
		//DecimalFormat df = new DecimalFormat("0.00");
		
		
		String nodeDefinition = "";		
		//DecimalFormat df = new DecimalFormat("0.0000");
		//DecimalFormat dfBeliefs = new DecimalFormat("0.0");
		
		for (AndOrNode n : graph.getNodes()) {
			String nodeId = n.getId().replace('-', '_');
			nodeDefinition += nodeId + " ";
			if (n.isLogicType()) {
				nodeDefinition += "[label=\"" + n.getType() + "\"];\n";	
			} else {	
				//AndOrEdge originalEdge = nodeEdgeMapping.get(n);
				//String src = originalEdge.getSource();
				//String tgt = originalEdge.getTarget(); 
				
				String label = n.getLabel().replace('-', '_');				
				//String label = src + "--\\>" + tgt; 
				
				nodeDefinition += "[label = \"" + label + "\"";
				//nodeDefinition += "[label = \"" + label + "| { compromise |{T=" + c + "|F=" + d + "}}\""; //use this for txt9
				//nodeDefinition += "[label = \"" + label + "| {" + n.getValue() + "}\""; 
				//nodeDefinition += "[label = \"" + label + "| { " +  n.getValue() + " |{src=" + src + "|tgt=" + tgt + "}}\""; 
				//nodeDefinition += "[label = \"" + label + "| { {src=" + src + "|tgt=" + tgt + "} | {" +  n.getValue() + "}}\""; 
				//nodeDefinition += "[label = \"" + label + "| { {" + src + " (AT) | " + tgt + "(LG)} | {" +  n.getValue() + "}}\"";
				//nodeDefinition += ", fillcolor=\"#9cd6ff\", color=\"blue\", style=\"filled\", shape=record];\n";
				nodeDefinition += ", fillcolor=\"#e6fff0\", color=\"#bae3cb\", style=\"filled\", shape=record];\n";
				
			}			
			//											
		}		
		
		
		
		List<AndOrEdge> edges = graph.getEdges(); 
		
		for (AndOrEdge edge : edges) {
			logger.trace("Link between " + edge.getSource() + " and " + edge.getTarget());
			String sourceId = edge.getSource().replace('-', '_');
			String targetId = edge.getTarget().replace('-', '_');
			
			tag += sourceId + "->" + targetId; // + "[];\n";
			if (ignoreEdgeLabel) {
				tag += "[];\n";
			} else {
				AndOrNode src = graph.getNode(edge.getSource()); 
				if (src.isAndType() || src.isOrType()) {
					tag += "[];\n";
				} else {
					tag += "[";				
					//String label = "";
					//String label = edge.getLabel();				
					String label = edge.getLabel().replace('-', '_') + " (" + edge.getValue() + ")";
					if (solutionEdges.contains(edge)) {
						label = "label=\"" + label + "\"" + ", fillcolor=\"#fc2f21\", color=\"#ff5447\", style=\"dashed\", fontcolor=\"#fc2f21\"";
						
					} else {
						label = "label=\"" + label + "\"";
					}
					tag += label + "];\n";
				}
			}												
		}
		
		//return tag;
		return nodeDefinition + tag;
	}
	
	private String getFooter() {		
		return "}";
	}
	
	
	/**
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException * ***/
	public String writeGraphToFile (AndOrGraph graph, String outputFilepath, List<AndOrEdge> solutionEdges) throws FileNotFoundException, UnsupportedEncodingException {
		logger.info("Writing Graph to DOT file");
		File outputFile = new File(outputFilepath);
				
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
		
		writer.println(this.getHeader());			
		writer.println(this.generateGraphTag(graph, solutionEdges));
		writer.println(this.getFooter());
				
		writer.close();
		//logger.info("CoreGraphOnlyProbabilities exportation finished");
		return outputFile.getAbsolutePath();
	}
	
	public String exportGraph (AndOrGraph graph, String format, String inputGraphDescriptorFilepath) throws FileNotFoundException, UnsupportedEncodingException, Exception {
		//AndOrGraph graph = new JSONReader().loadAndOrGraph(graphDescriptorFilepath);
		try {
			String nameDecorator = "-hrd-in."; 
			
			File graphDescriptor = new File(inputGraphDescriptorFilepath);
			String extension = GraphExporterFactory.DOT_FORMAT; 
			String name = graphDescriptor.getName().substring(0, graphDescriptor.getName().lastIndexOf("."));
			String dotFilepath = graphDescriptor.getParentFile().getPath() + File.separator + name + nameDecorator + extension;			
			dotFilepath = this.writeGraphToFile(graph, dotFilepath, null);
			
			//GraphExporter exporter = GraphExporterFactory.getExporter(GraphExporterFactory.DOT_FORMAT);
			String imageFilepath = graphDescriptor.getParentFile().getPath() + File.separator + name + nameDecorator + format;
					
			//throw new UnsupportedEncodingException("PDF exporter not implemented yet");
			
		
			//File graphDescriptor = new File(graphDescriptorFilepath);	    				
			File outputImageFile = new File(imageFilepath);
			//System.out.println("Output path: " + outputFile.getAbsolutePath());
			
			//String command = "/opt/local/bin/dot -Tpdf " + graphDescriptor.getAbsolutePath() + " -o " + outputFile.getAbsolutePath();
			String command = ToolConfig.DOT_TOOL + " -T" + format + " " + dotFilepath + " -o " + outputImageFile.getAbsolutePath();
			logger.debug("Command: " + command);
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
						
			//displayFile(outputFile.getAbsolutePath());
			return outputImageFile.getAbsolutePath();			
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
		
	public String exportSolutionGraph (AndOrGraph graph, String format, String inputGraphDescriptorFilepath, List<AndOrEdge> solutionEdges) throws FileNotFoundException, UnsupportedEncodingException, Exception {
		//AndOrGraph graph = new JSONReader().loadAndOrGraph(graphDescriptorFilepath);
		try {
			String nameDecorator = "-hrd-sol.";
			
			File graphDescriptor = new File(inputGraphDescriptorFilepath);
			String extension = GraphExporterFactory.DOT_FORMAT; 
			String name = graphDescriptor.getName().substring(0, graphDescriptor.getName().lastIndexOf("."));
			String dotFilepath = graphDescriptor.getParentFile().getPath() + File.separator + name + nameDecorator + extension;			
			dotFilepath = this.writeGraphToFile(graph, dotFilepath, solutionEdges);
			
			//GraphExporter exporter = GraphExporterFactory.getExporter(GraphExporterFactory.DOT_FORMAT);
			String imageFilepath = graphDescriptor.getParentFile().getPath() + File.separator + name + nameDecorator + format;
					
			//throw new UnsupportedEncodingException("PDF exporter not implemented yet");
			
		
			//File graphDescriptor = new File(graphDescriptorFilepath);	    				
			File outputImageFile = new File(imageFilepath);
			//System.out.println("Output path: " + outputFile.getAbsolutePath());
			
			//String command = "/opt/local/bin/dot -Tpdf " + graphDescriptor.getAbsolutePath() + " -o " + outputFile.getAbsolutePath();
			String command = ToolConfig.DOT_TOOL + " -T" + format + " " + dotFilepath + " -o " + outputImageFile.getAbsolutePath();
			logger.debug("Command: " + command);
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
						
			//displayFile(outputFile.getAbsolutePath());
			return outputImageFile.getAbsolutePath();			
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
