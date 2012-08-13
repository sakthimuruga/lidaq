package ie.deri.urq.lidaq.query;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;

import com.hp.hpl.jena.query.ResultSet;

abstract public class QueryEngine {
	
	abstract public void setup();
	abstract public void tearDown();
	abstract public void tearDown(QueryExecutionBenchmark qeb);
	abstract public ResultSet executeSelect(QueryConfig qc) throws Exception;
}
