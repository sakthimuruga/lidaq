/**
 *
 */
package ie.deri.urq.lidq.benchmark;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Dec 17, 2010
 */
public class Queries {

	private final static File queryDir = new File("queries");
	
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
	
	public static File getQueryFile(String queryName){
		init();
		return _queryMap.get(queryName);
		
	}

	public static Set<String> queryNames(){
		init();
		return _queryMap.keySet();
	}
	/**
	 * 
	 */
	private static void init() {
		if(_queryMap != null) return;
		
		_queryMap = new HashMap<String, File>();
		
		parseQueriesFromDir(queryDir);
		
	}

	/**
	 * @param querydir2
	 */
	private static void parseQueriesFromDir(File dir) {
		for(File file: dir.listFiles()){
			if(file.getName().startsWith(".") || file.getName().endsWith("~"))
				continue;
			
			if(file.isDirectory()) parseQueriesFromDir(file);
			else if(file.getName().endsWith(".sparql")){
					String name = file.getName().replaceAll(".sparql", "");
					_queryMap.put(name, file);
				}
			}
		}
	
}
