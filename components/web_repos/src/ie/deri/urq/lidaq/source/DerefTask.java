/**
 *
 */
package ie.deri.urq.lidaq.source;


import java.net.URI;
import java.util.concurrent.Callable;

import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.hooks.sink.SinkCallback;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 18, 2011
 */
public abstract class DerefTask implements Callable<CrawlResult>{
	
	
	protected final URI lu;
	protected final SourceLookupManager _slm;
	protected final SourceLookup _sl;
	
	protected SinkCallback _content;
	protected Callback _links;
	

	/**
	 * @param uri
	 * @param sl.getIndexReasonerCallback()
	 * @param sourceLookupManager
	 */
	public DerefTask(URI uri, SourceLookup sl,
			SourceLookupManager slm) {
		lu = uri;
		_slm = slm;
		_sl = sl;
		_links = sl.getLinkFilter();
		_content = sl.getContentHandler();
	}

	/**
	 * @return
	 */
	public URI getURI() {
		return lu;
	}
}