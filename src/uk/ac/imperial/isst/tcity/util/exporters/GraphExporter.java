/**
 * 
 */
package uk.ac.imperial.isst.tcity.util.exporters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public abstract class GraphExporter {
	
	/**
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException * ***/
	public abstract String exportGraph (String graphDescriptorFilepath, String outputFilepath) throws FileNotFoundException, UnsupportedEncodingException, Exception;
	
	
	public String exportGraph (String graphDescriptorFilepath) throws FileNotFoundException, UnsupportedEncodingException, Exception {
		File graphDescriptor = new File(graphDescriptorFilepath);
    	
    	String extension = this.getFormatExtension(); 
		String name = graphDescriptor.getName().substring(0, graphDescriptor.getName().lastIndexOf("."));
		String outputFilepath = graphDescriptor.getParentFile().getPath() + File.separator + name + "." + extension;
		
		//System.out.println("outputFilepath: " + outputFilepath);
		return this.exportGraph(graphDescriptorFilepath,outputFilepath);
	}
	
	
	public abstract String getFormatExtension ();
}
