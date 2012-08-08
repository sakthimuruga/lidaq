/**
 *
 */
package ie.deri.urq.lidaq.source;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.OWL;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 14, 2011
 */
public class OWLSameAsLinkFilter implements LinkFilter {

	private static final Logger logger = Logger
	.getLogger(OWLSameAsLinkFilter.class.getName());
	
	private final Set<Resource> _pivots = Collections.newSetFromMap(new ConcurrentHashMap<Resource,Boolean>());
	private SourceLookup _sl;
	
	/**
	 * @param _sl
	 */
	public OWLSameAsLinkFilter(SourceLookup sl) {
		_sl = sl;
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
		
		if(nx[1].equals(OWL.SAMEAS) && nx[0] instanceof Resource && nx[2] instanceof Resource){
		
			if( _pivots.contains(nx[0])){
				logger.fine("[SAMEAS] [FOLLOW] "+nx[2]+" from "+nx[0]);
				_sl.addNode(nx[2]);
				pivot((Resource) nx[2]);
				
			}
			if( _pivots.contains(nx[2])){
				logger.fine("[SAMEAS] [FOLLOW] "+nx[0]+" from "+nx[2]);
				_sl.addNode(nx[0]);
				pivot((Resource) nx[0]);
				
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ontologycentral.ldspider.hooks.links.LinkFilter#setErrorHandler(com.ontologycentral.ldspider.hooks.error.ErrorHandler)
	 */
	@Override
	public void setErrorHandler(ErrorHandler e) {;}

	/* (non-Javadoc)
	 * @see com.ontologycentral.ldspider.hooks.links.LinkFilter#setFollowABox(boolean)
	 */
	@Override
	public void setFollowABox(boolean follow) {;}

	/* (non-Javadoc)
	 * @see com.ontologycentral.ldspider.hooks.links.LinkFilter#setFollowTBox(boolean)
	 */
	@Override
	public void setFollowTBox(boolean follow) {;}

	/**
	 * @param n
	 */
	public void pivot(Resource n) {
		_pivots.add(n);
	}
}