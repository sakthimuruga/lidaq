/**
 *
 */
package ie.deri.urq.lodq;

import ie.deri.urq.lidaq.cli.Main;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.util.NodeFactory;

import junit.framework.TestCase;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Aug 21, 2010
 */
public class LIDAQCLITEST extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//	public void testEmpty() throws Exception {
	//		String [] args = {"LIDAQ"};
	//		Main.main(args);
	//	}

	public void testCLIMAIN() throws Exception {
		
		//		File queryRoot = new File("components/live_query/resources/queries");
		String query ="umbrich_knows";
		//query ="dbpedia";
		query ="aharth_knows";
		//		query ="rt_film_rating";
		//		query ="rt_film_rating";
		query ="polleres_knows";
		//		query ="harth_#knows_label";
		//		query ="o-path-2.2";
		//		query ="star-3-0.1";
		//		query ="star-2-1.1";
		//		query ="dbpedia-2triplePatterns";
		//		query = "queryUnsetPropsAnja";
		//		query ="testQuery";
		//		query = "s-path-2.1";
		query = "ld10";
		//		query = "graph1";

		
		
		
		
		File queryRoot = new File("/Users/juum/Documents/deri-svn/resources/queries");
		//query = "dbGraphTest";
		//		query = "dbGraphTestWURI";
		
		
		
		queryRoot = new File("queries.tmp");
		queryRoot.mkdirs();
		
		String queryString =
				"SELECT DISTINCT ?knows WHERE{" +
				"?s <http://xmlns.com/foaf/0.1/knows> ?knows " +
				"} BINDINGS ?s {" +
				"(<http://sw.deri.org/~aidanh/foaf/foaf.rdf#Aidan_Hogan> )\n" +
				"}";
		
//		queryString ="SELECT DISTINCT  *\n"
//+"WHERE\n"
//+"  { ?s0 <http://rdfs.org/sioc/ns#account_of> ?joinSO00 }\n"
//+"BINDINGS ?joinSO00\n"
//+"{ \n"
//+"  ( <http://identi.ca/user/26108> )\n"
//+"  ( <http://social.instigado.net/./index.php/user/160> )\n"
//+"  ( <http://social.instigado.net/./index.php/user/97> )\n"
//+"}\n";
		
		queryString ="SELECT DISTINCT *\n"
				+"WHERE\n"
				+"  { ?s0 <http://xmlns.com/foaf/0.1/knows> ?knows ." +
				"     ?knows <http://xmlns.com/foaf/0.1/name> ?name .}\n"
				+"BINDINGS ?s0\n"
				+"{ \n"
				+"  ( <http://sw.deri.org/~aidanh/foaf/foaf.rdf#Aidan_Hogan> )\n"
				+"}\n";
		
		FileWriter fw = new FileWriter(new File(queryRoot,"tmpquery.sparql"));
		fw.write(queryString);
		fw.close();
		query = "tmpquery";
		
		
		
		com.hp.hpl.jena.query.Query q = QueryFactory.create(queryString);
		List<Var> vars = new ArrayList<Var>();
		vars.add(Var.alloc("s"));
		List<Binding> vals = new ArrayList<Binding>();
		BindingMap b = new BindingMap();
		b.add(Var.alloc("s"), Node_URI.createURI("http://sw.deri.org/~aidanh/foaf/foaf.rdf#Aidan_Hogan"));
		vals.add(b);
//		q.setBindings(vars, vals);
		System.out.println(q);
		
		String []srcSel = {"SMART" , "ALL" , "AUTH" };
		
		//		String reasoning = "OFF";
		//		String reasoning = "RDFS";
		String []reasoning = {"OFF" , "RDFS" , "OWL" };
		
		
		
		String [] args = { 
				"LIDAQ"
				,"-r",reasoning[0]
						,"-sl",srcSel[0]
						,"-q", Queries.getQueryFile(queryRoot,query).getAbsolutePath()
						,"-of","text"
						,"-o","test/cli/"+query+"/"+query+"."+srcSel+".results.nq"
						,"-sA"
						,"-t", "10"
		};
		Main.main(args);
	}

}