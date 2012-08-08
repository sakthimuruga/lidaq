/**
 *
 */
package ie.deri.urq.lidaq.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 29, 2011
 */
public class SingleLineConsoleHandler extends ConsoleHandler {

	static SimpleDateFormat d = new SimpleDateFormat("kk:mm:ss:SSS");
	
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
}
