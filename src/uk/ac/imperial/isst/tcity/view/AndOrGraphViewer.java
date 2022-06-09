package uk.ac.imperial.isst.tcity.view;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;

import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class AndOrGraphViewer {
	private AndOrGraph cpag;
	private Graph displayGraph;
	private SpriteManager spriteManager;
	private Viewer viewer; 
	private String spriteHexAlpha = "C5";
	private boolean showEdgeLabel = true;
	
	public AndOrGraphViewer(AndOrGraph cpag) {		
		this(cpag, "Cyber-Physical Attack Graph");
	}
	
	public AndOrGraphViewer(AndOrGraph cpag, String title) {
		System.setProperty("org.graphstream.ui", "swing");
		this.cpag = cpag;
		this.displayGraph = this.createDisplayGraph(cpag);
		this.displayGraph.setAttribute("ui.title", title);
		this.spriteManager = new SpriteManager(displayGraph);
		this.viewer = null;
	}
	
	public void setTitle (String title) {
		this.displayGraph.setAttribute("ui.title", title);		
	}
	
	public void disableAutoLayout () {
		if (this.viewer != null) {
			viewer.disableAutoLayout();	
			viewer.getDefaultView().getCamera().setAutoFitView(false);
		}		
	}
	
	public void enableAutoLayout () {
		if (this.viewer != null) {
			viewer.enableAutoLayout();
			viewer.getDefaultView().getCamera().setAutoFitView(true);
		}		
	}
	
	public void updateEdgeAttributes (boolean showColor, boolean showValue) {		
		//strings.stream().filter(s -> s.length() == 2).forEach(System.out::println);
		Consumer<Edge> consumer = e -> {
			AndOrEdge andOrEdge  = cpag.getEdge(e.getSourceNode().getId(), e.getTargetNode().getId());
			if (showValue) {
				if (showEdgeLabel) {
					e.setAttribute("ui.label", andOrEdge.getLabel() + " (" + andOrEdge.getValue() + ")");
				} else {
					e.setAttribute("ui.label", andOrEdge.getValue());
				}
			} else {
				e.removeAttribute("ui.label");
			}
			
			if (showColor) {
				Float edgeValue = new Float(andOrEdge.getValue());
				e.setAttribute("ui.color", edgeValue);
			} else {
				e.removeAttribute("ui.color");
			}
		}; 
		
		displayGraph.edges().forEach(consumer);
				
	}
	
	public void display () {
		//this.displayGraph = this.createDisplayGraph(cpag); 
		//displayGraph.display(false);
		this.viewer = this.displayGraph.display();

		//ViewPanel view = (ViewPanel) viewer.getDefaultView(); // ViewPanel is the view for gs-ui-swing
		//view.resizeFrame(800, 600);
		//view.getCamera().setViewCenter(3000, 8000, 0);
		//view.getCamera().setViewPercent(1.25);
		
		//this.viewer.getDefaultView().enableMouseOptions();
		//this.viewer.getDefaultView().setMouseManager(new MouseOverMouseManager(EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE)));
	}
	
	public void updateNodeInfo(String nodeId, Object newValue) {
		Sprite s = spriteManager.getSprite(nodeId);		
		
		Double value = null;
		
		if (newValue.getClass().equals(Double.class)) {
			value = (Double)newValue;
		} else {
			try {
				value = Double.parseDouble(newValue.toString());
			} catch (Exception e) {
				//pass
				//e.printStackTrace();
				value = null;
			}
		}
		
		if (value == null) {
			//System.out.println("HAS ATTRIBUTE null value: " + s.hasAttribute("text-color"));
			s.setAttribute("ui.label", newValue);
			//String attribute = " text-background-mode: rounded-box; text-background-color: rgb(45, 147, 250); ";
			String attribute = "text-background-mode: none; text-color: #F00; ";
			//s.setAttribute("ui.style", " text-background-mode: none; ");
			//s.setAttribute("ui.style", " text-color: #F00; ");
			s.setAttribute("ui.style", attribute);				
		} else {
			//System.out.println("value not NULL: " + s.hasAttribute("text-color"));
			double factor = 255.0; //2.0f; //255.0;
			Double redDouble = Math.min(factor,  2 * factor * value); // new Double(factor * risk);
			Double greenDouble = Math.min(factor, 2 * factor * (1 - value)); //new Double(factor * (1 - risk));
			Double blueDouble = new Double(0.0);				
			
			if (value < 0.5) {
				redDouble = factor;
				greenDouble = factor;
				blueDouble = factor * (1-value);
			}
			//String attribute = "fill-color: rgb(" + redDouble.intValue() + "," + greenDouble.intValue() + ", " + blueDouble.intValue() + ");";
			

			//int alpha = 200; 
			//String attribute = " text-background-mode: rounded-box; text-color: #444; text-background-color: rgb(" + redDouble.intValue() + "," + greenDouble.intValue() + ", " + blueDouble.intValue() + ", " + alpha + ");";
			//String attribute = " text-background-mode: rounded-box; text-color: #444; text-background-color: rgb(" + redDouble.intValue() + "," + greenDouble.intValue() + ", " + blueDouble.intValue() + ");";
			String hex = String.format("#%02x%02x%02x", redDouble.intValue(), greenDouble.intValue(), blueDouble.intValue()); 			
			String attribute = " text-background-mode: rounded-box; text-color: #444; text-background-color: " + hex + spriteHexAlpha + "; ";
			s.setAttribute("ui.style", attribute);						
		} 
		//s.setPosition(0.25);			
		
	}
	
	public void addInfoToNodes(Map<String,Object> infoMap) {
		//String spriteStyle = "sprite { size:20px; text-size: 16px; fill-color: #777; text-alignment: under; text-color: white; text-style: bold; text-background-mode: rounded-box; text-background-color: #689efcCC; text-padding: 5px, 4px; text-offset: 0px, 5px; }";
		//String spriteStyle = "sprite { size:20px; text-size: 16px; fill-mode: dyn-plain; fill-color: black, yellow, orange, red; text-alignment: under; text-color: white; text-style: bold; text-background-mode: rounded-box; text-background-color: #689efcCC; text-padding: 5px, 4px; text-offset: 0px, 5px; }";
		String spriteStyle = "sprite { ";
		spriteStyle += "size:40px; text-size: 14px; fill-mode: none; ";
		//spriteStyle += "text-alignment: center; text-padding: 5px, 4px; ";
		spriteStyle += "text-alignment: above; text-padding: 5px, 4px;";
		spriteStyle += "text-color: #444; text-style: bold; text-background-mode: rounded-box; text-background-color: #689efcCC; ";
		//spriteStyle += "text-padding: 5px, 4px; "; //text-offset: 0px, 5px; ";
				
		//spriteStyle += "size: 20px, 10px; text-size: 14px; shape: rounded-box; fill-mode: dyn-plain; fill-color: white, yellow, orange, red; ";
		//spriteStyle += "text-alignment: center; text-color: #444; text-style: bold; ";
		//spriteStyle += "text-padding: 5px, 4px; "; // text-offset: 0px, 5px;
		
		spriteStyle += " }";
		
		//this.displayGraph.setAttribute("ui.stylesheet", "sprite { shape: rounder-box; size: 32px, 32px; }");
		this.displayGraph.setAttribute("ui.stylesheet", spriteStyle);
		for (Entry<String,Object> e : infoMap.entrySet()) {
			//System.out.println("Entry: " + e.getKey() + ", value:" + e.getValue());
			if (e.getKey().equalsIgnoreCase(cpag.getSource())) {
				continue;
			}
			Sprite s = spriteManager.addSprite(e.getKey());		
			//s.setPosition(2, 1, 0);
			s.attachToNode(e.getKey());				
			s.setAttribute("ui.label", e.getValue());
			//s.setPosition(0.25);			
			try {
				//Float floatValue = Float.parseFloat(e.getValue().toString());
				//s.setAttribute("ui.color", floatValue);
				
				Double value = Double.parseDouble(e.getValue().toString());
				double factor = 255.0; //2.0f; //255.0;
				Double redDouble = Math.min(factor,  2 * factor * value); // new Double(factor * risk);
				Double greenDouble = Math.min(factor, 2 * factor * (1 - value)); //new Double(factor * (1 - risk));
				Double blueDouble = new Double(0.0);				
				
				if (value < 0.5) {
					redDouble = factor;
					greenDouble = factor;
					blueDouble = factor * (1-value);
				}
				//String attribute = "fill-color: rgb(" + redDouble.intValue() + "," + greenDouble.intValue() + ", " + blueDouble.intValue() + ");";
				s.setAttribute("ui.style", "text-color: #444; text-background-mode: rounded-box; ");
				
				//int alpha = 200; 
				//String attribute = "text-background-color: rgb(" + redDouble.intValue() + "," + greenDouble.intValue() + ", " + blueDouble.intValue() + ", " + alpha + ");";				 
				//String attribute = "text-background-color: #BB5533CC; ";
				
				//String attribute = "text-background-color: rgb(" + redDouble.intValue() + "," + greenDouble.intValue() + ", " + blueDouble.intValue() + ");";
				String hex = String.format("#%02x%02x%02x", redDouble.intValue(), greenDouble.intValue(), blueDouble.intValue()); 
				//System.out.println("Hex value: " + hex);
				String attribute = "text-background-color: " + hex + spriteHexAlpha + "; ";
				s.setAttribute("ui.style", attribute);
			} catch (Exception ex) {
				//pass
			}
			
			 
			
		}
		
	}
	
	private Graph createDisplayGraph(AndOrGraph cpag) {
		Graph graph = new SingleGraph("CPAG");				
		
        //graph.setAttribute("ui.stylesheet", "node { text-size: 18px; size:20px; text-offset: 15px; }");
		//graph.setAttribute("ui.stylesheet", "node.marked { fill-color: blue; text-size: 28px; }");
		//graph.setAttribute("ui.stylesheet", "node { shape: circle; fill-color: white; stroke-mode: plain; }");
		//graph.setAttribute("ui.stylesheet", "edge { shape: line; fill-mode: dyn-plain; fill-color: black, yellow, orange, red; arrow-size: 10px, 8px; }");
		//graph.setAttribute("ui.stylesheet", "sprite { shape: box; size: 32px, 32px; fill-mode: image-scaled; fill-image: url('" + andIconPath + "'); }");
		// Warning: assigning a style after another eliminates the former => it's not cumulative, does not work!
		
		String stylesheet = "";
		//stylesheet += "node { text-size: 18px; size:20px; text-offset: 15px; shape: rounded-box; fill-color: white; stroke-mode: plain; } "; 				
		//stylesheet += "node { text-size: 18px; size:40px; shape: rounded-box; fill-color: white; stroke-mode: plain; } ";
		stylesheet += "node { size:20px; text-size: 16px; fill-color: #777; text-alignment: under; text-color: white; text-style: bold; text-background-mode: rounded-box; text-background-color: #222C; text-padding: 5px, 4px; text-offset: 0px, 5px; }";
		//stylesheet += "node { fill-color: #777; size-mode: fit; shape: rounded-box; fill-color: white; stroke-mode: plain; padding: 3px, 2px; }";
		//stylesheet += "node { size:20px; text-size: 18px; fill-color: #777; size-mode: fit; shape: rounded-box; fill-color: white; stroke-mode: plain; padding: 10px, 2px; }";
		stylesheet += "edge { shape: line; size: 2px; fill-mode: dyn-plain; fill-color: black, yellow, orange, red; arrow-size: 10px, 8px; } ";
		//stylesheet += "node { text-size: 18px; size:20px; text-offset: 15px; shape: freeplane; size-mode: fit; fill-color: white; stroke-mode: plain; } ";
		//stylesheet += "edge { shape: freeplane; fill-mode: dyn-plain; fill-color: black, yellow, orange, red; arrow-size: 10px, 8px; } ";
		graph.setAttribute("ui.stylesheet", stylesheet);
		//stylesheet += "node { shape: freeplane; size-mode: fit; size:20px; text-size: 16px; fill-color: #777; text-alignment: under; text-color: white; text-style: bold; text-background-color: #222C; text-padding: 5px, 4px; text-offset: 0px, 5px; }";
		//stylesheet += "edge { shape: freeplane; fill-mode: dyn-plain; fill-color: black, yellow, orange, red; arrow-size: 10px, 8px; } ";
		
		
		//graph.setAttribute("ui.quality");
        //graph.setAttribute("ui.antialias");
        boolean directed = true;
        
        
        for (AndOrNode n : cpag.getNodes()) {
        	Node x = graph.addNode(n.getId());
        	if (n.isLogicType()) {
        		//x.setAttribute("ui.style", "fill-color: #ccccff; shape: diamond; text-size: 14px; text-background-color: #ccccffCC; ");
        		//x.setAttribute("ui.style", "fill-color: #ccccff; shape: diamond; text-size: 12px; text-color: black; text-background-color: #ccccffAA; ");
        		x.setAttribute("ui.style", "fill-color: #ccccff; shape: diamond; text-size: 14px; text-color: black; text-background-color: #FFFFFF00; ");
        		if (n.isAndType()) {
            		x.setAttribute("ui.label", "AND");
            	}
        		if (n.isOrType()) {
        			x.setAttribute("ui.label", "OR");        			
        		}
        	} else {
        		// attack/action nodes
        		if (n.isActionType()) {
        			//x.setAttribute("ui.style", "fill-color: #ccccff; shape: rounded-box; text-size: 14px; text-color: black; text-background-color: #FFFFFF00; ");
        			//x.setAttribute("ui.style", "shape: rounded-box; size:20px; text-size: 16px; fill-color: #7F7; text-alignment: under; text-color: white; text-style: bold; text-background-mode: rounded-box; text-background-color: #222C; text-padding: 5px, 4px; text-offset: 0px, 5px;");
        			//x.setAttribute("ui.style", "shape: rounded-box; size:20px; text-size: 16px; fill-color: #fc8803; text-alignment: under; text-color: white; text-style: bold; text-background-mode: rounded-box; text-background-color: #222C; text-padding: 5px, 4px; text-offset: 0px, 5px;");
        			x.setAttribute("ui.style", "shape: rounded-box; size:20px; text-size: 16px; fill-color: #00d43c; text-alignment: under; text-color: white; text-style: bold; text-background-mode: rounded-box; text-background-color: #222C; text-padding: 5px, 4px; text-offset: 0px, 5px;");
	        		        			
        			// PENDING: find a solution for this
        			//System.out.println("Label contains: " + n.getLabel().contains("\\n"));
        			//System.out.println("Label before: " + n.getLabel());	
        			//String s = n.getLabel().replaceAll("\n", "X");
        			//s = n.getLabel().replaceAll("[\\s|\\t|\\r\\n]+"," ").trim();
	        		//System.out.println("Label after: " + s);
	        		
        			x.setAttribute("ui.label", n.getLabel());
        		} else {
	        		//x.setAttribute("ui.label", n.getId());
	        		x.setAttribute("ui.label", n.getLabel());
	
	        		//text-background-color: #222C
	        		if (cpag.getSource().equalsIgnoreCase(n.getId())) {        			
	        			int iconSize = 36;
	        			String iconPath = "resources/figures/hacker-icon.png";
	        			
	        			//if (n.isInitType()) {
	        			x.setAttribute("ui.style", "fill-color: #2D2; text-background-color: #2D2C; ");
	        			//x.setAttribute("xyz", -100, 0, 0);
	        			x.setAttribute("ui.style", "size: " + iconSize + "px; fill-mode: image-scaled; fill-image: url('" + iconPath + "'); ");
	        		}
	        		      
	        		//if (n.isGoalOrFunction()) {
	        		if (cpag.getTarget().equalsIgnoreCase(n.getId())) {
	        			int iconSize = 32;
	        			String iconPath = "resources/figures/target-icon.png";
	        			x.setAttribute("ui.style", "fill-color: #D22; text-background-color: #D22C; ");
	        			x.setAttribute("ui.style", "size: " + iconSize + "px; fill-mode: image-scaled; fill-image: url('" + iconPath + "'); ");
	        		}       
        		}
        	}        	        	
        	        	
        	//x.setAttribute("ui.style", "fill-color: rgb(0,0,255);");
        }
		
        for (AndOrEdge cpagEdge : cpag.getEdges()) {
        	String edgeId = cpagEdge.getSource() + "__" + cpagEdge.getTarget();
        	Edge e = graph.addEdge(edgeId, cpagEdge.getSource(), cpagEdge.getTarget(), directed);
        	//e.setAttribute("ui.label", edgeId);
        	e.setAttribute("ui.style", "text-size: 11px;");
        	Float edgeValue = null;
        	try {
        		edgeValue = new Float(cpagEdge.getValue());
        		if (showEdgeLabel) {
        			e.setAttribute("ui.label", cpagEdge.getLabel() + " (" + edgeValue + ")");
        		} else {
        			e.setAttribute("ui.label", edgeValue);
        		}
        		
            	e.setAttribute("ui.color", edgeValue);
            	
        	} catch (NumberFormatException ex) {
        		if (showEdgeLabel) {
        			e.setAttribute("ui.label", cpagEdge.getLabel() + " (" + cpagEdge.getValue() + ")");
        		} else {
        			e.setAttribute("ui.label", cpagEdge.getValue());
        		}        		
            	e.setAttribute("ui.color", cpagEdge.getValue());
        	}
        	
        }
        
        /*
		graph.getNode("A").setAttribute("risk", 0.33);
		graph.getNode("A").setAttribute("centrality", 0.8);
		graph.getNode("B").setAttribute("risk", 0.66);
		graph.getNode("B").setAttribute("centrality", 0.5);
		graph.getNode("C").setAttribute("risk", 0.99);
		graph.getNode("C").setAttribute("centrality", 0.2);
		*/
        
		//graph.getNode(cpag.getTarget()).setAttribute("ui.style", "fill-color: rgb(255,0,0);");
		return graph;
	}
}
