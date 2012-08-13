/**
 *
 */
package ie.deri.urq.lidaq.benchmark;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.stats.Count;

import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.algebra.Op;


/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 8, 2010
 */
public class QueryExecutionBenchmark extends Benchmark {
	public static final String QUERY_ID = "query_id";
	public static final String QUERY = "sparql";
	private static final String BENCH_DIR = "bench_dir";
	private static final String OP_TREE = "op_tree";
	public static final String TOTAL_RESULTS = "total_results";
	public static final String TOTAL_TIME = "total_time";
	public static final String FIRST_RESULT = "first_result";
	
    private final  Map<String,List<String>> _om = new HashMap<String,List<String>>();
    private final  List<String> _keyOrder = new ArrayList<String>();
    {
    	_keyOrder.add(QUERY_ID);
    	_keyOrder.add(QUERY);
    	_keyOrder.add(BENCH_DIR);
    	_keyOrder.add(OP_TREE);
    	_keyOrder.add(TOTAL_RESULTS);
    	_keyOrder.add(TOTAL_TIME);
    	_keyOrder.add(FIRST_RESULT);
    	_om.put(QueryExecutionBenchmark.class.getSimpleName(), _keyOrder);
    }
	
	private static final long serialVersionUID = 1L;
	public static final String LAST_RESULT = "last_result";
	private static final String RESULTOUT = "result-output";
	
	private Count<String> _iterResults;
	private TreeMap<String, Node[]> _iterKeys;
	private int _termsPerColumns;
	private int _termsTotal;
	
	

	public static List<String> summaryOrder = new ArrayList<String>();
	static{
		summaryOrder.add(QUERY_ID);
		summaryOrder.add(TOTAL_RESULTS);
		summaryOrder.add(TOTAL_TIME);
		summaryOrder.add(FIRST_RESULT);
//		summaryOrder.add(LAST_RESULT);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_2XX_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_3XX_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_4XX_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_5XX_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_6XX_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_TBOX_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_2XX_TBOX_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_3XX_TBOX_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_4XX_TBOX_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_5XX_TBOX_LOOKUPS);
		summaryOrder.add(SourceLookupBenchmark.TOTAL_6XX_TBOX_LOOKUPS);
		summaryOrder.add(WebRepositoryBenchmark.CACHE_SIZE);
		summaryOrder.add(WebRepositoryBenchmark.RETRIEVED);
		summaryOrder.add(WebRepositoryBenchmark.TBOX_CACHE_SIZE);
		summaryOrder.add(ReasonerBenchmark.INFERED_STMTS);
	}

	public void restoreConfig(File f) throws FileNotFoundException{
		Scanner s = new Scanner(f);
		while(s.hasNextLine()){
			String [] tt = s.nextLine().split(" = ");
			if(tt.length==2 && summaryOrder.contains(tt[0])){
				setProperty(tt[0], tt[1]);
			}
		}
	}
	
	
	public static String oneLineSummaryHeader(){
		StringBuffer sb = new  StringBuffer("#id");
		int count =1;
		for(String s: summaryOrder){
			sb.append(",").append(s).append("(").append(count++).append(")");
		}
		return sb.toString();
	}
	public String oneLineSummary(String sep){
		StringBuffer sb = new  StringBuffer();
		for(String s: summaryOrder){
			sb.append(sep).append(getProperty(s));
		}
		return sb.toString();
	}

	public QueryExecutionBenchmark(){
		this("","");
	}
	/**
	 * 
	 */
	public QueryExecutionBenchmark(final String id, final String query) {
		super();
		setProperty(QUERY_ID,id);
		setProperty(QUERY,query);
		_iterKeys = new TreeMap<String, Node[]>();
		_iterResults = new Count<String>();
		
	}

	/**
	 * @param logDir
	 */
	public void setBenchDir(File benchDir) {
		setProperty(BENCH_DIR, benchDir.getAbsolutePath());
	}

	
	public File getBenchDir() {
		return new File(getProperty(BENCH_DIR));
	}

	/**
	 * @param op
	 */
	public void addOperatorTree(Op op) {
		setProperty(OP_TREE, op.toString());
	}

	public void initOperator(String key, String pattern){
		_keyOrder.add(key);
		_keyOrder.add(key+"-VarBind");
		_keyOrder.add(key+"-SolBind");
		setProperty(key, pattern);
	}
	
	/**
	 * @param string
	 * @param size
	 */
	public void addQueryIteratorVarBind(String key, int size) {
		setProperty(key+"-VarBind", ""+size);

	}
	public void addQueryIteratorSolBind(String key, int size) {
		setProperty(key+"-SolBind", ""+size);

	}
	/**
	 * @param string
	 * @param key
	 */
	public void addQueryIteratorKey(String s_key, Node[] key) {
		_iterKeys.put(s_key, key);

	}
	/**
	 * @param file
	 */
	public void setResultOutput(File file) {
		setProperty(RESULTOUT,file.getAbsolutePath());

	}
//	/**
//	 * @return
//	 */
//	public File getCacheFile() {
//		if(this.get(BENCH_DIR) == null) return null;
//		if(!((File)this.get(BENCH_DIR)).exists()) ((File)this.get(BENCH_DIR)).mkdirs();
//		
//		File cache = new File((File)this.get(BENCH_DIR),"cache.nq");
//		return cache;
//	}
//	/**
//	 * @return
//	 */
//	public File getAccesLog() {
//		if(this.get(BENCH_DIR) == null) return null;
//		return new File((File)this.get(BENCH_DIR),"access.log");
//		
//	}
	
	/* (non-Javadoc)
	 * @see ie.deri.urq.lidaq.benchmark.Benchmark#getKeyOrder()
	 */
	@Override
	public Map<String, List<String>> getKeyOrder() {
		return _om;
	}
	
	public void loadBenchmark(File file) throws Exception{
		FileInputStream fis = new FileInputStream(file);
		load(fis);
		fis.close();
	}
	
	public void storeBenchmark(File file) throws IOException {
		PrintStream ps = new PrintStream(file);
		store(ps, "");
		ps.close();
	}
	
	public void setFirstResult(long timeFirstResult) {
		setProperty(FIRST_RESULT, ""+timeFirstResult);
		
	}
	public void setLastResult(long timeLastResult) {
		setProperty(LAST_RESULT, ""+timeLastResult);
		
	}
	public void setTotatlTime(long time) {
		setProperty(TOTAL_TIME, ""+time);
		
	}
	public void setResultSize(int rowNumber) {
		setProperty(TOTAL_RESULTS, ""+rowNumber);
	}
	
	public Long getFirstResult() {
		return Long.valueOf(getProperty(FIRST_RESULT));
		
	}
	public Long getLastResult() {
		return Long.valueOf(getProperty(LAST_RESULT));
		
	}
	public Long getTotatlTime() {
		return Long.valueOf(getProperty(TOTAL_TIME));
		
	}
	public Integer getResultSize() {
		return Integer.valueOf(getProperty(TOTAL_RESULTS));
		
	}

	public void setQueryID(String queryID) {
		setProperty(QUERY_ID,queryID);
	}

	public void setQuery(String query) {
		setProperty(QUERY,query);
	}


	public String getQueryID() {
		return getProperty(QUERY_ID);
	}

	public String getQuery() {
		return getProperty(QUERY);
	}
	
	public String getDynQuery() {
		return getProperty("dynamicQuery");
	}

	public String getStaticQuery() {
		return getProperty("staticQuery");
	}


	public void loadResults(File result) throws FileNotFoundException {
		Scanner s = new Scanner(result) ;
		
		HashSet<String>[] counts=new  HashSet[0] ; 
		boolean firstLine = true;
		String []tt;
		while(s.hasNextLine()){
			tt = s.nextLine().split("\t");
			if(firstLine){
				firstLine = false;
				counts = new HashSet[tt.length];
				for(int i=0; i < counts.length;i++){
					counts[i] = new HashSet<String>();
				}
			}else{
				if(tt.length != counts.length){
					System.err.println("Problem with tabs in "+result);
					continue;
				}
				for(int i=0; i < counts.length;i++){
					counts[i].add(tt[i]);
				}
			}
		}
		HashSet<String> all = new HashSet<String>();
		for(int i=0; i < counts.length;i++){
			all.addAll(counts[i]);
			_termsPerColumns+=counts[i].size();
		}
		_termsTotal = all.size();
	}
	public int getTermsPerColumns(){
		return _termsPerColumns;
	}
	
	public int getTermsTotal(){
		return _termsTotal;
	}
}
