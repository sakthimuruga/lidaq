/**
 *
 */
package ie.deri.urq.lidaq.cli;

import ie.deri.urq.lidaq.Utils;
import ie.deri.urq.lidaq.cli.bench.BENCH_ARGUMENTS;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 17, 2010
 */
public class AKSWBenchGet extends CLIObject {
	private static final Logger logger = Logger.getLogger(AKSWBenchGet.class.getName());

	/* (non-Javadoc)
	 * @see ie.deri.urq.lodq.cli.CLIObject#getDescription()
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		return "SPARQL queries over Linked Data";
	}

	/* (non-Javadoc)
	 * @see ie.deri.urq.lodq.cli.CLIObject#addOptions(org.apache.commons.cli.Options)
	 */
	protected void addOptions(Options opts) {
		opts.addOption(BENCH_ARGUMENTS.OPTION_INPUT_FOLDER);
		opts.addOption(BENCH_ARGUMENTS.OPTION_OUTPUTFILE);
		//		opts.addOption(BENCH_ARGUMENTS.OPTION_EVAL_STABLE);

	}

	final String endpointURL = "http://dbpedia.org/sparql";

	/* (non-Javadoc)
	 * @see ie.deri.urq.lodq.cli.CLIObject#execute(org.apache.commons.cli.CommandLine)
	 */
	protected void execute(CommandLine cmd) {
		//proxy

		File inDir = new File(CLIObject.getOptionValue(cmd,BENCH_ARGUMENTS.PARAM_INPUT_FOLDER));
		File outDir = new File(CLIObject.getOptionValue(cmd,BENCH_ARGUMENTS.PARAM_OUTPUTFILE));

		
		File tmp = new File(outDir,"tmp");
		tmp.mkdirs();
		Scanner s;
//		try {
//			s = new Scanner(inDir);
//			int count = 0;
//			while(s.hasNextLine()){
//				String [] tt = s.nextLine().split("\t");
//				System.out.println(tt[0]);
//				System.out.println(new File(tmp,"dbspb-"+count+"-"+tt[0]+".sparql"));
//				System.out.println(tt[1]);
//				System.out.println(new File(tmp,"dbspb-"+count+"-"+tt[0]+".aux.sparql"));
//				System.out.println(tt[2]);
//				System.out.println("_________");
//
//				PrintStream ps = new PrintStream(new File(tmp,"dbspb-"+count+"-"+tt[0]+".sparql"));
//				ps.println(tt[1]);ps.close();
//
//				ps = new PrintStream(new File(tmp,"dbspb-"+count+"-"+tt[0]+".aux.sparql"));
//				ps.println(tt[2]);ps.close();
//				count++;
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("++++++++ QUERYING ++++++++");
//				for(File f: tmp.listFiles()){
//					if(f.getName().endsWith(".aux.sparql")){
//						try{
//							System.out.println("Query for "+f);
//							String name = f.getName().substring(0,f.getName().indexOf(".aux.sparql"));
//							String query = Utils.readFileContent(f);
//							System.out.println(query);
//							QueryExecution qe=null;
//							try{
//								qe = QueryExecutionFactory.sparqlService(endpointURL,query);
//								ResultSet rsStatic = qe.execSelect();
//								PrintStream ps = new PrintStream(new File(tmp,name+".entities"));
//								ResultSetFormatter.outputAsTSV(ps, rsStatic);
//								ps.close();
//								System.out.println("Found "+rsStatic.getRowNumber()+" results, stored in "+new File(tmp,name+".entities"));
//							}catch(Exception e){
//								qe.close();
//								System.out.println(e.getClass().getSimpleName()+" msg: "+e.getMessage()+" for "+f);
//							}
//							try {
//								Thread.sleep(2000);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//						}
//						catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
		System.out.println("++++++++ SAMPLING ++++++++");
		for(File f: tmp.listFiles()){
			//&&(f.getName().contains("bspb-23-4-TriplePatterns")    ||f.getName().contains("dbspb-4-union,distinct") )
			if(f.getName().endsWith(".entities")){
				System.out.println("Sampling "+f);
				String name = f.getName().substring(0,f.getName().indexOf(".entities"));
				Scanner sc;
				try {
					sc = new Scanner(f);
					String [] header = null;
					Set<String[]> res = new HashSet<String[]>();
					while(sc.hasNextLine()){
						String []tt = sc.nextLine().split("\t");
						if(header == null) header = tt;
						else{
							res.add(tt);
						}
					}
					if(res.size()>=25){
						File qDir = new File(outDir,name);
						qDir.mkdirs();


						Set<Integer> mappings = new HashSet<Integer>();
						while((mappings.size()<25) || mappings.size()==res.size()){
							mappings.add( (int) (Math.random()* res.size()));
						}
						System.out.println(mappings);
						System.out.println(Arrays.toString(header));
						//						System.out.println(tmplQuery);
						for(int i: mappings){
							int c = 0;
							for(String[] r: res){
								if(c==i){
									String tmplQuery = Utils.readFileContent(new File(tmp,name+".sparql"));
									if(header != null){
										for(int a=0; a < header.length; a++){
											String replace = header[a].trim().substring(1,header[a].length());
											tmplQuery=tmplQuery.replace("%%"+replace+"%%", r[a]);
										}
									}
									PrintStream ps = new PrintStream(new File(qDir,i+".sparql"));
									ps.println(tmplQuery);ps.close();
								}c++;
							}
						}
					}
					else{
						System.out.println("No results for "+f);
					}
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	//	@Override
	//	public QueryConfig parseQueryConfig(CommandLine cmd, File queryFile) {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}
	//
	//	@Override
	//	public QueryExecutionBenchmark benchmark(QueryConfig qc, File outDir) {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}
	//
	//	@Override
	//	public void shutdown() {
	//		// TODO Auto-generated method stub
	//		
	//	}
}