/**
 *
 */
package ie.deri.urq.lidq.benchmark;

import ie.deri.urq.lidaq.LTBQEQueryEngine;
import ie.deri.urq.lidaq.query.LTBQEQueryConfig;
import ie.deri.urq.lidaq.query.ResultSetWrapper;
import ie.deri.urq.lidaq.repos.WebRepository;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;
import ie.deri.urq.lidaq.source.CrawlTaskFactory;
import ie.deri.urq.lidaq.source.CrawlTaskFactory.IDXCrawlTaskFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 9, 2011
 */
public class BenchmarkSuite {
	
	private static final Logger logger = Logger.getLogger(BenchmarkSuite.class
			.getName());

	private WebRepositoryManager wrm;
	private LTBQEQueryEngine lodq;
	private IDXCrawlTaskFactory ctf = null;

	/**
	 * 
	 */
	public BenchmarkSuite(final String pHost,  final String pPort) {
		wrm = new WebRepositoryManager(pHost,pPort);
		lodq = new LTBQEQueryEngine(wrm);
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public boolean benchmark(LTBQEQueryConfig qc) throws Exception {
//		logger.info("Benchmarking \n------------------\n"+qc.getQuery()+"\n----------------");
//		WebRepository webrep = wrm.getRepository();
//		webrep.enableReasoning(qc.getReasoningMode());
//		long start= System.currentTimeMillis();
//		if(ctf !=null)
//			webrep.setCrawlTaskFactory(ctf);
//		
//		ResultSetWrapper resSet = lodq.executeSelect(qc.getQuery(), qc,webrep);
//		FileOutputStream os = new FileOutputStream(new File(qc.getBenchmark().getBenchDir(),"results.nq"));
//		com.hp.hpl.jena.query.ResultSetFormatter.output(os,resSet,ResultSetFormat.syntaxText);
//		int c = resSet.size();
//		long end = System.currentTimeMillis();
//		os.close();
//		end = System.currentTimeMillis();
//		webrep.close();
//
//		if(qc.getBenchmark() != null){
//			qc.getBenchmark().put(QueryExecutionBenchmark.TOTAL_RESULTS,resSet.getRowNumber());
//			qc.getBenchmark().put(QueryExecutionBenchmark.TOTAL_TIME,(end-start));
//			qc.getBenchmark().putAll(webrep.getBenchmark());
//			qc.getBenchmark().getKeyOrder().putAll(webrep.getBenchmark().getKeyOrder());
//		}
//		qc.setResultSize(c);
//		qc.setTime(end-start);
//		webrep.writeAccessLog(qc.getBenchmark().getBenchDir());
//
//		qc.writeResults();
//		System.out.println(qc.getBenchmark());
		return true;
	}

	public void shutdown(){
		wrm.shutdown();
	}

	public void setDerefFactory(File idxDir, File redDir) throws IOException {
		ctf = new CrawlTaskFactory.IDXCrawlTaskFactory(idxDir,redDir);
	}
}