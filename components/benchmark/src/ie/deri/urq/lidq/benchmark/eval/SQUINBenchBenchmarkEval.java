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
public class SQUINBenchBenchmarkEval{

	private  TldManager ext;


	public static DecimalFormat twoDForm = new DecimalFormat("#.##");
	public static void main(String[] args) throws FileNotFoundException, IOException {
		File indir = new File("/Users/juum/Resources/evaluations/ldquery_reason/squinBench");
		
		
		
		
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
		order.add("ld1");
		order.add("ld2");
		order.add("ld3");
		order.add("ld4");
		order.add("ld5");
		order.add("ld6");
		order.add("ld7");
		order.add("ld8");
		order.add("ld9");
		order.add("ld10");
		order.add("ld11");
		
		
		File out = new File("/Users/juum/Resources/evaluations/ldquery_reason/squinBench.out");
		out.mkdirs();
		for(String orders: order){
			File bench = new File(indir,orders+".bench");
			
			QueryStats1	qst = new QueryStats1("squin_"+orders+".bench");
			QueryExecutionBenchmark qeb = new QueryExecutionBenchmark();
			qeb.load(new FileInputStream(bench));
			File result = new File(bench.getAbsolutePath().replace(".bench",".tsv"));
			qeb.loadResults(result);
			qst.add(bench, qeb, false);
			qst.setQuery(qeb.getQuery());
			
			qst.printTexTable(out);
			
		}
		
		
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