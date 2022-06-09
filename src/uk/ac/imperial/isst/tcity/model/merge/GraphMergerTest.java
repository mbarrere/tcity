package uk.ac.imperial.isst.tcity.model.merge;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.util.JSONReader;
import uk.ac.imperial.isst.tcity.view.AndOrGraphViewer;

public class GraphMergerTest {
	
    private static final Logger logger = LogManager.getLogger(GraphMergerTest.class);
 
    public static void main(final String... args) {
    	test1();
    }
    
    public static void test1 () {
 
    	Configurator.setRootLevel(Level.DEBUG);
        logger.debug("Entering application.");        
								
		try {
			String input1Filepath = "examples/merge/g1.json";
			String input2Filepath = "examples/merge/g3.json";
			
			AndOrGraph g1 = new JSONReader().loadAndOrGraph(input1Filepath);
			new AndOrGraphViewer(g1, "CPAG: " + input1Filepath).display();
			
			AndOrGraph g2 = new JSONReader().loadAndOrGraph(input2Filepath);
			new AndOrGraphViewer(g2, "CPAG: " + input2Filepath).display();
			
			//AndOrGraph g = new GraphMerger().mergeSimple(g1, g2);
			AndOrGraph g = new GraphMerger().merge(g1, g2);
						
			//String graphFilepath = new GraphImageExporter("PNG").exportGraph(g);
			//GraphDisplay.showImage(graphFilepath);
			AndOrGraphViewer viewer = new AndOrGraphViewer(g);
			viewer.setTitle("CPAG composition");
			viewer.display();					
						
		} catch (Exception e) {			
			e.printStackTrace();
		}
		       
    }
    
   
}
