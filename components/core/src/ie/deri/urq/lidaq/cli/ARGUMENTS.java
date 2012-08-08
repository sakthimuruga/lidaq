/**
 *
 */
package ie.deri.urq.lidaq.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;

/**
 * Commandline parameters
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jul 19, 2011
 */
public class ARGUMENTS {

	public static final int SHORT_ARG = 0;
	public static final int LONG_ARG = 1;
	
	/*
	 * HELP
	 */
	protected static final String PARAM_HELP = "?";
	protected static final String[] PARAM_HELP1 = createParam("h","help");
	protected static final OptionGroup OPTIONGROUP_HELP = new OptionGroup();
	static{
		OPTIONGROUP_HELP.addOption(createOption("help", 0, "print help screen", PARAM_HELP, null,false));
		OPTIONGROUP_HELP.addOption(createOption("help", 0, "print help screen", PARAM_HELP1[SHORT_ARG], PARAM_HELP1[LONG_ARG],false));
	}
	
	/*
	 * GENERAL ARGUMENTS
	 */
	public static final String[] PARAM_SPARQL_QUERY = createParam("q","query");
	public static final Option OPTION_SPARQL_QUERY = createOption("query file", 1, "sparql query", PARAM_SPARQL_QUERY[SHORT_ARG], PARAM_SPARQL_QUERY[LONG_ARG],false);

	public static final String[] PARAM_PROXY_HOST = createParam("pH","pHost");
	public static final Option OPTION_PROXY_HOST = createOption("host", 1 , "proxy host" , PARAM_PROXY_HOST[SHORT_ARG],PARAM_PROXY_HOST[LONG_ARG],false);
	public static final String[] PARAM_PROXY_PORT = createParam("pP","pPort");
	public static final Option OPTION_PROXY_PORT = createOption("port",1,"proxy port",PARAM_PROXY_PORT[SHORT_ARG],PARAM_PROXY_PORT[LONG_ARG],false);

	
	public static final String [] PARAM_DEBUG = createParam("d","verbose");;
	public static final Option OPTION_DEBUG = createOption("flag",0,"enable verbose mode",PARAM_DEBUG[SHORT_ARG],PARAM_DEBUG[LONG_ARG],false);
	
	public static final String [] PARAM_OUTPUTFILE = createParam("o","outFile");;
	public static final Option OPTION_OUTPUTFILE = createOption("file",1,"dumping results to file",PARAM_OUTPUTFILE[SHORT_ARG],PARAM_OUTPUTFILE[LONG_ARG],false);
	
	public static final String [] PARAM_INPUT_FOLDER = createParam("id","inDir");;
	public static final Option OPTION_INPUT_FOLDER = createOption("directory",1,"input directory",PARAM_INPUT_FOLDER[SHORT_ARG],PARAM_INPUT_FOLDER[LONG_ARG],true);;
	
	public static final String [] PARAM_OUTPUT_FOLDER = createParam("od","outDir");;
	public static final Option OPTION_OUTPUT_FOLDER = createOption("directory",1,"output directory",PARAM_OUTPUT_FOLDER[SHORT_ARG],PARAM_OUTPUT_FOLDER[LONG_ARG],true);;
	
	

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	protected static String[] createParam(String s, String l) {
		String [] arg = new String[2];
		arg[SHORT_ARG]=s;
		arg[LONG_ARG]=l;
		return arg;
	}
	protected static Option createOption(String argName, int args, String description, String shortArgname, String longArgname, boolean mandatory){

		Option o;
		if(shortArgname!=null){
			o  = OptionBuilder.withArgName(argName)
			.withDescription(description).create(shortArgname);
		}
		else
			o  = OptionBuilder.withArgName(argName)
			.withDescription(description).create();

		if(longArgname!=null){
			o.setLongOpt(longArgname);
		}
		if(args >= 0)
			o.setArgs(args);
		
		o.setRequired(mandatory);
		return o;
	}
}
