package uk.ac.imperial.isst.tcity.cpag.editor;

import java.io.Serializable;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */

public interface GraphListener extends Serializable{

	public static final long serialVersionUID = 5673009196816218789L;

	public void graphUpdated(Graph graph);
	public void nodeClicked(Graph graph, Node node);
	public void edgeClicked(Graph graph, Edge edge);
	
	public void bayesianReset(Graph graph);
}
