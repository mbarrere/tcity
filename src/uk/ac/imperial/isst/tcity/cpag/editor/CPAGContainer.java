package uk.ac.imperial.isst.tcity.cpag.editor;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;

public class CPAGContainer {

	private AndOrGraph graph;
	
	public CPAGContainer(AndOrGraph graph) {
		super();
		this.graph = graph;
	}

	public AndOrGraph getGraph() {
		return graph;
	}

	public void setGraph(AndOrGraph graph) {
		this.graph = graph;
	}
	
	
}
