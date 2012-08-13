package ie.deri.urq.lidaq.query.arq;
import ie.deri.urq.lidaq.query.LTBQEQueryConfig;
import ie.deri.urq.lidaq.repos.QueryBasedSourceSelectionStrategies;
import ie.deri.urq.lidaq.repos.WebRepository;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;

import org.openjena.atlas.lib.StrUtils;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 8, 2011
 */
public class ARQParsing {
	static String NS = "http://example/" ;
	


	public static void main(String[] args) {
//
		String[] queryString = 
        {
            "PREFIX ns: <"+NS+">" ,
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
            "SELECT ?name ?knows2",
            "{ " ,
            "<http://harth.org/andreas/foaf#ah> foaf:knows ?knows .",
//            "FILTER (?knows < 30) .",
            	 "?knows foaf:name ?name .",
            	 "?knows foaf:knows ?knows2 .",
//            	"OPTIONAL { ?knows foaf:img ?img .}",
            "}"
        } ; 
//		String[] queryString = 
//        {
//		"SELECT ?join1 ?join2 ?join3", 
//				"WHERE{",
//				 "?join2 <http://www.w3.org/2002/07/owl#disjointWith> ?join3 .",
//				 "?join1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?join2 .",
//				 "<http://my.opera.com/WolfDog09/xml/foaf#me> <http://xmlns.com/foaf/0.1/knows> ?join1 .",
//				"}"
//	} ; 
		
		Query query = QueryFactory.create(StrUtils.strjoin("\n", queryString)) ;
		Op op = Algebra.compile(query) ;
		System.out.println("________OP_________");
		System.out.println(op);
		System.out.println("___________________");
		
//		op = Transformer.transform(new TransformFilterPlacement(), op);
//		System.out.println(op);
//		System.out.println("_______QUAD________");
//		op = Algebra.toQuadForm(op);
//		System.out.println(op);
//		System.out.println("___________________");
		
//		System.out.println("______UNION_______");
//		op = TransformUnionQuery.transform(op);
//		System.out.println(op);
//		System.out.println("___________________");

//		System.out.println("_______Join________");
//		TransformBGPToJoin t = new TransformBGPToJoin();
//		op = Transformer.transform(t, op);
//		System.out.println(op);
//		System.out.println("___________________");
				
////		System.out.println();
//		final OpBGP deepLeft = t.getDeepLeft();
		ExecutionContext ec2 = new ExecutionContext(ARQ.getContext(), null, null, null) ;
		
		System.out.println("_______OPT_________");
		
		ARQ.getContext().setFalse( ARQ.optFilterPlacement );
		op = Algebra.optimize(op,ARQ.getContext()) ;
//		OpExecutor
		System.out.println(op);
		System.out.println("___________________");
//		QueryEngineBase
		QueryIterator input = QueryIterRoot.create(ec2);
//		Iterator<Var> iter = ((QueryIterRoot) input).getBinding().vars();
		
//		System.out.println("Binding: "+((QueryIterRoot) input).getBinding().);
//		
		
		final LTBQEQueryConfig qc = new LTBQEQueryConfig();
		qc.setQuery(StrUtils.strjoin("\n", queryString));
		qc.sourceSelection(QueryBasedSourceSelectionStrategies.SMART);
		
		WebRepositoryManager wrm = new WebRepositoryManager(null, null);
		final WebRepository wm = wrm.getRepository();
		OpExecutorFactory plainFactory = 
			new OpExecutorFactory()
		{
			@Override
			public OpExecutor create(ExecutionContext execCxt)
			{
				// The default OpExecutor of ARQ.
				return new LIDAQOpExecutor(execCxt,wm,qc) ;
			}
		} ;
		ec2.setExecutor(plainFactory) ;
//		QueryIterator input = QueryIterRoot.create(ec2);
		QueryIterator qIter = QC.execute(op, input, ec2) ;

		ResultSet r = ResultSetFactory.create(qIter, query.getResultVars());
		
		
		
		while( r.hasNext()){
			System.out.println("RESULT: "+r.next());
		}
		System.out.println("NO MORE RESULTS");
		wm.close();
//		loop(qIter);
		wrm.shutdown();


	}


	/**
	 * @param qIter
	 */
	private static void loop(QueryIterator qIter) {
		
		
	}
}
