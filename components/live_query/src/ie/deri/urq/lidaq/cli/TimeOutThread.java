package ie.deri.urq.lidaq.cli;

import java.util.logging.Logger;

import ie.deri.urq.lidaq.repos.WebRepository;

public class TimeOutThread extends Thread {

	private static final Logger logger = Logger.getLogger(TimeOutThread.class.getName());
	
	private static final Integer DEFAULT_WAIT = 10; //10 seconds
	private Integer _timeout;
	private WebRepository _wr;
	private Integer _wait= DEFAULT_WAIT;
	private boolean _stop = false;
	public TimeOutThread(Integer timeout, WebRepository wr) {
		_wr = wr;
		_timeout = timeout;
		if(timeout < DEFAULT_WAIT){
			_wait = Math.max(1,timeout/2);
		}
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		
		logger.info("[START] with timeout of "+_wait+" seconds");
		
		while(!_stop && ((System.currentTimeMillis()-start)/1000) <_timeout){
			try {
				Thread.sleep(_wait);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(_stop)
			logger.info("[STOPPED] [NORMAL]");
		else{
			_wr.interrupt();
			logger.info("[STOPPED] [TIMEOUT] "+ ((System.currentTimeMillis()-start)/1000)+" < "+_timeout);
		}
			
		
	}
	
	public void stopThread(){
		_stop= true;
	}
}
