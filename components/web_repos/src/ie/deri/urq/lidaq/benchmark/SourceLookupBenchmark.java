/**
 *
 */
package ie.deri.urq.lidaq.benchmark;

import ie.deri.urq.lidaq.source.CrawlResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Feb 14, 2011
 */
public class SourceLookupBenchmark extends Benchmark {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger =Logger
			.getLogger(SourceLookupBenchmark.class.getName());
	
	public static final String TOTAL_LOOKUPS ="total_lookups";
	public static final String TOTAL_2XX_LOOKUPS = "total_2xx_lookups";
	public static final String TOTAL_3XX_LOOKUPS = "total_3xx_lookups";
    public static final String TOTAL_4XX_LOOKUPS = "total_4xx_lookups";
    public static final String TOTAL_5XX_LOOKUPS = "total_5xx_lookups";
    public static final String TOTAL_6XX_LOOKUPS = "total_6xx_lookups";

	
	public static final String TOTAL_TBOX_LOOKUPS ="total_tbox_lookups";
	public static final String TOTAL_2XX_TBOX_LOOKUPS = "total_2xx_tbox_lookups";
	public static final String TOTAL_3XX_TBOX_LOOKUPS = "total_3xx_tbox_lookups";
    public static final String TOTAL_4XX_TBOX_LOOKUPS = "total_4xx_tbox_lookups";
    public static final String TOTAL_5XX_TBOX_LOOKUPS = "total_5xx_tbox_lookups";
    public static final String TOTAL_6XX_TBOX_LOOKUPS = "total_6xx_tbox_lookups";

	
    private final  Map<String,List<String>> _om = new HashMap<String,List<String>>();
    private final  List<String> _keyOrder = new ArrayList<String>();
   
    {
    	_keyOrder.add(TOTAL_LOOKUPS);
    	_keyOrder.add(TOTAL_2XX_LOOKUPS);
    	_keyOrder.add(TOTAL_3XX_LOOKUPS);
    	_keyOrder.add(TOTAL_4XX_LOOKUPS);
    	_keyOrder.add(TOTAL_5XX_LOOKUPS);
    	_keyOrder.add(TOTAL_6XX_LOOKUPS);
    	_keyOrder.add(TOTAL_TBOX_LOOKUPS);
    	_keyOrder.add(TOTAL_2XX_TBOX_LOOKUPS);
    	_keyOrder.add(TOTAL_3XX_TBOX_LOOKUPS);
    	_keyOrder.add(TOTAL_4XX_TBOX_LOOKUPS);
    	_keyOrder.add(TOTAL_5XX_TBOX_LOOKUPS);
    	_keyOrder.add(TOTAL_6XX_TBOX_LOOKUPS);
    	_om.put(SourceLookupBenchmark.class.getSimpleName(), _keyOrder);
    }
    /**
	 * 
	 */
	public SourceLookupBenchmark() {
		this.put(TOTAL_LOOKUPS,0);
		this.put(TOTAL_2XX_LOOKUPS,0);
		this.put(TOTAL_3XX_LOOKUPS,0);
		this.put(TOTAL_4XX_LOOKUPS,0);
		this.put(TOTAL_5XX_LOOKUPS,0);
		this.put(TOTAL_6XX_LOOKUPS,0);
		this.put(TOTAL_TBOX_LOOKUPS,0);
		this.put(TOTAL_2XX_TBOX_LOOKUPS,0);
		this.put(TOTAL_3XX_TBOX_LOOKUPS,0);
		this.put(TOTAL_4XX_TBOX_LOOKUPS,0);
		this.put(TOTAL_5XX_TBOX_LOOKUPS,0);
		this.put(TOTAL_6XX_TBOX_LOOKUPS,0);
	}

	/**
	 * @param cr
	 */
	public void update(Future<CrawlResult> cr, boolean tboxUpdate) {
		try {
			if(!tboxUpdate){
				if((""+cr.get().getStatus()).startsWith("2")) update(TOTAL_2XX_LOOKUPS);
				if((""+cr.get().getStatus()).startsWith("3")) update(TOTAL_3XX_LOOKUPS);
				if((""+cr.get().getStatus()).startsWith("4")) update(TOTAL_4XX_LOOKUPS);
				if((""+cr.get().getStatus()).startsWith("5")) update(TOTAL_5XX_LOOKUPS);
				if((""+cr.get().getStatus()).startsWith("6")) update(TOTAL_6XX_LOOKUPS);
				update(TOTAL_LOOKUPS);
			}else{
				if((""+cr.get().getStatus()).startsWith("2")) update(TOTAL_2XX_TBOX_LOOKUPS);
				if((""+cr.get().getStatus()).startsWith("3")) update(TOTAL_3XX_TBOX_LOOKUPS);
				if((""+cr.get().getStatus()).startsWith("4")) update(TOTAL_4XX_TBOX_LOOKUPS);
				if((""+cr.get().getStatus()).startsWith("5")) update(TOTAL_5XX_TBOX_LOOKUPS);
				if((""+cr.get().getStatus()).startsWith("6")) update(TOTAL_6XX_TBOX_LOOKUPS);
				update(TOTAL_TBOX_LOOKUPS);
			}
		}catch(OutOfMemoryError oom){
			
		}
		catch (Exception e) {
			update(TOTAL_6XX_LOOKUPS);
			logger.warning("Cannot update this result "+e.getClass().getSimpleName()+" msg:"+e.getMessage());
		}
	}

	/**
	 * @param total2xxLookups
	 */
	private void update(String key) {
		String s = getProperty(key);
		Integer si=0;
		if(s!=null) si= Integer.valueOf(s);
		this.put(key, ""+(si+1));
	}
	/* (non-Javadoc)
	 * @see ie.deri.urq.lidaq.benchmark.Benchmark#getKeyOrder()
	 */
	@Override
	public Map<String,List<String>> getKeyOrder() {
		return _om;
	}
}
