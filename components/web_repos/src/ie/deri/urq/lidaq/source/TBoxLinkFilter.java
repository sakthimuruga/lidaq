package ie.deri.urq.lidaq.source;


import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.namespace.OWL;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.namespace.RDFS;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;

public class TBoxLinkFilter implements LinkFilter {

	private SourceLookup _sl;

	public TBoxLinkFilter(SourceLookup sltBox) {
		_sl = sltBox;
	}

	@Override
	public void endDocument() {
		// TODO Auto-generated method stub

	}

	@Override
	public void processStatement(Node[] arg0) {
		_sl.addNode(arg0[1]);
		if(arg0[1].equals(RDF.TYPE))
			_sl.addNode(arg0[2]);
		
		if(arg0[1].equals(RDFS.DOMAIN) 
				|| arg0[1].equals(RDFS.RANGE)
				|| arg0[1].equals(RDFS.SUBCLASSOF)
				|| arg0[1].equals(RDFS.SUBPROPERTYOF)
				|| arg0[1].equals(OWL.IMPORTS) 
				){
			_sl.addNode(arg0[2]);
			_sl.addNode(arg0[0]);
		}
	}

	@Override
	public void startDocument() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setErrorHandler(ErrorHandler arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFollowABox(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFollowTBox(boolean arg0) {
		// TODO Auto-generated method stub

	}

}
