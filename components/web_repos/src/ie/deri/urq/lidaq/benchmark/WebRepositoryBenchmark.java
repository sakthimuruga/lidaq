/**
 *
 */
package ie.deri.urq.lidaq.benchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 20, 2011
 */
public class WebRepositoryBenchmark extends Benchmark{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String CACHE_SIZE = "cache_size";
	public static final String REGISTERED_KEYS = "registered_keys";
	public static final String RETURNED_STMTS = "returned_stmts";
	public static final String SEED_URIS = "no_seed_URIs";
	public static final String FOLLOW_SEEALSO = "follow_seeAlso";
	public static final String FOLLOW_SAMEAS = "follow_sameAs";
	public static final String SOURCE_SEL = "src_sel";
	public static final String TBOX_CACHE_SIZE = "tbox_cache_size";
	public static final String RETRIEVED = "retrieved";
	
	
    private final  Map<String,List<String>> _om = new HashMap<String,List<String>>();
    private final  List<String> _keyOrder = new ArrayList<String>();
    {
    	_keyOrder.add(SEED_URIS);
    	_keyOrder.add(FOLLOW_SEEALSO);
    	_keyOrder.add(FOLLOW_SAMEAS);
    	_keyOrder.add(SOURCE_SEL);
    	_keyOrder.add(REGISTERED_KEYS);
    	_keyOrder.add(RETURNED_STMTS);
    	_keyOrder.add(RETRIEVED);
    	_keyOrder.add(CACHE_SIZE);
    	_keyOrder.add(TBOX_CACHE_SIZE);
    	
    	_om.put(WebRepositoryBenchmark.class.getSimpleName(), _keyOrder);
    }
	
    public WebRepositoryBenchmark() {
    	setProperty(TBOX_CACHE_SIZE, "0");
	}

	/* (non-Javadoc)
	 * @see ie.deri.urq.lidaq.benchmark.Benchmark#getKeyOrder()
	 */
	@Override
	public Map<String, List<String>> getKeyOrder() {
		// TODO Auto-generated method stub
		return _om;
	}

	
}
