package uk.ac.imperial.isst.tcity.cpag.editor.display;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import uk.ac.imperial.isst.tcity.cpag.editor.EditorConfig;
import uk.ac.imperial.isst.tcity.cpag.editor.Node;
import uk.ac.imperial.isst.tcity.lab.editor.tests.shapes.RegularPolygon;

public class SplitterNodeDisplay extends NodeDisplay {
	
	private static final long serialVersionUID = 5396347452110584370L;	
	
	public SplitterNodeDisplay(NodeColorConfig colorConfig) {		
		super(colorConfig);				
		//this.borderColor = Color.decode("#b87aff");
		//this.fillColor = Color.decode("#e8cfff");		
	}
	
	public void drawRect(Node node, Graphics g, String label) { 			
		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth(label) + this.getPaddingX();
		int height = fm.getHeight() + this.getPaddingY();
		node.setR(height/2);		
		
		g.setColor(this.getBorderColor());
		
		int rx = node.getX() - (width/2);
		int ry = node.getY() - (height/2);			
		g.drawRect(rx, ry, width, height);
		
		g.setColor(this.getFillColor());
		g.fillRect(rx, ry, width, height);
		
		g.setColor(this.getTextColor());			
		int tx = rx + (this.getPaddingX()/2);			
		int ty = ry + fm.getHeight()/2 + this.getPaddingY();
		g.drawString(label, tx, ty);		
		
	}

	public void draw(Node node, Graphics g, String label) { 
		
		int points = 8; // hexagon
		
		Font currentFont = g.getFont();
		if (EditorConfig.LOGICAL_NODE_FONT_SHIFT != 0) {
			g.setFont(currentFont.deriveFont(new Float(currentFont.getSize() + EditorConfig.LOGICAL_NODE_FONT_SHIFT).floatValue()));
		}
		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth(label) + this.getPaddingX();
		int height = fm.getHeight() + this.getPaddingY();
		node.setR((Math.max(width, height))/2);		
		
		RegularPolygon poly = new RegularPolygon(node.getX(), node.getY(), node.getR(), points, 0);
		 		
		g.setColor(this.getBorderColor());
	    ((Graphics2D)g).draw(poly);
	    
	    g.setColor(this.getFillColor());
	    g.fillPolygon(poly.xpoints, poly.ypoints, poly.xpoints.length);
		
		int rx = node.getX() - (width/2);
		int ry = node.getY() - (height/2);			
		
		g.setColor(this.getTextColor());			
		int tx = rx + (this.getPaddingX()/2);			
		int ty = ry + fm.getHeight()/2 + this.getPaddingY();
		g.drawString(label, tx, ty);		
		
		if (EditorConfig.LOGICAL_NODE_FONT_SHIFT != 0) {
			g.setFont(currentFont);
		}
	}
	
}
