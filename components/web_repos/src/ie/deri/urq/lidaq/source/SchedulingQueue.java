/**
 *
 */
package ie.deri.urq.lidaq.source;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.Future;

import org.semanticweb.yars.tld.TldManager;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 21, 2011
 */
public class SchedulingQueue {

	private TldManager _tldm;

	private final Stack<Future<CrawlResult>> _q = new Stack<Future<CrawlResult>>();
	private final Map<String, Stack<Future<CrawlResult>>> pldTasks = new HashMap<String, Stack<Future<CrawlResult>>>();
	private final TreeMap<String, Long> PLDAddedTime = new TreeMap<String, Long>();
	
	
	
	/**
	 * @param _tldm
	 */
	public SchedulingQueue(TldManager tldm) {
		_tldm = tldm;
	}

	//ms
	long waitTimePerPLD = 500;
	
	/**
	 * add the task directly to the queue if nothing was scheduled or add it to the pld stack
	 * @param f
	 */
	public void add(Future<CrawlResult> f,URI u) {
		if(u==null) return;
		String pld = _tldm.getPLD(u);
		if(pld==null) pld = "NO_PLD";
		long time = System.currentTimeMillis();
		if(!PLDAddedTime.containsKey(pld)){
			_q.add(f); 
			synchronized (PLDAddedTime) {
				PLDAddedTime.put(pld, time);
			}
		}else{
			Stack<Future<CrawlResult>> c = pldTasks.get(pld);
			if(c==null){
				c = new Stack<Future<CrawlResult>>();
				pldTasks.put(pld,c);
			}
			c.add(f);
		}
	}

	/**
	 * @return
	 */
	public Future<CrawlResult> next() {
		return _q.pop();
	}

	/**
	 * 
	 */
	private void schedule() {
		synchronized (PLDAddedTime) {
			long time = System.currentTimeMillis();
			for(Entry<String,Long> ent: PLDAddedTime.entrySet()){
				if(waitTimePerPLD-(time-ent.getValue())<=0){
					Stack<Future<CrawlResult>> c = pldTasks.get(ent.getKey());
					
					if(c!=null&&!c.isEmpty()){
						_q.add(c.pop());
						PLDAddedTime.put(ent.getKey(), time);
					}
				}
			}
		}
	}

	/**
	 * @return
	 */
	public boolean hasNext() {
		if(_q.isEmpty()){
			schedule();
		}
		return !_q.isEmpty();
	}
}