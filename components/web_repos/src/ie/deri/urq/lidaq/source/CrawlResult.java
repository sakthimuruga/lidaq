/**
 *
 */
package ie.deri.urq.lidaq.source;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.HttpHostConnectException;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 18, 2011
 */
public class CrawlResult {

	public static final int NO_HTTPENTITY = 601;
	public static final int SOCKETTIMEOUTEXCEPTION = 602;
	public static final int UNKNOWNHOSTEXCEPTION = 603;
	public static final int HTTPHOSTCCONNECTEXCEPTION = 604;
	public static final int CANCELATIONEXCEPTION = 605;
	//1 minute 
	public static final long LAST_RECEIVED_TIME_FRAME = 60000;
	private long _cByte;
	private int _status;
	private final URI _uri;
	private Throwable _exp;
	private Map<String, Long> _time = new HashMap<String, Long>();

	/**
	 * @param lu
	 */
	public CrawlResult(final URI lu) {
		_uri = lu;
	}

	/**
	 * @param skipSuffix
	 */
	public void setStatus(final int status) {
		_status = status;
		
	}

	/**
	 * @param bytes
	 */
	public void setContentBytes(final long bytes) {
		_cByte = bytes;
		
	}

	/**
	 * @param string
	 * @param l
	 */
	public void setTime(final String key,final  long time) {
		_time .put(key, time);
		
	}

	/**
	 * @param e
	 */
	public void setException(final Throwable e) {
		_exp = e;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("[GET] ");
		sb.append(_uri).append(" respCode ").append(_status).append(" before ").append(_time.get("before")).append(" lookup-check")
		.append(_time.get("lookup-check")).append(" lookup ").append(_time.get("lookup"));
		if(_exp != null) sb.append(" Exception "+_exp.getClass().getSimpleName()).append(" ").append(_exp.getMessage());
		
		return sb.toString();
	}

	/**
	 * @return
	 */
	public Integer getStatus() {
		return _status;
	}

	
	
	
	public static int getExceptionCode(Exception e) {
		if( e instanceof SocketTimeoutException)
			return SOCKETTIMEOUTEXCEPTION;
		else if( e instanceof UnknownHostException)
			return UNKNOWNHOSTEXCEPTION;
		else if( e instanceof HttpHostConnectException)
			return HTTPHOSTCCONNECTEXCEPTION;
		
		return 606;
	}
}
