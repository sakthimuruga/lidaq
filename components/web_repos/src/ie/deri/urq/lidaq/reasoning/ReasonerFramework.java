package ie.deri.urq.lidaq.reasoning;
/**
 *
 */



import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.benchmark.Benchmark;
import ie.deri.urq.lidaq.benchmark.ReasonerBenchmark;
import ie.deri.urq.lidaq.repos.WebRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.semanticweb.saorr.engine.Reasoner;
import org.semanticweb.saorr.engine.ReasonerEnvironment;
import org.semanticweb.saorr.engine.ReasonerSettings;
import org.semanticweb.saorr.engine.input.NxaGzInput;
import org.semanticweb.saorr.engine.unique.UniqueTripleFilter;
import org.semanticweb.saorr.engine.unique.UniquingHashset;
import org.semanticweb.saorr.fragments.owl2rl.OWL2RL_T_SPLIT;
import org.semanticweb.saorr.fragments.rdfs.RDFS_Fragment;
import org.semanticweb.saorr.fragments.rdfs.RDFS_T_SPLIT;
import org.semanticweb.saorr.index.StatementStore;
import org.semanticweb.saorr.rules.LinkedRuleIndex;
import org.semanticweb.saorr.rules.Rule;
import org.semanticweb.saorr.rules.Rules;
import org.semanticweb.saorr.rules.SortedRuleSet;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.CallbackCount;
import org.semanticweb.yars.util.Callbacks;
import org.semanticweb.yars.util.ResetableIterator;


/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 14, 2010
 */
public class ReasonerFramework extends Thread implements Serializable, Callback{
	private static final Logger logger = Logger.getLogger(ReasonerFramework.class.getName());
	private static final long serialVersionUID = 1L;

	private static LinkedRuleIndex<Rule> TBOX_RDFS = null;
	private static LinkedRuleIndex<Rule> TBOX_RDFSSAMEAS = null;

	private final Iterator<Node[]> _iter;
	private final NodeArrayBlockingQueue _q;
	private org.semanticweb.saorr.engine.Reasoner r;
	
	private final StatementStore _store;

	private boolean done = false;

	private final Callbacks _cbs;
	private final REASONING_MODE _rMode;
	final private ReasonerBenchmark b;
	final private CallbackCount _cc;

	public ReasonerFramework(WebRepository webRepository, Callback indexer, StatementStore store, REASONING_MODE rMode) throws Exception {
		_store = store;
		_rMode = rMode;
		
		_cc = new CallbackCount();
		_cbs = new Callbacks(_cc,indexer);
		
		_q = new NodeArrayBlockingQueue(500000);
		if(webRepository!=null)
			_iter = new ie.deri.urq.lidaq.reasoning.BlockingQueueIterator(_q, webRepository.getSourceLookup());
		else
			_iter = new ie.deri.urq.lidaq.reasoning.BlockingQueueIterator(_q);
		init(null);
		
		b = new ReasonerBenchmark();
	}

	private void init(LinkedRuleIndex<Rule> tbox) throws Exception{
		logger.info("[INIT] store "+_store+" rMode "+_rMode);
		ReasonerSettings rs = new ReasonerSettings();
		
		ReasonerEnvironment re = new ReasonerEnvironment(_iter,_cbs);
		
		
		if(tbox != null){
			rs.setUseAboxRuleIndex(true);
			re.setAboxRuleIndex(tbox);
		}
		else if(_rMode.name().equals(REASONING_MODE.ALL.name())){
			rs.setUseAboxRuleIndex(true);
			re.setAboxRuleIndex(deserialiseRDFSSAMEASTBox());
		}
		else if(_rMode.name().equals(REASONING_MODE.RDFS.name())){
			rs.setUseAboxRuleIndex(true);
			re.setAboxRuleIndex(deserialiseRDFSTBox());
		}
		else if(_rMode.name().equals(REASONING_MODE.OWL.name())){
			rs.setUseAboxRuleIndex(false);
			SortedRuleSet<Rule> all =
				Rules.toSet(SAMEAS_RULES);
			rs.setFragment(all);
		}
		else if(_rMode.name().equals(REASONING_MODE.RDFS_DYN_CLOSURE.name())||_rMode.name().equals(REASONING_MODE.RDFS_DYN_DIR.name())){
			rs.setUseAboxRuleIndex(false);
			SortedRuleSet<Rule> all =
				Rules.toSet(RDFS_RULES_DYNAMIC);
			rs.setFragment(all);
		}
		else if(_rMode.name().equals(REASONING_MODE.ALL_DYN_CLOSURE.name())||_rMode.name().equals(REASONING_MODE.ALL_DYN_DIR.name())){
			rs.setUseAboxRuleIndex(false);
			SortedRuleSet<Rule> all =
				Rules.toSet(RDFSSAMEAS_RULES_DYNAMIC);
			rs.setFragment(all);
		}
		
		
		
//		re.setUniqueStatementFilter(new UniqueTripleFilter(new UniquingHashset(100000)));
		re.setUniqueStatementFilter(new UniqueQuadFilter(new UniquingHashset(100000)));
		//can use rs.setAbox(...) if needed
		re.setABox(_store);
		
		
		rs.setSkipTBox(true);
		rs.setSkipAxiomatic(true);
		rs.setPrintContexts(true); //set false for just triples
		r = new Reasoner(rs, re);
		
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		long start= System.currentTimeMillis();
		try{
			logger.info(" [START]");
			r.reason();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		done= true;
		logger.info(" [STOP] "+(System.currentTimeMillis()-start)+" ms");
	}

	/**
	 * @return
	 */
	public int cacheSize() {
		if(_q.size() != 0 && done) return 0;
		return _q.size();
	}

	public static void serialiseTBox(File tboxFile,LinkedRuleIndex<Rule> tbox) throws Exception {
		ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(tboxFile)));
		oos.writeObject(tbox);
		oos.close();
	}

	public void setTBox(LinkedRuleIndex<Rule> tbox ) throws Exception{
		init(tbox);
	}
	

	public final static Rule[] RDFS_RULES_DYNAMIC = { RDFS_Fragment.RDFS2, RDFS_Fragment.RDFS3,
		RDFS_Fragment.RDFS7, RDFS_Fragment.RDFS9};

	public final static Rule[] RDFS_RULES = { RDFS_T_SPLIT.RDFS2, RDFS_T_SPLIT.RDFS3,
		RDFS_T_SPLIT.RDFS7, RDFS_T_SPLIT.RDFS9};

	public final static Rule[] SAMEAS_RULES = {OWL2RL_T_SPLIT.EQ_REP_O,
		OWL2RL_T_SPLIT.EQ_REP_P, OWL2RL_T_SPLIT.EQ_REP_S, OWL2RL_T_SPLIT.EQ_SYM,
		OWL2RL_T_SPLIT.EQ_TRANS};

	public final static Rule [] RDFSSAMEAS_RULES = { 
		RDFS_T_SPLIT.RDFS2, RDFS_T_SPLIT.RDFS3,
		RDFS_T_SPLIT.RDFS7, RDFS_T_SPLIT.RDFS9,OWL2RL_T_SPLIT.EQ_REP_O,
		OWL2RL_T_SPLIT.EQ_REP_P, OWL2RL_T_SPLIT.EQ_REP_S, OWL2RL_T_SPLIT.EQ_SYM,
		OWL2RL_T_SPLIT.EQ_TRANS};//concat rdfs and sameAs

	public final static Rule [] RDFSSAMEAS_RULES_DYNAMIC = { RDFS_Fragment.RDFS2, RDFS_Fragment.RDFS3,
		RDFS_Fragment.RDFS7, RDFS_Fragment.RDFS9,OWL2RL_T_SPLIT.EQ_REP_O,
		OWL2RL_T_SPLIT.EQ_REP_P, OWL2RL_T_SPLIT.EQ_REP_S, OWL2RL_T_SPLIT.EQ_SYM,
		OWL2RL_T_SPLIT.EQ_TRANS};//concat rdfs and sameAs

	public static LinkedRuleIndex<Rule> loadTBox(File tbox) throws Exception {
	if(tbox == null) return null;
		return loadTBox(tbox, RDFSSAMEAS_RULES);
	}

	/**
	 * @param tbox
	 * @return 
	 * @throws Exception 
	 */
	public static LinkedRuleIndex<Rule> loadTBox(File tbox,Rule [] rulez) throws Exception {
		NxaGzInput tboxIn = new  NxaGzInput(tbox,3);

//		System.out.println(Arrays.toString(rulez));
		Rules rules = new Rules(rulez);
		rules.setAuthoritative();

		Callback cnqos = new Callback() {
			public void startDocument() {;}
			public void processStatement(Node[] nx) {;}
			public void endDocument() {;}
		};
		return buildTbox(tboxIn,
				rules, cnqos, true, false);
	}


	private static LinkedRuleIndex<Rule> buildTbox(ResetableIterator<Node[]> tboxin, Rules rules, Callback
			tboxout, boolean auth, boolean sat) throws Exception{
		
		if(auth){
			rules.setAuthoritative();
		}

		SortedRuleSet<Rule> all =
			Rules.toSet(rules.getRulesArray());

		ReasonerSettings rs = new ReasonerSettings();
		rs.setAuthorativeReasoning(auth);
		rs.setFragment(all);
		rs.setMergeRules(true);
		rs.setPrintContexts(true);
		rs.setSaturateRules(sat);
		rs.setSkipABox(true);
		rs.setSkipAxiomatic(true);
		rs.setSkipTBox(false);
		rs.setTBoxRecursion(false);
		rs.setTemplateRules(true);
		rs.setUseAboxRuleIndex(true);
		ReasonerEnvironment re = new ReasonerEnvironment(null,
				tboxin, tboxout);
		Reasoner r = new Reasoner(rs, re);
		r.reason();

		return re.getAboxRuleIndex();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.yars.nx.parser.Callback#startDocument()
	 */
	@Override
	public void startDocument() {;}

	/* (non-Javadoc)
	 * @see org.semanticweb.yars.nx.parser.Callback#endDocument()
	 */
	@Override
	public void endDocument() {;}

	/* (non-Javadoc)
	 * @see org.semanticweb.yars.nx.parser.Callback#processStatement(org.semanticweb.yars.nx.Node[])
	 */
	@Override
	public void processStatement(Node[] nx) {
		_q.add(nx);
	}

	public static LinkedRuleIndex<Rule> deserialiseTBox(File tbox) throws Exception {
		return deserialiseTBox(new FileInputStream(tbox));
	}

	public static LinkedRuleIndex<Rule> deserialiseTBox(InputStream in) throws Exception {
		logger.info("[TBOX] loading");
		ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(in));
		LinkedRuleIndex<Rule> tmplRules = (LinkedRuleIndex<Rule>) ois.readObject();
		ois.close();
		logger.info("[TBOX] loaded "+tmplRules);
		return tmplRules;
	}


	/**
	 * @return
	 * @throws Exception 
	 */
	public static LinkedRuleIndex<Rule> deserialiseRDFSTBox() throws Exception {
		if(TBOX_RDFS== null){
			TBOX_RDFS=deserialiseTBox(ReasonerFramework.class.getResourceAsStream("/resources/tbox_rdfs.gz.ser"));
		}
		return TBOX_RDFS;
	}


	/**
	 * @return
	 */
	public static LinkedRuleIndex<Rule> deserialiseRDFSSAMEASTBox()  throws Exception {

		if(TBOX_RDFSSAMEAS == null) 
			TBOX_RDFSSAMEAS= deserialiseTBox(ReasonerFramework.class.getResourceAsStream("/resources/tbox_rdfssameAs.gz.ser"));
		//			TBOX_RDFSSAMEAS= deserialiseTBox(new File("/Users/juum/Documents/Code/java/lidaq/components/web_repos/resources/tbox_rdfssameAs.gz.ser"));
		return TBOX_RDFSSAMEAS;
	}

	/**
	 * @return
	 */
	public Benchmark getBenchmark() {
		b.put(ReasonerBenchmark.REASONING_MODE,_rMode.name());
		b.put(ReasonerBenchmark.INFERED_STMTS,_cc.getStmts());
		
		return b;
	}
	
	public static void main(String[] args) throws Exception {
		File tboxFile = new File(args[0]);
		File outtboxFile = new File(args[1]);
		logger.info("loading tbox "+tboxFile);
		LinkedRuleIndex<org.semanticweb.saorr.rules.Rule> tbox = ReasonerFramework.loadTBox(tboxFile, ReasonerFramework.RDFS_RULES);
		logger.info("Serialising tbox to "+outtboxFile);
//		ReasonerFramework.deserialiseTBox(new File("/Users/juum/Documents/Code/java/lidaq/components/web_repos/src/resources/tbox_rdfs1.gz.ser"));
		ReasonerFramework.serialiseTBox(outtboxFile,tbox);
		logger.info("Loaded TBox with "+tbox.getAllLinkedRules().size() +" rules");
	}

	public void shutdown() {
		_q.clear();
		logger.info("[CLEAR] [INJECT] [POISON] into reasoner queue");
		processStatement(NodeArrayBlockingQueue.POISON_TOKEN);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			logger.warning("[CLEAR] [INJECT] [POISON] InterruptedException");
		}
		if(!done) ((ie.deri.urq.lidaq.reasoning.BlockingQueueIterator)_iter).shutdown();
		r = null;
	}

	public void interruptFramework() {
		_q.clear();
		
		done=true;
	}
}