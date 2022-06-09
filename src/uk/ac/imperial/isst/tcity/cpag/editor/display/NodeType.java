package uk.ac.imperial.isst.tcity.cpag.editor.display;

import java.util.LinkedHashMap;
import java.util.Map;

public enum NodeType {
	BASIC_NODE("Basic node"),
	CPAG_BASE_NODE("CPAG base node"), 
	LOGICAL_NODE ("Logical operator"), 
	CYBER_NODE ("Cyber privilege"),
	PHYSICAL_NODE ("Physical privilege"),	
	ACTION_NODE ("Action node"),
	IMPACT_NODE ("Impact node"),
	AND_NODE ("AND operator"),
	OR_NODE ("OR operator"),
	SPLITTER_NODE ("Splitter gate"),
	CUSTOM_NODE ("Custom node") 
	;
	
	String nodeType;
	
	NodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	
	@Override
	public String toString() {
		return nodeType;
	}
	
	public static Map<NodeType,String> typeMap = null;
	
	public static Map<NodeType,String> getTypeMap() {
		if (typeMap == null) {
			typeMap = new LinkedHashMap<NodeType,String>();
			typeMap.put(NodeType.BASIC_NODE, "atomic");
			typeMap.put(NodeType.CYBER_NODE, "cyber");
			typeMap.put(NodeType.PHYSICAL_NODE, "physical");
			typeMap.put(NodeType.IMPACT_NODE, "impact");
			typeMap.put(NodeType.AND_NODE, "AND");
			typeMap.put(NodeType.OR_NODE, "OR");
			typeMap.put(NodeType.SPLITTER_NODE, "SP");
			typeMap.put(NodeType.ACTION_NODE, "action");
			typeMap.put(NodeType.CUSTOM_NODE, "custom");
		}
		return typeMap;
	}
	
}