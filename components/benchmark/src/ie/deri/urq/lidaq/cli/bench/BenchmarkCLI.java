package ie.deri.urq.lidaq.cli.bench;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.query.QueryConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;


public abstract class  BenchmarkCLI{


	private static final Logger logger = Logger.getLogger(BenchmarkCLI.class.getSimpleName());
	//	abstract public QueryConfig parseQueryConfig(CommandLine cmd, File queryFile) throws IOException;

	//	abstract public QueryExecutionBenchmark execute(QueryConfig qc, File out) throws Exception;

	abstract public Options getOption();
	public void setup(){
		getBenchmarkEngine().setup();
	}
	public void shutdown(){
		getBenchmarkEngine().shutdown();
	}

	protected void updateLogger(File file) {
		FileHandler handler;
		try {
			handler = new FileHandler(file.getAbsolutePath()+".log");
			java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
			for(Handler h :root.getHandlers()){
				if( h instanceof FileHandler){
					root.removeHandler(h);
				}
			}
			handler.setFormatter(new SimpleFormatter());
			root.addHandler(handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printOptions() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(this.getClass().getSimpleName(), getOption() ,true);
	}

	public Object getCommand() {
		return this.getClass().getSimpleName();
	}

	abstract public String getDescription();

	public void execute(File benchDir, File query,
			CommandLine cmd){
		if(!benchDir.exists()) benchDir.mkdirs();

		File summaryFile = new File(benchDir,"summary.txt");
		PrintWriter pw = null;

		File processLog = new File(benchDir,"log_process.txt");

		PrintWriter log;
		try {
			if(summaryFile.exists())
				pw = new PrintWriter(new FileOutputStream(summaryFile, true));
			else
				pw = new PrintWriter(new FileOutputStream(summaryFile));

			if(query.isDirectory()){
				logger.info(">>Query dir "+query);
				log = new PrintWriter(new FileOutputStream(processLog));
				logger.info("  >>Logging process to "+processLog);
				for(File queryFile: query.listFiles()){
					if(!queryFile.getName().endsWith(".sparql")) continue;
					try{
						runQuery(cmd, queryFile,benchDir,pw,log);
					}finally{
						System.gc();System.gc();
					}
				}
				log.close();
			}
			else if(query.getName().endsWith(".sparql")){
				logger.info(">>Query file "+query);
				try{
					runQuery(cmd, query,benchDir,pw,null);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			pw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	private void runQuery(CommandLine cmd, File queryFile, File benchDir, PrintWriter pw, PrintWriter log) {
		try{

			QueryConfig qc = getBenchmarkEngine().parseQueryConfig(cmd, queryFile);
			
			QueryExecutionBenchmark qeb = getBenchmarkEngine().benchmark(qc,benchDir);
			
			logger.info("[QEB] "+qeb.oneLineSummary(" "));
			
			if(pw!=null){
				pw.println(qeb.oneLineSummary(" "));
				pw.flush();
			}
			if(log!=null){
				log.println(queryFile);
				log.flush();
			}
		}catch (Exception e) {
			logger.warning("[EXCEPTION] "+e.getClass().getSimpleName()+" msg:"+e.getMessage());
			e.printStackTrace();
		}

	}
	abstract protected BenchmarkEngine getBenchmarkEngine();




}