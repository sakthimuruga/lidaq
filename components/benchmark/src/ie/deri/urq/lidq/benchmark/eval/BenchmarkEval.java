/**
 *
 */
package ie.deri.urq.lidq.benchmark.eval;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.benchmark.ReasonerBenchmark;
import ie.deri.urq.lidaq.benchmark.SourceLookupBenchmark;
import ie.deri.urq.lidaq.benchmark.WebRepositoryBenchmark;
import ie.deri.urq.lidq.benchmark.QueryStats;
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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.semanticweb.yars.stats.Count;
import org.semanticweb.yars.tld.TldManager;

import com.ibm.icu.text.DecimalFormat;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 17, 2010
 */
public class BenchmarkEval{

	private  TldManager ext;


	/**
	 * @param inDir
	 * @param outDir
	 */
	public void evaluate(File inDir, File outDir) {

		try{		//proxy
			ext = new TldManager();
			if(!outDir.exists()) outDir.mkdirs();

			List<File> files = getQueries(inDir); 

			StabilityStats stabStats = new StabilityStats();


			File outFile1 = new File(outDir,"eval-queries");outFile1.mkdirs();

			Count<String> allIncsError = new Count<String>();
			HashMap<File, AggQueryStats> aggMap = new HashMap<File, AggQueryStats>();
			Count<String> totalpldInc = new Count<String>();
			Count<String> totalpldMiss = new Count<String>();

			final ArrayList<String> acceptedQueries = new ArrayList<String>();
			acceptedQueries.add("base");
			acceptedQueries.add("smart");
			acceptedQueries.add("seealso");
			acceptedQueries.add("rdfs");
			//			acceptedQueries.add("rdfsd");
			//			acceptedQueries.add("rdfsc");
			acceptedQueries.add("sameas");
			acceptedQueries.add("all");
			//			acceptedQueries.add("alldir");
			//			acceptedQueries.add("cl");

			String stableSTR="";
			do{
				final File query = files.get(0);

				boolean accept = false;
				for(String s: acceptedQueries){
					if(query.getName().endsWith(s)){
						accept =true;
					}
				}
				if(!accept){
					files.remove(query);
					continue;
				}
				final String queryType = query.getName().substring(0,query.getName().indexOf("."));
				File [] allQueries = inDir.listFiles(new FilenameFilter() {
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
				});Arrays.sort(allQueries);
				System.err.println("|-->Analysing ("+allQueries.length+"/"+files.size()+") ==>Output in "+new File(outDir,queryType));

				int succ [] = new int[allQueries.length];
				File qtr = new File(outDir,queryType);qtr.mkdirs();

				Map<String,QueryStats> qsMap = new HashMap<String,QueryStats>();
				int qNo = 0;
				for(File qu: allQueries){
					qNo++;
					int count = 0;
					int stable = 0;

					int [] res = new int [6];
					Arrays.fill(res, 0);

					PrintWriter [] pws = initPrintWriters(new File(qtr,qu.getName()),6);

					System.err.print("|---> Analysing "+qu); files.remove(qu);
					for(File bench: qu.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".bench");
							//							return (name.startsWith("q") && name.endsWith(".bench"));
						}
					})){
						count++;

						String queryID = bench.getName().substring(0,bench.getName().indexOf("ql_")+2);

						QueryStats qst = qsMap.get(queryID);
						if(qst==null){ 
							qst = new QueryStats(queryID,acceptedQueries);
							qsMap.put(queryID,qst);
						}
						QueryExecutionBenchmark qeb = new QueryExecutionBenchmark();
						qeb.load(new FileInputStream(bench));
						qst.analyse(qu, qeb);

						//						res[status]+=1;
						//						pws[status].println(queryID);
					}

					for(PrintWriter pw: pws)pw.close();
					System.err.println("|<---- Analysed "+queryType);
				}





				/**-----------------------**
				 *        DISCOVERY        * 
				 **-----------------------**/				
				System.err.println("|--< "+queryType+" >--- Discovering stable queries from a total of "+qsMap.size()+" queries");
				int success =0;
				if(allQueries.length >= acceptedQueries.size()){
					int cnt = 0;

					Count<String> pldInc = new Count<String>();
					Count<String> pldMiss = new Count<String>();
					int failure = 0, succe = 0, uriFailure =0;;

					for(Entry<String,QueryStats> ent: qsMap.entrySet()){
						if(ent.getValue().isStable(null,null)){
							success++;
							succe++;
						}
						else{
							failure++;
							try{
								HashMap<String, Integer[]> uris = ent.getValue().getInconsistentURIs();
								HashSet<String> pldsInc = new HashSet<String>();
								HashSet<String> pldsMiss = new HashSet<String>();

								for(Entry<String, Integer[]> ent11: uris.entrySet()){
									boolean inconst = false;
									boolean miss = false;
									for(Integer en : ent11.getValue()){
										if(en == null || en == 1)inconst= true;
										else if(en == -1)miss= true;
									}
									if(inconst || miss){
										String pldstr= ext.getPLD(new URI(ent11.getKey()));
										if(pldstr != null){
											if(inconst)
												pldsInc.add(pldstr.trim().toLowerCase());
											if(miss)
												pldsMiss.add(pldstr.trim().toLowerCase());
										}
									}
								}
								for(String st: pldsInc){
									pldInc.add(st,1);
									totalpldInc.add(st,1);
								}
								for(String st: pldsMiss){
									pldMiss.add(st,1);
									totalpldMiss.add(st,1);
								}
								if(pldsMiss.size()!=0|| pldsInc.size()!=0){
									uriFailure++;
									if(pldMiss.getTotal()+pldInc.getTotal() < uriFailure){
										System.out.println("Adding went wrong "+(pldMiss.size()+pldInc.size())+" "+uriFailure);
									}
								}else{
								}
							}catch(Exception e){
								System.out.println(ent.getKey()+" exception:"+e.getClass().getSimpleName()+" msg: "+e.getMessage());
							}
						}
						cnt++;
						if(cnt%10==0){
							System.out.println("\n processed:"+cnt);
						}
					}
					System.out.println(queryType +" success:"+succe+" failure:"+failure+" from which URI failure: "+uriFailure);
					System.out.println("_______UNSTABLE INC PLDS (query count) "+pldInc.getTotal());
					pldInc.printOrderedStats(5,System.out);
					System.out.println("_______UNSTABLE MISS PLDS (query count) "+pldMiss.getTotal());
					pldMiss.printOrderedStats(5,System.out);
					//					fw.close();
				}
				//				System.err.println("|--< "+queryType+" >--- "+success+"/"+qsMap.size());
				/**-----------------------**
				 *     END OF DISCOVERY    * 
				 **-----------------------**/

				//				
				////				System.exit(0);
				//				
				//				/**-----------------------**
				//				 *   ANALYSE THE RESULTS   *
				//				 **-----------------------**/
				File results = new File(outDir,"results");results.mkdirs(); 
				PrintWriter pw = new PrintWriter(new File(results,queryType+".all.csv"));
				//
				int c = 0;
				AggQueryStats aqs = new AggQueryStats(query.getName());
				aggMap.put(query, aqs);
				//				//lets analyse it
				int [] resp = new int [7];
				Arrays.fill(resp, 0);

				File outQuer = new File(outFile1, queryType);
				outQuer.mkdirs();

				PrintWriter stable_pw = new PrintWriter(new File(outQuer,"stable.csv"));
				PrintWriter error_pw = new PrintWriter(new File(outQuer,"error.csv"));
				PrintWriter inconsistence_pw = new PrintWriter(new File(outQuer,"inconsistence.csv"));
				PrintStream inconsistence_pwSUM = new PrintStream(new File(outQuer,"inconsistence_sum.csv"));
				PrintWriter multi_pw = new PrintWriter(new File(outQuer,"multi.csv"));

				if(qsMap.size()!=0){
					Entry<String,QueryStats> ent1 = qsMap.entrySet().iterator().next();
					stable_pw.println(ent1.getValue().getStatusArrayHeaderString());
					inconsistence_pw.println(ent1.getValue().getStatusArrayHeaderString());
					multi_pw.println(ent1.getValue().getStatusArrayHeaderString());
					error_pw.println(ent1.getValue().getStatusArrayHeaderString());
				}
				Count<String> incsError = new Count<String>();
				PrintWriter [] pws = initPrintWriters(new File(qtr,"agg"),7);

				Count<String> plds5 = new Count<String>();
				Count<String> plds6 = new Count<String>();
				int six=0;
				//				for(Entry<String,QueryStats> ent: qsMap.entrySet()){
				//					if(c==0){
				//						pw.println(ent.getValue().csvHeader());
				//						c++;
				//					}
				//					if(qNo != ent.getValue().entries()) continue;
				//
				//					/**ok we have a benchmark for each run **/
				//
				//					StabilityStats.STABILITYCODE status = ent.getValue().getOnlyOverlapingStats();
				//					stabStats.update(status);
				//					
				//					resp[status]+=1;
				//					//					 0 - means everything was ok and non-empty result
				//					//					 *   1 - means everything was ok, but empty results
				//					//					 *   2 - null as result value, which indicates some error
				//					//					 *   3 - we have an unstable abox query 
				//					//					 *   4 - we have an unstable tbox query
				//					//					 *   5 - multiple failures
				//
				//					pws[status].println(ent.getValue().getQueryID()+" "+Arrays.toString(ent.getValue().getAggStatus()));
				//					String ww = ent.getValue().getQueryName();
				//					if(status == 0)
				//						stable_pw.println(ent.getValue().getStatusArrayString());
				//					else if(status == 6){
				//						inconsistence_pw.println(ent.getValue().getStatusArrayString());
				//						String [] ss = ent.getValue().getStatusArrayString().split(" ");
				//						TreeSet<String> set = new TreeSet<String>();
				//						int cnt =0;
				//						for(String str: ss){
				//							if(cnt !=0)
				//								set.add(str);
				//							cnt++;
				//						}
				//						String res = "";
				//						for(String str: set.descendingSet()){
				//							res+=str;
				//						}
				//						incsError.add(res);
				//						allIncsError.add(res);
				//					}
				//					else if(status == 5)
				//						multi_pw.println(ent.getValue().getStatusArrayString());
				//					else 
				//						error_pw.println(ent.getValue().getStatusArrayString());
				//					aqs.update(ent.getValue(),true);
				//					pw.println(ent.getValue().csvLine());
				//				}


				for(PrintWriter pwss: pws)pwss.close();
				stable_pw.close();error_pw.close();
				inconsistence_pw.close();
				incsError.printOrderedStats(inconsistence_pwSUM);
				multi_pw.close();pw.close();

				inconsistence_pwSUM.close();


				String name = query.getName();
				name= name.substring(0,name.indexOf("."));
				int total=0;
				for(int i =0; i< resp.length;i++){
					total+=resp[i];
				}
				//				stabStats
				//				aggStatsSum[0]+=total;
				//				aggStats.append("\\textbf{"+name+"}").append("& ").append(total);
				//				for(int i=0; i <resp.length;i++){
				//					if(i!=2)
				//						aggStats.append(" & ").append(resp[i]);
				//					aggStatsSum[i+1]+=resp[i];
				//				}
				//				aggStats.append("\\\\\n");

				PrintWriter pwAgg = new PrintWriter(new File(outDir,queryType+".tex"));
				pwAgg.println(aqs.toTexTable());
				pwAgg.close();

				pwAgg = new PrintWriter(new File(results,queryType+".desc.stats"));
				pwAgg.println(aqs.descStats());
				pwAgg.close();

				pwAgg = new PrintWriter(new File(outDir,queryType+".dat"));
				pwAgg.println(aqs.toDataTable());
				pwAgg.close();
				System.err.println("|<---- Analysed "+queryType+" "+Arrays.toString(resp));
				stableSTR+="|>> "+queryType+" "+aqs.getStable()+" "+ aqs.getError()+"\n";
				System.err.println("|>> "+queryType+" "+aqs.getStable()+" "+ aqs.getError());

			}while(files.size()!=0);

			System.out.println("_______UNSTABLE INC PLDS (query count) total:"+totalpldInc.getTotal());
			totalpldInc.printOrderedStats(10,System.out);
			System.out.println("_______UNSTABLE MISS PLDS (query count) total:"+totalpldMiss.getTotal());
			totalpldMiss.printOrderedStats(10,System.out);




			HashMap<String, PrintWriter> extWriter = new HashMap<String, PrintWriter>();
			HashMap<String, Integer> extXnt = new HashMap<String, Integer>();
			for(Map.Entry<File, AggQueryStats> ent: aggMap.entrySet()){

				DescriptiveStatistics[] descBase = null;
				for(Map.Entry<File, DescriptiveStatistics[]> entQue: ent.getValue().setupValues.entrySet()){
					String s = entQue.getKey().getName();
					String setup = s.substring(s.lastIndexOf(".")+1);
					if(s.contains("1base")){
						descBase = entQue.getValue();
						break;
					}
				}

				for(Map.Entry<File, DescriptiveStatistics[]> entQue: ent.getValue().setupValues.entrySet()){
					String s = entQue.getKey().getName();
					String setup = s.substring(s.lastIndexOf(".")+1);
					PrintWriter pw = extWriter.get(setup);
					if(pw==null){
						pw = new PrintWriter(new File(outDir,setup+".dat"));
						extWriter.put(setup,pw);
						extXnt.put(setup, 0);
					}
					String st = ent.getKey().getName();
					if(s.contains("1base")){
						pw.println(st.substring(0,st.lastIndexOf("."))+" "+(extXnt.get(setup))+" "+ent.getValue().toString(entQue.getValue()));	
					}else{
						pw.println(st.substring(0,st.lastIndexOf("."))+" "+(extXnt.get(setup))+" "+ent.getValue().diffToString(entQue.getValue(),descBase));
					}
					extXnt.put(setup, extXnt.put(setup, 0)+1);
					pw.flush();
				}
			}

			for(PrintWriter pw: extWriter.values()){ pw.close();}
			PrintStream p = new PrintStream(new File(outDir,"inconsistentAgg.tex"));
			for(Map.Entry<String, Integer> ent: allIncsError.entrySet()){
				p.print(ent.getValue());
				for(int i =0; i< 6; i++){
					if(i==3) continue;
					p.print("&");
					if(ent.getKey().contains(""+i))
						p.print("X");
					else
						p.print("--");

				}
				p.println("\\\\");
			}
			p.close();
			//			PrintWriter pwAgg = new PrintWriter(new File(outDir,"queryStats.tex"));
			//			pwAgg.print(aggStats.toString());
			//			pwAgg.println("\\hline");
			//
			//			for(int i =0; i < aggStatsSum.length;i++){
			//				if(i!=3)
			//					pwAgg.print("&"+aggStatsSum[i]);
			//			}
			//			pwAgg.println("\\\\ \\hline");
			//			pwAgg.close();
			//			System.out.println(aggStats.toString());
			System.err.println(stableSTR);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		System.out.println("We are done");
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


	private List<File> getQueries(File inDir) {
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


