package uk.ac.imperial.isst.tcity.maxsat.config;

public class MaxSatConfig {
	
	private SatMode satMode;
	private MathMode mathMode;
	private OptimMode optimMode;
	
	public MaxSatConfig() {
		this.satMode = SatMode.FALSIFY;
		this.mathMode = MathMode.LINEAR;
		this.optimMode = OptimMode.MIN;
	}

	public SatMode getSatMode() {
		return satMode;
	}

	public void setSatMode(SatMode satMode) {
		this.satMode = satMode;
	}

	public MathMode getMathMode() {
		return mathMode;
	}

	public void setMathMode(MathMode mathMode) {
		this.mathMode = mathMode;
	}

	public OptimMode getOptimMode() {
		return optimMode;
	}

	public void setOptimMode(OptimMode optimMode) {
		this.optimMode = optimMode;
	}
		
}
