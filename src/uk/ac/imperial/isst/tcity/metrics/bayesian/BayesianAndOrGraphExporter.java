/**
 * 
 */
package uk.ac.imperial.isst.tcity.metrics.bayesian;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
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
public class BayesianAndOrGraphExporter {

	private static final Logger logger = LogManager.getLogger(BayesianAndOrGraphExporter.class);
	
	private final String extension = GraphExporterFactory.PNG_FORMAT;
	
	public static String size = "8.5,11";
	//public static String size = "4.5,11";
	//public static String size = "7.75,10.25";
	//public static String size = "6.375,8.25";
	//public static String size = "4.2,5.94";
	//public static String size = "1.05,1.485";
	//public static String size = "21.0,29.7";
	public static String ratio = "compress";
	public static String rankdir = "TD"; //LR
	public static String nodefontsize = "30"; //"36"; 
	public static String edgefontsize = "28"; //"28";
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
		//header += "ratio=0.9; ";
		header += "nodesep=0.9; ";
		//header += "node[fontsize=12,shape=box,style=rounded];";
		//header += "node[fontsize=16,penwidth=2,shape=box];";
		header += "node[fontsize=" + nodefontsize + ",shape=box];";
		//header += "edge[weight=0.5, dir=forward,style=\"setlinewidth(1.0)\"];";		
		//header += "edge[fontsize=" + edgefontsize + ",dir=forward,style=\"setlinewidth(10.0)\"];";
		header += "edge[fontsize=" + edgefontsize + ",dir=forward, penwidth=2.0];";
		return header;
	}
	
		
	
	private String generateGraphTag(BayesianAndOrGraph bgraph) {
		
		AndOrGraph graph = bgraph.getAndOrGraph();
		String tag = "";
		boolean ignoreEdgeLabel = false;
		//DecimalFormat df = new DecimalFormat("0.00");
		
		
		String nodeDefinition = "";		
		DecimalFormat df = new DecimalFormat("0.0000");
		//DecimalFormat dfBeliefs = new DecimalFormat("0.0");
		
		for (AndOrNode n : bgraph.getAndOrGraph().getNodes()) {
			String nodeId = n.getId().replace('-', '_');
			nodeDefinition += nodeId + " ";	
			if (n.isAndType() || n.isOrType()) {
				// OPERATOR NODES
				//nodeDefinition += "[label=\"" + n.getType() + "\"];\n";	
				nodeDefinition += "[label=\"" + n.getType() + "\"" + 
						//", fillcolor=\"#ddbfff\", color=\"#b87aff\", style=\"filled\", shape=record];\n";
						", fillcolor=\"#e8cfffCC\", color=\"#b87aff\", style=\"filled\", shape=record];\n";
			} else {
				//String c = dfBeliefs.format(n.getCompromiseBelief()*100) + "%";
				String c = df.format(bgraph.getUnconditionalProbability(n));
				//String d = dfBeliefs.format(n.getNotCompromiseBelief()*100) + "%";
				String d = df.format(1.0 - bgraph.getUnconditionalProbability(n));
				String label = n.getLabel().replace('-', '_');
				nodeDefinition += "[label = \"" + label + "| { compromise |{T=" + c + "|F=" + d + "}}\""; //use this for txt9
				//nodeDefinition += "[label = \"" + n.getName() + "| { compromise | T=" + c + "|F=" + d + "}\"";
				/*
				 if (n.getId().equalsIgnoreCase(bgraph.getAndOrGraph().getTarget())) {
					//TARGET - green
					//nodeDefinition += ", fillcolor=\"#FFD6C2\", color=\"red\", style=\"filled\", shape=record];\n";
					nodeDefinition += ", fillcolor=\"#dfffdb\", color=\"#6ae366\", style=\"filled\", shape=record];\n";
				} else {
					if (n.getId().equalsIgnoreCase(bgraph.getAndOrGraph().getSource())) {
						//SOURCE - red
						//nodeDefinition += ", fillcolor=\"#dfffdb\", color=\"#6ae366\", style=\"filled\", shape=record];\n";
						nodeDefinition += ", fillcolor=\"#FFD6C2\", color=\"red\", style=\"filled\", shape=record];\n";
					} else {
						//STATES - blue
						//nodeDefinition += ", fillcolor=\"#9cd6ff\", color=\"blue\", style=\"filled\", shape=record];\n";
						if (n.getType().equalsIgnoreCase("action") || n.getType().equalsIgnoreCase("attack") || n.getType().equalsIgnoreCase("physical-action")) {
							// ACTIONS
							//nodeDefinition += ", fillcolor=\"#ffa600\", color=\"#e87007\", style=\"filled\", shape=record];\n";
							//nodeDefinition += ", fillcolor=\"#ffe6bf\", color=\"#e87007\", style=\"filled\", shape=record];\n";
							nodeDefinition += ", fillcolor=\"#ffffff\", color=\"#e87007\", style=\"filled\", shape=record];\n";
						} else {
							nodeDefinition += ", fillcolor=\"#c4e9ff\", color=\"#75aed1\", style=\"filled\", shape=record];\n";
						}
					}
				}
				 */
				
				/*
				if (n.getId().equalsIgnoreCase(bgraph.getAndOrGraph().getTarget())) {
					//TARGET - green			
					nodeDefinition += ", fillcolor=\"#dfffdb\", color=\"#6ae366\", style=\"filled\", shape=record];\n";
				} 
				*/
				
				// Privilege default (white)
				String nodeStyle = ", fillcolor=\"white\", color=\"#75aed1\", style=\"filled\", shape=record];\n";
				
				if (n.getId().equalsIgnoreCase(bgraph.getAndOrGraph().getSource())) {
					//SOURCE (red)				
					nodeStyle = ", fillcolor=\"#FFD6C2\", color=\"red\", style=\"filled\", shape=record];\n";
				} 				
					
				if (n.getType().equalsIgnoreCase("action") || n.getType().equalsIgnoreCase("attack") || n.getType().equalsIgnoreCase("physical-action")) {
					// Actions (white)					
					nodeStyle = ", fillcolor=\"#ffffff\", color=\"#e87007\", style=\"filled\", shape=record];\n";
				}
				if (n.getType().equalsIgnoreCase("impact")) {
					// Impact (green)
					nodeStyle = ", fillcolor=\"#dfffdbAA\", color=\"#6ae366\", style=\"filled\", shape=record];\n";
				}
				if (n.getType().equalsIgnoreCase("cyber")) {
					// Cyber privileges (blue)
					nodeStyle = ", fillcolor=\"#c4e9ffDD\", color=\"#75aed1\", style=\"filled\", shape=record];\n";
				}
				if (n.getType().equalsIgnoreCase("physical")) {
					// Physical privileges (orange)
					nodeStyle = ", fillcolor=\"#fcad0377\", color=\"#fca103\", style=\"filled\", shape=record];\n";
				}
					
				nodeDefinition += nodeStyle;
						
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
				AndOrNode dst = graph.getNode(edge.getTarget());
				
				if (src.isAndType() || src.isOrType()) {
					tag += "[];\n";
				} else {
					if (dst.isActionType()) {
						tag += "[];\n";	
					} else {
						if (src.isSplitterType()) {
							tag += "[];\n";	
						} else {
							tag += "[";				
							//String label = edge.getLabel();				
							String label = edge.getLabel().replace('-', '_') + " (" + edge.getValue() + ")";
							label = "label=\"" + label + "\"";				
							tag += label + "];\n";
						}
					}
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
	public String writeGraphToFile (BayesianAndOrGraph graph, String outputFilepath) throws FileNotFoundException, UnsupportedEncodingException {
		logger.info("Writing Graph to DOT file");
		File outputFile = new File(outputFilepath);
				
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
		
		writer.println(this.getHeader());			
		writer.println(this.generateGraphTag(graph));
		writer.println(this.getFooter());
				
		writer.close();
		//logger.info("CoreGraphOnlyProbabilities exportation finished");
		return outputFile.getAbsolutePath();
	}
	
	public String exportGraph (BayesianAndOrGraph bgraph, String format, String inputGraphDescriptorFilepath) throws FileNotFoundException, UnsupportedEncodingException, Exception {
		//AndOrGraph graph = new JSONReader().loadAndOrGraph(graphDescriptorFilepath);
		try {
			//System.out.println("inputGraphDescriptorFilepath: " + inputGraphDescriptorFilepath);
			
			File graphDescriptor = new File(inputGraphDescriptorFilepath);
			String extension = GraphExporterFactory.DOT_FORMAT; 
			String name = graphDescriptor.getName().substring(0, graphDescriptor.getName().lastIndexOf("."));
			//System.out.println("graphDescriptor.getParentFile(): " + graphDescriptor.getParentFile());
			
			String parentFilePath = "";
			if (graphDescriptor.getParentFile() != null) {
				parentFilePath = graphDescriptor.getParentFile().getPath() + File.separator;
			}
			
			String dotFilepath = parentFilePath + name + "-bn." + extension;			
			dotFilepath = this.writeGraphToFile(bgraph, dotFilepath);
			
			//GraphExporter exporter = GraphExporterFactory.getExporter(GraphExporterFactory.DOT_FORMAT);
			String imageFilepath = parentFilePath  + name + "-bn." + format;
					
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
