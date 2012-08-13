package ie.deri.urq.lidq.benchmark;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.cli.bench.ResultSetBenchmarkWrapper;
import ie.deri.urq.lidaq.query.QueryConfig;
import ie.deri.urq.lidaq.query.QueryEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class QueryBenchmark {
private final static Logger	logger = Logger.getLogger("");
protected void updateLogger(File file) {
	FileHandler handler;
	try {
		handler = new FileHandler(file.getAbsolutePath()+".log");
		java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
        for(Handler h :root.getHandlers()){
        	if( h instanceof FileHandler){
        		root.removeHandler(h);
        	}
        }
        handler.setFormatter(new SimpleFormatter());
		root.addHandler(handler);
	} catch (Exception e) {
		e.printStackTrace();
	}
}	

/* 
	  * results.format
	  * log -> pipe everything to standard out
	  * settings.props
	  * summary.prop
	  * */
	public QueryExecutionBenchmark benchmark(final QueryEngine qe, final QueryConfig qc, File benchDir) throws Exception{
		updateLogger(new File(benchDir,qc.getQueryID()));
		
		QueryExecutionBenchmark qeb = new QueryExecutionBenchmark(qc.getQueryID(), qc.getQuery());
		qc.setQueryExecutionBenchmark(qeb);
		qeb.setBenchDir(benchDir);
		
		storeProps(qc,new File(benchDir,qc.getQueryID()+".props"));	
		
		long start = System.currentTimeMillis();
		qe.setup();
		
		ResultSet rs = qe.executeSelect(qc);
		
		ResultSetBenchmarkWrapper rsw=null;
		if(rs !=null){
			rsw = new ResultSetBenchmarkWrapper(rs,start);
			printResultSet(rsw, new File(benchDir,qc.getQueryID()+".tsv") );
		}
		long end = System.currentTimeMillis();
		
		qe.tearDown(qeb);
		if(rsw !=null){
			qeb.setFirstResult(rsw.getTimeFirstResult());
			qeb.setLastResult(rsw.getTimeLastResult());
			qeb.setTotatlTime(end-start);
			qeb.setResultSize(rsw.getRowNumber());
		}
		else{
			qeb.setFirstResult(-1);
			qeb.setLastResult(-1);
			qeb.setTotatlTime(end-start);
			qeb.setResultSize(-1);
		}	
		storeBenchmark(qeb,new File(benchDir,qc.getQueryID()+".bench"));
		return qeb;
	}
	
	private void printResultSet(ResultSet rs, File resultFile) throws FileNotFoundException {
		
		PrintStream ps = new PrintStream(resultFile);
		try{
			ResultSetFormatter.outputAsTSV(ps, rs);
		}finally{
			ps.close();
		}
		logger.info("[BENCH] stored results to "+resultFile);
	}

	/**
	 * 
	 * @param qe
	 * @param qc
	 * @param benchDir
	 * @return - 
	 * @throws Exception
	 */
//	abstract public boolean innerBench(QueryEngine qe, QueryConfig qc, File benchDir) throws Exception ;
	
	private void storeBenchmark(QueryExecutionBenchmark qeb, File file) throws IOException {
		qeb.storeBenchmark(file);
		logger.info("[BENCH] stored benchmark results to "+file+" ("+file.exists()+")");
	}

//	abstract protected QueryExecutionBenchmark innerBench(QueryEngine qe, QueryConfig qc, File benchDir)throws Exception;

	private void storeProps(QueryConfig qc, File file) throws IOException {
//		String s = qc.getClass().getSimpleName().toLowerCase();
		qc.storeConfig(file);
		logger.info("[BENCH] stored properties to "+file+" ("+file.exists()+")");
		
	}
}
