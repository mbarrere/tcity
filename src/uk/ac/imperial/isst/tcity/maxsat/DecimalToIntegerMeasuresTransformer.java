package uk.ac.imperial.isst.tcity.maxsat;

import java.util.List;
import java.util.Map;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;
import uk.ac.imperial.isst.tcity.model.Measure;

public class DecimalToIntegerMeasuresTransformer {
	
	public static Double MAX_DOUBLE =  Double.MAX_VALUE;
	
	private int maxShift = 0;
	private int nodeIncrement = 0;
	
	public DecimalToIntegerMeasuresTransformer() {
		
	}
	
	
	public int getMaxShift() {
		return this.maxShift;
	}

	@SuppressWarnings("unused")
	private void loadMapsAndValuesWithMeasures(AndOrGraph graph, Map<String, Measure> measureByInstanceId, Map<String, Integer> map, Map<String, Integer> weights) {
		
		List<AndOrNode> nodes = graph.getNodes();		
		int index = 1;		
		
		for (AndOrNode n : nodes) {
			if (n.isAtomicType()) {
				map.put(n.getId(), index);				
				index++;

				if (!"inf".equalsIgnoreCase(n.getValue())) {
					int integerPlaces = n.getValue().indexOf('.');
					int decimalPlaces = 0;
					if (integerPlaces != -1) {
						decimalPlaces = n.getValue().length() - integerPlaces - 1;
					}
					if (maxShift < decimalPlaces) {
						maxShift = decimalPlaces;
					}
				}
			}
		}
				
		//for problems with measures and 0 weight on nodes				
		//NEW for measure instances 2019-03-12
		if (measureByInstanceId != null && !measureByInstanceId.isEmpty()) {
			this.nodeIncrement = 1;
			for (String instanceId : measureByInstanceId.keySet()) {
				Measure measure = measureByInstanceId.get(instanceId);
				String cost = measure.getCost();
				
				map.put(instanceId, index);				
				index++;
				
				if (!"inf".equalsIgnoreCase(cost)) {				
					int integerPlaces = cost.indexOf('.');
					int decimalPlaces = 0;
					if (integerPlaces != -1) {
						decimalPlaces = cost.length() - integerPlaces - 1;
					}
					if (maxShift < decimalPlaces) {
						maxShift = decimalPlaces;
					}				
				}
			}
			
			for (String instanceId : measureByInstanceId.keySet()) {
				Double cost = 0.0;
				Measure measure = measureByInstanceId.get(instanceId);
				String costString = measure.getCost();
				
				if ("inf".equalsIgnoreCase(costString)) {
					cost = MAX_DOUBLE;
				} else {					
					cost = Double.parseDouble(costString);
					for (int i = 0; i < maxShift; i++) {
						cost = cost * 10;
					}	
				}							
				weights.put(instanceId, cost.intValue());
			}
		}
		// up to this point

		for (AndOrNode n : nodes) {
			if (n.isAtomicType()) {
				Double value = 0.0;
				if ("inf".equalsIgnoreCase(n.getValue())) {
					value = MAX_DOUBLE;
				} else {
					value = Double.parseDouble(n.getValue());
					for (int i = 0; i < maxShift; i++) {
						value = value * 10;
					}
					value += this.nodeIncrement; //for nodes with weight 0 and problems with measures
				}				
				weights.put(n.getId(), value.intValue());				
			}
		}		
	}
	
	@SuppressWarnings("unused")
	private void loadMapsAndValuesWithMeasures2(List<AndOrNode> nodes, AndOrGraph graph, Map<String, Integer> map, Map<String, Integer> weights) {
		
		int index = 1;
				
		for (AndOrNode n : nodes) {
			if (n.isAtomicType()) {
				map.put(n.getId(), index);				
				index++;

				if (!"inf".equalsIgnoreCase(n.getValue())) {
					int integerPlaces = n.getValue().indexOf('.');
					int decimalPlaces = 0;
					if (integerPlaces != -1) {
						decimalPlaces = n.getValue().length() - integerPlaces - 1;
					}
					if (maxShift < decimalPlaces) {
						maxShift = decimalPlaces;
					}
				}
			}
		}
						

		for (AndOrNode n : nodes) {
			if (n.isAtomicType()) {
				Double value = 0.0;
				if ("inf".equalsIgnoreCase(n.getValue())) {
					value = MAX_DOUBLE;
				} else {
					value = Double.parseDouble(n.getValue());
					for (int i = 0; i < maxShift; i++) {
						value = value * 10;
					}
					value += this.nodeIncrement; //for nodes with weight 0 and problems with measures
				}				
				weights.put(n.getId(), value.intValue());				
			}
		}		
	}
}
