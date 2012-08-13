package ie.deri.urq.lidaq.cli.bench;

import ie.deri.urq.lidaq.query.ResultSetWrapper;

import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class ResultSetBenchmarkWrapper implements ResultSet {

	private ResultSet _set;
	private long _start;

	boolean recievedFirst = false;
	int results = 0;
	private long _firstResult;
	private long _lastResult;
	
	public ResultSetBenchmarkWrapper(ResultSet set, long start) {
		_set = set;
		_start = start;
		if(set instanceof ResultSetWrapper){
			_start = ((ResultSetWrapper)set).getStartTime();
		}
		_firstResult= 0L;
		_lastResult =0L;
	}
	
	public long getTimeFirstResult() {
		return _firstResult;
	}
	public long getTimeLastResult() {
		return _lastResult;
	}
	

	@Override
	public void remove() {
		if(_set!=null)
			_set.remove();
	}

	@Override
	public boolean hasNext() { 
		if(_set==null) return false;
		boolean next = _set.hasNext();
		
		if(next && !recievedFirst){
			_firstResult = System.currentTimeMillis()-_start;
			recievedFirst = true;
		}
		else if(!next){
			_lastResult = System.currentTimeMillis()-_start;
		}if(next)results++;
		return next;
	}

	@Override
	public QuerySolution next() {
		return _set.next();
	}

	@Override
	public QuerySolution nextSolution() {
		return _set.nextSolution();
	}

	@Override
	public Binding nextBinding() {
		return _set.nextBinding();
	}

	@Override
	public int getRowNumber() {
		return results;
	}

	@Override
	public List<String> getResultVars() {
		
		return _set.getResultVars();
	}

	@Override
	public Model getResourceModel() {
		return _set.getResourceModel();
	}
	
}
