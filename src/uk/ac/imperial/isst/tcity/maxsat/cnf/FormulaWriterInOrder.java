/**
 * 
 */
package uk.ac.imperial.isst.tcity.maxsat.cnf;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 *
 */
public class FormulaWriterInOrder {

	final static Logger logger = LogManager.getLogger(FormulaWriterInOrder.class);
	
	public void writeFormula(OutputStream os, Formula formula) {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os));
		try {
			StringBuffer s = writeFormulaRec(formula);
			if (s.length() >= 2) {
				s.deleteCharAt(0);
				s.deleteCharAt(s.length()-1);
				w.write(s.toString());
				
			} else {
				w.write("Empty formula!");
			}
			w.write("\n");
			w.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void writeFormulaLog(Level level, Formula formula) {		
		
		StringBuffer s = writeFormulaRec(formula);
		if (s.length() >= 2) {
			s.deleteCharAt(0);
			s.deleteCharAt(s.length()-1);
			logger.log(level, s.toString());
			
		} else {
			logger.log(level, "Empty formula!");
		}									
	}
	
	public StringBuffer writeFormulaRec(Formula formula) {
		StringBuffer s = new StringBuffer();
		
		if (formula.getClass().equals(FormulaOr.class)) {
			FormulaOr formOr = (FormulaOr) formula;
			Iterator<Formula> iterOr = formOr.fms.iterator();
			//for each form.fms
			s.append("(");
			while (iterOr.hasNext()) {
				Formula f = iterOr.next();
				StringBuffer sRec = writeFormulaRec(f);
				s.append(sRec);
				if (iterOr.hasNext()) {
					s.append(" | ");
				}
			}
			s.append(")");
		}
		
		if (formula.getClass().equals(FormulaAnd.class)) {
			FormulaAnd formAnd = (FormulaAnd) formula;
			Iterator<Formula> iterAnd = formAnd.fms.iterator();
			
			s.append("(");
			while (iterAnd.hasNext()) {
				Formula f = iterAnd.next();
				StringBuffer sRec = writeFormulaRec(f);
				s.append(sRec);
				if (iterAnd.hasNext()) {
					s.append(" & ");
				}
			}
			s.append(")");
		}
		
		if (formula.getClass().equals(FormulaVar.class)) {
			FormulaVar formVar = (FormulaVar) formula;
			s.append(formVar.name);						
		}
		
		if (formula.getClass().equals(FormulaNeg.class)) {
			FormulaNeg formNeg = (FormulaNeg) formula;
			s.append("~");
			StringBuffer sRec = writeFormulaRec(formNeg.fm);			
			s.append(sRec);
		}
		
		return s;
	}
	
	
}
