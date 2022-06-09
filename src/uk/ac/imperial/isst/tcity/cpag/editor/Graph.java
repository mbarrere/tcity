package uk.ac.imperial.isst.tcity.cpag.editor;
import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.imperial.isst.tcity.TCityConstants;
import uk.ac.imperial.isst.tcity.cpag.editor.display.ImagePanel;
import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeDimensions;
import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeDisplay;
import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;


class GraphException extends Exception{
	private static final long serialVersionUID = 5280421833743690760L;
	
	public GraphException(String message) {
		super(message);
	}
	
}

public class Graph implements Serializable{

	private static final long serialVersionUID = 5673009196816218789L;

	private String graphTitle;
	private List<Node> nodes;
	private List<Edge> edges;
	private String source;
	private String target;
	
	@JsonIgnore
	private transient List<GraphListener> listeners; // transient means this field will not be serialized
	@JsonIgnore
	private boolean autolayout = false;
	
	public Graph() {
		this("Cyber-physical attack graph");
	}
	
	public Graph(String title) {
		setGraphTitle(title);
		setNodes(new ArrayList<Node>());
		setEdges(new ArrayList<Edge>());
		this.source = null;
		this.target = null;
		this.listeners = new ArrayList<GraphListener>();
	}
	
	public void registerListener (GraphListener listener) {
		if (this.listeners == null) {			
			this.listeners = new ArrayList<GraphListener>();
		}
		if (!this.listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	public void unregisterListener (GraphListener listener) {
		this.listeners.remove(listener);			
	}
	
	public String getGraphTitle() {
		return graphTitle;
	}

	public void setGraphTitle(String graphTitle) {
		if(graphTitle == null)
			graphTitle = "";
		else
			this.graphTitle = graphTitle;
	}
	
	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
	
	public void draw(Graphics g) {
		for (Edge edge : getEdges()) {
			edge.draw(g);
		}
		
		int iconSizeX = 21;
		int iconSizeY = 21; 
		for (Node node : getNodes()) {
			node.draw(g);
			if (node.getId().equalsIgnoreCase(this.source)) {
				NodeDimensions dim = NodeDisplay.getNodeDimensions(g, node.getLabel());		
				ImagePanel targetPanel = new ImagePanel("resources/figures/hacker-icon.png", 
						node.getX() + (dim.getWidth()/2) - 1, 
						node.getY() - (dim.getHeight()/2) - 7, 
						iconSizeX, iconSizeY);
				targetPanel.display(g);
			}
			if (node.getId().equalsIgnoreCase(this.target)) {
				NodeDimensions dim = NodeDisplay.getNodeDimensions(g, node.getLabel());
				//ImagePanel targetPanel = new ImagePanel("resources/figures/crosshair.png",
				//ImagePanel targetPanel = new ImagePanel("resources/figures/target-icon-4534.png",
				ImagePanel targetPanel = new ImagePanel("resources/figures/crosshair8.png",
						node.getX() + (dim.getWidth()/2) - 1, 
						node.getY() - (dim.getHeight()/2) - 7, 
						iconSizeX, iconSizeY);
				targetPanel.display(g);
			}
		}
	}
	
	public void addNode(Node n) {
		nodes.add(n);
	}
	
	public void addEdge(Edge e) {
		for (Edge edge : edges) {
			if(e.equals(edge))
				return;
		}
		edges.add(e);
	}

	public Node findNodeUnderCursor(int mx, int my) {
		for (Node node : nodes) {
			if(node.isUnderCursor(mx, my)) {
				return node;
			}
		}
		return null;
	}
	
	public Edge findEdgeUnderCursor(int mx, int my) {
		for (Edge edge : edges) {
			if(edge.isUnderCursor(mx, my)) {
				return edge;
			}
		}
		return null;
	}

	public void removeNode(Node nodeUnderCursor) {
		removeAttachedEdges(nodeUnderCursor);
		nodes.remove(nodeUnderCursor);		
	}
	
	protected void removeAttachedEdges(Node nodeUnderCursor) {
		edges.removeIf(e -> {
			return e.getNodeA().equals(nodeUnderCursor) 
				|| e.getNodeB().equals(nodeUnderCursor);
		});
	}
	
	public void removeEdge(Edge edgeUnderCursor) {
		edges.remove(edgeUnderCursor);
	}
	
	public void moveGraph(int dx, int dy) {
		for (Node node : nodes) {
			node.move(dx, dy);
		}
	}
	
	public static void serializeGraph(String fileName, Graph graph) throws GraphException {
		try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))){
			out.writeObject(graph);
		}catch(IOException e) {
			e.printStackTrace();
			throw new GraphException("Serialization error!");
		}
	}
	
	public static void serializeGraph(File file, Graph graph) throws GraphException {
		try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))){
			out.writeObject(graph);
		}catch(IOException e) {
			e.printStackTrace();
			throw new GraphException("Serialization error");
		}
	}
	
	public static Graph deserializeGraph(String fileName) throws GraphException {
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))){
			Graph graph = (Graph) in.readObject();
			return graph;
		}catch (FileNotFoundException e) {
			throw new GraphException("No file found!");
		}catch(IOException e) {
			throw new GraphException("File fatal error!");
		} catch (ClassNotFoundException e) {
			throw new GraphException("No such object was found!");
		} 
	}
	
	public static Graph deserializeGraph(File file) throws GraphException {
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))){
			Graph graph = (Graph) in.readObject();
			return graph;
		}catch(IOException e) {
			throw new GraphException("File fatal error!");
		} catch (ClassNotFoundException e) {
			throw new GraphException("No such object was found!");
		}
	}

	@JsonIgnore
	public String getListOfNodes() {
		int index = 1;
		String list = "Number of nodes: " + Integer.toString(nodes.size()) + "\n";
		list += "N. [Type]: (position) {parameters}\n";
		for (Node node : nodes) {
			list += Integer.toString(index++);
			list += ". ";
			list += node.toString();
			list += "\n";
		}
		return list;
	}

	@JsonIgnore
	public String getListOfEdges() {
		int index = 1;
		String list = "Number of edges: " + Integer.toString(edges.size()) + "\n";
		list += "N. [Type]: (Node A) ===> (Node B) {parameters}\n";
		for (Edge edge : edges) {
			list += Integer.toString(index++);
			list += ". ";
			list += edge.toString();
			list += "\n";
		}
		return list;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return graphTitle + "("+ nodes.size() + " nodes, " + edges.size() + " edges)";
	}
	
	
	public static Graph importJSON(String fileName) throws GraphException {
		try{
			Graph graph = Graph.loadGraphFromJSON(fileName); 
			return graph;
		} catch(FileNotFoundException e) {
			//e.printStackTrace();
			throw new GraphException(e.getLocalizedMessage());
		} catch(JsonParseException e) {
			//e.printStackTrace();
			throw new GraphException(e.getLocalizedMessage());
		} catch (JsonMappingException e) {			
			//e.printStackTrace();
			throw new GraphException(e.getLocalizedMessage());
		} catch (IOException e) {			
			//e.printStackTrace();
			throw new GraphException(e.getLocalizedMessage());
		} 
	}
	
	/*
	public static Graph importJSON(File file) throws GraphException {		
		try{
			Graph graph = Graph.loadGraphFromJSON(file.getAbsolutePath()); 
			return graph;
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			throw new GraphException(e.getMessage());
		} catch(Exception e) {
			e.printStackTrace();
			throw new GraphException(e.getMessage());
		} 
	}
	*/
	
	public Node findNodeById(String id) {
		if (id == null || id.trim().isEmpty()) {
			return null;
		}
		
		for (Node n : nodes) {
			if ( id.equalsIgnoreCase(n.getId()) ) {
				return n;
			};  
		}
		return null;
	}
	
	public Node findNodeByLabel(String label) {
		if (label == null || label.trim().isEmpty()) {
			return null;
		}
		
		for (Node n : nodes) {
			if ( label.equalsIgnoreCase(n.getLabel()) ) {
				return n;
			};  
		}
		return null;
	}		
	
	private void updateEdgeReferences() throws GraphException {
		for (Edge e : edges) {
			Node src = findNodeById(e.getSource());
			Node tgt = findNodeById(e.getTarget());
			
			if (src != null) {
				e.setNodeA(src);
			} else {
				throw new GraphException("Invalid edge specification. The edge source node '" + e.getSource() + "'" + " is not in the list of nodes.)");
			}
			if (tgt != null) {
				e.setNodeB(tgt);
			} else {
				throw new GraphException("Invalid edge specification. The edge target node '" + e.getTarget() + "'" + " is not in the list of nodes.)");
			}
		}
	}
	
	private static Graph loadGraphFromJSON (String filename) throws JsonParseException, JsonMappingException, IOException, GraphException {			
		
	    File inputJSONFile = new File(filename);
	    	    	    
	    if (!inputJSONFile.exists()) {
	    	throw new FileNotFoundException("The specified JSON file does not exists: " + filename);
	    }
	    
	    
	    ObjectMapper mapper = new ObjectMapper();
	    mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
	 	//GraphContainer container = mapper.readValue(Paths.get("book.json").toFile(), GraphContainer.class);
	    //GraphContainer container = mapper.readValue(inputJSONFile, GraphContainer.class);
	    GraphContainer container = mapper.readValue(inputJSONFile, GraphContainer.class);
	    
	    Graph graph = container.getGraph();
	    graph.updateEdgeReferences();
	    
	    
	    //System.out.println("Analysing nodes position");		
	    //List<Node> graphNodes = graph.getNodes();
	    JsonNode rootNode = mapper.readTree(inputJSONFile);
	    JsonNode nodesNode = rootNode.path("graph").path("nodes");	
	    
	    try {		    					    	    	    		 
		 	Iterator<JsonNode> nodesIter = nodesNode.iterator();
		 	boolean finished = false; 
		 	while (nodesIter.hasNext() && !finished) {
		 		JsonNode jsonNode = nodesIter.next();
		 		String jsonId = jsonNode.path("id").textValue();
		 		//System.out.println("node id in json: " + jsonId);
		 		String nodeXPosition = jsonNode.path("x").toString();
		 		String nodeYPosition = jsonNode.path("y").toString();
		 		//System.out.println("jsonXnode for node '" + jsonId + "':" + nodeXPosition);
		 		
		 		try {
		 			if (nodeXPosition != null && nodeXPosition.startsWith("\"")) {
		 				nodeXPosition = nodeXPosition.substring(1, nodeXPosition.length()-1);		 				
		 			}
		 			Integer.parseInt(nodeXPosition);
		 			
		 			if (nodeYPosition != null && nodeYPosition.startsWith("\"")) {
		 				nodeYPosition = nodeYPosition.substring(1, nodeYPosition.length()-1);		 				
		 			}
		 			Integer.parseInt(nodeXPosition);
		 		} catch (NumberFormatException e) {
		 			System.out.println("Enabling autolayout: node '" + jsonId + "' does not have (x,y) positions");
		 			finished = true;
		 			graph.setAutolayout(true);
		 		}
		 		/*
		 		if (nodeXPosition == null || nodeXPosition.trim().isEmpty()) {		    		 					 			
		 			System.out.println("Node '" + jsonId + "' does not have its x position. \nSTOPPING ");		 			
		 			finished = true;
		 		}	    		 
		 		
		 		if (!jsonNode.hasNonNull("x")) {
		 			System.out.println("Node '" + jsonId + "' has null X \nSTOPPING ");
		 			finished = true;
		 		}
		 		*/
		 	}		 	
		 			 	
	 	} catch (Exception e) {				
	 		e.printStackTrace();
			//throw new IllegalSpecificationException(e.getMessage());
		}
	   
	 	return graph;
	 	 	
	}
	
		
	public boolean isAutolayout() {
		return autolayout;
	}

	public void setAutolayout(boolean autolayout) {
		this.autolayout = autolayout;
	}

	public void applyLayout(int width, int height, boolean topDown) {
		
		//System.out.println("Panel size: " + width + "x" + height);
		int graphDepth = 0;
		int graphWidth = 0;
		
		int currentLevel = 0;		
		List<AndOrNode> currentLevelNodes = new ArrayList<AndOrNode>();
		
		Map<String, Integer> levels = new LinkedHashMap<String,Integer>();
		
		try {
			AndOrGraph cpag = Graph.buildCPAG(this);
			List<AndOrNode> cpagNodes = cpag.getNodes();
			for (AndOrNode n : cpagNodes) {
				//System.out.println("Analysing node '" + n.getId() + "' (baseline)");
				List<AndOrNode> incoming = cpag.getIncomingNodes(n.getId());
				if (incoming == null || incoming.isEmpty()) {
					currentLevelNodes.add(n);
				}
			}
			graphWidth = currentLevelNodes.size();
			
			boolean finished = false;			
			Set<AndOrNode> nextLevelNodes = new LinkedHashSet<AndOrNode>();
			
			while (!finished) {
				for (AndOrNode n : currentLevelNodes) {
					//System.out.println("Analysing node '" + n.getId() + "' (level " + currentLevel + ")");
					if (levels.containsKey(n.getId())) {
						Integer previousLevel = levels.get(n.getId());
						if (currentLevel > previousLevel) {
							levels.put(n.getId(), currentLevel);
						}
					} else {
						levels.put(n.getId(), currentLevel);
					}
					
					nextLevelNodes.addAll(cpag.getOutgoingNodes(n.getId()));
				}
				
				
				currentLevel++;
				if (nextLevelNodes.isEmpty()) {
					finished = true;
					graphDepth = currentLevel;
				}
				int nextLevelSize = nextLevelNodes.size();
				if (nextLevelSize > graphWidth) {
					graphWidth = nextLevelSize;
				}
				currentLevelNodes.clear();
				currentLevelNodes.addAll(nextLevelNodes);
				nextLevelNodes.clear();				
			}
			
			//System.out.println("Graph depth: " + graphDepth);
			//System.out.println("Graph width: " + graphWidth);
			
			//System.out.println("Nodes and their levels: ");
			
			Map<Integer,List<String>> nodesByLevel = new LinkedHashMap<Integer,List<String>>();
			
			for (Entry<String,Integer> e : levels.entrySet()) {
				//System.out.println("\t'" + e.getKey() + "':" + e.getValue());
				
				List<String> nodesAtLevel = nodesByLevel.get(e.getValue());
				if (nodesAtLevel == null) {
					nodesAtLevel = new ArrayList<String>();
					nodesByLevel.put(e.getValue(), nodesAtLevel);
				}
				if (!nodesAtLevel.contains(e.getKey())) {
					nodesAtLevel.add(e.getKey());
				}
			}
			
			/*
			System.out.println("Levels and their nodes: ");
			for (Entry<Integer,List<String>> e : nodesByLevel.entrySet()) {
				System.out.println("\t'" + e.getKey() + "':" + e.getValue());				
			}
			*/
			
			if (topDown) {				
				int vPadding = height / (graphDepth + 1);
				
				int currentY = 0;
				
				//System.out.println("vPadding: " + vPadding);
				//System.out.println("hPadding: " + hPadding);
				
				for (Entry<Integer,List<String>> e : nodesByLevel.entrySet()) {
					//System.out.println("Level '" + e.getKey() + "':" + e.getValue());
					List<String> nodesAtLevel = e.getValue();
					
					//int hPad = (e.getKey()+1) * hPadding;
					int hPad = width / (nodesAtLevel.size()+1);
					
					currentY += vPadding;
					int currentX = 0;
					
					//int nodesAtLevelCount = nodesAtLevel.size();
					for (String nodeId : nodesAtLevel) {
						currentX += hPad;
						Node graphNode = this.findNodeById(nodeId); 
						graphNode.setX(currentX);
						graphNode.setY(currentY);
						//System.out.println("Node '" + nodeId + "' position: (" + graphNode.getX() + "," + graphNode.getY() + ")");
					}
				}
			} else {
				//int vPadding = height / (graphWidth + 1);
				int hPadding = width / (graphDepth + 1);
				
				int currentX = 0;
				
				//System.out.println("vPadding: " + vPadding);
				//System.out.println("hPadding: " + hPadding);
				
				for (Entry<Integer,List<String>> e : nodesByLevel.entrySet()) {
					//System.out.println("Level '" + e.getKey() + "':" + e.getValue());
					List<String> nodesAtLevel = e.getValue();
					
					//int hPad = (e.getKey()+1) * hPadding;
					int vPad = height / (nodesAtLevel.size()+1);
					
					currentX += hPadding;
					int currentY = 0;
					
					//int nodesAtLevelCount = nodesAtLevel.size();
					for (String nodeId : nodesAtLevel) {
						currentY += vPad;
						Node graphNode = this.findNodeById(nodeId); 
						graphNode.setX(currentX);
						graphNode.setY(currentY);
						//System.out.println("Node '" + nodeId + "' position: (" + graphNode.getX() + "," + graphNode.getY() + ")");
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static Graph buildGraphFromCPAG(AndOrGraph cpag) throws Exception {
		
		Graph graph = new Graph();
		
		for (AndOrNode node : cpag.getNodes()) {			
			Node x = new Node();
			x.setId(node.getId());
			x.setType(node.getType());
			x.setValue(node.getValue());
			x.setLabel(node.getLabel());
			graph.addNode(x);
		}				
		
		for (AndOrEdge edge : cpag.getEdges()) {			
			
			Node srcNode = graph.findNodeById(edge.getSource());
			Node tgtNode = graph.findNodeById(edge.getTarget());
						
			Edge x = new Edge(srcNode, tgtNode, Color.BLACK, TCityConstants.EDGE_STROKE, edge.getValue(), edge.getLabel());
			graph.addEdge(x);
		}
		
		graph.setSource(cpag.getSource());
		graph.setTarget(cpag.getTarget());
		
		return graph;
	}
	
	public static AndOrGraph buildCPAG(Graph graph) throws Exception {
		
		AndOrGraph cpag = new AndOrGraph();
		
		for (Node cn : graph.getNodes()) {			
			AndOrNode x = new AndOrNode(String.valueOf(cn.getId()), cn.getType(), cn.getValue(), cn.getLabel());
			cpag.addNode(x);
		}
		
		for (Edge edge : graph.getEdges()) {			
			
			Node srcNode = edge.getNodeA();
			Node tgtNode = edge.getNodeB();
						
			AndOrEdge  x = new AndOrEdge(String.valueOf(srcNode.getId()), String.valueOf(tgtNode.getId()), edge.getValue(), "");
			cpag.addEdge(x);
		}
		
		if (graph.getSource() != null) {
			cpag.setSource(graph.getSource());
		}
		
		if (graph.getTarget() != null) {
			cpag.setTarget(graph.getTarget());
			//throw new Exception ("CPAG target not specified");
		}
		return cpag;
	}
	
	public void setNodesProperty (String propertyName, Map<String,Double> map) throws GraphException {
		for (Entry<String,Double> entry : map.entrySet()) {
			Node graphNode = this.findNodeById(entry.getKey());
			if (graphNode == null) {
				//throw new GraphException("Node '" + entry.getKey() + "' not found in graphical display");
				//pass
			} else {
				DecimalFormat df = new DecimalFormat("0.0000");
				String value = df.format(entry.getValue());
				graphNode.getProperties().put(propertyName, value);
			}
		}
	}
	
	
	public Edge findEdge(String sourceId, String targetId) {
		if (sourceId == null || sourceId.trim().isEmpty()) {
			return null;
		}
		if (targetId == null || targetId.trim().isEmpty()) {
			return null;
		}
		
		for (Edge e : edges) {
			if ( sourceId.equalsIgnoreCase(e.getSource()) && 
					targetId.equalsIgnoreCase(e.getTarget()) ) {
				return e;
			};  
		}
		return null;
	}
	
	public void setEdgesProperty (String propertyName, List<AndOrEdge> solutionEdges) throws GraphException {
		for (AndOrEdge e : solutionEdges) {
			//System.out.println("Solution edge: " + e.getSource() + "->" + e.getTarget());
		
			Edge graphEdge = this.findEdge(e.getSource(), e.getTarget());
			if (graphEdge == null) {
				//System.out.println("-> edge not found in graph");
				//throw new GraphException("Node '" + entry.getKey() + "' not found in graphical display");
				//pass
			} else {
				//System.out.println("-> setting value: " + e.getValue());
				//DecimalFormat df = new DecimalFormat("0.0000");
				//String value = df.format(e.getValue());
				//graphEdge.getProperties().put(propertyName, value);
				graphEdge.getProperties().put(propertyName, e.getValue());
			}
		}
	}
}
