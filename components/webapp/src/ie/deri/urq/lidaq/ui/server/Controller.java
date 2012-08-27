package ie.deri.urq.lidaq.ui.server;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.LinkedDataQueryEngine;
import ie.deri.urq.lidaq.query.OutputFormats;
import ie.deri.urq.lidaq.query.ResultSetWrapper;
import ie.deri.urq.lidaq.repos.QueryBasedSourceSelectionStrategies;
import ie.deri.urq.lidaq.repos.SourceSelectionStrategy;
import ie.deri.urq.lidaq.repos.WebRepository;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;

public class Controller extends HttpServlet {
	private final Logger log = Logger.getLogger(Controller.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File _indicesDir;

	private ServletContext _ctx;
	private QueryLogger logger ;
	private LinkedDataQueryEngine _lidaq;
	private WebRepositoryManager _wrm;

	/** private variables **/

	@Override
	public void init(ServletConfig config) throws ServletException {
		// initialise here all objects needed for the processing
		_ctx = config.getServletContext();
		
		_lidaq = (LinkedDataQueryEngine) _ctx.getAttribute(Initialiser.LIDQ);
		_wrm = (WebRepositoryManager) _ctx.getAttribute(Initialiser.WEBREP);
		logger = (QueryLogger) _ctx.getAttribute(Initialiser.LOGGER);
		
	}

	
	public File getIndexDir() {
		return _indicesDir;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doProcess(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doProcess(req, resp);

	}

	private void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		String query = request.getParameter("query");
		String charenc = request.getCharacterEncoding();
		String ip = request.getRemoteAddr();
		String host = request.getRemoteHost();
		long timestamp = System.currentTimeMillis();

		logger.log("["+timestamp+"]-[REQUEST] "+ip+" ("+host+") "+request.getRequestURI()+request.getQueryString());
		log.info("["+timestamp+"]-[REQUEST] "+ip+" ("+host+") "+request.getRequestURI()+request.getQueryString());
//		logHeaders(request,timestamp);

		long start = System.currentTimeMillis();
		if(query!=null){
//			logger.log("["+timestamp+"]-[QUERY]>"+query.trim());
			log.info("["+timestamp+"]-[QUERY]>"+query.trim());
			if (charenc == null) {
				charenc = "ISO-8859-1";
			}
			try {
				query = new String(query.getBytes(charenc),"UTF8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			String accept = request.getParameter("accept");
			if (accept == null) {
				accept = request.getHeader("Accept");
			}

//			QueryConfig qc = new QueryConfig(query);
//			QueryExecutionBenchmark b = new QueryExecutionBenchmark(""+timestamp,query);
//			qc.queryBenchmark(b);
//			String srcSel = request.getParameter("srcSel");
//			if(srcSel.equals("smart")){
//				qc.sourceSelection(QueryBasedSourceSelectionStrategies.SMART);
//			}else if(srcSel.equals("all")){
//				qc.sourceSelection(BasicSourceSelectionStrategies.ALL);
//			}else if(srcSel.equals("so")){
//				qc.sourceSelection(BasicSourceSelectionStrategies.AUTH);
//			}
//			qc.setFollowSeeAlso(request.getParameter("seeAlso")!=null);
//			if(request.getParameter("rdfs")!=null && request.getParameter("sameAs")!=null )
//				qc.reasoningMode(REASONING_MODE.ALL);
//			else if (request.getParameter("rdfs")!=null )
//				qc.reasoningMode(REASONING_MODE.RDFS);
//			else if(request.getParameter("sameAs")!=null )
//				qc.reasoningMode(REASONING_MODE.OWL);
//			else
//				qc.reasoningMode(REASONING_MODE.OFF);

			SourceSelectionStrategy srcSel = QueryBasedSourceSelectionStrategies.SMART;
			boolean seeAlso = true;
			REASONING_MODE rMode = REASONING_MODE.OFF;
//			log(qc.toString());
			boolean any23 = false;
			WebRepository wr = null;
			try {
				wr = _wrm.getRepository();
				ResultSetWrapper results = _lidaq.lidaq(query,""+timestamp, srcSel, seeAlso, rMode,any23, wr);
				
				OutputStream os = response.getOutputStream();
				ResultSetFormat resultFormat = parseResultFormat(accept);
				ResultSetFormatter.output(os, results, resultFormat);
				wr.close();
				log.info("["+timestamp+"]-[DETAILS] "+results.getQueryConfig().toString());
				logger.log("["+timestamp+"]-[RESULTS] "+results.getQueryConfig().oneLineSummary());
				log.info("["+timestamp+"]-[RESULTS] "+results.getQueryConfig().getBenchmark().oneLineSummary(" "));
			} catch (Exception e) {
				//        		log("Exception",e);
				e.printStackTrace();
				logger.log("["+timestamp+"]-[EXPECTION] "+e.getClass().getSimpleName());
				log.info("["+timestamp+"]-[EXPECTION] "+e.getClass().getSimpleName());
				response.sendError(response.SC_INTERNAL_SERVER_ERROR, getStackTraceAsString(e));
			}finally{
				wr.close();
			}
		} else {
			logger.log("["+timestamp+"]-[ERROR] query was null");
			log.info("["+timestamp+"]-[ERROR] query was null");
			response.sendError(response.SC_BAD_REQUEST, "Please specify the 'query' parameter");
		}
		log.info("["+timestamp+"]-[TIME] "+(System.currentTimeMillis()-start)+" ms");
//		logger.log("["+timestamp+"]-[TIME] "+(System.currentTimeMillis()-start)+" ms");
	}
	
	private ResultSetFormat parseResultFormat(String format) {
		ResultSetFormat s = null;
		if(format.equals("application/n3")){
			s = OutputFormats.lookup("n3");
		}else if(format.equals("application/rdf+xml")){
			s = OutputFormats.lookup("rdf/xml");
		}else if(format.equals("application/sparql-results+xml")){
			s = OutputFormats.lookup("xml");
		}else if(format.equals("application/sparql-results+json")){
			s = OutputFormats.lookup("json");
		}else if(format.equals("text/plain")){
			s = OutputFormats.lookup("text");
		}  
		if(s == null)
			throw new RuntimeException("Unrecognized output format: "+format);
		return s;
	}

	/**
	 * @param request
	 * @param timestamp 
	 */
	private void logHeaders(HttpServletRequest request, long timestamp) {
		StringBuffer sb = new StringBuffer();
		Enumeration enum1 = request.getHeaderNames();
		for (; enum1.hasMoreElements(); ) {
			// Get the name of the request header
			String name = (String)enum1.nextElement();
			sb.append("["+timestamp+"]-[HEADERS]  ").append(name);
			Enumeration valuesEnum = request.getHeaders(name);
			for (; valuesEnum.hasMoreElements(); ) {
				String  value = (String)valuesEnum.nextElement();
				sb.append("\t").append(value);
			}
		}
		logger.log(sb.toString());
	}

	private static String getStackTraceAsString(Exception e) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(bytes, true);
		e.printStackTrace(writer);
		return bytes.toString();
	}



	private String debugParameters(HttpServletRequest req) {

		String para = "requestedURI: " + req.getRequestURI() + "\n"
		+ "Session " + req.getSession().getId() + "\n";
		Enumeration paraNames = req.getParameterNames();
		para += "Parameters\n";
		while (paraNames.hasMoreElements()) {
			Object name = paraNames.nextElement();
			para += " " + name + ": " + req.getParameter(name.toString())
			+ "\n";
		}
		para += "Session beans\n";
		Enumeration sessionParaNames = req.getSession().getAttributeNames();
		while (sessionParaNames.hasMoreElements()) {
			Object name = sessionParaNames.nextElement();
			para += " " + name + ": "
			+ req.getSession().getAttribute((String) name) + "\n";
		}
		return para;
	}
}