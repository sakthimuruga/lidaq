/**
 *
 */
package ie.deri.urq.lidaq.repos;

import ie.deri.urq.lidaq.source.SourceSelection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Feb 16, 2011
 */
public class QueryBasedSourceSelection extends SourceSelection {

	
	private static final Logger logger = Logger
			.getLogger(QueryBasedSourceSelection.class.getName());
	
	private Set<Variable> _joinVars;
	private HashSet<Variable> _distVars;
	/**
	 * @param srcSel
	 */
	public QueryBasedSourceSelection(SourceSelectionStrategy srcSel) {
		super(srcSel);
	}
	
	public Set<Resource> selectURIs(TriplePattern tp,
			int size) {
		if(_selStrategy== QueryBasedSourceSelectionStrategies.SMART){
			return smartSources(tp,size);
		}
		else return super.selectURIs(tp, size);
	}
	
	public Set<Resource> selectURIs(Node[] stmt, TriplePattern p) {
		if(_selStrategy== QueryBasedSourceSelectionStrategies.SMART){
			return smartSources(stmt,p);
		}
		else return super.selectURIs(stmt,p);
	}
	
	/**
	 * @param stmt
	 * @param p
	 * @return
	 */
	private Set<Resource> smartSources(Node[] stmt, TriplePattern key) {
		HashSet<Resource> nodes = new HashSet<Resource>();
		
		if(key.predicate() instanceof Variable && _distVars.contains(key.predicate())){
			//prediacte is a distinguised variable -> deref sub and object in key
			Node [] keyNodes = key.getKey();
			if(keyNodes[0] instanceof Variable && stmt[0] instanceof Resource && stmt[0].toString().toLowerCase().startsWith("http")) nodes.add((Resource) stmt[0]);
			if(keyNodes[2] instanceof Variable && stmt[2] instanceof Resource && stmt[2].toString().toLowerCase().startsWith("http")) nodes.add((Resource) stmt[2]);
		}
		return nodes;
	}

	/**
	 * @param key2 
	 * @param key
	 * @param varPos 
	 * @param cachedResults 
	 * @return
	 */
	private Set<Resource> smartSources(TriplePattern key, int cachedResults) {
		Set<Resource> res = new HashSet<Resource>();
		
		if(key.predicate() instanceof Variable && _distVars.contains(key.predicate())){
			//prediacte is a distinguised variable -> deref sub and object in key
			if(key.subject() instanceof Resource && key.subject().toString().toLowerCase().startsWith("http")) res.add((Resource) key.subject());
			if(key.object() instanceof Resource && key.object().toString().toLowerCase().startsWith("http")) res.add((Resource) key.object());
		}
		else{
			//if |var| =1 and join var == bind var = check cache and deref then
			
			int joinVars =0, distVars=0;
			int distNotJoin =0;
			for(Node n: key.getKeyVariables()){
				if(_distVars.contains(n)){
					distVars++;
					if(!_joinVars.contains(n))distNotJoin++;
				}
				if(_joinVars.contains(n)) joinVars++;
				
			}
			if((joinVars >= 2) || (joinVars>0 && distVars>0 && distNotJoin>0) || (joinVars == 1 && cachedResults==0)){	
				//if we have two join vars
				//OR
				//if we have a distringuished variable which is not the join var
				//OR we have one joinVar and no cache results
				for(int pos : key.getVariablePosition()){
//					if(pos==2 && key.getKey()[1].equals(RDF.TYPE)) continue;//do not deref classes
					if(key.getKey()[pos] instanceof Resource && key.getKey()[pos].toString().toLowerCase().startsWith("http")) res.add((Resource) key.getKey()[pos]);
				}
			}
		}
		logger.info("["+_selStrategy+"] Selected "+res+" for "+key+" with "+cachedResults+" cached results");
		return res;
	}

	
	
	/**
	 * @param joinVariables
	 */
	public void setJoinVariable(Set<Variable> joinVariables) {
		logger.info(" [JOIN]-[VARIABLE] "+joinVariables);
		_joinVars = joinVariables;
		
	}

	/**
	 * @param resultVars
	 */
	public void setResultVariable(ArrayList<Variable> resultVars) {
		logger.info(" [RESULT]-[VARIABLE] "+resultVars);
		_distVars = new HashSet<Variable>( resultVars);
		
	}
}
