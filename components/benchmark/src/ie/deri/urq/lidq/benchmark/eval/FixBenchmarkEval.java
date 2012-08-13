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
public class FixBenchmarkEval{

	private  TldManager ext;
	/**
	 * @param inDir
	 * @param outDir
	 */
	public void evaluate(File inDir, File outDir) {

		try{		//proxy
			ext = new TldManager();
			if(!outDir.exists()) outDir.mkdirs();

			//			createSummary(inDir);
			List<File> files = getQueries(inDir); 


			File outFile1 = new File(outDir,"eval-queries");outFile1.mkdirs();

			final ArrayList<String> acceptedQueries = new ArrayList<String>();
			acceptedQueries.add("base");
			acceptedQueries.add("smart");
			acceptedQueries.add("seealso");
			acceptedQueries.add("rdfs");
			acceptedQueries.add("rdfsd");
			acceptedQueries.add("rdfsc");
			acceptedQueries.add("sameas");
			acceptedQueries.add("all");
			acceptedQueries.add("alldir");
			acceptedQueries.add("cl");

			FileWriter fw = new FileWriter(new File(outDir,"run_missing.sh"));
			
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
							if(name.endsWith(s)){
								hasSum =true;
							}
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
				Map<String,Boolean[]> queryIDs = new HashMap<String,Boolean[]>();




				for(File qu: allQueries){
					qNo++;
					int count = 0;
					int stable = 0;

					int [] res = new int [acceptedQueries.size()];
					Arrays.fill(res, 0);

					PrintWriter [] pws = initPrintWriters(new File(qtr,qu.getName()),6);

					System.err.print("|---> Analysing "+qu); files.remove(qu);
					//					File sum = new File(qu,"summary.txt");
					//					Scanner sc = new Scanner(sum);

					for(File bench: qu.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return (name.startsWith("q") && name.endsWith(".bench"));
						}
					})){
						count++;

						String queryID = bench.getName().substring(0,bench.getName().indexOf("ql_")+2);

						Boolean [] check = queryIDs.get(queryID);
						if(check==null){
							check = new Boolean[acceptedQueries.size()];
							Arrays.fill(check, false);
							queryIDs.put(queryID, check);
						}

						QueryExecutionBenchmark qeb = new QueryExecutionBenchmark();
						qeb.load(new FileInputStream(bench));

						QueryStats qst = qsMap.get(queryID);
						if(qst==null){ 
							qst = new QueryStats(queryID,acceptedQueries);
							qsMap.put(queryID,qst);
							qst.setQuery(qeb.getQuery());
						}

						for(int i=0; i< acceptedQueries.size();i++){
							if(bench.getParentFile().getName().contains(acceptedQueries.get(i))){
								check[i]=true;
							}
						}
						int status = qst.analyse(qu, qeb);

						res[status]+=1;
						pws[status].println(queryID);
					}
					for(PrintWriter pw: pws)pw.close();
					System.err.println("|<---- Analysed "+queryType+" "+Arrays.toString(res));
				}

				
				
				HashMap<Integer, String> cmds = new HashMap<Integer, String>();
						cmds.put(1, "java -XX:OnOutOfMemoryError=\"kill -9 %p\" $OPT -jar $JAR Bench -b LTBQE -bd $SMART     -q $q -r OFF -sl smart 1>$SMART/$q.out 2>$SMART/$q.err");
						cmds.put(2, "java -XX:OnOutOfMemoryError=\"kill -9 %p\" $OPT -jar $JAR Bench -b LTBQE -bd $SEEALSO   -q $q -r OFF -sl smart -sA 1>$SEEALSO/$q.out 2>$SEEALSO/$q.err");
						cmds.put(3, "java -XX:OnOutOfMemoryError=\"kill -9 %p\" $OPT -jar $JAR Bench -b LTBQE -bd $SAMEAS   -q $q -r OWL -sl smart 1>$SAMEAS/$q.out 2>$SAMEAS/$q.err");
						cmds.put(4, "java -XX:OnOutOfMemoryError=\"kill -9 %p\" $OPT -jar $JAR Bench -b LTBQE -bd $RDFS     -q $q -r RDFS -sl smart 1>$RDFS/$q.out 2>$RDFS/$q.err");
						cmds.put(5, "java -XX:OnOutOfMemoryError=\"kill -9 %p\" $OPT -jar $JAR Bench -b LTBQE -bd $RDFS_D   -q $q -r RDFS_DYN_DIR -sl smart 1>$RDFS_D/$q.out 2>$RDFS_D/$q.err");
						cmds.put(6, "java -XX:OnOutOfMemoryError=\"kill -9 %p\" $OPT -jar $JAR Bench -b LTBQE -bd $RDFS_C   -q $q -r RDFS_DYN_CLOSURE -sl smart 1>$RDFS_C/$q.out 2>$RDFS_C/$q.err");
						cmds.put(7, "java -XX:OnOutOfMemoryError=\"kill -9 %p\" $OPT -jar $JAR Bench -b LTBQE -bd $ALL      -q $q -sA -r ALL -sl smart 1>$ALL/$q.out 2>$ALL/$q.err");
						cmds.put(8, "java -XX:OnOutOfMemoryError=\"kill -9 %p\" $OPT -jar $JAR Bench -b LTBQE -bd $ALL_DIR  -q $q -sA -r ALL_DYN_DIR -sl smart 1>$ALL_DIR/$q.out 2>$ALL_DIR/$q.err");
						cmds.put(9, "java -XX:OnOutOfMemoryError=\"kill -9 %p\" $OPT -jar $JAR Bench -b LTBQE -bd $ALL_CL   -q $q -sA -r ALL_DYN_CLOSURE -sl smart 1>$ALL_CL/$q.out 2>$ALL_CL/$q.err");
						cmds.put(0, "java -XX:OnOutOfMemoryError=\"kill -9 %p\" $OPT -jar $JAR Bench -b LTBQE -bd $BASE -q $q -r OFF -sl ALL 1>$BASE_LOG/$q.out 2>$BASE_LOG/$q.err");
						/**-----------------------**
						 *        DISCOVERY        * 
						 **-----------------------**/				
						System.err.println("|--< "+queryType+" >--- Discovering stable queries from "+qsMap.size());
						int success =0;
						for(Entry<String,Boolean[]> ent: queryIDs.entrySet()){
							System.out.println(ent.getKey()+" ");
							boolean copy = false;
							for(int i=0; i < ent.getValue().length;i++){
								if(!ent.getValue()[i]){
									if(!copy){
										FileWriter fos = new FileWriter(new File(outDir,ent.getKey()));
										fos.write(qsMap.get(ent.getKey()).getQuery());
										fos.close();
										copy= true;
										fw.write("echo \""+ent.getKey()+"\"\nq="+ent.getKey()+"\n");
									}
									fw.write("echo \""+i+"\"\n"+cmds.get(i)+"\n");
								}
							}
						}
						// System.err.println("|--< "+queryType+" >--- "+success+"/"+qsMap.size());
						/**-----------------------**
						 *     END OF DISCOVERY    * 
						 **-----------------------**/
			}while(files.size()!=0);
			fw.close();
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

