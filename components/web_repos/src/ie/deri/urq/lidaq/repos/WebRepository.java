/**
 *
 */
package ie.deri.urq.lidaq.repos;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.benchmark.WebRepositoryBenchmark;
import ie.deri.urq.lidaq.reasoning.ReasonerFramework;
import ie.deri.urq.lidaq.source.BasicSourceSelectionStrategies;
import ie.deri.urq.lidaq.source.CrawlResult;
import ie.deri.urq.lidaq.source.CrawlTaskFactory;
import ie.deri.urq.lidaq.source.LinkFilterSet;
import ie.deri.urq.lidaq.source.OWLSameAsLinkFilter;
import ie.deri.urq.lidaq.source.RDFSSeeAlsoLinkFilter;
import ie.deri.urq.lidaq.source.SourceLookup;
import ie.deri.urq.lidaq.source.SourceLookupManager;
import ie.deri.urq.lidaq.source.SourceSelection;
import ie.deri.urq.lidaq.source.TBoxLinkFilter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.MethodNotSupportedException;
import org.semanticweb.saorr.Statement;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.namespace.OWL;
import org.semanticweb.yars.nx.namespace.RDFS;
import org.semanticweb.yars.nx.parser.Callback;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 18, 2011
 */
public class WebRepository {
	AtomicInteger aCounter = new AtomicInteger(0);  
	
	public static final BNode INTERRUPT = new BNode("INTERRUPT");
	
	private static final Logger logger =Logger.getLogger(WebRepository.class.getName());

	private final PatternIndex _pI;
	private final MapTripleStoreWrapper _tS;

	private final CopyOnWriteArraySet<KeyObserver> _obs;
	private final SourceLookup _sl;
	private  SourceSelection _srcSel;

	private ReasonerFramework _rF = null;
	private final LinkFilterSet _lF;

	private boolean _followSeeAlso = false;
	private RDFSSeeAlsoLinkFilter _lFSeeAlso;

	private boolean _followOwlSameAs = false;
	private OWLSameAsLinkFilter _lFSameAs;

	Node[] spoVar = {new Variable("s"),new Variable("p"),new Variable("o")};
	private REASONING_MODE _rMode = REASONING_MODE.OFF;

	private HashSet<Resource> _predFilter;

	private final SourceLookupManager _slm;
	
	private SourceLookup _sltBox;



	private WebRepositoryBeforeReasoningFiller _wrbrf;
	
	private WebRepositoryAfterReasoningFiller _wrarf;
	
	private WebRepositoryBeforeReasoningFiller _tbox_wrbrf;
	
	private Node [] seeAlso = new Node[4];
	{
		seeAlso[1]= RDFS.SEEALSO;
		seeAlso[2]= new Variable("o");
		seeAlso[3]= new Variable("c");
	}
	
	private int _seedURIs;
	
	//how many statements are send to the query engine
	private int _returnedStmts;

	
	
	private Node [] sameAs = new Node[4];
	{ sameAs[1]= OWL.SAMEAS;
	  sameAs[2]= new Variable("o");
	  sameAs[3]= new Variable("c");
	}

	/**
	 * 
	 */
	public WebRepository(final SourceLookupManager slm) {
		_slm = slm;
		_sl = _slm.getSourceLookup();
		_tS = new MapTripleStoreWrapper(this);
		_pI = new PatternIndex();

		_obs  = new CopyOnWriteArraySet<KeyObserver>();
		//use the authorative sources selection as defaul
		setSourceSelection(new SourceSelection(BasicSourceSelectionStrategies.AUTH));

		_wrbrf = new WebRepositoryBeforeReasoningFiller(this);
		_sl.setCallback(_wrbrf);
		_lF = new LinkFilterSet();
		_sl.setLinkHandler(_lF);
		_sl.setWebRepository(this);

		logger.info("[INIT] ts:"+_tS.size()+" obs:"+_obs.size()+" pi:"+_pI.size());
	}

	public SourceLookup getSourceLookup(){
		return _sl;
	}
	public void setSourceSelection(final SourceSelection srcSel){
		_srcSel = srcSel;
	}

	
	
	
	
	/**
	 * @param statement
	 */
	public synchronized void notify(Node[] stmt) {
		if(_lFSameAs !=null) _lFSameAs.processStatement(stmt);
		
		Set<TriplePattern> patterns = _pI.getRelevantPattern(stmt);
		for(TriplePattern p: patterns){
			Set<Resource> derefNodes = _srcSel.selectURIs(stmt,p);
			int sameAsCount=0;
			for(Node s : derefNodes){
				if(_followSeeAlso){
					seeAlso[0] = s;
					for(Nodes nn: _tS.retrieveStatements(new Statement(seeAlso))){
						_sl.addNode(nn.getNodes()[2]);
					}
					_lFSeeAlso.pivot((Resource)s);
					//
				}
				if(_followOwlSameAs){
					sameAs[0] = s;
					for(Nodes nn: _tS.retrieveStatements(new Statement(sameAs))){
						_sl.addNode(nn.getNodes()[2]);
					}
					_lFSameAs.pivot((Resource)s);
				}
				try {	
					_sl.addURI(new URI(s.toString()));
				} catch (URISyntaxException e) {
					logger.info("[NOTIFY] "+e.getClass().getSimpleName()+" msg:"+e.getMessage()+" cause:"+s);
				}
			}
			
			for(KeyObserver s: p.getOperators()){
				logger.info("[NOTIFY] "+Nodes.toN3(stmt)+" observers:"+s.getID()+" (deref-src:"+derefNodes.size()+", owlSameAs:"+sameAsCount+") for pattern "+Nodes.toN3(p.getKey()));
				s.notifyResult(stmt);
			}
		}
		if(patterns.size() != 0)
			_returnedStmts++;
	}

	/**
	 * @param r
	 */
	public void submitSeedSource(Resource r) {
		try {
			if(_followSeeAlso){
				logger.info("[SEED] [PIVOT] [SEEALSO] Adding "+r+" as pivot for rdfs:seeAlso");
				_lFSeeAlso.pivot(r);
			}
			if(_followOwlSameAs){
				_lFSameAs.pivot(r);
				logger.fine("[SEED] [PIVOT] [SAMEAS] Adding "+r+" as pivot for owl:sameAs");
			}
			_sl.addURI(new URI(r.toString()));
			logger.log(Level.INFO, "[SEED] adding URI "+r);
			_seedURIs++;
		} catch (Throwable e) {
			logger.log(Level.WARNING, "[SEED] adding URI "+r,e);
		}
	}

	public <T> void registerObserver(T[] key, T[] varVals, int[] varPos, Set<T> srcs, KeyObserver<T> obs) throws MethodNotSupportedException{
		throw new MethodNotSupportedException("");
	}

	public <T> void registerKey(T[] key, Set<String> srcs, KeyObserver<T> obs) {
		registerKey(obs.convertFrom(key),srcs,obs);
	}

	/**
	 * Register a KeyObserver object
	 * @param obs - {@link KeyObserver}
	 */
	public void registerKey(Node[] key, Set<String> srcs, KeyObserver obs) {
		registerKey(key, null, null, srcs, obs);
	}


	public void registerKey(Node[] key, Node[] joinVals, int[] varPos, Set<String> srcs, KeyObserver<Node> obs){
		if(srcs==null) srcs = new HashSet<String>(0);

		List<Variable> keyVars = new ArrayList<Variable>();
		for(Node n: key){
			if(n instanceof Literal && Variable.isJoinLiteral((Literal)n)){
				keyVars.add(Variable.fromJoinLiteral((Literal)n));
			}
			if(n instanceof Variable)
				keyVars.add((Variable)n);
		}
		Node [] tpKey = Arrays.copyOf(key , Math.max(4, key.length));
		if(joinVals != null && varPos != null){
			Node [] join = Arrays.copyOf(joinVals , joinVals.length);
			for(int i=0; i<join.length; i++){
				tpKey[varPos[i]] = join[i];
			}
		}
		if(key.length==3){
			tpKey[3]= new Variable("varContext"+aCounter.getAndIncrement());
		}
		TriplePattern tp = new TriplePattern(tpKey, keyVars, varPos, srcs, obs);
		
		_pI.indexPattern(tp);
		_obs.add(obs);

		//notify the observer about already 
		Statement s = new Statement(tpKey);
		Set<Nodes> res = _tS.retrieveStatements(s);
		if(tpKey.toString().equals("http://dbpedia.org/resource/Off_the_Wall_%28album%29")){
			System.out.println("We found "+res.size()+" statements for "+Nodes.toN3(tpKey));
		}
		notifyWithNodes(obs,res);

		//srcSel decides which sources needs to be dereferenced
		int resSize = 0;
		if(res !=null) 
			resSize = res.size();
		int sameAsCount=0;
		Set<Resource> derefNodes = _srcSel.selectURIs(tp, resSize);
		for(Resource n : derefNodes){
			if(n.toString().equals("http://dbpedia.org/resource/Off_the_Wall_%28album%29")){
				System.out.println("Request to deref "+n);
			}
			_sl.addNode(n);
			
			if(_followSeeAlso){
				seeAlso[0] = n;
				for(Nodes nn: _tS.retrieveStatements(new Statement(seeAlso)))
					_sl.addNode(nn.getNodes()[2]);
				_lFSeeAlso.pivot(n);
			}
			if(_followOwlSameAs){
				sameAs[0] = n;
				for(Nodes nn: _tS.retrieveStatements(new Statement(sameAs))){
					sameAsCount++;
					_sl.addNode(nn.getNodes()[2]);
				}
				_lFSameAs.pivot(n);
			}
		}
		logger.info("[REGISTER] "+Nodes.toN3(tpKey)+" from "+obs.getID()+" (deref-src:"+derefNodes.size()+", owlSameAs:"+sameAsCount+")");
	}

//	class WebRepsitoryBeforeReasonerFiller implements Callback{
//
//		final private WebRepository _webRepos;
//
//		private int count;
//
//		private long lastReceived =-1L;
//
//		public WebRepsitoryBeforeReasonerFiller(final WebRepository webRepos) {
//			_webRepos = webRepos;
//			lastReceived  = System.currentTimeMillis();
//		}
//
//		public void processStatement(Node[] statement) {
////			if(statement[0].toString().equals("http://dbpedia.org/resource/Off_the_Wall_%28album%29")){
////				System.out.println(" Found "+Nodes.toN3(statement));
////			}
//			if(statement[1].toString().equals("http://code.google.com/p/ldspider/ns#headerInfo")
//					||statement[1].toString().startsWith("http://www.w3.org/2006/http#"))
//				return;
//			lastReceived  = System.currentTimeMillis();
//			count++;
//		
//			_webRepos.notify(statement);
////			logger.info("[FETCHED] "+ Nodes.toN3(statement));
//			if(! _webRepos.doReasoning() || _rMode.name().equals(REASONING_MODE.RDFS.name())){
//				if(_predFilter == null || _predFilter.contains(statement[1])){
//					/*should be fine to apply a pred content filter here, 
//					either we do no reasoning, then it is fine, or we do 
//					RDFS reasoning and the predicates will be added to the reasoner anyway*/
//					
//					_webRepos.getTripleStore().setCurrentStatement(new Statement(statement));
//					_webRepos.getTripleStore().indexCurrentStatement();
//				}
//			}
//			if(_webRepos.doReasoning()){
//				_webRepos.getReasonerFramework().processStatement(statement);
//				
//			}
//		}
//
//		public int getCount(){return count;}
//		public void endDocument() { ; }
//		public void startDocument() { ; }
//	}



	public Callback getIndexCallback() {
		if(_wrarf == null){
			_wrarf = new WebRepositoryAfterReasoningFiller(this); 
		}
		return _wrarf;
	}



	/**
	 * @return
	 */
	public MapTripleStoreWrapper getTripleStore() {
		return _tS;
	}



	/**
	 * @param observer
	 * @param res
	 */
	private void notify(KeyObserver observer, TreeSet<Node[]> res) {
		if(res == null) return;
		for(Node[] stmt: res){
			observer.notifyResult(stmt);
		}
	}

	private void notifyWithNodes(KeyObserver observer, Set<Nodes> res) {
		if(res == null) return;
		for(Nodes stmt: res){
			if(stmt.getNodes().length !=4){
				logger.warning("WOW this should be of length 4 "+stmt);
			}
			observer.notifyResult(stmt.getNodes());
		}
	}

//	/**
//	 * @return - the current number of statements in the in-mem triple store
//	 */
//	public int cacheSize() {
//		return _tS.size();
//	}

	public int aboxTasks(){
		return _sl.submittedTasks();
	}
	public int tboxTasks(){
		if(_sltBox != null)
			return _sltBox.submittedTasks();
		return 0;
	}
	public int reasoningCache(){
		if(doReasoning())
			return _rF.cacheSize();
		return 0;
	}


	/**
	 * @return
	 */
	public boolean idle() {
		return idle(false);
	}
	
	public boolean idle(boolean log) {
		boolean slIdle = _sl.idle();
		StringBuilder sb=null; 
		if(log) sb = new StringBuilder();
		
		if(_sltBox != null) slIdle = slIdle && _sltBox.idle();
		
		if(doReasoning()){
			slIdle = slIdle && _rF.cacheSize()==0;  
		}
		
		long elapsedTime=0L;
		long elapsedTimeTBox=0L;
		if(!slIdle){
			//ok seems like we have sometimes the problem that we have stale connections and do not clean them
			if( !_sl.idle() && _wrbrf.lastStmtReceived() != -1){
				elapsedTime = System.currentTimeMillis() - _wrbrf.lastStmtReceived(); 
				if( elapsedTime > (CrawlResult.LAST_RECEIVED_TIME_FRAME)){
					if(!doReasoning()){
						slIdle = true;
					}
					else if(_rF.cacheSize()==0){
						//ok, reasoner is not doing anything
						if (_sltBox != null && _sltBox.idle()){
							//tbox source lookup is idle as well, damn thats a problem, give it another shot
							logger.warning("[STALE] last result "+elapsedTime+" ms; assuming stale abox source lookup component");
							slIdle = true;
						}
					}
				}
			}
			else if( _sltBox != null && !_sltBox.idle()){
				elapsedTimeTBox = System.currentTimeMillis() - _tbox_wrbrf.lastStmtReceived();
				if(_tbox_wrbrf != null && elapsedTime > (CrawlResult.LAST_RECEIVED_TIME_FRAME)){
					if(!doReasoning() || (doReasoning() && _rF.cacheSize() == 0)){
						//ok, reasoner is not doing anything
						if (_sl.idle()){
							//tbox source lookup is idle as well, damn thats a problem, give it another shot
							logger.warning("[STALE] last result "+elapsedTime+" ms; assuming stale tbox source lookup component");

							slIdle = true;
						}
					}
				}
				//	is there anything else running?
			}
		}
		if(sb!=null){
			sb.append("[IDLE] ").append(slIdle).append(" pending tasks sl:").append(_sl.submittedTasks()).append(" last-abx:").append(elapsedTime).append(" last-tbox:").append(elapsedTimeTBox);
			if(_sltBox!=null)
			sb.append(" sl-tbox:").append(_sltBox.submittedTasks());
		if(doReasoning())
			sb.append(" r:").append(_rF.cacheSize()); //+" active:"+_rF.activeCount()
			logger.warning(sb.toString());
		}
		return  slIdle ;
	}

	/**
	 * 
	 */
	public void notifyIdle() {
		for(KeyObserver obs : _obs){
			obs.notifyResult(null);
		}
	}

	/**
	 * @return
	 */
	public WebRepositoryBenchmark getBenchmark() {
		WebRepositoryBenchmark b = new WebRepositoryBenchmark();
		b.putAll(_sl.getBenchmark());
		if(_sltBox!=null){
			b.put(WebRepositoryBenchmark.TBOX_CACHE_SIZE, _tbox_wrbrf.getCount());
			for(Entry<Object,Object> ent:_sltBox.getBenchmark().entrySet() ){
				if(b.containsKey(ent.getKey())){
					Integer i = Integer.valueOf(b.get(ent.getKey()).toString());
					b.put(ent.getKey(), ""+(i+Integer.valueOf(ent.getValue().toString())));
				}else{
					b.put(ent.getKey(), ent.getValue());
				}
			}
		}
		b.getKeyOrder().putAll(_sl.getBenchmark().getKeyOrder());

		b.put(WebRepositoryBenchmark.SEED_URIS, _seedURIs);
		b.put(WebRepositoryBenchmark.FOLLOW_SEEALSO, _followSeeAlso);
		b.put(WebRepositoryBenchmark.FOLLOW_SAMEAS, _followOwlSameAs);
		b.put(WebRepositoryBenchmark.SOURCE_SEL, _srcSel.getType());
		b.put(WebRepositoryBenchmark.CACHE_SIZE, _tS.size());
		b.put(WebRepositoryBenchmark.RETRIEVED, _wrbrf.getCount());
		b.put(WebRepositoryBenchmark.REGISTERED_KEYS, _pI.size());
		b.put(WebRepositoryBenchmark.RETURNED_STMTS, _returnedStmts);

		if(doReasoning()){
			b.putAll(_rF.getBenchmark());
			b.getKeyOrder().putAll(_rF.getBenchmark().getKeyOrder());
		}
		return b;
	}

	/**
	 * @param benchmark
	 */
	public void dumpCache(File cacheFile) {
		logger.info("[DUMP] cache to "+cacheFile);
		try {
			_tS.dumpCache(cacheFile);
		} catch (Exception e) {
			logger.warning("[DUMP] "+e.getClass().getSimpleName()+" msg:"+e.getMessage());
			e.printStackTrace();
		}
	}

	public void close(){
		boolean sl = _sl.shutdown();
		logger.warning("[CLOSED] ABox source-lookup: "+sl);
		if(_sltBox != null){
			boolean slt = _sltBox.shutdown();
			logger.warning("[CLOSED] TBox source-lookup: "+slt);
		}
		if(_rF != null){
			_rF.shutdown();
		}
	}

	/**
	 * @param string 
	 * @param b
	 */
	public void writeAccessLog(File dir, String prefix) {
		if(dir == null || prefix == null) 
			logger.info("cannot write access log to non existing file");

		_sl.writeAccessLog(new File(dir,prefix+"-abox.access.log"));
		if(_sltBox != null){
			_sltBox.writeAccessLog(new File(dir,prefix+"-tbox.access.log"));
		}
	}

	public void followSeeAlso(boolean enable) {
		_followSeeAlso = enable;
		if(enable){
		_lFSeeAlso = new RDFSSeeAlsoLinkFilter(_sl);
			logger.info("Added seeAlso link filter, enable "+_followSeeAlso);
			_lF.addFilter(_lFSeeAlso);
		}
	}


	private void followOwlSameAs(boolean enable) {
		_followOwlSameAs = enable;
		if(enable){
			_lFSameAs = new OWLSameAsLinkFilter(_sl);
			_lF.addFilter(_lFSameAs);
		}
	}


	/**
	 * @param tbox
	 * @throws Exception 
	 */
	public void enableReasoning(REASONING_MODE rMode) throws Exception{
		if(rMode == null) _rMode = REASONING_MODE.OFF;

		if(rMode!= null && _rMode.name().equals(rMode.name()) && _rF!=null) return;

		_rMode =rMode;
		// if no reasong, we are done
		if(rMode == null || rMode.name().equals(REASONING_MODE.OFF.name())) return;
		
		// if owl or all reasoning, enable following same as
		if(rMode.name().startsWith(REASONING_MODE.ALL.name()) || rMode.name().equals(REASONING_MODE.OWL.name()))
			followOwlSameAs(true);
		
		//if dyn reasoning
		if(rMode.name().contains("DYN")){
			_sltBox = _slm.getSourceLookup();
			_sltBox.isTbox(true);
			_tbox_wrbrf = new WebRepositoryBeforeReasoningFiller(this);
			_sltBox.setCallback(_tbox_wrbrf);

			TBoxLinkFilter tboxLF = new TBoxLinkFilter(_sltBox);
			if(rMode.name().contains("CLOSURE"))
				_sltBox.setLinkHandler(tboxLF);

			_lF.addFilter(tboxLF);
		}
		_rF = new ReasonerFramework(this,getIndexCallback(),_tS,rMode);
		_rF.start();
	}

	/**
	 * @return
	 */
	public boolean doReasoning() {
		return _rF != null;
	}
	/**
	 * @return
	 */
	public ReasonerFramework getReasonerFramework() {
		return _rF;
	}

	/**
	 * @param predicateSet
	 */
	public void setContentFilter(HashSet<Resource> predicateSet) {
		_predFilter = predicateSet;
	}

	/**
	 * 
	 */
	public void enableANY23Parsing() {
		_sl.enableANY23Parsing();
	}

	public void setCrawlTaskFactory(CrawlTaskFactory ctf){
		_slm.setCrawlTaskFactory(ctf);
	}

	public Enum<REASONING_MODE> getResonerMode() {
		return _rMode;
	}

	public HashSet<Resource> getPredFilter() {
		return _predFilter;
	}

	public int retrievedStmts() {
		if(_wrbrf==null) return 0;
		return _wrbrf.getCount();
	}

	
	public void interrupt() {
		Node [] n = {WebRepository.INTERRUPT};
		_sl.interrupt();
		if(_rF != null)
			_rF.interruptFramework();
		for(KeyObserver obs : _obs){
			obs.notifyResult(n);
		}
		
	}

	
}
