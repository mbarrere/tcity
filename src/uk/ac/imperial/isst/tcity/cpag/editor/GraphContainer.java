package uk.ac.imperial.isst.tcity.cpag.editor;

import java.io.Serializable;

public class GraphContainer implements Serializable {

	public static final long serialVersionUID = 5673009196816218789L;

	private Graph graph;
	
	public GraphContainer() {
		super();
		this.graph = null;
	}
	
	public GraphContainer(Graph graph) {
		super();
		this.graph = graph;
	}

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}
	
	
}
