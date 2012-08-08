/**
 *
 */
package ie.deri.urq.lidaq.log;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Aug 21, 2010
 */
public class LIDAQLOGGER {

	
	
	private static final Logger logger = Logger
			.getLogger(LIDAQLOGGER.class.getName());
	
	static SimpleDateFormat d = new SimpleDateFormat("kk:mm:ss:SSS");
	
	
	/**
	 * LOG LEVEL CONVENTION
	 * SEVERE: everything which causes that the system is not functioning
	 * WARNING: (Potential) errors which will influence the result set (e.g. http exceptions,)
	 * INFO: High level status messages which easily allow us to track the workflow
	 * FINE: we need more details about crucial steps
	 * FINER: 
	 * FINEST: ok, all the crap 
	 */
	
	/** 
	 * 
	 */
	public static final Handler ConsolLogger = new Handler() {
		
		private final  HashMap<String, String> logNames = new HashMap<String, String>();
		
		@Override
		public void publish(LogRecord record) {
			String logName = record.getLoggerName();
			String shortName = logNames.get(logName);
			if(shortName==null){
				shortName = logName.substring(logName.lastIndexOf(".")+1);
				logNames.put(logName, shortName);
			}
			StringBuilder sb = new StringBuilder();
			sb.append(d.format(new Date(record.getMillis()))).append(" ").append(record.getLevel().getLocalizedName()).append(" [").append(shortName).append("] ").append(record.getMessage());
			System.err.println(sb.toString());
		}
		
		@Override
		public void flush() {
		;
			
		}
		
		@Override
		public void close() throws SecurityException {
		;
			
		}
	};
	
	public static Logger addHandler(Logger logger1){
		logger1.addHandler(LIDAQLOGGER.ConsolLogger);
		logger1.setUseParentHandlers(false);
		return logger1;
	}


	public static void setDefaultLogging() {
		 logger.warning("Changing log levels to default logging");
		 loadConfig("defaultLogging.properties");
	 }
	 

	
	

	public static void operatorDebug() {
		logger.warning("Changing log levels to operator debug");
		 loadConfig("operatorLogging.properties");
	}
	/**
	 * 
	 */
	public static void lodqDebug() {
		logger.warning("Changing log levels to operator debug");
		 loadConfig("debugLogging.properties");
		
	}
	
	
	
	private static void loadConfig(String config){
		InputStream is = null;
		try{
//			LogManager.getLogManager().reset();
			is = LIDAQLOGGER.class.getResourceAsStream(config);
			if(is !=null){
			
//			LogManager.getLogManager().readConfiguration(is);
			}
		}catch(Exception e){
			
			e.printStackTrace();
		}
		
	}
}
