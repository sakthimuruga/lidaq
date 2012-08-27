package ie.deri.urq.lidaq.ui.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class QueryLogger {

private static final Logger logger = Logger.getLogger(QueryLogger.class
		.getName());
	private File _logDir;
	private FileWriter _fw;
	private File _logFile;
	private String _curDate;
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
	
	public QueryLogger(File diskLocation) {
		
		_logDir = new File(diskLocation,"logs");
		
		if(!_logDir.exists()){_logDir.mkdirs();}
		logger.info("[INIT] query logger at directory: "+_logDir+" exists:"+_logDir.exists()+" r:"+_logDir.canRead()+" w:"+_logDir.canWrite());
		
	}

	public void rollOver() {
		_logFile = getNextLogFile();
		if(_fw !=null){
			try {
				_fw.flush();
				_fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			if(_logFile.exists())
				_fw = new FileWriter(_logFile,true);
			else
				_fw = new FileWriter(_logFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getNextLogFile() {
		_curDate = formatter.format(new Date(System.currentTimeMillis()));
		File logFile = new File(_logDir,_curDate+".log");
		int counter =0;
		while(logFile.exists()){
			logFile = new File(_logDir,_curDate+"."+counter+".log");
			counter++;
		}
		return logFile;
	}

	public void log(String string) {
		if(_fw!=null){
			try {
				_fw.write(string+"\n");
				_fw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		checkAndRollOver();
	}

	private long maxFileSize = 10 * 1024* 1024;
	private void checkAndRollOver() {
		if(_logFile.length()>maxFileSize
				|| !_curDate.equals(formatter.format(new Date(System.currentTimeMillis()))))
			rollOver();
		
	}
}