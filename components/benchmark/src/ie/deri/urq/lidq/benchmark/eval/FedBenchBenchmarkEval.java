/**
 *
 */
package ie.deri.urq.lidq.benchmark.eval;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidq.benchmark.QueryStats1;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.semanticweb.yars.stats.Count;
import org.semanticweb.yars.tld.TldManager;

import com.ibm.icu.text.DecimalFormat;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 17, 2010
 */
public class FedBenchBenchmarkEval{

	private  TldManager ext;


	public static DecimalFormat twoDForm = new DecimalFormat("#.##");
	public static void main(String[] args) throws FileNotFoundException, IOException {
		File indir = new File("/Users/juum/Resources/evaluations/ldquery_reason/fedbench");
		
		File [] dirs = {
				new File(indir,"fedbench_ld.15.03.2012"),
				new File(indir,"fedbench_ld.03.03.12"),
				new File(indir,"fedbench_ld.12.03.2012"),
				new File(indir,"fedbench_ld.20.03.2012"),
				new File(indir,"fedbench_ld.27.03.2012")
		};
		
		
		HashMap<String, List<QueryStats1>>  map = new HashMap<String, List<QueryStats1>>();
		final ArrayList<String> acceptedQueries = new ArrayList<String>();
		acceptedQueries.add("base");
		acceptedQueries.add("smart");
		acceptedQueries.add("seealso");
		acceptedQueries.add("rdfs");
		acceptedQueries.add("rdfsd");
		acceptedQueries.add("rdfsc");
//		acceptedQueries.add("squin");
		acceptedQueries.add("sameas");
		acceptedQueries.add("all");
		acceptedQueries.add("alldir");
		acceptedQueries.add("cl");
		
		ArrayList<String> order = new ArrayList<String>();
		order.add("ld1.sparql");
		order.add("ld2.sparql");
		order.add("ld3.sparql");
		order.add("ld4.sparql");
		order.add("ld5.sparql");
		order.add("ld6.sparql");
		order.add("ld8.sparql");
		order.add("ld9.sparql");
		order.add("ld10.sparql");
		order.add("ld11.sparql");
		
		
		for(File file: dirs){
			List<File> files = getQueries(file);
			do{
				final File query = files.get(0);
				boolean accept = false;
				for(String s: acceptedQueries){
					if(query.getName().endsWith(s))
						accept =true;
				}
				if(!accept){
					files.remove(query);
					continue;
				}
				final String queryType = query.getName().substring(0,query.getName().indexOf("."));
				File [] allQueries = file.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						boolean hasSum = false;
						for(String s: acceptedQueries){
							if(name.endsWith(s)){ hasSum =true;}
						}
						if(hasSum)
							hasSum = name.startsWith(queryType);// && new File(dir,name+"/summary.txt").exists(); 
						if(hasSum &&!new File(dir,name+"/summary.txt").exists()) System.out.println(name +" has no summary.txt");
						return  hasSum;
					}
				});
				Arrays.sort(allQueries);
				System.err.println("|-->Analysing ("+allQueries.length+"/"+files.size()+") |==> Output in ");
				Map<String,QueryStats1> qsMap = new HashMap<String,QueryStats1>();
				for(File qu: allQueries){
					System.err.println("|---> Analysing "+qu); files.remove(qu);
					for(File bench: qu.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".bench");
						}
					})){
						String queryID = bench.getName().substring(0,bench.getName().indexOf("ql_")+2);
						
						QueryStats1 qst = qsMap.get(queryID);
						if(qst==null){ 
							qst = new QueryStats1(queryID);
							qsMap.put(queryID,qst);

						}
						QueryExecutionBenchmark qeb = new QueryExecutionBenchmark();
						qeb.load(new FileInputStream(bench));
						File result = new File(bench.getAbsolutePath().replace(".bench",".tsv"));
						qeb.loadResults(result);
						qst.add(qu, qeb, qu.getName().contains("smart"));
						qst.setQuery(qeb.getQuery());
						
						if(queryID.contains("ld11")){
							System.out.println(qu.getName()+"-> "+qst.getResultsFor(qu.getName()));
						}

					}//end single query files
				}//end different query setups
				for(Entry<String, QueryStats1>ent: qsMap.entrySet()){
					if(!map.containsKey(ent.getKey())){
						map.put(ent.getKey(),new ArrayList<QueryStats1>());
					}
					map.get(ent.getKey()).add(ent.getValue());
				}
			}while(files.size()!=0);
			
			
			
			for(String stat: acceptedQueries){
				FileWriter fw = new FileWriter(new File("/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/swj/eval/"+stat+".table.tex"));
				System.out.println(stat);
				StringBuilder header = new StringBuilder();
				StringBuilder body = new StringBuilder();
				for(String orders: order){
					System.out.println(orders);
					if(!map.containsKey(orders)) continue;
					
					header.append("&").append(orders).append("&");
					SummaryStatistics sum = new SummaryStatistics();
				
					ArrayList<Integer>values = new ArrayList<Integer>(); 
				
					for(QueryStats1 qs : map.get(orders)){
						System.out.println("  --->>"+qs.getResultsFor(stat));						
//						System.out.println(qs.getResultsFor(stat));
//						if(qs.getResultsFor(stat)>0)
						values.add(qs.getResultsFor(stat));
					}
					
					Collections.sort(values);
					if(values.size()==5){
						System.out.println();
					}
					for(int i = values.size()-1; i>=0; i--){
						if(i >= values.size()-4){
							sum.addValue(values.get(i));
						}
					}
					
					
					if(sum.getN()>0 && sum.getMax()!=0){
						while(sum.getN()!=4)
							sum.addValue(0);
						body.append("&").append(twoDForm.format(sum.getMean())).append("&\\num{+-").append(twoDForm.format(sum.getStandardDeviation())).append("}\n");
						System.out.println("   "+twoDForm.format(sum.getMean())+" +- "+twoDForm.format(sum.getStandardDeviation()));
					}else{
						System.out.println("  - ");
						body.append("&-&-\n");
					}
				}
				fw.write(header.toString());fw.write("\n");
				fw.write(body.toString());
				fw.close();
			}
		}
		for(final String stat: acceptedQueries){
			for(String orders: order){
				System.out.println(orders);
				if(!map.containsKey(orders)) continue;
			
				TreeSet<QueryStats1>orderedMap = new TreeSet<QueryStats1>(new Comparator<QueryStats1>() {
					public int compare(QueryStats1 o1, QueryStats1 o2) {
						int diff = o1.getResultsFor(stat).compareTo(o2.getResultsFor(stat));
						if(diff == 0)
							diff = o1.getTimeFor(stat).compareTo(o2.getTimeFor(stat));
						return diff;
				}});

				for(QueryStats1 qs : map.get(orders)){
					orderedMap.add(qs);
				}
			}
		}
		
		
		
		
		
		
//			do{
//				final File query = files.get(0);
//				boolean accept = false;
//				for(String s: acceptedQueries){
//					if(query.getName().endsWith(s))
//						accept =true;
//				}
//				if(!accept){
//					files.remove(query);
//					continue;
//				}
//				final String queryType = query.getName().substring(0,query.getName().indexOf("."));
//				File [] allQueries = f.listFiles(new FilenameFilter() {
//					public boolean accept(File dir, String name) {
//						boolean hasSum = false;
//						for(String s: acceptedQueries){
//							if(name.endsWith(s)){ hasSum =true;}
//						}
//						if(hasSum)
//							hasSum = name.startsWith(queryType);// && new File(dir,name+"/summary.txt").exists(); 
//						if(hasSum &&!new File(dir,name+"/summary.txt").exists()) System.out.println(name +" has no summary.txt");
//						return  hasSum;
//					}
//				});
//				Arrays.sort(allQueries);
//				System.err.println("|-->Analysing ("+allQueries.length+"/"+files.size()+") |==> Output in "+new File(outDir,queryType));
//
//				Map<String,QueryStats1> qsMap = new HashMap<String,QueryStats1>();
//				for(File qu: allQueries){
//					System.err.println("|---> Analysing "+qu); files.remove(qu);
//					for(File bench: qu.listFiles(new FilenameFilter() {
//						public boolean accept(File dir, String name) {
//							return name.endsWith(".bench");
//						}
//					})){
//						String queryID = bench.getName().substring(0,bench.getName().indexOf("ql_")+2);
//
//						QueryStats1 qst = qsMap.get(queryID);
//						if(qst==null){ 
//							qst = new QueryStats1(queryID);
//							qsMap.put(queryID,qst);
//
//						}
//						QueryExecutionBenchmark qeb = new QueryExecutionBenchmark();
//						qeb.load(new FileInputStream(bench));
//						qst.add(qu, qeb, qu.getName().contains("smart"));
//						qst.setQuery(qeb.getQuery());
//
//					}//end single query files
//				}//end different query setups
//			
//			
//			
//			
//		}
//		
//		
//		
//		
//		
//		Count<String> totalTime = new Count<String>();
//		int totalQueries=0, totalLessOneSec = 0;
//		StringBuilder summary = new StringBuilder();
//		try{		//proxy
//			ext = new TldManager();
//			if(!outDir.exists()) outDir.mkdirs();
//
//			List<File> files = getQueries(inDir); 
//
//
//			File queriesOut = new File(outDir,"queries");
//			queriesOut.mkdirs();
//
//			File outFile1 = new File(outDir,"eval-queries");outFile1.mkdirs();
//
//			HashMap<File, AggQueryStats> aggMap = new HashMap<File, AggQueryStats>();
//
//			final ArrayList<String> acceptedQueries = new ArrayList<String>();
//			acceptedQueries.add("base");
//			acceptedQueries.add("smart");
//			acceptedQueries.add("seealso");
//			acceptedQueries.add("rdfs");
////			acceptedQueries.add("rdfsd");
////			acceptedQueries.add("rdfsc");
////			acceptedQueries.add("squin");
//			acceptedQueries.add("sameas");
//			acceptedQueries.add("all");
////			acceptedQueries.add("alldir");
////			acceptedQueries.add("cl");
//			
//			StringBuilder sbStability = new StringBuilder();
//			do{
//				final File query = files.get(0);
//
//				boolean accept = false;
//				for(String s: acceptedQueries){
//					if(query.getName().endsWith(s)){
//						accept =true;
//					}
//				}
//				if(!accept){
//					files.remove(query);
//					continue;
//				}
//				final String queryType = query.getName().substring(0,query.getName().indexOf("."));
//				File [] allQueries = inDir.listFiles(new FilenameFilter() {
//					public boolean accept(File dir, String name) {
//						boolean hasSum = false;
//						for(String s: acceptedQueries){
//							if(name.endsWith(s)){ hasSum =true;}
//						}
//						if(hasSum)
//							hasSum = name.startsWith(queryType);// && new File(dir,name+"/summary.txt").exists(); 
//						if(hasSum &&!new File(dir,name+"/summary.txt").exists()) System.out.println(name +" has no summary.txt");
//						return  hasSum;
//					}
//				});Arrays.sort(allQueries);
//				System.err.println("|-->Analysing ("+allQueries.length+"/"+files.size()+") |==> Output in "+new File(outDir,queryType));
//
//				Map<String,QueryStats1> qsMap = new HashMap<String,QueryStats1>();
//				for(File qu: allQueries){
//					System.err.println("|---> Analysing "+qu); files.remove(qu);
//					for(File bench: qu.listFiles(new FilenameFilter() {
//						public boolean accept(File dir, String name) {
//							return name.endsWith(".bench");
//						}
//					})){
//						String queryID = bench.getName().substring(0,bench.getName().indexOf("ql_")+2);
//
//						QueryStats1 qst = qsMap.get(queryID);
//						if(qst==null){ 
//							qst = new QueryStats1(queryID);
//							qsMap.put(queryID,qst);
//
//						}
//						QueryExecutionBenchmark qeb = new QueryExecutionBenchmark();
//						qeb.load(new FileInputStream(bench));
//						qst.add(qu, qeb, qu.getName().contains("smart"));
//						qst.setQuery(qeb.getQuery());
//
//					}//end single query files
//				}//end different query setups
//				System.out.println(queryType+" total:"+qsMap.size());
//				
//
//				AggQueryStats aqs = new AggQueryStats(query.getName());
//				aggMap.put(query, aqs);
//				//				/** print the queries **/
//				//				File queriesDir = new File(outDir,"swj.queries");
//				//				queriesDir.mkdirs();
//				//				queriesDir = new File(queriesDir,queryType);
//				//				queriesDir.mkdirs();
//				int success=0;
//				int zeroResults=0;
//
//
//
//				File outDirSingle = new File(outDir,queryType);
//				outDirSingle.mkdirs();
//				System.out.println("Printing single query stats to "+outDirSingle);
//
//
//				/**
//				 * we can have 
//				 *  (0) 404
//				 *  (1) 498 robots
//				 *  (2) 499 mime type 
//				 *  (3) 502
//				 *  ()  603 UNKNOWNHOSTEXCEPTION
//				 *  () combination
//				 *  () data
//				 */
//
//				String [] error = {"403","404","498","499","500","502","602","603"};
//
//				int [] emptyResultErrors = new int[error.length+2];
//				
//				Count<String> improved =new Count<String>();
//				
//				for(Entry<String,QueryStats1> ent: qsMap.entrySet()){
//					/** print the queries **/
//					// PrintWriter pw = new PrintWriter(new File(queriesDir,ent.getValue().getQueryID()));
//					// pw.println(ent.getValue().getQuery());
//					// pw.close();
//					
//					totalQueries++;
//					if(ent.getValue().isStable(acceptedQueries)){
//						totalTime.add(""+ent.getValue().getBaseValue()[QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1]/1000);
//						
////						if(ent.getValue().getBaseValue()[QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1]<1000)
////							totalLessOneSec++;
//						/** --------------------------- **
//						 * HTTP connection STABLE Query  *
//						 ** --------------------------- **/
//						success++;
//						System.err.println("STABLE: "+queryType+"/"+ent.getValue().getQueryID());
//						if(ent.getValue().hasZeroResult()){
//							/** ------------ **
//							 * Empty Results  *
//							 ** ------------ **/
//							zeroResults++;
//							Map<String,String> map = ent.getValue().getCoreURIs(acceptedQueries);
//							int respCode=-1;
//							for(Entry<String,String>entURI: map.entrySet()){
//								String resp = entURI.getValue();
//								boolean knownError = false;
//								for(int i =0;i<error.length;i++){
//									if(resp.startsWith(error[i])){
//										knownError=true;
//										if(respCode !=-1 && respCode !=i){
//											respCode=error.length;
//										}else respCode=i;
//									}	
//								}
//							}
//							if(respCode==-1){
////								System.out.println("DATA related empty results:"+queryType+":"+ent.getValue().getQueryID());
//								respCode=error.length+1;
//							}
//							emptyResultErrors[respCode]++;
//							
//							/** 
//							 * WE ALSO WANT TO KNOW IF WE IMPROVED THE RESULTS FOR THESE QUERIES
//							 */
//							for(String s: ent.getValue().getImprovedSetups()){
//								improved.add(s);	
//							}
////							aqs.update(ent.getValue());
//						}else{
//							aqs.update(ent.getValue());
//						}
////						ent.getValue().printTexTable(outDirSingle);
//					}else{
//						//WHY? 
//						System.out.println("UNSTABLE: "+ent.getKey());
//
//					}
//					ent.getValue().printTexTable(outDirSingle);
//				}
//				
//				summary.append(queryType).append("\n   q:").append(qsMap.size()).append(" succ:").append(success).append(" zero:").append(zeroResults);
//				
//				System.out.println("______");
//				System.out.print(queryType+" ");
//				summary.append("\n   imp:");
//				for(Entry<String,Integer> entImpr: improved.entrySet()){
//					System.out.print(entImpr.getKey()+" "+entImpr.getValue()+" ");
//					summary.append(" ").append(entImpr.getKey()).append(" ").append(entImpr.getValue());
//				}
//				summary.append("\n");
//				System.out.println();
//				sbStability.append(queryType).append("&").append(qsMap.size()).append("&").append(success).append("&").append(zeroResults);
//				for(int i =0; i< emptyResultErrors.length;i++){
//					sbStability.append("&");
//					if(emptyResultErrors[i]==0)
//						sbStability.append("-");
//					else
//						sbStability.append(emptyResultErrors[i]);
//				}
//				sbStability.append("\\\\ \n");
//
//				System.out.println("["+queryType+"] "+success+"/"+qsMap.size());
//				//
//				/** PRINT statistics **/
//				PrintWriter pwAgg = new PrintWriter(new File(outDir,queryType+".tex"));
//				pwAgg.println(aqs.toTexTable());
//				pwAgg.close();
//
//				pwAgg = new PrintWriter(new File(outDir,queryType+".rel.tex"));
//				pwAgg.println(aqs.toRelativeTexTable());
//				pwAgg.close();
//
//				pwAgg = new PrintWriter(new File(outDir,queryType+".desc.stats"));
//				pwAgg.println(aqs.descStats());
//				pwAgg.close();
//
//				pwAgg = new PrintWriter(new File(outDir,queryType+".dat"));
//				pwAgg.println(aqs.toDataTable());
//				pwAgg.close();
//
//				pwAgg = new PrintWriter(new File(outDir,queryType+".rel.dat"));
//				pwAgg.println(aqs.toRelDataTable());
//				pwAgg.close();
//				
//				pwAgg = new PrintWriter(new File(outDir,queryType+".recall.tex"));
//				pwAgg.println(aqs.toRecallTexTable());
//				pwAgg.close();
//				
//
//				pwAgg = new PrintWriter(new File(outDir,queryType+".comb.tex"));
//				pwAgg.println(aqs.toCombinedTexTable());
//				pwAgg.close();
//
//
//				//				/**-----------------------**
//				//				 *        DISCOVERY        * 
//				//				 **-----------------------**/				
//				//				System.err.println("|--< "+queryType+" >--- Discovering stable queries from a total of "+qsMap.size()+" queries");
//				//				int success =0;
//				//				if(allQueries.length >= acceptedQueries.size()){
//				//					int cnt = 0;
//				//
//				//					Count<String> pldInc = new Count<String>();
//				//					Count<String> pldMiss = new Count<String>();
//				//					int failure = 0, succe = 0, uriFailure =0;;
//				//
//				//					for(Entry<String,QueryStats> ent: qsMap.entrySet()){
//				//						if(ent.getValue().isStable(null,null)){
//				//							success++;
//				//							succe++;
//				//						}
//				//						else{
//				//							failure++;
//				//							try{
//				//								HashMap<String, Integer[]> uris = ent.getValue().getInconsistentURIs();
//				//								HashSet<String> pldsInc = new HashSet<String>();
//				//								HashSet<String> pldsMiss = new HashSet<String>();
//				//
//				//								for(Entry<String, Integer[]> ent11: uris.entrySet()){
//				//									boolean inconst = false;
//				//									boolean miss = false;
//				//									for(Integer en : ent11.getValue()){
//				//										if(en == null || en == 1)inconst= true;
//				//										else if(en == -1)miss= true;
//				//									}
//				//									if(inconst || miss){
//				//										String pldstr= ext.getPLD(new URI(ent11.getKey()));
//				//										if(pldstr != null){
//				//											if(inconst)
//				//												pldsInc.add(pldstr.trim().toLowerCase());
//				//											if(miss)
//				//												pldsMiss.add(pldstr.trim().toLowerCase());
//				//										}
//				//									}
//				//								}
//				//								for(String st: pldsInc){
//				//									pldInc.add(st,1);
//				//									totalpldInc.add(st,1);
//				//								}
//				//								for(String st: pldsMiss){
//				//									pldMiss.add(st,1);
//				//									totalpldMiss.add(st,1);
//				//								}
//				//								if(pldsMiss.size()!=0|| pldsInc.size()!=0){
//				//									uriFailure++;
//				//									if(pldMiss.getTotal()+pldInc.getTotal() < uriFailure){
//				//										System.out.println("Adding went wrong "+(pldMiss.size()+pldInc.size())+" "+uriFailure);
//				//									}
//				//								}else{
//				//								}
//				//							}catch(Exception e){
//				//								System.out.println(ent.getKey()+" exception:"+e.getClass().getSimpleName()+" msg: "+e.getMessage());
//				//							}
//				//						}
//				//						cnt++;
//				//						if(cnt%10==0){
//				//							System.out.println("\n processed:"+cnt);
//				//						}
//				//					}
//				//					System.out.println(queryType +" success:"+succe+" failure:"+failure+" from which URI failure: "+uriFailure);
//				//					System.out.println("_______UNSTABLE INC PLDS (query count) "+pldInc.getTotal());
//				//					pldInc.printOrderedStats(5,System.out);
//				//					System.out.println("_______UNSTABLE MISS PLDS (query count) "+pldMiss.getTotal());
//				//					pldMiss.printOrderedStats(5,System.out);
//				////					fw.close();
//				//				}
//				//				//				System.err.println("|--< "+queryType+" >--- "+success+"/"+qsMap.size());
//				//				/**-----------------------**
//				//				 *     END OF DISCOVERY    * 
//				//				 **-----------------------**/
//				//
//				//				//				
//				//				////				System.exit(0);
//				//				//				
//				//				//				/**-----------------------**
//				//				//				 *   ANALYSE THE RESULTS   *
//				//				//				 **-----------------------**/
//				//				File results = new File(outDir,"results");results.mkdirs(); 
//				//				PrintWriter pw = new PrintWriter(new File(results,queryType+".all.csv"));
//				//				//
//				//				int c = 0;
//				//				AggQueryStats aqs = new AggQueryStats(query.getName());
//				//				aggMap.put(query, aqs);
//				//				//				//lets analyse it
//				//				int [] resp = new int [7];
//				//				Arrays.fill(resp, 0);
//				//
//				//				File outQuer = new File(outFile1, queryType);
//				//				outQuer.mkdirs();
//				//
//				//				PrintWriter stable_pw = new PrintWriter(new File(outQuer,"stable.csv"));
//				//				PrintWriter error_pw = new PrintWriter(new File(outQuer,"error.csv"));
//				//				PrintWriter inconsistence_pw = new PrintWriter(new File(outQuer,"inconsistence.csv"));
//				//				PrintStream inconsistence_pwSUM = new PrintStream(new File(outQuer,"inconsistence_sum.csv"));
//				//				PrintWriter multi_pw = new PrintWriter(new File(outQuer,"multi.csv"));
//				//
//				//				if(qsMap.size()!=0){
//				//					Entry<String,QueryStats> ent1 = qsMap.entrySet().iterator().next();
//				//					stable_pw.println(ent1.getValue().getStatusArrayHeaderString());
//				//					inconsistence_pw.println(ent1.getValue().getStatusArrayHeaderString());
//				//					multi_pw.println(ent1.getValue().getStatusArrayHeaderString());
//				//					error_pw.println(ent1.getValue().getStatusArrayHeaderString());
//				//				}
//				//				Count<String> incsError = new Count<String>();
//				//				PrintWriter [] pws = initPrintWriters(new File(qtr,"agg"),7);
//				//
//				//				Count<String> plds5 = new Count<String>();
//				//				Count<String> plds6 = new Count<String>();
//				//				int six=0;
//				////				for(Entry<String,QueryStats> ent: qsMap.entrySet()){
//				////					if(c==0){
//				////						pw.println(ent.getValue().csvHeader());
//				////						c++;
//				////					}
//				////					if(qNo != ent.getValue().entries()) continue;
//				////
//				////					/**ok we have a benchmark for each run **/
//				////
//				////					StabilityStats.STABILITYCODE status = ent.getValue().getOnlyOverlapingStats();
//				////					stabStats.update(status);
//				////					
//				////					resp[status]+=1;
//				////					//					 0 - means everything was ok and non-empty result
//				////					//					 *   1 - means everything was ok, but empty results
//				////					//					 *   2 - null as result value, which indicates some error
//				////					//					 *   3 - we have an unstable abox query 
//				////					//					 *   4 - we have an unstable tbox query
//				////					//					 *   5 - multiple failures
//				////
//				////					pws[status].println(ent.getValue().getQueryID()+" "+Arrays.toString(ent.getValue().getAggStatus()));
//				////					String ww = ent.getValue().getQueryName();
//				////					if(status == 0)
//				////						stable_pw.println(ent.getValue().getStatusArrayString());
//				////					else if(status == 6){
//				////						inconsistence_pw.println(ent.getValue().getStatusArrayString());
//				////						String [] ss = ent.getValue().getStatusArrayString().split(" ");
//				////						TreeSet<String> set = new TreeSet<String>();
//				////						int cnt =0;
//				////						for(String str: ss){
//				////							if(cnt !=0)
//				////								set.add(str);
//				////							cnt++;
//				////						}
//				////						String res = "";
//				////						for(String str: set.descendingSet()){
//				////							res+=str;
//				////						}
//				////						incsError.add(res);
//				////						allIncsError.add(res);
//				////					}
//				////					else if(status == 5)
//				////						multi_pw.println(ent.getValue().getStatusArrayString());
//				////					else 
//				////						error_pw.println(ent.getValue().getStatusArrayString());
//				////					aqs.update(ent.getValue(),true);
//				////					pw.println(ent.getValue().csvLine());
//				////				}
//				//
//				//
//				//				for(PrintWriter pwss: pws)pwss.close();
//				//				stable_pw.close();error_pw.close();
//				//				inconsistence_pw.close();
//				//				incsError.printOrderedStats(inconsistence_pwSUM);
//				//				multi_pw.close();pw.close();
//				//				
//				//				inconsistence_pwSUM.close();
//				//				
//				//
//				//				String name = query.getName();
//				//				name= name.substring(0,name.indexOf("."));
//				//				int total=0;
//				//				for(int i =0; i< resp.length;i++){
//				//					total+=resp[i];
//				//				}
//				////				stabStats
//				////				aggStatsSum[0]+=total;
//				////				aggStats.append("\\textbf{"+name+"}").append("& ").append(total);
//				////				for(int i=0; i <resp.length;i++){
//				////					if(i!=2)
//				////						aggStats.append(" & ").append(resp[i]);
//				////					aggStatsSum[i+1]+=resp[i];
//				////				}
//				////				aggStats.append("\\\\\n");
//				//
//				//				PrintWriter pwAgg = new PrintWriter(new File(outDir,queryType+".tex"));
//				//				pwAgg.println(aqs.toTexTable());
//				//				pwAgg.close();
//				//
//				//				pwAgg = new PrintWriter(new File(results,queryType+".desc.stats"));
//				//				pwAgg.println(aqs.descStats());
//				//				pwAgg.close();
//				//
//				//				pwAgg = new PrintWriter(new File(outDir,queryType+".dat"));
//				//				pwAgg.println(aqs.toDataTable());
//				//				pwAgg.close();
//				//				System.err.println("|<---- Analysed "+queryType+" "+Arrays.toString(resp));
//				//				stableSTR+="|>> "+queryType+" "+aqs.getStable()+" "+ aqs.getError()+"\n";
//				//				System.err.println("|>> "+queryType+" "+aqs.getStable()+" "+ aqs.getError());
//
//			}while(files.size()!=0);
//			System.err.println("------------");
//System.err.println(summary.toString());
//			PrintWriter pw = new PrintWriter(new File(outDir,"stability.tex"));
//			pw.print(sbStability.toString());
//			pw.close();
//
//
//			//			System.out.println("_______UNSTABLE INC PLDS (query count) total:"+totalpldInc.getTotal());
//			//			totalpldInc.printOrderedStats(10,System.out);
//			//			System.out.println("_______UNSTABLE MISS PLDS (query count) total:"+totalpldMiss.getTotal());
//			//			totalpldMiss.printOrderedStats(10,System.out);
//			//
//			//
//			//
//			//
//			//			HashMap<String, PrintWriter> extWriter = new HashMap<String, PrintWriter>();
//			//			HashMap<String, Integer> extXnt = new HashMap<String, Integer>();
//			//			for(Map.Entry<File, AggQueryStats> ent: aggMap.entrySet()){
//			//
//			//				DescriptiveStatistics[] descBase = null;
//			//				for(Map.Entry<String, DescriptiveStatistics[]> entQue: ent.getValue().setupValues.entrySet()){
//			//					String s = entQue.getKey();
//			//					String setup = s.substring(s.lastIndexOf(".")+1);
//			//					if(s.contains("1base")){
//			//						descBase = entQue.getValue();
//			//						break;
//			//					}
//			//				}
//			//
//			//				for(Map.Entry<String, DescriptiveStatistics[]> entQue: ent.getValue().setupValues.entrySet()){
//			//					String s = entQue.getKey();
//			//					String setup = s.substring(s.lastIndexOf(".")+1);
//			//					PrintWriter pw = extWriter.get(setup);
//			//					if(pw==null){
//			//						pw = new PrintWriter(new File(outDir,setup+".dat"));
//			//						extWriter.put(setup,pw);
//			//						extXnt.put(setup, 0);
//			//					}
//			//					String st = ent.getKey().getName();
//			//					if(s.contains("1base")){
//			//						pw.println(st.substring(0,st.lastIndexOf("."))+" "+(extXnt.get(setup))+" "+ent.getValue().toString(entQue.getValue()));	
//			//					}else{
//			//						pw.println(st.substring(0,st.lastIndexOf("."))+" "+(extXnt.get(setup))+" "+ent.getValue().diffToString(entQue.getValue(),descBase));
//			//					}
//			//					extXnt.put(setup, extXnt.put(setup, 0)+1);
//			//					pw.flush();
//			//				}
//			//			}
//			//
//			//			for(PrintWriter pw: extWriter.values()){ pw.close();}
//			//			PrintStream p = new PrintStream(new File(outDir,"inconsistentAgg.tex"));
//			//			for(Map.Entry<String, Integer> ent: allIncsError.entrySet()){
//			//				p.print(ent.getValue());
//			//				for(int i =0; i< 6; i++){
//			//					if(i==3) continue;
//			//					p.print("&");
//			//					if(ent.getKey().contains(""+i))
//			//						p.print("X");
//			//					else
//			//						p.print("--");
//			//
//			//				}
//			//				p.println("\\\\");
//			//			}
//			//			p.close();
//			////			PrintWriter pwAgg = new PrintWriter(new File(outDir,"queryStats.tex"));
//			////			pwAgg.print(aggStats.toString());
//			////			pwAgg.println("\\hline");
//			////
//			////			for(int i =0; i < aggStatsSum.length;i++){
//			////				if(i!=3)
//			////					pwAgg.print("&"+aggStatsSum[i]);
//			////			}
//			////			pwAgg.println("\\\\ \\hline");
//			////			pwAgg.close();
//			////			System.out.println(aggStats.toString());
//			//			System.err.println(stableSTR);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//
//		System.out.println("We are done");
//		System.err.println("TOTAL QUERIES: "+totalQueries+" with "+totalLessOneSec+" queries with execution time less than 1 sec");
//		totalTime.printOrderedStats(System.out);
	}


	private void createSummary(File inDir) {
		try{
			for(File f: inDir.listFiles()){
				if(f.isDirectory()){
					FileWriter fw = new FileWriter(new File(f,"summary.txt"));
					for(File q: f.listFiles()){
						if(new File(q,"summary.txt").exists()){
							QueryExecutionBenchmark qeb = new QueryExecutionBenchmark(q.getName().substring(0,q.getName().indexOf(".sparql")+7), "");
							qeb.restoreConfig(new File(q,"summary.txt"));
							fw.write(qeb.oneLineSummary(" ")+"\n");
						}
					}
					fw.close();
				}
			}}
		catch(Exception e){
			e.printStackTrace();
		}
	}


	private HashSet<String> getABoxAccess(File qu, String queryID, String string) {

		HashSet<String> set = new HashSet<String>();
		for(File f: qu.listFiles()){
			if(f.getName().startsWith(queryID)){
				Scanner s;
				try {
					s = new Scanner(new File(f,"abox.access.log"));
					while(s.hasNextLine()){
						String []tt=s.nextLine().split(" ");
						String url = tt[2];
						String resp = tt[3];
						if(resp.startsWith(string))
							try {
								set.add(ext.getPLD(new URI(url)));
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}

					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			}
		}
		return set;
	}


	private PrintWriter[] initPrintWriters(File qu, int j) throws FileNotFoundException {
		PrintWriter[] pws = new PrintWriter[j];
		for(int i =0;i < pws.length;i++){
			pws[i]= new PrintWriter(new File(qu+"-type-"+i+".txt"));
		}
		return pws;
	}


	private static List<File> getQueries(File inDir) {
		System.out.println("Parsing queries from "+inDir);
		ArrayList<File> files = new ArrayList<File>();
		File[] queries = inDir.listFiles(new FileFilter() {
			public boolean accept(File query) {

				return true;
				//				boolean ex = new File(query,"summary.txt").exists();
				//				if(!ex)System.out.println(query);
				//				return  ex;
			}});
		for(File query: queries){ 
			files.add(query);}
		return files;
	}


	public  void copyFile(File source, File dest) throws IOException {
		if(!dest.exists()) {
			dest.createNewFile();
		}
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new  FileInputStream(source);
			out = new FileOutputStream(dest);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		}
		finally {
			if(in != null) {
				in.close();
			}
			if(out != null) {
				out.close();
			}
		}
	}
}

//
//class AggQueryStats{
//	private static ArrayList<String>names = new ArrayList<String>();
//	static {
//		names.add(".1base");
//		names.add(".2smart");
//		names.add(".3seealso");
//		names.add(".4sameas");
//		names.add(".5rdfs");
//		//		names.add(".6rdfsd");
//		//		names.add(".7rdfsc");
//		names.add(".8all");
//		//		names.add(".9alldir");
//		//		names.add(".10cl");
//	}
//	private String qc;
//	/**
//	 * 
//	 */
//	public AggQueryStats(String queryClass) {
//		qc = queryClass;
//	}
//
//	/**
//	 * @return
//	 */
//	public String descStats() {
//		StringBuilder sb = new StringBuilder("#");
//		for(String s: QueryExecutionBenchmark.summaryOrder){
//			sb.append("\tmin-").append(s).append("\tmean-").append(s).append("\tmax-").append(s).append("\tsd-").append(s).append("\tcount-").append(s);
//		}
//
//		sb.append("\n").append("#Query class ").append(qc).append(" with "+success+" queries");
//		for(Entry<String,DescriptiveStatistics[]> ent : setupValues.entrySet()){
//			sb.append("\n").append(ent.getKey());
//			for(int i=0; i< ent.getValue().length;i++){
//				DescriptiveStatistics d = ent.getValue()[i];
//				//					if(i == 4 || i==5||i==6||i==7|i==8) continue;
//				sb.append("\t").append(twoDForm.format(d.getMin())).append("\t").append(twoDForm.format(d.getMean())).append("\t").append(twoDForm.format(d.getMax())).append("\t").append(twoDForm.format(d.getStandardDeviation())).append("\t").append(twoDForm.format(d.getN()));		
//			}
//		}
//		sb.append("\n");
//
//		return sb.toString();
//	}
//
//
//
//	Map<String,DescriptiveStatistics[]> setupValues = new TreeMap<String, DescriptiveStatistics[]>();
//	int success = 0;
//	int errors = 0;
//
//	public int getStable(){
//		return success;
//	}
//	public int getError(){
//		return errors;
//	}
//
//	//	public void update(QueryStats value) {
//	//		update(value, false);
//	//	}
//
//	/**
//	 * @param value
//	 */
//	public void update(QueryStats value, boolean onlyStable) {
//		if( onlyStable && !value.isStable()){
//			errors++;
//		}
//		else{
//			success++;
//			for(Entry<File,Integer[]> ent : value.setupValues.entrySet()){
//				Integer [] qV = ent.getValue();
//				DescriptiveStatistics [] aggV = setupValues.get(ent.getKey().getName());
//				if(aggV == null){
//					aggV = new DescriptiveStatistics[qV.length];
//					setupValues.put(ent.getKey().getName(), aggV);
//				}
//				for(int i =0; i< aggV.length; i++){
//					if(aggV[i]==null) aggV[i]=new DescriptiveStatistics();
//					aggV[i].addValue(qV[i]);
//				}
//			}
//		}
//	}
//
//
//	public static DecimalFormat twoDForm = new DecimalFormat("#.##");
//	
//
//
//
//	public String toTexTable(){
//		try{
//			StringBuilder sb = new StringBuilder();
//			//		sb.append("\\textbf{Setup} & \\multicolumn{2}{c}{\\textbf{results}}& \\multicolumn{2}{c}{\\textbf{ time (ms)}}& \\multicolumn{2}{c}{\\textbf{ first (ms)}}& \\multicolumn{2}{c}{\\textbf{ABox http}}&& \\multicolumn{2}{c}{\\textbf{TBox http}}& \\multicolumn{2}{c}{\\textbf{ABox stmts}}& \\multicolumn{2}{c}{\\textbf{TBox stmts}}&        \\multicolumn{2}{c}{\\textbf{ inferred}}\\\\ \\hline");
//			//		sb.append("\n\\textbf{Setup} & \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}\\");
//
//			//				sb.append("\n").append("\\multicolumn{13}{l}{Query class \\textbf{"+qc+"} with "+success+" queries}\\\\\\hline");
//
//
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
//		}catch(Exception e){
//			return "";
//		}
//	}
//
//	public String toDataTable(){
//		try{
//			StringBuilder sb = new StringBuilder();
//			//			"\\textbf{Setup} & \\multicolumn{2}{c}{\\textbf{results}}& \\multicolumn{2}{c}{\\textbf{ time (ms)}}& \\multicolumn{2}{c}{\\textbf{ first (ms)}}& \\multicolumn{2}{c}{\\textbf{ABox http}}&& \\multicolumn{2}{c}{\\textbf{TBox http}}& \\multicolumn{2}{c}{\\textbf{ABox stmts}}& \\multicolumn{2}{c}{\\textbf{TBox stmts}}&        \\multicolumn{2}{c}{\\textbf{ inferred}}\\\\ \\hline");
//			//			sb.append("\n\\textbf{Setup} & \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}\\");
//			//			sb.append("\n").append("\\multicolumn{13}{l}{Query class \\textbf{"+qc+"}} with "+success+" queries\\\\\\hline");
//			String name = setupValues.keySet().iterator().next();
//			name= name.substring(0,name.indexOf("."));
//
//			int count = 1;
//			for(String s: names){
//				DescriptiveStatistics[] desc = setupValues.get((name+s));
//				if(desc!=null){
//					sb.append(s).append(" ").append(count++);
//					sb.append(toString(desc));
//				}
//				else{count++;}
//				sb.append("\n");
//			}
//
//
//			return sb.toString();
//		}catch(Exception e){
//			return "";
//		}
//	}
//
//	public String toString(DescriptiveStatistics[] desc) {
//		StringBuilder sb = new StringBuilder();
//		for(int i=0; i< desc.length;i++){
//			DescriptiveStatistics d = desc[i];
//			if(i == 4 || i==5||i==6||i==7|i==8) continue; // abox lookups
//
//			if(i==1 || i==2)
//				sb.append(" ").append(twoDForm.format(d.getMean()/1000D)).append(" ").append(twoDForm.format(d.getStandardDeviation()/1000D));
//			else
//				sb.append(" ").append(twoDForm.format(d.getMean())).append(" ").append(twoDForm.format(d.getStandardDeviation()));
//		}
//		return sb.toString();
//	}
//
//	public String diffToString(DescriptiveStatistics[] desc, DescriptiveStatistics[] base) {
//		StringBuilder sb = new StringBuilder();
//		for(int i=0; i< desc.length;i++){
//			DescriptiveStatistics d = desc[i];
//			double meanDiff = (d.getMean()/base[i].getMean())-1;
//			double sdDiff = (d.getStandardDeviation()/base[i].getStandardDeviation())-1;
//
//			if(i == 4 || i==5||i==6||i==7|i==8) continue; // abox lookups
//
//			if(i==1 || i==2){
//				if(meanDiff!=0) meanDiff = meanDiff/1000D;
//				if(sdDiff!=0) meanDiff = sdDiff/1000D;
//			}
//
//			sb.append(" ").append(twoDForm.format(meanDiff)).append(" ").append(twoDForm.format(sdDiff));
//
//		}
//		return sb.toString();
//	}
//
//
//}