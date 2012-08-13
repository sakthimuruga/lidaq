package ie.deri.urq.lidq.benchmark.eval;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.benchmark.ReasonerBenchmark;
import ie.deri.urq.lidaq.benchmark.SourceLookupBenchmark;
import ie.deri.urq.lidaq.benchmark.WebRepositoryBenchmark;
import ie.deri.urq.lidq.benchmark.QueryStats;
import ie.deri.urq.lidq.benchmark.QueryStats1;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class AggQueryStats{
	private static ArrayList<String>names = new ArrayList<String>();
	static {
		names.add(".1base");
		names.add(".2smart");
		names.add(".3seealso");
		names.add(".4sameas");
		names.add(".5rdfs");
		names.add(".6rdfsd");
		names.add(".7rdfsc");
		//		names.add(".9squin");
		names.add(".8all");
		names.add(".9alldir");
		names.add(".10cl");
	}
	private String qc;
	/**
	 * 
	 */
	public AggQueryStats(String queryClass) {
		qc = queryClass;
	}

	/**
	 * @return
	 */
	public String descStats() {
		StringBuilder sb = new StringBuilder("#");
		for(String s: QueryExecutionBenchmark.summaryOrder){
			sb.append("\tmin-").append(s).append("\tmean-").append(s).append("\tmax-").append(s).append("\tsd-").append(s).append("\tcount-").append(s);
		}

		sb.append("\n").append("#Query class ").append(qc).append(" with "+success+" queries");
		for(Entry<File,DescriptiveStatistics[]> ent : setupValues.entrySet()){
			sb.append("\n").append(ent.getKey());
			for(int i=0; i< ent.getValue().length;i++){
				DescriptiveStatistics d = ent.getValue()[i];
				//					if(i == 4 || i==5||i==6||i==7|i==8) continue;
				sb.append("\t").append(twoDForm.format(d.getMin())).append("\t").append(twoDForm.format(d.getMean())).append("\t").append(twoDForm.format(d.getMax())).append("\t").append(twoDForm.format(d.getStandardDeviation())).append("\t").append(twoDForm.format(d.getN()));		
			}
		}
		sb.append("\n");

		return sb.toString();
	}


	Map<File,DescriptiveStatistics[]> termCounts = new TreeMap<File, DescriptiveStatistics[]>();
	Map<File,DescriptiveStatistics[]> setupValues = new TreeMap<File, DescriptiveStatistics[]>();
	Map<File,DescriptiveStatistics[]> relativeSetupValues = new TreeMap<File, DescriptiveStatistics[]>();
	int success = 0;
	int errors = 0;
	private File baseFile;

	public int getStable(){
		return success;
	}
	public int getError(){
		return errors;
	}

	//	public void update(QueryStats value) {
	//		update(value, false);
	//	}

	public void update(QueryStats1 value) {
		Integer[] baseValue = value.getBaseValue(); 
		baseFile = value.getBaseFile();
		for(Entry<File,Integer[]> ent : value.setupValues.entrySet()){
			Integer [] qV = ent.getValue();
			
			DescriptiveStatistics [] counts = termCounts.get(ent.getKey());
			DescriptiveStatistics [] aggAbsV = setupValues.get(ent.getKey());
			DescriptiveStatistics [] aggRelV = relativeSetupValues.get(ent.getKey());

			if(counts == null){
				counts = new DescriptiveStatistics[2];
				counts[0]= new DescriptiveStatistics();
				counts[1]= new DescriptiveStatistics();
				termCounts.put(ent.getKey(), counts);
			}
			counts[0].addValue(value.getTermCount(ent.getKey()));
			counts[1].addValue(value.getTermColumnCount(ent.getKey()));
			
			if(aggAbsV == null){
				aggAbsV = new DescriptiveStatistics[qV.length];
				setupValues.put(ent.getKey(), aggAbsV);
			}
			for(int i =0; i< aggAbsV.length; i++){
				if(aggAbsV[i]==null) aggAbsV[i]=new DescriptiveStatistics();
				aggAbsV[i].addValue(qV[i]);
			}

			if(aggRelV == null){
				aggRelV = new DescriptiveStatistics[qV.length];
				relativeSetupValues.put(ent.getKey(), aggRelV);
			}
			for(int i =0; i< aggRelV.length; i++){
				//				if(i == (QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1)){
				//					System.out.println(value.getQueryID()+" "+100*(double)(qV[i]-baseValue[i])/(double)baseValue[i]);
				//				}
				if(aggRelV[i]==null) aggRelV[i]=new DescriptiveStatistics();
				if(ent.getKey().equals(baseFile)){
					aggRelV[i].addValue(qV[i]);	
				}else{
					if(baseValue[i] != 0){
						double valu = ((double)(qV[i])/(double)baseValue[i]);
						if(i == (QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1)){
							//							System.out.println(value.getQueryID()+" "+valu);
							if(ent.getKey().getName().contains("base")){
								//								System.out.println(aggRelV[i].getMean());
							}
						}
						aggRelV[i].addValue(valu);
					}
				}

			}
		}
	}

//
//	/**
//	 * @param value
//	 */
//	public void update(QueryStats value, boolean onlyStable) {
//		if(onlyStable && !value.isStable() && value.emptyResults()){
//			errors++;
//		}
//		else{
//			success++;
//			Entry<File,Integer[]> base = null;
//			for(Entry<File,Integer[]> ent : value.setupValues.entrySet()){
//				Integer [] qV = ent.getValue();
//				if(ent.getKey().getName().contains("2smart")){
//					base = ent;
//				}
//				DescriptiveStatistics [] aggV = setupValues.get(ent.getKey().getName());
//				if(aggV == null){
//					aggV = new DescriptiveStatistics[qV.length];
//					setupValues.put(ent.getKey(), aggV);
//				}
//				for(int i =0; i< aggV.length; i++){
//					if(aggV[i]==null) aggV[i]=new DescriptiveStatistics();
//					aggV[i].addValue(qV[i]);
//				}
//			}
//			for(Entry<File,Integer[]> ent : value.setupValues.entrySet()){
//				Integer [] qV = ent.getValue();
//
//				DescriptiveStatistics [] aggV = relativeSetupValues.get(ent.getKey().getName());
//				if(aggV == null){
//					aggV = new DescriptiveStatistics[qV.length];
//					relativeSetupValues.put(ent.getKey(), aggV);
//				}
//				for(int i =0; i< aggV.length; i++){
//					if(aggV[i]==null) aggV[i]=new DescriptiveStatistics();
//					if(ent.getKey().getName().contains("2smart")){
//						aggV[i].addValue(qV[i]);	
//					}else{
//						if(base.getValue()[i]!=0){
//							//							System.out.println((double)qV[i]+" "+(double)base.getValue()[i]);
//							//							System.out.println(base.getValue()[0]);
//							//						}else{
//							//							System.out.println((double)qV[i]+" "+(double)base.getValue()[i]);
//							double v = (double)(qV[i]-base.getValue()[i])/(double)base.getValue()[i];
//							
//							aggV[i].addValue(v);
//							if(v>700) System.out.println(value.getQueryID()+" "+v);
//						}
//					}
//
//				}
//			}
//
//		}
//	}


	public static java.text.DecimalFormat twoDForm = new java.text.DecimalFormat("#,###.##");
	public static java.text.DecimalFormat oneDForm = new java.text.DecimalFormat("#,###.#");



	public String toTexTable(){
		try{
			StringBuilder sb = new StringBuilder();
			//		sb.append("\\textbf{Setup} & \\multicolumn{2}{c}{\\textbf{results}}& \\multicolumn{2}{c}{\\textbf{ time (ms)}}& \\multicolumn{2}{c}{\\textbf{ first (ms)}}& \\multicolumn{2}{c}{\\textbf{ABox http}}&& \\multicolumn{2}{c}{\\textbf{TBox http}}& \\multicolumn{2}{c}{\\textbf{ABox stmts}}& \\multicolumn{2}{c}{\\textbf{TBox stmts}}&        \\multicolumn{2}{c}{\\textbf{ inferred}}\\\\ \\hline");
			//		sb.append("\n\\textbf{Setup} & \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}\\");

			//				sb.append("\n").append("\\multicolumn{13}{l}{Query class \\textbf{"+qc+"} with "+success+" queries}\\\\\\hline");
			for(Entry<File, DescriptiveStatistics[]> ent: setupValues.entrySet()){
				DescriptiveStatistics [] values = ent.getValue();
				String name = ent.getKey().getName();
				name = name.substring(name.lastIndexOf(".")+1);
				name = name.replaceAll("[0-9]","");
				sb.append(QueryStats1.nameMapping.get(name));
				int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
				sb.append("& ").append(twoDForm.format(values[i].getMean()));
				sb.append("& ").append(twoDForm.format(values[i].getStandardDeviation()));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
				sb.append("& ").append(oneDForm.format(values[i].getMean()/1000D));
				sb.append("& ").append(oneDForm.format(values[i].getStandardDeviation()/1000D));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.FIRST_RESULT)-1;
				sb.append("& ").append(oneDForm.format(values[i].getMean()/1000D));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_LOOKUPS)-1;
				sb.append("& ").append(twoDForm.format(values[i].getMean()));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(WebRepositoryBenchmark.CACHE_SIZE)-1;
				sb.append("& ").append(twoDForm.format(values[i].getMean()));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(ReasonerBenchmark.INFERED_STMTS)-1;
				if(values[i].getMean()>0)
					sb.append("& ").append(twoDForm.format(values[i].getMean()));
				else
					sb.append("& ").append("\\noresult");

				sb.append("\\\\ \n");
			}
			sb.append("\n");
			System.out.println(success+"/"+errors);

			return sb.toString();
		}catch(Exception e){
			return "";
		}
	}

	public String toTermTable(){
		try{
			StringBuilder sb = new StringBuilder();
			//		sb.append("\\textbf{Setup} & \\multicolumn{2}{c}{\\textbf{results}}& \\multicolumn{2}{c}{\\textbf{ time (ms)}}& \\multicolumn{2}{c}{\\textbf{ first (ms)}}& \\multicolumn{2}{c}{\\textbf{ABox http}}&& \\multicolumn{2}{c}{\\textbf{TBox http}}& \\multicolumn{2}{c}{\\textbf{ABox stmts}}& \\multicolumn{2}{c}{\\textbf{TBox stmts}}&        \\multicolumn{2}{c}{\\textbf{ inferred}}\\\\ \\hline");
			//		sb.append("\n\\textbf{Setup} & \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}& \\multicolumn{1}{c}{$\\varnothing$}&\\multicolumn{1}{c}{$\\sigma$}\\");

			//				sb.append("\n").append("\\multicolumn{13}{l}{Query class \\textbf{"+qc+"} with "+success+" queries}\\\\\\hline");
			for(Entry<File, DescriptiveStatistics[]> ent: setupValues.entrySet()){
				DescriptiveStatistics [] values = ent.getValue();
				String name = ent.getKey().getName();
				name = name.substring(name.lastIndexOf(".")+1);
				name = name.replaceAll("[0-9]","");
				sb.append(QueryStats1.nameMapping.get(name));
				
				values = termCounts.get(ent.getKey());
				sb.append("& ").append(twoDForm.format(values[1].getMean()));
				sb.append("& ").append(twoDForm.format(values[1].getStandardDeviation()));
				
				values = ent.getValue();
				int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
				sb.append("& ").append(twoDForm.format(values[i].getMean()));
				sb.append("& ").append(twoDForm.format(values[i].getStandardDeviation()));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
				sb.append("& ").append(oneDForm.format(values[i].getMean()/1000D));
				sb.append("& ").append(oneDForm.format(values[i].getStandardDeviation()/1000D));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.FIRST_RESULT)-1;
				sb.append("& ").append(oneDForm.format(values[i].getMean()/1000D));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_LOOKUPS)-1;
				sb.append("& ").append(twoDForm.format(values[i].getMean()));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(WebRepositoryBenchmark.CACHE_SIZE)-1;
				sb.append("& ").append(twoDForm.format(values[i].getMean()));

				i = QueryExecutionBenchmark.summaryOrder.indexOf(ReasonerBenchmark.INFERED_STMTS)-1;
				if(values[i].getMean()>0)
					sb.append("& ").append(twoDForm.format(values[i].getMean()));
				else
					sb.append("& ").append("\\noresult");

				sb.append("\\\\ \n");
			}
			sb.append("\n");
			System.out.println(success+"/"+errors);

			return sb.toString();
		}catch(Exception e){
			return "";
		}
	}


	public String toRecallTexTable(){
		try{
			StringBuilder sb = new StringBuilder();
			StringBuilder sbBase = new StringBuilder();

			/**
			 * Recall Results Time (ms) First (ms) HTTP ABox stmts (in k) Inferred (in k)
			 */
			for(Entry<File, DescriptiveStatistics[]> ent: relativeSetupValues.entrySet()){
				if(ent.getKey().equals(baseFile)){
					addRecallData(sbBase,ent.getKey());
				}else{
					addRecallData(sb,ent.getKey());
				}
			}
			sbBase.append(sb.toString());
			String s = sbBase.toString();
			s= s.replaceAll("%","\\%");
			return s;
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}

	private void addRecallData(StringBuilder sb, File key) {
		String name = key.getName();
		name = name.substring(name.lastIndexOf(".")+1);
		name = name.replaceAll("[0-9]","");

		sb.append(QueryStats1.nameMapping.get(name));

		int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
		if(key.equals(baseFile)){
			//recall for base is always 1
			sb.append("& 1");
		}else{
			sb.append("& ").append(percentFormat.format(relativeSetupValues.get(key)[i].getMean()));
		}
		sb.append("& ").append(twoDForm.format(setupValues.get(key)[i].getMean()));
		i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
		sb.append("& ").append(oneDForm.format(setupValues.get(key)[i].getMean()/1000D));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.FIRST_RESULT)-1;
		sb.append("& ").append(oneDForm.format(setupValues.get(key)[i].getMean()/1000D));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_LOOKUPS)-1;
		sb.append("& ").append(twoDForm.format(setupValues.get(key)[i].getMean()));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(WebRepositoryBenchmark.CACHE_SIZE)-1;
		sb.append("& ").append(twoDForm.format(setupValues.get(key)[i].getMean()/1000D));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(ReasonerBenchmark.INFERED_STMTS)-1;
		if(setupValues.get(key)[i].getMean()>0)
			sb.append("& ").append(twoDForm.format(setupValues.get(key)[i].getMean()/1000D));
		else
			sb.append("& -");

		sb.append("\\\\ \n");

	}

	public String toRelativeTexTable(){
		try{
			StringBuilder sb = new StringBuilder();
			StringBuilder sbBase = new StringBuilder();

			for(Entry<File, DescriptiveStatistics[]> ent: relativeSetupValues.entrySet()){
				if(ent.getKey().equals(baseFile)){
					addValues(sbBase,ent);
				}else
					addValues(sb,ent);
			}

			System.out.println(success+"/"+errors);

			sbBase.append(sb.toString());
			String s = sbBase.toString();
			//			System.out.println(s);
			s= s.replaceAll("%","\\%");
			//			System.out.println(s);
			return s;
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}

	public String toCombinedTexTable(){
		try{
			StringBuilder sb = new StringBuilder();
			StringBuilder sbBase = new StringBuilder();

			for(Entry<File, DescriptiveStatistics[]> ent: relativeSetupValues.entrySet()){
				if(ent.getKey().equals(baseFile)){
					addCombinedValues(sbBase,ent,setupValues.get(ent.getKey()));
				}else
					addCombinedValues(sb,ent,setupValues.get(ent.getKey()));
			}

			System.out.println(success+"/"+errors);

			sbBase.append(sb.toString());
			String s = sbBase.toString();
			//			System.out.println(s);
			s= s.replaceAll("%","\\%");
			//			System.out.println(s);
			return s;
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}

	private void addCombinedValues(StringBuilder sb,
			Entry<File, DescriptiveStatistics[]> ent, DescriptiveStatistics[] absolute) {
		DescriptiveStatistics [] values = ent.getValue();
		String name = ent.getKey().getName();
		name = name.substring(name.lastIndexOf(".")+1);
		name = name.replaceAll("[0-9]","");
		sb.append("\\texttt{").append(name).append("}");

		NumberFormat format;
		if(ent.getKey().equals(baseFile)){
			format= twoDForm;
			int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
			sb.append("& ").append(format.format(absolute[i].getMean())).append(" &");

			i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
			sb.append("& ").append(oneDForm.format(absolute[i].getMean()/1000D)).append(" &");

			i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.FIRST_RESULT)-1;
			sb.append("& ").append(oneDForm.format(absolute[i].getMean()/1000D)).append(" &");

			i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_LOOKUPS)-1;
			sb.append("& ").append(format.format(absolute[i].getMean())).append(" &");

			i = QueryExecutionBenchmark.summaryOrder.indexOf(WebRepositoryBenchmark.CACHE_SIZE)-1;
			sb.append("& ").append(format.format(absolute[i].getMean()/1000D)).append(" &");

			i = QueryExecutionBenchmark.summaryOrder.indexOf(ReasonerBenchmark.INFERED_STMTS)-1;
			if(values[i].getMean()>0)
				sb.append("& ").append(format.format(absolute[i].getMean()/1000D)).append(" &");
			else
				sb.append("& - &");
		}else{
			format = percentFormat;
			int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
			sb.append("& ").append(twoDForm.format(absolute[i].getMean())).append(" &(").append(format.format(values[i].getMean())).append(")");

			i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
			sb.append("& ").append(oneDForm.format(absolute[i].getMean()/1000D)).append(" &(").append(format.format(values[i].getMean())).append(")");

			i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.FIRST_RESULT)-1;
			sb.append("& ").append(oneDForm.format(absolute[i].getMean()/1000D)).append(" &(").append(format.format(values[i].getMean())).append(")");

			i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_LOOKUPS)-1;
			sb.append("& ").append(twoDForm.format(absolute[i].getMean())).append(" &(").append(format.format(values[i].getMean())).append(")");

			i = QueryExecutionBenchmark.summaryOrder.indexOf(WebRepositoryBenchmark.CACHE_SIZE)-1;
			sb.append("& ").append(twoDForm.format(absolute[i].getMean()/1000D)).append(" &(").append(format.format(values[i].getMean())).append(")");

			i = QueryExecutionBenchmark.summaryOrder.indexOf(ReasonerBenchmark.INFERED_STMTS)-1;
			if(values[i].getMean()>0)
				sb.append("& ").append(twoDForm.format(absolute[i].getMean()/1000D)).append(" &(").append(format.format(values[i].getMean())).append(")");
			else
				sb.append("& - &(-)");
		}
		sb.append("\\\\ \n");


	}



	static NumberFormat percentFormat = new MyNumberFormat();


	private void addDataValues(StringBuilder sb,
			Entry<File, DescriptiveStatistics[]> ent,int c,NumberFormat format) {
		DescriptiveStatistics [] values = ent.getValue();
		String name = ent.getKey().getName();
		name = name.substring(name.lastIndexOf(".")+1);
		name = name.replaceAll("[0-9]","");
		sb.append(c).append(" ").append(QueryStats1.nameMapping.get(name));
		
		int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
		sb.append(" ").append(format.format(values[i].getMean()));
//		sb.append(" ").append(format.format(values[i].getPercentile(50)));
//		sb.append(" ").append(format.format(values[i].getPercentile(75)));
//		sb.append(" ").append(format.format(values[i].getPercentile(90)));
//		sb.append(" ").append(format.format(values[i].getPercentile(99)));
//		sb.append(" ").append(format.format(values[i].getPercentile(100)));
//		

		i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
		if(format instanceof MyNumberFormat){
			sb.append(" ").append(format.format(values[i].getMean()));
//			sb.append(" ").append(format.format(values[i].getPercentile(50)));
//			sb.append(" ").append(format.format(values[i].getPercentile(75)));
//			sb.append(" ").append(format.format(values[i].getPercentile(90)));
//			sb.append(" ").append(format.format(values[i].getPercentile(99)));
//			sb.append(" ").append(format.format(values[i].getPercentile(100)));
		}
		else
			sb.append(" ").append(oneDForm.format(values[i].getMean()/1000D));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.FIRST_RESULT)-1;
		if(format instanceof MyNumberFormat)
			sb.append(" ").append(format.format(values[i].getMean()));
		else
			sb.append(" ").append(oneDForm.format(values[i].getMean()/1000D));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_LOOKUPS)-1;
		sb.append(" ").append(format.format(values[i].getMean()));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(WebRepositoryBenchmark.CACHE_SIZE)-1;
		if(format instanceof MyNumberFormat)
			sb.append(" ").append(format.format(values[i].getMean()));
		else
			sb.append(" ").append(format.format(values[i].getMean()/1000D));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(ReasonerBenchmark.INFERED_STMTS)-1;
		if(values[i].getMean()>0)
			if(format instanceof MyNumberFormat)
				sb.append(" ").append(format.format(values[i].getMean()));
			else
				sb.append(" ").append(format.format(values[i].getMean()/1000D));
		else
			sb.append(" ").append("0");

		sb.append("\n");
	}

	private void addDataPercValues(StringBuilder sb,
			Entry<File, DescriptiveStatistics[]> ent,int c,NumberFormat format) {
		DescriptiveStatistics [] values = ent.getValue();
		String name = ent.getKey().getName();
		name = name.substring(name.lastIndexOf(".")+1);
		name = name.replaceAll("[0-9]","");
		sb.append(c).append(" ").append(QueryStats1.nameMapping.get(name));
		
		int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
		sb.append(" ").append(format.format(values[i].getMean()));
		sb.append(" ").append(format.format(values[i].getPercentile(50)));
		sb.append(" ").append(format.format(values[i].getPercentile(75)));
		sb.append(" ").append(format.format(values[i].getPercentile(90)));
		sb.append(" ").append(format.format(values[i].getPercentile(99)));
		sb.append(" ").append(format.format(values[i].getPercentile(100)));
		

		i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
//		if(format instanceof MyNumberFormat){
			sb.append(" ").append(oneDForm.format(values[i].getPercentile(50)));
			sb.append(" ").append(oneDForm.format(values[i].getPercentile(75)));
			sb.append(" ").append(oneDForm.format(values[i].getPercentile(90)));
			sb.append(" ").append(oneDForm.format(values[i].getPercentile(99)));
			sb.append(" ").append(oneDForm.format(values[i].getPercentile(100)));
//		}
//		else
//			sb.append(" ").append(format.format(values[i].getMean()/1000D));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.FIRST_RESULT)-1;
		if(format instanceof MyNumberFormat)
			sb.append(" ").append(format.format(values[i].getMean()));
		else
			sb.append(" ").append(oneDForm.format(values[i].getMean()/1000D));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_LOOKUPS)-1;
		sb.append(" ").append(format.format(values[i].getMean()));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(WebRepositoryBenchmark.CACHE_SIZE)-1;
		if(format instanceof MyNumberFormat)
			sb.append(" ").append(format.format(values[i].getMean()));
		else
			sb.append(" ").append(format.format(values[i].getMean()/1000D));

		i = QueryExecutionBenchmark.summaryOrder.indexOf(ReasonerBenchmark.INFERED_STMTS)-1;
		if(values[i].getMean()>0)
			if(format instanceof MyNumberFormat)
				sb.append(" ").append(format.format(values[i].getMean()));
			else
				sb.append(" ").append(format.format(values[i].getMean()/1000D));
		else
			sb.append(" ").append("0");
		sb.append("\n");
	}
	
	private void addDataTermPercValues(StringBuilder sb,
			Entry<File, DescriptiveStatistics[]> ent,int c,NumberFormat format) {
		DescriptiveStatistics [] values = termCounts.get(ent.getKey());
		String name = ent.getKey().getName();
		name = name.substring(name.lastIndexOf(".")+1);
		name = name.replaceAll("[0-9]","");
		sb.append(c).append(" ").append(QueryStats1.nameMapping.get(name));
		
//		int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
//		sb.append(" ").append(format.format(values[i].getMean()));
//		sb.append(" ").append(format.format(values[0].getPercentile(50)));
//		sb.append(" ").append(format.format(values[0].getPercentile(75)));
//		sb.append(" ").append(format.format(values[0].getPercentile(90)));
//		sb.append(" ").append(format.format(values[0].getPercentile(99)));
//		sb.append(" ").append(format.format(values[0].getPercentile(100)));
//		
		sb.append(" ").append(format.format(values[1].getPercentile(50)));
		sb.append(" ").append(format.format(values[1].getPercentile(75)));
		sb.append(" ").append(format.format(values[1].getPercentile(90)));
		sb.append(" ").append(format.format(values[1].getPercentile(99)));
		sb.append(" ").append(format.format(values[1].getPercentile(100)));
		
		sb.append("\n");
	}
	

	private void addValues(StringBuilder sb,
			Entry<File, DescriptiveStatistics[]> ent) {
		DescriptiveStatistics [] values = ent.getValue();
		String name = ent.getKey().getName();
		name = name.substring(name.lastIndexOf(".")+1);
		name = name.replaceAll("[0-9]","");
		sb.append("\\texttt{").append(QueryStats1.nameMapping.get(name)).append("}");

		NumberFormat format;
		if(ent.getKey().equals(baseFile)){
			format= twoDForm;
			int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
			sb.append("& ").append(format.format(values[i].getMean()));

			i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
			sb.append("& ").append(oneDForm.format(values[i].getMean()/1000D));

			i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.FIRST_RESULT)-1;
			sb.append("& ").append(oneDForm.format(values[i].getMean()/1000D));

			i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_LOOKUPS)-1;
			sb.append("& ").append(format.format(values[i].getMean()));

			i = QueryExecutionBenchmark.summaryOrder.indexOf(WebRepositoryBenchmark.CACHE_SIZE)-1;
			sb.append("& ").append(format.format(values[i].getMean()/1000));

			i = QueryExecutionBenchmark.summaryOrder.indexOf(ReasonerBenchmark.INFERED_STMTS)-1;
			if(values[i].getMean()>0)
				sb.append("& ").append(format.format(values[i].getMean()/1000));
			else
				sb.append("& ").append("\\noresult");
		}else{
			format = percentFormat;
			int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
			sb.append("& ").append(format.format(values[i].getMean()));

			i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
			sb.append("& ").append(oneDForm.format(values[i].getMean()));

			i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.FIRST_RESULT)-1;
			sb.append("& ").append(oneDForm.format(values[i].getMean()));

			i = QueryExecutionBenchmark.summaryOrder.indexOf(SourceLookupBenchmark.TOTAL_LOOKUPS)-1;
			sb.append("& ").append(format.format(values[i].getMean()));

			i = QueryExecutionBenchmark.summaryOrder.indexOf(WebRepositoryBenchmark.CACHE_SIZE)-1;
			sb.append("& ").append(format.format(values[i].getMean()));

			i = QueryExecutionBenchmark.summaryOrder.indexOf(ReasonerBenchmark.INFERED_STMTS)-1;
			if(values[i].getMean()>0)
				sb.append("& ").append(format.format(values[i].getMean()));
			else
				sb.append("& ").append("\\noresult");
		}
		sb.append("\\\\ \n");

	}

	public String toDataTable(){
		try{
			StringBuilder sb = new StringBuilder();
			StringBuilder sbBase = new StringBuilder();

			int c=0;
			for(Entry<File, DescriptiveStatistics[]> ent: setupValues.entrySet()){
				addDataValues(sb,ent,c++,twoDForm);
			}
			sbBase.append(sb.toString());
			String s = sbBase.toString();
			s= s.replaceAll("%","").replaceAll("&", "").replaceAll("\\\\", "");
			//			s = s.replaceAll("+", "").replaceAll("-", "");

			return s;
		}catch(Exception e){
			System.out.println( e.getClass().getSimpleName()+" msg:"+e.getMessage());
		}
		return "";
	}
	
	public String toDataTablePerc(){
		try{
			StringBuilder sb = new StringBuilder();
			StringBuilder sbBase = new StringBuilder();

			int c=0;
			for(Entry<File, DescriptiveStatistics[]> ent: relativeSetupValues.entrySet()){
				addDataPercValues(sb,ent,c++,twoDForm);
			}
			sbBase.append(sb.toString());
			String s = sbBase.toString();
			s= s.replaceAll("%","").replaceAll("&", "").replaceAll("\\\\", "");
			//			s = s.replaceAll("+", "").replaceAll("-", "");

			return s;
		}catch(Exception e){
			System.out.println( e.getClass().getSimpleName()+" msg:"+e.getMessage());
		}
		return "";
	}
	
	
	public String toDataTermTablePerc(){
		try{
			StringBuilder sb = new StringBuilder();
			StringBuilder sbBase = new StringBuilder();

			int c=0;
			for(Entry<File, DescriptiveStatistics[]> ent: setupValues.entrySet()){
				addDataTermPercValues(sb,ent,c++,twoDForm);
			}
			sbBase.append(sb.toString());
			String s = sbBase.toString();
			s= s.replaceAll("%","").replaceAll("&", "").replaceAll("\\\\", "");
			//			s = s.replaceAll("+", "").replaceAll("-", "");

			return s;
		}catch(Exception e){
			System.out.println( e.getClass().getSimpleName()+" msg:"+e.getMessage());
		}
		return "";
	}
	

	public String toRelDataTable(){
		try{
			StringBuilder sb = new StringBuilder();
			StringBuilder sbBase = new StringBuilder();

			int c=0;
			for(Entry<File, DescriptiveStatistics[]> ent: relativeSetupValues.entrySet()){
				if(ent.getKey().equals(baseFile)){
					//					addValues(sbBase,ent);
				}else
					addDataValues(sb,ent,c++,new MyNumberFormat(false));
			}
			sbBase.append(sb.toString());
			String s = sbBase.toString();
			s= s.replaceAll("%","").replaceAll("&", "").replaceAll("\\\\", "");
			//			s = s.replaceAll("+", "").replaceAll("-", "");

			return s;
		}catch(Exception e){
			System.out.println( e.getClass().getSimpleName()+" msg:"+e.getMessage());
		}
		return "";
	}
	
	
	public String toRelPercDataTable(){
		try{
			StringBuilder sb = new StringBuilder();
			StringBuilder sbBase = new StringBuilder();

			int c=0;
			for(Entry<File, DescriptiveStatistics[]> ent: relativeSetupValues.entrySet()){
				if(ent.getKey().equals(baseFile)){
					//					addValues(sbBase,ent);
				}else
					addDataPercValues(sb,ent,c++,twoDForm);
			}
			sbBase.append(sb.toString());
			String s = sbBase.toString();
			s= s.replaceAll("%","").replaceAll("&", "").replaceAll("\\\\", "");
			//			s = s.replaceAll("+", "").replaceAll("-", "");

			return s;
		}catch(Exception e){
			System.out.println( e.getClass().getSimpleName()+" msg:"+e.getMessage());
		}
		return "";
	}


	public String toString(DescriptiveStatistics[] desc) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i< desc.length;i++){
			DescriptiveStatistics d = desc[i];
			if(i == 4 || i==5||i==6||i==7|i==8) continue; // abox lookups

			if(i==1 || i==2)
				sb.append(" ").append(twoDForm.format(d.getMean()/1000D)).append(" ").append(twoDForm.format(d.getStandardDeviation()/1000D));
			else
				sb.append(" ").append(twoDForm.format(d.getMean())).append(" ").append(twoDForm.format(d.getStandardDeviation()));
		}
		return sb.toString();
	}

	public String diffToString(DescriptiveStatistics[] desc, DescriptiveStatistics[] base) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i< desc.length;i++){
			DescriptiveStatistics d = desc[i];
			double meanDiff = (d.getMean()/base[i].getMean())-1;
			double sdDiff = (d.getStandardDeviation()/base[i].getStandardDeviation())-1;

			if(i == 4 || i==5||i==6||i==7|i==8) continue; // abox lookups

			if(i==1 || i==2){
				if(meanDiff!=0) meanDiff = meanDiff/1000D;
				if(sdDiff!=0) meanDiff = sdDiff/1000D;
			}

			sb.append(" ").append(twoDForm.format(meanDiff)).append(" ").append(twoDForm.format(sdDiff));

		}
		return sb.toString();
	}

	public String resultTimeTatioString() {
		String resString=qc;
		String header = "";
		double results =0, time =0;
		for(Entry<File, DescriptiveStatistics[]> ent: setupValues.entrySet()){
			
			String name = ent.getKey().getName();
			name = name.substring(name.lastIndexOf(".")+1);
			name = name.replaceAll("[0-9]","");
			header+="&"+(QueryStats1.nameMapping.get(name));
			
			int i =QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_RESULTS)-1;
			results = ent.getValue()[i].getMean();
			
			i = QueryExecutionBenchmark.summaryOrder.indexOf(QueryExecutionBenchmark.TOTAL_TIME)-1;
			time = (ent.getValue()[i].getMean()/1000D);
			
			resString+= "&"+twoDForm.format(results/time);
		}
		return header+"\n"+resString;
		

		

	}
}
