package uk.ac.imperial.isst.tcity.cpag.editor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeDisplayFactory;
import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeType;

/*
enum EdgeType {
	BASIC_EDGE("Basic edge"),
	CPAG_BASE_EDGE("CPAG base edge");
	
	String edgeType;
	
	EdgeType(String edgeType) {
		this.edgeType = edgeType;
	}
	
	@Override
	public String toString() {
		return edgeType;
	}
}
*/

//@JsonTypeName("edge")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Edge implements Serializable {

	private static final long serialVersionUID = -2550804644989255961L;
	
	public static final Integer[] STROKE_VALUES = {1, 2, 3, 4, 6, 7, 8, 9, 10};
	
	//@JsonProperty("source")
	@JsonIgnore
	protected Node nodeA;
	//@JsonProperty("target")
	@JsonIgnore
	protected Node nodeB;
	//@JsonIgnore
	//protected EdgeType edgeType = EdgeType.BASIC_EDGE;		
	@JsonIgnore
	protected int stroke;
	@JsonIgnore
	protected Color color;
	
	protected String value;
	protected String source;
	protected String target;
	protected String label;
		
	protected Map<String,Object> properties;
		
	
	public Edge() {
		this.color = ColorUtils.fromHexColor(ColorUtils.HEX_DARK_GRAY);
		this.stroke = 1;
		this.properties = new LinkedHashMap<String,Object>();
	}
	
	public Edge(Node a, Node b) {
		this(a, b, Color.BLACK, 1, "1.0","");
	}	
	
	public Edge(Node a, Node b, Color color, int stroke, String value, String label) {
		this.nodeA = a;
		this.nodeB = b;		
		this.source = String.valueOf(a.getId());
		this.target = String.valueOf(b.getId());
		this.color = color;
		this.stroke = stroke;
		//this.edgeType = EdgeType.CPAG_BASE_EDGE;
		if (value == null) {
			this.value = "1.0";
		} else {
			this.value = value;
		}
		this.label = label;
		this.properties = new LinkedHashMap<String,Object>();
	}
	
	public Edge(Node a, Node b, Color color, int stroke, String value) {
		this(a, b, color, stroke, value, "");
	}
	
	public Node getNodeA() {
		return nodeA;
	}

	public void setNodeA(Node nodeA) {
		this.nodeA = nodeA;
	}

	public Node getNodeB() {
		return nodeB;
	}

	public void setNodeB(Node nodeB) {
		this.nodeB = nodeB;
	}
		
	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public int getStroke() {
		return stroke;
	}

	public void setStroke(int stroke) {
		if(stroke < STROKE_VALUES[0])
			this.stroke = STROKE_VALUES[0];
		else if(stroke > STROKE_VALUES[STROKE_VALUES.length-1])
			this.stroke =  STROKE_VALUES[STROKE_VALUES.length-1];
		else
			this.stroke = stroke;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		if(color == null)
			this.color = Color.BLACK;
		else
			this.color = color;
	}

	public void changeStroke(int step) {
		setStroke(stroke+step);		
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	
	protected void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
	    int dx = x2 - x1, dy = y2 - y1;
	    double D = Math.sqrt(dx*dx + dy*dy);
	    double xm = D - d, xn = xm, ym = h, yn = -h, x;
	    double sin = dy / D, cos = dx / D;

	    x = xm*cos - ym*sin + x1;
	    ym = xm*sin + ym*cos + y1;
	    xm = x;

	    x = xn*cos - yn*sin + x1;
	    yn = xn*sin + yn*cos + y1;
	    xn = x;

	    int middleX = (x1+x2)/2;
	    int middleY = (y1+y2)/2;
	    
	    //int shiftX = middleX/2;
	    int shiftX = 0;
	    //int shiftY = middleY/2;
	    int shiftY = 0;
	    //int[] oldxpoints = {x2, (int) xm, (int) xn};
	    int[] xpoints = {middleX, (int) xm - (dx/2) + shiftX, (int) xn - (dx/2) + shiftX};
	    //int[] oldypoints = {y2, (int) ym, (int) yn};
	    int[] ypoints = {middleY, (int) ym - (dy/2) + shiftY, (int) yn - (dy/2) + shiftY};

	    //System.out.println("- Drawing: xpoints: " + xpoints[0] + "," + xpoints[1] + "," + xpoints[2] + ",");
	    //System.out.println("- Drawing: ypoints: " + ypoints[0] + "," + ypoints[1] + "," + ypoints[2] + ",");	    
	    g.drawLine(x1, y1, x2, y2);
	    g.fillPolygon(xpoints, ypoints, 3);
	    //g.fillPolygon(middleXpoints, middleYpoints, 3);
	}
	
	//@Override
	public void draw(Graphics g) {
		int xa = nodeA.getX();
		int ya = nodeA.getY();
		int xb = nodeB.getX();
		int yb = nodeB.getY();
		
		Graphics2D g2 = (Graphics2D) g;
		// set stroke just for this line
		g2.setStroke(new BasicStroke(stroke));
		g2.setColor(color);
		
		
		String hValue = null;
		
		if (OperationsManager.DISPLAY_HARDENING) {
			if (this.getProperties() != null && !this.getProperties().isEmpty()) {
				hValue = (String)this.getProperties().get(OperationsManager.HARDENING_KEY);
				if (hValue != null) {
					//System.out.println("-> found hValue: " + hValue);					
					g2.setStroke(new BasicStroke(3));
					g2.setColor(Color.RED);							
				}
			}
		}
		
		// Martin's change (arrow tip added)
		//g2.drawLine(xa, ya, xb, yb);
		//System.out.println("Drawing arrow...");
		this.drawArrowLine(g2, xa, ya, xb, yb, 8, 8);
		// reset to default stroke value
		g2.setStroke(new BasicStroke());
		
		int xPadding = 8;
		int yPadding = -5;
		int tx = (xa+xb)/2 + xPadding;
		int ty = (ya+yb)/2 + yPadding;
		
		if (hValue != null) {
			Font currentFont = g.getFont();
			if (EditorConfig.EDGE_FONT_SHIFT != 0) {
				g.setFont(currentFont.deriveFont(new Float(currentFont.getSize() + EditorConfig.EDGE_FONT_SHIFT).floatValue()));
			}			
			
			g.drawString("(" + hValue + ")", tx, ty);		
			
			if (EditorConfig.EDGE_FONT_SHIFT != 0) {
				g.setFont(currentFont);
			}						
		} else {
			if (this.value != null && !this.value.equalsIgnoreCase("none")) {				
				
				//String edgeText = this.value;
						
				Font currentFont = g.getFont();
				if (EditorConfig.EDGE_FONT_SHIFT != 0) {
					g.setFont(currentFont.deriveFont(new Float(currentFont.getSize() + EditorConfig.EDGE_FONT_SHIFT).floatValue()));
				}
				
				/*
				if (this.label != null && !this.label.trim().isEmpty()) {
					g.drawString(this.label + "(v:" + this.value + ")" , tx, ty);
				} else {
					g.drawString(this.value, tx, ty);
				}*/
				/*
				if (this.value != null && !this.value.trim().isEmpty()) {
					if (!value.equals("1") && !value.equals("1.0")) {
						g.drawString(this.value, tx, ty);
					}
				}
				*/
				//if (this.isActionType(nodeA)) {
				if (NodeDisplayFactory.isActionType(nodeA.getType()) || NodeDisplayFactory.isSplitterType(nodeA.getType())) {
					g.drawString(this.value, tx, ty);
				}
				
				if (EditorConfig.EDGE_FONT_SHIFT != 0) {
					g.setFont(currentFont);
				}						
			}
		}
	}
	
	
	@SuppressWarnings("unused")
	private boolean isActionType(Node n) {
		if (n.getType() != null && !n.getType().trim().isEmpty()) {
			return (n.getType().equalsIgnoreCase(NodeType.getTypeMap().get(NodeType.ACTION_NODE)));
		}
		return false;
	}
	
	public boolean isUnderCursor(int mx, int my) {
		
		if( mx < Math.min(nodeA.getX(), nodeB.getX()) ||
			mx > Math.max(nodeA.getX(), nodeB.getX()) ||
			my < Math.min(nodeA.getY(), nodeB.getY()) ||
			my > Math.max(nodeA.getY(), nodeB.getY()) ) {
			return false;
		}
		
		
		int A = nodeB.getY() - nodeA.getY();
		int B = nodeB.getX() - nodeA.getX();
		
		double distance = Math.abs(A*mx - B*my + nodeB.getX()*nodeA.getY() - nodeB.getY()*nodeA.getX())/Math.sqrt(A*A+B*B);
		return distance <= 5;
	}
	
	
	public void move(int dx, int dy) {
		nodeA.move(dx, dy);
		nodeB.move(dx, dy);
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		if (properties == null) {
			this.properties = new LinkedHashMap<String,Object>();
		} else {
			this.properties = properties;
		}
	}
	

	@Override
	public String toString() {
		//String colorHex = "#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
		//return super.toString() + "{s: " + Integer.toString(stroke) + ", c: " + colorHex + "}";
		
		return "('" + this.getSource() + "'->'" + this.getTarget()+ "'): {value:'" + this.getValue() + "', label:'" + this.getLabel() + "'}";
		
	}

}
