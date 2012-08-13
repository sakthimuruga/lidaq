/**
 *
 */
package ie.deri.urq.lodq;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Dec 17, 2010
 */
public class Queries {


	private final static File queryDir = new File("components/live_query/resources/queries");

	//	private static String [] queries = {
	//		"test.sparql", //0
	//		"mixed/harth_friends_geolocation.sparql", //1
	//		"mixed/harth_geolocation.sparql", //2
	//		"lookups/polleres_knows.sparql", //3
	//		"path/harth_#knows_#label.sparql", //4
	//		"path/harth_knows_#label.sparql", //5
	//		"path/identica_knows.sparql", //6
	//		"path/identica_knows_seeAlso.sparql", //7
	//		"path/path.12.sparql",//8
	//		"path/co_authorLabel_graph.sparql",//9
	//		"lookups/eurostats.sparql",//10
	//		"mixed/eurostat.sparql",//11
	//		"path/umbrich_knows_#label.sparql"
	//	};

	private static HashMap<String, File> _queryMap;

	public static File getQueryFile(File queryRoot, String queryName){
		init(queryRoot);
		return _queryMap.get(queryName);

	}

	public static Set<String> queryNames(File queryRoot){
		init(queryRoot);
		return _queryMap.keySet();
	}
	/**
	 * @param queryRoot 
	 * 
	 */
	private static void init(File queryRoot) {
		if(_queryMap != null) return;

		_queryMap = new HashMap<String, File>();
		System.err.println("Checking directory "+queryRoot.getAbsolutePath()+" for query files");

		parseQueries(queryRoot);
	}

	private static void parseQueries(File queryRoot) {
		for(File subItem: queryRoot.listFiles()){
			if(subItem.getName().startsWith(".")) continue;
			else if(subItem.isDirectory()){
				parseQueries(subItem);
			}
			if(!subItem.getName().endsWith("~")){
				String name = subItem.getName().replaceAll(".sparql", "");
//				System.out.println("  loading "+subItem);
				_queryMap.put(name, subItem);
			}
		}
	}

}
