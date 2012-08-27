package ie.deri.urq.lidaq.ui.client;

import ie.deri.urq.lidaq.ui.shared.Bindings;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("query")
public interface LiLiDaqService extends RemoteService {
	

	public List<Bindings> getNewResults(String execID) throws Exception;
	
	
	HashMap<String, String> getTemplateQueries();
	/**
	 * @param q
	 * @param v_srcSel
	 * @param seeAlso
	 * @param v_rMode
	 * @param eps
	 * @return
	 * @throws Exception
	 */
	String[] executeQuery(String q, String v_srcSel, boolean seeAlso,
			String v_rMode, HashMap<String, String> eps,boolean any23On) throws QueryException;
}
