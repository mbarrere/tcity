package uk.ac.imperial.isst.tcity.cpag.editor.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Map;

import javax.swing.ImageIcon;

import uk.ac.imperial.isst.tcity.cpag.editor.EditorConfig;
import uk.ac.imperial.isst.tcity.cpag.editor.Node;
import uk.ac.imperial.isst.tcity.cpag.editor.OperationsManager;


public class NodeDisplay implements Serializable {
	
	private static final long serialVersionUID = 5396347452110584370L;	
	protected NodeColorConfig colorConfig;
	
	private int paddingX = 10;
	private int paddingY = 10;
	
	public NodeDisplay() {
		this(new NodeColorConfig());
		//this(Color.BLACK, Color.WHITE, Color.BLACK);		
	}
	
	public NodeDisplay(NodeColorConfig colorConfig) {
		this.colorConfig = colorConfig;		
	}
	
	public NodeColorConfig getColorConfig() {
		return colorConfig;
	}

	public void setColorConfig(NodeColorConfig colorConfig) {
		this.colorConfig = colorConfig;
	}

	public Color getFillColor() {
		return Color.decode(this.colorConfig.getFillColor());
	}
	
	public Color getBorderColor() {
		return Color.decode(this.colorConfig.getLineColor());
	}
	
	public Color getTextColor() {
		return Color.decode(this.colorConfig.getTextColor());
	}
	
	
	
	
	public int getPaddingX() {
		return paddingX;
	}

	public void setPaddingX(int paddingX) {
		this.paddingX = paddingX;
	}

	public int getPaddingY() {
		return paddingY;
	}

	public void setPaddingY(int paddingY) {
		this.paddingY = paddingY;
	}

	public static NodeDimensions getNodeDimensions (Graphics g, String label) {
		
		FontMetrics fm = g.getFontMetrics();
		
		int width = fm.stringWidth(label);
		
		String regex = "\r\n|[\r\n]";
		String[] lines = label.split(regex);
		int height = fm.getHeight() * lines.length;
		
		if (lines.length > 1) {
			width = 0;
			//System.out.println("Multiline detected (" + lines.length + "): " + label);
			for (String line : lines) {
				int lineWidth = fm.stringWidth(line);
				if (lineWidth > width) {
					width = lineWidth;
				}	            
			}
		}
		
		return new NodeDimensions(width, height);
		
	}
	
	protected void drawMultilineString(Graphics g, String label, int tx, int ty) {
		
		String regex = "\r\n|[\r\n]";
		//String regex = "\\n";
		String[] lines = label.split(regex);
		
		g.setColor(this.getTextColor());
		if (lines.length == 1) {
			//System.out.println("Single line detected: " + label);
			g.drawString(label, tx, ty);
			/*
			int retIndex = label.indexOf("\\n");
			if (retIndex != -1) {
				boolean finished = false;
				int initPos = 0;
				int endPos = retIndex;
				while (!finished) {
					String line = label.substring(initPos, endPos);
					System.out.println("=> Subline: " + line);
					g.drawString(line, tx, ty);
					ty += g.getFontMetrics().getHeight();
					//System.out.println("=> \n  detected in line: " + label);
					initPos = endPos + 2;
					endPos = label.indexOf("\\n", initPos);
					if (endPos == -1) {
						finished = true;
						line = label.substring(initPos, label.length());
						System.out.println("=> Final Subline: " + line);
						g.drawString(line, tx, ty);
						ty += g.getFontMetrics().getHeight();
					}
				}
			} else {
				g.drawString(label, tx, ty);
			}
			*/
		} else {
			//System.out.println("Multiline detected (" + lines.length + "): " + label);
			for (String line : label.split(regex)) {
	            g.drawString(line, tx, ty);
	            ty += g.getFontMetrics().getHeight();
			}
		}
	}

	//public void draw(Node node, Graphics g, String label, Color fillColor, Color borderColor) {
	public void draw(Node node, Graphics g, String label) { //, Color fillColor, Color borderColor) {
				
		Font currentFont = g.getFont();
		if (EditorConfig.BASE_NODE_FONT_SHIFT != 0) {
			g.setFont(currentFont.deriveFont(new Float(currentFont.getSize() + EditorConfig.BASE_NODE_FONT_SHIFT).floatValue()));
		}
		
		//To change the font size of an object: 
		//g.setFont(g.getFont().deriveFont(new Float(40).floatValue()));
		
		FontMetrics fm = g.getFontMetrics();		
		
		NodeDimensions dim = NodeDisplay.getNodeDimensions(g, label);		
		//int width = fm.stringWidth(label) + xPadding;
		int width = dim.getWidth() + this.getPaddingX();
		//int height = fm.getHeight() + yPadding;
		int height = dim.getHeight() + this.getPaddingY();
		node.setR(height/2);
		
		g.setColor(this.getFillColor());		
		int rx = node.getX() - (width/2);
		int ry = node.getY() - (height/2);
		g.fillOval(rx, ry, width, height);
		
		g.setColor(this.getBorderColor());
		g.drawOval(rx, ry, width, height);
		//int tx = x - fm.stringWidth(text)/2;
		//int ty = y - fm.getHeight()/2 + fm.getAscent();
		int tx = rx + (this.getPaddingX()/2);			
		int ty = ry + fm.getHeight()/2 + this.getPaddingY();			
		//g.drawString(label, tx, ty);
		this.drawMultilineString(g, label, tx, ty); 
		
		if (OperationsManager.DISPLAY_BAYESIAN) {
			Map<String,Object> props = node.getProperties(); 
			
			if (props != null && !props.isEmpty()) {
				String bValue = (String)props.get(OperationsManager.BAYESIAN_KEY);
				if (bValue != null) {
					//String annexLabel = bValue;
					//String annexLabel = "(T=" + bValue + ")";
					//String annexLabel = "T=" + bValue;					
					String annexLabel = "R=" + bValue;
					/*
					String bValueFalse = null;
					try {
						Double d = Double.parseDouble(bValue);
						bValueFalse = new Double(1.0-d).toString();
					} catch(Exception e) {
						//pass
					}
					
					if (bValueFalse != null) {
						//annexLabel = "Compromise\nT=" + bValue + "|F=" + bValueFalse;
						annexLabel = "T=" + bValue + "|F=" + bValueFalse;
					}
					*/
					//new AnnexNode().drawAnnexNodePolygon(g, node.getX(), node.getY() + (2*node.getR()), bValue, 6); 
					new AnnexNode().drawAnnexNodeRectangle(g, node.getX(), node.getY() + (2*node.getR()), annexLabel);
				}
			}
		}
		
		if (EditorConfig.BASE_NODE_FONT_SHIFT != 0) {
			g.setFont(currentFont);
		}
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createImageIcon(String path,
	                                           String description) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}

	
	
}
