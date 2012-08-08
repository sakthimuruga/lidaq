/**
 *
 */
package ie.deri.urq.lidaq.source;

import gumi.builders.UrlBuilder;
import ie.deri.urq.lidaq.benchmark.SourceLookupBenchmark;
import ie.deri.urq.lidaq.repos.WebRepository;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.hooks.content.ContentHandlerRdfXml;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterRdfXml;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterSuffix;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;
import com.ontologycentral.ldspider.hooks.sink.SinkCallback;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 18, 2011
 */
public class SourceLookup {


	private static final Logger logger = Logger.getLogger(SourceLookup.class
			.getName());
	
	final private SourceLookupBenchmark slb;
	CopyOnWriteArraySet<Future<CrawlResult>> submittedTasks = new CopyOnWriteArraySet<Future<CrawlResult>>();
	CopyOnWriteArraySet<Future<CrawlResult>> finishedTasks = new CopyOnWriteArraySet<Future<CrawlResult>>();
	HashSet<URI> seen = new HashSet<URI>();
	Redirects redirects = new Redirects();

	private final LinkFilterSet _lf;
	SinkCallback _cb;
	private SourceLookupManager _slm;
	private TaskChecker _taskChecker;

	public static String[] BLACKLIST = { ".html", ".xhtml", ".jpg", ".pdf", ".png", ".jpeg", ".gif" };
	final ContentHandlerRdfXml _rdfxmlContentHandler;
	FetchFilterSuffix _blacklist;
	FetchFilter _ff;
	public ANY23ContentHandler _any23Content;
	
	
	private WebRepository _webRep;

	private String _type = "ABOX";

	private boolean _tboxLookup = false;

	/**
	 * @param _cm
	 */
	public SourceLookup(SourceLookupManager slm) {
		_slm = slm;
		_taskChecker = new TaskChecker();
		_taskChecker.start();
		slb = new SourceLookupBenchmark();
		_lf = new LinkFilterSet();
		
		_rdfxmlContentHandler = new ContentHandlerRdfXml();
		_blacklist = new FetchFilterSuffix(CrawlerConstants.BLACKLIST);
		_ff = new FetchFilterRdfXml();
	}

	public void enableANY23Parsing(){
		_any23Content = new ANY23ContentHandler();
		_ff = new FetchFilterANY23();
		_blacklist = new FetchFilterSuffix(BLACKLIST);
	}

	public void addNode(Node n){
		if(n instanceof Resource && n.toString().startsWith("http"))
			try {
				addURI(new URI(n.toString()));
			} catch (URISyntaxException e) {
				;
			}
	}

	/**
	 * @param webRepositoryConnector 
	 * @param uri - request to dereference this URI
	 */
	public void addURI(URI uri) {
		try {
			URI u = normalise(uri);

			if(seen.contains(u)) return;
			seen.add(u);

			Future<CrawlResult> res = _slm.submitTask(u, this);
			submittedTasks.add(res);
			synchronized (_taskChecker) {
				_taskChecker.notify();	
			}
		} catch (URISyntaxException e) {
			;
		}
	}

	/**
	 * @param indexReasonerCallback
	 */
	public void setCallback(Callback cb) {
		_cb =  new SinkCallback(cb);
	}

	/**
	 * @param linkFilterDummy
	 */
	public void setLinkHandler(LinkFilter lf) {
		_lf.addFilter(lf);
	}

	public boolean seen(Node n){
		URI u;
		try {
			u = normalise(new URI(n.toString()));
			return seen.contains(u);
		} catch (URISyntaxException e) {
			return false;
		}
	}

	public static URI normalise(URI u) throws URISyntaxException {
		String path = u.getRawPath();
		
		if (path == null || path.length() == 0) {
			path = "/";
		} else if (path.endsWith("/index.html")) {
			path = path.substring(0, path.length()-10);
		} else if (path.endsWith("/index.htm") || path.endsWith("/index.php") || path.endsWith("/index.asp")) {
			path = path.substring(0, path.length()-9);
		}

		if (u.getHost() == null) {
			throw new URISyntaxException("no host in ", u.toString());
		}

		URI norm 
//		= UrlBuilder.empty()
//	    .withScheme(u.getScheme().toLowerCase())
//	    .withHost(u.getHost().toLowerCase())
//	    .withPort(u.getPort())
//	    .withPath(u.getPath())
//	    .withQuery(u.getQuery()).toUri();
		
		 = new URI(u.getScheme().toLowerCase(),
				u.getUserInfo(), u.getHost().toLowerCase(), u.getPort(),
				path, u.getQuery(), null);
		norm = new URI(norm.normalize().toASCIIString().replaceAll("%25", "%"));
		return norm;
	}


	/**
	 * @param lu
	 * @param to
	 * @param status
	 */
	public void setRedirect(URI lu, URI to, int status) {
		URI u;
		try {
			u = normalise(to);
			redirects.put(lu, u);
			addURI(u);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}

	public boolean idle() {
		return submittedTasks.size() == 0;
	}


	/**
	 * Hard shutdown, cancel all pending tasks
	 */
	public boolean shutdown(){
		boolean success = true;
		logger.info("[SHUTDOWN] ["+_type+"] [REQUEST]");
		_taskChecker.shutdown();
		
			while(submittedTasks.size() != 0){
				int cancelled =0;
//		for(int i=0; i< 3; i++){
				int before = submittedTasks.size();
				for(Future<CrawlResult> cr : submittedTasks){
					try{
						if(cr.isDone()){
							slb.update(cr,_tboxLookup);
							finishedTasks.add(cr);
						}else{
							cancelled++;
							cr.cancel(true);
						}
					}catch(Exception e){
						logger.log(Level.WARNING, "[SHUTDOWN] ["+_type+"] exception",e);
						finishedTasks.add(cr);
						success = false;
					}
				}
				synchronized (submittedTasks) {
					submittedTasks.removeAll(finishedTasks);
				}
				logger.info("[SHUTDOWN] ["+_type+"] [ATTEMPT] before:"+before+" still:"+submittedTasks.size()+" cancelled:"+cancelled);
			}
			logger.info("[SHUTDOWN] ["+_type+"] processed "+finishedTasks.size()+" crawl tasks.");
			return success;
	}

	class TaskChecker extends Thread{
		/**
		 * 
		 */
		public TaskChecker() {
			super();
		}
		boolean run = true;

		public void run() {
			while(run){
				synchronized (this) {
					try{	
						wait(500);
						for(Future<CrawlResult> cr : submittedTasks){
							if(cr.isDone()){
								slb.update(cr,_tboxLookup);
								finishedTasks.add(cr);
							}
						}
						synchronized (submittedTasks) {
							submittedTasks.removeAll(finishedTasks);
							if(submittedTasks.size() == 0){
								if(_webRep!=null)
									_webRep.notifyIdle();
							}
						}
					} catch (InterruptedException e) {
						logger.log(Level.WARNING, "["+_type+"] TaskChecker got interrupted",e);
					}
				}
			}
			logger.info("[SHUTDOWN] ["+_type+"] TaskChecker");
		}

		public void shutdown(){
			run = false;
		}
	}

	/**
	 * @param webRepository
	 */
	public void setWebRepository(WebRepository webRepository) {
		_webRep = webRepository;
	}

	public SourceLookupBenchmark getBenchmark(){
		return slb;
	}

	/**
	 * @param accessLog
	 */
	public void writeAccessLog(File accessLog) {
		logger.info("["+_type+"] Writing "+finishedTasks.size()+" access.log to "+accessLog);
		PrintWriter pw;
		try {
			pw = new PrintWriter(accessLog);

			for(Future<CrawlResult> fr: finishedTasks){
				try{
					String s = fr.get().toString();
					pw.println("[RESULT] ["+_type+"] "+s);
				}catch(Exception e){
					logger.warning("["+_type+"] Cannot write acceslog "+e.getClass().getSimpleName()+" msg:"+e.getMessage());
				}
			}			
			for(Future<CrawlResult> fr: submittedTasks){
				try{
					String s = fr.get().toString();
					pw.println("[MISS] ["+_type+"] "+s);
				}catch(Exception e){
					logger.warning("["+_type+"] Cannot write acceslog "+e.getClass().getSimpleName()+" msg:"+e.getMessage());
				}
			}
			pw.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not write access log",e);
		}
	}

	/**
	 * @return
	 */
	public Callback getLinkFilter() {
		return _lf;
	}


	/**
	 * @return
	 */
	public SinkCallback getContentHandler() {
		return _cb;
	}

	public void isTbox(boolean b) {
		_tboxLookup  = b;
		_type = "TBOX";
		
	}

	public int submittedTasks() {
		return submittedTasks.size();
	}

	public void interrupt() {
		
		
	}
}
