/**
 *
 */
package ie.deri.urq.lidaq.source;


import java.util.HashSet;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.RDFS;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 23, 2010
 */
public class RDFSSeeAlsoLinkFilter implements LinkFilter {

	private static final Logger logger = Logger.getLogger(RDFSSeeAlsoLinkFilter.class
			.getName());
	
	private SourceLookup _sl;

	private final HashSet<Resource> _pivots = new HashSet<Resource>();
	/**
	 * @param sourceLookup 
	 * @param _consolidator
	 * @param _q
	 */
	public RDFSSeeAlsoLinkFilter(SourceLookup sourceLookup) {
		_sl = sourceLookup;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.yars.nx.parser.Callback#endDocument()
	 */
	@Override
	public void endDocument() {;}

	/* (non-Javadoc)
	 * @see org.semanticweb.yars.nx.parser.Callback#processStatement(org.semanticweb.yars.nx.Node[])
	 */
	@Override
	public void processStatement(Node[] arg0) {
		if(arg0[1].equals(RDFS.SEEALSO)){
		 if(arg0[0] instanceof Resource && _pivots.contains(arg0[0]) && arg0[2] instanceof Resource){
			_sl.addNode(arg0[2]);
			logger.fine("[FOLLOW] "+arg0[2]+" cur pivots "+_pivots);
		}}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.yars.nx.parser.Callback#startDocument()
	 */
	@Override
	public void startDocument() {;}

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