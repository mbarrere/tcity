/**
 * 
 */
package uk.ac.imperial.isst.tcity.util.exporters;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class GraphExporterFactory {

	public static final String JSON_FORMAT = "json";
	public static final String PDF_FORMAT = "pdf";
	public static final String PNG_FORMAT = "png";
	public static final String DOT_FORMAT = "dot";		
	
	public static GraphExporter getExporter(String type) throws UnsupportedExportFormatException {
		
		/*
		if (JSON_FORMAT.equalsIgnoreCase(type)) {
			return new JSONGraphExporter();
		}
		*/
		
		if (PDF_FORMAT.equalsIgnoreCase(type)) {
			return new GraphImageExporter(PDF_FORMAT);
		}
		
		if (PNG_FORMAT.equalsIgnoreCase(type)) {
			return new GraphImageExporter(PNG_FORMAT);
		}
		
		if (DOT_FORMAT.equalsIgnoreCase(type)) {
			return new DOTGraphExporter();
		}
		
		throw new UnsupportedExportFormatException("Export format type (" + type + ") is not currently supported.");
		
	}
}
