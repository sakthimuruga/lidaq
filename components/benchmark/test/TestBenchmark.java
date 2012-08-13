import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.Utils;
import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.cli.Main;
import ie.deri.urq.lidaq.log.LIDAQLOGGER;
import ie.deri.urq.lidaq.query.LTBQEQueryConfig;
import ie.deri.urq.lidaq.repos.QueryBasedSourceSelectionStrategies;
import ie.deri.urq.lidq.benchmark.BenchmarkSuite;
import ie.deri.urq.lidq.benchmark.Queries;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 *
 */

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Apr 8, 2011
 */
public class TestBenchmark extends TestCase   {

	
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		LIDAQLOGGER.setDefaultLogging();
		String query ="umbrich_knows";
		//		query ="dbpedia";
		//		query ="aharth_knows";
				query ="rt_film_rating";
				query ="rt_film_rating";
		query ="polleres_knows";
//		query ="o-path-2.2";
//		query ="star-3-0.1";
//		query ="star-2-1.1";
//		query ="dbpedia-2triplePatterns";
//		query ="3-triplepattern.1";
//		query ="o-lookup.1";
//		query ="query2ndDegree1Andreas";
//		query ="queryUnsetPropsAnja";
		
		
		
		
//		File queryRoot = new File("components/live_query/resources/queries");
		File queryRoot = new File("/Users/juum/Documents/deri-svn/resources/queries");
		query="ld11";
//		query="q-178-2_0_1_0";
//		query="q-159-0_0_1_0";
//		query="q-121-0_0_1_0";
//		query="q-124-0_0_1_1";
//		query="q-151-0_0_1_1";
		query="nur_aini";
		query="ld9";
		query="dbpediaGermany";
		String [] reas = {"ALL_DYN_CLOSURE","RDFS","OFF","OWL"};
		String srcSel = "smarts";
		String [] arg ={
				"Bench",
				"-b","LTBQE",
				"-bd","benchDir/"+query+"/",
				"-sl",srcSel
				,"-r",reas[2]
//				,"-sA"
//				,"-t","30"
				,"-q", ie.deri.urq.lodq.Queries.getQueryFile(queryRoot,query).getAbsolutePath()
		};
		Main.main(arg);
	}
	
	public void testSingleQuery() throws Exception {
		LIDAQLOGGER.operatorDebug();
//		LODQ2_LogHandling.setDefaultLogging();
		BenchmarkSuite bs = new BenchmarkSuite(null, null);
		
//		File queryFile = new File("queries/test/q1.sparql");
		File queryFile = new File("/Users/juum/Data/queries/oholaf.sparql");
		File outDir  = new File("queries/test/q1.sparql.out");
		String query = Utils.readFileContent(queryFile);
		
		LTBQEQueryConfig qc = new LTBQEQueryConfig(query);
		qc.sourceSelection(QueryBasedSourceSelectionStrategies.SMART);
//		qc.sourceSelection(BasicSourceSelectionStrategies.SMART);
		qc.reasoningMode(REASONING_MODE.OFF);
		
		qc.setFollowSeeAlso(true);

		String fileID = queryFile.getName();
		fileID+="_smart";
		fileID+="_off";
		QueryExecutionBenchmark 	b = new QueryExecutionBenchmark(fileID,query);
//		b.setBenchDir(outDir);
//		qc.queryBenchmark(b);
		System.out.println("EVERYTHING OK: "+bs.benchmark(qc));
		System.out.println(b.oneLineSummary(" "));
		System.out.println(qc);
		bs.shutdown();
	}
}
