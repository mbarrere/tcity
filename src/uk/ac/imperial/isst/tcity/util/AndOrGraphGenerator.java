/**
 * 
 */
package uk.ac.imperial.isst.tcity.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class AndOrGraphGenerator {

	private static final Logger logger = LogManager.getLogger(AndOrGraphGenerator.class);
	
	private String source;
	private String target;
	private int maxNodesCount;
	private int andsCount = 0;
	private int orsCount = 0;
	private int atomicCount = 0;
		
	private double andProportion = 0.0;	
	private double orProportion = 0.0;	
	private double atomicProportion = 0.0;
	
	public AndOrGraphGenerator(String source, String target, int maxNodesCount,
			double andProportion, double orProportion, double atomicProportion) {
		super();
		this.source = source;
		this.target = target;
		this.maxNodesCount = maxNodesCount;
		this.andProportion = andProportion;
		this.orProportion = orProportion;
		this.atomicProportion = atomicProportion;
	}
	
	private String getNewType() {
		
		double limit1 = this.andProportion;
		double limit2 = limit1 + this.orProportion;
		double limit3 = limit2 + this.atomicProportion;
		
		double rangeMin = 0;
		double rangeMax = limit3;
		
		Random r = new Random();
		double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
		
		if (randomValue <= limit1) {
			return "and";
		}
		if (randomValue > limit1 && randomValue <= limit2) {
			return "or";
		}		
		return "atomic";
	}
	
	@SuppressWarnings("unused")
	private String getNewTypeOld2() {
		Random rand = new Random();
		int n = rand.nextInt(2) + 1;
		//double draw = rand.nextDouble();		
		if (n == 1) return "and";
		return "atomic";
	}
	
	@SuppressWarnings("unused")
	private String getNewTypeOld() {
		Random rand = new Random();
		int n = rand.nextInt(3) + 1;
		//double draw = rand.nextDouble();
		if (n == 1) return "and";
		if (n == 2) return "or";
		return "atomic";
	}
	
	private String getNewValue(boolean useInt) {
		Random rand = new Random();		
		
		if (useInt) {
			Integer i = rand.nextInt(100) + 1;
			return String.valueOf(i);
		} else {
			DecimalFormat df = new DecimalFormat("#.####");
			df.setRoundingMode(RoundingMode.CEILING);
			
			double d = rand.nextDouble() * 50;
			return df.format(d);		
		}
	}
	
	public AndOrGraph genGraph() throws Exception {
		return this.genGraph(false);		
	}
	
	public AndOrGraph genGraph(boolean useInt) throws Exception {
		AndOrGraph graph = new AndOrGraph();
		
		//AndOrNode source = new AndOrNode("s", "init", "inf", "s");
		AndOrNode tgtNode = new AndOrNode(this.target, "actuator", "inf", this.target);
		graph.addNode(tgtNode);
		graph.setTarget(tgtNode.getId());
		Queue<AndOrNode> queue = new LinkedBlockingQueue<AndOrNode>();
		queue.add(tgtNode);
				
		int nCount = 1;
		String nodePrefix = "n";
		
		while (!queue.isEmpty()) {
			AndOrNode n = queue.poll();						
			
			if (n.isAtomicType()) {
				this.atomicCount++;
				if (nCount < this.maxNodesCount) {	
					String id = nodePrefix + nCount;	
					nCount++;
					String type = this.getNewType();
					String value = this.getNewValue(useInt);
					AndOrNode child = new AndOrNode(id, type, value, id);					
					graph.addNode(child);
					graph.addEdge(new AndOrEdge(child.getId(), n.getId(), "inf", "none"));
					queue.add(child);
				}				
			}
			if (n.isAndType() || n.isOrType()) {
				if (n.isAndType()) { this.andsCount++; }
				if (n.isOrType()) { this.orsCount++; }
				String preValue = n.getValue();
				n.setValue("none");
				
				int childrenCount = 2;	//Proportion here
				if ( (nCount + childrenCount) > this.maxNodesCount) {
					childrenCount = this.maxNodesCount - nCount;
					if (childrenCount < 2) {
						//childrenCount = 2;						
						if (n.isAndType()) { this.andsCount--; }
						if (n.isOrType()) { this.orsCount--; }
						n.setType("atomic");
						n.setValue(preValue);
						this.atomicCount++;
						childrenCount = 0;
						//System.out.println("Node " + n.getId() + ", current count: " + nCount);
					}
				}
				for (int i = 0; i< childrenCount; i++) {
					String id = nodePrefix + nCount;
					nCount++;
					String type = this.getNewType();
					String value = this.getNewValue(useInt);
					AndOrNode child = new AndOrNode(id, type, value, id);
					graph.addNode(child);
					graph.addEdge(new AndOrEdge(child.getId(), n.getId(), "inf", "none"));
					queue.add(child);
				}
			}		
			
		}
		
		//System.out.println("Graph: " + graph);
		
		//String artificialSource = GraphUtils.ARTIFICIAL_SOURCE;
		GraphUtils utils = new GraphUtils();
	 	String problemSource = utils.unifySources(graph, this.source);
	 	graph.setSource(problemSource);
	 		 	
	 	utils.verifyInputGraph(graph);	 	 	
	 	utils.showNodeDistribution(graph);
	 	
	 	if (logger.isDebugEnabled()) {
		 	System.out.println("");	 	
		 	System.out.println("Graph #nodes: " + graph.getNodes().size());
		 	System.out.println("Graph #atomic: " + this.atomicCount);
		 	System.out.println("Graph +1 artificial source: " + 1); 
		 	System.out.println("Graph #ands: " + this.andsCount);
		 	System.out.println("Graph #ors: " + this.orsCount);
		 	System.out.println("Graph #edges: " + graph.getEdges().size());
	 	}
		return graph;		
	}
	
}
