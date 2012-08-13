/**
 *
 */
package ie.deri.urq.lidaq.cli.bench;

import ie.deri.urq.lidaq.cli.LIDAQ;
import ie.deri.urq.lidq.benchmark.LTBQEBenchmarkEngine;

import java.util.logging.Logger;

import org.apache.commons.cli.Options;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 17, 2010
 */
public class LTBQE extends BenchmarkCLI{

	private static final Logger logger = Logger.getLogger(LTBQE.class.getName());
	
	private BenchmarkEngine lbe = new LTBQEBenchmarkEngine();

	public Options getOption() {
		return new LIDAQ().getOptions();
	}

	public String getDescription() {
		return "Benchmarking LTBQ";
	}

	@Override
	protected BenchmarkEngine getBenchmarkEngine() {
		
		return lbe;
	}
}