package ie.deri.urq.lidq.benchmark.eval;

import ie.deri.urq.lidq.benchmark.eval.StabilityStats.STABILITYCODE;

public class StabilityStats {

	
	
	public static enum STABILITYCODE{
		
	}
	
	private StringBuilder aggStats = new StringBuilder();	
	private int [] aggStatsSum = new int[8];
	public StabilityStats() {
		aggStats.append("&&&&\\multicolumn{2}{c}{\\textbf{\\underline{network error}}}& &      \\\\\n");
		
		aggStats.append("\\textbf{Query type}&" +
				"		 \\textbf{stable}&" +
				"		 \\textbf{error}&" +
				"		 \\textbf{abox}&" +
				"		 \\textbf{tbox}&" +
				"		 \\textbf{multiple errors} &" +
				"		 \\textbf{inconsistent}\\\\\\hline\n");
	}
	public void update(STABILITYCODE status) {
		// TODO Auto-generated method stub
		
	}
}
