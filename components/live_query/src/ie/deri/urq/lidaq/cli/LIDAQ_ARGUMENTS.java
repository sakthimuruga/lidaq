/**
 *
 */
package ie.deri.urq.lidaq.cli;



import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.query.OutputFormats;
import ie.deri.urq.lidaq.source.SourceSelection.SOURCE_SELECTION;

import org.apache.commons.cli.Option;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jul 19, 2011
 */
public class LIDAQ_ARGUMENTS extends ARGUMENTS{

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
	
	
	public static final String[] PARAM_SOURCE_SELECTION = createParam("sl","srcSel");
	public static final Option OPTION_SOURCE_SELECTION = createOption("srcSel", 1 , "source selection strategy ( "+srcSelStrats+")" , PARAM_SOURCE_SELECTION[SHORT_ARG],PARAM_SOURCE_SELECTION[LONG_ARG],false);
	
	public static final String[] PARAM_TIMEOUT = createParam("t","timeout");
	public static final Option OPTION_TIMEOUT = createOption("time", 1 , "query timeout" , PARAM_TIMEOUT[SHORT_ARG],PARAM_TIMEOUT[LONG_ARG],false);
	
	
	
	public static final String[] PARAM_REASONING = createParam("r","rMode");;
	public static final Option OPTION_REASONING = createOption("rMode", 1 , "reasoning mode  ( "+rMode+")" , PARAM_REASONING[SHORT_ARG],PARAM_REASONING[LONG_ARG],false);;
	
	public static final String PARAM_FOLLOW_SEEALSO = "sA";
	public static final Option OPTION_FOLLOW_SEEALSO = createOption("flag", 0 , "follow seeAlso links" , PARAM_FOLLOW_SEEALSO,null,false);
	
	public static final String [] PARAM_FORMAT = createParam("of","format");
	public static final Option OPTION_FORMAT = createOption("format", 1 , "output format ( "+format+")" , PARAM_FORMAT[SHORT_ARG],PARAM_FORMAT[LONG_ARG],false);
	
	public static final String PARAM_ENABLE_ANY23 = "any23";;
	public static final Option OPTION_ENABLE_ANY23 = createOption("flag", 0 , "parse HTML files with any23" , PARAM_ENABLE_ANY23,null,false);
}
