package uk.ac.imperial.isst.tcity.cpag.editor;
import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeColorConfig;
import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeDisplay;
import uk.ac.imperial.isst.tcity.cpag.editor.display.NodeDisplayFactory;
/*
@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "type", 
		  defaultImpl = CustomNode.class)
		@JsonSubTypes({ 
		  @Type(value = CyberNode.class, name = "cyber"),
		  @Type(value = CyberNode.class, name = "CYBER"),
		  @Type(value = PhysicalNode.class, name = "physical"),
		  @Type(value = PhysicalNode.class, name = "PHYSICAL"),
		  @Type(value = ActionNode.class, name = "action"),
		  @Type(value = ActionNode.class, name = "ACTION"),
		  @Type(value = ActionNode.class, name = "attack"),
		  @Type(value = ActionNode.class, name = "ATTACK"),
		  @Type(value = ImpactNode.class, name = "impact"),
		  @Type(value = ImpactNode.class, name = "IMPACT"),
		  @Type(value = AndNode.class, name = "and"),
		  @Type(value = AndNode.class, name = "AND"),
		  @Type(value = OrNode.class, name = "or"),
		  @Type(value = OrNode.class, name = "OR"),
		  @Type(value = SplitterNode.class, name = "splitter"),
		  @Type(value = SplitterNode.class, name = "SPLITTER"), 
		  @Type(value = Node.class, name = "init"),
		  @Type(value = Node.class, name = "INIT"),
		  @Type(value = ImpactNode.class, name = "goal"),
		  @Type(value = ImpactNode.class, name = "GOAL"), 
		  @Type(value = CustomNode.class, name = "custom"),
		  @Type(value = CustomNode.class, name = "CUSTOM")
		  
		})
		*/

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Node implements Serializable {

	private static final long serialVersionUID = 5396347452110584370L;	
	public static final Integer DEFAULT_RADIUS = 20;
	public static int ID_COUNTER = 1;
	
	protected int x;
	protected int y;
	protected int r;

	//@JsonProperty("text")
	protected String label;			
	protected String id;
	protected String value;	
	protected String type;
	
	//protected List<NodeProperty> properties;
	protected Map<String,Object> properties;
	
	protected NodeColorConfig colorConfig;
	@JsonIgnore
	protected NodeDisplay display;
	
	
	//JSON constructor
	public Node() {
		this.r = DEFAULT_RADIUS;
		this.display = null; 
		this.colorConfig = null;
		
		this.id = String.valueOf(Node.ID_COUNTER++);
		this.type = "custom";
		this.value = "";
		this.label = "???";
		this.properties = new LinkedHashMap<String,Object>();
		//properties.put("bValue", "0.5");
	}
		
	// custom builder
	public Node(int x, int y, String label, String type, String value) {
		this.x = x;
		this.y = y;						
		this.label = label;
		
		this.id = String.valueOf(Node.ID_COUNTER++);
		if (value == null) {
			this.value = "";
		} else {
			this.value = value;
		}
	
		this.type = type;
		if (this.type == null) {
			this.type = "custom";
		} 
		
		this.properties = new LinkedHashMap<String,Object>();
		
		this.display = NodeDisplayFactory.getNodeDisplay(this.type);
		this.colorConfig = this.display.getColorConfig();
		
	}
	
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}

	
	
	public boolean isUnderCursor(int mx, int my) {
		int a = x - mx;
		int b = y - my;
		
		return a*a + b*b <= r*r;
	}

	public void move(int dx, int dy) {
		x += dx;
		y += dy;
	}	
	
	
	public int getR() {
		return r;
	}
	
	public void setR(int r) {
		this.r = r;
		/*
		if(r < RADIUS_VALUES[0])
			this.r = RADIUS_VALUES[0];
		else if(r > RADIUS_VALUES[RADIUS_VALUES.length-1])
			this.r = RADIUS_VALUES[RADIUS_VALUES.length-1];
		else
			this.r = r;
			*/
	}
	
	public void changeRadius(int step) {
		setR(r+step);
	}
	
	public Color getColor() {
		return ColorUtils.fromHexColor(this.getColorConfig().getFillColor());
	}
	
	@JsonIgnore
	public void setColor(Color color) {
		if(color != null) {
			this.getColorConfig().setFillColor(ColorUtils.toHexColor(color));
		}			
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label){
		if(label == null) {
			this.label = "";
		}else {
			this.label = label;
		}		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
	
	public void draw(Graphics g) {
		if (this.display == null) {
			this.display = NodeDisplayFactory.getNodeDisplay(this.getType());
			
			if (this.colorConfig == null) {
				this.colorConfig = this.display.getColorConfig();
			} else {
				this.display.setColorConfig(this.colorConfig);
			}
		}
		display.draw(this, g, this.getLabel()); 
		
		
	}		
	
	public String getType() {		 		
		return this.type;		
	}
		
	public void setType(String type) {
		this.type = type;
		if (this.type == null) {
			this.type = "custom";
		} 
	}

	public NodeColorConfig getColorConfig() {
		return colorConfig;
	}

	public void setColorConfig(NodeColorConfig colorConfig) {
		this.colorConfig = colorConfig;		
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
		//return super.toString() + "{r: " + Integer.toString(r) + ", c: " + colorHex + ", t: " + text + "}";
		//return "[" + nodeType + "]: (" + Integer.toString(x) + ", " + Integer.toString(y) + ") " + "{id: '" + id + "', label: '" + label + "', type: '" + type + "', value: '" + value + "'}";
		//return "(" + Integer.toString(x) + ", " + Integer.toString(y) + ") " + "{id: '" + id + "', label: '" + label + "', type: '" + type + "', value: '" + value + "'}";
		
		return ("'" + this.getId() + "' : (" + this.getX() + "," + this.getY() + ") - {label:'" + this.getLabel() + "', type:'" + this.getType() + "', value:'" + this.getValue() + "'}");
	}

}
