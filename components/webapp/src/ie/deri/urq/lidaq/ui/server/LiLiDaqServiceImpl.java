package ie.deri.urq.lidaq.ui.server;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;
import ie.deri.urq.lidaq.LinkedDataQueryEngine;
import ie.deri.urq.lidaq.Utils;
import ie.deri.urq.lidaq.repos.QueryBasedSourceSelectionStrategies;
import ie.deri.urq.lidaq.repos.SourceSelectionStrategy;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;
import ie.deri.urq.lidaq.source.BasicSourceSelectionStrategies;
import ie.deri.urq.lidaq.ui.client.LiLiDaqService;
import ie.deri.urq.lidaq.ui.client.QueryException;
import ie.deri.urq.lidaq.ui.shared.Bindings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class LiLiDaqServiceImpl extends RemoteServiceServlet implements
LiLiDaqService {
	private static final Logger log = Logger
			.getLogger(LiLiDaqServiceImpl.class.getName());

	Map<String, ResultFusion > runs = new HashMap<String,ResultFusion>();
	private WebRepositoryManager _wrm;

	private ServletContext _ctx;

	private LinkedDataQueryEngine _lidaq;

	private QueryLogger logger;

	private HashMap<String, String> tmplQueries = new HashMap<String, String>();


	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		_ctx = config.getServletContext();
		_lidaq = (LinkedDataQueryEngine) _ctx.getAttribute(Initialiser.LIDQ);
		_wrm = (WebRepositoryManager) _ctx.getAttribute(Initialiser.WEBREP);
		logger = (QueryLogger) _ctx.getAttribute(Initialiser.LOGGER);

		for(File f: new File(config.getServletContext().getRealPath("tmplQueries")).listFiles()){
			String label = f.getName();
			if(!label.contains(".sparql"))continue;
			label = label.substring(0,label.indexOf(".sparql"));
			try {
				tmplQueries.put(label.replaceAll("_", " "),Utils.readFileContent(f));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		log.info("[INIT] lidaq:"+_lidaq+" wrm:"+_wrm+" logger:"+logger);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		_wrm.shutdown();
	}


	/* (non-Javadoc)
	 * @see ie.deri.urq.lilidaq.ui.client.LiLiDaqService#sparqlLidaq(java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String)
	 */
	@Override
	public String[] executeQuery(String q, String v_srcSel, boolean seeAlso,
			String v_rMode, HashMap<String, String> eps,boolean any23On) throws QueryException {
		try{
			String queryID = ""+System.currentTimeMillis();
			REASONING_MODE rMode;
			SourceSelectionStrategy srcSel;
			if(v_srcSel.equals("smart")){
				srcSel = QueryBasedSourceSelectionStrategies.SMART;
			}else if(v_srcSel.equals("all")){
				srcSel = BasicSourceSelectionStrategies.ALL;
			}else if(v_srcSel.equals("onlySrc")){
				srcSel = BasicSourceSelectionStrategies.ONLY_SOURCES;
			}
			else if(v_srcSel.equals("so")){
				srcSel = BasicSourceSelectionStrategies.AUTH;
			}
			else
				srcSel = BasicSourceSelectionStrategies.ONLY_SOURCES;

			if(v_rMode.equalsIgnoreCase("all"))
				rMode=REASONING_MODE.ALL;
			else if (v_rMode.equalsIgnoreCase("rdfs"))
				rMode=REASONING_MODE.RDFS;
			else if(v_rMode.equalsIgnoreCase("owl") )
				rMode = REASONING_MODE.OWL;
			else
				rMode = REASONING_MODE.OFF;

			log.info("[EXECUTE] ["+queryID+"] endpoints: "+eps+" query "+q+" srcSel:"+srcSel+" seeAlso:"+seeAlso+" rMode:"+rMode);

			ResultFusion rf = new ResultFusion(q,queryID,eps,srcSel,seeAlso,rMode,_lidaq,_wrm,logger,any23On);

			String[] s = rf.getVarList();
			String [] res = new String[s.length+1];
			res[0] = queryID;
			for(int i=0; i< s.length; i++){
				res[i+1]= s[i];
			}

			runs.put(queryID, rf);

			rf.start();

			return res;
		}catch(RuntimeException e){
			log.info("Exception "+e.getClass().getSimpleName()+" msg:"+e.getMessage());
			throw new QueryException(e.getClass().getSimpleName()+" \nmsg: "+e.getMessage()); 
		}catch(Exception e){
			log.info("Exception "+e.getClass().getSimpleName()+" msg:"+e.getMessage());
			throw new QueryException(e.getClass().getSimpleName()+" \nmsg: "+e.getMessage()); 
		}
	}

	/* (non-Javadoc)
	 * @see ie.deri.urq.lilidaq.ui.client.GreetingService#getNewResults(java.lang.String)
	 */
	@Override
	public List<Bindings> getNewResults(String queryID) {
		log.info("Getting query results for "+queryID);
		ResultFusion rf = runs.get(queryID);
		if(rf == null)  return null;
		List<Bindings> b = rf.getNewResults();
		log.info("Getting query results for "+queryID+" "+b);
		return b;
	}

	/* (non-Javadoc)
	 * @see ie.deri.urq.lilidaq.ui.client.LiLiDaqService#getTemplateQueries()
	 */
	@Override
	public HashMap<String, String> getTemplateQueries() {
		// TODO Auto-generated method stub
		return tmplQueries;
	}
}
