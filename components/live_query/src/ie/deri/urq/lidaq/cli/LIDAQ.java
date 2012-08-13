/**
 *
 */
package ie.deri.urq.lidaq.cli;

import ie.deri.urq.lidaq.LTBQEQueryEngine;
import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.log.LIDAQLOGGER;
import ie.deri.urq.lidaq.query.LTBQEQueryConfig;
import ie.deri.urq.lidaq.query.ResultSetWrapper;
import ie.deri.urq.lidaq.repos.WebRepository;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 17, 2010
 */
public class LIDAQ extends CLIObject {

	private static final Logger logger = Logger.getLogger(LIDAQ.class.getName());

	public String getDescription() {
		return "SPARQL queries over Linked Data";
	}

	protected void addOptions(Options opts) {
		opts.addOption(ARGUMENTS.OPTION_SPARQL_QUERY);
		opts.addOption(ARGUMENTS.OPTION_PROXY_HOST);
		opts.addOption(ARGUMENTS.OPTION_PROXY_PORT);
		opts.addOption(ARGUMENTS.OPTION_DEBUG);
		opts.addOption(ARGUMENTS.OPTION_OUTPUTFILE);
		
		opts.addOption(LIDAQ_ARGUMENTS.OPTION_TIMEOUT);
		opts.addOption(LIDAQ_ARGUMENTS.OPTION_SOURCE_SELECTION);
		opts.addOption(LIDAQ_ARGUMENTS.OPTION_REASONING);
		opts.addOption(LIDAQ_ARGUMENTS.OPTION_FORMAT);
		opts.addOption(LIDAQ_ARGUMENTS.OPTION_FOLLOW_SEEALSO);
		opts.addOption(LIDAQ_ARGUMENTS.OPTION_ENABLE_ANY23);
	}

	/* (non-Javadoc)
	 * @see ie.deri.urq.lodq.cli.CLIObject#execute(org.apache.commons.cli.CommandLine)
	 */
	@Override
	protected void execute(CommandLine cmd) {
		try {
			//proxy
			long start, end;
			String proxyPort = ie.deri.urq.lidaq.cli.CLIObject.getOptionValue(cmd, ARGUMENTS.PARAM_PROXY_PORT,null);
			String proxyHost =ie.deri.urq.lidaq.cli.CLIObject.getOptionValue(cmd, ARGUMENTS.PARAM_PROXY_HOST,null);
			
			if(CLIObject.hasOption(cmd,ARGUMENTS.PARAM_DEBUG)){
				LIDAQLOGGER.lodqDebug();
			}
			else{
				LIDAQLOGGER.setDefaultLogging();
			}
			WebRepositoryManager wrm = new WebRepositoryManager(proxyHost, proxyPort);
			LTBQEQueryEngine lodq = new LTBQEQueryEngine(wrm);
			
			//QUERY MANDATORY
			File queryFile = new File(CLIObject.getOptionValue(cmd,ARGUMENTS.PARAM_SPARQL_QUERY));
			
			LTBQEQueryConfig qc = LTBQEQueryConfig.parseQueryConfig(cmd,queryFile);
			
			start = System.currentTimeMillis();
			int count =0;

			WebRepository wr = wrm.getRepository();
			QueryExecutionBenchmark qeb = new QueryExecutionBenchmark();
			qc.setQueryExecutionBenchmark(qeb);
			
//			TimeOutThread timeOutThread = null;
//			if(qc.getTimeout()!=-1){
//				timeOutThread = new TimeOutThread(qc.getTimeout(),wr);
//				timeOutThread.start();
//			}
			ResultSetWrapper resSet = lodq.executeSelect(qc.getQuery(), qc, wr);
			
			//output
			OutputStream os = System.out;
			if(CLIObject.hasOption(cmd,ARGUMENTS.PARAM_OUTPUTFILE)){
				File out = new File(CLIObject.getOptionValue(cmd,ARGUMENTS.PARAM_OUTPUTFILE));
				out.getParentFile().mkdirs();
				os = new FileOutputStream(out);
				qeb.setResultOutput(out);
			}
			
			ResultSetFormat resultFormat = LTBQEQueryConfig.parseResultFormat(CLIObject.getOptionValue(cmd,LIDAQ_ARGUMENTS.PARAM_FORMAT, "rdf/n3"));
			ResultSetFormatter.output(os, resSet, resultFormat);
			count = resSet.size();
			os.close();
			wr.close();
			
			end = System.currentTimeMillis();
			qeb.setResultSize(count);
			qeb.setFirstResult(resSet.getFirstResult());
			qeb.setTotatlTime(end-start);
			logger.info("[STOP] time "+(end-start)+" ms for "+count+" results");

			System.err.println(qc);
			wrm.shutdown();
			if(qc.getQueryExecutionBenchmark()!=null)
				System.err.println(qc.getQueryExecutionBenchmark().oneLineSummary(" "));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}