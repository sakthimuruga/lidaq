/**
 *
 */
package ie.deri.urq.lodq;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.LinkedDataQueryEngine;
import ie.deri.urq.lidaq.Utils;
import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.http.SourceSelection.SOURCE_SELECTION;
import ie.deri.urq.lidaq.log.WebRep_LogHandling;
import ie.deri.urq.lidaq.query.QueryConfig;
import ie.deri.urq.lidaq.repos.WebRepository;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;
import ie.deri.urq.lodq.output.ResultSet;
import ie.deri.urq.lodq.output.ResultSetFormatter;

import java.io.File;
import java.io.PrintWriter;

import junit.framework.TestCase;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Aug 21, 2010
 */
public class LODQSequentialTest extends TestCase {

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


	public void testBasic() throws Exception {
					WebRep_LogHandling.lodqDebug();
//		LODQ2_LogHandling.setDefaultLogging();
		WebRepositoryManager wrm = new WebRepositoryManager("localhost","8080");
		LinkedDataQueryEngine lodq = new LinkedDataQueryEngine(wrm);
//		lodq.setProxy("10.196.104.206","8080");
		
		for(int i =0; i < 5; i++){
			long start= System.currentTimeMillis();
		
			WebRepository webrep = wrm.getRepository();
			String query = Utils.readFileContent(Queries.getQueryFile("umbrich_knows_#label"));

			//query config
			QueryConfig qc = new QueryConfig(query);
			qc.sourceSelection(SOURCE_SELECTION.AUTH);
			qc.reasoningMode(REASONING_MODE.OFF);

			//do we benchmark
			QueryExecutionBenchmark b = new QueryExecutionBenchmark("emptyCache-"+i,query);
			b.setBenchDir(new File("test/bench_dumps"));
			qc.queryBenchmark(b);

			ResultSet resSet = lodq.executeSelect(query, qc,webrep);

			PrintWriter pw = new PrintWriter(new File(b.getBenchDir(),"results.nq"));
//			PrintWriter pw = new PrintWriter(System.out);
			int c = ResultSetFormatter.toNQ(resSet, pw, true);
			pw.flush();
			System.out.println("Consumed "+c+" results");
			
			qc.getBenchmark().add("WebRepository",webrep.getBenchmark());
			webrep.dumpCache(qc.getBenchmark().getBenchDir());
			long end = System.currentTimeMillis();
			System.err.println("\n\n\n");
			System.err.println("["+i+"] in "+(end-start)+" ms");
			System.err.println(qc.getBenchmark());
			Thread.sleep(5000);
		}
		
		wrm.shutdown();
	}
}