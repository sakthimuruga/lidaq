/**
 *
 */
package ie.deri.urq.lidaq.repos;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Feb 14, 2011
 */
public abstract class KeyObserver<T> implements Comparable<KeyObserver<T>>{

//	private final String[] _key;
//	private final Node[] _keyAsNode;
//
//	private Set<String> _sources;
//
//	/**
//	 * @param key
//	 * @param sources
//	 */
//	public KeyObserver(Node[] key, HashSet<String> sources) {
//		if(sources == null)
//			throw new IllegalArgumentException("source set is null");
//		_key = parseKeyAsString(key);
//		_keyAsNode = key;
//		_sources = sources;
//	}
//	
//	/**
//	 * @param key
//	 * @return
//	 */
//	private String[] parseKeyAsString(Node[] key) {
//		String [] res = new String[key.length];
//		for(int i = 0; i < res.length;i++){
//			if(key[i] instanceof Resource)
//				res[i]=key[i].toString();
//			else
//				res[i]=key[i].toN3();
//		}
//		return null;
//	}
//
//	/**
//	 * @param key - lookup key 
//	 * @param sources - a possibly empty set of sources
//	 */
//	public KeyObserver(String [] key, Set<String> sources) {
//		if(sources == null)
//			throw new IllegalArgumentException("source set is null");
//		_key = key;
//		_keyAsNode = parseToNodes(key);
//		_sources = sources;
//	}
	
//	@Override
//	public void update(Observable o, Object arg) {
//		//to string
//		String [] update = null;
//		if(arg != null){
//			Node[] statement = ((Node[])arg);
//			update = new String[statement.length];
//			
//			for(int pos = 0; pos < statement.length; pos++){
//				update[pos] = statement[pos].toString();
//			}
//		}
//		receiveStatement(update);
//	}

	
	public abstract String getID();
	

	/**
	 * 
	 * @param statement - a String array of length 4, 
	 * statement[0] = subject
	 * statement[1] = predicate
	 * statement[2] = object
	 * statement[3] = source
	 */
	protected abstract void receiveStatement(T [] statement);
	
	protected abstract T[] convertTo(Node[] n);
	protected abstract Node[] convertFrom(T[] t);
//	protected abstract void receiveStatement(Node [] statement);

//	/**
//	 * @return
//	 */
//	public String[] getKey() {
//		return _key;
//	}
//	
//	public Node[] getKeyAsNode(){
//		return _keyAsNode;
//	}
	
//	/**
//	 * @param key
//	 * @return
//	 */
//	public static Node[] parseToNodes(String[] key) {
//		Node [] res = new Node[4];
//		for(int pos =0; pos < key.length; pos++){
//			if(key[pos].startsWith("\""))
//				try {
//					res[pos]= NxParser.parseLiteral(key[pos]);
//				} catch (ParseException e) {
//					res[pos]= new Literal(key[pos]);
//				}
//			else if(key[pos].startsWith("_:"))
//				try {
//					res[pos]= NxParser.parseBNode(key[pos]);
//				} catch (ParseException e) {
//					res[pos]= new BNode(key[pos]);
//				}
//			else if(key[pos].startsWith("?"))
//				try {
//					res[pos]= NxParser.parseVariable(key[pos]);
//				} catch (ParseException e) {
//					res[pos]= new Variable(key[pos]);
//				}
//			else
//				try {
//					res[pos]= NxParser.parseResource(key[pos]);
//				} catch (ParseException e) {
//					res[pos]= new Resource(key[pos]);
//				}
//		}
//		if(key.length==3)
//			res[3] = new Variable("c");
//		return res;
//	}
//
//	/**
//	 * @return
//	 */
//	public Set<String> getSources() {
//		return _sources;
//	}

	/**
	 * 
	 * @param statement - a String array of length 4, 
	 * statement[0] = subject
	 * statement[1] = predicate
	 * statement[2] = object
	 * statement[3] = source
	 */
	 public void notifyResult(Node[] stmt){
		 receiveStatement(convertTo(stmt));
	 }
}