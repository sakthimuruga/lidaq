/**
 *
 */
package ie.deri.urq.lodq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jan 25, 2011
 */
public class TemplateQueries {
	private static File queryDir = new File("resources/template_queries");


	static Map<String, List<String>> queryCats =null;
	static Map<String, List<Node[]>> queryEntities=null;


	/**
	 * 
	 */
	private static void init() {
		if(queryEntities!=null && queryCats!=null ) return;
		queryCats = new HashMap<String,List<String>>();
		queryEntities = new HashMap<String,List<Node[]>>();
		for(File f: queryDir.listFiles()){
			if(f.isDirectory()){
				List<String> queryFiles = new ArrayList<String>();
				for(File qf: f.listFiles()){
					if(!qf.getName().equals("entities.nq")){
						queryFiles.add(qf.getName().replaceAll(".sparql", ""));
					}
					else{
						List<Node[]> entities = new ArrayList<Node[]>();
						NxParser nxp;
						try {
							nxp = new NxParser(new FileInputStream(qf));
							while(nxp.hasNext()){
								entities.add(nxp.next());
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (ParseException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						queryEntities.put(f.getName(), entities);
					}
				}
				queryCats.put(f.getName(),queryFiles);
			}
		}
	}

	public static Set<String> getQueryCategories(){
		init();
		return queryCats.keySet();
	}

	public static List<String> getQueriesInCategory(String category){
		init();
		return queryCats.get(category);
	}
	public static List<Node[]> getEntitiesInCategory(String category){
		init();
		return queryEntities.get(category);
	}

	public static List<String> createQueries(String category, String queryFile){
		init();
		List<String> queries = new ArrayList<String>();
		if(queryCats.containsKey(category) &&queryCats.get(category).contains(queryFile) &&
				queryEntities.containsKey(category)){
			File qf = new File(queryDir,category);
			qf = new File(qf, queryFile+".sparql");
			if(qf.exists()){
				String query;
				try {
					query = readQueryFromFile(qf);

					for(Node[] entities: queryEntities.get(category)){
						for(int i =0; i < entities.length;i++){
							String q=	query.replaceAll("%entity"+(i+1), entities[i].toN3());
							queries.add(q);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return queries;
	}


	private static String readQueryFromFile(File queryFile) throws IOException{
		init();
		BufferedReader br = new BufferedReader(new FileReader(queryFile));
		StringBuffer query = new StringBuffer();
		String line = null;
		while((line=br.readLine())!=null){
			query.append(line).append("\n");
		}
		br.close();
		return query.toString();
	}

	
	public static void main(String[] args) {
		for(String s: TemplateQueries.getQueryCategories()){
			System.out.println("[CATEGORY] "+s);
			System.out.println(">[Entities]");
			for(Node[] n: TemplateQueries.getEntitiesInCategory(s)){
				System.out.println(" "+Nodes.toN3(n));
			}
			System.out.println("_____________");
			for(String q: TemplateQueries.getQueriesInCategory(s)){
				System.out.println(">[Queries]\n");
				for(String qu: TemplateQueries.createQueries(s, q)){
					System.out.println(qu);
				}
			}
			System.out.println("_____________");
			
			
		}
		
		
	}
}
