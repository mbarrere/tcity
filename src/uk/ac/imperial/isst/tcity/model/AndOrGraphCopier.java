package uk.ac.imperial.isst.tcity.model;

public class AndOrGraphCopier {

	public AndOrGraph cleanCopy (AndOrGraph g) {
		AndOrGraph graph = new AndOrGraph();
				
		for (AndOrNode n : g.getNodes()) {
			try {
				graph.addNode(n);
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
		
		for (AndOrEdge e : g.getEdges()) {
			try {
				graph.addEdge(e);
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
		
		graph.setSource(g.getSource());
		graph.setTarget(g.getTarget());
		
		return graph;
	}
}
