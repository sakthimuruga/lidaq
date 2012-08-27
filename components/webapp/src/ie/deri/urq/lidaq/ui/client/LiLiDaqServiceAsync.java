/**
 *
 */
package ie.deri.urq.lidaq.ui.client;

import ie.deri.urq.lidaq.ui.shared.Bindings;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jul 3, 2011
 */
public interface LiLiDaqServiceAsync {


	void getNewResults(String queryID,
			AsyncCallback<List<Bindings>> callback);

	/**
	 * 
	 */
	void getTemplateQueries(AsyncCallback<HashMap<String,String>> callback);

	
	void executeQuery(String q, String v_srcSel, boolean seeAlso,
			String v_rMode, HashMap<String, String> eps,
			boolean any23On, AsyncCallback<String[]> callback);

}
