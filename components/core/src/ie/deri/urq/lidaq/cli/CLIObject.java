package ie.deri.urq.lidaq.cli;

import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public abstract class CLIObject {
	
	private static final Logger logger = Logger.getLogger(CLIObject.class
			.getName());
	private Options _opts;

	public Options getOptions(){
		return _opts;
	}
	
	public abstract String getDescription();

	public String getCommand(){
		return this.getClass().getSimpleName();
	}
	public CLIObject() {
		init();
	}
//	
//	public static final String PARAM_BENCHDIR = "bd";
//	
//	public static final String PARAM_FOLLOW_SEEALSO = "sa";
//	public static final String PARAM_SOURCE_SELECTION = "s";
//	public static final String PARAM_REASONING = "r";
//	public static final String PARAM_PRINTVARS = "v";
//	
//		
//	
//	
//	
//	public static final Option OPTION_BENCHDIR = createOption("bench dir",1,"output for benchmark",PARAM_BENCHDIR,null);
//	
//	static String srcSelStrats = "", rMode ="", format = "";
//	static {
//		for(SOURCE_SELECTION s: SOURCE_SELECTION.values()){
//			srcSelStrats += s.toString()+" ";
//		}
//		for(REASONING_MODE s: REASONING_MODE.values()){
//			rMode += s.toString()+" ";
//		}
////		format += ResultSetFormat.syntaxXML+" "+ResultSetFormat.syntaxText+" "+ResultSetFormat.syntaxJSON+" "+ResultSetFormat.syntaxRDF_XML;
//		for(String s: OutputFormats.listFormats()){
//			format += s.toString()+" ";
//		}
//	}
//	public static final Option OPTION_SOURCE_SELECTION = createOption("strategy",1,"source selection strategy ( "+srcSelStrats+")",PARAM_SOURCE_SELECTION,null);
//	public static final Option OPTION_FOLLOW_SEEALSO = createOption("flag",0,"follow rdfs:seeAlso links",PARAM_FOLLOW_SEEALSO,null);
//	
//	
//	public static final Option OPTION_REASONING = OptionBuilder.withArgName("mode")
//	.hasArgs(1)
//	.withDescription("reasoning ("+rMode+")")
//	.create(PARAM_REASONING);
//	public static final Option OPTION_DEBUG = OptionBuilder.withArgName("debug_flag")
//	.hasArgs(0)
//	.withDescription("flag")
//	.create(PARAM_DEBUG);
//	public static final Option OPTION_FORMAT = OptionBuilder.withArgName("format")
//	.hasArgs(1)
//	.withDescription("output format ("+format+")")
//	.create(PARAM_FORMAT);
//	public static final Option OPTION_OUTPUTFILE = OptionBuilder.withArgName("file location")
//	.hasArgs(1)
//	.withDescription("output file for result")
//	.create(PARAM_OUTPUTFILE );
//	public static final Option OPTION_PRINTVARS = OptionBuilder.withArgName("flag")
//	.hasArgs(0)
//	.withDescription("print project variables")
//	.create(PARAM_PRINTVARS);
//	public static final String PARAM_BENCHMARK = "b";
//	public static final Option OPTION_BENCHMARK = OptionBuilder.withArgName("flag")
//	.hasArgs(0)
//	.withDescription("print project variables")
//	.create(PARAM_BENCHMARK);
//	public static final String PARAM_TBOX_SER = "ser";
//	public static final Option OPTION_TBOX_SER = OptionBuilder.withArgName("flag")
//	.hasArgs(0)
//	.withDescription("tbox is in serialised format")
//	.create(PARAM_TBOX_SER );
//	
//	
	
	
	
		
	protected void init() {
		_opts = new Options();
		_opts.addOptionGroup(ARGUMENTS.OPTIONGROUP_HELP);
		addOptions(_opts);
	}

	
	
	
	/**
	 * add all Option(Groups) to this object
	 * Note: The help flag is set automatically ("?")
	 * @param opts
	 */
	abstract protected void addOptions(Options opts);

	public void run(String[] args) {
		logger.info("=======[START] [ARGS] "+Arrays.toString(args));
		CommandLine cmd = verifyArgs(args);
		
		long start = System.currentTimeMillis();
		execute(cmd);
		long end = System.currentTimeMillis();
		logger.info("=======[END] ("+(end-start)+" ms)");
	}

	abstract protected void execute(CommandLine cmd);

	protected CommandLine verifyArgs(String[] args) {
		init();

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(_opts, args);
		} catch (org.apache.commons.cli.ParseException e) {
			logger.info("ERROR: "+e.getClass().getSimpleName()+" : "+e.getMessage()+" args={"+Arrays.toString(args)+"}");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(this.getClass().getSimpleName(), _opts ,true);
			System.exit(-1);
		}
		if(cmd!=null && (cmd.hasOption(ARGUMENTS.PARAM_HELP)||cmd.hasOption(ARGUMENTS.PARAM_HELP1[ARGUMENTS.SHORT_ARG])||cmd.hasOption(ARGUMENTS.PARAM_HELP1[ARGUMENTS.LONG_ARG]))){
			logger.info("Here is a help (args length "+cmd.getArgList().size()+"): ");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(this.getClass().getSimpleName(), _opts ,true);
			System.exit(-1);
		}
		return cmd;
	}

	/**
	 * @param cmd
	 * @param paramSparqlQuery
	 * @return
	 */
	public static String getOptionValue(CommandLine cmd,
			String[] param) {
		if(cmd.hasOption(param[ARGUMENTS.SHORT_ARG]))
			return cmd.getOptionValue(param[ARGUMENTS.SHORT_ARG]);
		if(cmd.hasOption(param[ARGUMENTS.LONG_ARG]))
			return cmd.getOptionValue(param[ARGUMENTS.LONG_ARG]);
		return null;
	}

	/**
	 * @param cmd
	 * @param paramProxyPort
	 * @param object
	 * @return
	 */
	public static String getOptionValue(CommandLine cmd,
			String[] param, String defaultValue) {
		String s = getOptionValue(cmd, param);
		if(s==null) return defaultValue;
		return s;
	}

	public static String[] getOptionValues(CommandLine cmd,
			String[] param) {
		if(cmd.hasOption(param[ARGUMENTS.SHORT_ARG]))
			return cmd.getOptionValues(param[ARGUMENTS.SHORT_ARG]);
		if(cmd.hasOption(param[ARGUMENTS.LONG_ARG]))
			return cmd.getOptionValues(param[ARGUMENTS.LONG_ARG]);
		return null;
	}
	
	/**
	 * @param paramDebug
	 * @return
	 */
	public static boolean hasOption(CommandLine cmd, String[] param) {
		if(cmd.hasOption(param[ARGUMENTS.SHORT_ARG]))
			return true;
		if(cmd.hasOption(param[ARGUMENTS.LONG_ARG]))
			return true;
		
		return false;
	}
	
	public static boolean hasOption(CommandLine cmd, String param) {
		return cmd.hasOption(param);
	}
}