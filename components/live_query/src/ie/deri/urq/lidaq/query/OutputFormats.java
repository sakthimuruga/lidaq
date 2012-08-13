/**
 *
 */
package ie.deri.urq.lidaq.query;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 13, 2011
 */
public class OutputFormats extends ResultSetFormat{

	private static HashSet<String> s;

	/**
	 * @param fmt
	 */
	protected OutputFormats(ResultSetFormat fmt) {
		super(fmt);
		// TODO Auto-generated constructor stub
	}

	public static Set<String> listFormats(){
//		ResultSetFormat.syntaxXML+" "+ResultSetFormat.syntaxText+" "+ResultSetFormat.syntaxJSON+" "+ResultSetFormat.syntaxRDF_XML;
		if(s == null){
			
		s = new HashSet<String>();
		s.add("srx") ;
        s.add("xml") ;
        s.add("rdf") ; 
//        syntaxNames.put("rdf/n3",  syntaxRDF_N3) ;
        s.add("rdf/xml") ;
//        syntaxNames.put("n3",      syntaxRDF_N3) ;
//        syntaxNames.put("ttl",     syntaxRDF_TURTLE) ;
//        syntaxNames.put("turtle",  syntaxRDF_TURTLE) ;
        s.add("text") ;
        s.add("json") ;
        s.add("yaml") ;    // The JSON format is a subset of YAML
//        syntaxNames.put("sse",     syntaxSSE) ;
//        syntaxNames.put("csv",     syntaxCSV) ;
//        syntaxNames.put("tsv",     syntaxTSV) ;
		}
		return s;
	}

	
	
}
