package uk.ac.imperial.isst.tcity.cpag.editor;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class GraphTreePanel extends JPanel implements TreeSelectionListener, GraphListener {
	
	private static final long serialVersionUID = 3544581658578869882L;
	private JTree tree;
	private JEditorPane viewPane;
	private DefaultMutableTreeNode rootNode;  
	private DefaultTreeModel treeModel;
	
	//Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = true;
	
	public GraphTreePanel(Graph graph) {
        super(new GridLayout(1,0));    
        
        //Create the nodes.
        this.rootNode = new DefaultMutableTreeNode("Graph");
        this.treeModel = new DefaultTreeModel(rootNode);
        
        createNodes(rootNode, graph);

        //Create a tree that allows one selection at a time.
        //tree = new JTree(rootNode);
        tree = new JTree(this.treeModel);
        
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);
        
        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        viewPane = new JEditorPane();
        viewPane.setEditable(false);
        //initHelp();
        JScrollPane scrollingView = new JScrollPane(viewPane);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(scrollingView);

        Dimension minimumSize = new Dimension(100, 100);
        scrollingView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        //splitPane.setDividerLocation(this.getHeight()/2); 
        splitPane.setDividerLocation(300);
        splitPane.setPreferredSize(new Dimension(200, 300));

        //Add the split pane to this panel.
        add(splitPane);
        
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

    }
	
	public void clearTree() {
	    this.rootNode.removeAllChildren();
	    this.viewPane.setText("");
	    this.treeModel.reload(this.rootNode);
	}
	
	private void createNodes(DefaultMutableTreeNode top, Graph graph) {
	 
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode item = null;
        //int counter = 1;
        
        //System.out.println("Creating tree nodes from graph: " + graph.toString());

        category = new DefaultMutableTreeNode("Nodes");
        top.add(category);

        for (Node n : graph.getNodes()) {
        	//System.out.println("\tNode: " + n.toString());
        	//item = new DefaultMutableTreeNode(n.getLabel() + " {id:'" + n.getId() + "', type:'" + n.getType() + "', value:'" + n.getValue() + "'}");
        	//item = new DefaultMutableTreeNode("'" + n.getId() + "': (" + n.getX() + "," + n.getY() + ") -> {id:'" + n.getId() + "', type:'" + n.getType() + "', value:'" + n.getValue() + "'}");
        	//item = new DefaultMutableTreeNode("'" + n.getId() + "' : (" + n.getX() + "," + n.getY() + ") - {label:'" + n.getLabel() + "', type:'" + n.getType() + "', value:'" + n.getValue() + "'}");
        	item = new DefaultMutableTreeNode(n);
        	category.add(item);
        	//category.add(new DefaultMutableTreeNode("id: " + counter++));
        }

        category = new DefaultMutableTreeNode("Edges");
        top.add(category);

        for (Edge e : graph.getEdges()) {
        	item = new DefaultMutableTreeNode(e);
        	//CPAGBaseEdge edge = (CPAGBaseEdge)e;
        	//item = new DefaultMutableTreeNode("('" + edge.getSource() + "'->'" + edge.getTarget()+ "'): {value:'" + edge.getValue() + "', label:'" + edge.getValue() + "'}");
        	item = new DefaultMutableTreeNode(e);
        	category.add(item);
        }	       
    }
	 
	 /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode item = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();

        if (item == null) return;

        Object itemInfo = item.getUserObject();
                
        if (item.isLeaf()) {    
        	if (itemInfo instanceof Node) {
        		Node node = (Node)itemInfo;
	    		displayNodeInfo(node);
            }
        	if (itemInfo instanceof Edge) {
        		Edge edge = (Edge)itemInfo;
	    		displayEdgeInfo(edge);
            }           
        } else {
        	displayInfo("Click on nodes and edges to display information");
        }
        //displayInfo("Class item: " + item.getClass() + " | Class itemInfo: " + itemInfo.getClass());        
        //displayInfo(itemInfo.toString());
    }
	
    private void displayInfo(String info) {
        if (info != null) {
            viewPane.setText(info);
        } 
    }
    
    private void displayNodeInfo(Node node) {
        if (node != null) {
        	String info = "Node: '" + node.getLabel() + "'";
        	info += "\n" + "- identifier: '" + node.getId() + "'";
        	info += "\n" + "- type: '" + node.getType() + "'";
        	info += "\n" + "- value: '" + node.getValue() + "'";
        	
        	Map<String,Object> props = node.getProperties(); 
        	if (props != null && !props.isEmpty()) {
        		info += "\n\n" + "Properties: ";
        		for (Entry<String,Object> e : props.entrySet()) {
        			info += "\n'" + e.getKey() + "' : '" + e.getValue() +"'";
        		}
        	}
            viewPane.setText(info);
        } 
    }
    
    private void displayEdgeInfo(Edge edge) {
        if (edge != null) {        	
        	String info = "Edge: " + "('" + edge.getSource() + "'->'" + edge.getTarget()+ "')";
        	info += "\n" + "- label: '" + edge.getLabel() + "'";        	
        	info += "\n" + "- value: '" + edge.getValue() + "'";
            viewPane.setText(info);
            //viewPane.setText(edge.toString());
        } 
    }
    
    public void graphUpdated(Graph graph) {
    	this.clearTree();    	    
        this.createNodes(this.rootNode, graph);    
        this.setTreeExpandedState(this.tree, true);
        //this.repaint();
		//System.out.println("Graph tree panel notified of graph updates");
	}
    
    public void setTreeExpandedState(JTree tree, boolean expanded) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
        this.setNodeExpandedState(tree, node, expanded);
    }

    @SuppressWarnings("unchecked")
    public void setNodeExpandedState(JTree tree, DefaultMutableTreeNode node, boolean expanded) {
        ArrayList<DefaultMutableTreeNode> list = Collections.list(node.children());
        for (DefaultMutableTreeNode treeNode : list) {
            setNodeExpandedState(tree, treeNode, expanded);
        }
        if (!expanded && node.isRoot()) {
            return;
        }
        TreePath path = new TreePath(node.getPath());
        if (expanded) {
            tree.expandPath(path);
        } else {
            tree.collapsePath(path);
        }
    }
    
    @SuppressWarnings("unchecked")
	public void nodeClicked(Graph graph, Node node) {    	
    	try {    		
	    	//System.out.println("TreePanel: Node clicked: " + node.getLabel());	    	
	    	DefaultMutableTreeNode nodesNode = (DefaultMutableTreeNode)Collections.list(this.rootNode.children()).get(0);
	    	ArrayList<DefaultMutableTreeNode> nodeList = Collections.list(nodesNode.children());
	    	
	    	for (DefaultMutableTreeNode treeNode : nodeList) {
	    		Object itemInfo = treeNode.getUserObject();
	    		//System.out.println("TreePanel node: " + itemInfo);
	    		 
         		Node x = (Node)itemInfo;
         		if (node.equals(x)) {
         			this.tree.setSelectionPath(new TreePath(this.treeModel.getPathToRoot(treeNode)));
         			//displayInfo(node.toString());
         			return;
         		} 	    			             
	        }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}    	
    }
    
    @SuppressWarnings("unchecked")
	public void edgeClicked(Graph graph, Edge edge) {
		
		try {			
			//System.out.println("TreePanel: Edge clicked: " + e.getLabel());	 
			
	    	DefaultMutableTreeNode edgesNode = (DefaultMutableTreeNode)Collections.list(this.rootNode.children()).get(1);
	    	ArrayList<DefaultMutableTreeNode> edgeList = Collections.list(edgesNode.children());
	    	
	    	for (DefaultMutableTreeNode treeNode : edgeList) {
	    		Object itemInfo = treeNode.getUserObject();
	    		//System.out.println("TreePanel node: " + itemInfo);
	    		 
	    		Edge x = (Edge)itemInfo;
         		if (edge.equals(x)) {
         			this.tree.setSelectionPath(new TreePath(this.treeModel.getPathToRoot(treeNode)));
         			//displayInfo(node.toString());
         			return;
         		} 	    			             
	        }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}  
	}
    

	@Override
	public void bayesianReset(Graph graph) {
		//pass		
	}

}
