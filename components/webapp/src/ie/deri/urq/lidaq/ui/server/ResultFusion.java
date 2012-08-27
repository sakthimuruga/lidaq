/**
 *
 */
package ie.deri.urq.lidaq.ui.server;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.LinkedDataQueryEngine;
import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.benchmark.ReasonerBenchmark;
import ie.deri.urq.lidaq.benchmark.SourceLookupBenchmark;
import ie.deri.urq.lidaq.benchmark.WebRepositoryBenchmark;
import ie.deri.urq.lidaq.query.ResultSetWrapper;
import ie.deri.urq.lidaq.repos.SourceSelectionStrategy;
import ie.deri.urq.lidaq.repos.WebRepository;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;
import ie.deri.urq.lidaq.ui.shared.Bindings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.sun.corba.se.impl.orbutil.closure.Future;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jul 3, 2011
 */
public class ResultFusion extends Thread {


	private static final Logger log = Logger.getLogger(ResultFusion.class
			.getName());

	private final static	DecimalFormat twoDForm = new DecimalFormat("#.##");

	private String _q;
	private List<String> _rVars;
	private WebRepositoryManager _wrm;
	private ConcurrentLinkedQueue<Bindings> statics = new ConcurrentLinkedQueue<Bindings>();
	private ConcurrentLinkedQueue<Bindings> fresh = new ConcurrentLinkedQueue<Bindings>();
	private LinkedDataQueryEngine _lidaq;

	private boolean done = false;

	private boolean _seeAlso;

	private SourceSelectionStrategy _srcSel;

	private REASONING_MODE _rMode;

	private String _queryID;

	private QueryLogger logger;

	private ResultSetWrapper liveResults;

	private long start;

	private Query query;

	private HashMap<String, String> _eps;

	private boolean _any23;
	/**
	 * @param q
	 * @param eps
	 * @param eps 
	 * @param _lidaq 
	 * @param rMode 
	 * @param seeAlso 
	 * @param srcSel 
	 * @param logger2 
	 */
	public ResultFusion(String q, String queryID,HashMap<String, String> eps, SourceSelectionStrategy srcSel, boolean seeAlso, REASONING_MODE rMode, LinkedDataQueryEngine lidaq, WebRepositoryManager wrm, QueryLogger logger,boolean any23On) throws Exception{
		_q = q;
		_eps = eps;
		query = QueryFactory.create(_q);

		_rVars = query.getResultVars();
		_rVars.add("status");
		_rVars.add("time");
		_wrm = wrm;
		_lidaq = lidaq;
		_seeAlso = seeAlso;
		_srcSel = srcSel;
		_rMode = rMode;
		_queryID = queryID;
		_any23 = any23On;
		this.logger = logger;

	}
	private Exception[] es =new Exception[2]; 
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try{
			start = System.currentTimeMillis();
			ExecutorService es = Executors.newFixedThreadPool(_eps.size()+1);
			ArrayList<Callable<Exception>> tasks = new ArrayList<Callable<Exception>>(_eps.size()+1);
			
			// exception happens here
//			SparqlQueryThread [] ts = new SparqlQueryThread[_eps.size()];
			LidaqQueryThread lt = null;
			if(_eps.size() != 0){
				int i=0;
				for(Map.Entry<String,String> eps: _eps.entrySet()){
					SparqlQueryThread sp = new SparqlQueryThread(_q, eps.getKey(), eps.getValue());
					tasks.add(sp);
//					ts[i] = sp;
//					ts[i++].start();
				}
			}
			if(_rMode!=null && _srcSel != null){
				lt = new LidaqQueryThread(_q,_srcSel,_seeAlso,_rMode,_any23);
				tasks.add(lt);
//				lt.start();
			}
			List<java.util.concurrent.Future<Exception>> res =  es.invokeAll(tasks,10,TimeUnit.SECONDS);
			
			String ex = "";
			for(java.util.concurrent.Future<Exception> sres :res){
				Exception e = sres.get();
				if(e!=null)
					ex+="[] "+e.getClass().getSimpleName()+" msg:"+e.toString();
			}
//			if(ts.length!=0){for(Thread t :ts){t.join();}}
//			if(lt!=null)lt.join();

//			
//			if(es[0]!=null){
//				
//			}
//			if(es[1]!=null){
//				ex+="[LIDAQ] "+es.getClass().getSimpleName()+" msg:"+es[1].toString();
//			}
			if(ex.length()==0)
				throw new Exception(ex);
			log.info("We are done!");
		} 
		catch (Exception e) {
			log.warning("Exception "+e.getClass().getSimpleName()+" msg: "+e.getMessage());
			throw new RuntimeException(e);
		}finally{
			done = true;
		}

	}

	/**
	 * @return
	 */
	public String[] getVarList() {

		return _rVars.toArray(new String[_rVars.size()]);
	}




	/**
	 * @return
	 */
	public List<Bindings> getNewResults() {
		List<Bindings> b = new ArrayList<Bindings>();


		//get statistics binding
		Bindings stats = new Bindings();

		if(liveResults!=null){
			QueryExecutionBenchmark qeb = liveResults.getLiveBenchmark();

			stats.put("2XX", qeb.get(SourceLookupBenchmark.TOTAL_2XX_LOOKUPS).toString());
			stats.put("3XX", qeb.get(SourceLookupBenchmark.TOTAL_3XX_LOOKUPS).toString());
			stats.put("4XX", qeb.get(SourceLookupBenchmark.TOTAL_4XX_LOOKUPS).toString());
			stats.put("5XX", qeb.get(SourceLookupBenchmark.TOTAL_5XX_LOOKUPS).toString());
			stats.put("6XX", qeb.get(SourceLookupBenchmark.TOTAL_6XX_LOOKUPS).toString());
			stats.put("Retrieved", qeb.get(WebRepositoryBenchmark.CACHE_SIZE).toString());
			if(qeb.get(ReasonerBenchmark.INFERED_STMTS)!=null)
				stats.put("Infered", qeb.get(ReasonerBenchmark.INFERED_STMTS).toString());
			else
				stats.put("Infered", "0");
		}else{
			stats.put("2XX", "0");
			stats.put("3XX", "0");
			stats.put("4XX", "0");
			stats.put("5XX", "0");
			stats.put("6XX", "0");
			stats.put("Retrieved", "0");
			stats.put("Infered", "0");
		}
		b.add(stats);
		while(!statics.isEmpty()){
			b.add(statics.poll());
		}
		while(!fresh.isEmpty()){
			b.add(fresh.poll());
		}
		log.info("Returning: "+b.size()+" "+done+" "+b);
		if(b.size()==1 && done){
			b.add(Bindings.doneBinding);
		}
		return b;
	}
	class LidaqQueryThread implements Callable<Exception> {
		private String _query;
		private boolean _seeAlso;
		private SourceSelectionStrategy _srcSel;
		private REASONING_MODE _rMode;
		private boolean any23;

		/**
		 * @param _q
		 * @param _srcSel
		 * @param _seeAlso
		 * @param _rMode
		 */
		public LidaqQueryThread(String q, SourceSelectionStrategy srcSel,
				boolean seeAlso, REASONING_MODE rMode, boolean any23) {
			_query = q;
			this._srcSel = srcSel;
			this._seeAlso = seeAlso;
			this._rMode = rMode;
			this.any23 = any23;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public Exception call() {
			
			
			try {
				logger.log("["+_queryID+"]-[REQUEST] ui "+URLEncoder.encode(_q,"UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				logger.log("["+_queryID+"]-[REQUEST] ui "+_q.replaceAll("\n", " "));
				e1.printStackTrace();
			}
			WebRepository wr = null;
			try {
				wr = _wrm.getRepository();
				liveResults = _lidaq.lidaq(_q, _queryID, _srcSel, _seeAlso, _rMode,any23, wr);
				while(liveResults.hasNext()){

					QuerySolution next = liveResults.next();
					Bindings rbw = new Bindings(); 
					Iterator<String> iter = next.varNames();
					while(iter.hasNext()){
						String var = iter.next();
						rbw.put(var, next.get(var).toString());
					}
					rbw.put("status","lidaq");
					rbw.put("time",""+(double)(System.currentTimeMillis()-start)/(double)1000);
					synchronized (fresh) {
						fresh.add(rbw);	
					}
				}
				wr.close();
				System.out.println(liveResults.getQueryConfig().toString());
				log.info("["+_queryID+"]-[DETAILS] "+liveResults.getQueryConfig().toString());
				logger.log("["+_queryID+"]-[RESULTS] "+liveResults.getQueryConfig().oneLineSummary());
				log.info("["+_queryID+"]-[RESULTS] "+liveResults.getQueryConfig().getBenchmark().oneLineSummary(" "));
				return null;
			} catch (Exception e) {
				//        		log("Exception",e);
				e.printStackTrace();
				
				logger.log("["+_queryID+"]-[EXPECTION] "+e.getClass().getSimpleName());
				log.info("["+_queryID+"]-[EXPECTION] "+e.getClass().getSimpleName());
				es[1]=e;
				return new RuntimeException(e.getClass().getSimpleName()+" msg: "+e.getMessage());
				
			}finally{
				
				if(wr!=null)wr.close();
			}
		}
	}
	
	class SparqlQueryThread  implements Callable<Exception>{

		private final String _epURI;
		private final String _epName;
		private final String _query;
		/**
		 * 
		 */
		public SparqlQueryThread(String query, String epName, String epURI) {
			_epURI = epURI;
			_epName = epName;
			_query = query;
		}
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public Exception call() {
			QueryExecution qe = null;
			//
			try {
				qe = QueryExecutionFactory.sparqlService(_epURI,_query);
				ResultSet rsStatic;
				rsStatic = qe.execSelect();
				int count =0;
				while(rsStatic.hasNext()){
					count++;
					QuerySolution next = rsStatic.next();
					Bindings rbw = new Bindings(); 
					Iterator<String> iter = next.varNames();

					while(iter.hasNext()){
						String var = iter.next();
						rbw.put(var, next.get(var).toString());
					}
					rbw.put("status",_epName);
					rbw.put("time",""+(double)(System.currentTimeMillis()-start)/(double)1000);
					synchronized (statics) {
						statics.add(rbw);
					}
				}
				log.info("Found "+count+" static results");
				return null;
			} 
			catch(Exception e) { 
				log.warning(e.getClass().getSimpleName()+" msg:"+e.getMessage());
				es[0]=e;
				return new RuntimeException(e.getClass().getSimpleName()+" msg: "+e.getMessage());
			}
			finally {
				if(qe != null)qe.close();
			}
		}
	}
}


