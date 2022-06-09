/**
 * 
 */
package uk.ac.imperial.isst.tcity.maxsat.cnf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.TCity;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class TseitinStructure {

	final static Logger logger = LogManager.getLogger(TseitinStructure.class);
	
	private static Double MAX_DOUBLE =  TCity.MAX_DOUBLE;
	
	private TseitinVisitor tseitinVisitor;			
	private List<Integer> nodes;
	private List<List<Integer>> hardClauses;
	private Map<Integer,Double> weights;
	private Long executionTime;
	//private SecurityMetric securityMetric;
	private Integer x;
	private Formula formula;
	
	public TseitinStructure(TseitinVisitor tseitinVisitor, Integer x, Formula formula) {
		super();
		this.tseitinVisitor = tseitinVisitor;
		this.nodes = null;
		this.hardClauses = null;
		this.weights = new LinkedHashMap<Integer,Double>();
		this.executionTime = Long.valueOf(0);
		//this.securityMetric = null;
		this.x = x;
		this.formula = formula;
	}

	public void setup(AndOrGraph graph, Long time) throws Exception {
							
		logger.debug("Setting up Tseitin structure...");		
		this.tseitinVisitor.buildTseitinStructure(this);				
		logger.debug("Setting up Tseitin weights...");		
		
		this.addWeights(tseitinVisitor.getVarNameMap(), graph);
		this.executionTime = time;		
	}
	
	
	
	public Formula getFormula() {
		return formula;
	}

	public void setFormula(Formula formula) {
		this.formula = formula;
	}

	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}
	
	public TseitinVisitor getTseitinVisitor() {
		return tseitinVisitor;
	}

	public void setTseitinVisitor(TseitinVisitor tseitinVisitor) {
		this.tseitinVisitor = tseitinVisitor;
	}

	public Long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Long executionTime) {
		this.executionTime = executionTime;
	}

	public List<Integer> getNodes() {
		return nodes;
	}

	public void setNodes(List<Integer> nodes) {
		this.nodes = nodes;
	}

	public List<List<Integer>> getHardClauses() {
		return hardClauses;
	}

	public void setHardClauses(List<List<Integer>> hardClauses) {
		this.hardClauses = hardClauses;
	}
	
	public void addWeights(Map<Integer, Object> varNameMap, AndOrGraph graph) {		
		//Map<String, Measure> measureByInstanceId = problemSpec.getMeasureByInstanceId();
		
		for (Integer i : nodes) {
			Object nodeId = varNameMap.get(i);
			if (nodeId != null) {
							
				logger.debug("Analysing node: " + nodeId);		
				
				AndOrNode node = graph.getNode((String)nodeId);
				if (node != null) {
					if ("inf".equalsIgnoreCase(node.getValue())) {
						weights.put(i, MAX_DOUBLE);
					} else {
						weights.put(i, Double.valueOf(node.getValue()));
					}
				} 
			} else {
				//weights.put(i, MAX_DOUBLE);
				weights.put(i, 0.0);
			}
		}
	}
	
	public List<Integer> getTseitinLiterals() {
		
		Map<Integer, Object> varNameMap = this.tseitinVisitor.getVarNameMap();
		List<Integer> literals = new ArrayList<Integer>();
		
		for (Integer i : nodes) {
			Object nodeId = varNameMap.get(i);
			if (nodeId == null) {
				literals.add(i);				
			} 
		}
		
		return literals;
	}
	
	public Map<Integer, Double> getWeights() {
		return weights;
	}

	public void setWeights(Map<Integer, Double> weights) {
		this.weights = weights;
	}

	@Override
	public String toString() {
		return "TseitinStructure [nodes=" + nodes + ", hardClauses="
				+ hardClauses + "]";
	}
	
}
