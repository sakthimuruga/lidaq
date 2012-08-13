/**
 *
 */
package ie.deri.urq.lidaq.cli;

import ie.deri.urq.lidaq.cli.CLIObject;
import ie.deri.urq.lidaq.cli.bench.BENCH_ARGUMENTS;
import ie.deri.urq.lidq.benchmark.eval.BenchmarkEval;
import ie.deri.urq.lidq.benchmark.eval.FixBenchmarkEval;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 17, 2010
 */
public class FixBEval extends CLIObject {
	private static final Logger logger = Logger.getLogger(FixBEval.class.getName());
	
	/* (non-Javadoc)
	 * @see ie.deri.urq.lodq.cli.CLIObject#getDescription()
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		return "SPARQL queries over Linked Data";
	}

	/* (non-Javadoc)
	 * @see ie.deri.urq.lodq.cli.CLIObject#addOptions(org.apache.commons.cli.Options)
	 */
	protected void addOptions(Options opts) {
		opts.addOption(BENCH_ARGUMENTS.OPTION_BENCHDIR);
		opts.addOption(BENCH_ARGUMENTS.OPTION_OUTPUTFILE);
		opts.addOption(BENCH_ARGUMENTS.OPTION_EVAL_STABLE);
		
	}

	/* (non-Javadoc)
	 * @see ie.deri.urq.lodq.cli.CLIObject#execute(org.apache.commons.cli.CommandLine)
	 */
	protected void execute(CommandLine cmd) {
			//proxy
			File inDir = new File(CLIObject.getOptionValue(cmd,BENCH_ARGUMENTS.PARAM_BENCHDIR));
			
			File outDir = new File(CLIObject.getOptionValue(cmd,BENCH_ARGUMENTS.PARAM_OUTPUTFILE));
			
			FixBenchmarkEval eval = new FixBenchmarkEval();
				logger.info("Evaluate all queries");
				eval.evaluate(inDir, outDir);
	}

//	@Override
//	public QueryConfig parseQueryConfig(CommandLine cmd, File queryFile) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public QueryExecutionBenchmark benchmark(QueryConfig qc, File outDir) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void shutdown() {
//		// TODO Auto-generated method stub
//		
//	}
}