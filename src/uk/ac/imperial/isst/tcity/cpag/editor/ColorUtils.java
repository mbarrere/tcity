package uk.ac.imperial.isst.tcity.cpag.editor;

import java.awt.Color;

public class ColorUtils {

	public static final String HEX_WHITE = "#FFFFFF";
	public static final String HEX_BLACK = "#000000";
	public static final String HEX_ORANGE = "#fab041";
	//public static final String HEX_DARK_GRAY = "#4c4c4f";#373738
	public static final String HEX_DARK_GRAY = "#373738";
	
	public static String toHexColor(Color color) {
		return "#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
	}
	
	public static Color fromHexColor(String hexColor) {
		return Color.decode(hexColor);
	}
}
