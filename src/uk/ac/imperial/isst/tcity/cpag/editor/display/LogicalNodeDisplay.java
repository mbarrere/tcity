package uk.ac.imperial.isst.tcity.cpag.editor.display;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import uk.ac.imperial.isst.tcity.cpag.editor.EditorConfig;
import uk.ac.imperial.isst.tcity.cpag.editor.Node;
import uk.ac.imperial.isst.tcity.lab.editor.tests.shapes.RegularPolygon;

public class LogicalNodeDisplay extends NodeDisplay {
	
	private static final long serialVersionUID = 5396347452110584370L;	
	
	public LogicalNodeDisplay(NodeColorConfig colorConfig) {		
		super(colorConfig);		
		//this.borderColor = Color.decode("#b87aff");
		//this.fillColor = Color.decode("#e8cfff");		
		//this.setPaddingX(this.getPaddingX()*2);
		//this.setPaddingY(this.getPaddingY()*2);

	}

	public void drawRect(Node node, Graphics g, String label) { 
		if (label == null || label.trim().isEmpty()) {
			if (node.getType() == null || node.getType().trim().isEmpty()) {
				label = "??";
			} else {
				label = node.getType().toUpperCase();
			}
		} 
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
	
	public void drawDiamond(Node node, Graphics g, String label) { 
		if (label == null || label.trim().isEmpty()) {
			if (node.getType() == null || node.getType().trim().isEmpty()) {
				label = "??";
			} else {
				label = node.getType().toUpperCase();
			}
		} 
				
		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth(label) + this.getPaddingX();
		int height = fm.getHeight() + this.getPaddingY();
		node.setR(height/2);		
		
		Diamond diamond	= new Diamond(width, height);
		
		//super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        //int x = (getWidth() - diamond.getBounds().width) / 2;
        //int y = (getHeight()- diamond.getBounds().height) / 2;
        
        int rx = node.getX() - (width/2);
		int ry = node.getY() - (height/2);			
		//g.drawRect(rx, ry, width, height);
		
        AffineTransform at = AffineTransform.getTranslateInstance(rx, ry);
        Shape shape = at.createTransformedShape(diamond);
        //g2d.setColor(Color.YELLOW);
        g2d.setColor(this.getFillColor());
        g2d.fill(shape);
        //g2d.setColor(Color.RED);
        g2d.setColor(this.getBorderColor());
        g2d.draw(shape);
        g2d.dispose();
		
        
		g.setColor(this.getTextColor());			
		int tx = rx + (this.getPaddingX()/2);			
		int ty = ry + fm.getHeight()/2 + this.getPaddingY();
		g.drawString(label, tx, ty);		
		
	}
	
	public void draw(Node node, Graphics g, String label) { 
		
		Font currentFont = g.getFont();
			
		if (EditorConfig.LOGICAL_NODE_FONT_SHIFT != 0) {			
			g.setFont(currentFont.deriveFont(new Float(currentFont.getSize() + EditorConfig.LOGICAL_NODE_FONT_SHIFT).floatValue()));
		}		
		
		/*
		if (label == null || label.trim().isEmpty()) {
			label = node.getType().toUpperCase();
		}
		*/
		label = node.getType().toUpperCase();
		
		int points = 4; // rhomboid
		
		FontMetrics fm = g.getFontMetrics();
		//int width = fm.stringWidth(label) + this.getPaddingX();
		int textWidth = Math.max(fm.stringWidth("AND"), fm.stringWidth(label));
		int width = textWidth + this.getPaddingX();
		int height = fm.getHeight() + this.getPaddingY();
		node.setR((Math.max(width, height))/2);		
		//node.setR((height+10)/2);
		
		//RegularPolygon poly = new RegularPolygon(node.getX(), node.getY(), node.getR(), points, Math.PI / 4); //rectangle with 4 points
		RegularPolygon poly = new RegularPolygon(node.getX(), node.getY(), node.getR(), points, 0);
		 		
		g.setColor(this.getBorderColor());
	    ((Graphics2D)g).draw(poly);
	    
	    g.setColor(this.getFillColor());
	    g.fillPolygon(poly.xpoints, poly.ypoints, points);
		
		int rx = node.getX() - (fm.stringWidth(label)/2);
		int ry = node.getY() + (fm.getHeight()/4); //(fm.getHeight()/2);			
		
		g.setColor(this.getTextColor());					
		g.drawString(label, rx, ry);
		
		if (EditorConfig.LOGICAL_NODE_FONT_SHIFT != 0) {
			g.setFont(currentFont);
		}
		
	}
	
	 public class Diamond extends Path2D.Double {

		private static final long serialVersionUID = 1L;

			public Diamond(double width, double height) {
	            moveTo(0, height / 2);
	            lineTo(width / 2, 0);
	            lineTo(width, height / 2);
	            lineTo(width / 2, height);
	            closePath();
	        }

	    }
}
