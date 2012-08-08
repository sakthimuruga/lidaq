/**
 *
 */
package ie.deri.urq.lidaq.source;


import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.Header;
import org.apache.http.util.EntityUtils;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.Callbacks;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.hooks.sink.Provenance;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 18, 2011
 */
public class HttpDerefTask extends DerefTask{

	private static final Logger logger = Logger.getLogger(HttpDerefTask.class
			.getName());

	/**
	 * @param uri
	 * @param sl.getIndexReasonerCallback()
	 * @param sourceLookupManager
	 */
	public HttpDerefTask(URI uri, SourceLookup sl,
			SourceLookupManager slm) {
		super(uri, sl, slm);
		logger.info("[HttpDerefTask] [INIT] uri:"+uri);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public CrawlResult call() throws Exception {

		CrawlResult r = new CrawlResult(lu);
		HttpGet hget =null;
		HttpEntity hen=null;
		try{
			long time = System.currentTimeMillis();
			long time1 = System.currentTimeMillis();
			long time2 = time1;
			long time3 = time1;
			long bytes = -1;
			int status = 0;

			String type = null;

			if (!_sl._blacklist.fetchOk(lu, 0, null)) {
				logger.info("[GET] "+lu+" [DENIED] access per blacklist.");
				r.setStatus(CrawlerConstants.SKIP_SUFFIX);
			} else if (!_slm._robots.accessOk(lu)) {
				logger.info("[GET] "+lu+" [DENIED] access per robots.txt.");
				r.setStatus(CrawlerConstants.SKIP_ROBOTS);
			} else {
				time2 = System.currentTimeMillis();

				hget = new HttpGet(lu);
				hget.setHeaders(CrawlerConstants.HEADERS);
				
				try {
					HttpResponse hres = _slm._cm.connect(hget);
					hen = hres.getEntity();
					
					status = hres.getStatusLine().getStatusCode();
					r.setStatus(status);
					if (hres.getFirstHeader("Content-Type") != null) {
						type = hres.getFirstHeader("Content-Type").getValue();
					}
					logger.finer("[GET] "+lu+" status " + status);

					if (status == HttpStatus.SC_OK) {				
						if (hen != null) {
							if (_sl._ff.fetchOk(lu, status, hen)) {
								logger.finer("[GET] "+lu+" streaming content");
								logger.info("[GET] "+lu+" streaming content");
								InputStream is = hen.getContent();
								Callback contentCb = _content.newDataset(new Provenance(lu, hres.getAllHeaders(), status));
								Callbacks cbs = new Callbacks(new Callback[] { contentCb, _links } );

								if(_sl._rdfxmlContentHandler.canHandle(type)){
									_sl._rdfxmlContentHandler.handle(lu, type, is, cbs);
									is.close();
								}else if(_sl._any23Content != null && _sl._any23Content.canHandle(type)){
									_sl._any23Content.handle(lu, type, is, cbs);
								}
								logger.finer("[GET] "+lu+" done streaming content");
//								EntityUtils.consume(hen);
							} else {
								logger.info("[GET] "+lu+" [DENIED] disallowed via fetch filter type " + type);
								r.setStatus(CrawlerConstants.SKIP_MIMETYPE);
								hget.abort();
								hen = null;
								status = 0;
							}
						} else {
							logger.info("[GET] "+lu+" [PROB] HttpEntity is null");
							r.setStatus(CrawlResult.NO_HTTPENTITY);
						}
					} else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY || status == HttpStatus.SC_SEE_OTHER) { 
						// treating all redirects the same but shouldn't: 301 -> rename context URI, 302 -> keep original context URI, 303 -> spec inconclusive
						Header[] loc = hres.getHeaders("location");
						String path = loc[0].getValue();
						logger.finer("[GET] "+lu+" redirecting (" + status + ") to " + path);
						URI to = new URI(path);

						// handle local redirects
						if (!to.isAbsolute()) {
							to = lu.resolve(path);
						}
						_sl.setRedirect(lu, to, status);
//						EntityUtils.consume(hen);
					}
					if (hen != null) {
						bytes = hen.getContentLength();
						r.setContentBytes(bytes);
					}
				} catch (ArrayIndexOutOfBoundsException aie) {
					hget.abort();
					logger.warning("[GET] "+lu+" [EXCEPTION] " + aie.getClass().getName()+" msg:"+aie.getMessage());
					r.setStatus(CrawlResult.getExceptionCode(aie));
					r.setException(aie);
				} catch (NullPointerException npe) {
					hget.abort();
					logger.warning("[GET] "+lu+" [EXCEPTION] " + npe.getClass().getName()+" msg:"+npe.getMessage());
					r.setStatus(CrawlResult.getExceptionCode(npe));
					r.setException(npe);
				} catch (Exception e) {
					hget.abort();
					logger.warning("[GET] "+lu+" [EXCEPTION] " + e.getClass().getName()+" msg:"+e.getMessage());
					r.setStatus(CrawlResult.getExceptionCode(e));
					r.setException(e);
				}finally{
					if(!hget.isAborted())
						hget.abort();
				}
				time3 = System.currentTimeMillis();

				r.setTime("before",(time1-time));
				r.setTime("lookup",(time3-time2));
				r.setTime("lookup-check",(time2-time1));
				logger.info("[RESULT] "+r);
			}
		}catch(final CancellationException e ){
			r.setStatus(CrawlResult.CANCELATIONEXCEPTION);
			r.setException(e);
			logger.warning("[GET] "+lu+" [EXCEPTION] " + e.getClass().getName()+" msg:"+e.getMessage());
		}catch(Exception e){
			r.setStatus(CrawlResult.getExceptionCode(e));
			r.setException(e);

			logger.warning("[GET] "+lu+" [EXCEPTION] " + e.getClass().getName()+" msg:"+e.getMessage());
		}
		return r;
	}
}