package uk.ac.imperial.isst.tcity.metrics.bayesian;
/**
 * 
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.IBayesInferrer;
import org.eclipse.recommenders.jayes.inference.jtree.JunctionTreeAlgorithm;

import uk.ac.imperial.isst.tcity.TCityConstants;
import uk.ac.imperial.isst.tcity.model.AndOrEdge;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.util.Tarjan;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class BayesianTransformer {

	final static Logger logger = LogManager.getLogger(BayesianTransformer.class);
	
	public BayesianTransformer() {
		
	}
	
	private List<BayesNode> getBayesNodeParents(List<AndOrNode> parents, Map<String,BayesNode> bayesMap) {
		List<BayesNode> bayesParents = new ArrayList<BayesNode>();
		
		for (AndOrNode n : parents) {
			//logger.info("Parent node in core graph: " + n.getId());
			BayesNode bNode = bayesMap.get(n.getId());
			if (bNode != null) { 
				//logger.info("Parent node is in the map (already processed) with Bayes id: " + bNode.getId());
				bayesParents.add(bNode);
			} else {
				//the current bNode is not ready for being processed
				//logger.info("Parent node is NOT the map (not processed)");
				break;
			}
		}
		return bayesParents;
	}
	
	private BayesNet getBayesNetForInputGraph (AndOrGraph graph, AndOrNode source, AndOrNode target) throws Exception {
				
		try {
			boolean hasLoops = new Tarjan().hasLoops(graph);
			if (hasLoops) {
				throw new Exception("Unable to compute Bayesian net => input graph contains loops");
			} else {				
				logger.debug("Acyclic input graph: OK!");
			}
		} catch (Exception e) {			
			e.printStackTrace();
			throw new Exception(e);
		}
		
		BayesNet net = new BayesNet();		
		
		//List<Node> nodes = coreGraph.getNodes();
		Map<String,BayesNode> bayesMap = new HashMap<String,BayesNode>();
		Map<Integer,AndOrNode> reverseBayesMap = new HashMap<Integer,AndOrNode>();
		
		List<AndOrNode> queue = new ArrayList<AndOrNode>();
		queue.add(graph.getNode(source.getId()));
		
		while (!queue.isEmpty()) {
			AndOrNode n = queue.remove(0);
			logger.debug("Processing node " + n.getId());
			
			BayesNode bNode = bayesMap.get(n.getId());
			
			if (bNode != null) {
				logger.debug("Node " + n.getId() + " already processed");
				continue;
			}		
			
			List<AndOrNode> parents = graph.getIncomingNodes(n.getId());
			List<BayesNode> bNodeParents = this.getBayesNodeParents(parents, bayesMap);
			
			if (bNodeParents.size() != parents.size()) {
				// node not ready to be processed
				logger.debug("Postponing node " + n.getId());
				queue.add(n);
			} else {
				logger.debug("Node " + n.getId() + " ready for processing");
				bNode = net.createNode(n.getId());			
				bNode.addOutcomes("true", "false");
				
				bayesMap.put(n.getId(), bNode);
				reverseBayesMap.put(bNode.getId(),n);
				
				bNode.setParents(bNodeParents);
			    
				if (bNodeParents.size() == 0) {							    
				    bNode.setProbabilities(1.0, 0.0);		
				    //logger.info("P(X=T) for node " + n.getId() + " = " + 1.0);
			    	//logger.info("P(X=F) for node " + n.getId() + " = " + 0.0);				    
				} else {
			    
					int exp = bNodeParents.size()+1;
				    int tableSize = new Double (Math.pow(2, exp)).intValue(); //size = 2^#parents+1
				    logger.debug("Probability table size: 2^" + exp + " = " + tableSize + " = " + Integer.toString(tableSize,2) + "b");
				    double probabilityTable[] = new double[tableSize];
				    
				    int binaryTotalLenght = bNodeParents.size();			    
				    logger.debug("Binary total lenght = number of parents = " + binaryTotalLenght);
				    
				    for (int i = 0; i < tableSize; i=i+2) {				    	
				    	logger.debug("Table position (i)" + i);
				    	String currentBinary = Integer.toString(i/2,2);
				    	int currentBinaryLenght = currentBinary.length();
				    	
				    	for (int index = 0; index < (binaryTotalLenght-currentBinaryLenght); index++) {
				    		currentBinary = "0" + currentBinary;			    		
				    	}
				    	
				    	logger.debug("Binary at position " + i + " = " + currentBinary);
				    	
				    	double q = 1.0;
				    	
				    	if (n.isAndType()) {
				    		for (int index = 0; index < binaryTotalLenght; index++) {
					    		//logger.info("q value before index " + (index) + ": " + q);
					    		char bit = currentBinary.charAt(index);			    		
					    		
					    		//combination = combination + ((bit == '0')?"T":"F") ;
					    		AndOrNode parent = reverseBayesMap.get(bNode.getParents().get(index).getId());
					    		//logger.info("bNode.getParents().get(" + index + "): " + bNode.getParents().get(index).getId());
					    		//logger.info("Parent: " + parent);
					    		
					    		double incomingLinkScore = Double.parseDouble(graph.getEdge(parent.getId(), n.getId()).getValue());
					    		
					    		logger.debug("(AND type) Link score between parent '" + (parent.getId()) + "' and node '" + n.getId() + "' => " + incomingLinkScore);
					    		//logger.info("Link involvement " + ((bit == '0')?"T":"F"));
					    		//double incomingIncomingFailure = 1 - incomingLinkScore;
					    		
					    		// q is computed for probability of FALSE, 1 - q for TRUE
					    		// 0 is the bit value for P(Xi=TRUE) in the BayesNet ordering
					    		// 000 => TTT, 111 => FFF, 101 => FTF
					    		q = q * ((bit == '0')?incomingLinkScore:0.0); 
					    	}
				    		q = 1.0 - q;
				    	} else {				    		
				    		// Compute Noisy-OR operator
					    	for (int index = 0; index < binaryTotalLenght; index++) {
					    		//logger.info("q value before index " + (index) + ": " + q);
					    		char bit = currentBinary.charAt(index);			    		
					    		
					    		//combination = combination + ((bit == '0')?"T":"F") ;
					    		AndOrNode parent = reverseBayesMap.get(bNode.getParents().get(index).getId());
					    		//logger.info("bNode.getParents().get(" + index + "): " + bNode.getParents().get(index).getId());
					    		//logger.info("Parent: " + parent);
					    		
					    		AndOrEdge edge = graph.getEdge(parent.getId(), n.getId());
					    		logger.trace("Edge: " + edge);
					    		if (edge.getValue().equalsIgnoreCase(TCityConstants.Infinite)) {
					    			//edge.setValue(Double.toString(Double.MAX_VALUE));
					    			edge.setValue("1.0");
					    		}
					    		if (edge.getValue().equalsIgnoreCase(TCityConstants.NotApplicable)) {
					    			edge.setValue("1.0");
					    		}
					    		double incomingLinkScore = Double.parseDouble(edge.getValue());
					    		
					    		logger.debug("Link score between parent '" + (parent.getId()) + "' and node '" + n.getId() + "' => " + incomingLinkScore);
					    		//logger.info("Link involvement " + ((bit == '0')?"T":"F"));
					    		//double incomingIncomingFailure = 1 - incomingLinkScore;
					    		
					    		// q is computed for probability of FALSE, 1 - q for TRUE
					    		// 0 is the bit value for P(Xi=TRUE) in the BayesNet ordering
					    		// 000 => TTT, 111 => FFF, 101 => FTF
					    		q = q * ((bit == '0')?1.0-incomingLinkScore:1.0); 
					    	}
				    	}
				    	//logger.info("P(X=T) at prob[" + (i) + "]=" + (1-q));
				    	probabilityTable[i] = 1.0-q; //P(X=T)
				    	
				    	//logger.info("P(X=F) at prob[" + (i+1) + "]=" + q);
				    	probabilityTable[i+1] = q; //P(X=F)
				    	
				    }
				    
				    
				    bNode.setProbabilities(probabilityTable);
				    /*
				    bNode.setProbabilities(ArrayFlatten.flatten(new double[][] { { 0.53, 0.47 }, // s = true
				            { 0.0, 1.0 } // s = false
				    }));
				    */
				    
				    /*
				    logger.info("Probability table for node " + n.getId());
				    for (int i = 0; i < probabilityTable.length; i++) {
				    	logger.info("P(" + i + ") = " + probabilityTable[i]);
				    }
				    */
				}
			    
			    List<AndOrNode> children = graph.getOutgoingNodes(n.getId());
			    for (AndOrNode c : children) {
			    	if (bayesMap.get(c.getId()) == null) {
			    		queue.add(c);
			    	}
			    }
			    
			    
			}
		    
		}
		
		
	    /*
	    BayesNode t = net.createNode("t");
	    t.addOutcomes("true", "false");
	    t.setParents(Arrays.asList(a, b));
	    // @formatter:off
	    t.setProbabilities(ArrayUtils.flatten(new double[][][] { 
	    { // a = true
	        { 0.9475, 0.0525 }, // b = true
	        { 0.93, 0.07 } // b = false	            
	    }, { // a = false
	        { 0.25, 0.75 }, // b = true
	        { 0.0, 1.0 } // b = false	            
	    } }));
	    */
	    
	    return net;
	}
	
	public BayesianAndOrGraph createBayesianAndOrGraph (AndOrGraph graph, AndOrNode source, AndOrNode target) throws Exception {
		logger.info("Bayesian. Computing inconditional probabilities for graph G(V=" + graph.getNodes().size() + ",E=" + graph.getEdges().size() + ")");
		
		if (graph.getNodes().size() == 0 || graph.getEdges().size() == 0) {
			throw new Exception("Unable to compute Bayesian net => input graph is empty");
		}
		
		long start = System.currentTimeMillis();
		BayesNet net = this.getBayesNetForInputGraph(graph, source, target);
	    
	    IBayesInferrer inferer = new JunctionTreeAlgorithm();
		inferer.setNetwork(net);

		BayesNode bayesSourceNode = net.getNode(source.getId());
		//logger.info("bayesSourceNode: " + bayesSourceNode);
		BayesNode bayesTargetNode = net.getNode(target.getId());
		//logger.info("bayesTargetNode: " + bayesTargetNode);
		logger.info("Bayes => sourceNode: " + bayesSourceNode + ", targetNode: " + bayesTargetNode);
		
		Map<BayesNode,String> evidence = new HashMap<BayesNode,String>();
		evidence.put(bayesSourceNode, "true");		
		//evidence.put(net.getNode("h8"), "true");
		inferer.setEvidence(evidence);

		
		BayesianAndOrGraph bgraph = new BayesianAndOrGraph(); 
		bgraph.setBayesNet(net);
		bgraph.setAndOrGraph(graph);
		
		for (AndOrNode n : graph.getNodes()) {
			/*
			NaggenNode<Node> naggenNode = new AtomicNaggenNode<Node>(n.getId());
			if (n.getName() == null) {
				naggenNode.setName(n.getId());
			} else {
				naggenNode.setName(n.getName());
			}
			ngraph.addNode(naggenNode);	
			//logger.info("=*=*=> Naggen node " + naggenNode.getId() + " added to naggen graph");
			
			((AtomicNaggenNode<Node>)naggenNode).addNode(n);
			*/
			
			BayesNode bNode = net.getNode(n.getId());
			double[] beliefs = inferer.getBeliefs(bNode);
			
			logger.debug("Beliefs bayesNode: " + bNode.getId());
			//for (int i = 0; i < beliefs.length; i++) {
			//	logger.debug("P(" + n.getId() + "=" + (1-i) + "): " + beliefs[i]);
			//}
			
			List<Double> probs = new ArrayList<Double>();
			//for(double bvalue : beliefs) {
			for (int i = 0; i < beliefs.length; i++) {
				double bvalue = beliefs[i];									
				BigDecimal bd = new BigDecimal(bvalue).setScale(14, RoundingMode.HALF_UP);
			    double roundedValue = bd.doubleValue();
			    logger.debug("P(" + n.getId() + "=" + (1-i) + "): " + roundedValue);
				probs.add(new Double(roundedValue));
				
			}
		    
			bgraph.addNodeBeliefs(n, probs);								
			
		}
		
		//Edges
		/*
		for (Entry<Node,Map<Node,List<VulnerableLink>>> sourceEntry : coreGraph.getLinksBySourceGroupedByTarget().entrySet()) {
			Node sourceNode = sourceEntry.getKey();
			
			for (Entry<Node,List<VulnerableLink>> targetEntry : sourceEntry.getValue().entrySet()) {
				Node targetNode = targetEntry.getKey();
				
				//List<VulnerableLink> links = targetEntry.getValue();
				CoreLink coreLink = null;
				if (coreGraph.getCoreGraphMetaDescritor() != null) {
					coreLink = coreGraph.getCoreGraphMetaDescritor().getEdge(sourceNode, targetNode);
				} else {
					AttackPath mlap = new AttackPath();
					mlap.addNewNode(sourceNode, sourceNode.getVulnerabilityInstances());
					mlap.addNewNode(targetNode, coreGraph.getAllVulnerabilityInstancesBetweenSourceAndTarget(sourceNode, targetNode));
					coreLink = new CoreLink(sourceNode, targetNode, mlap, coreGraph.getMaxProbLinkFromSourceToTarget(sourceNode, targetNode).getProbability());
				}
				List<CoreLink> links = new ArrayList<CoreLink>();
				links.add(coreLink);
				
				NaggenNode<Node> naggenSourceNode = ngraph.getNode(sourceNode.getId());
				NaggenNode<Node> naggenTargetNode = ngraph.getNode(targetNode.getId());
				
				NaggenLink<Node,CoreLink> naggenLink = new NaggenLink<Node,CoreLink>(naggenSourceNode, naggenTargetNode, links);
				logger.debug("==> Adding link: " + naggenSourceNode + " -> " + naggenTargetNode + " " + coreLink.stepsToTargetHashTag());
				ngraph.addLink(naggenSourceNode, naggenTargetNode, naggenLink);					
			}
		}			
		*/
		//ngraph.display();
		
		
		//bgraph.buildIndexByNativeNodes();
		
		//logger.info("Bayesian target belief: " + bgraph.getNaggenNodeForNativeNode(target).getCompromiseBelief());
		
		long end = System.currentTimeMillis();
		logger.info("[TIME] Bayesian generation took " + (end-start)+ " ms (" + (end-start+500)/1000 + " seconds)");
		return bgraph;
	}
	
	
	
	public BayesianAndOrGraph createBayesianAndOrGraphWithEvidence (AndOrGraph graph, AndOrNode source, AndOrNode target, Map<String, Boolean> evidenceMap) throws Exception {
		logger.info("Bayesian. Computing inconditional probabilities for graph G(V=" + graph.getNodes().size() + ",E=" + graph.getEdges().size() + ")");
		
		if (graph.getNodes().size() == 0 || graph.getEdges().size() == 0) {
			throw new Exception("Unable to compute Bayesian net => input graph is empty");
		}
		
		long start = System.currentTimeMillis();
		BayesNet net = this.getBayesNetForInputGraph(graph, source, target);
	    
	    IBayesInferrer inferer = new JunctionTreeAlgorithm();
		inferer.setNetwork(net);

		BayesNode bayesSourceNode = net.getNode(source.getId());
		//logger.info("bayesSourceNode: " + bayesSourceNode);
		BayesNode bayesTargetNode = net.getNode(target.getId());
		//logger.info("bayesTargetNode: " + bayesTargetNode);
		logger.info("Bayes => sourceNode: " + bayesSourceNode + ", targetNode: " + bayesTargetNode);
		
		Map<BayesNode,String> evidence = new HashMap<BayesNode,String>();
		evidence.put(bayesSourceNode, "true");				
				
		for (Entry<String,Boolean> e : evidenceMap.entrySet()) {
			BayesNode bayesNode = net.getNode(e.getKey());			
			evidence.put(bayesNode, e.getValue().toString());
			logger.info("Adding evidence: nodeId=" + e.getKey() + ", bayesNode="+ bayesNode + " => " + e.getValue().toString());
		}
		
		inferer.setEvidence(evidence);
		
		BayesianAndOrGraph bgraph = new BayesianAndOrGraph(); 
		bgraph.setBayesNet(net);
		bgraph.setAndOrGraph(graph);
		
		for (AndOrNode n : graph.getNodes()) {
			/*
			NaggenNode<Node> naggenNode = new AtomicNaggenNode<Node>(n.getId());
			if (n.getName() == null) {
				naggenNode.setName(n.getId());
			} else {
				naggenNode.setName(n.getName());
			}
			ngraph.addNode(naggenNode);	
			//logger.info("=*=*=> Naggen node " + naggenNode.getId() + " added to naggen graph");
			
			((AtomicNaggenNode<Node>)naggenNode).addNode(n);
			*/
			
			BayesNode bNode = net.getNode(n.getId());
			double[] beliefs = inferer.getBeliefs(bNode);
			
			logger.debug("Beliefs bayesNode: " + bNode.getId());
			//for (int i = 0; i < beliefs.length; i++) {
			//	logger.debug("P(" + n.getId() + "=" + (1-i) + "): " + beliefs[i]);
			//}
			
			List<Double> probs = new ArrayList<Double>();
			//for(double bvalue : beliefs) {
			for (int i = 0; i < beliefs.length; i++) {
				double bvalue = beliefs[i];									
				BigDecimal bd = new BigDecimal(bvalue).setScale(14, RoundingMode.HALF_UP);
			    double roundedValue = bd.doubleValue();
			    logger.debug("P(" + n.getId() + "=" + (1-i) + "): " + roundedValue);
				probs.add(new Double(roundedValue));
				
			}
		    
			bgraph.addNodeBeliefs(n, probs);								
			
		}
		
		//Edges
		/*
		for (Entry<Node,Map<Node,List<VulnerableLink>>> sourceEntry : coreGraph.getLinksBySourceGroupedByTarget().entrySet()) {
			Node sourceNode = sourceEntry.getKey();
			
			for (Entry<Node,List<VulnerableLink>> targetEntry : sourceEntry.getValue().entrySet()) {
				Node targetNode = targetEntry.getKey();
				
				//List<VulnerableLink> links = targetEntry.getValue();
				CoreLink coreLink = null;
				if (coreGraph.getCoreGraphMetaDescritor() != null) {
					coreLink = coreGraph.getCoreGraphMetaDescritor().getEdge(sourceNode, targetNode);
				} else {
					AttackPath mlap = new AttackPath();
					mlap.addNewNode(sourceNode, sourceNode.getVulnerabilityInstances());
					mlap.addNewNode(targetNode, coreGraph.getAllVulnerabilityInstancesBetweenSourceAndTarget(sourceNode, targetNode));
					coreLink = new CoreLink(sourceNode, targetNode, mlap, coreGraph.getMaxProbLinkFromSourceToTarget(sourceNode, targetNode).getProbability());
				}
				List<CoreLink> links = new ArrayList<CoreLink>();
				links.add(coreLink);
				
				NaggenNode<Node> naggenSourceNode = ngraph.getNode(sourceNode.getId());
				NaggenNode<Node> naggenTargetNode = ngraph.getNode(targetNode.getId());
				
				NaggenLink<Node,CoreLink> naggenLink = new NaggenLink<Node,CoreLink>(naggenSourceNode, naggenTargetNode, links);
				logger.debug("==> Adding link: " + naggenSourceNode + " -> " + naggenTargetNode + " " + coreLink.stepsToTargetHashTag());
				ngraph.addLink(naggenSourceNode, naggenTargetNode, naggenLink);					
			}
		}			
		*/
		//ngraph.display();
		
		
		//bgraph.buildIndexByNativeNodes();
		
		//logger.info("Bayesian target belief: " + bgraph.getNaggenNodeForNativeNode(target).getCompromiseBelief());
		
		long end = System.currentTimeMillis();
		logger.info("[TIME] Bayesian generation took " + (end-start)+ " ms (" + (end-start+500)/1000 + " seconds)");
		return bgraph;
	}
	
	
	
	/*
	
	
	public NaggenGraph<Node,CoreLink> createBayesianNaggenGraph (AttackGraph coreGraph, Node source, Node target) throws AttackGraphTransformationException {
		logger.info("Bayesian. Computing inconditional probabilities for attack graph G(V=" + coreGraph.getNodesCount() + ",E=" + coreGraph.getEdgesCount() + ")");
		
		if (coreGraph.getNodesCount() == 0 || coreGraph.getEdgesCount() == 0) {
			throw new AttackGraphTransformationException("Unable to compute Bayesian net => core graph is empty");
		}
		
		long start = System.currentTimeMillis();
		BayesNet net = this.getBayesNetForCoreGraph(coreGraph, source, target);
	    
	    IBayesInferrer inferer = new JunctionTreeAlgorithm();
		inferer.setNetwork(net);

		BayesNode bayesSourceNode = net.getNode(source.getId());
		//logger.info("bayesSourceNode: " + bayesSourceNode);
		BayesNode bayesTargetNode = net.getNode(target.getId());
		//logger.info("bayesTargetNode: " + bayesTargetNode);
		logger.info("Bayes => sourceNode: " + bayesSourceNode + ", targetNode: " + bayesTargetNode);
		
		Map<BayesNode,String> evidence = new HashMap<BayesNode,String>();
		evidence.put(bayesSourceNode, "true");		
		//evidence.put(net.getNode("h8"), "true");
		inferer.setEvidence(evidence);

		
		NaggenGraph<Node,CoreLink>  ngraph = new NaggenGraph<Node,CoreLink>();
		ngraph.setBayesNet(net);
		
		for (Node n : coreGraph.getNodes()) {
			NaggenNode<Node> naggenNode = new AtomicNaggenNode<Node>(n.getId());
			if (n.getName() == null) {
				naggenNode.setName(n.getId());
			} else {
				naggenNode.setName(n.getName());
			}
			ngraph.addNode(naggenNode);	
			//logger.info("=*=*=> Naggen node " + naggenNode.getId() + " added to naggen graph");
			
			((AtomicNaggenNode<Node>)naggenNode).addNode(n);
			
			BayesNode bNode = net.getNode(n.getId());
			double[] beliefs = inferer.getBeliefs(bNode);
			
			logger.debug("Beliefs bayesNode: " + bNode.getId());
			for (int i = 0; i < beliefs.length; i++) {
				logger.debug("P(" + target.getId() + "=" + (1-i) + "): " + beliefs[i]);
				naggenNode.setBeliefs(beliefs);
			}
		}
			
		for (Entry<Node,Map<Node,List<VulnerableLink>>> sourceEntry : coreGraph.getLinksBySourceGroupedByTarget().entrySet()) {
			Node sourceNode = sourceEntry.getKey();
			
			for (Entry<Node,List<VulnerableLink>> targetEntry : sourceEntry.getValue().entrySet()) {
				Node targetNode = targetEntry.getKey();
				
				//List<VulnerableLink> links = targetEntry.getValue();
				CoreLink coreLink = null;
				if (coreGraph.getCoreGraphMetaDescritor() != null) {
					coreLink = coreGraph.getCoreGraphMetaDescritor().getEdge(sourceNode, targetNode);
				} else {
					AttackPath mlap = new AttackPath();
					mlap.addNewNode(sourceNode, sourceNode.getVulnerabilityInstances());
					mlap.addNewNode(targetNode, coreGraph.getAllVulnerabilityInstancesBetweenSourceAndTarget(sourceNode, targetNode));
					coreLink = new CoreLink(sourceNode, targetNode, mlap, coreGraph.getMaxProbLinkFromSourceToTarget(sourceNode, targetNode).getProbability());
				}
				List<CoreLink> links = new ArrayList<CoreLink>();
				links.add(coreLink);
				
				NaggenNode<Node> naggenSourceNode = ngraph.getNode(sourceNode.getId());
				NaggenNode<Node> naggenTargetNode = ngraph.getNode(targetNode.getId());
				
				NaggenLink<Node,CoreLink> naggenLink = new NaggenLink<Node,CoreLink>(naggenSourceNode, naggenTargetNode, links);
				logger.debug("==> Adding link: " + naggenSourceNode + " -> " + naggenTargetNode + " " + coreLink.stepsToTargetHashTag());
				ngraph.addLink(naggenSourceNode, naggenTargetNode, naggenLink);					
			}
		}			
		
		//ngraph.display();
		ngraph.buildIndexByNativeNodes();
		
		logger.info("Bayesian target belief: " + ngraph.getNaggenNodeForNativeNode(target).getCompromiseBelief());
		long end = System.currentTimeMillis();
		logger.info("[TIME] Bayesian generation took " + (end-start)+ " ms (" + (end-start+500)/1000 + " seconds)");
		return ngraph;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// from NaggenGraph to NaggenGraph 
	
	
	private List<BayesNode> getBayesNodeNaggenParents(Set<NaggenNode<Node>> parents, Map<String,BayesNode> bayesMap) {
		List<BayesNode> bayesParents = new ArrayList<BayesNode>();
		
		for (NaggenNode<Node> n : parents) {
			//logger.info("Parent node in core graph: " + n.getId());
			BayesNode bNode = bayesMap.get(n.getId());
			if (bNode != null) { 
				//logger.info("Parent node is in the map (already processed) with Bayes id: " + bNode.getId());
				bayesParents.add(bNode);
			} else {
				//the current bNode is not ready for being processed
				//logger.info("Parent node is NOT the map (not processed)");
				break;
			}
		}
		return bayesParents;
	}
	
	
	private BayesNet getBayesNetForNaggenGraph (NaggenGraph<Node,CoreLink> ngraph, Node source, NaggenNode<Node> naggenEvidenceNode, Map<NaggenNode<Node>,Map<Vulnerability,Double>> kmap) throws AttackGraphTransformationException {
		
		try {
			boolean hasLoops = new NaggenCycleAnalyser<Node,CoreLink>().hasLoops(ngraph);
			if (hasLoops) {
				throw new AttackGraphTransformationException("Unable to compute Bayesian net => Naggen graph contains loops");
			} else {
				logger.debug("Acyclic Naggen graph: OK!");
			}
		} catch (AttackGraphGenerationException e) {			
			e.printStackTrace();
			throw new AttackGraphTransformationException(e);
		}
		
		BayesNet net = new BayesNet();
		
		//List<Node> nodes = coreGraph.getNodes();
		Map<String,BayesNode> bayesMap = new HashMap<String,BayesNode>();
		Map<Integer,NaggenNode<Node>> reverseBayesMap = new HashMap<Integer,NaggenNode<Node>>();
		
		List<NaggenNode<Node>> queue = new ArrayList<NaggenNode<Node>>();
		
		NaggenNode<Node> naggenSourceNode = ngraph.getNaggenNodeForNativeNode(source);
		//logger.info("Naggen node for native source node " + source.getId() + " => " + naggenSourceNode.getId());
		//queue.add(naggenSourceNode);
		for (NaggenNode<Node> n : ngraph.getNodes()) {
			Set<NaggenNode<Node>> incNodes = ngraph.getIncomingNodesToTarget(n);
			if (incNodes == null || incNodes.size() == 0) {
				queue.add(n);
			}
		}
		
		while (!queue.isEmpty()) {
			NaggenNode<Node> n = queue.remove(0);
			logger.debug("Processing naggen node " + n.getId());
			
			BayesNode bNode = bayesMap.get(n.getId());
			
			if (bNode != null) {
				logger.debug("NaggenNode " + n.getId() + " already processed");
				continue;
			}		
			
			Set<NaggenNode<Node>> parents = ngraph.getIncomingNodesToTarget(n);
			List<BayesNode> bNodeParents = this.getBayesNodeNaggenParents(parents, bayesMap);
			
			if (bNodeParents.size() != parents.size()) {
				// node not ready to be processed
				logger.debug("Postponing node " + n.getId());
				queue.add(n);
			} else {
				logger.debug("Node " + n.getId() + " ready for processing");
				bNode = net.createNode(n.getId());			
				bNode.addOutcomes("true", "false");
				
				bayesMap.put(n.getId(), bNode);
				reverseBayesMap.put(bNode.getId(),n);
				
				bNode.setParents(bNodeParents);
			    
				if (bNodeParents.size() == 0) {
					//special case... assign 1.0 just to the Internet
					if (naggenSourceNode.equals(n)) {
						bNode.setProbabilities(1.0, 0.0);
					} else {
						//horrible workaround for dependencies
						//FIXME!!!
						if (n.getId().startsWith("DEP_")) {
							double truthValue = 0.001;
						
							if (naggenEvidenceNode != null) {
								Vulnerability nMaxVuln = ((AtomicNaggenNode<Node>)n).getMaxVulnerability();
								if (((AtomicNaggenNode<Node>)naggenEvidenceNode).getMaxVulnerability().equals(nMaxVuln)) {
									truthValue = 1.0;
								} else {
									if (kmap != null) {
										Map<Vulnerability,Double> beliefsEvNode = kmap.get(naggenEvidenceNode);
										logger.info("BeliefsEvNode: " + beliefsEvNode);
										Double scoreBelief = beliefsEvNode.get(nMaxVuln);
										if (scoreBelief != null) {
											logger.info("=> Beliefs found for vuln " + nMaxVuln.getId() + " with value: " + scoreBelief);
											truthValue = scoreBelief;
										}
									}
								}
							}
							bNode.setProbabilities(truthValue, 1-truthValue);
							//Map<NaggenNode<Node>,Map<Vulnerability,Double>> kmap
						} else {
							//
							//Vulnerability vuln = ((AtomicNaggenNode)n).getMaxVulnerability();
							//if (vuln != null) { 
							//	double prob = vuln.getScore()/10.0;
							//	bNode.setProbabilities(prob, 1-prob);
							//} else {
							//	bNode.setProbabilities(0.0, 1.0);
							//}
							//
							bNode.setProbabilities(0.001, 0.999);
						}
					}
				    //logger.info("P(X=T) for node " + n.getId() + " = " + 1.0);
			    	//logger.info("P(X=F) for node " + n.getId() + " = " + 0.0);				    
				} else {
			    
					int exp = bNodeParents.size()+1;
				    int tableSize = new Double (Math.pow(2, exp)).intValue(); //size = 2^#parents+1
				    logger.debug("Probability table size: 2^" + exp + " = " + tableSize + " = " + Integer.toString(tableSize,2) + "b");
				    double probabilityTable[] = new double[tableSize];
				    
				    int binaryTotalLenght = bNodeParents.size();			    
				    logger.debug("Binary total lenght = number of parents = " + binaryTotalLenght);
				    
				    for (int i = 0; i < tableSize; i=i+2) {
				    	//logger.info("");
				    	logger.debug("Table position " + i);
				    	String currentBinary = Integer.toString(i/2,2);
				    	int currentBinaryLenght = currentBinary.length();
				    	
				    	for (int index = 0; index < (binaryTotalLenght-currentBinaryLenght); index++) {
				    		currentBinary = "0" + currentBinary;			    		
				    	}
				    	
				    	logger.debug("Binary at position " + i + " = " + currentBinary);
				    	
				    	double q = 1.0;
				    	for (int index = 0; index < binaryTotalLenght; index++) {
				    		//logger.info("q value before index " + (index) + ": " + q);
				    		char bit = currentBinary.charAt(index);			    		
				    		
				    		//combination = combination + ((bit == '0')?"T":"F") ;
				    		NaggenNode<Node> parent = reverseBayesMap.get(bNode.getParents().get(index).getId());
				    		//logger.info("bNode.getParents().get(" + index + "): " + bNode.getParents().get(index).getId());
				    		//logger.info("Parent: " + parent);
				    		double incomingLinkScore = ngraph.getProbabilityFromSourceToTarget(parent, n);
				    		logger.debug("Link score between parent " + (parent.getId()) + " and node " + n.getId() + " => " + incomingLinkScore);
				    		//logger.info("Link involvement " + ((bit == '0')?"T":"F"));
				    		//double incomingIncomingFailure = 1 - incomingLinkScore;
				    		
				    		// q is computed for probability of FALSE, 1 - q for TRUE
				    		// 0 is the bit value for P(Xi=TRUE) in the BayesNet ordering
				    		// 000 => TTT, 111 => FFF, 101 => FTF
				    		q = q * ((bit == '0')?1-incomingLinkScore:1); 
				    	}
				    	
				    	//logger.info("P(X=T) at prob[" + (i) + "]=" + (1-q));
				    	probabilityTable[i] = 1-q;
				    	
				    	//logger.info("P(X=F) at prob[" + (i+1) + "]=" + q);
				    	probabilityTable[i+1] = q;
				    	
				    }
				    
				    
				    bNode.setProbabilities(probabilityTable);
				    
				    //bNode.setProbabilities(ArrayFlatten.flatten(new double[][] { { 0.53, 0.47 }, // s = true
				     //       { 0.0, 1.0 } // s = false
				    //}));
				    
				    
				    
				    //logger.info("Probability table for node " + n.getId());
				    //for (int i = 0; i < probabilityTable.length; i++) {
				    //	logger.info("P(" + i + ") = " + probabilityTable[i]);
				    //}
				    
				}
			    
			    Set<NaggenNode<Node>> children = ngraph.getOutgoingNodesFromSource(n);
			    for (NaggenNode<Node> c : children) {
			    	if (bayesMap.get(c.getId()) == null) {
			    		queue.add(c);
			    	}
			    }			  
			}		    
		}
		
		return net;
	}
		
	// Disclaimer: changes the input Naggen graph 
	public NaggenGraph<Node,CoreLink> toBayesianNaggenGraph (NaggenGraph<Node,CoreLink> ngraph, Node source, Node target) throws AttackGraphTransformationException {
		return this.toBayesianNaggenGraphMain(ngraph, source, target, null, null);
	}
	
	// Disclaimer: changes the input Naggen graph 
	public NaggenGraph<Node,CoreLink> toBayesianNaggenGraphWithEvidence (NaggenGraph<Node,CoreLink> ngraph, Node source, Node target, Node evidenceNode, Map<NaggenNode<Node>,Map<Vulnerability,Double>> kmap) throws AttackGraphTransformationException {
		NaggenNode<Node> naggenEvidenceNode = ngraph.getNaggenNodeForNativeNode(evidenceNode);
		logger.info("naggenEvidenceNode: " + evidenceNode);
		
		return this.toBayesianNaggenGraphMain(ngraph, source, target, naggenEvidenceNode, kmap);		
	}
	
	// Disclaimer: changes the input Naggen graph 
	private NaggenGraph<Node,CoreLink> toBayesianNaggenGraphMain (NaggenGraph<Node,CoreLink> ngraph, Node source, Node target, NaggenNode<Node> naggenEvidenceNode, Map<NaggenNode<Node>,Map<Vulnerability,Double>> kmap) throws AttackGraphTransformationException {
		logger.info("Computing Bayesian beliefs for Naggen Graph");
		
		logger.info("Source: " + source.getId());
		logger.info("Target: " + target.getId());
		//ngraph.display();
		
		if (ngraph.getNodes().isEmpty()) {
			throw new AttackGraphTransformationException("NaggenGraph is empty, NO ACTION TAKEN on Bayesian analysis.");
		}
				
		if (!ngraph.hasIndexByNativeNodes()) {
			throw new AttackGraphTransformationException("NaggenGraph has not an index by native nodes");
		}
				
		BayesNet net = this.getBayesNetForNaggenGraph(ngraph, source, naggenEvidenceNode, kmap);
	    
	    IBayesInferrer inferer = new JunctionTreeAlgorithm();
		inferer.setNetwork(net);

		NaggenNode<Node> naggenSourceNode = ngraph.getNaggenNodeForNativeNode(source);
		NaggenNode<Node> naggenTargetNode = ngraph.getNaggenNodeForNativeNode(target);
		logger.info("naggenSourceNode: " + naggenSourceNode);
		logger.info("naggenTargetNode: " + naggenTargetNode);
		
		BayesNode bayesSourceNode = net.getNode(naggenSourceNode.getId());
		logger.debug("bayesSourceNode: " + bayesSourceNode);
		BayesNode bayesTargetNode = net.getNode(naggenTargetNode.getId());
		logger.debug("bayesTargetNode: " + bayesTargetNode);
		
		Map<BayesNode,String> evidence = new HashMap<BayesNode,String>();
		evidence.put(bayesSourceNode, "true");				
		//testing
		//BayesNode middleNode = net.getNode(ngraph.getNaggenNodeForNativeNode(new Node("h7")).getId());
		//evidence.put(middleNode, "true");
		//evidence.put(bayesTargetNode, "true");
		if (naggenEvidenceNode != null) {
			BayesNode bayesEvidenceNode = net.getNode(naggenEvidenceNode.getId());
			logger.debug("bayesEvidenceNode: " + bayesEvidenceNode);
			evidence.put(bayesEvidenceNode, "true");
		}	
		
		inferer.setEvidence(evidence);

		
		ngraph.setBayesNet(net);
		
		for (NaggenNode<Node> n : ngraph.getNodes()) {
			
			BayesNode bNode = net.getNode(n.getId());
			double[] beliefs = inferer.getBeliefs(bNode);
			
			logger.debug("Beliefs bayesNode: " + bNode.getId());
			for (int i = 0; i < beliefs.length; i++) {
				logger.debug("P(" + target.getId() + "=" + (1-i) + "): " + beliefs[i]);
				n.setBeliefs(beliefs);
			}
		}
		
		//ngraph.display();
		ngraph.buildIndexByNativeNodes();
		return ngraph;
	}
	*/
	
}
