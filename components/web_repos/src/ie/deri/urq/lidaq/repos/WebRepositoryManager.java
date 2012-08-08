/**
 *
 */
package ie.deri.urq.lidaq.repos;

import ie.deri.urq.lidaq.source.SourceLookupManager;

import java.util.logging.Logger;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Feb 11, 2011
 */
public class WebRepositoryManager{


	private static final Logger logger =Logger.getLogger(WebRepositoryManager.class
			.getName());
	

	final private String _proxyHost;
	final private String _proxyPort;
	 
	private final SourceLookupManager _slm;


	
	/**
	 * Disable the use of a proxy
	 */
	public WebRepositoryManager() {
		this(null,null);
	}
	/**
	 * 
	 */
	public WebRepositoryManager(final String proxyHost,final  String proxyPort) {
		_proxyPort = proxyPort;
		_proxyHost = proxyHost;
		_slm = new SourceLookupManager(_proxyHost, _proxyPort);
	}

	public WebRepository getRepository(){
		return new WebRepository(_slm);
	}
	


	public void shutdown(){
		if(_slm != null){
			_slm.shutdown();
		}
	}
}