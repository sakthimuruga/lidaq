/**
 *
 */
package ie.deri.urq.lodq;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.LinkedDataQueryEngine;
import ie.deri.urq.lidaq.Utils;
import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.query.QueryConfig;
import ie.deri.urq.lidaq.query.ResultSetWrapper;
import ie.deri.urq.lidaq.repos.QueryBasedSourceSelectionStrategies;
import ie.deri.urq.lidaq.repos.WebRepository;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;

import java.io.OutputStream;

import junit.framework.TestCase;

import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;


 

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 7, 2011
 */
public class LIDAQAPI extends TestCase{

	
	private WebRepositoryManager wrm;
	private LinkedDataQueryEngine lodq;
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		wrm = new WebRepositoryManager(null, null);
		lodq = new LinkedDataQueryEngine(wrm);
		
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
		wrm.shutdown();
	}
	
	public void testSingle() throws Exception {
		String query ="testQuery";
		
		
		//QUERY
		String q = Utils.readFileContent(Queries.getQueryFile(query));
		System.out.println(q);
		//query config
		QueryConfig qc = new QueryConfig(q);
		qc.sourceSelection(QueryBasedSourceSelectionStrategies.SMART);
		qc.reasoningMode(REASONING_MODE.OFF);
		
//		if(qc.getReasoningMode().name().equals(REASONING_MODE.RDFS.name())){
//			qc.setTbox(ReasonerFramework.deserialiseRDFSTBox());
//		}
//		else if(qc.getReasoningMode().name().equals(REASONING_MODE.OWL.name())){
//			qc.setTbox(ReasonerFramework.deserialiseSAMEASTBox());
//			
//		}
//		else if(qc.getReasoningMode().name().equals(REASONING_MODE.ALL.name())){
//			qc.setTbox(ReasonerFramework.deserialiseRDFSSAMEASTBox());
//		}
//		else if(qc.getReasoningMode().name().equals(REASONING_MODE.OFF.name())){
//			;
//		}
		
//		qc.setTbox(tbox);
		qc.setFollowSeeAlso(true);
		
		//do we benchmark
		QueryExecutionBenchmark b =  null;
		b = new QueryExecutionBenchmark("test",q);
		
		qc.queryBenchmark(b);
		
		
		int count =0;

		WebRepository wr = wrm.getRepository();
		
		
		ResultSetWrapper resSet = lodq.executeSelect(q, qc,wr);
		//output
		OutputStream os = System.out;
//		if(cmd.hasOption(CLIObject.PARAM_OUTPUTFILE)){
//			File out = new File(cmd.getOptionValue(CLIObject.PARAM_OUTPUTFILE));
//			out.getParentFile().mkdirs();
//			os = new FileOutputStream(out);
//			qc.setResultOutput(out);
//		}
		
		ResultSetFormat resultFormat = ResultSetFormat.syntaxRDF_XML;
		ResultSetFormatter.output(os, resSet, resultFormat);
		count = resSet.size();
//		count = ResultSetFormatter.format(resSet, pw, cmd.hasOption(CLIObject.PARAM_PRINTVARS), resultFormat);
		os.close();
		wr.close();
		
		
		if(b != null){
			b.put(QueryExecutionBenchmark.TOTAL_RESULTS,resSet.size());
//			b.put(Benchmark.TOTAL_TIME,(end-start));
		}
		qc.setResultSize(count);
//		qc.setTime(end-start);
//		logger.info("[STOP] time "+(end-start)+" ms for "+count+" results");
		
//		if(cmd.hasOption(CLIObject.PARAM_BENCHMARK)){
			b.add("WebRepository",wr.getBenchmark());
//			lodq.updateBenchmark(b);
//		}
		
//		if(b != null && cmd.hasOption(CLIObject.PARAM_BENCHDIR)){
//			File outDir = new File(cmd.getOptionValue(CLIObject.PARAM_BENCHDIR));
//			outDir.mkdirs();
//			b.setBenchDir(outDir);
//			wr.writeAccessLog(b.getAccesLog());
//			wr.dumpCache(b.getCacheFile());
////			lodq.dumpCache(b);
//			qc.writeResults();
//		}
		System.err.println(qc);
//		wrm.shutdown();
		if(b!=null)
		System.err.println(b.oneLineSummary(" "));

	}
}
