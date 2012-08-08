
package ie.deri.urq.lidaq.source;

import ie.deri.urq.lidaq.repos.SourceSelectionStrategy;
import ie.deri.urq.lidaq.repos.TriplePattern;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Feb 16, 2011
 */
public class SourceSelection {

	private static final Logger logger = Logger.getLogger(SourceSelection.class
		.getName());
	
	public static enum SOURCE_SELECTION{
		AUTH, ALL, ONLY_SOURCES 
	}


	protected SourceSelectionStrategy _selStrategy;
	
	/**
	 * 
	 */
	public SourceSelection(final SourceSelectionStrategy srcSel) {
		_selStrategy = srcSel;
	}

	
	/**
	 * @param key
	 * @param joinVals
	 * @param varPos
	 * @param size
	 * @return
	 */
	public Set<Resource> selectURIs(TriplePattern tp,
			int size) {
		Set<Resource> res =  new HashSet<Resource>();
		
		if(_selStrategy==BasicSourceSelectionStrategies.AUTH){
			res= authorativeSources(tp.getKey());
		}else if(_selStrategy==BasicSourceSelectionStrategies.ALL){
			res = allSources(tp.getKey());
		}else if(_selStrategy==BasicSourceSelectionStrategies.ONLY_SOURCES){
			;
		}
		for(String uri :tp.getSources()){
			res.add(new Resource(uri));
		}
		logger.info("["+_selStrategy+"] Selected "+res+" from "+Nodes.toN3(tp.getKey()));
		return res;
	}

	

	

	/**
	 * @param tp
	 * @return
	 */
	private Set<Resource> allSources(Node[] tp) {
		Set<Resource> res = new HashSet<Resource>();
		for(int i=0;i<3;i++){
			if(tp[i] instanceof Resource && tp[i].toString().startsWith("http"))
				res.add((Resource) tp[i]);
		}
		return res;
	}

	/**
	 * @param tp
	 * @return
	 */
	private Set<Resource> authorativeSources(Node[] tp) {
		Set<Resource> src = new HashSet<Resource>();
		if(tp[0] instanceof Resource&& tp[0].toString().startsWith("http")){
			src.add((Resource) tp[0]);
		}
		if(tp[2] instanceof Resource&& tp[2].toString().startsWith("http")){
			src.add((Resource) tp[2]);
		}
		
		return src;
	}


	/**
	 * @param stmt
	 * @param p
	 * @return
	 */
	public Set<Resource> selectURIs(Node[] stmt, TriplePattern p) {
		Set<Resource> src = new HashSet<Resource>();
		
		if(_selStrategy==BasicSourceSelectionStrategies.AUTH){
			src = authorativeSources(stmt);
		}else if(_selStrategy==BasicSourceSelectionStrategies.ALL){
			src = allSources(stmt);
		}else if(_selStrategy==BasicSourceSelectionStrategies.ONLY_SOURCES){
			;
		}
		
		return src;
	}


	/**
	 * @return
	 */
	public String getType() {
		return _selStrategy.toString();
	}
}