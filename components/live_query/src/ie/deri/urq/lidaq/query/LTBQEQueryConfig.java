/**
 *
 */
package ie.deri.urq.lidaq.query;


import ie.deri.urq.lidaq.Utils;
import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.cli.CLIObject;
import ie.deri.urq.lidaq.cli.LIDAQ_ARGUMENTS;
import ie.deri.urq.lidaq.repos.QueryBasedSourceSelectionStrategies;
import ie.deri.urq.lidaq.repos.SourceSelectionStrategy;
import ie.deri.urq.lidaq.source.BasicSourceSelectionStrategies;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.semanticweb.saorr.rules.LinkedRuleIndex;
import org.semanticweb.saorr.rules.Rule;
import org.semanticweb.yars.nx.Variable;

import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jan 10, 2011
 */
public class LTBQEQueryConfig extends QueryConfig{

//	static final String HLINE = "__________________________";
//	static final String QUERY = "________| QUERY |_________";
//	static final String CONFIG = "________| CONFIG |________";
//	private static final Object RESULT = "________| RESULT |________";
	
	private static final String SRCSEL = "src-sel";
	private static final String RMODE = "reasoning";
	private static final String ANY23 = "any23";
	private static final String SEEALSO = "seeAlso";
//	private static final String RESULTNVARS = null;
	private static final String TIMEOUT = "timeout";

//	private QueryExecutionBenchmark _bench;
//	private Set<Variable> _joinVariables;
//	private ArrayList<Variable> _resultVars;
//	private LinkedRuleIndex<Rule> _tbox;
	
	/**
	 * 
	 */
	public LTBQEQueryConfig() {
		super();
	}

	
	public LTBQEQueryConfig(String queryID){
		super(queryID);
	}
	
	/**
	 * @param asap
	 */
	public void sourceSelection(SourceSelectionStrategy srcsel) {
		setProperty(SRCSEL, srcsel.toString());
	}

	/**
	 * @param off
	 */
	public void reasoningMode(REASONING_MODE rMode) {
		setProperty(RMODE, rMode.toString());
	}
	
	
//	public String oneLineSummary(){
//		StringBuffer sb = new StringBuffer();
//		sb.append(getBenchmark().oneLineSummary(" ")).append(" ").append(_srcsel).append(" ").append(_rMode).append(" ").append(_followSeeAlso).append(" ").append(_any23);
//		
//		return sb.toString();
//	}
//	/* (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	@Override
//	public String toString() {
//		StringBuffer sb = new StringBuffer();
//		sb.append(RESULT).append("\n");
//		sb.append(" #results: ").append(_resSize).append("\n");
//		sb.append(" time: ").append(_time).append(" ms \n");
//		sb.append(QUERY).append("\n");
//		sb.append(getQuery()).append("\n");
//		sb.append(CONFIG).append("\n");
//		sb.append(" SourceSelection: ").append(_srcsel).append("\n");
//		sb.append(" ReasoningMode: ").append(_rMode).append("\n");
//		sb.append(" Follow rdfs:seeAlso: ").append(_followSeeAlso).append("\n");
//		sb.append(" Enable Any23 parsing: ").append(_any23).append("\n");
//		if(_outputFile!=null)
//			sb.append(" Result-file: ").append(_outputFile).append("\n");
//		if(_bench!=null)
//			sb.append(_bench.toString());
//		sb.append(HLINE).append("\n");
//		return sb.toString();
//	}

		/**
	 * @return
	 */
	public REASONING_MODE getReasoningMode() {
		return getReasoningMode(getProperty(RMODE));
	}

	/**
	 * @return
	 */
	public SourceSelectionStrategy getSourceSelection() {
		return getSourceSelectionStrategy(getProperty(SRCSEL));
	}

//	public Set<Variable> getJoinVariable() {
//		return _joinVariables; 
//	}

	
	/**
	 * @return
	 */
	public char[] printLineSummary() {
		return null;
	}

//	/**
//	 * @return
//	 */
//	public LinkedRuleIndex<Rule> getTbox() {
//		return _tbox;
//	}
//	
//	public void setTbox(LinkedRuleIndex<Rule> linkedRuleIndex) {
//		_tbox = linkedRuleIndex;
//	}

	
	/**
	 * @param seeAlso
	 */
	public void setFollowSeeAlso(Boolean seeAlso) {
		setProperty(SEEALSO,seeAlso.toString());
		
	}
	
	/**
	 * @param seeAlso
	 */
	public boolean followSeeAlso() {
		String followValue =  getProperty(SEEALSO);
		Boolean follow =Boolean.parseBoolean(followValue); 
		
		return follow;
	}

	
	/**
	 * @param query2 
	 * @return
	 */
	public static LTBQEQueryConfig defaultQueryConfig(String query) {
		//query config
		LTBQEQueryConfig qc = new LTBQEQueryConfig(query);
		qc.sourceSelection(QueryBasedSourceSelectionStrategies.SMART);
		qc.reasoningMode(REASONING_MODE.OFF);
//		qc.setTbox(null);
		qc.setFollowSeeAlso(false);
//		qc.queryBenchmark(null);
		
		return qc;
	}

	
	/**
	 * @param any23
	 */
	public void setEnableAny23(Boolean any23) {
		setProperty(ANY23,any23.toString());
	}
	
	public boolean enableAny23(){
		return Boolean.getBoolean(getProperty(ANY23));
	}
	
	
	public static LTBQEQueryConfig parseQueryConfig(CommandLine cmd, File queryFile) throws IOException{
		//TBOX
		String fileID = queryFile.getName();
		String query = Utils.readFileContent(queryFile);

		SourceSelectionStrategy  srcSelStrat = getSourceSelectionStrategy(CLIObject.getOptionValue(cmd, LIDAQ_ARGUMENTS.PARAM_SOURCE_SELECTION, QueryBasedSourceSelectionStrategies.SMART.toString()));
		fileID+="_"+srcSelStrat.toString();
		REASONING_MODE rMode = getReasoningMode(CLIObject.getOptionValue(cmd, LIDAQ_ARGUMENTS.PARAM_REASONING, REASONING_MODE.OFF.name()));
		fileID+="_"+rMode.name();

		boolean seeAlso = cmd.hasOption(LIDAQ_ARGUMENTS.PARAM_FOLLOW_SEEALSO);
		boolean any23 = cmd.hasOption(LIDAQ_ARGUMENTS.PARAM_ENABLE_ANY23);
		
		int timeout;
		 try{
			 timeout= Integer.valueOf(CLIObject.getOptionValue(cmd, LIDAQ_ARGUMENTS.PARAM_TIMEOUT, "-1"));
		 }catch(Exception e){
			 e.printStackTrace();
			 timeout=-1;
		 }
		//query config
		LTBQEQueryConfig qc = new LTBQEQueryConfig(fileID);
		qc.setQuery(query);
		qc.sourceSelection(srcSelStrat);
		qc.reasoningMode(rMode);
		qc.setFollowSeeAlso(seeAlso);
		qc.setEnableAny23(any23);
		qc.setTimeout(timeout);
		//do we benchmark
//		QueryExecutionBenchmark b =  null;
//		if(CLIObject.hasOption(cmd,ARGUMENTS.PARAM_BENCHMARK) || CLIObject.hasOption(cmd,ARGUMENTS.PARAM_BENCHDIR)){
//			b = new QueryExecutionBenchmark(fileID,query);
//			b.setBenchDir(new File( CLIObject.getOptionValue(cmd,ARGUMENTS.PARAM_BENCHDIR)));
//		}
//		qc.queryBenchmark(b);
		
		
		return qc;
	}

	public void setTimeout(int timeout) {
		setProperty(TIMEOUT,""+timeout);
	}

	public Integer getTimeout() {
		String timeout = getProperty(TIMEOUT);
		if(timeout==null) return -1;
		try{
			return Integer.valueOf(timeout);
		}catch(NumberFormatException nfe){
			nfe.printStackTrace();
			return -1;
		}
	}

	/**
	 * @param optionValue
	 * @return
	 */
	public static ResultSetFormat parseResultFormat(String format) {
		ResultSetFormat s = OutputFormats.lookup(format);
		if(s == null)
			throw new RuntimeException("Unrecognized output format: "+format);
		return s;
	}

	/**
	 * @param optionObject
	 * @return
	 */
	public static REASONING_MODE getReasoningMode(String rMode) {
		for(REASONING_MODE r: REASONING_MODE.values()){
			if(rMode.equalsIgnoreCase(r.toString())) return r;
		}
		return REASONING_MODE.OFF;
	}

	/**
	 * @param optionObject
	 * @return
	 */
	public static SourceSelectionStrategy getSourceSelectionStrategy(String srcSelStrat) {
		for(SourceSelectionStrategy s: BasicSourceSelectionStrategies.values()){
			if(srcSelStrat.equalsIgnoreCase(s.toString())) return s;
		}
		for(SourceSelectionStrategy s: QueryBasedSourceSelectionStrategies.values()){
			if(srcSelStrat.equalsIgnoreCase(s.toString())) return s;
		}
		return QueryBasedSourceSelectionStrategies.SMART;
	}
}