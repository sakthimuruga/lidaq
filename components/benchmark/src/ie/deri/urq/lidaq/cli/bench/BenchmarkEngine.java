package ie.deri.urq.lidaq.cli.bench;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.query.QueryConfig;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;


public abstract class BenchmarkEngine{

	abstract public QueryConfig parseQueryConfig(CommandLine cmd, File queryFile) throws IOException;

	abstract public QueryExecutionBenchmark benchmark(QueryConfig qc, File out) throws Exception;

	abstract public void setup();
	abstract public void shutdown();
	
	

//	public void printOptions() {
//		HelpFormatter formatter = new HelpFormatter();
//		formatter.printHelp(this.getClass().getSimpleName(), getOption() ,true);
//	}
//
//	public Object getCommand() {
//		return this.getClass().getSimpleName();
//	}
//
//	abstract public String getDescription();
}