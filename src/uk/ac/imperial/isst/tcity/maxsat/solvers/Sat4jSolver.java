/**
 * 
 */
package uk.ac.imperial.isst.tcity.maxsat.solvers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.TimeoutException;

import uk.ac.imperial.isst.tcity.TCity;
import uk.ac.imperial.isst.tcity.maxsat.MaxSatSolution;
import uk.ac.imperial.isst.tcity.maxsat.cnf.Formula;
import uk.ac.imperial.isst.tcity.maxsat.cnf.TseitinStructure;
import uk.ac.imperial.isst.tcity.maxsat.cnf.TseitinVisitor;
import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;


/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class Sat4jSolver {

	private static final Logger logger = LogManager.getLogger(Sat4jSolver.class);
	
	public static String SOLVER_ID = "MAX-SAT-SOLVER";			
	public static Double MAX_DOUBLE =  TCity.MAX_DOUBLE;
	private boolean lookForMinimumSets = false;		
	private int maxShift = 0;
	
	public Sat4jSolver() {		
	}
	
	
	public int getMaxShift() {
		return this.maxShift;
	}
	
	private void loadMapAndWeights(AndOrGraph graph, Map<String, Integer> map, Map<String, Integer> weights) {
		
		List<AndOrNode> nodes = graph.getNodes();						
		int index = 1;
		
		for (AndOrNode n : nodes) {
			if (n.isAtomicType()) {
				map.put(n.getId(), index);				
				index++;

				if (!"inf".equalsIgnoreCase(n.getValue())) {
					int integerPlaces = n.getValue().indexOf('.');
					int decimalPlaces = 0;
					if (integerPlaces != -1) {
						decimalPlaces = n.getValue().length() - integerPlaces - 1;
					}
					if (maxShift < decimalPlaces) {
						maxShift = decimalPlaces;
					}
				}
			}
		}
		
		for (AndOrNode n : nodes) {
			if (n.isAtomicType()) {
				Double value = 0.0;
				if ("inf".equalsIgnoreCase(n.getValue())) {
					value = MAX_DOUBLE;
				} else {
					value = Double.parseDouble(n.getValue());
					for (int i = 0; i < maxShift; i++) {
						value = value * 10;
					}
					//value += this.nodeIncrement; //for nodes with weight 0 and problems with measures
				}				
				weights.put(n.getId(), value.intValue());				
			}
		}		
	}
	
	private void addClauses(WeightedMaxSatDecorator maxSATSolver, Formula formula, Set<Set<Integer>> clauses, Integer x, Map<Integer, Object> varNameMap, Map<String, Integer> map, Map<String, Integer> weights) throws ContradictionException {
		
		maxSATSolver.addHardClause(new VecInt(new int[]{x}));
		
		for (Set<Integer> c : clauses) {
			int[] carr = new int[c.size()];
	        int i = 0;
	        for (Integer y : c) {
	        	carr[i] = y;
	        	i++;
	        }
	        
	        if (logger.isTraceEnabled()) {
		        System.out.print("=> Hard clause: ");
		        for (int k = 0; k < carr.length; k++) { 
		        	System.out.print(carr[k] + ", "); 
		        }
		        System.out.println();
	        }
	        
	        maxSATSolver.addHardClause(new VecInt(carr));
		}
		
		if (logger.isDebugEnabled()) {
			System.out.println("Adding clauses soft...");
		}
		List<Integer> softs = new ArrayList<Integer>();
		for (Entry<Integer, Object> e : varNameMap.entrySet()) {
			String nodeId = e.getValue().toString();
			Integer index = e.getKey();
			if (logger.isDebugEnabled()) {
				System.out.println("- Node id: " + nodeId + ", index: " + index + ", weight: " + weights.get(nodeId));
			}
			softs.add(index);
			maxSATSolver.addSoftClause(weights.get(nodeId), new VecInt(new int[] { index }));
		}
	}
	
	
	public MaxSatSolution solve(AndOrGraph graph, Map<String,Object> stats, TseitinStructure ts) throws InterruptedException {
		
		if (logger.isDebugEnabled()) {
			System.out.println("[*] MAX-SAT solver started! " + new Timestamp(System.currentTimeMillis()));
		}
		MaxSatSolution m = null;
		try {			
			List<AndOrNode> nodes = graph.getNodes();
			if (logger.isDebugEnabled()) {
				for (AndOrNode n : nodes) {
					System.out.println(n);
				}
			}
						
			Formula formula = ts.getFormula();						
			
			TseitinVisitor tseitinVisitor = ts.getTseitinVisitor();		
			Integer x = ts.getX();
			Set<Set<Integer>> clauses = tseitinVisitor.getClauses();
			Map<Integer, Object> varNameMap = tseitinVisitor.getVarNameMap();			
			int varCount = x;
			
			stats.put("tseitin.#variables", x);
			stats.put("tseitin.#clauses", (clauses.size()+1));
			
			if (logger.isDebugEnabled()) {							
				//System.out.println("Tseitin transformation time: " + tseitinTime + " ms (" + (tseitinTime/1000) + " seconds).");
				System.out.println("Tseitin CNF sentence (DIMACS format): ");
				System.out.println("- Number of variables: " + x);				
				System.out.println("- Number of clauses: " + (clauses.size()+1));				
				if (logger.isTraceEnabled()) {
					tseitinVisitor.writeResultDIMACS(System.out, x);			
					System.out.println("VarName MAP (DIMACS): ");	
					for (Entry<Integer, Object> e : varNameMap.entrySet()) {
						System.out.println("Integer: " + e.getKey() + "; object: " + e.getValue());
					}
					
					System.out.println("IDS MAP (DIMACS): ");	
					for (Entry<Object, Integer> e : tseitinVisitor.getIdsMap().entrySet()) {
						System.out.println("Object: " + e.getKey() + "; integer: " + e.getValue());
					}
				}				
			}									

			/*** FORMULATE PROBLEM ***/
			
			//ISolver solver = SolverFactory.newDefault();
			WeightedMaxSatDecorator maxSATSolver = new WeightedMaxSatDecorator(
					SolverFactory.newLight());
			
			Map<String, Integer> map = new LinkedHashMap<String, Integer>();
			//Map<Integer, String> reverseMap = new LinkedHashMap<Integer, String>();
			Map<String, Integer> weights = new LinkedHashMap<String, Integer>();
	
			if (logger.isDebugEnabled()) {
				System.out.println("loadMapsAndValuesWithMeasures...");
			}
						
			this.loadMapAndWeights(graph, map, weights);
						
						
			maxSATSolver.newVar(varCount);	
			maxSATSolver.setExpectedNumberOfClauses(clauses.size());
			
			this.addClauses(maxSATSolver, formula, clauses, x, varNameMap, map, weights);
			
			
			/***  SOLVE PROBLEM  *////
			
			//IProblem problem = new OptToSatAdapter(new PseudoOptDecorator(maxSATSolver));		
	        IOptimizationProblem problem = new PseudoOptDecorator(maxSATSolver);
			        
			boolean isSatisfiable = problem.isSatisfiable();
	
			if (!isSatisfiable) {
				throw new Exception("The specified problem is not satisfiable");
			}				
			
			long start = System.currentTimeMillis();				
			m = this.buildSolution(graph, problem, varNameMap, weights);
			long firstRoundTime = System.currentTimeMillis() - start;	
			stats.put("first.time.ms", firstRoundTime);
			stats.put("first.time.sec", (firstRoundTime/1000));
			
			if (logger.isDebugEnabled()) {
				System.out.println("\n==================================");
				System.out.println("### First solution found: ");
				System.out.println("Computation time: " + firstRoundTime + " ms (" + (firstRoundTime/1000) + " seconds).");
				m.display();
				
			}
			
			if (logger.isTraceEnabled()) {
				System.out.println("\n==================================");
				System.out.println("### Looking for a better solution");
			}
			start = System.currentTimeMillis();
			m = findBetterSolution(problem, new VecInt(new int[] {}), graph, map, varNameMap, weights);
			long secondRoundTime = System.currentTimeMillis() - start;
			stats.put("second.time.ms", secondRoundTime);
			stats.put("second.time.sec", (secondRoundTime/1000));
			if (logger.isTraceEnabled()) { 
				System.out.println("Computation time: " + secondRoundTime + " ms (" + (secondRoundTime/1000) + " seconds).");
				m.display();
			}
			
	        if (m.getNodes().size() == 1) {
	        	stats.put("third.used", 0);
	        	//pass	        	 
	        } else {
	        	if (this.lookForMinimumSets) {
	        		stats.put("third.used", 1);
	        		start = System.currentTimeMillis();
	        		
	        		if (logger.isDebugEnabled()) {
		        		System.out.println("\n==================================");
		        		System.out.println("### Looking for solution with minimum amount of nodes");
	        		}
		        	AndOrNode lowest = getNodeWithLowestValue(m); 
		        	VecInt assumps = new VecInt(new int[] {map.get(lowest.getId())});
		        	if (logger.isDebugEnabled()) {
		        		System.out.println("=> Node with lowest value: " + lowest.getId());
		        	}
		        	
		        	
		        	/*** TEST ***/
		        	// Test: rebuild the problem assigning INFINITE to one of the nodes in the current solution
		        	// This will force to find a different one
		        	
		        	WeightedMaxSatDecorator maxSATSolver2 = new WeightedMaxSatDecorator(
							SolverFactory.newLight());
									
					maxSATSolver2.newVar(varCount);	
					maxSATSolver2.setExpectedNumberOfClauses(clauses.size());					
					
					Integer preValue = weights.get(lowest.getId());
					//System.out.println("=> Lowest node value: " + weights.get(lowest.getId()));
					Double d = MAX_DOUBLE;
					weights.put(lowest.getId(), d.intValue());
					//System.out.println("=> Lowest node value after: " + weights.get(lowest.getId()));
					this.addClauses(maxSATSolver2, formula, clauses, x, varNameMap, map, weights);
										
					IOptimizationProblem problem2 = new PseudoOptDecorator(maxSATSolver2);
					isSatisfiable = problem2.isSatisfiable();
					
					if (!isSatisfiable) {
						System.out.println("=> Not satisfiable, returning same solution");
						return m;						
					}
					MaxSatSolution m2 = findBetterSolution(problem2, assumps, graph, map, varNameMap, weights);
					weights.put(lowest.getId(), preValue);
					
					//System.out.println("=> Lowest node value final: " + weights.get(lowest.getId()));
					
					long finalRoundTime = System.currentTimeMillis() - start;
					stats.put("third.time.ms", finalRoundTime);
					stats.put("third.time.sec", (finalRoundTime/1000));
					
					/**** test end ****/
											            
					if (logger.isDebugEnabled()) {
		            	System.out.println("Computation time: " + finalRoundTime + " ms (" + (finalRoundTime/1000) + " seconds).");
		            	m2.display();
		            }
		            
		            if (m2.getCost().doubleValue() < m.getCost().doubleValue() || 
		            		(m2.getCost().doubleValue() == m.getCost().doubleValue() && m2.getNodes().size() < m.getNodes().size()) ) {
		            	m = m2;
		            	stats.put("third.success", 1);
		            } else {
		            	stats.put("third.success", 0);
		            }
		            
	        	} else {
	        		//pass
	        	}
	            
	        }	        
	        
	        m.setSolverId(SOLVER_ID);
		} catch (Exception e) {
			throw new InterruptedException(e.getMessage());
		}

        return m;
	}
	
	private MaxSatSolution buildSolution (AndOrGraph graph, IOptimizationProblem problem, Map<Integer, Object> varNameMap, Map<String, Integer> weights) throws TimeoutException {
		MaxSatSolution solution = new MaxSatSolution(graph);
		Double maxDouble = MAX_DOUBLE;
		Double cost = 0.0;		
		
		for (int i = 1; i <= problem.model().length; i++) {
			logger.trace("Building solution: " + i + "->" + problem.model(i));
			Object var = varNameMap.get(i);
			if (var != null) {
				String id = var.toString();

				if (!problem.model(i)) {
					//if (weights.get(id).intValue() == MAX_VALUE) {
					if (weights.get(id).intValue() == maxDouble.intValue()) {
						cost = MAX_DOUBLE;
					} else {						
						cost += weights.get(id);						
					}
					
					AndOrNode node = graph.getNode(id);
					if (node != null) {						
						solution.getNodes().add(graph.getNode(id));
					} 				
				}
			}
			
		}
				
		if (!MAX_DOUBLE.equals(cost)) { 
			//cost = cost - totalNodeInc;
			for (int i = 0; i < this.getMaxShift(); i++) {
				cost = cost / 10.0;				
			}				
		}		
		solution.setCost(cost);				
		return solution;
	}
	
	private MaxSatSolution findBetterSolution (IOptimizationProblem problem, VecInt assumps, AndOrGraph graph, Map<String, Integer> map, Map<Integer, Object> varNameMap, Map<String, Integer> weights) throws TimeoutException {		        
        boolean isSatisfiable = false;
        final int OPTIMUM_FOUND = 0;
	    final int UNSATISFIABLE = 1;	    
	            
        int exitCode = UNSATISFIABLE;
        
        int[] model = problem.model();
        
        try {            
            while (problem.admitABetterSolution(assumps)) {            	
                model = problem.model(); 
                if (logger.isTraceEnabled()) {
                	System.out.println("=> Problem admits better solution");
	                for (int i = 0; i < model.length; i++) {
	                    System.out.print("* " + model[i] + " ");
	                }
	                System.out.println();
                }
                isSatisfiable = true;
                problem.discardCurrentSolution();
            }
            if (isSatisfiable) {
                exitCode = OPTIMUM_FOUND;
            } else {
                exitCode = UNSATISFIABLE;
            }
        } catch (ContradictionException ex) {
            //assert isSatisfiable;
            exitCode = OPTIMUM_FOUND;
        }
        
        if (exitCode == OPTIMUM_FOUND) {
        	return this.buildSolution(graph, problem, varNameMap, weights);
        	//throw new Exception("[ERROR]");
        } else {
        	if (logger.isDebugEnabled()) {
        		System.out.println("=> No optimum found");
        	}
        	MaxSatSolution m = new MaxSatSolution(graph);
        	m.setCost(MAX_DOUBLE);
        	return m;
        }    	    	
	}
	
	private AndOrNode getNodeWithLowestValue(MaxSatSolution m) {
		AndOrNode minNode = null;
		Double cost = MAX_DOUBLE;
		List<AndOrNode> nodes = m.getNodes();
		
		for (int i = 0; i < nodes.size(); i++) {
			AndOrNode n = nodes.get(i);
			Double value = 0.0;
			if ("inf".equalsIgnoreCase(n.getValue())) {
				value = MAX_DOUBLE;
			} else {
				value = Double.parseDouble(n.getValue());				
			}
			
			if (value < cost) {
				cost = value;
				minNode = n;
			}		
		}
		
		return minNode;
	}

}
