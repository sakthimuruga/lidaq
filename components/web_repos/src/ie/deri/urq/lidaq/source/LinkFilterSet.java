/**
 *
 */
package ie.deri.urq.lidaq.source;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.Node;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date May 11, 2011
 */
public class LinkFilterSet implements LinkFilter {

	
	private final Set<LinkFilter> filters = new HashSet<LinkFilter>();
	
	
	
	public void addFilter(LinkFilter lf ){
		filters.add(lf);
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
		for(LinkFilter lf: filters){
			lf.processStatement(nx);
		}
	}

	/* (non-Javadoc)
	 * @see com.ontologycentral.ldspider.hooks.links.LinkFilter#setErrorHandler(com.ontologycentral.ldspider.hooks.error.ErrorHandler)
	 */
	@Override
	public void setErrorHandler(ErrorHandler arg0) {;}

	/* (non-Javadoc)
	 * @see com.ontologycentral.ldspider.hooks.links.LinkFilter#setFollowABox(boolean)
	 */
	@Override
	public void setFollowABox(boolean arg0) {;}

	/* (non-Javadoc)
	 * @see com.ontologycentral.ldspider.hooks.links.LinkFilter#setFollowTBox(boolean)
	 */
	@Override
	public void setFollowTBox(boolean arg0) {;}
}