package uk.ac.imperial.isst.tcity.cpag.editor.display;

import java.awt.Color;

import uk.ac.imperial.isst.tcity.cpag.editor.ColorUtils;

public class NodeDisplayFactory {
	
	public static boolean isActionType(String type) {
		return (type != null) && ( 
				type.equalsIgnoreCase("action")
				|| type.equalsIgnoreCase("cyber-action")
				|| type.equalsIgnoreCase("physical-action") 				
				|| type.equalsIgnoreCase("attack")
				|| type.equalsIgnoreCase("cyber-attack")
				|| type.equalsIgnoreCase("physical-attack") 
				|| type.equalsIgnoreCase("cyber-physical")
				|| type.equalsIgnoreCase("cyber-physical-attack")
				);
	}
	
	public static boolean isLogicType(String type) {
		return (type != null) && (
				type.equalsIgnoreCase("or") || type.equalsIgnoreCase("and")
				);
	}
	
	public static boolean isSplitterType(String type) {
		return (type != null) && (
				type.equalsIgnoreCase("splitter") ||
				type.equalsIgnoreCase("SP")
				);
	}
	
	public static boolean isCyberType(String type) {
		return (type != null) && (
				type.equalsIgnoreCase("cyber")
				);
	}
	
	public static boolean isPhysicalType(String type) {
		return (type != null) && (
				type.equalsIgnoreCase("physical")
				);
	}
	
	public static boolean isImpactType(String type) {
		return (type != null) && (
				type.equalsIgnoreCase("impact")
				);
	}
	
	public static boolean isInitType(String type) {
		return (type != null) && (
				type.equalsIgnoreCase("init")
				);
	}
	
	public static boolean isPrivilegeType(String type) {
		return isCyberType(type) || isPhysicalType(type) || isImpactType(type) || isInitType(type);
	}
	
	public static NodeDisplay getNodeDisplay (String type) {
		//System.out.println("NodeDisplayFactory. Type requested: " + type);
		
		if (type == null || type.trim().isEmpty()) {
			return new NodeDisplay();
		}
		
		if (isActionType(type)) {
			//return new ActionNodeDisplay(new NodeColorConfig(ColorUtils.HEX_BLACK, ColorUtils.HEX_WHITE, ColorUtils.HEX_BLACK));
			//return new ActionNodeDisplay(new NodeColorConfig("#75aed1", ColorUtils.HEX_WHITE, ColorUtils.HEX_BLACK));
			return new ActionNodeDisplay(new NodeColorConfig(ColorUtils.toHexColor(Color.GRAY), ColorUtils.HEX_WHITE, ColorUtils.HEX_BLACK));
		}
		
		if (isLogicType(type)) {
			return new LogicalNodeDisplay(new NodeColorConfig("#b87aff", "#e8cfff", ColorUtils.HEX_BLACK));
		}		
		
		if (isSplitterType(type)) {
			return new SplitterNodeDisplay(new NodeColorConfig("#b87aff", "#e8cfff", ColorUtils.HEX_BLACK));
		}
		
		if (type.equalsIgnoreCase("cyber")){
			//return new NodeDisplay(Color.decode("#75aed1"), Color.decode("#c4e9ff"));
			return new NodeDisplay(new NodeColorConfig("#75aed1", "#c4e9ff", ColorUtils.HEX_BLACK));
		}
		
		if (type.equalsIgnoreCase("impact")){
			//return new NodeDisplay(Color.decode("#6ae366"), Color.decode("#dfffdb"));
			return new NodeDisplay(new NodeColorConfig("#6ae366", "#dfffdb", ColorUtils.HEX_BLACK));
		}
		
		if (type.equalsIgnoreCase("physical")){
			//return new NodeDisplay(Color.decode("#fca103"), Color.decode("#f7be54"));
			//return new NodeDisplay(new NodeColorConfig("#fca103", "#f7be54", ColorUtils.HEX_BLACK));
			//yellow
			return new NodeDisplay(new NodeColorConfig("#ffe959", "#fcf2b1", ColorUtils.HEX_BLACK));
		}
		
		// test
		//if (type.equalsIgnoreCase("cpa")){ return new NodeDisplay(Color.RED, Color.GREEN); }

		if (type.equalsIgnoreCase("init")){			
			return new NodeDisplay(new NodeColorConfig("#f5684c", "#FFD6C2", ColorUtils.HEX_BLACK));
		}
		
		return new NodeDisplay();
	}

}
