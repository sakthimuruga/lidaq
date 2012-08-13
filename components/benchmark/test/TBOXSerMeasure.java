import ie.deri.urq.lidaq.reasoning.ReasonerFramework;

import org.semanticweb.saorr.rules.LinkedRuleIndex;
import org.semanticweb.saorr.rules.Rule;


public class TBOXSerMeasure {

	
	
	public static void main(String[] args) {
		
		long all=0L;
		int rounds=0;
		
		for(; rounds<10; rounds++){
		long start= System.currentTimeMillis();
		try {
			LinkedRuleIndex<Rule> t = ReasonerFramework.deserialiseTBox(ReasonerFramework.class.getResourceAsStream("/resources/tbox_rdfs.gz.ser"));
			t=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("Loaded in "+(end-start));
		all+=(end-start);
		
		System.gc();System.gc();System.gc();
		}
		System.out.println("Avg: "+(all/(double)rounds));
	}
}

