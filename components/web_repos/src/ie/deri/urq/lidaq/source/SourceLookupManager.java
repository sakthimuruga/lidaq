/**
 *
 */
package ie.deri.urq.lidaq.source;


import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import org.semanticweb.yars.tld.TldManager;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.http.ConnectionManager;
import com.ontologycentral.ldspider.http.robot.Robots;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Aug 21, 2010
 */
public class SourceLookupManager{
	private static final Logger logger =Logger.getLogger(SourceLookupManager.class
			.getName());
	
	
	public static final String IDLE = "IDLE";
	
	private CrawlTaskFactory ctf = CrawlTaskFactory.HTTPFACTORY;
	
	private int ABOX_THREADS = 64;
//	private int TBOX_THREADS = 25;
	private final ExecutorService _pool;
	
	final ConnectionManager _cm;
//	private TldManager _tldm;
	final Robots _robots;
	
	int added=0;

	private TaskScheduler _ts;
	private TldManager _tldm;
	private SchedulingQueue _queue;

		
	public SourceLookupManager(String pHost, String pPort) {
		Integer port = 0;
		if(pPort != null)
			port = Integer.valueOf(pPort);
		
		_cm = new ConnectionManager(pHost, port, null, null, (ABOX_THREADS*2)*CrawlerConstants.MAX_CONNECTIONS_PER_THREAD);
		_cm.setRetries(CrawlerConstants.RETRIES);
		
		 _pool = Executors.newFixedThreadPool(ABOX_THREADS);
		 
		try {
			_tldm = new TldManager();
		} catch (IOException e1) {
			logger.info("cannot get tld file locally " + e1.getMessage());
		}
		_robots = new Robots(_cm);
		_queue = new SchedulingQueue(_tldm);
		_ts = new TaskScheduler();
		 _ts.start();
		logger.info("Initialised: threads: abox: "+ABOX_THREADS +" (pHost:"+pHost+" pPort:"+pPort+")");
	}

	public Future<CrawlResult> submitTask(final URI uri, SourceLookup sl){
		HttpDerefTask t = new HttpDerefTask(uri, sl, this);

		Future<CrawlResult> f = new FutureTask(t);
		_queue.add(f,uri);
		synchronized (_ts) {
			_ts.notify();
		}
		return f;
//		return _pool.submit(t);
	}
	
	/**
	 * 
	 */
	public void shutdown() {
		_ts.run=false;
		_pool.shutdown();
		_cm.shutdown();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("LDSpider\n");

		return sb.toString();	
	}

	/**
	 * @return
	 */
	public SourceLookup getSourceLookup() {
		return new SourceLookup(this);
	}
	
	class TaskScheduler extends Thread{
		
		boolean run = true;
		public void run() {
			while(run){
				while(_queue.hasNext()){
					logger.info("[SCHEDULE] next task");
					_pool.execute((Runnable)_queue.next());
				}
				synchronized (this) {
					try {
						this.wait(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}	
			}
		}
	}

	public void setCrawlTaskFactory(CrawlTaskFactory ctf2) {
		ctf = ctf2;
	}
}