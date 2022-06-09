package uk.ac.imperial.isst.tcity.cpag.editor.display;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import uk.ac.imperial.isst.tcity.cpag.editor.ColorUtils;
import uk.ac.imperial.isst.tcity.cpag.editor.EditorConfig;
import uk.ac.imperial.isst.tcity.lab.editor.tests.shapes.RegularPolygon;

public class AnnexNode extends NodeDisplay {
		
	private static final long serialVersionUID = -1936944540113043677L;

	public AnnexNode() {
		super(new NodeColorConfig("#fca103", "#f7be54", ColorUtils.HEX_BLACK));
		//super(new NodeColorConfig("#fca103", "#fcbd65", ColorUtils.HEX_BLACK));
		//orange
		//super(new NodeColorConfig("#fca103", "#f7be54", ColorUtils.HEX_BLACK));
		//super(new NodeColorConfig("#fa7d43", ColorUtils.HEX_ORANGE, ColorUtils.HEX_BLACK));
		//super(new NodeColorConfig("#fca103", "#f7be54", ColorUtils.HEX_BLACK));
		
		//super(new NodeColorConfig(ColorUtils.HEX_BLACK, ColorUtils.HEX_ORANGE, ColorUtils.HEX_BLACK));

		//yellow
		//super(new NodeColorConfig("#ffe959", "#fcf2b1", ColorUtils.HEX_BLACK));
		//#ffe959 //strong yellow
		//#fce447 //vert strong
		//#fced8b //light yellow
		//#fcf2b1 //very light
	}

	public void drawAnnexNodePolygon (Graphics g, int x, int y, String label, int polygonPoints) {
		
		Font currentFont = g.getFont();
		if (EditorConfig.ANNEX_NODE_FONT_SHIFT != 0) {			
			g.setFont(currentFont.deriveFont(new Float(currentFont.getSize() + EditorConfig.ANNEX_NODE_FONT_SHIFT).floatValue()));
		}
		
		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth(label) + this.getPaddingX();
		int height = fm.getHeight() + this.getPaddingY();
		
		int annexNodeRadius = (Math.max(width, height))/2;		
		
		RegularPolygon poly = new RegularPolygon(x, y, annexNodeRadius, polygonPoints, 0);
			
		g.setColor(this.getBorderColor());
	    ((Graphics2D)g).draw(poly);
	    
	    g.setColor(this.getFillColor());
	    g.fillPolygon(poly.xpoints, poly.ypoints, poly.xpoints.length);
		
		int rx = x - (width/2);
		int ry = y - (height/2);			
		
		g.setColor(this.getTextColor());			
		int tx = rx + (this.getPaddingX()/2);			
		int ty = ry + fm.getHeight()/2 + this.getPaddingY();
		g.drawString(label, tx, ty);
		
		if (EditorConfig.ANNEX_NODE_FONT_SHIFT != 0) {
			g.setFont(currentFont);
		}
	}
	
	public void drawAnnexNodeRectangle (Graphics g, int x, int y, String label) {
		
		Font currentFont = g.getFont();
		if (EditorConfig.ANNEX_NODE_FONT_SHIFT != 0) {			
			g.setFont(currentFont.deriveFont(new Float(currentFont.getSize() + EditorConfig.ANNEX_NODE_FONT_SHIFT).floatValue()));
		}
		
		
		FontMetrics fm = g.getFontMetrics();
		
		NodeDimensions dim = NodeDisplay.getNodeDimensions(g, label);				
		int width = dim.getWidth() + this.getPaddingX();		
		//int height = dim.getHeight() + this.getPaddingY();		
		int height = dim.getHeight();
		//node.setR(height/2);			
		
		g.setColor(this.getBorderColor());		
		int rx = x - (width/2);
		int ry = y - (height/2);			
		g.drawRect(rx, ry, width, height);
		
		g.setColor(this.getFillColor());
		g.fillRect(rx, ry, width, height);
					
		int tx = rx + (this.getPaddingX()/2);			
		//int ty = ry + fm.getHeight()/2 + (this.getPaddingY()/2);
		//int ty = y + (fm.getHeight()/2) - (this.getPaddingY()/2);
		//int ty = ry + height/2 + (fm.getHeight()/4);
		int ty = y + (fm.getHeight()/4);
		this.drawMultilineString(g,label, tx, ty);	
				
		
		if (EditorConfig.ANNEX_NODE_FONT_SHIFT != 0) {
			g.setFont(currentFont);
		}
	}
}
