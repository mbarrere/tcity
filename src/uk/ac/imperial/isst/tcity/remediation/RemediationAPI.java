package uk.ac.imperial.isst.tcity.remediation;

import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class RemediationAPI {

	public Double getRemediationCost(AndOrGraph graph, AndOrNode node) {
		return new Double(0.6);
	}
	
	public Double getRemediationCost(AndOrGraph graph, AndOrEdge edge) {
		return new Double(0.6);
	}
}
