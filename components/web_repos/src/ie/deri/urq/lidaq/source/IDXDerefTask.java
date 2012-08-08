/**
 *
 */
package ie.deri.urq.lidaq.source;


import java.net.URI;
import java.util.Iterator;
import java.util.logging.Logger;

import org.semanticweb.nxindex.NodesIndex;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.Callbacks;

import com.ontologycentral.ldspider.hooks.sink.Provenance;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 18, 2011
 */
public class IDXDerefTask extends DerefTask{

	private static final Logger logger = Logger.getLogger(IDXDerefTask.class
			.getName());
	private NodesIndex[] _ni;
	private Redirects _rIDX;

	/**
	 * @param uri
	 * @param rIDX 
	 * @param ni 
	 * @param sl.getIndexReasonerCallback()
	 * @param sourceLookupManager
	 */
	public IDXDerefTask(URI uri, SourceLookup sl,
			SourceLookupManager slm, NodesIndex[] ni, Redirects rIDX) {
		super(uri, sl, slm);
		_ni = ni;
		_rIDX = rIDX;

	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public CrawlResult call() throws Exception {
		CrawlResult r = new CrawlResult(lu);
		try{
			Callbacks cbs = new Callbacks(new Callback[] { _content.newDataset(new Provenance(lu, null, 0)), _links } );

			//we need the index and the redirect index
			logger.info("[SCAN] index for "+lu);
			//get the authorative document URI
			Node redirDoc = new Resource(_rIDX.getRedirect(lu).toASCIIString());
			boolean cnt = false;
			//scan the spoc index
			Node[] key = {new Resource(lu.toASCIIString())};
			
			//filter for authorative statements 
			//run through link filter and
			for(int i=0;i<2;i++){
				Iterator<Node[]> iter = _ni[i].getIterator(key); 
				while(iter.hasNext()){
					Node [] n = iter.next();
					if(n[3].equals(redirDoc))
						cbs.processStatement(n);
					cnt=true;
				}
				//scan the opsc index 
			}
			if(!cnt)r.setStatus(404);
			else{
				r.setStatus(200);	
			}
		}catch(Exception e){
			r.setStatus(CrawlResult.getExceptionCode(e));
			r.setException(e);
		}
		return r;
	}
}