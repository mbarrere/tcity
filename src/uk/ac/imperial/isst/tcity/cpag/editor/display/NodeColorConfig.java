package uk.ac.imperial.isst.tcity.cpag.editor.display;

import java.io.Serializable;

import uk.ac.imperial.isst.tcity.cpag.editor.ColorUtils;

public class NodeColorConfig implements Serializable 
{
	private static final long serialVersionUID = 5396347452110584370L;
	
	private String lineColor;
	private String fillColor;
	private String textColor;
	
	public NodeColorConfig() {
		this(ColorUtils.HEX_BLACK, ColorUtils.HEX_WHITE, ColorUtils.HEX_BLACK);		
	}
	
	public NodeColorConfig(String lineColor, String fillColor, String textColor) {
		super();
		this.lineColor = lineColor;
		this.fillColor = fillColor;
		this.textColor = textColor;
	}

	public String getLineColor() {
		return lineColor;
	}

	public void setLineColor(String lineColor) {
		this.lineColor = lineColor;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}
}
