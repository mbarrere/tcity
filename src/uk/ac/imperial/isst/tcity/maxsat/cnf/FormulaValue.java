package uk.ac.imperial.isst.tcity.maxsat.cnf;

/**
 * @author Martin Barrere <m.barrere@imperial.ac.uk>
 */

final class FormulaValue extends Formula {

	  private Boolean value; 
	  
	  public FormulaValue(Boolean value) {
		  this.value = value;
	  }
	  
	  
	  public Boolean getValue() {
		return value;
	  }
	
	  public void setValue(Boolean value) {
		  this.value = value;
	  }
	
	  public <A> A accept(FormulaVisitor<A> visitor) {
	    return visitor.visitValue(this);
	  }
	
	  @Override
	  public String toString() {        
	    return value.toString();
	  }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormulaValue other = (FormulaValue) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}

