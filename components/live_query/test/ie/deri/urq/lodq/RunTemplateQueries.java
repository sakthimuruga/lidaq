/**
 *
 */
package ie.deri.urq.lodq;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.CONSTANTS.SOURCE_SELECTION;
import ie.deri.urq.lidaq.LinkedDataQueryEngine;
import ie.deri.urq.lidaq.benchmark.Benchmark;
import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.log.WebRep_LogHandling;
import ie.deri.urq.lidaq.query.QueryConfig;
import ie.deri.urq.lodq.output.ResultSet;

import java.io.File;
import java.util.TreeMap;

import junit.framework.TestCase;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jan 25, 2011
 */
public class RunTemplateQueries extends TestCase{

	private String proxyPort;
	private String proxyHost;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		WebRep_LogHandling.setDefaultLogging();

		proxyPort = "8080";
		proxyHost = "localhost";

	}

	public void testPerson() throws Exception {
		File outDir = new File("test/temp_query/person");
		outDir.mkdirs();

		TreeMap<String, Integer> qRes = new TreeMap<String, Integer>();
		for(String q: TemplateQueries.getQueriesInCategory("people")){

			for(String query: TemplateQueries.createQueries("people", q)){

				LinkedDataQueryEngine lodq = new LinkedDataQueryEngine();
				lodq.setProxy(proxyHost, proxyPort);

				//query config
				QueryConfig qc = new QueryConfig(query);

				qc.sourceSelection(SOURCE_SELECTION.GREEDY);
				qc.reasoningMode(REASONING_MODE.OFF);

				//do we benchmark
				QueryExecutionBenchmark b = new QueryExecutionBenchmark(q,query);
				b.setBenchDir(outDir);

				qc.queryBenchmark(b);

				long start = System.currentTimeMillis();
				ResultSet resSet;

				resSet = lodq.executeSelect(query, qc);
				int count = 0; 
				while(resSet.hasNext()){
					resSet.next();
					count++;
				}

				long end = System.currentTimeMillis();
				b.put(Benchmark.TOTAL_RESULTS, resSet.size());

				lodq.shutdown();

				b.put(Benchmark.TOTAL_TIME,(end-start));
				lodq.updateBenchmark(b);

				lodq.dumpCache(qc.getBenchmark());
				qc.writeResults();

				System.err.println(qc);
				qRes.put(q,count);
				System.out.println("   res: "+count+" in "+(end-start)+" ms");


			}//for

		}//for
	}
}
