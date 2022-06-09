package uk.ac.imperial.isst.tcity.util;

/**
 *     Java Program to Implement Tarjan Algorithm
 **/
 
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
 
/** class Tarjan **/
public class Tarjan
{
    /** number of vertices **/
    private int V;    
    /** preorder number counter **/
    private int preCount;
    /** low number of v **/
    private int[] low;
    /** to check if v is visited **/
    private boolean[] visited;      
    /** to store given graph **/
    //private List<Integer>[] graph;
    private AndOrGraph graph;
    /** to store all scc **/
    private List<List<Integer>> sccComp;
    private Stack<Integer> stack;
 
    final static Logger logger = LogManager.getLogger(Tarjan.class);
    
    public boolean hasLoops(AndOrGraph graph) throws Exception { 
		
		List<List<Integer>> scc = this.getSCComponents(graph);
        logger.debug("Strongly connected components at NaggenGraph: " + scc);      
	            
        //if (scc.size() != nodes.size()) return true;		
        //previous method is faster... leaving this for testing
        for (List<Integer> loop : scc) {
			if (loop.size() > 1) {
				logger.debug("Loop component detected: " + loop);
				return true;
			}			
		}
		
		//check self-loops
        //pending... required???
		
        return false;
			
	}

    /** function to get all strongly connected components **/
    //public List<List<Integer>> getSCComponents(List<Integer>[] graph) 
    public List<List<Integer>> getSCComponents(AndOrGraph graph)
    {
        V = graph.getNodes().size();
        this.graph = graph;
        low = new int[V];
        visited = new boolean[V];
        stack = new Stack<Integer>();
        sccComp = new ArrayList<List<Integer>>();
 
        for (int v = 0; v < V; v++) {
              if (!visited[v]) {
                //dfs(v);
            	  dfs(this.getNodeByIndex(this.graph, v));
              }
        }
        return sccComp;
    }
    
    /** function dfs **/
    //public void dfs(int v) 
    public void dfs(AndOrNode n)
    {
    	int v = this.getCurrentIndexForNode(this.graph, n);
        low[v] = preCount++;
        visited[v] = true;
        stack.push(v);
        int min = low[v];
        //for (int w : graph[v]) // for each node w reachable from v
        //System.out.println("Visiting node: " + n.getId());
        List<AndOrNode> outgoingNodes = this.graph.getOutgoingNodes(n.getId()); 
        if (outgoingNodes != null) {
	        for (AndOrNode m : outgoingNodes) // for each node w reachable from v
	        {	        	
	        	int w = this.getCurrentIndexForNode(this.graph, m);
	            if (!visited[w])
	                dfs(m);
	            if (low[w] < min) 
	                min = low[w];
	        }
        }
        if (min < low[v]) 
        { 
            low[v] = min; 
            return; 
        }        
        List<Integer> component = new ArrayList<Integer>();
        int w;
        do
        {
            w = stack.pop();
            component.add(w);
            low[w] = V;                
        } while (w != v);
        sccComp.add(component);        
    }    
    
    
	public int getCurrentIndexForNode (AndOrGraph graph, AndOrNode n) {
		return graph.getNodes().indexOf(n);
	}
	
	public AndOrNode getNodeByIndex (AndOrGraph graph, int index) {
		return graph.getNodes().get(index);
	}
}
