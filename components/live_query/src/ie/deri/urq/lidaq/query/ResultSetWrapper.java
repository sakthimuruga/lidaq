/**
 *
 */
package ie.deri.urq.lidaq.query;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidaq.query.LTBQEQueryConfig;
import ie.deri.urq.lidaq.repos.WebRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 13, 2011
 */
public class ResultSetWrapper extends ResultSetStream{

	private static final Logger logger = Logger
	.getLogger(ResultSetWrapper.class.getName());

	private final LTBQEQueryConfig _qc;
	private long _start;

	private final AtomicInteger _resNo;
	private WebRepository _wr;

	
	
	boolean recievedFirst = false;
	private long _firstResult=0l;
	public long getFirstResult() {
		return _firstResult;
	}



	/**
	 * @param createQueryIterator
	 * @param resultVars
	 * @param q
	 * @param qc
	 * @param start
	 * @param webrep 
	 */
	public ResultSetWrapper(QueryIterator createQueryIterator,
			List<String> resultVars,  LTBQEQueryConfig qc, Long start, WebRepository webrep) {
		super(resultVars, null, createQueryIterator);

		_qc = qc;
		
		if(start == null)
			_start = System.currentTimeMillis();
		else{
			_start = start;
		}
		_wr = webrep;
		_resNo= new AtomicInteger(0);
	}
	
	

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.ResultSetStream#hasNext()
	 */
	@Override
	public boolean hasNext() {
		boolean next = super.hasNext();
		
		if(next && !recievedFirst){
			_firstResult = System.currentTimeMillis()-_start;
			recievedFirst = true;
		}
		else if(!next && !recievedFirst){
			logger.info("[END OF RESULTS] size "+_resNo.get());
			QueryExecutionBenchmark b = _qc.getQueryExecutionBenchmark();
			if(b!=null){
				b.putAll(_wr.getBenchmark());
				b.getKeyOrder().putAll(_wr.getBenchmark().getKeyOrder());
				_qc.getQueryExecutionBenchmark().setFirstResult((_firstResult-_start));
				b.setResultSize(size());
				b.setTotatlTime(System.currentTimeMillis()-_start);
			}
		}
		if(next)_resNo.incrementAndGet();
		
		return next;
	}
	public LTBQEQueryConfig getQueryConfig(){
		return _qc;
	}
	/**
	 * @return
	 */
	public int size() {
		return _resNo.get();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.ResultSetStream#nextBinding()
	 */
	@Override
	public Binding nextBinding() {
		Binding b = super.nextBinding();
		logger.info("[RETURN] "+b);
		return b;
	}
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.ResultSetStream#nextSolution()
	 */
	@Override
	public QuerySolution nextSolution() {
		return super.nextSolution();
	}
	
	public QueryExecutionBenchmark getLiveBenchmark(){
		QueryExecutionBenchmark b = _qc.getQueryExecutionBenchmark();
		if(b!=null){
			b.putAll(_wr.getBenchmark());
			b.getKeyOrder().putAll(_wr.getBenchmark().getKeyOrder());
		}
		return b;
	}



	public long getStartTime() {
		return _start;
	}
}
