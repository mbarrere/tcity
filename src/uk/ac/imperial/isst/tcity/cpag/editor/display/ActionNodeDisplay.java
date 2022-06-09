package uk.ac.imperial.isst.tcity.cpag.editor.display;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import uk.ac.imperial.isst.tcity.cpag.editor.EditorConfig;
import uk.ac.imperial.isst.tcity.cpag.editor.Node;

public class ActionNodeDisplay extends NodeDisplay {
	
	private static final long serialVersionUID = 5396347452110584370L;	
	
	public ActionNodeDisplay(NodeColorConfig colorConfig) {		
		super(colorConfig);				
		//this.setPaddingY(this.getPaddingY()-28);
	}		

	public void draw(Node node, Graphics g, String label) {
		
		Font currentFont = g.getFont();
		if (EditorConfig.ACTION_NODE_FONT_SHIFT != 0) {
			g.setFont(currentFont.deriveFont(new Float(currentFont.getSize()+ EditorConfig.ACTION_NODE_FONT_SHIFT).floatValue()));
		}
		
		FontMetrics fm = g.getFontMetrics();
			
		NodeDimensions dim = NodeDisplay.getNodeDimensions(g, label);				
		int width = dim.getWidth() + this.getPaddingX();		
		int height = dim.getHeight() + this.getPaddingY() - 4;		
		node.setR(height/2);			
		
		g.setColor(this.getBorderColor());		
		int rx = node.getX() - (width/2);
		int ry = node.getY() - (height/2);			
		g.drawRect(rx, ry, width, height);
		
		g.setColor(this.getFillColor());
		g.fillRect(rx, ry, width, height);
					
		int tx = rx + (this.getPaddingX()/2);			
		int ty = ry + fm.getHeight()/2 + this.getPaddingY() - 4;
		this.drawMultilineString(g,label, tx, ty);	
		
		if (EditorConfig.ACTION_NODE_FONT_SHIFT != 0) {
			g.setFont(currentFont);
		}
		
	}
}
