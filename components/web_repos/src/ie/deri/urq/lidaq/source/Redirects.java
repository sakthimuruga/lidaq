package ie.deri.urq.lidaq.source;



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.NxParser;

public class Redirects implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger _log =Logger.getLogger(Redirects.class.getName());

	Map<URI, URI> _map;
	
	public Redirects() {
		this(null);
		
	}

	public Redirects(File redirectLocation){
		_map = Collections.synchronizedMap(new Hashtable<URI, URI>());
		parseRedirects(redirectLocation);
	}

	private void parseRedirects(File redirects){
		try{
		if(redirects==null || !redirects.exists()) return;
		_log.info("Reading redirects from file "+redirects);
		long start = System.currentTimeMillis();
		InputStream is = new FileInputStream(redirects);
		if(redirects.getName().endsWith(".gz")){
			is = new GZIPInputStream(is);
		}
		NxParser nxp = new NxParser(is);
		Node [] red = null;
		int count =0;
		while(nxp.hasNext()){
			red = nxp.next();
			count++;
			URI from;
			try {
				from = new URI(red[0].toString());
				URI to = new URI(red[1].toString());
				this.put(from, to);
				
				if(count%100000==0)
					_log.info("Parsed "+count+ " entries and stored "+_map.size()+" entries in the index in "+(System.currentTimeMillis()-start)+" ms.");
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		is.close();
		_log.info("Parsed "+count+ " entries and stored "+_map.size()+" entries in the index in "+(System.currentTimeMillis()-start)+" ms.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public void put(URI from, URI to) {
		if (_map.containsKey(from)) {
			_log.info("URI " + from + " already redirects to " + _map.get(from));
		}
		
		_map.put(from, to);
	}
	
	public URI getRedirect(URI from) {
		Set<URI> done = new HashSet<URI>();
		URI to = null;
		while(_map.containsKey(from)){
			to = _map.get(from);
			if(done.contains(to)){
				//loop
				_log.info("[DEREF]-[LOOP] select "+to+" for node "+from);
				to = from;
				break;
			}
			done.add(to);
			from = to;
		}
		if(to == null)
			to = from;
		try {
			return normalise(to);
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/**
	 * @param n
	 * @param redir
	 * @return
	 * @throws URISyntaxException 
	 */
	public Node getRedirect(Node n){
		try {
			URI from;
			from = new URI(n.toString());
			Set<URI> done = new HashSet<URI>();
			done.add(from);
			URI to = getRedirect(from);
			String norm = to.toASCIIString(); 
//			logger.info("[DEREF] URI  "+n + " src: "+norm);
			return new Resource(norm);
		} catch (Exception e) {
			_log.info("[WARN] "+e.getClass().getSimpleName()+" "+e.getMessage());
			return new Resource("");
		}
	}
	
	public static URI normalise(URI u) throws URISyntaxException {
		String path = u.getPath();
		if (path == null || path.length() == 0) {
			path = "/";
		} else if (path.endsWith("/index.html")) {
			path = path.substring(0, path.length()-10);
		} else if (path.endsWith("/index.htm") || path.endsWith("/index.php") || path.endsWith("/index.asp")) {
			path = path.substring(0, path.length()-9);
		}

		if (u.getHost() == null) {
			throw new URISyntaxException("no host in ", u.toString());
		}

		// remove fragment
		URI norm = new URI(u.getScheme().toLowerCase(),
				u.getUserInfo(), u.getHost().toLowerCase(), u.getPort(),
				path, u.getQuery(), null);

		return norm.normalize();
	}
}