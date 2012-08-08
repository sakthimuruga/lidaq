/**
 *
 */
package ie.deri.urq.lidaq.benchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 29, 2010
 */
public class ReasonerBenchmark extends Benchmark {

	public ReasonerBenchmark() {
		put(INFERED_STMTS, 0);
	}
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final  Map<String,List<String>> _om = new HashMap<String,List<String>>();
    private final  List<String> _keyOrder = new ArrayList<String>();
   
    {
    	_keyOrder.add(REASONING_MODE);
    	_keyOrder.add(INFERED_STMTS);
    	_om.put(ReasonerBenchmark.class.getSimpleName(), _keyOrder);
    }
	
	public static final String REASONING_MODE = "reasoning_mode";
	public static final String INFERED_STMTS = "infered_stmts";
	
	public Map<String, List<String>> getKeyOrder() {
		return _om;
	}
}