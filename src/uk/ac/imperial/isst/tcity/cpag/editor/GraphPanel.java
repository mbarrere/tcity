package uk.ac.imperial.isst.tcity.cpag.editor;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.TCityConstants;
import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeDisplayFactory;
import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeType;
import uk.ac.imperial.isst.tcity.metrics.HardeningAnalysis;
import uk.ac.imperial.isst.tcity.metrics.RiskAnalysis;
import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.model.merge.GraphMerger;
import uk.ac.imperial.isst.tcity.util.GraphUtils;
import uk.ac.imperial.isst.tcity.util.JSONWriter;


public class GraphPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener,  ComponentListener {

	
	private static final long serialVersionUID = 3544581658578869882L;
	private static final Logger logger = LogManager.getLogger(GraphPanel.class);
	
	private Grid grid;
	private boolean drawGrid;
	
	private Graph graph;
	
	
	private boolean mouseLeftButton = false;
	@SuppressWarnings("unused")
	private boolean mouseRightButton = false;	
	
	private int mouseX;
	private int mouseY;
	
	private Node nodeUnderCursor;
	private Edge edgeUnderCursor;
	
	private boolean chooseNodeB = false;
	private Node newEdgeNodeA;
	private Node newEdgeNodeB;
	
	private List<GraphListener> listeners;
	private Color backgroundColor; 
	
	private boolean autoBayesian;
	private boolean autoHardening;
	
	public GraphPanel(Graph g) {
		this.listeners = new ArrayList<GraphListener>();
		
		if(g == null) {
			graph = new Graph("Empty CPAG");
		}else {
			setGraph(g);
		}
		
		//Color color = UIManager.getColor ( "Panel.background" );
		this.backgroundColor = this.getBackground();
		
		grid = new Grid(getSize(), 50);
		drawGrid = true;
		
		addMouseMotionListener(this);
		addMouseListener(this);
		addKeyListener(this);
		
		addComponentListener(this);
		
		setFocusable(true);
		requestFocus();
		
		this.autoBayesian = OperationsManager.DISPLAY_BAYESIAN;
		this.autoHardening = OperationsManager.DISPLAY_HARDENING;
		
	}
	
	public Graph getGraph() {
		return graph;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public void changeBackgroundColour () {
		Color color = JColorChooser.showDialog(this, "Choose new color", this.getBackground());
		
		if (color != null) {
			this.setBackground(color);
			repaint();
		}
		
	}

	@Override
	public void repaint() {
		super.repaint();
		if (this.graph != null) {
			//System.out.println("Graph panel notifying listeners");
			this.notifyGraphUpdated();
		}
	}
	
	public void notifyGraphUpdated() {
		if (listeners == null) {
			//System.out.println("Listeners is NULL: ");
			this.listeners = new ArrayList<GraphListener>();
		}
		for (GraphListener listener : this.listeners) {
			//System.out.println("Listener: " + listener);
			listener.graphUpdated(this.graph);
		}
	}
	
	public void notifyNodeClicked(Node node) {
		if (listeners == null) {	
			this.listeners = new ArrayList<GraphListener>();
		}
		for (GraphListener listener : this.listeners) {
			//System.out.println("Listener: " + listener);
			listener.nodeClicked(this.graph, node);
		}
	}
	
	public void notifyEdgeClicked(Edge edge) {
		if (listeners == null) {
			//System.out.println("Listeners is NULL: ");
			this.listeners = new ArrayList<GraphListener>();
		}
		for (GraphListener listener : this.listeners) {
			//System.out.println("Listener: " + listener);
			listener.edgeClicked(this.graph, edge);
		}
	}
	
	public void notifyBayesianReset() {
		if (listeners == null) {	
			this.listeners = new ArrayList<GraphListener>();
		}
		for (GraphListener listener : this.listeners) {
			//System.out.println("Listener: " + listener);
			listener.bayesianReset(this.graph);
		}
	}
	
	public void registerGraphListener (GraphListener listener) {
		if (!this.listeners.contains(listener)) {
			listeners.add(listener);
		}
		if (this.graph != null) {
			this.graph.registerListener(listener);
		}
	}
	
	public void setGraph(Graph graph) {
		if(graph == null)
			this.graph = new Graph("Graph");
		else
			this.graph = graph;
		
		for (GraphListener listener : this.listeners) {
			this.graph.registerListener(listener);
		}
	}
	
	public void showExampleGraph() {	
		graph = new Graph("Example");
		Node a = new Node(250, 100, "A", NodeType.CYBER_NODE.toString(), "none");
		Node b = new Node(550, 100, "B", NodeType.getTypeMap().get(NodeType.CYBER_NODE), "none");
		Node c = new Node(550, 400, "C", NodeType.getTypeMap().get(NodeType.CYBER_NODE), "none");
		Node d = new Node(250, 400, "D", NodeType.getTypeMap().get(NodeType.CYBER_NODE), "none");
		
		Node e = new Node(350, 200, "E", NodeType.getTypeMap().get(NodeType.CYBER_NODE), "none");
		Node f = new Node(450, 200, "F", NodeType.getTypeMap().get(NodeType.CYBER_NODE), "none");
		Node g = new Node(450, 300, "G", NodeType.getTypeMap().get(NodeType.CYBER_NODE), "none");
		Node h = new Node(350, 300, "H", NodeType.getTypeMap().get(NodeType.CYBER_NODE), "none");
		
		Edge ab = new Edge(a, b);
		Edge bc = new Edge(b, c);
		Edge cd = new Edge(c, d);
		Edge da = new Edge(d, a);
		
		Edge ef = new Edge(e, f);
		Edge fg = new Edge(f, g);
		Edge gh = new Edge(g, h);
		Edge he = new Edge(h, e);
		
		Edge ae = new Edge(a, e, Color.RED, 4, "0.5");
		Edge bf = new Edge(b, f, Color.GREEN, 4, "0.7");
		Edge cg = new Edge(c, g, Color.MAGENTA, 4, "0.3");
		Edge dh = new Edge(d, h, Color.ORANGE, 4, "0.9");
		
		graph.addNode(a);
		graph.addNode(b);
		graph.addNode(c);
		graph.addNode(d);
		graph.addNode(e);
		graph.addNode(f);
		graph.addNode(g);
		graph.addNode(h);
		
		graph.addEdge(ab);
		graph.addEdge(bc);
		graph.addEdge(cd);
		graph.addEdge(da);
		
		graph.addEdge(ef);
		graph.addEdge(fg);
		graph.addEdge(gh);
		graph.addEdge(he);
		
		graph.addEdge(ae);
		graph.addEdge(bf);
		graph.addEdge(cg);
		graph.addEdge(dh);
		
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(grid != null && drawGrid) 
			grid.draw(g);

		if(graph != null)
			graph.draw(g);
	}

	public void createNewGraph() {
		setGraph(new Graph("Cyber-physical attack graph"));
		repaint();
	}
	
	
	public void serializeGraph(String fileName) {
		if(graph == null)
			return;
		
		if(!fileName.endsWith(".bin")) {
			fileName += ".bin";
		}
		try {
			Graph.serializeGraph(fileName, graph);
			JOptionPane.showMessageDialog(null, "Saved to file " + fileName);
		} catch (GraphException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Save error!", JOptionPane.ERROR_MESSAGE);
		}
			
	}
	
	public void serializeGraph() {
		if(graph == null)
			return;
		
		JFileChooser fc = new JFileChooser(".");
		fc.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Binary files *.bin", "bin");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);

		int choosenOption = fc.showSaveDialog(this);

		if (choosenOption == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			String fileName = selectedFile.getAbsolutePath();
			if(!fileName.endsWith(".bin")) {
				selectedFile = new File(fileName + ".bin");
			}
			try {
				Graph.serializeGraph(selectedFile, graph);
				JOptionPane.showMessageDialog(null, "Saved to file " + selectedFile.getAbsolutePath());
			} catch (GraphException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, e.getMessage(), "Save error!", JOptionPane.ERROR_MESSAGE);
			}
		}
			
	}
	
	public void deserializeGraph(String fileName) {
		if(graph == null)
			return;
		
		if(!fileName.endsWith(".bin")) {
			fileName += ".bin";
		}
		try {
			//graph = Graph.deserializeGraph(fileName);
			this.setGraph(Graph.deserializeGraph(fileName));
			int lastId = getLastId(graph);
			Node.ID_COUNTER = lastId + 1;
			//JOptionPane.showMessageDialog(null, "Loaded from file " + fileName);
			repaint();
		} catch (GraphException e) {
			//JOptionPane.showMessageDialog(this, e.getMessage(), "Load error!", JOptionPane.ERROR_MESSAGE);
		}			
	}
	

	public void deserializeGraph() {
		if(graph == null)
			return;
		
		JFileChooser fc = new JFileChooser(".");
		fc.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Binary files *.bin", "bin");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		
		int choosenOption = fc.showOpenDialog(this);
		if(choosenOption == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			try {
				//graph = Graph.deserializeGraph(selectedFile);
				this.setGraph(Graph.deserializeGraph(selectedFile));
				JOptionPane.showMessageDialog(null, "Loaded from file " + selectedFile.getAbsolutePath());
				repaint();
			} catch (GraphException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(),"Error!", JOptionPane.ERROR_MESSAGE);
			}
		}

	}
	
	private int getLastId(Graph graph) {
		List<Node> nodeList = graph.getNodes();
		if (nodeList == null || nodeList.isEmpty())
			return 0;
		
		int maxId = 0;
		for (Node cn : nodeList) {
			try {				
				Integer intId = Integer.parseInt(cn.getId());
				if (intId > maxId) {
					maxId = intId;
				}
			} catch (Exception e) {
				//pass (this happens when identifiers are strings, not integers
			}
		}
		return maxId;
	}
	
	
	public void enableGrid(boolean drawGrid) {
		this.drawGrid = drawGrid;
		if(this.drawGrid) {
			grid.scaleGrid(getSize());
		}
		repaint();
	}
	
	public void showNodesList() {
		String nodesList = graph.getListOfNodes();
		JOptionPane.showMessageDialog(this, nodesList,"Nodes list", JOptionPane.INFORMATION_MESSAGE);
	}

	public void showEdgesList() {
		String nodesList = graph.getListOfEdges();
		JOptionPane.showMessageDialog(this, nodesList,"Edges list", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void exportFullJSON() {
		if(graph == null)
			return;
		
		JFileChooser fc = new JFileChooser(".");
		fc.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files *.json", "json");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);

		int choosenOption = fc.showSaveDialog(this);

		if (choosenOption == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			String fileName = selectedFile.getAbsolutePath();
			if(!fileName.endsWith(".json")) {
				selectedFile = new File(fileName + ".json");
			}
			try {
				
				new JSONWriter().writeCPAG(new GraphContainer(graph), selectedFile.getAbsolutePath());
				
				//JOptionPane.showMessageDialog(null, "Exported to JSON file " + selectedFile.getAbsolutePath());
				JOptionPane.showMessageDialog(null, "Exported to JSON file " + selectedFile.getCanonicalPath());
				
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "JSON export error!", JOptionPane.ERROR_MESSAGE);
			}
		}
			
	}
	
	public void exportCPAG() {
		if(graph == null)
			return;
		
		JFileChooser fc = new JFileChooser(".");
		fc.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files *.json", "json");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);

		int choosenOption = fc.showSaveDialog(this);

		if (choosenOption == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			String fileName = selectedFile.getAbsolutePath();
			if(!fileName.endsWith(".json")) {
				selectedFile = new File(fileName + ".json");
			}
			try {				
				AndOrGraph cpag = Graph.buildCPAG(graph); 
				new JSONWriter().writeCPAG(new CPAGContainer(cpag), selectedFile.getAbsolutePath());								
				
				//JOptionPane.showMessageDialog(null, "CPAG structure exported to JSON file " + selectedFile.getAbsolutePath());
				JOptionPane.showMessageDialog(null, "CPAG structure exported to JSON file " + selectedFile.getCanonicalPath());
				
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "JSON export error!", JOptionPane.ERROR_MESSAGE);
			}
		}
			
	}
	
	public boolean importJSON(String fileName) {
		if(graph == null)
			return false;
		
		if(!fileName.endsWith(".json")) {
			fileName += ".json";
		}
		try {
			//graph = Graph.importJSON(fileName);
			this.setGraph(Graph.importJSON(fileName));
			int lastId = getLastId(graph);
			Node.ID_COUNTER = lastId + 1;
			//JOptionPane.showMessageDialog(null, "JSON loaded from file: " + fileName);
						
			if (graph.isAutolayout()) {
				this.applyAutolayout(false); //topDown = false
			} else {
				repaint();
				graph.setAutolayout(true);
			}
			
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error importing JSON!", JOptionPane.ERROR_MESSAGE);
			return false;
		}
			
	}
	
	public void applyAutolayout(boolean topDown) {
		graph.applyLayout(this.getWidth(), this.getHeight(), topDown);
		repaint();
	}
	
	public boolean importJSON() {
		if(graph == null)
			return false;
		
		JFileChooser fc = new JFileChooser(".");
		fc.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files *.json", "json");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		
		int choosenOption = fc.showOpenDialog(this);
		if(choosenOption == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			
			String filepath = selectedFile.getAbsolutePath();
			
			try {
				filepath = selectedFile.getCanonicalPath();
				//System.out.println("File absolute path: " + selectedFile.getAbsolutePath());
				//System.out.println("File canonical path: " + selectedFile.getCanonicalPath());
				//System.out.println("File name: " + selectedFile.getName());
			} catch (IOException e) {
				//e.printStackTrace();
				return false;
			} 
			
			return this.importJSON(filepath);
			
			
		} else {
			return false;
		}

	}
	
	
	
	
	
	
	// --------------------------MOUSE
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(mouseLeftButton) {
			moveGraphDrag(e.getX(), e.getY());
		}else {
			setMouseCursor(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		setMouseCursor(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {	
		setMouseCursor(e);	
		
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub	
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub			
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//System.out.println("Event: mouse pressed");
		if(e.getButton() == MouseEvent.BUTTON1) {
			mouseLeftButton = true;
		}
		
		if(e.getButton() == MouseEvent.BUTTON3) {
			mouseRightButton = true;
		}
		
		setMouseCursor(e);		
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		//System.out.println("Event: mouse released");
		if(e.getButton() == MouseEvent.BUTTON1) {
			mouseLeftButton = false;
			finalizeAddEdge();
		}
		
		if(e.getButton() == MouseEvent.BUTTON3) {
			mouseRightButton = false;
			chooseNodeB = false;
			if(nodeUnderCursor != null) {
				createNodePopupMenu(e, nodeUnderCursor);
			}else if(edgeUnderCursor != null){
				createEdgePopupMenu(e, edgeUnderCursor);
			}else {
				createPlainPopupMenu(e);
			}
		}
		setMouseCursor(e);
		
		if (nodeUnderCursor != null) {
			this.notifyNodeClicked(nodeUnderCursor);
			
		}else if(edgeUnderCursor != null) {			
			this.notifyEdgeClicked(edgeUnderCursor);
		}
	}
	
	// --------------------------POPUP MENU
	
	private void createPlainPopupMenu(MouseEvent e){
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem newNodeMenuItem = new JMenuItem("New node");
		popupMenu.add(newNodeMenuItem);
		newNodeMenuItem.addActionListener((action)->{
			createNewNode(e.getX(), e.getY());
		});
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
		
	}
	
	private void createNodePopupMenu(MouseEvent e, Node n){
		JPopupMenu popupMenu = new JPopupMenu();
		
		
		JMenuItem addEdgeMenuItem = new JMenuItem("Add edge");
		popupMenu.add(addEdgeMenuItem);
		addEdgeMenuItem.addActionListener((action)->{
			initializeAddEdge(n);
		});
		
		popupMenu.addSeparator();
		
		/*
		JMenuItem setSourceMenuItem = new JMenuItem("Set as source");
		popupMenu.add(setSourceMenuItem);
		setSourceMenuItem.addActionListener((action)->{		
			setSourceNode(n);
		});
		*/
		
		//if(nodeUnderCursor instanceof Node) {	
		JMenuItem setTargetMenuItem = new JMenuItem("Set as target");
		popupMenu.add(setTargetMenuItem);
		setTargetMenuItem.addActionListener((action)->{		
			setTargetNode(n);
		});
		
		popupMenu.addSeparator();
		/*
		JMenuItem changeNodeRadiusMenuItem = new JMenuItem("Change node radius");
		popupMenu.add(changeNodeRadiusMenuItem);
		changeNodeRadiusMenuItem.addActionListener((action)->{		
			changeNodeRadius(n);
		});			
		*/
		JMenuItem changeTextMenuItem = new JMenuItem("Change node label");
		popupMenu.add(changeTextMenuItem);
		changeTextMenuItem.addActionListener((action)->{		
			changeNodeText(n);
		});
		
		JMenuItem changeValueMenuItem = new JMenuItem("Change node value");
		popupMenu.add(changeValueMenuItem);
		changeValueMenuItem.addActionListener((action)->{		
			changeNodeValue(n);
		});
		
		JMenuItem changeNodeColorMenuItem = new JMenuItem("Change node color");
		popupMenu.add(changeNodeColorMenuItem);
		changeNodeColorMenuItem.addActionListener((action)->{		
			changeNodeColor(n);
		});
		
		
		popupMenu.addSeparator();
		//}
		
		JMenuItem removeNodeMenuItem = new JMenuItem("Remove node");
		popupMenu.add(removeNodeMenuItem);
		removeNodeMenuItem.addActionListener((action)->{
			removeNode(n);
		});
		
		
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
		
	}
	

	private void createEdgePopupMenu(MouseEvent event, Edge e) {
		JPopupMenu popupMenu = new JPopupMenu();		
				
		//popupMenu.addSeparator();
		JMenuItem changeEdgeValueItem = new JMenuItem("Change edge value");
		popupMenu.add(changeEdgeValueItem);
		changeEdgeValueItem.addActionListener((action)->{
			changeEdgeValue(e);
		});
		
		popupMenu.addSeparator();
		JMenuItem changeEdgeStrokeMenuItem = new JMenuItem("Change edge size");
		popupMenu.add(changeEdgeStrokeMenuItem);
		changeEdgeStrokeMenuItem.addActionListener((action)->{
			changeEdgeStroke(e);
		});
		
		JMenuItem changeEdgeColorMenuItem = new JMenuItem("Change edge color");
		popupMenu.add(changeEdgeColorMenuItem);
		changeEdgeColorMenuItem.addActionListener((action)->{
			changeEdgeColor(e);
		});
		popupMenu.addSeparator();
		
				
		JMenuItem removeEdgeMenuItem = new JMenuItem("Remove edge");
		popupMenu.add(removeEdgeMenuItem);
		removeEdgeMenuItem.addActionListener((action)->{
			removeEdge(e);
		});
		
		popupMenu.show(event.getComponent(), event.getX(), event.getY());
	}
	
	// --------------------------KEYBOARD
	
	@Override
	public void keyPressed(KeyEvent e) {
		int moveDistance;
		if (e.isShiftDown())
			moveDistance = 10;
		else
			moveDistance = 1;
		
		switch(e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			moveGraphStep(-moveDistance, 0);
			break;
		case KeyEvent.VK_RIGHT:
			moveGraphStep(moveDistance, 0);
			break;
		case KeyEvent.VK_UP:
			moveGraphStep(0, -moveDistance);
			break;
		case KeyEvent.VK_DOWN:
			moveGraphStep(0, moveDistance);
			break;
		case KeyEvent.VK_DELETE:
			if(nodeUnderCursor != null) {
				graph.removeNode(nodeUnderCursor);
			}else if(edgeUnderCursor != null) {
				graph.removeEdge(edgeUnderCursor);
			}
			break;
		}
		
		if(e.isControlDown()) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_S:
				serializeGraph();
				break;
			case KeyEvent.VK_O:
				deserializeGraph();
				break;
			}
		}
		
		repaint();
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char key = e.getKeyChar();
		
		int quickChangeStep = 1;
		
		switch (key) {
		case 'r':
			quickSetColor(Color.RED);
			break;
		case 'g':
			quickSetColor(Color.GREEN);
			break;
		case 'b':
			quickSetColor(Color.BLUE);
			break;
		case 'q':
			enableGrid(!drawGrid);
			break;
		case 'z':
			createNewNode(mouseX, mouseY);
			break;
		case 'x':
			initializeAddEdge(nodeUnderCursor);
			break;		
		case '=':
			quickChangeSize(quickChangeStep);
			break;
		case '-':
			quickChangeSize(-quickChangeStep);
			break;
		}
		
	}
	
	private void quickChangeSize(int step){
		if(nodeUnderCursor instanceof Node) {
			nodeUnderCursor.changeRadius(step);
		}else if(edgeUnderCursor instanceof Edge) {
			edgeUnderCursor.changeStroke(step);
		}
	}

	private void quickSetColor(Color c) {
		if(nodeUnderCursor instanceof Node) {
			nodeUnderCursor.setColor(c);
		}else if(edgeUnderCursor instanceof Edge) {
			edgeUnderCursor.setColor(c);
		}
	}
	
	// --------------------------METHODS

	public void setMouseCursor(MouseEvent e) {
		if(e != null) {
			nodeUnderCursor = graph.findNodeUnderCursor(e.getX(), e.getY());
			if(nodeUnderCursor == null) {
				edgeUnderCursor = graph.findEdgeUnderCursor(e.getX(), e.getY());
			}
			mouseX = e.getX();
			mouseY = e.getY();
		}
		
		int mouseCursor;
		if (nodeUnderCursor != null) {
			mouseCursor = Cursor.HAND_CURSOR;
			//System.out.println("Node clicked: " + nodeUnderCursor.getLabel());
			//this.notifyNodeClicked(nodeUnderCursor);
		}else if(edgeUnderCursor != null) {
			mouseCursor = Cursor.CROSSHAIR_CURSOR;
			//this.notifyEdgeClicked(edgeUnderCursor);
		}else if(chooseNodeB) {
			mouseCursor = Cursor.WAIT_CURSOR;
		} else if (mouseLeftButton) {
			mouseCursor = Cursor.MOVE_CURSOR;
		} else {
			mouseCursor = Cursor.DEFAULT_CURSOR;
		}
		setCursor(Cursor.getPredefinedCursor(mouseCursor));
		
	}
	
	private void moveGraphDrag(int mx, int my) {
		int dx = mx - mouseX;
		int dy = my - mouseY;
		
		if(nodeUnderCursor != null) {
			nodeUnderCursor.move(dx, dy);
		}else if(edgeUnderCursor != null){
			edgeUnderCursor.move(dx, dy);
		}else {
			graph.moveGraph(dx, dy);
		}
		
		mouseX = mx;
		mouseY = my;
		repaint();
	}
	
	private void moveGraphStep(int dx, int dy) {
		graph.moveGraph(dx, dy);
		repaint();
	}
	
	private void createNewNode(int mx, int my) {
		try {
			NodeType[] generalNodeTypes = new NodeType[] {
					NodeType.CYBER_NODE, 
					NodeType.PHYSICAL_NODE, 
					NodeType.ACTION_NODE, 
					NodeType.LOGICAL_NODE, 
					NodeType.IMPACT_NODE, 
					NodeType.CUSTOM_NODE
				};
			NodeType nodeType = (NodeType) JOptionPane.showInputDialog(this, "Choose node type", "New node", JOptionPane.DEFAULT_OPTION, null, generalNodeTypes, NodeType.CYBER_NODE);

			if(nodeType == null) {
				throw new NullPointerException();
			}
			else if(nodeType == NodeType.LOGICAL_NODE) {
				
				NodeType[] opTypes = new NodeType[] {NodeType.AND_NODE, NodeType.OR_NODE, NodeType.SPLITTER_NODE};
				NodeType opType = (NodeType) JOptionPane.showInputDialog(this, "Choose operator type", "New node", JOptionPane.DEFAULT_OPTION, null, opTypes, NodeType.OR_NODE);
				
				String type = NodeType.getTypeMap().get(opType);
				logger.debug("New node type: " + type);
				graph.addNode(new Node(mx, my, type, type, ""));
									
			} else if(nodeType == NodeType.CUSTOM_NODE) {
				Color color = JColorChooser.showDialog(this, "Choose color", Color.GRAY);
				//Color color = Color.WHITE;						
				String label = JOptionPane.showInputDialog(this, "Custom node label:", "New CUSTOM node", JOptionPane.QUESTION_MESSAGE);
				if (label != null) {
					String type = JOptionPane.showInputDialog(this, "Custom node type:", "New CUSTOM node", JOptionPane.QUESTION_MESSAGE);
					if (type == null || type.isEmpty()) {
						type = "custom";
					}					
					String value = JOptionPane.showInputDialog(this, "Custom node value:", "New CUSTOM node value", JOptionPane.QUESTION_MESSAGE);
					if (value == null) {
						value = "none";
					}
					Node customNode = new Node(mx, my, label, type, value);
					customNode.setColor(color);
					graph.addNode(customNode);
				}
			} else {
				String label = JOptionPane.showInputDialog(this, "Node label:", "New " + nodeType.toString(), JOptionPane.QUESTION_MESSAGE);
				if (label != null) {
					String value = JOptionPane.showInputDialog(this, "Node value:", "New node value", JOptionPane.QUESTION_MESSAGE);
					if (value == null) {
						value = "none";
					}
					//graph.addNode(new Node(mx, my, color, text, type, value));
					String type = NodeType.getTypeMap().get(nodeType);
					graph.addNode(new Node(mx, my, label, type, value));
				}
			}			
			
			repaint();
			
			this.notifyGraphStructureOrValuesUpdated();
			
		}catch(NullPointerException e) {			
			JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	
	private void removeNode(Node n){
		graph.removeNode(n);
		if (n.equals(graph.getTarget())) {
			graph.setTarget(null);
		}
		repaint();
		
		this.notifyGraphStructureOrValuesUpdated();
	}
	
	private void initializeAddEdge(Node n) {
		if(nodeUnderCursor != null) {
			newEdgeNodeA = n;
			chooseNodeB = true;
			setMouseCursor(null);
		}
	}
	
	private void finalizeAddEdge() {
		if(chooseNodeB) {
			if(nodeUnderCursor != null) {
				if(nodeUnderCursor.equals(newEdgeNodeA)) {
					JOptionPane.showMessageDialog(this, "Choose different node!", "Error!", JOptionPane.ERROR_MESSAGE);
				}else {
					try {
						newEdgeNodeB = nodeUnderCursor;
						//EdgeType edgeType = (EdgeType) JOptionPane.showInputDialog(this, "Choose edge type", "New edge", JOptionPane.DEFAULT_OPTION, null, EdgeType.values(), EdgeType.BASIC_EDGE);
						//Color color = JColorChooser.showDialog(this, "Choose color", Color.BLACK);

						Color color = Color.BLACK;
						int stroke = 1; //((Integer)JOptionPane.showInputDialog(this, "Choose size", "New edge", JOptionPane.DEFAULT_OPTION, null, CPAGBaseEdge.STROKE_VALUES, CPAGBaseEdge.STROKE_VALUES[0])).intValue();
						
						Node nodeA = newEdgeNodeA;
						Node nodeB = newEdgeNodeB;
						String value = JOptionPane.showInputDialog(this, "Edge value:", "New edge value", JOptionPane.QUESTION_MESSAGE);
						if (value == null || value.isEmpty()) {
							value = "1.0";
						}
						String label = JOptionPane.showInputDialog(this, "Edge label:", "New edge", JOptionPane.QUESTION_MESSAGE);
						if (label == null) {
							label = "";
						}
						graph.addEdge(new Edge(nodeA, nodeB, color, stroke, value, label));
						
						repaint();
						
						this.notifyGraphStructureOrValuesUpdated();
						
					}catch (NullPointerException e){
						JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
					}
					
				}
			}
			chooseNodeB = false;
		}
	}
	
	private void removeEdge(Edge e) {
		graph.removeEdge(e);
		repaint();
		
		this.notifyGraphStructureOrValuesUpdated();
	}
	
	/*
	@SuppressWarnings("unused")
	private void changeNodeRadius(Node n) {
		try {
			int radius = ((Integer)JOptionPane.showInputDialog(this, "Choose radius:", "Edit node", JOptionPane.QUESTION_MESSAGE, null, Node.RADIUS_VALUES, Node.RADIUS_VALUES[0])).intValue();
			n.setR(radius);
			repaint();
		}catch (ClassCastException e) {
			JOptionPane.showMessageDialog(this, "This node cannot have different radius.", "Error!", JOptionPane.ERROR_MESSAGE);
		}catch (NullPointerException e) {
			JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	*/
	
	@SuppressWarnings("unused")
	private void setSourceNode(Node n) {		
		try {			
			this.graph.setSource(n.getId());
			repaint();
			
			this.notifyGraphStructureOrValuesUpdated();
			
		}catch(ClassCastException e) {
			JOptionPane.showMessageDialog(this, "This node cannot have text.", "Error!", JOptionPane.INFORMATION_MESSAGE);
		}catch (NullPointerException e) {
			JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private void setTargetNode(Node n) {		
		try {			
			this.graph.setTarget(n.getId());
			repaint();
			
			this.notifyGraphStructureOrValuesUpdated();
			
		}catch(ClassCastException e) {
			JOptionPane.showMessageDialog(this, "This node cannot have text.", "Error!", JOptionPane.INFORMATION_MESSAGE);
		}catch (NullPointerException e) {
			JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private void changeNodeColor(Node n) {
		try {
			Color color = JColorChooser.showDialog(this, "Choose new color", n.getColor());
			n.setColor(color);
			repaint();
		}catch(ClassCastException e){
			JOptionPane.showMessageDialog(this, "This node cannot have different color.", "Error!", JOptionPane.ERROR_MESSAGE);
		}catch (NullPointerException e) {
			//e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
			
	}

	private void changeNodeText(Node n) {
		//String text = JOptionPane.showInputDialog(this, "Input text:", "Edit node", JOptionPane.QUESTION_MESSAGE);
		Object[] values = null;
		String text = (String)JOptionPane.showInputDialog(this, "Input text:", "Edit node", JOptionPane.QUESTION_MESSAGE, null, values, n.getLabel());
		try {
			n.setLabel(text);
			repaint();
			
			this.notifyGraphStructureOrValuesUpdated();
			
		}catch(ClassCastException e) {
			JOptionPane.showMessageDialog(this, "This node cannot have text.", "Error!", JOptionPane.INFORMATION_MESSAGE);
		}catch (NullPointerException e) {
			JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private void changeNodeValue(Node n) {	
		//String value = JOptionPane.showInputDialog(this, "Node value:", "New node value", JOptionPane.QUESTION_MESSAGE);
		Object[] values = null;
		String value = (String)JOptionPane.showInputDialog(this, "Node value:", "New node value", JOptionPane.QUESTION_MESSAGE, null, values, n.getValue());
		
		try {
			n.setValue(value);
			repaint();
		}catch(ClassCastException e) {
			JOptionPane.showMessageDialog(this, "This node cannot have a value.", "Error!", JOptionPane.INFORMATION_MESSAGE);
		}catch (NullPointerException e) {
			JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private void changeEdgeStroke(Edge e) {
		try {
			int stroke = ((Integer)JOptionPane.showInputDialog(this, "Choose stroke", "Edit edge", JOptionPane.DEFAULT_OPTION, null, Edge.STROKE_VALUES, Edge.STROKE_VALUES[0])).intValue();
			e.setStroke(stroke);
			repaint();
		} catch (ClassCastException exc) {
			JOptionPane.showMessageDialog(this, "This edge cannot have different stroke.", "Error!", JOptionPane.INFORMATION_MESSAGE);
		}catch (NullPointerException exc) {
			JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}

	private void changeEdgeColor(Edge e){
		try {
			Color color = JColorChooser.showDialog(this, "Choose color", Color.BLACK);
			e.setColor(color);
			repaint();
		} catch (ClassCastException exc) {
			JOptionPane.showMessageDialog(this, "This edge cannot have different color.", "Error!", JOptionPane.INFORMATION_MESSAGE);
		}catch (NullPointerException exc) {
			JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	
	private void changeEdgeValue(Edge e){
		try {
			//String value = JOptionPane.showInputDialog(this, "Edge value:", "New edge value", JOptionPane.QUESTION_MESSAGE);
			Object[] values = null;
			String value = (String)JOptionPane.showInputDialog(this, "Edge value:", "New edge value", JOptionPane.QUESTION_MESSAGE, null, values, e.getValue());									
			e.setValue(value);			
			repaint();
			
			this.notifyGraphStructureOrValuesUpdated();
			
		} catch (ClassCastException exc) {
			JOptionPane.showMessageDialog(this, "This edge cannot have a different value.", "Error!", JOptionPane.INFORMATION_MESSAGE);
		}catch (NullPointerException exc) {
			JOptionPane.showMessageDialog(this, "Operation canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}

	// --------------------------COMPONENT_EVENT
	
	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		Object eSource = e.getSource();
		if(eSource == this && drawGrid) {
			grid.scaleGrid(getSize());
			repaint();
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	///////// Bayesian risk analysis ///////////
	
	public boolean bayesianAnalysis() {
		if(graph == null)
			return false;
		//JOptionPane.showMessageDialog(this, "Bayesian analysis is not implemented yet.", "Sorry...", JOptionPane.WARNING_MESSAGE);
		
		try {
			
			AndOrGraph cpag = Graph.buildCPAG(graph);
			
			if (cpag.getNodes().size() == 0) {
				throw new Exception("The current CPAG is empty.");				
			}
			
			for (AndOrEdge edge : cpag.getEdges()) {			
				String value = edge.getValue();
				
				try {
					Double d = Double.parseDouble(value);
					if (d < 0 || d > 1.0) {
						throw new Exception();
					}
				} catch (Exception e) {
					//throw new Exception("The value asssociated to edge '" + edge.getSource() + "'->'" + edge.getTarget() + "' (" + value +") is not a valid probability value. It should be a value between 0.0 and 1.0.");
					throw new Exception("The value '" + value + "' asssociated to edge '" + edge.getSource() + "'->'" + edge.getTarget() + "' is not a valid probability value.\nIt should be a value between 0.0 and 1.0.");
				}
				
			}
			
			Map<String,String> problems = new LinkedHashMap<String,String>();
			Map<String,String> warnings = new LinkedHashMap<String,String>();
			
			new GraphValidator().validate(cpag, problems, warnings);
			//this.displayValidationResults(problems, warnings);
			if (problems != null && !problems.isEmpty()) {
				this.displayValidationResults(problems, warnings);
				return false;
			} 
			
			logger.debug("Display graph source id: " + graph.getSource());  
			if (graph.getSource() == null) {
				String artificialSource = "_s_";
				String srcId = new GraphUtils().unifySources(cpag, artificialSource);
				//JOptionPane.showMessageDialog(this, "Please select the source of the CPAG", "CPAG source not selected", JOptionPane.ERROR_MESSAGE);
				//return false;				
				cpag.setSource(srcId);				
				
			} else {				
				AndOrNode sourceNode = cpag.getNode(graph.getSource());
				if (sourceNode == null) {
					// the last source of the display graph does not exist anymore
					String artificialSource = "_s_";
					String srcId = new GraphUtils().unifySources(cpag, artificialSource);
					cpag.setSource(srcId);
				}			
			}
			
			logger.debug("Display graph target id: " + graph.getTarget());
			if (graph.getTarget() == null) {
				String artificialTarget = "_t_";
				String tgtId = new GraphUtils().unifyTargets(cpag, artificialTarget);
				cpag.setTarget(tgtId);
				
				/*
				if (!artificialTarget.equalsIgnoreCase(tgtId)) {
					//Node n = graph.findNodeById(tgtId);					
					//JOptionPane.showMessageDialog(this, "Autoselected CPAG target => node:'" + tgtId + "',label:'" + n.getLabel() + "'", "Autoselected CPAG target", JOptionPane.INFORMATION_MESSAGE);
					graph.setTarget(tgtId);
				}
				*/
			/*
				if (tgtId == null) {
					JOptionPane.showMessageDialog(this, "Please select the target of the CPAG", "CPAG target not selected", JOptionPane.ERROR_MESSAGE);
					return false;
				} else {
					graph.setTarget(tgtId);
					Node n = graph.findNodeById(tgtId);
					JOptionPane.showMessageDialog(this, "Autoselected CPAG target => node:'" + tgtId + "',label:'" + n.getLabel() + "'", "Autoselected CPAG target", JOptionPane.INFORMATION_MESSAGE);
				}
			*/
			}									
			
			if (cpag.getSource().equalsIgnoreCase(cpag.getTarget())) {
				throw new Exception("The source and target of the CPAG are identical => choose different nodes.");
			}
			
			
			RiskAnalysis analyser = new RiskAnalysis(cpag);
			analyser.compute(); 
			Map<String,Double> riskMap = analyser.getRiskMap();
			
			this.graph.setNodesProperty(OperationsManager.BAYESIAN_KEY, riskMap);
			repaint();
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error...", JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
			return false;
		}
		
		
	}	

	public boolean setAutoBayesian(boolean enabled) {
		this.autoBayesian = enabled;
		OperationsManager.DISPLAY_BAYESIAN = this.autoBayesian;
		if (this.autoBayesian) {
			try {
				this.autoBayesian = this.bayesianAnalysis();
				if (!this.autoBayesian) {
					repaint();	
					this.notifyBayesianReset();
				}
			} catch (Exception e) {
				this.autoBayesian = false;
				
			}
		} else {
			repaint();			
		}
		OperationsManager.DISPLAY_BAYESIAN = this.autoBayesian;
		return this.autoBayesian;
	}
	
	public boolean getAutoBayesian() {
		return this.autoBayesian;
	}
	
	public void notifyGraphStructureOrValuesUpdated() {
		//logger.debug("Graph values or structure updated -> notifying listeners...");
		if (this.autoBayesian) {
			this.setAutoBayesian(true);
		} 
		
		if (this.autoHardening) {
			this.hardeningAnalysis();
		} 
	}
	
	
	
	///////// HARDENING ///////////
	
	public boolean hardeningAnalysis() {
		if(graph == null)
			return false;
		
		if (OperationsManager.HARDENING_ENABLED){
			try {
				AndOrGraph cpag = Graph.buildCPAG(this.graph);
				
				if (graph.getSource() == null) {
					String artificialSource = "_s_";
					String srcId = new GraphUtils().unifySources(cpag, artificialSource);				
					cpag.setSource(srcId);
					if (!artificialSource.equalsIgnoreCase(srcId)) {					
						graph.setSource(srcId);
					} else {
						List<AndOrEdge> outgoingEdges = cpag.getOutgoingEdges(cpag.getSource());
						for (AndOrEdge e : outgoingEdges) {
							e.setValue(TCityConstants.Infinite);
						}
						//JOptionPane.showMessageDialog(this, "Multiple sources detected. An artificial source node has been created.", "Source unification", JOptionPane.INFORMATION_MESSAGE);
					}
					
				}
				if (graph.getTarget() == null) {
					String artificialTarget = "_t_";
					String tgtId = new GraphUtils().unifyTargets(cpag, artificialTarget);
					cpag.setTarget(tgtId);
					if (!artificialTarget.equalsIgnoreCase(tgtId)) {
						graph.setTarget(tgtId);
					} else {
						List<AndOrEdge> incomingEdges = cpag.getIncomingEdges(cpag.getTarget());
						for (AndOrEdge e : incomingEdges) {
							e.setValue(TCityConstants.Infinite);
						}
					}
				}	
							
				HardeningAnalysis hardeningAnalysis = new HardeningAnalysis(cpag);			
				hardeningAnalysis.compute();
				
				try {
					List<AndOrEdge> solutionEdges = new ArrayList<AndOrEdge>();
					for (AndOrNode solNode : hardeningAnalysis.getSolution().getNodes()) {
						solutionEdges.add(hardeningAnalysis.getLineGraph().getNodeEdgeMapping().get(solNode));
					}
					//String hardeningGraphFilepath = new HardeningAndOrGraphExporter().exportSolutionGraph(this.graph, GraphExporterFactory.PNG_FORMAT, inputFilepath, solutionEdges);
					
					this.graph.setEdgesProperty(OperationsManager.HARDENING_KEY, solutionEdges);
					repaint();
				} catch (Exception e) {
					logger.error(e.getMessage());	
					e.printStackTrace();
					return false;
				}		
				
			} catch (Exception e) {			
				e.printStackTrace();			
				JOptionPane.showMessageDialog(this, e.getMessage(), "Hardening error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;			
		} else {
			JOptionPane.showMessageDialog(this, "Hardening analysis is not available yet.", "Sorry...", JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}
	
	public boolean setAutoHardening(boolean enabled) {
		this.autoHardening = enabled;
		OperationsManager.DISPLAY_HARDENING = this.autoHardening;
		if (this.autoHardening) {
			try {
				this.autoHardening = this.hardeningAnalysis();								
			} catch (Exception e) {
				this.autoHardening = false;								
			}
		} else {
			repaint();			
		}
		OperationsManager.DISPLAY_HARDENING = this.autoHardening;
		return this.autoHardening;
	}
	
	public boolean getAutoHardening() {
		return this.autoHardening;
	}	
	
	///////// CPAG Validation ///////////
	
	public void validateCPAG() {
		if(graph == null)
			return;
		
		//JOptionPane.showMessageDialog(this, "CPAG validation is not implemented yet.", "Sorry...", JOptionPane.WARNING_MESSAGE);
		AndOrGraph cpag = null;
		try {
			cpag = Graph.buildCPAG(graph);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "CPAG generation exception", JOptionPane.ERROR_MESSAGE);
			return;		
		}
		
		
		Map<String,String> problems = new LinkedHashMap<String,String>();
		Map<String,String> warnings = new LinkedHashMap<String,String>();
		
		new GraphValidator().validate(cpag, problems, warnings);
		this.displayValidationResults(problems, warnings);
		
	}
	
	public void displayValidationResults(Map<String,String> problems, Map<String,String> warnings) {
		if (problems == null || problems.isEmpty()) {
			
			String message = "The current CPAG is valid.\n";
			
			if (warnings != null && !warnings.isEmpty()) {
				String warningList = "";
				for (Entry<String,String> e : warnings.entrySet()) {
					warningList += "[!] " + e.getKey() + ". " + e.getValue() + "\n";
				}
				message += "\n** Warnings **\n" + warningList;
			}
			JOptionPane.showMessageDialog(this, message, "Passed", JOptionPane.INFORMATION_MESSAGE);
		} else {
			String errorList = "** Problems **\n";
			
			for (Entry<String,String> e : problems.entrySet()) {
				errorList += "[!] " + e.getKey() + ". " + e.getValue() + "\n";
			}
			
			if (warnings != null && !warnings.isEmpty()) {
				String warningList = "";
				for (Entry<String,String> e : warnings.entrySet()) {
					warningList += "- " + e.getKey() + ". " + e.getValue() + "\n";
				}
				errorList += "\n** Warnings **\n" + warningList;
			}
			
			JOptionPane.showMessageDialog(this, errorList, "Problems found", JOptionPane.WARNING_MESSAGE);
		}
	}

	
	///////// CPAG MERGING ///////////
	
	public void mergeCPAGs() {
		if(graph == null)
			return;
		
		JFileChooser fc = new JFileChooser(".");
		fc.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files *.json", "json");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		
		int choosenOption = fc.showOpenDialog(this);
		if(choosenOption == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			
			String filepath = selectedFile.getAbsolutePath();
			
			try {
				filepath = selectedFile.getCanonicalPath();
				//System.out.println("File absolute path: " + selectedFile.getAbsolutePath());
				//System.out.println("File canonical path: " + selectedFile.getCanonicalPath());
				//System.out.println("File name: " + selectedFile.getName());
				
				Graph graph2 = Graph.importJSON(filepath);
				
				//int lastId = getLastId(graph);
				//Node.ID_COUNTER = lastId + 1;
				//JOptionPane.showMessageDialog(null, "JSON loaded from file: " + filepath);
				
				//Graph mergedGraph = this.mergeCPAGs(this.graph, graph2); 		
				Graph mergedGraph = this.mergeCPAGs(this.graph, graph2);
				this.setGraph(mergedGraph);
				
				logger.debug("Graphs successfully merged!"); 
				
				this.applyAutolayout(false); //topDown = false
				/*
				if (graph.isAutolayout()) {
					this.applyAutolayout(false); //topDown = false
				} else {
					repaint();
					graph.setAutolayout(true);
				}
				*/
				
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, e.getMessage(), "Merging error", JOptionPane.ERROR_MESSAGE);
			} 
			
			//this.importJSON(filepath);			
			
		}
		//JOptionPane.showMessageDialog(this, "CPAG merging is not implemented yet.", "Sorry...", JOptionPane.WARNING_MESSAGE);
				
	}
	
	
	
	public Graph mergeCPAGs(Graph g1, Graph g2) throws Exception {
		
		AndOrGraph cpag1 = Graph.buildCPAG(g1);
		AndOrGraph cpag2 = Graph.buildCPAG(g2);
		
		GraphMerger merger = new GraphMerger();		
		AndOrGraph cpag = merger.merge(cpag1, cpag2);
		
		Graph mergedGraph = Graph.buildGraphFromCPAG(cpag);
		return mergedGraph;
	}

	
	
	
	
	
	
	
	@SuppressWarnings("unused")
	private Graph mergeCPAGsMixed(Graph g1, Graph g2) throws Exception {
		
		Graph g = new Graph("Merged CPAG");
		
		
		logger.debug("Merge started...");
		
		
		// Copy base graph G1 into G
		for (Node n : g1.getNodes()) {
			g.addNode(n);
		}
		
		for (Edge e : g1.getEdges()) {
			g.addEdge(e);
		}
		
		g.setSource(g1.getSource());
		g.setTarget(g1.getTarget());
		
		// Extend G with G2
		
		Map<String,Node> sameLabelNodesIdMap = new LinkedHashMap<String,Node>(); // {old key, new key}
		Map<String,String> newIdMap = new LinkedHashMap<String,String>();
		
		for (Node n : g2.getNodes()) {
			
			boolean nodeMerged = false;
			
			Node sameLabelNode = g.findNodeByLabel(n.getLabel());
			if (sameLabelNode != null) {
				logger.debug("Same label found: " + n.getLabel());
				
				// Valid for privilege nodes only
				String type = n.getType();
				if (NodeDisplayFactory.isPrivilegeType(type)) {
					logger.debug("=> merging security privileges... ");
					sameLabelNodesIdMap.put(n.getId(), sameLabelNode);
					nodeMerged = true;
				}
				
			} 
			
			if (!nodeMerged) {			
				Node x = g.findNodeById(n.getId());
				if (x != null) {
					logger.debug("Nodes with the same id found. ID: " + n.getId());
					// repeated ID
					String newId = String.valueOf(Node.ID_COUNTER++);
					newIdMap.put(n.getId(), newId);
					n.setId(newId);
				} 
				g.addNode(n);
			}
		}
		
		for (Edge e : g2.getEdges()) {
			String edgeSrcId = e.getSource();
			String edgeTgtId = e.getTarget();
			
			if (sameLabelNodesIdMap.containsKey(edgeSrcId)) {
				Node newSrc = sameLabelNodesIdMap.get(edgeSrcId);
				e.setNodeA(newSrc);
				e.setSource(newSrc.getId());
			}
			if (sameLabelNodesIdMap.containsKey(edgeTgtId)) {
				Node newTgt = sameLabelNodesIdMap.get(edgeTgtId);
				e.setNodeB(newTgt);
				e.setTarget(newTgt.getId());
			}
			
			
			if (newIdMap.containsKey(e.getSource())) {
				e.setSource(newIdMap.get(e.getSource()));
			}
			if (newIdMap.containsKey(e.getTarget())) {
				e.setTarget(newIdMap.get(e.getTarget()));
			}
			
			g.addEdge(e);
		}
		
		AndOrGraph cpag = Graph.buildCPAG(g);
		
		GraphMerger merger = new GraphMerger();
		// Place an OR node before security privileges with more than one input
		merger.unifyDisjunctiveConditions(cpag);
				
		// Collapse same logical nodes
		//this.collapseLogicalNodesCubic(g);
		merger.collapseLogicalNodesQuad(cpag);				

		// Remove redundant OR nodes
		merger.removeRedundantDisjunctions(cpag);
		
		
		Graph mergedGraph = Graph.buildGraphFromCPAG(cpag);
		return mergedGraph;
	}
	
}
