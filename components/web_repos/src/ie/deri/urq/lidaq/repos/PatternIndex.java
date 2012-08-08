package ie.deri.urq.lidaq.repos;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.util.LRUMapCache;

/**
 * An index for rules -- given a particular statement, the rule
 * index will return the rules that might be interested in the given
 * statement.
 *
 * The rule index has no real notion of terminological or assertional 
 * patterns, other than which (or both) to index. It's probably wise to 
 * keep two separate indexes where such a distinction is necessary.
 * 
 * @author Aidan Hogan
 *
 */
public class PatternIndex implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5922641019308677111L;
	private static Logger logger = Logger.getLogger(PatternIndex.class.getName());

	protected static int HASHSET_INIT_CAPACITY = 2;
	protected static int CACHE_SIZE = 500;

	private TreeSet<TriplePattern> _empty;

	//indexes for full statements
	private Map<Integer, Set<TriplePattern>> _s;
	private Map<Integer, Set<TriplePattern>> _p;
	private Map<Integer, Set<TriplePattern>> _o;

	private Map<Integer, Set<TriplePattern>> _sp;
	private Map<Integer, Set<TriplePattern>> _po;
	private Map<Integer, Set<TriplePattern>> _so;

	private Map<Integer, Set<TriplePattern>> _spo;

	//indexes for partial consequents
	private Map<Integer, Set<TriplePattern>> _cs;
	private Map<Integer, Set<TriplePattern>> _cp;
	private Map<Integer, Set<TriplePattern>> _co;

	private Map<Integer, Set<TriplePattern>> _csp;
	private Map<Integer, Set<TriplePattern>> _cpo;
	private Map<Integer, Set<TriplePattern>> _cso;

	//	private Map<E,LinkedRule<E>> _intern;

	private int _raw = 0;
	private int _rawLinks = 0;

	private long _rawConsequent;

	private int _merged = 0;

	private long _satConsequent;
	private long _satLinks;


	private transient LRUMapCache<Node[],Set<TriplePattern>> _cache = null;

	/**
	 * Build an index for rule patterns
	 * @param aboxRules rules to index
	 * @param tbox if t-box patterns should be indexed
	 * @param abox if a-box patterns should be indexed
	 */
	public PatternIndex(){
		_empty = new TreeSet<TriplePattern>();

		//indexes for full statements
		_s = new ConcurrentHashMap<Integer, Set<TriplePattern>>();
		_p = new ConcurrentHashMap<Integer, Set<TriplePattern>>();
		_o = new ConcurrentHashMap<Integer, Set<TriplePattern>>();

		_sp = new ConcurrentHashMap<Integer, Set<TriplePattern>>();
		_po = new ConcurrentHashMap<Integer, Set<TriplePattern>>();
		_so = new ConcurrentHashMap<Integer, Set<TriplePattern>>();

		_spo = new ConcurrentHashMap<Integer, Set<TriplePattern>>();

		//indexes for partial consequents
		_cs = new ConcurrentHashMap<Integer, Set<TriplePattern>>();
		_cp = new ConcurrentHashMap<Integer, Set<TriplePattern>>();
		_co = new ConcurrentHashMap<Integer, Set<TriplePattern>>();

		_csp = new ConcurrentHashMap<Integer, Set<TriplePattern>>();
		_cpo = new ConcurrentHashMap<Integer, Set<TriplePattern>>();
		_cso = new ConcurrentHashMap<Integer, Set<TriplePattern>>();

		_cache = new LRUMapCache<Node[],Set<TriplePattern>>(500);
	}


	public void indexPattern(TriplePattern pattern){
		boolean s = !(pattern.subject() instanceof Variable);
		boolean p = !(pattern.predicate() instanceof Variable);
		boolean o = !(pattern.object() instanceof Variable);
		if(!s && !p && !o){
			_empty.add(pattern);
		} else if(!s && !o){
			addPattern(_p, pattern.predicate().hashCode(), pattern);
		} else if(!s && !p){
			addPattern(_o, pattern.object().hashCode(), pattern);
		} else if(!p && !o){
			addPattern(_s, pattern.subject().hashCode(), pattern);
		} else if(!s){
			int poh = Nodes.hashCode(pattern.predicate(), pattern.object());
			addPattern(_po, poh, pattern);
			addPattern(_cp, pattern.predicate().hashCode(), pattern);
			addPattern(_co, pattern.object().hashCode(), pattern);
		} else if(!p){
			int soh = Nodes.hashCode(pattern.subject(), pattern.object());
			addPattern(_so, soh, pattern);
			addPattern(_cs, pattern.subject().hashCode(), pattern);
			addPattern(_co, pattern.object().hashCode(), pattern);
		} else if(!o){
			int sph = Nodes.hashCode(pattern.subject(), pattern.predicate());
			addPattern(_sp, sph, pattern);
			addPattern(_cs, pattern.subject().hashCode(), pattern);
			addPattern(_cp, pattern.predicate().hashCode(), pattern);
		} else{
			int spoh =  Nodes.hashCode(pattern.subject(), pattern.predicate(), pattern.object());
			addPattern(_spo, spoh, pattern);
			int poh = Nodes.hashCode(pattern.predicate(), pattern.object());
			addPattern(_cpo, poh, pattern);
			int soh = Nodes.hashCode(pattern.subject(), pattern.object());
			addPattern(_cso, soh, pattern);
			int sph = Nodes.hashCode(pattern.subject(), pattern.predicate());
			addPattern(_csp, sph, pattern);
			addPattern(_cs, pattern.subject().hashCode(), pattern);
			addPattern(_cp, pattern.predicate().hashCode(), pattern);
			addPattern(_co, pattern.object().hashCode(), pattern);
		}
	}


	/**
	 * If the index has been built/indexed/saturated as
	 * necessary, call this method to set some temporary
	 * data structures to null and free some space!
	 */
	public void freeResources(){
		//		_intern = null;
		_cs = null;
		_cp = null;
		_co = null;
		_csp = null;
		_cpo = null;
		_cso = null;
	}



	public synchronized void addPattern( Map<Integer, Set<TriplePattern>> ruleindex, int key, TriplePattern pattern){
		synchronized (ruleindex) {
			Set<TriplePattern> rules = ruleindex.get(key);
			if(rules==null){
				rules = Collections.newSetFromMap(new ConcurrentHashMap<TriplePattern, Boolean>());
				ruleindex.put(key, rules);
			}
			synchronized (rules) {
				if(rules.contains(pattern)){
					for(TriplePattern in: rules){
						if(in.equals(pattern))
							in.update(pattern);		
					}
				}else
					rules.add(pattern);
			}
		}		
	}

	public int size(){
		return _s.size()+_so.size()+_sp.size()+_spo.size()+_p.size()+_po.size()+_o.size();
	}

	/**
	 * Get the rules that could possibly be interested in a given statement.
	 * Statement can contain variables.
	 * @param stmt
	 * @return
	 */
	public Set<TriplePattern> getRelevantPattern(Node[] stmt){
		boolean s = !(stmt[0] instanceof Variable);
		boolean p = !(stmt[1] instanceof Variable);
		boolean o = !(stmt[2] instanceof Variable);

		if(_cache==null){
			_cache = new LRUMapCache<Node[],Set<TriplePattern>>(500);
		}
		Set<TriplePattern> rules = _cache.get(stmt); 
		if(rules!=null){
			return rules;
		} else{
			rules = Collections.newSetFromMap(new ConcurrentHashMap<TriplePattern, Boolean>());
		}

		Set<TriplePattern> hs = null;
		if(s&p&o){
			int spoh =  Nodes.hashCode(stmt[0], stmt[1], stmt[2]);
			hs = _spo.get(spoh);
			if(hs!=null)
				rules.addAll(hs);
		}
		hs = null;

		if(p&o){
			int poh = Nodes.hashCode(stmt[1], stmt[2]);
			hs = _po.get(poh);
			if(hs!=null)
				rules.addAll(hs);

			hs = null;

			if(!s){
				hs = _cpo.get(poh);
				if(hs!=null)
					rules.addAll(hs);
			}

			hs = null;
		}

		if(s&o){
			int soh = Nodes.hashCode(stmt[0], stmt[2]);
			hs = _so.get(soh);
			if(hs!=null)
				rules.addAll(hs);

			hs = null;

			if(!p){
				hs = _cso.get(soh);
				if(hs!=null)
					rules.addAll(hs);
			}

			hs = null;
		}

		if(s&p){
			int sph = Nodes.hashCode(stmt[0], stmt[1]);
			hs = _sp.get(sph);
			if(hs!=null)
				rules.addAll(hs);

			hs = null;

			if(!o){
				hs = _csp.get(sph);
				if(hs!=null)
					rules.addAll(hs);
			}

			hs = null;
		}

		if(s){
			hs = _s.get(stmt[0].hashCode());
			if(hs!=null)
				rules.addAll(hs);

			hs = null;

			if(!p && !o){
				hs = _cs.get(stmt[0].hashCode());
				if(hs!=null)
					rules.addAll(hs);
			}

			hs = null;
		}

		if(p){
			hs = _p.get(stmt[1].hashCode());
			if(hs!=null)
				rules.addAll(hs);

			hs = null;

			if(!s && !o){
				hs = _cp.get(stmt[1].hashCode());
				if(hs!=null)
					rules.addAll(hs);
			}

			hs = null;
		}

		if(o){
			hs = _o.get(stmt[2].hashCode());
			if(hs!=null)
				rules.addAll(hs);

			hs = null;

			if(!s && !p){
				hs = _co.get(stmt[2].hashCode());
				if(hs!=null)
					rules.addAll(hs);
			}

			hs = null;
		}

		rules.addAll(_empty);

		_cache.put(stmt, rules);

		return rules;
	}

	//	public TreeSet<TriplePattern> getAllPatterns(){
	//		TreeSet<TriplePattern> slrs = new TreeSet<TriplePattern>();
	//		for(Map.Entry<Integer,TreeSet<TriplePattern>> slr:_s.entrySet()){
	//			slrs.addAll(slr.getValue();
	//		}
	//		for(Map.Entry<Integer,TreeSet<TriplePattern>> slr:_p.entrySet()){
	//			slrs.addAll(slr.getValue();
	//		}
	//		for(Map.Entry<Integer,TreeSet<TriplePattern>> slr:_o.entrySet()){
	//			slrs.addAll(slr.getValue();
	//		}
	//		for(Map.Entry<Integer,TreeSet<TriplePattern>> slr:_sp.entrySet()){
	//			slrs.addAll(slr.getValue();
	//		}
	//		for(Map.Entry<Integer,TreeSet<TriplePattern>> slr:_po.entrySet()){
	//			slrs.addAll(slr.getValue();
	//		}
	//		for(Map.Entry<Integer,TreeSet<TriplePattern>> slr:_so.entrySet()){
	//			slrs.addAll(slr.getValue();
	//		}
	//		for(Map.Entry<Integer,TreeSet<TriplePattern>> slr:_spo.entrySet()){
	//			slrs.addAll(slr.getValue();
	//		}
	//		slrs.addAll(_empty);
	//		
	//		return slrs;
	//	}
}
