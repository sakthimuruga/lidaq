package ie.deri.urq.lodq;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.http.SourceSelection.SOURCE_SELECTION;
import ie.deri.urq.lidaq.query.OutputFormats;
import ie.deri.urq.lodq.CLIObject;

import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

public abstract class CLIObject {
	
	private static final Logger logger = Logger.getLogger(CLIObject.class
			.getName());
	private Options _opts;

	public abstract String getDescription();

	public String getCommand(){
		return this.getClass().getSimpleName();
	}

	public static final int SHORT_ARG = 0;
	public static final int LONG_ARG = 1;
	
	/*
	 * Commandline parameters
	 */
	private static final String PARAM_HELP = "?";
	private static final String[] PARAM_HELP1 = createParam("h","help");
	
	
	public static final String PARAM_LOAD_TBOX = "lt";
	public static final String PARAM_STORE_TBOX = "st";
	
	public static final String[] PARAM_SPARQL_QUERY = createParam("q","query");
	
	
	public static final String PARAM_LOAD_PROXY_HOST = "host";
	public static final String PARAM_PROXY_PORT = "port";
	
	public static final String PARAM_DEBUG = "d";
	
	public static final String PARAM_OUTPUTFILE = "o";
	public static final String PARAM_FORMAT = "of";
	
	public static final String PARAM_BENCHDIR = "bd";
	
	public static final String PARAM_FOLLOW_SEEALSO = "sa";
	public static final String PARAM_SOURCE_SELECTION = "s";
	public static final String PARAM_REASONING = "r";
	public static final String PARAM_PRINTVARS = "v";
	/**
	 * HELP
	 */
	private static final OptionGroup OPTIONGROUP_HELP = new OptionGroup();
	public static final Option OPTION_SPARQL_QUERY = createOption("query file", 1, "sparql query", PARAM_SPARQL_QUERY[SHORT_ARG], PARAM_SPARQL_QUERY[LONG_ARG]); 
		
	public static final Option OPTION_LOAD_TBOX = createOption("tbox location",1, "load tbox serialised file",PARAM_LOAD_TBOX,null);
	public static final Option OPTION_STORE_TBOX = createOption("tbox location",1,"store tbox serialised file",PARAM_STORE_TBOX,null);
	
	
	public static final Option OPTION_PROXY_HOST = createOption("host", 1 , "proxy host" , PARAM_LOAD_PROXY_HOST,null);
	public static final Option OPTION_PROXY_PORT = createOption("port",1,"proxy port",PARAM_PROXY_PORT,null);
	
	public static final Option OPTION_BENCHDIR = createOption("bench dir",1,"output for benchmark",PARAM_BENCHDIR,null);
	
	static String srcSelStrats = "", rMode ="", format = "";
	static {
		for(SOURCE_SELECTION s: SOURCE_SELECTION.values()){
			srcSelStrats += s.toString()+" ";
		}
		for(REASONING_MODE s: REASONING_MODE.values()){
			rMode += s.toString()+" ";
		}
//		format += ResultSetFormat.syntaxXML+" "+ResultSetFormat.syntaxText+" "+ResultSetFormat.syntaxJSON+" "+ResultSetFormat.syntaxRDF_XML;
		for(String s: OutputFormats.listFormats()){
			format += s.toString()+" ";
		}
	}
	public static final Option OPTION_SOURCE_SELECTION = createOption("strategy",1,"source selection strategy ( "+srcSelStrats+")",PARAM_SOURCE_SELECTION,null);
	public static final Option OPTION_FOLLOW_SEEALSO = createOption("flag",0,"follow rdfs:seeAlso links",PARAM_FOLLOW_SEEALSO,null);
	
	
	public static final Option OPTION_REASONING = OptionBuilder.withArgName("mode")
	.hasArgs(1)
	.withDescription("reasoning ("+rMode+")")
	.create(PARAM_REASONING);
	public static final Option OPTION_DEBUG = OptionBuilder.withArgName("debug_flag")
	.hasArgs(0)
	.withDescription("flag")
	.create(PARAM_DEBUG);
	public static final Option OPTION_FORMAT = OptionBuilder.withArgName("format")
	.hasArgs(1)
	.withDescription("output format ("+format+")")
	.create(PARAM_FORMAT);
	public static final Option OPTION_OUTPUTFILE = OptionBuilder.withArgName("file location")
	.hasArgs(1)
	.withDescription("output file for result")
	.create(PARAM_OUTPUTFILE );
	public static final Option OPTION_PRINTVARS = OptionBuilder.withArgName("flag")
	.hasArgs(0)
	.withDescription("print project variables")
	.create(PARAM_PRINTVARS);
	public static final String PARAM_BENCHMARK = "b";
	public static final Option OPTION_BENCHMARK = OptionBuilder.withArgName("flag")
	.hasArgs(0)
	.withDescription("print project variables")
	.create(PARAM_BENCHMARK);
	public static final String PARAM_TBOX_SER = "ser";
	public static final Option OPTION_TBOX_SER = OptionBuilder.withArgName("flag")
	.hasArgs(0)
	.withDescription("tbox is in serialised format")
	.create(PARAM_TBOX_SER );
	static{
		OPTIONGROUP_HELP.addOption(createOption("help", 0, "print help screen", PARAM_HELP, null));
		OPTIONGROUP_HELP.addOption(createOption("help", 0, "print help screen", PARAM_HELP1[SHORT_ARG], PARAM_HELP1[LONG_ARG]));
	}
	
	
	private static Option createOption(String argName, int args, String description, String shortArgname, String longArgname){
		
		Option o;
		if(shortArgname!=null){
			o  = OptionBuilder.withArgName(argName)
			.hasArgs(args)
			.withDescription(description).create(shortArgname);
		}
		else
			o  = OptionBuilder.withArgName(argName)
			.hasArgs(args)
			.withDescription(description).create();
		
		if(longArgname!=null){
			o.setLongOpt(longArgname);
		}
		return o;
	}
	
		
	private void init() {
		_opts = new Options();
		_opts.addOptionGroup(OPTIONGROUP_HELP);
		addOptions(_opts);
	}

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	private static String[] createParam(String s, String l) {
		String [] arg = new String[2];
		arg[SHORT_ARG]=s;
		arg[LONG_ARG]=l;
		return arg;
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
			logger.info("ERROR: "+e.getClass().getSimpleName()+" : "+e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(this.getClass().getSimpleName(), _opts ,true);
			System.exit(-1);
		}
		if(args.length == 0 || cmd.hasOption(CLIObject.PARAM_HELP)||cmd.hasOption(CLIObject.PARAM_HELP1[SHORT_ARG])||cmd.hasOption(CLIObject.PARAM_HELP1[LONG_ARG])){
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
		if(cmd.hasOption(param[SHORT_ARG]))
			return cmd.getOptionValue(param[SHORT_ARG]);
		if(cmd.hasOption(param[LONG_ARG]))
			return cmd.getOptionValue(param[LONG_ARG]);
		return null;
	}
}