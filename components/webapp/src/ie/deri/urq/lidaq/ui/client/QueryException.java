package ie.deri.urq.lidaq.ui.client;

import java.io.Serializable;

public class QueryException extends Exception implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String msg;

	public QueryException() {
		// TODO Auto-generated constructor stub
	}
	
	public QueryException(String e) {
		msg = e;
	}
	
	@Override
	public String toString() {
	
		return msg;
	}

}
