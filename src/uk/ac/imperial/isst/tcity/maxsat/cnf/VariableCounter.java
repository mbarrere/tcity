package uk.ac.imperial.isst.tcity.maxsat.cnf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class VariableCounter {

	public Set<String> getAtomicVariables(Formula formula) {		
		Set<String> variables = new HashSet<String>();
		this.getAtomicVariablesRec(formula, variables);
		//System.out.println("Variables: " + variables.size());
		return variables;
	}
	
	public void getAtomicVariablesRec(Formula formula, Set<String> variables) {	
		
		if (formula.getClass().equals(FormulaOr.class)) {
			FormulaOr formOr = (FormulaOr) formula;
			Iterator<Formula> iterOr = formOr.fms.iterator();			
			
			while (iterOr.hasNext()) {				
				getAtomicVariablesRec(iterOr.next(), variables);								
			}						
		}
		
		if (formula.getClass().equals(FormulaAnd.class)) {
			FormulaAnd formAnd = (FormulaAnd) formula;
			Iterator<Formula> iterAnd = formAnd.fms.iterator();
			
			while (iterAnd.hasNext()) {				
				getAtomicVariablesRec(iterAnd.next(), variables);				
			}			
		}
		
		if (formula.getClass().equals(FormulaVar.class)) {
			FormulaVar formVar = (FormulaVar) formula;
			variables.add(formVar.name.toString());			
		}
		
		if (formula.getClass().equals(FormulaNeg.class)) {			
			FormulaNeg formNeg = (FormulaNeg) formula;
			
			getAtomicVariablesRec(formNeg.fm, variables);			
		}
		
	}
		
}
