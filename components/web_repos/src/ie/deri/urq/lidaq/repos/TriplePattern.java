/**
 *
 */
package ie.deri.urq.lidaq.repos;

import ie.deri.urq.lidaq.log.LIDAQLOGGER;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Variable;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 18, 2011
 */
public class TriplePattern  implements Comparable<TriplePattern>{

private static final Logger logger = LIDAQLOGGER.addHandler(Logger.getLogger(TriplePattern.class
		.getName()));
	
	final private TreeSet<String> _srcs = new TreeSet<String>();
	final private TreeSet<KeyObserver> _obs = new TreeSet<KeyObserver>();
	final private Node [] key;
	private List<Variable> _keyVars;
	private int[] _varPos;

	/**
	 * @param triplePattern
	 * @param varPos 
	 * @param keyVars 
	 * @param srcs 
	 * @param observer
	 */
	public TriplePattern(Node[] triplePattern, List<Variable> keyVars, int[] varPos, Set<String> srcs, KeyObserver observer) {
		key = triplePattern;
		_keyVars = keyVars; 
		_varPos = varPos;
		if(observer != null){
			_obs.add(observer);
			_srcs.addAll(srcs);
		}
//		logger.info("[INIT] "+this.toString());
	}

	/**
	 * @return
	 */
	public Node subject() {
		return key[0];
	}

	/**
	 * @return
	 */
	public Node predicate() {
		return key[1];
	}

	/**
	 * @return
	 */
	public Node object() {
		return key[2];
	}

	/**
	 * @param pattern
	 */
	public void update(TriplePattern pattern) {
		for(KeyObserver ko: pattern._obs ){
			_obs.add(ko);	
		}
		_srcs.addAll(pattern._srcs);
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof TriplePattern)) return false;
		return (this.compareTo((TriplePattern) obj)==0);
	}
	/**
	 * @return
	 */
	public TreeSet<KeyObserver> getOperators() {
		return _obs;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TriplePattern arg0) {
		int diff = NodeComparator.NC.compare(key,arg0.key);
		if(diff == 0){
			diff = _keyVars.size()- arg0._keyVars.size();
		}
		if(diff == 0 ){
			for(Variable v: _keyVars){
				if(!arg0._keyVars.contains(v)) diff++;
			}
			for(Variable v: arg0._keyVars){
				if(!_keyVars.contains(v)) diff++;
			}
		}
		if(diff == 0){
			diff = _varPos.length-arg0._varPos.length;
			if(!Arrays.equals(_varPos, arg0._varPos))
				diff =-1;
		}
		return diff; 
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TriplePattern["+Nodes.toN3(key)+"] "+_srcs.size()+" srcs and "+_obs.size()+" observers";
	}

	/**
	 * @return
	 */
	public Node[] getKey() {
		// TODO Auto-generated method stub
		return key;
	}

	public List<Variable> getKeyVariables(){
		return _keyVars;
	}
	
	public int [] getVariablePosition(){
		return _varPos;
	}
	
	/**
	 * @return
	 */
	public Set<String> getSources() {
		return _srcs;
	}

	@Override
	public int hashCode() {
		
		return super.hashCode();
	}
}
