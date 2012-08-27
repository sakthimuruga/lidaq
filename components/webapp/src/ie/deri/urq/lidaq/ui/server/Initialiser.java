package ie.deri.urq.lidaq.ui.server;

import ie.deri.urq.lidaq.LinkedDataQueryEngine;
import ie.deri.urq.lidaq.repos.WebRepositoryManager;

import java.io.File;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Initialiser implements ServletContextListener{
	public static final String INDICES = "i";
	public static final String DATA_DIR = "DATA_DIR";
	public static final String PROXY_HOST = "http.proxyHost";
	public static final String PROXY_PORT = "http.proxyPort";
	public static final String PROXY_ENABLE = "useProxy";
	public static final String LIDQ = "LIDQ";
	public static final String WEBREP = "WEBREP";
	public static final String LOGGER = "logger";
	
	private static final Logger logger = Logger.getLogger(Initialiser.class
			.getName());
	

	public void contextDestroyed(ServletContextEvent arg0) {
		WebRepositoryManager wrm = (WebRepositoryManager) arg0.getServletContext().getAttribute(WEBREP);
		wrm.shutdown();
	}

	public void contextInitialized(ServletContextEvent arg0) {
		String proxyPort = null;
		String proxyHost = null;
		logger.info("checking if we should use a proxy");
		if(arg0.getServletContext().getInitParameter(PROXY_ENABLE)!=null &&arg0.getServletContext().getInitParameter(PROXY_ENABLE).equalsIgnoreCase("TRUE")){
			proxyPort = arg0.getServletContext().getInitParameter(PROXY_PORT);
			proxyHost = arg0.getServletContext().getInitParameter(PROXY_HOST);
		}try{
			WebRepositoryManager wrm = new WebRepositoryManager(proxyHost, proxyPort);
			logger.info("Initialised WebRepositoryManager");
			LinkedDataQueryEngine lidaq = new LinkedDataQueryEngine(wrm);
			logger.info("Initialised LinkedDataQueryEngine");
			arg0.getServletContext().setAttribute(LIDQ, lidaq);
			arg0.getServletContext().setAttribute(WEBREP, wrm);
			logger.info("Found proxy settings with host "+proxyHost+" and port "+proxyPort);
			File indicesDir = new File(arg0.getServletContext().getInitParameter("DATA_DIR"));
			QueryLogger logger = new QueryLogger(indicesDir);
			
			logger.rollOver();
			arg0.getServletContext().setAttribute(LOGGER, logger);
		}catch(Exception e){
			logger.log(java.util.logging.Level.SEVERE, "during startup", e.getCause());
		}
	}
}
