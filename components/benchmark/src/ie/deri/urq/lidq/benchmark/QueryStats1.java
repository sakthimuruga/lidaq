package ie.deri.urq.lidq.benchmark;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.benchmark.ReasonerBenchmark;
import ie.deri.urq.lidaq.benchmark.SourceLookupBenchmark;
import ie.deri.urq.lidaq.benchmark.WebRepositoryBenchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.icu.text.DecimalFormat;

public class QueryStats1{

	public static Map<String,String>nameMapping = new HashMap<String, String>();
	static{
		nameMapping.put("smart", "\\lbase");
		nameMapping.put("base", "\\lcore");
		nameMapping.put("seealso", "\\lseealso");
		nameMapping.put("sameas", "\\lsameas");
		nameMapping.put("rdfs", "\\lrdfss");
		nameMapping.put("rdfsd", "\\lrdfsd");
		nameMapping.put("rdfsc", "\\lrdfse");
		
		nameMapping.put("all", "\\lcombs");
		nameMapping.put("alldir", "\\lcombd");
		nameMapping.put("cl", "\\lcombe");
		
	}
	
	
	public Map<File,Integer[]> setupValues = new TreeMap<File, Integer[]>(new Comparator<File>() {
		public int compare(File o1, File o2) {
			int idx1 = 0,idx2 = 0;
			for(String s: names){
				if(o1.getName().contains(s)){
					idx1 = names.indexOf(s);
				}
				if(o2.getName().contains(s)){
					idx2 = names.indexOf(s);
				}
			}
//			System.out.println(idx1+" "+idx2);
			return idx1-idx2;
		}
	});
	private String _queryID;
	private boolean error =false;
	private int _setups;
	String [] fixes;

	private static ArrayList<String>names = new ArrayList<String>();
	static {
		names.add(".1base");
		names.add(".2smart");
		names.add(".3seealso");
		names.add(".4sameas");
		names.add(".5rdfs");
		names.add(".6rdfsd");
		names.add(".7rdfsc");
		names.add(".8all");
		names.add(".9alldir");
//		names.add(".9squin");
		names.add(".10cl");
	}

	/**
	 * @param ww
	 * @param acceptedQueries 
	 * @param length 
	 */
	public QueryStats1(String ww) {
		_queryID = ww;
	}

	public String getQueryID(){
		return _queryID;
	}

	public String getStatusArrayHeaderString() {
		String s = _queryID;
		for(Entry<File,Integer[]> ent : setupValues.entrySet()){
			s+=" "+ent.getKey().getName();
		}
		return s;
	}

	private HashSet<String>[][][] urlSet = null;
	private int baseIdx;
	private String _query;
	
	private Integer[] baseValues;
	private File baseFile;
	private HashMap<File, Integer> _termsPerColumns = new HashMap<File, Integer>();
	private  HashMap<File, Integer> _termsTotal = new HashMap<File, Integer>();

	public Integer[] getBaseValue(){
		return baseValues;
	}
	
	private String analyse(int i, HashSet<String>[][] cur,
			HashSet<String>[][][] set, HashSet<String> all,
			HashSet<String>[] base, Map<File, Integer[]>[] confs, Map<File,Integer[]> map, int baseIdx1, int b2) {

		String s = null;
		int b=0;
		int next = i+1;
		String res = "";
		boolean stable = false;
		for(Entry<File,Integer[]> ent : confs[i].entrySet()){
			if(baseIdx1!=i || (baseIdx1==i && b2 == b)){
				cur[i]= set[i][b];
				if(cur[i]!=null){
					map.put(ent.getKey(), ent.getValue());
//					if(map.size()!=next){
//						System.out.println(i+" "+map.size()+" "+next);
//					}
					if(i!=fixes.length-1){
						String t = analyse(next,cur,set,all,base,confs,map,baseIdx1,b2);
						if( t != null){
							res += t+""+b;
							break;
						}else{
							map.remove(ent.getKey());
							res=null;
						}
					}else{
						if(analyseStable(cur, all, base,map)){
							res =""+b;
							stable = true;
							break;
						}
						else{
							res = null;
							map.remove(ent.getKey());
						}
					}

				}
			}
			b++;
		}
		//		if(stable)
		//			System.out.println(res);

		return res;
	}

	private HashSet<String>[] getURLsSets(File qu) {
		HashSet<String>[] set = new HashSet[2];
		set[0]= new HashSet<String>();
		set[1]= new HashSet<String>();

		for(File f: qu.listFiles()){
			if(f.getName().startsWith(_queryID)){
				Scanner s;
				try {
					String fname = f.getName();
					fname = fname.substring(0,fname.indexOf(".sparql")+7);

					for(File fs: f.getParentFile().listFiles()){
						if(fs.getName().endsWith("-abox.access.log") && fs.getName().startsWith(fname)){
							s = new Scanner(fs);
							while(s.hasNextLine()){
								String []tt=s.nextLine().split(" ");
								String url = tt[3];
								String resp = tt[5];
								if(resp.startsWith("5") || resp.startsWith("6")|| resp.startsWith("4"))
									set[0].add(url);
								else if(resp.startsWith("2")||resp.startsWith("3"))
									set[1].add(url);
							}
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return set;
	}
	private HashMap<String,String> getURLMap(File qu) {
		HashMap<String,String> set = new HashMap<String, String>();
		
		for(File f: qu.listFiles()){
			if(f.getName().startsWith(_queryID)){
				Scanner s;
				try {
					String fname = f.getName();
					fname = fname.substring(0,fname.indexOf(".sparql")+7);

					for(File fs: f.getParentFile().listFiles()){
						if(fs.getName().endsWith("-abox.access.log") && fs.getName().startsWith(fname)){
							s = new Scanner(fs);
							while(s.hasNextLine()){
								String []tt=s.nextLine().split(" ");
								String url = tt[3];
								String resp = tt[5];
								
								set.put(url, resp);
							}
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return set;
	}


	/**
	 * @return
	 */
	public String csvHeader() {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		int c =0;
		for(String s : QueryExecutionBenchmark.summaryOrder){
			if(c!=0){
				sb1.append(",").append(s);
				for(File t: setupValues.keySet()){
					sb1.append(",");
					sb2.append(",").append(t.getName());
				}
			}else
				sb1.append("Query");
			c++;
		}
		return sb1.toString()+"\n"+sb2.toString();
	}

	/**
	 * @return
	 */
	public String csvLine() {
		StringBuilder sb1 = new StringBuilder(_queryID);

		for(int i=0; i< QueryExecutionBenchmark.summaryOrder.size()-1;i++){
			for(File t: setupValues.keySet()){
				sb1.append(",").append(setupValues.get(t)[i]);
			}
		}
		return sb1.toString();
	}


	/**
	 * 
	 * @param qu
	 * @param tt
	 * @return - status code see getStatus method
	 */
	//	public int analyse(File qu, String[] tt){
	//		Integer [] v = new Integer[QueryExecutionBenchmark.summaryOrder.size()-1];
	//		for(int i=0; i< QueryExecutionBenchmark.summaryOrder.size()-1; i++){
	//			try{
	//				if(tt[i+1] !=null){
	//					v[i] = Integer.valueOf(tt[i+1]);
	//				}
	//				else if(i!=2) v[i]= -1;
	//				else v[i]=0;
	//			}catch(Exception e){
	//				v[i] = 0;	
	//			}
	//		}
	//		setupValues.put(qu, v);
	//		int status = getStatus(qu);
	//
	//
	//		return status;
	//	}
	public int add(File qu, QueryExecutionBenchmark qeb, boolean base) {
		Integer [] v = new Integer[QueryExecutionBenchmark.summaryOrder.size()-1];
		for(int i=0; i< QueryExecutionBenchmark.summaryOrder.size()-1; i++){
			try{
				if(qeb.getProperty(QueryExecutionBenchmark.summaryOrder.get(i+1)) != null){
					v[i] = Integer.valueOf(qeb.getProperty(QueryExecutionBenchmark.summaryOrder.get(i+1)));
				}
				else if(i!=2) v[i]= -1;
				else v[i]=0;
			}catch(Exception e){
				v[i] = 0;	
			}
		}

		setupValues.put(qu, v);
		if(base){
			baseValues = v;
			baseFile= qu;
		}
		
		_termsPerColumns.put(qu,qeb.getTermsPerColumns());
		_termsTotal.put(qu,qeb.getTermsTotal());
		return 1;
	}



	/**
	 * 
	 * @param querySetup
	 * @param tt
	 * @return - status code about the current line 
	 *   0 - means everything was ok and non-empty result
	 *   1 - means everything was ok, but empty results
	 *   2 - null as result value, which indicates some error
	 *   3 - we have an unstable abox query 
	 *   4 - we have an unstable tbox query
	 *   5 - multiple failures
	 */
	//	STABILITYCODE getStatus(File querySetup) {
	//
	//		Integer [] v = setupValues.get(querySetup);
	//
	////		//check the number of lookups 
	////		int total = v[3];
	////		boolean ignore = false;
	////		for(int i=4; i<9;i++){
	////			total-=v[i];
	////			if(total <0 && i==8){
	////				
	////				//				ignore = true;
	////			}
	////		}
	//
	//		if(v[0]==0){
	//			NON_EMPTY_RESULT
	//		}
	//		
	//		int resp = 0;
	//		
	//			if(resp == 0){
	//				resp = 1;
	//			}else if(resp!=3){resp =5;}
	//			error = true;
	//		}	
	//		if(v[0]==-1){
	//			if(resp == 0 ){
	//				resp = 2;
	//			}else if(resp!=3){resp =5;}
	//			error = true;
	//		}
	//
	//		if(v[7] !=0 || (!ignore && v[8] !=0)){
	//			if(resp == 0 || resp == 3){
	//				resp = 3;
	//			}else if(resp!=3){resp = 5;}
	//		}
	//		if(v[13] !=0 || v[14] !=0){
	//			if(resp == 0 || resp ==4){
	//				resp = 4;
	//			}else if(resp!=3){resp =5;}
	//		}
	//		//		if(resp !=3 && ignore)System.out.println(resp+" "+Arrays.toString(v));
	//		//		if((resp==5 || resp==3) && ignore)System.out.println(Arrays.toString(v));
	//
	//		return resp;
	//	}


	public int entries() {
		return setupValues.size();
	}

	public Map<String, String> getCoreURIs(ArrayList<String> acceptedQueries) {
		Map<String,String> urlSets = new HashMap<String, String>();
		for(File ent: setupValues.keySet()){
			for(String s:acceptedQueries ){
				if(ent.getName().endsWith(s)){
					for(Entry<String,String> entURI: getURLMap(ent).entrySet()){
						if(!urlSets.containsKey(entURI.getKey())){
							urlSets.put(entURI.getKey(), entURI.getValue());
						}
					}
					
				}
			}
		}
		return urlSets;
	}

	public boolean isStable(ArrayList<String> acceptedQueries) {
		
//		return true;
		
		Map<File,HashSet<String>[]> urlSets = new HashMap<File, HashSet<String>[]>();
		for(File ent: setupValues.keySet()){
			for(String s:acceptedQueries ){
				if(ent.getName().endsWith(s)){
					HashSet<String>[] urlSet= getURLsSets(ent);
					urlSets.put(ent, urlSet);
				}
			}
		}
		if(urlSets.size() != acceptedQueries.size()) return false;
		HashSet<String>[] base = urlSets.remove(baseFile);

		String setup="";
		boolean stable = true;
		for(Entry<File,HashSet<String>[]> urlSetEntry: urlSets.entrySet()){
			for(String s: base[0]){
				if(!urlSetEntry.getValue()[0].contains(s)){
					//					System.out.println(urlSetEntry.getKey().getName()+" "+ _queryID+" 0 "+s);
					stable=false;
					break;
				}
			}
			for(String s: base[1]){
				if(!urlSetEntry.getValue()[1].contains(s)){
					//					System.out.println(urlSetEntry.getKey().getName()+" "+ _queryID+" 1 "+s);
					stable=false;
					break;
				}
			}
			if(!stable){
				setup+=""+urlSetEntry.getKey().getName()+",";
				//				break;
			}
		}
		if(stable){
			for(File ent: setupValues.keySet()){
				for(String s:acceptedQueries ){
					Integer[]v = setupValues.get(ent);
					int i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
					if(v[i] > 7200000){
						stable = false;
						System.out.println("TIMEOUT: "+getQueryID());
					}
				}
			}
		}
		
//		return true;
		return stable;
	}

	public boolean analyseStable(HashSet<String>[][] set,
			HashSet<String> all,
			HashSet<String>[] base, Map<File,Integer[]> map){
		//in theory , all the URIs in smart should appear 
		boolean stable = true;
		for(int i=0;i<fixes.length;i++){
			for(String s : all){
				if(base[0].contains(s) && !set[i][0].contains(s)){
					stable = false;
				}
				if(base[1].contains(s) && !set[i][1].contains(s)){
					stable = false;
				}
			}
		}
		boolean stable2 = stable;
		if(stable){
			int [] results = new int[fixes.length];

			if(map.size() != results.length){
				System.out.println("Non matching map size for "+_queryID +" "+map.size());
				return false;
			}
			int b=0;

			/**
			 * check for empty results 
			 **/
			for(Integer[] ent : map.values()){
				results[b++]= ent[0];
				if(ent[0]==0){ 
					stable2=false;
				}
			}
			for( int i=0;i< fixes.length;i++){
				if(i!=2 && results[2]>results[i])
					stable2 = false;
			}
		}
		if(stable2){
			boolean nonNull= false;
			boolean print =false;
			for(Integer[] ent : map.values()){
				if(ent[0]==0 && nonNull){
					print = true;
				}else if(ent[0]!=0){
					nonNull=true;
				}
			}
			if(print){
				System.out.println("Some empty results: "+_queryID);

				//	for( int i=0;i< 10;i++){
				//		if(i!=2 && results[2]>results[i])
				//			stable2 = false;
				//		}
				//	for(Integer[] ent : map.values()){
				//		System.out.println(Arrays.toString(ent));
				//	}
			}
		}

		return stable2;
	}

	public HashMap<String, Integer[]> getInconsistentURIs() {
		HashSet<String>[][] urlSetLoc = new HashSet[setupValues.size()][2];
		HashSet<String>[] all = new HashSet[2];
		all[0]= new HashSet<String>();
		all[1]= new HashSet<String>();
		int a =0;
		for(Entry<File,Integer[]> ent : setupValues.entrySet()){
			urlSetLoc[a]= getURLsSets(ent.getKey());
			all[0].addAll(urlSetLoc[a][0]);
			all[1].addAll(urlSetLoc[a][1]);
			a++;
		}
		HashMap<String, Integer[]> map = new HashMap<String, Integer[]>();
		for(String s: urlSetLoc[2][0]){
			if(map.containsKey(s)) continue;
			Integer [] inte = new Integer[urlSetLoc.length];
			for(int j =0; j < urlSetLoc.length; j++){
				if(urlSetLoc[j][1].contains(s)){
					inte[j]=1;
				}
				else if(urlSetLoc[j][0].contains(s)){
					inte[j]=0;
				}
				else 
					inte[j]=-1;
			}
			map.put(s, inte);
		}
		for(String s: urlSetLoc[2][1]){
			if(map.containsKey(s)) continue;
			Integer [] inte = new Integer[urlSetLoc.length];
			for(int j =0; j < urlSetLoc.length; j++){
				if(urlSetLoc[j][0].contains(s)){
					inte[j]=1;
				}
				else if(urlSetLoc[j][1].contains(s)){
					inte[j]=0;
				}
				else 
					inte[j]=-1;
			}
			map.put(s, inte);
		}

		return map;
	}

	public void setQuery(String query) {
		_query = query;

	}

	public String getQuery(){
		return _query;
	}
	public static DecimalFormat twoDForm = new DecimalFormat("#,###.##");
	public static DecimalFormat oneDForm = new DecimalFormat("#,###.#");
	public void printTexTable(File outDir) {
		try{
			StringBuilder sbTable = new StringBuilder();
			StringBuilder sb = new StringBuilder();

			String id = _queryID.replace(".sparql", "");
			
			//				summaryOrder.add(QUERY_ID);
			//				summaryOrder.add(TOTAL_RESULTS);
			//				summaryOrder.add(TOTAL_TIME);
			//				summaryOrder.add(FIRST_RESULT);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_2XX_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_3XX_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_4XX_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_5XX_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_6XX_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_TBOX_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_2XX_TBOX_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_3XX_TBOX_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_4XX_TBOX_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_5XX_TBOX_LOOKUPS);
			//				summaryOrder.add(SourceLookupBenchmark.TOTAL_6XX_TBOX_LOOKUPS);
			//				summaryOrder.add(WebRepositoryBenchmark.CACHE_SIZE);
			//				summaryOrder.add(WebRepositoryBenchmark.TBOX_CACHE_SIZE);
			//				summaryOrder.add(ReasonerBenchmark.INFERED_STMTS);
			
//			sbTable.append("\\begin{table}[htc!]\n\\tabcolsep 0.15cm\n\\small\n")
//			.append("\\centering\n\\begin{tabular}{@{}rp{0.8cm}p{0.8cm}p{0.8cm}p{0.7cm}p{0.9cm}p{0.8cm}@{}}\n\\toprule\n")
//			.append("\\textbf{Setup} &\\textbf{Results} &\\textbf{Time (sec)} &\\textbf{First (sec)} &\\textbf{HTTP}&\\textbf{Retrieved (k)} &\\textbf{Inferred (k)} \\\\ \\midrule\n");
			
			File f= new File(outDir,_queryID+".data.tex");
			PrintWriter pw = new PrintWriter(f);
			for(Entry<File, Integer[]> ent: setupValues.entrySet()){
				Integer [] values = ent.getValue();
				String name = ent.getKey().getName();
				name = name.substring(name.lastIndexOf(".")+1);
				name = name.replaceAll("[0-9]","");
				
				sb.append("").append(nameMapping.get(name));
				sb.append("& ").append(twoDForm.format(_termsPerColumns.get(ent.getKey())));
				
				int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
				sb.append("& ").append(twoDForm.format(values[i]));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
				sb.append("& ").append(oneDForm.format(values[i]/1000D));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.FIRST_RESULT)-1;
				sb.append("& ").append(oneDForm.format(values[i]/1000D));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_LOOKUPS)-1;
				int totalLookups =values[i];
				
				i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_TBOX_LOOKUPS)-1;
				totalLookups +=values[i];
				sb.append("& ").append(twoDForm.format(totalLookups));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(WebRepositoryBenchmark.RETRIEVED)-1;
				sb.append("& ").append(twoDForm.format(values[i]));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(ReasonerBenchmark.INFERED_STMTS)-1;
				if(values[i]>0)
					sb.append("& ").append(twoDForm.format(values[i]));
				else
					sb.append("& ").append("\\noresult ");

				sb.append("\\\\ \n");
			}
			pw.println(sb.toString());
			pw.close();
			sbTable.append("\\input{").append(f).append("}\n");
//			System.out.print("Printed "+f);
			sbTable.append("\\bottomrule\n")
			.append("\\end{tabular}\n")
			.append("\\vspace{-1em}\n\\caption{\\label{tab:").append(id).append("}Benchmark results for FedBench query \\textbf{"+id+"}.}\n")
			.append("\\end{table}");

			f= new File(outDir,_queryID+".tex");
			pw = new PrintWriter(f);
			pw.println(sbTable.toString());
			pw.close();
			
//			System.out.println(" and "+f);
			//			String name = setupValues.keySet().iterator().next();
			//			name= name.substring(0,name.indexOf("."));
			//
			//			for(String s: names){
			//				//			System.out.println(name+s);
			//				DescriptiveStatistics[] desc = setupValues.get((name+s));
			//
			//				if(s.contains("rdfs")){
			//					sb.append("\n").append("$\\rho${\\tt DF}");
			//				}else
			//					sb.append("\n").append("{\\tt ").append(s.substring(2)).append("}");
			//				if(desc!=null){
			//					for(int i=0; i< desc.length;i++){
			//						DescriptiveStatistics d = desc[i];
			//						if(i == 4 || i==5||i==6||i==7|i==8) continue; // abox lookups
			//						if(i==9||i == 10 || i==11||i==12||i==13|i==14) continue; // tbox lookups
			//						if(i==16) continue;
			//						if(i==1 || i==2 || i==15 || i==16|| i==17)
			//							sb.append("& ").append(twoDForm.format(d.getMean()/1000D)).append("&($\\pm$").append(twoDForm.format(d.getStandardDeviation()/1000D)).append(")");
			//						else
			//							sb.append("& ").append(twoDForm.format(d.getMean())).append("&($\\pm$").append(twoDForm.format(d.getStandardDeviation())).append(")");
			//					}
			//				}
			//				else{sb.append("&-&-&-&-&-&-&");}
			//				sb.append("\\\\");
			//			}
			//			sb.append("\n\\hline");
			//			System.out.println(success+"/"+errors);
			//
			//			return sb.toString().replaceAll(name, "");
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public File getBaseFile() {
		return baseFile;
	}

	/**
	 * 
	 * @return the number of results in the baseline approach
	 */
	public boolean hasZeroResult() {
		int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
		return baseValues[i]==0;
	}

	public Set<String> getImprovedSetups() {
		Set<String> setups = new HashSet<String>();
		
		
		int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
		for(Entry<File, Integer[]> ent: setupValues.entrySet()){
			Integer [] values = ent.getValue();
			
		
			if(values[i]>baseValues[i]){
				String name = ent.getKey().getName();
				name = name.substring(name.lastIndexOf(".")+1);
				name = name.replaceAll("[0-9]","");
				setups.add(name);
			}
				
		}
		return setups;
	
		
	}
	
	
	public Integer getTimeFor(String s) {
		String res = "0";
		for(Entry<File, Integer[]> ent: setupValues.entrySet()){
			String name = ent.getKey().getName();
			
			if(!name.endsWith(s)) continue;
			
			name = name.substring(name.lastIndexOf(".")+1);
			name = name.replaceAll("[0-9]","");
			Integer [] values = ent.getValue();
			
			int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
			res = twoDForm.format(values[i]);

		}
		return Integer.valueOf(res);
	}
	
	public Integer getResultsFor(String s){
		String res = "0";
		for(Entry<File, Integer[]> ent: setupValues.entrySet()){
			String name = ent.getKey().getName();
			
			if(!name.endsWith(s)) continue;
			
			name = name.substring(name.lastIndexOf(".")+1);
			name = name.replaceAll("[0-9]","");
			Integer [] values = ent.getValue();
			
			int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
			res = twoDForm.format(values[i]);

		}
		return Integer.valueOf(res);
	}

	public double getTermCount(File key) {
		return _termsTotal.get(key);
	}

	public double getTermColumnCount(File key) {
		return _termsPerColumns.get(key);
	}

	

	
}

