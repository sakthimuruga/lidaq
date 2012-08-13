/**
 *
 */
package ie.deri.urq.lidq.benchmark.eval;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidq.benchmark.QueryStats;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.semanticweb.yars.stats.Count;
import org.semanticweb.yars.tld.TldManager;

import com.ibm.icu.text.DecimalFormat;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 17, 2010
 */
public class BenchmarkEvalDynamicImport{

	private  TldManager ext;
	/**
	 * @param inDir
	 * @param outDir
	 */
	public void evaluate(File inDir, File outDir) {

//		try{		//proxy
//			ext = new TldManager();
//			if(!outDir.exists()) outDir.mkdirs();
//
//			//			createSummary(inDir);
//
//			List<File> files = getQueries(inDir); 
//
//			StringBuilder aggStats = new StringBuilder();
//			aggStats.append("&&&&\\multicolumn{2}{c}{\\textbf{\\underline{network error}}}& &      \\\\\n");
//			aggStats.append("\\textbf{Query type}&\\textbf{total}&\\textbf{stable}&\\textbf{empty result}&\\textbf{abox}&\\textbf{tbox}&\\textbf{multiple errors} &\\textbf{inconsistent}\\\\\\hline\n");
//
//			int [] aggStatsSum = new int[8];
//
//			File outFile1 = new File(outDir,"eval-queries");outFile1.mkdirs();
//
//			Count<String> allIncsError = new Count<String>();
//			HashMap<File, AggQueryStats> aggMap = new HashMap<File, AggQueryStats>();
//
//			
//			Count<String> totalpldInc = new Count<String>();
//			Count<String> totalpldMiss = new Count<String>();
//			
//			
//			String stableSTR="";
//			do{
//				final File query = files.get(0);
//
//				final String queryType = query.getName().substring(0,query.getName().indexOf("."));
//				File [] allQueries = inDir.listFiles(new FilenameFilter() {
//					public boolean accept(File dir, String name) {
//						boolean hasSum = name.startsWith(queryType) && new File(dir,name+"/summary.txt").exists(); 
//						if(!new File(dir,name+"/summary.txt").exists()) System.out.println(name +" has no summary.txt");
//						return  hasSum;
//					}
//				});Arrays.sort(allQueries);
//				System.err.println("|-->Analysing ("+allQueries.length+"/"+files.size()+") ==>Output in "+new File(outDir,queryType));
//
//				int succ [] = new int[allQueries.length];
//				File qtr = new File(outDir,queryType);qtr.mkdirs();
//
//				Map<String,QueryStats> qsMap = new HashMap<String,QueryStats>();
//				int qNo = 0;
//				for(File qu: allQueries){
//					qNo++;
//					File sum = new File(qu,"summary.txt");
//					Scanner sc = new Scanner(sum);
//					int count = 0;
//					int stable = 0;
//
//					int [] res = new int [6];
//					Arrays.fill(res, 0);
//
//					PrintWriter [] pws = initPrintWriters(new File(qtr,qu.getName()),6);
//
//					System.err.print("|---> Analysing "+qu); files.remove(qu);
//					while(sc.hasNextLine()){
//						String [] tt = sc.nextLine().trim().split(" ");
//						if(tt.length != 19){
//							continue;
//						}
//						count++;
//						String queryID = tt[0].substring(0,tt[0].indexOf(".sparql"));
//
//						QueryStats qst = qsMap.get(queryID);
//						if(qst==null){ 
//							qst = new QueryStats(queryID,allQueries.length);
//							qsMap.put(queryID,qst);
//						}
//						int status = qst.analyse(qu, tt);
//
//						res[status]+=1;
//						pws[status].println(queryID);
//					}
//					for(PrintWriter pw: pws)pw.close();
//					System.err.println("|<---- Analysed "+queryType+" "+Arrays.toString(res));
//				}
//
//				/**-----------------------**
//				 *        DISCOVERY        * 
//				 **-----------------------**/				
//				System.err.println("|--< "+queryType+" >--- Discovering stable queries from "+qsMap.size());
//				int success =0;
//				if(allQueries.length >= 10){
//					File f = new File(outDir,"mv.sh");
//					File f2 = new File(outDir,"tmp");
//					f2.mkdirs();
//					if(!f.exists()) f.createNewFile();
//					FileWriter fw = new FileWriter(f);
//					int cnt = 0;
//
//					Count<String> pldInc = new Count<String>();
//					Count<String> pldMiss = new Count<String>();
//					int failure = 0, succe = 0, uriFailure =0;;
//					
//					for(Entry<String,QueryStats> ent: qsMap.entrySet()){
//						if(ent.getValue().isStable(null,null)){
//							success++;
//							succe++;
//						}
//						else{
//							failure++;
//							/** 
//							 * WHY??
//							 */
//							try{
//							//	System.out.println(">>"+ent.getValue().getQueryID());
//							HashMap<String, Integer[]> uris = ent.getValue().getInconsistentURIs();
//							//							if(uris.size() ==13){
//							HashSet<String> pldsInc = new HashSet<String>();
//							HashSet<String> pldsMiss = new HashSet<String>();
//							
//							for(Entry<String, Integer[]> ent11: uris.entrySet()){
//								boolean inconst = false;
//								boolean miss = false;
//								for(Integer en : ent11.getValue()){
//									if(en == null || en == 1)inconst= true;
//									else if(en == -1)miss= true;
////									
//									if(en != null && en==1){
//										System.out.println(ent.getKey()+" "+ent11.getKey()+" "+Arrays.toString(ent11.getValue()));
//										System.out.println("______");
//									}
//									if(en != null && en==-1){
//										System.out.println(ent.getKey()+" "+ent11.getKey()+" "+Arrays.toString(ent11.getValue()));
//									}
//								}
//								if(inconst || miss){
//									String pldstr= ext.getPLD(new URI(ent11.getKey()));
//									if(pldstr != null){
//										if(inconst)
//											pldsInc.add(pldstr.trim().toLowerCase());
//										if(miss)
//											pldsMiss.add(pldstr.trim().toLowerCase());
////									if(pldstr.equals("xmlns.com")){
////										System.out.println(ent.getKey()+" "+Arrays.toString(ent11.getValue()));
////									}
//									}
//								}
//							}
//							for(String st: pldsInc){
////								System.out.println("Adding "+st);
//								pldInc.add(st,1);
//								totalpldInc.add(st,1);
////								if(st.equals("xmlns.com")){
////									System.out.println(ent.getKey()+" "+ent.getValue().getQueryID());
////								}
//							}
//							for(String st: pldsMiss){
////								System.out.println("Adding "+st);
//								pldMiss.add(st,1);
//								totalpldMiss.add(st,1);
////								if(st.equals("xmlns.com")){
////									System.out.println(ent.getKey()+" "+ent.getValue().getQueryID());
////								}
//							}
//							if(pldsMiss.size()!=0|| pldsInc.size()!=0){
//								uriFailure++;
////								System.out.println("pldsMiss.size()"+pldsMiss.size()+" pldsInc.size(): "+pldsInc.size());
//								if(pldMiss.getTotal()+pldInc.getTotal() < uriFailure){
//									System.out.println("Adding went wrong "+(pldMiss.size()+pldInc.size())+" "+uriFailure);
//								}
//							}else{
//								//ok what is the failure
//								
//								System.out.println(ent.getKey()+" >> "+ent.getValue().getOnlyOverlapingStats()+" >> "+Arrays.toString(ent.getValue().getAggStatus()));
//							}
//							}catch(Exception e){
//								System.out.println(ent.getKey()+" exception:"+e.getClass().getSimpleName()+" msg: "+e.getMessage());
//							}
//						}
//						cnt++;
//						if(cnt%10==0){
//							System.out.println(cnt);
//						}
//					}
//					System.out.println(queryType +" success:"+succe+" failure:"+failure+" from which URI failure: "+uriFailure);
//					System.out.println("_______UNSTABLE INC PLDS (query count) "+pldInc.getTotal());
//					pldInc.printOrderedStats(5,System.out);
//					System.out.println("_______UNSTABLE MISS PLDS (query count) "+pldMiss.getTotal());
//					pldMiss.printOrderedStats(5,System.out);
//					fw.close();
//				}
//				//				System.err.println("|--< "+queryType+" >--- "+success+"/"+qsMap.size());
//				/**-----------------------**
//				 *     END OF DISCOVERY    * 
//				 **-----------------------**/
//
//				//				
//				////				System.exit(0);
//				//				
//				//				/**-----------------------**
//				//				 *   ANALYSE THE RESULTS   *
//				//				 **-----------------------**/
//				File results = new File(outDir,"results");results.mkdirs(); 
//				PrintWriter pw = new PrintWriter(new File(results,queryType+".all.csv"));
//				//
//				int c = 0;
//				AggQueryStats aqs = new AggQueryStats(query.getName());
//				aggMap.put(query, aqs);
//				//				//lets analyse it
//				int [] resp = new int [7];
//				Arrays.fill(resp, 0);
//
//				File outQuer = new File(outFile1, queryType);
//				outQuer.mkdirs();
//
//				PrintWriter stable_pw = new PrintWriter(new File(outQuer,"stable.csv"));
//				PrintWriter error_pw = new PrintWriter(new File(outQuer,"error.csv"));
//				PrintWriter inconsistence_pw = new PrintWriter(new File(outQuer,"inconsistence.csv"));
//				PrintStream inconsistence_pwSUM = new PrintStream(new File(outQuer,"inconsistence_sum.csv"));
//				PrintWriter multi_pw = new PrintWriter(new File(outQuer,"multi.csv"));
//
//				if(qsMap.size()!=0){
//					Entry<String,QueryStats> ent1 = qsMap.entrySet().iterator().next();
//					stable_pw.println(ent1.getValue().getStatusArrayHeaderString());
//					inconsistence_pw.println(ent1.getValue().getStatusArrayHeaderString());
//					multi_pw.println(ent1.getValue().getStatusArrayHeaderString());
//					error_pw.println(ent1.getValue().getStatusArrayHeaderString());
//				}
//				Count<String> incsError = new Count<String>();
//				PrintWriter [] pws = initPrintWriters(new File(qtr,"agg"),7);
//
//				Count<String> plds5 = new Count<String>();
//				Count<String> plds6 = new Count<String>();
//				int six=0;
//				for(Entry<String,QueryStats> ent: qsMap.entrySet()){
//					if(c==0){
//						pw.println(ent.getValue().csvHeader());
//						c++;
//					}
//					if(qNo != ent.getValue().entries()) continue;
//
//					/**ok we have a benchmark for each run **/
//
//					int status = ent.getValue().getOnlyOverlapingStats(); 
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
//
//
//				for(PrintWriter pwss: pws)pwss.close();
//				stable_pw.close();
//				error_pw.close();
//				inconsistence_pw.close();
//				incsError.printOrderedStats(inconsistence_pwSUM);
//				multi_pw.close();
//				inconsistence_pwSUM.close();
//				pw.close();
//
//				String name = query.getName();
//				name= name.substring(0,name.indexOf("."));
//				int total=0;
//				for(int i =0; i< resp.length;i++){
//					total+=resp[i];
//				}
//				aggStatsSum[0]+=total;
//				aggStats.append("\\textbf{"+name+"}").append("& ").append(total);
//				for(int i=0; i <resp.length;i++){
//					if(i!=2)
//						aggStats.append(" & ").append(resp[i]);
//					aggStatsSum[i+1]+=resp[i];
//				}
//				aggStats.append("\\\\\n");
//
//				PrintWriter pwAgg = new PrintWriter(new File(outDir,queryType+".tex"));
//				pwAgg.println(aqs.toTexTable());
//				pwAgg.close();
//
//				pwAgg = new PrintWriter(new File(results,queryType+".desc.stats"));
//				pwAgg.println(aqs.descStats());
//				pwAgg.close();
//
//				pwAgg = new PrintWriter(new File(outDir,queryType+".dat"));
//				pwAgg.println(aqs.toDataTable());
//				pwAgg.close();
//				System.err.println("|<---- Analysed "+queryType+" "+Arrays.toString(resp));
//				stableSTR+="|>> "+queryType+" "+aqs.getStable()+" "+ aqs.getError()+"\n";
//				System.err.println("|>> "+queryType+" "+aqs.getStable()+" "+ aqs.getError());
//
//			}while(files.size()!=0);
//
//			System.out.println("_______UNSTABLE INC PLDS (query count) total:"+totalpldInc.getTotal());
//			totalpldInc.printOrderedStats(10,System.out);
//			System.out.println("_______UNSTABLE MISS PLDS (query count) total:"+totalpldMiss.getTotal());
//			totalpldMiss.printOrderedStats(10,System.out);
//			
//			
//			
//			
//			HashMap<String, PrintWriter> extWriter = new HashMap<String, PrintWriter>();
//			HashMap<String, Integer> extXnt = new HashMap<String, Integer>();
//			for(Map.Entry<File, AggQueryStats> ent: aggMap.entrySet()){
//
//				DescriptiveStatistics[] descBase = null;
//				for(Map.Entry<String, DescriptiveStatistics[]> entQue: ent.getValue().setupValues.entrySet()){
//					String s = entQue.getKey();
//					String setup = s.substring(s.lastIndexOf(".")+1);
//					if(s.contains("1base")){
//						descBase = entQue.getValue();
//						break;
//					}
//				}
//
//				for(Map.Entry<String, DescriptiveStatistics[]> entQue: ent.getValue().setupValues.entrySet()){
//					String s = entQue.getKey();
//					String setup = s.substring(s.lastIndexOf(".")+1);
//					PrintWriter pw = extWriter.get(setup);
//					if(pw==null){
//						pw = new PrintWriter(new File(outDir,setup+".dat"));
//						extWriter.put(setup,pw);
//						extXnt.put(setup, 0);
//					}
//					String st = ent.getKey().getName();
//					if(s.contains("1base")){
//						pw.println(st.substring(0,st.lastIndexOf("."))+" "+(extXnt.get(setup))+" "+ent.getValue().toString(entQue.getValue()));	
//					}else{
//						pw.println(st.substring(0,st.lastIndexOf("."))+" "+(extXnt.get(setup))+" "+ent.getValue().diffToString(entQue.getValue(),descBase));
//					}
//					extXnt.put(setup, extXnt.put(setup, 0)+1);
//					pw.flush();
//				}
//			}
//
//			for(PrintWriter pw: extWriter.values()){ pw.close();}
//			PrintStream p = new PrintStream(new File(outDir,"inconsistentAgg.tex"));
//			for(Map.Entry<String, Integer> ent: allIncsError.entrySet()){
//				p.print(ent.getValue());
//				for(int i =0; i< 6; i++){
//					if(i==3) continue;
//					p.print("&");
//					if(ent.getKey().contains(""+i))
//						p.print("X");
//					else
//						p.print("--");
//
//				}
//				p.println("\\\\");
//			}
//			p.close();
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
//			System.err.println(stableSTR);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//
//		System.out.println("We are done");
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
				boolean ex = new File(query,"summary.txt").exists();
				if(!ex)System.out.println(query);
				return  ex;
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
//
//	private String qc;
//	/**
//	 * 
//	 */
//	public AggQueryStats(String queryClass) {
//		qc = queryClass;
//	}
//
//
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
//	ArrayList<String>names = new ArrayList<String>();
//	{
//		names.add(".1base");
//		names.add(".2smart");
//		names.add(".3seealso");
//		names.add(".4sameas");
//		names.add(".5rdfs");
//		names.add(".6rdfsd");
//		names.add(".7rdfsc");
//		names.add(".8all");
//		names.add(".9alldir");
//		names.add(".10cl");
//	}
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
//				sb.append("\n").append("{\\tt ").append(s).append("})");
//				if(desc!=null){
//					for(int i=0; i< desc.length;i++){
//						DescriptiveStatistics d = desc[i];
//						if(i == 4 || i==5||i==6||i==7|i==8) continue; // abox lookups
//						if(i == 10 || i==11||i==12||i==13|i==14) continue; // abox lookups
//						if(i==1 || i==2 || i==15 || i==16|| i==17)
//							sb.append("& ").append(twoDForm.format(d.getMean()/1000D)).append("&($\\pm$").append(twoDForm.format(d.getStandardDeviation()/1000D)).append(")");
//						else
//							sb.append("& ").append(twoDForm.format(d.getMean())).append("&($\\pm$").append(twoDForm.format(d.getStandardDeviation())).append(")");
//					}
//				}
//				else{sb.append("&-&-&-&-&-&-& ");}
//				sb.append("\\\\");
//			}
//			sb.append("\n\\hline");
//			System.out.println(success+"/"+errors);
//
//
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
//
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


//}