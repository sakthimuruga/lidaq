/**
 *
 */
package ie.deri.urq.lidaq;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.cli.TimeOutThread;
import ie.deri.urq.lidaq.query.LTBQEQueryConfig;
import ie.deri.urq.lidaq.query.QueryConfig;
import ie.deri.urq.lidaq.query.QueryEngine;
import ie.deri.urq.lidaq.query.ResultSetWrapper;
import ie.deri.urq.lidaq.query.arq.LIDAQOpExecutor;
import ie.deri.urq.lidaq.query.arq.SmarContentFilterVisitor1;
import ie.deri.urq.lidaq.repos.QueryBasedSourceSelection;
import ie.deri.urq.lidaq.repos.SourceSelectionStrategy;
import ie.deri.urq.lidaq.repos.WebRepository;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;
import ie.deri.urq.lidaq.source.BasicSourceSelectionStrategies;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.namespace.OWL;
import org.semanticweb.yars.nx.namespace.RDFS;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Aug 21, 2010
 */
public class LTBQEQueryEngine extends QueryEngine{

	private static final Logger logger = Logger.getLogger(LTBQEQueryEngine.class.getName());

	

	private final WebRepositoryManager wrm;

	private WebRepository _webRep;

	private final ExecutionContext ec2;

	private TimeOutThread timeOutThread;

	/**
	 * @param wrm2
	 */
	public LTBQEQueryEngine(WebRepositoryManager wrm) {
		this.wrm = wrm;
		ARQ.getContext().setFalse( ARQ.optFilterPlacement );
		ec2 = new ExecutionContext(ARQ.getContext(), null, null, null) ;
		
	}

	public ResultSetWrapper lidaq(String query, String queryID,SourceSelectionStrategy srcSel,
			boolean seeAlso,
			REASONING_MODE rMode, boolean any23, WebRepository webrep) throws Exception{
		QueryExecutionBenchmark b = new QueryExecutionBenchmark(queryID, query);
		LTBQEQueryConfig qc = new LTBQEQueryConfig(queryID);
		qc.setQuery(query);
		qc.setQueryExecutionBenchmark(b);
		qc.sourceSelection(srcSel);
		qc.reasoningMode(rMode);
		qc.setFollowSeeAlso(seeAlso);
		qc.setEnableAny23(any23);  
		return executeSelect(query,qc,webrep);
	}

	/**
	 * @param query
	 * @param qc
	 * @param webrep
	 * @return
	 * @throws Exception 
	 */
	public ResultSetWrapper executeSelect(final String query, final LTBQEQueryConfig qc,
			final WebRepository webrep) throws Exception {
		Query q = QueryFactory.create(query) ;
		
		ResultSetWrapper resSet = new ResultSetWrapper(createQueryIterator(q, qc,webrep), q.getResultVars(), qc,null,webrep);

		return resSet;
	}

	/**
	 * 
	 * @param qc - the {@link LTBQEQueryConfig} file
	 * @param webrep - the current used {@link WebRepository}
	 * @param time - external setting of start time
	 * @return - a {@link ResultSetWrapper} object
	 * @throws Exception
	 */
	public ResultSetWrapper executeSelect(LTBQEQueryConfig qc,
			WebRepository webrep, final long time) throws Exception {
		Query q = QueryFactory.create(qc.getQuery()) ;
		
		ResultSetWrapper resSet = new ResultSetWrapper(createQueryIterator(q, qc,webrep), q.getResultVars(), qc,time,webrep);

		return resSet;
	}
	
	public ResultSetWrapper executeSelect(LTBQEQueryConfig qc,
			WebRepository webrep) throws Exception {
		Query q = QueryFactory.create(qc.getQuery()) ;
		long start = System.currentTimeMillis();
		ResultSetWrapper resSet = new ResultSetWrapper(createQueryIterator(q, qc,webrep), q.getResultVars(), qc,start,webrep);

		return resSet;
	}	

	@Override
	public ResultSet executeSelect(QueryConfig qc) throws Exception {
		if(_webRep==null)setup();
		return executeSelect(qc.getQuery(), (LTBQEQueryConfig) qc, _webRep);
	}
	
	
	@Override
	public void tearDown() {
		logger.warning("[SHUTDOWN] [REQUEST] LTBQEQueryEngine ");
		if(timeOutThread!=null){
			timeOutThread.stopThread();
		}
		_webRep.close();
		logger.warning("[SHUTDOWN] [DONE] LTBQEQueryEngine ");
	}

	@Override
	public void tearDown(QueryExecutionBenchmark qeb) {
		tearDown();

		if(qeb != null){
			qeb.putAll(_webRep.getBenchmark());
			qeb.getKeyOrder().putAll(_webRep.getBenchmark().getKeyOrder());
			if(qeb.getBenchDir() != null){
				_webRep.writeAccessLog(qeb.getBenchDir(),qeb.getQueryID());
				_webRep.dumpCache(new File(qeb.getBenchDir(),qeb.getQueryID()+".data.nq.gz"));
			}
			
		}
		_webRep = null;
	}

	@Override
	public void setup() {
		if(_webRep!=null){
			_webRep=null;
			System.gc();
		}
		_webRep = wrm.getRepository();	
	}

	
	/**
	 * @param query
	 * @param qc
	 * @return 
	 * @throws Exception 
	 */
	public ResultSet executeSelect(String query, LTBQEQueryConfig qc) throws Exception{
		WebRepository webRep = wrm.getRepository();
		return executeSelect(query, qc, webRep);
	}


	public QueryIterator createQueryIterator(final String sparqlQuery, LTBQEQueryConfig qc) throws Exception {
		Query q = QueryFactory.create(sparqlQuery) ;
		WebRepository webRep = wrm.getRepository();
		return createQueryIterator(q, qc,webRep);
	}

	/**
	 * @param query
	 * @param qc
	 * @param webrep2 
	 * @return
	 * @throws Exception 
	 */
	public QueryIterator createQueryIterator(final Query query, final LTBQEQueryConfig qc, final WebRepository webRep) throws Exception {
		logger.fine("Parsing query into operator tree");
		Op op = parseQueryIntoOp(query);
		
		JoinVariableVisitor1 jv = new JoinVariableVisitor1();
		OpWalker.walk(op, jv);
		
		Set<Variable> joinVars = new HashSet<Variable>();
		joinVars.addAll(jv.getJoinVariables());
		if(query.getBindingVariables()!=null){
			for(Var v :query.getBindingVariables()){
				joinVars.add(new Variable(v.getName()));
			}
		}
		
		QueryBasedSourceSelection srcSel = new QueryBasedSourceSelection(qc.getSourceSelection());
		srcSel.setJoinVariable(joinVars);
		srcSel.setResultVariable(convert(query.getResultVars()));
		webRep.setSourceSelection(srcSel);
		
		if(qc.enableAny23())webRep.enableANY23Parsing();

		SmarContentFilterVisitor1 scfv = new SmarContentFilterVisitor1();
		OpWalker.walk(op, scfv);
		if(!scfv.hasPredicateVariable() && 
				( qc.getReasoningMode().name().equals(REASONING_MODE.OFF.name()) || 
						qc.getReasoningMode().name().equals(REASONING_MODE.OWL.name()) )
				){
			//filter statements only if we do no reasoning 
			scfv.getPredicateSet().add(RDFS.RANGE);
			scfv.getPredicateSet().add(RDFS.DOMAIN);
			scfv.getPredicateSet().add(RDFS.SUBCLASSOF);
			scfv.getPredicateSet().add(RDFS.SUBPROPERTYOF);
			scfv.getPredicateSet().add(OWL.SAMEAS);
			webRep.setContentFilter(scfv.getPredicateSet());
		}

		webRep.followSeeAlso(qc.followSeeAlso());
		webRep.enableReasoning(qc.getReasoningMode());

		if(qc.getQueryExecutionBenchmark()!=null)
			qc.getQueryExecutionBenchmark().addOperatorTree(op);

		/** Assumption is that subject and object URIs in the query are relevant **/
		GetURIVisitor1 uriV = new GetURIVisitor1();
		OpWalker.walk(op, uriV);
		
		for(Resource r : uriV.getSubjectURIs()){
			   webRep.submitSeedSource(r);
		   }
		   for(Resource r : uriV.getObjectURIs()){
			   webRep.submitSeedSource(r);
		   }
		if(qc.getSourceSelection() == BasicSourceSelectionStrategies.ALL){ 
		   for(Resource r : uriV.getPredicateURIs()){
			   webRep.submitSeedSource(r);
		   }
		}
		
		
		
		OpExecutorFactory plainFactory = 
				new OpExecutorFactory(){
					public OpExecutor create(ExecutionContext execCxt){
						return new LIDAQOpExecutor(execCxt,webRep,qc) ;
					}
				};
		ec2.setExecutor(plainFactory) ;
		
		QueryIterator input = QueryIterRoot.create(ec2);
		
		if(qc.getTimeout()!=-1){
			timeOutThread = new TimeOutThread(qc.getTimeout(),webRep);
			timeOutThread.start();
		}
		logger.fine("Creatign the query iterators");
		return  QC.execute(op, input, ec2);
	}

	//	/**
	//	 * @param q
	//	 * @param b
	//	 * @return
	//	 * @throws ParseException 
	//	 */
	//	public QueryIterator createQueryIterator(Query q, QueryBenchmark b, SOURCE_SELECTION srcSelStrat, REASONING_MODE rMode) throws ParseException {
	//		long start = System.currentTimeMillis();
	//		
	//		logger.info("Query evaluation with source selection: "+srcSelStrat+" and reasoing "+rMode);
	//		
	//		_cache = new Cache();
	//		_reasoner = new Reasoner(_cache, rMode);
	//		_consolidator = new Consolidator(_cache, rMode);
	//
	//		_sl = new SourceLookup(_cache,_reasoner,_consolidator,_pHost,_pPort);
	//
	//		if(b.getLogDir() != null)
	//			_sl.enableAccessLog(new File(b.getLogDir(),b.getID()+".access.log"));
	//		if(_cachedRedirects != null )
	//			_sl.setTBoxRedirects(_cachedRedirects);
	//		if(_cachedSeen != null)
	//			_sl.setTBoxSeen(_cachedSeen);
	//		if(_cachedTBoxRules != null && _reasoner!=null)
	//			_reasoner.setTBoxRules(_cachedTBoxRules);
	//
	//		_sourceLookupBenchmark = _pool.submit(_sl);
	//		_reasonerBenchmark = _pool.submit(_reasoner);
	//		_consolidationBenchmark = _pool.submit(_consolidator);
	//
	//		Operator op = parseQueryIntoOp(q);
	//		
	//		JoinVariableVisitor jv = new JoinVariableVisitor();
	//		jv.visit(op);
	//		jv.getJoinVariables();
	//		
	//		_visitor = new VisitorLODQ(_sl,_cache,_consolidator, q, jv.getJoinVariables(),qc);
	//		
	//		QueryIterator qi = _visitor.visit(op);
	//	
	//		return qi;
	//		
	//		
	//	}

	//	/**
	//	 * @param query
	//	 * @param qc
	//	 * @return
	//	 * @throws ParseException 
	//	 */
	//	public ResultSet executeQuery(String sparqlQuery, QueryConfig qc) throws ParseException {
	//		
	//		
	//		
	//		Query q = QueryParser.parse(sparqlQuery) ;
	//		return executeQuery(q, qc);
	//		
	//		
	//	}
	//	
	//	public ResultSet executeQuery(Query query, QueryConfig qc) {
	//		long start = System.currentTimeMillis();
	//		QueryIterator qi = createQueryIterator(query, qc);
	//		
	//		long start1 = System.currentTimeMillis();
	//		ResultSet resSet = ResultSet.consume(qi, query);
	//		shutdown();
	//		long end = System.currentTimeMillis();
	//
	//		logger.info("Query evaluation finished in "+(end-start)+" ms with "+resSet.size()+" results");
	//
	//		return resSet;
	//		
	//	}


	//
	//	public ResultSet executeQuery(final String sparqlQuery, QueryBenchmark b, SOURCE_SELECTION srcSelStrat, REASONING_MODE rMode) throws ParseException{
	//		long start = System.currentTimeMillis();
	//		Query q = QueryParser.parse(sparqlQuery) ;
	//		QueryIterator qi = createQueryIterator(q, b,srcSelStrat,rMode);
	//		long start1 = System.currentTimeMillis();
	//		ResultSet resSet = ResultSet.consume(qi, q);
	//		shutdown();
	//		long end = System.currentTimeMillis();
	//
	//	
	//		logger.info("Query evaluation finished in "+(end-start)+" ms with "+resSet.size()+" results");
	//
	//		return resSet;
	//	}
	//
	//	public void executeAndFormat(String sparqlQuery, SOURCE_SELECTION sourceStrategy, REASONING_MODE rMode, ResultSetFormatter.TYPE type, PrintWriter out, boolean printvars, QueryBenchmark b) throws ParseException{
	//		long start = System.currentTimeMillis();
	//		Query q = QueryParser.parse(sparqlQuery) ;
	//		QueryIterator qi = createQueryIterator(q, b,sourceStrategy,rMode);
	//		long start1 = System.currentTimeMillis();
	//		int totalSize = ResultSetFormatter.format(qi, q, out, printvars, type);
	//
	//		shutdown();
	//		long end = System.currentTimeMillis();
	//
	//		if(b!=null){
	////			b.consumeTime((end-start1));
	////			b.totalTime((end-start));
	////			b.resultSize(totalSize);
	////			b.lookups(_sl.lookups());
	////			b.cacheSize(_cache.size());
	////			b.inferred(_reasoner.getInferredStmts().size());
	//		}
	//		logger.info("Query evaluation finished in "+(end-start)+" ms with "+totalSize+" results");
	//	}

	//	public QueryIterator getQueryIterator(final String sparqlQuery, final SOURCE_SELECTION srcSelStrat) throws ParseException{
	//		return getQueryIterator(QueryParser.parse(sparqlQuery),srcSelStrat) ;
	//	}
	//
	//	public QueryIterator getQueryIterator(final Query q, final QueryConfig qC) throws ParseException{
	//		
	//		Operator op = parseQueryIntoOp(q);
	//		
	//		JoinVariableVisitor jv = new JoinVariableVisitor();
	//		jv.visit(op);
	//		jv.getJoinVariables();
	//		
	//		_visitor = new VisitorLODQ(_sl,_cache,_consolidator, q, jv.getJoinVariables(),srcSelStrat);
	//		
	//		QueryIterator qi = _visitor.visit(op);
	//	
	//		return qi;
	//	}

	/**
	 * @param resultVars
	 * @return
	 */
	private ArrayList<Variable> convert(List<String> resultVars) {
		ArrayList<Variable> vars = new ArrayList<Variable>();
		for(String v: resultVars){
			vars.add(new Variable(v));
		}
		return vars;
	}


	public void storeTBox(File file){
		//		ObjectOutputStream oos;
		//		try {
		//			oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
		//			oos.writeObject(_sl.getTBoxRedirects());
		//			oos.writeObject(_sl.getTBoxSeen());
		//			oos.writeObject(_reasoner.getTBoxRules());
		//			oos.close();
		//		} catch (FileNotFoundException e) {
		//			e.printStackTrace();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
	}

	public void restoreTBox(File file){
		//		ObjectInputStream ois;
		//		try {
		//			ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
		//			_cachedRedirects = (Redirects)ois.readObject();
		//			//			_deref.setTBoxRedirects((Redirects)ois.readObject());
		//			_cachedSeen = (Set<URI>)ois.readObject();
		//			_cachedTBoxRules = (StatementStore)ois.readObject();
		//
		//			ois.close();
		//		} catch (FileNotFoundException e) {
		//			e.printStackTrace();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		} catch (ClassNotFoundException e) {
		//			e.printStackTrace();
		//		}
	}

	//	public void shutdown(){
	//		if(wrm!=null)
	//			wrm.shutdown();
	////		if(_reasoner !=null)
	////			_reasoner.shutdown();
	////		if(_consolidator != null)
	////			_consolidator.shutdown();
	//		shutdownAndAwaitTermination(_pool);
	//	}

	/**
	 * @param q - {@link Query} 
	 * @return the operator plan for the query
	 */
	private Op parseQueryIntoOp(Query q){

		Op op = Algebra.compile(q) ;
		ARQ.getContext().setFalse( ARQ.optFilterPlacement );
		op = Algebra.optimize(op,ARQ.getContext()) ;

		return op;
	}

	private void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					logger.warning("[FATAL] Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	//	/**
	//	 * 
	//	 * @param pHost
	//	 * @param pPort
	//	 */
	//	private void setProxy(String pHost, String pPort) {
	//		_pHost = pHost;
	//		_pPort = pPort;
	//	}

	/**
	 * @param b
	 */
	public void updateBenchmark(QueryExecutionBenchmark b) {

		//		if( b == null) return;
		//		try {
		//			b.update(webRep.getBenchmark());
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		try {
		//			b.update(_consolidationBenchmark.get());
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		try {
		//			b.put(Benchmark.CACHE_SIZE,_cache.size());
		//			b.put(Benchmark.ABOX_RAW_SIZE,_cache.rawAboxSize());
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		try {
		//			b.update(_reasonerBenchmark.get());
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
	}



	/**
	 * @param benchmark
	 */
	public void dumpCache(QueryExecutionBenchmark benchmark) {
		if( benchmark == null) return;

		//		File cacheDir = benchmark.getBenchDir();
		//		File aBoxDump =new File(cacheDir,"abox.nq");
		//		File tBoxDump = new File(cacheDir,"tbox.nq");
		//			FileWriter fw;
		//			try {
		//				fw = new FileWriter(aBoxDump);
		//				_cache.dumpAbox(fw);
		//				
		//				fw.close();
		//				fw = new FileWriter(tBoxDump);
		//				for(Node [] n: _cache.getTBoxData()){
		//					fw.write(Nodes.toN3(n)+"\n");
		//					fw.flush();
		//				}
		//			} catch (IOException e) {
		//				e.printStackTrace();
		//			}
		//		
	}

	public WebRepository getRepository() {
		return wrm.getRepository();
	}
}