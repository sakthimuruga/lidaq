/**
 *
 */
package ie.deri.urq.lidaq.cli.bench;

import ie.deri.urq.lidaq.cli.ARGUMENTS;

import org.apache.commons.cli.Option;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jul 19, 2011
 */
public class BENCH_ARGUMENTS extends ARGUMENTS{
	public static final String[] PARAM_QUERY_INDEX = createParam("idx","index");
	public static final Option OPTION_QUERY_INDEX = createOption("dir", 1 , "query over index" , PARAM_QUERY_INDEX[SHORT_ARG], PARAM_QUERY_INDEX[LONG_ARG],false);
	
	public static final String[] PARAM_REDIRECTS = createParam("red","redix");
	public static final Option OPTION_REDIRECTS = createOption("file", 1 , "file containing redirect information" , PARAM_REDIRECTS[SHORT_ARG], PARAM_REDIRECTS[LONG_ARG],false);
	
	public static final String[] PARAM_EVAL_STABLE = createParam("st","stable");
	public static final Option OPTION_EVAL_STABLE = createOption("flag", 0 , "only stable queries" , PARAM_EVAL_STABLE[SHORT_ARG], PARAM_EVAL_STABLE[LONG_ARG],false);
	
	public static final String [] PARAM_BENCHDIR = createParam("bd","benchDir");;
	public static final Option OPTION_BENCHDIR = createOption("directory",1,"benchmark directory",PARAM_BENCHDIR[SHORT_ARG],PARAM_BENCHDIR[LONG_ARG],false);
	
	public static final String [] PARAM_BENCHMARK = createParam("b","bench");;
	public static final Option OPTION_BENCHMARK = createOption("benchmark",1,"benchmark",PARAM_BENCHMARK[SHORT_ARG],PARAM_BENCHMARK[LONG_ARG],false);
	
}