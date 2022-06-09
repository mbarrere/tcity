package uk.ac.imperial.isst.tcity.maxsat.cnf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.isst.tcity.model.AndOrGraph;
import uk.ac.imperial.isst.tcity.model.AndOrNode;

public class FormulaBuilder {
	
	final static Logger logger = LogManager.getLogger(FormulaBuilder.class);
	
	public AndOrGraph buildGraphFromFormula(Formula formula, String sourceId, String targetId) throws Exception {		
		
		if (!formula.getClass().equals(FormulaAnd.class)) {
			logger.error("Cannot build targeted graph from formula. First level connectors should be AND.");
			return null;
		}
		
		AndOrGraph graph = new AndOrGraph();
		
		List<Formula> fms = new ArrayList<Formula>();
		
		FormulaAnd formAnd = (FormulaAnd) formula;
		Iterator<Formula> iterAnd = formAnd.fms.iterator();
		
		boolean targetFound = false;
		
		while (iterAnd.hasNext()) {				
			Formula subAndFormula = iterAnd.next();
			
			if (subAndFormula.getClass().equals(FormulaVar.class)) {
				FormulaVar formVar = (FormulaVar) formula;
								
				if (formVar.name.equals(targetId)) {
					targetFound = true; 									
				} else {
					fms.add(subAndFormula);
				}
			} else {			
				fms.add(subAndFormula);
			}						
		}
		
		if (!targetFound) {
			logger.error("Cannot build targeted graph from formula. Target variable not found.");
			return null;
		}
		
		AndOrNode target = new AndOrNode(targetId, "atomic");
		graph.addNode(target);
		
		@SuppressWarnings("unused")
		Formula mainFormula = CNF.and(fms);
		
		//Set<String> variables = new VariableCounter().getAtomicVariables(formula);
		//this.buildGraphFromFormulaRec(mainFormula, graph, sourceId, targetId, 1);
		return graph;
	}
	
	public void buildGraphFromFormulaRec(Formula formula, AndOrGraph graph, String source, String target, int logicCounter) {		
		/*
		if (formula.getClass().equals(FormulaOr.class)) {
			
			AndOrNode orNode = new AndOrNode("or" + logicCounter++, "or");
			
			FormulaOr formOr = (FormulaOr) formula;
			Iterator<Formula> iterOr = formOr.fms.iterator();			
			
			while (iterOr.hasNext()) {				
				Formula subOrFormula = reduceFormulaRec(iterOr.next(), x, tseitinVisitor);				
								
				if (subOrFormula.getClass().equals(FormulaValue.class)) {
					FormulaValue formValue = (FormulaValue)subOrFormula;
					if (formValue.getValue() == true) {
						return formValue;
					}
				} else {
					fms.add(subOrFormula);
				}
			}
			
			if (fms.size() == 0) {
				return new FormulaValue(false);
			}
			if (fms.size() == 1) {
				return fms.get(0);				
			}
			if (fms.size() > 1) {
				return new FormulaOr(fms);
			}
			
		}
		
		if (formula.getClass().equals(FormulaAnd.class)) {
			FormulaAnd formAnd = (FormulaAnd) formula;
			Iterator<Formula> iterAnd = formAnd.fms.iterator();
			
			while (iterAnd.hasNext()) {				
				Formula subAndFormula = reduceFormulaRec(iterAnd.next(), x, tseitinVisitor);
				if (subAndFormula.getClass().equals(FormulaValue.class)) {
					FormulaValue formValue = (FormulaValue)subAndFormula;
					if (formValue.getValue() == false) {
						return formValue;
					}
				} else {
					fms.add(subAndFormula);
				}
			}
			
			if (fms.size() == 0) {
				return new FormulaValue(true);
			}
			if (fms.size() == 1) {
				return fms.get(0);				
			}
			if (fms.size() > 1) {
				return new FormulaAnd(fms);
			}
		}
		
		if (formula.getClass().equals(FormulaVar.class)) {
			FormulaVar formVar = (FormulaVar) formula;
			
			Object xVarName = vars.get(Math.abs(x));
			if (formVar.name.equals(xVarName)) {
				return new FormulaValue(x > 0); 									
			} else {
				return formVar;
			}
		}
		
		if (formula.getClass().equals(FormulaNeg.class)) {			
			FormulaNeg formNeg = (FormulaNeg) formula;
			
			Formula subNegFormula = reduceFormulaRec(formNeg.fm, x, tseitinVisitor);
			
			if (subNegFormula.getClass().equals(FormulaValue.class)) {
				FormulaValue formValue = (FormulaValue)subNegFormula;				
				return new FormulaValue(!formValue.getValue());				
			} else {
				return new FormulaNeg(subNegFormula);
			}
		}
		*/
	}
	
}
