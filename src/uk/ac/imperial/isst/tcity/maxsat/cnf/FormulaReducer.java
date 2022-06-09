/**
 * 
 */
package uk.ac.imperial.isst.tcity.maxsat.cnf;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class FormulaReducer {

	final static Logger logger = LogManager.getLogger(FormulaReducer.class);

	public Formula reduceFormulaByElimination(Formula formula, String var, Boolean isVarNegated) {		
		return this.reduceFormulaByEliminationRec(formula, var, isVarNegated);		
	}

	
	public Formula reduceFormulaByEliminationRec(Formula formula, String var, Boolean isVarNegated) {
				
		List<Formula> fms = new ArrayList<Formula>();		
		
		if (formula.getClass().equals(FormulaOr.class)) {
			FormulaOr formOr = (FormulaOr) formula;
			Iterator<Formula> iterOr = formOr.fms.iterator();			
			
			while (iterOr.hasNext()) {				
				Formula subOrFormula = reduceFormulaByEliminationRec(iterOr.next(), var, isVarNegated);				
								
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
				Formula subAndFormula = reduceFormulaByEliminationRec(iterAnd.next(), var, isVarNegated);
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
						
			if (formVar.name.equals(var)) {
				return new FormulaValue(!isVarNegated); 									
			} else {
				return formVar;
			}
		}
		
		if (formula.getClass().equals(FormulaNeg.class)) {			
			FormulaNeg formNeg = (FormulaNeg) formula;
			
			Formula subNegFormula = reduceFormulaByEliminationRec(formNeg.fm, var, isVarNegated);
			
			if (subNegFormula.getClass().equals(FormulaValue.class)) {
				FormulaValue formValue = (FormulaValue)subNegFormula;				
				return new FormulaValue(!formValue.getValue());				
			} else {
				return new FormulaNeg(subNegFormula);
			}
		}
		
		return null;
	}
	
	
	
	
	
	
	////// OLD //////
	public Formula reduceFormulaWithTseitinOld(Formula formula, Integer x, TseitinVisitor tseitinVisitor) {		
		return this.reduceFormulaWithTseitinOldRec(formula, x, tseitinVisitor);		
	}
	
	public Formula reduceFormulaWithTseitinOldRec(Formula formula, Integer x, TseitinVisitor tseitinVisitor) {
		
		Map<Integer, Object>  vars = tseitinVisitor.getVarNameMap();
		
		List<Formula> fms = new ArrayList<Formula>();		
		
		if (formula.getClass().equals(FormulaOr.class)) {
			FormulaOr formOr = (FormulaOr) formula;
			Iterator<Formula> iterOr = formOr.fms.iterator();			
			
			while (iterOr.hasNext()) {				
				Formula subOrFormula = reduceFormulaWithTseitinOldRec(iterOr.next(), x, tseitinVisitor);				
								
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
				Formula subAndFormula = reduceFormulaWithTseitinOldRec(iterAnd.next(), x, tseitinVisitor);
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
			
			Formula subNegFormula = reduceFormulaWithTseitinOldRec(formNeg.fm, x, tseitinVisitor);
			
			if (subNegFormula.getClass().equals(FormulaValue.class)) {
				FormulaValue formValue = (FormulaValue)subNegFormula;				
				return new FormulaValue(!formValue.getValue());				
			} else {
				return new FormulaNeg(subNegFormula);
			}
		}
		
		return null;
	}
	
	
}
