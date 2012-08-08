/**
 *
 */
package ie.deri.urq.lidaq.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deri.any23.Any23;
import org.deri.any23.extractor.ExtractorRegistry;
import org.deri.any23.filter.IgnoreAccidentalRDFa;
import org.deri.any23.filter.IgnoreTitlesOfEmptyDocuments;
import org.deri.any23.mime.MIMEType;
import org.deri.any23.source.ByteArrayDocumentSource;
import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.hooks.content.ContentHandler;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jul 18, 2011
 */
public class ANY23ContentHandler 
implements ContentHandler {

	private final Any23 runner;

	public ANY23ContentHandler() {
		 runner = new Any23();
	}

	private final Logger _log = Logger.getLogger(this.getClass().getName());

	public boolean canHandle(String mime) {
		return ExtractorRegistry.getInstance().getExtractorGroup().filterByMIMEType(MIMEType.parse(mime))!=null;
	}

	public boolean handle(URI uri, String mime, InputStream source, Callback callback) {
		try {
			runner.extract(new ByteArrayDocumentSource(source, uri.toASCIIString(), mime), new IgnoreAccidentalRDFa(
	                new IgnoreTitlesOfEmptyDocuments(new CallbackNQuadTripleHandler(callback))
	        ));
			
			return true;
		}catch (IOException e) {
			_log.log(Level.WARNING, "Could not read document "+uri, e);
			return false;
		}catch (Exception e) {
			_log.log(Level.WARNING, "Could not read document "+uri, e);
			return false;
		}
	}
}
