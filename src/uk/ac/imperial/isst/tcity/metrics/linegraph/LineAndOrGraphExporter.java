/**
 * 
 */
package uk.ac.imperial.isst.tcity.metrics.linegraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.config.ToolConfig;
import uk.ac.imperial.isst.tcity.maxsat.MaxSatSolution;
import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.util.exporters.GraphExporterFactory;



/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class LineAndOrGraphExporter {

	private static final Logger logger = LogManager.getLogger(LineAndOrGraphExporter.class);
	
	private final String extension = GraphExporterFactory.PNG_FORMAT;
	
	public static String size = "8.5,11";	
	//public static String size = "7.75,10.25";
	//public static String size = "6.375,8.25";
	//public static String size = "4.2,5.94";
	//public static String size = "1.05,1.485";
	//public static String size = "21.0,29.7";
	public static String ratio = "compress";
	//public static String ratio = "fill";
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
		//  graph [pad="0.5", nodesep="1", ranksep="2"];
		header += "graph [layout = dot, dpi = 300]; ";
		//header += "graph [layout = dot, dpi = 300, pad=\"0.5\", nodesep=\"1\", ranksep=\"2\"]; ";
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
	
		
	
	private String generateGraphTag(LineAndOrGraph lgraph, MaxSatSolution solution) {
		
		AndOrGraph graph = lgraph.getAndOrGraph();
		Map<AndOrNode, AndOrEdge> nodeEdgeMapping = lgraph.getNodeEdgeMapping(); 
		List<AndOrNode> solutionNodes = solution.getNodes();
		String tag = "";
		boolean ignoreEdgeLabel = false;
		//DecimalFormat df = new DecimalFormat("0.00");
		
		
		String nodeDefinition = "";		
		//DecimalFormat df = new DecimalFormat("0.0000");		
		
		for (AndOrNode n : lgraph.getAndOrGraph().getNodes()) {
			String nodeId = n.getId().replace('-', '_');
			nodeDefinition += nodeId + " ";
			if (n.isLogicType()) {
				nodeDefinition += "[label=\"" + n.getType() + "\"];\n";	
			} else {				
				String label = "";
				AndOrEdge originalEdge = nodeEdgeMapping.get(n);
				if (originalEdge != null) {
					String src = originalEdge.getSource();
					String tgt = originalEdge.getTarget(); 
					
					String srcLabel = lgraph.getOriginalGraph().getNode(src).getLabel();
					String tgtLabel = lgraph.getOriginalGraph().getNode(tgt).getLabel();
					//String label = "{ {" + srcLabel + "|" + tgtLabel + "} } ";
					label = srcLabel + "--\\>" + tgtLabel;
					//nodeDefinition += "[label = \"" + label + "| { {" + n.getLabel() + "} | {" +  n.getValue() + "}}\"";
					if (solutionNodes.contains(n)) {
						nodeDefinition += "[label = \"" + label + "| { {" + n.getLabel() + "} | {" +  n.getValue() + " (*)}}\"";
						nodeDefinition += ", fillcolor=\"#fff3e6\", color=\"#fab366\", style=\"filled\", shape=record];\n";
					} else {
						nodeDefinition += "[label = \"" + label + "| { {" + n.getLabel() + "} | {" +  n.getValue() + "}}\"";
						nodeDefinition += ", fillcolor=\"#e6fff0\", color=\"#bae3cb\", style=\"filled\", shape=record];\n";
					}
				} else {
					if (LineGraphTransformer.INIT.equalsIgnoreCase(n.getType())) {
						label = "Init"; 
					} else {
						if (LineGraphTransformer.TARGET.equalsIgnoreCase(n.getType())) {
							label = "Goal"; 
						}
					}
					//label = n.getLabel().replace('-', '_');					
					nodeDefinition += "[label = \"" + label + "{" + n.getLabel() + "}\"";
					//nodeDefinition += ", fillcolor=\"#9cd6ff\", color=\"blue\", style=\"filled\", shape=oval];\n";
					nodeDefinition += ", fillcolor=\"#c4e9ff\", color=\"#75aed1\", style=\"filled\", shape=oval];\n";					
				}
				
				//nodeDefinition += "[label = \"" + label + "| { compromise |{T=" + c + "|F=" + d + "}}\""; //use this for txt9
				//nodeDefinition += "[label = \"" + label + "| {" + n.getValue() + "}\""; 
				
				//nodeDefinition += "[label = \"" + label + "| { " +  n.getValue() + " |{src=" + src + "|tgt=" + tgt + "}}\""; 
				//nodeDefinition += "[label = \"" + label + "| { {src=" + src + "|tgt=" + tgt + "} | {" +  n.getValue() + "}}\""; 
				//nodeDefinition += "[label = \"" + label + "| { {" + src + " (AT) | " + tgt + "(LG)} | {" +  n.getValue() + "}}\"";
				//nodeDefinition += ", fillcolor=\"#9cd6ff\", color=\"blue\", style=\"filled\", shape=record];\n";				
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
					String label = "";
					//String label = edge.getLabel();				
					//String label = edge.getLabel().replace('-', '_') + "(" + edge.getValue() + ")";
					label = "label=\"" + label + "\"";				
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
	public String writeGraphToFile (LineAndOrGraph lgraph, String outputFilepath, MaxSatSolution solution) throws FileNotFoundException, UnsupportedEncodingException {
		logger.info("Writing Graph to DOT file");
		File outputFile = new File(outputFilepath);
				
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
		
		writer.println(this.getHeader());			
		writer.println(this.generateGraphTag(lgraph, solution));
		writer.println(this.getFooter());
				
		writer.close();
		//logger.info("CoreGraphOnlyProbabilities exportation finished");
		return outputFile.getAbsolutePath();
	}
	
	public String exportGraph (LineAndOrGraph lgraph, String format, String inputGraphDescriptorFilepath, MaxSatSolution solution) throws FileNotFoundException, UnsupportedEncodingException, Exception {
		//AndOrGraph graph = new JSONReader().loadAndOrGraph(graphDescriptorFilepath);
		try {
			File graphDescriptor = new File(inputGraphDescriptorFilepath);
			String extension = GraphExporterFactory.DOT_FORMAT; 
			String name = graphDescriptor.getName().substring(0, graphDescriptor.getName().lastIndexOf("."));
			String dotFilepath = graphDescriptor.getParentFile().getPath() + File.separator + name + "-lg." + extension;			
			dotFilepath = this.writeGraphToFile(lgraph, dotFilepath, solution);
			
			//GraphExporter exporter = GraphExporterFactory.getExporter(GraphExporterFactory.DOT_FORMAT);
			String imageFilepath = graphDescriptor.getParentFile().getPath() + File.separator + name + "-lg." + format;
					
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
