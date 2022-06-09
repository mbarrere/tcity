/**
 * 
 */
package uk.ac.imperial.isst.tcity.util.exporters;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class GraphDisplay {

	private static final Logger logger = LogManager.getLogger(GraphDisplay.class);
	
	
	public static boolean isDisplayAvailable() {
		return Desktop.isDesktopSupported();
	}
	
	public static void showImage(String filepath) {
		if (Desktop.isDesktopSupported()) {
		    try {		    									
		        File myFile = new File(filepath);
		        Desktop.getDesktop().open(myFile);
		        		       
		    } catch (IOException ex) {
		    	logger.error("Visualization error: display is not supported for this type of file. ");
		    	//ex.printStackTrace();
		        // no application registered for PDFs/PNGs...
		    } catch (IllegalArgumentException ex) {
		    	logger.error("Visualization error: " + ex.getMessage());
		    	//ex.printStackTrace();
		        // no application registered for PDFs/PNGs...
		    }
		}else {
			logger.error("Visualization issue: desktop viewer not supported.");
		}
	}
}
