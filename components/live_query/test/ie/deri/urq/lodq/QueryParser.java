/**
 *
 */
package ie.deri.urq.lodq;

import ie.deri.urq.lidaq.query.arq.LIDAQOpExecutor;

import java.io.IOException;

import org.semanticweb.yars2.query.Query;
import org.semanticweb.yars2.query.parser.javacc.ParseException;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.QueryFactory;
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
 * @date Apr 9, 2011
 */
public class QueryParser {
	
	public static void main(String[] args) throws ParseException, IOException {
		String query ="" +
				"SELECT DISTINCT * \nWHERE {" +
				"\n{\nGRAPH ?c { ?sIn ?pIn <http://dbpedia.org/resource/Stapler> . }" +
				"\n}UNION{\nGRAPH ?c2 { <http://dbpedia.org/resource/Stapler> ?pOut ?oOut .}" +
				"\n}\n}";
		
		com.hp.hpl.jena.query.Query q = QueryFactory.create(query);
		Op op = Algebra.compile(q) ;
		
		ARQ.getContext().setFalse( ARQ.optFilterPlacement );
		op = Algebra.optimize(op,ARQ.getContext()) ;
		ExecutionContext ec2 = new ExecutionContext(ARQ.getContext(), null, null, null) ;
		ARQ.getContext().setFalse( ARQ.optFilterPlacement );
		QueryIterator input = QueryIterRoot.create(ec2);

		System.out.println(op);
		OpExecutorFactory plainFactory = 
				new OpExecutorFactory()
		{
			@Override
			public OpExecutor create(ExecutionContext execCxt)
			{
				return new LIDAQOpExecutor(execCxt,null,null) ;
			}
		} ;
		ec2.setExecutor(plainFactory) ;

		//		_visitor = new VisitorLODQ(webRep,query, qc);
		
		//		QueryIterator qi = _visitor.visit(op);
		
		QueryIterator qIter = QC.execute(op, input, ec2) ;
		while(qIter.hasNext()){
			qIter.next();
		}
			
	}
}
