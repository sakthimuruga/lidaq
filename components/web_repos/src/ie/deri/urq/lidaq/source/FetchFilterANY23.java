/**
 *
 */
package ie.deri.urq.lidaq.source;

import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.deri.any23.extractor.ExtractorRegistry;
import org.deri.any23.mime.MIMEType;

import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jul 19, 2011
 */
public class FetchFilterANY23 implements FetchFilter {

	/* (non-Javadoc)
	 * @see com.ontologycentral.ldspider.hooks.fetch.FetchFilter#fetchOk(java.net.URI, int, org.apache.http.HttpEntity)
	 */
	@Override
	public boolean fetchOk(URI u, int status, HttpEntity hen) {
		Header ct = hen.getContentType();
		if(ExtractorRegistry.getInstance().getExtractorGroup().filterByMIMEType(MIMEType.parse(ct.getValue()))!=null)return true;
		return false;
	}

}
