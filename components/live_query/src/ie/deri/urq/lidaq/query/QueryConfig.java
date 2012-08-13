/**
 *
 */
package ie.deri.urq.lidaq.query;


import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.repos.QueryBasedSourceSelectionStrategies;
import ie.deri.urq.lidaq.repos.SourceSelectionStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.saorr.rules.LinkedRuleIndex;
import org.semanticweb.saorr.rules.Rule;
import org.semanticweb.yars.nx.Variable;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jan 10, 2011
 */
public class QueryConfig{
	/**
	 * DEFAULT
	 */
	private final static String QUERY = "query";
	private final static String QUERY_ID = "query_id";
	
	
	
	private final Properties _props;
	
	private QueryExecutionBenchmark _qeb;
	
	public QueryConfig() {
		this(""+System.currentTimeMillis());
	}
	public QueryConfig(String queryID){
		_props = new Properties();
		_props.setProperty(QUERY_ID,queryID);
	}
	
	protected void setProperty(String key, String value){
		_props.setProperty(key, value);
	}
	protected String getProperty(String key){
		return _props.getProperty(key);
	}
	
	
	/**
	 * Mandatory property
	 * @param queryString
	 */
	public void setQuery(String queryString) {
		_props.setProperty(QueryConfig.QUERY, queryString);
	}
	
	public String getQuery() {
		return _props.getProperty(QueryConfig.QUERY);
	}
	
	public String getQueryID(){
		return _props.getProperty(QUERY_ID);
	}
	
	public void storeConfig(File file) throws IOException {
		PrintStream ps = new PrintStream(file);
		_props.store(ps, "");
		ps.close();
	}
	
	public void loadConfig(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		_props.load(fis);
		fis.close();
	}
	
	public void setQueryExecutionBenchmark(QueryExecutionBenchmark qeb){
		_qeb = qeb;
		_qeb.setQueryID(getQueryID());
		_qeb.setQuery(getQuery());
	}
	
	public QueryExecutionBenchmark getQueryExecutionBenchmark(){
		return _qeb;
	}
}