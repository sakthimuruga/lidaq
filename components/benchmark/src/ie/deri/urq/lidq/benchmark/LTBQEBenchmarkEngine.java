package ie.deri.urq.lidq.benchmark;

import ie.deri.urq.lidaq.LTBQEQueryEngine;
import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.cli.bench.BenchmarkEngine;
import ie.deri.urq.lidaq.query.LTBQEQueryConfig;
import ie.deri.urq.lidaq.query.QueryConfig;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;

public class LTBQEBenchmarkEngine extends BenchmarkEngine {

	private WebRepositoryManager wrm;
	private LTBQEQueryEngine lqe;
	private final QueryBenchmark qb = new QueryBenchmark();
	@Override
	public QueryConfig parseQueryConfig(CommandLine cmd, File queryFile)
			throws IOException {
		return LTBQEQueryConfig.parseQueryConfig(cmd, queryFile);
	}

	@Override
	public QueryExecutionBenchmark benchmark(QueryConfig qc, File out)
			throws Exception {
		return qb.benchmark(lqe, qc, out);
	}

	@Override
	public void setup() {
		wrm = new WebRepositoryManager();
		lqe = new LTBQEQueryEngine(wrm);
	}

	@Override
	public void shutdown() {
		wrm.shutdown();
	}
}