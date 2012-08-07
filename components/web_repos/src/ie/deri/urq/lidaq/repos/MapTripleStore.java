package ie.deri.urq.lidaq.repos;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.semanticweb.saorr.Statement;
import org.semanticweb.saorr.auth.AuthoritativeResource;
import org.semanticweb.saorr.index.RecursionCache;
import org.semanticweb.saorr.index.StatementStore;
import org.semanticweb.saorr.rules.RuleVariable;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.util.LRUMapCache;

/**
 * A triple store based on a map structure. You can pass any
 * map you like; for small jobs you can use a simple in memory 
 * map (default) and for larger jobs you can use on-disk maps
 * or even distributed maps. In fact, using a shared distributed 
 * map, distributed reasoning is possible. Specifically designed 
 * to use one map, so code is a bit awkward... but works.
 * 
 * @todo make indexing patterns more efficient: i.e., avoid the POS 
 * pattern as much as possible given rdf:type and predicate statements.
 * 
 * @author Aidan Hogan
 * @date 2009-10-01
 */
public class MapTripleStore implements StatementStore, Serializable{

	private static final long serialVersionUID = -2499725921062069882L;

	//looks tempting, but don't play around with this AFAIR
	private static final int[][] INDEX_ORDERS = { 
		{0,1,2}, 
		{1,2,0}, 
		{2,0,1} };

	private static final Statement[][] PATTERNS_SERVICED = {
		{ new Statement(new BNode("s"),new Variable("p"),new Variable("o")), new Statement(new BNode("s"),new BNode("p"),new Variable("o")), new Statement(new BNode("s"),new BNode("p"),new BNode("o")) },
		{ new Statement(new Variable("s"),new BNode("p"),new Variable("o")), new Statement(new Variable("s"),new BNode("p"),new BNode("o")) },
		{ new Statement(new Variable("s"),new Variable("p"),new BNode("o")), new Statement(new BNode("s"),new Variable("p"),new BNode("o")) }
	};

	private static final int DEFAULT_HM_INIT_CAP = 2;
	private static final float DEFAULT_HM_LOAD_FACTOR = 0.75f;

	private static final int DEFAULT_HS_INIT_CAP = 2;
	private static final float DEFAULT_HS_LOAD_FACTOR = 0.75f;

	private static final int NIP_CACHE_SIZE = 1000;

	//checked if map stores authoritative nodes
	private boolean _authStored = false;

		private Map<NodeIndexPair, Map<Node, Set<Node>>> _index;

	private int _size = 0;

	private LRUMapCache<NodeIndexPair,NodeIndexPair> _nipFlyweight;

	protected RecursionCache<Statement> _rc;

	public int size(){
		return _size;
	}

	/**
	 * Constructor
	 * @param index A map, if you can manage with the correct generics ;).
	 * Map should have a clear() method which empties (resets) the Map.
	 */
	public MapTripleStore(Map<NodeIndexPair,Map<Node, Set<Node>>> index){
		_index = index;
		_rc = new RecursionCache<Statement>();
		_nipFlyweight = new LRUMapCache<NodeIndexPair,NodeIndexPair>(NIP_CACHE_SIZE);
	}

	/**
	 * Constructor. By default uses an in-memory HashMap.
	 */
	public MapTripleStore(){
		this(new ConcurrentHashMap<NodeIndexPair, Map<Node, Set<Node>>>());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.saorr.index.StatementStore#contains(org.semanticweb.saorr.Statement)
	 */
	public boolean contains(Statement s) {
		int[] index = INDEX_ORDERS[getServicingIndex(s)];
		Node[] tpa = s.toNodeArray();

		if(tpa[index[0]] instanceof Variable){
			if(_index.size()>0)
				return true;
			return false;
		}
		NodeIndexPair nip = new NodeIndexPair(tpa[index[0]], index[0]);
		Map<Node, Set<Node>> map ;
		synchronized (_index) {
			map = _index.get(nip);
			synchronized (map) {
				//ensure we return AuthoritativeResource for plain ol' Resource lookups
				if(tpa[index[0]] instanceof Resource && _authStored){
					boolean auth = false;
					if(tpa[index[0]] instanceof AuthoritativeResource){
						AuthoritativeResource ar = (AuthoritativeResource)tpa[index[0]];
						auth = ar.isAuthoritative();
					}
					if(!auth){
						AuthoritativeResource ar = new AuthoritativeResource((Resource)tpa[index[0]], true);
						NodeIndexPair nip2 = new NodeIndexPair(ar, index[0]);

						Map<Node, Set<Node>> map2 = _index.get(nip2);

						if(map!=map2 && map2!=null && !map2.isEmpty()){
							if(map==null || map.isEmpty()){
								map = new ConcurrentHashMap<Node, Set<Node>>();
								_index.put(nip, map);
							}else{
								synchronized (map2) {
									for(Map.Entry<Node,Set<Node>> n:map2.entrySet()){
										Set<Node> ts = map.get(n.getKey());
										if(ts ==null){
											ts = Collections.newSetFromMap(new ConcurrentHashMap<Node, Boolean>());
											map.put(n.getKey(), ts);
										} 
										ts.addAll(n.getValue());
									}
								}
							}
						}
					}
				}

				if(map==null || map.isEmpty())
					return false;
				if(tpa[index[1]] instanceof Variable)
					return true;

				Set<Node> ts = map.get(tpa[index[1]]);

				//ensure we return AuthoritativeResource for plain ol' Resource lookups
				if(tpa[index[1]] instanceof Resource && _authStored){
					boolean auth = false;
					if(tpa[index[1]] instanceof AuthoritativeResource){
						AuthoritativeResource ar = (AuthoritativeResource)tpa[index[1]];
						auth = ar.isAuthoritative();
					}
					if(!auth){
						AuthoritativeResource ar = new AuthoritativeResource((Resource)tpa[index[1]], true);
						Set<Node> ts2 = map.get(ar);
						if(ts!=ts2 && ts2!=null && !ts2.isEmpty()){
							if(ts==null){
								ts =Collections.newSetFromMap(new ConcurrentHashMap<Node, Boolean>());
								map.put(tpa[index[1]], ts);
							}
							ts.addAll(ts2);
						}
					}
				}
				if(ts==null || ts.isEmpty())
					return false;
				if(tpa[index[2]] instanceof Variable)
					return true;

				//ensure we return AuthoritativeResource for plain ol' Resource lookups
				if(tpa[index[2]] instanceof Resource && _authStored){
					boolean auth = false;
					if(tpa[index[2]] instanceof AuthoritativeResource){
						AuthoritativeResource ar = (AuthoritativeResource)tpa[index[2]];
						auth = ar.isAuthoritative();
					}
					if(!auth){
						AuthoritativeResource ar = new AuthoritativeResource((Resource)tpa[index[2]], true);
						if(ts.contains(ar)){
							return true;
						}
					} 
				}
				return ts.contains(tpa[index[2]]);
			}//synch index
		}//synch index
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.saorr.index.StatementStore#insert(org.semanticweb.saorr.Statement)
	 */
	public boolean indexCurrentStatement() {
		Statement s = getRecursionCache().getCurrentStatement();
		if(s==null)
			return false;

		boolean unique = false;
		boolean fresh = getRecursionCache().notIndexed();

		HashSet<Integer> indices = new HashSet<Integer>();
		for(int i=0; i<INDEX_ORDERS.length; i++){
			boolean todo = false;
			for(Statement ip:PATTERNS_SERVICED[i]){
				todo|=getRecursionCache().addPattern(ip);
			}
			if(todo)
				indices.add(i);
		}

		for(int ind:indices){
			unique |= insert(s, INDEX_ORDERS[ind]);
		}
		if(unique) _size++;
		else if(fresh) getRecursionCache().setSeenCurrentStatement();
		return unique;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.saorr.index.StatementStore#insert(org.semanticweb.saorr.Statement, java.util.Set)
	 */
	public boolean indexCurrentStatement(Set<Statement> tp) {
		Statement s = getRecursionCache().getCurrentStatement();

		if(s==null)
			return false;

		boolean unique = false;
		boolean fresh = getRecursionCache().notIndexed();

		HashSet<Integer> indices = new HashSet<Integer>();
		for(Statement t:tp){
			int index = getServicingIndex(t);
			boolean todo = false;
			for(Statement ip:PATTERNS_SERVICED[index]){
				todo|=getRecursionCache().addPattern(ip);
			}
			if(todo)
				indices.add(index);
		}

		for(int ind:indices){
			unique |= insert(s, INDEX_ORDERS[ind]);
		}
		if(unique) _size++;
		else if(fresh) getRecursionCache().setSeenCurrentStatement();
		return unique;
	}

	private boolean insert(Statement s, int[] order){
		//		if(_size>1000 && !_authStored){
		//			System.err.println("Inserting into A-Box "+s);
		//		}
		Node[] na = s.toNodeArray();
		for(Node n:na){
			if(n instanceof AuthoritativeResource){
				_authStored |= ((AuthoritativeResource)n).isAuthoritative();
			}
		}
		NodeIndexPair nip = flyweight(new NodeIndexPair(na[order[0]], order[0]));
		Map<Node,Set<Node>> edges = getOrCreateMap(nip);
		Set<Node> vals = getOrCreateHashSet(edges, na[order[1]]);
		return vals.add(na[order[2]]);
	}

	private Map<Node,Set<Node>> getOrCreateMap(final NodeIndexPair key){
		Map<Node,Set<Node>> edges = _index.get(key);
		if(edges==null){
			edges = new ConcurrentHashMap<Node,Set<Node>>(DEFAULT_HM_INIT_CAP, DEFAULT_HM_LOAD_FACTOR);
			synchronized (_index) {
				_index.put(key, edges);
			}
		}
		return edges;
	}

	private Set<Node> getOrCreateHashSet(final Map<Node,Set<Node>> map, final Node key){
		Set<Node> vals = map.get(key);
		if(vals==null){
			vals = new HashSet<Node>(DEFAULT_HS_INIT_CAP, DEFAULT_HS_LOAD_FACTOR);
			map.put(key, vals);
		}
		return vals;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.saorr.index.StatementStore#retrieve(org.semanticweb.saorr.Statement)
	 */
	public Set<Nodes> retrieve(Statement s) {
		int[] index = INDEX_ORDERS[getServicingIndex(s)];
		Node[] tpa = s.toNodeTriple();

		if(tpa[index[0]] instanceof Variable){
			throw new UnsupportedOperationException("Index does not support open pattern query");
		}

		NodeIndexPair nip = new NodeIndexPair(tpa[index[0]], index[0]);
		synchronized (_index) {
			Map<Node, Set<Node>> map = _index.get(nip);
			synchronized (map) {
				//ensure we return AuthoritativeResource for plain ol' Resource lookups
				if(tpa[index[0]] instanceof Resource && _authStored){
					boolean auth = false;
					if(tpa[index[0]] instanceof AuthoritativeResource){
						AuthoritativeResource ar = (AuthoritativeResource)tpa[index[0]];
						auth = ar.isAuthoritative();
					}
					if(!auth){
						AuthoritativeResource ar = new AuthoritativeResource((Resource)tpa[index[0]], true);
						NodeIndexPair nip2 = new NodeIndexPair(ar, index[0]);
						Map<Node, Set<Node>> map2 = _index.get(nip2);
						synchronized (map2) {
							if(map!=map2 && map2!=null && !map2.isEmpty()){
								if(map==null || map.isEmpty()){
									map = new ConcurrentHashMap<Node, Set<Node>>();
									_index.put(nip, map);
								}else{
									for(Map.Entry<Node,Set<Node>> n:map2.entrySet()){
										Set<Node> ts = map.get(n.getKey());
										if(ts ==null){
											ts = new HashSet<Node>(DEFAULT_HS_INIT_CAP, DEFAULT_HS_LOAD_FACTOR);
											map.put(n.getKey(), ts);
										} 
										ts.addAll(n.getValue());
									}
								}
							}
						}
					}
				}


				if(map==null || map.isEmpty())
					return null;
				if(tpa[index[1]] instanceof Variable){
					return fillResults(map, index[1]>index[2]);
				}

				Set<Node> ts = map.get(tpa[index[1]]);

				//ensure we return AuthoritativeResource for plain ol' Resource lookups
				if(tpa[index[1]] instanceof Resource && _authStored){
					boolean auth = false;
					if(tpa[index[1]] instanceof AuthoritativeResource){
						AuthoritativeResource ar = (AuthoritativeResource)tpa[index[1]];
						auth = ar.isAuthoritative();
					}
					if(!auth){
						AuthoritativeResource ar = new AuthoritativeResource((Resource)tpa[index[1]], true);
						Set<Node> ts2 = map.get(ar);
						if(ts!=ts2 && ts2!=null && !ts2.isEmpty()){
							if(ts==null){
								ts = new HashSet<Node>(DEFAULT_HS_INIT_CAP, DEFAULT_HS_LOAD_FACTOR);
								map.put(tpa[index[1]], ts);
							}
							ts.addAll(ts2);
						}
					}
				}

				if(ts==null || ts.isEmpty())
					return null;
				if(tpa[index[2]] instanceof Variable){
					return fillResults(ts);
				}

				//ensure we return AuthoritativeResource for plain ol' Resource lookups
				if(tpa[index[2]] instanceof Resource && _authStored){
					boolean auth = false;
					if(tpa[index[2]] instanceof AuthoritativeResource){
						AuthoritativeResource ar = (AuthoritativeResource)tpa[index[2]];
						auth = ar.isAuthoritative();
					}
					if(!auth){
						AuthoritativeResource ar = new AuthoritativeResource((Resource)tpa[index[2]], true);
						if(ts.contains(ar)){
							return new HashSet<Nodes>(DEFAULT_HS_INIT_CAP, DEFAULT_HS_LOAD_FACTOR);
						}
					} 
				} 

				if(ts.contains(tpa[index[2]]))
					return new HashSet<Nodes>(DEFAULT_HS_INIT_CAP, DEFAULT_HS_LOAD_FACTOR);
			}
		}
		return null;
	}

	private Set<Nodes> fillResults(Map<Node, Set<Node>> map, boolean reverse) {
		Set<Nodes> results =  new HashSet<Nodes>(DEFAULT_HS_INIT_CAP, DEFAULT_HS_LOAD_FACTOR);
		for(Entry<Node, Set<Node>> e:map.entrySet()){
			fillResults(e.getKey(), e.getValue(), results, reverse);
		}
		return results;
	}

	private Set<Nodes> fillResults(Node key, Set<Node> vals){
		HashSet<Nodes> results = new HashSet<Nodes>(DEFAULT_HS_INIT_CAP, DEFAULT_HS_LOAD_FACTOR);
		fillResults(key, vals, results, false);
		return results;
	}

	private Set<Nodes> fillResults(Set<Node> vals){
		HashSet<Nodes> results = new HashSet<Nodes>(DEFAULT_HS_INIT_CAP, DEFAULT_HS_LOAD_FACTOR);
		for(Node n:vals){
			results.add(new Nodes(n));
		}
		return results;
	}

	private void fillResults(Node key, Set<Node> vals, Set<Nodes> results, boolean reverse){
		for(Node val:vals){
			if(!reverse)
				results.add(new Nodes(key, val));
			else results.add(new Nodes(val, key));
		}
	}

	private int getServicingIndex(Statement s){
		Node[] tpa = s.toNodeArray();
		boolean[] openVar = new boolean[tpa.length];
		for(int i=0; i<tpa.length; i++){
			Node n = tpa[i];
			if(n instanceof Variable){
				if(n instanceof RuleVariable){
					RuleVariable rv = (RuleVariable)n;
					if(rv.isJoin()){
						openVar[i] = false;
					} else{
						openVar[i] = true;
					}
				} else{
					openVar[i] = true;
				}
			} else{
				openVar[i] = false;
			}
		}

		for(int i=0; i<INDEX_ORDERS.length; i++){
			int[] order = INDEX_ORDERS[i];
			boolean var = false, ans = true;
			for(int j=0; j<order.length; j++){
				if(openVar[order[j]]){
					var = true;
				} else if(var){
					ans = false;
					break;
				}
			}

			if(ans){
				return i;
			}
		}

		throw new RuntimeException("Cannot find appropriate index for pattern "+s);
	}

	public void finishedStatement() {
		getRecursionCache().finishedStatement();
	}

	public boolean seenCurrentStatement() {
		return getRecursionCache().seenCurrentStatement();
	}

	public void setCurrentStatement(Statement s) {
		getRecursionCache().setCurrentStatement(s);
	}

	protected static class NodeIndexPair implements Comparable<NodeIndexPair>, Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Node _n;
		int _i;

		public NodeIndexPair(Node n, int pos){
			_n = n;
			_i = pos;
		}

		public Node getNode(){
			return _n;
		}

		public int getPosition(){
			return _i;
		}

		public int hashCode(){
			return _n.hashCode()-_i;
		}

		public boolean equals(Object o) {
			if(o==this)
				return true;
			if(o instanceof NodeIndexPair){
				NodeIndexPair pn = (NodeIndexPair)o;
				boolean e = _n.equals(pn._n);
				if(e) return _i == pn._i;
				return false;
			}
			return false;
		}

		public int compareTo(NodeIndexPair o) {
			if(o==this)
				return 0;
			int comp = _n.compareTo(o._n);
			if(comp==0) return _i - o._i;
			else return comp;
		}

		public String toString(){
			return _n.toN3()+"~"+_i;
		}
	}

	public void clear() {
		_size = 0;
		_authStored = false;
		_rc = new RecursionCache<Statement>();
		_index.clear();
	}

	protected synchronized RecursionCache<Statement> getRecursionCache(){
		return _rc;
	}

	private NodeIndexPair flyweight(NodeIndexPair nip){
		NodeIndexPair fwnip = _nipFlyweight.get(nip);
		if(fwnip == null){
			fwnip = nip;
			_nipFlyweight.put(fwnip, fwnip);
		}
		return fwnip;
	}

}
