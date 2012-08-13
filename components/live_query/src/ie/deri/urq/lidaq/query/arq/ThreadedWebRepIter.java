/**
 *
 */
package ie.deri.urq.lidaq.query.arq;

import ie.deri.urq.lidaq.query.LTBQEQueryConfig;
import ie.deri.urq.lidaq.repos.KeyObserver;
import ie.deri.urq.lidaq.repos.WebRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.openjena.atlas.io.IndentedWriter;
import org.semanticweb.yars.nx.Node;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 11, 2011
 */
public class ThreadedWebRepIter extends QueryIteratorBase {

	private static final Logger logger = Logger.getLogger(ThreadedWebRepIter.class
			.getName());
	
	private Triple _t;
	private WebRepository _webRep;
	private LTBQEQueryConfig _qc;
	private QueryIterator _qI;
	public final int _id;
	private BindingBlockingQueue _q;
	private BindingBlockingQueueIterator _bqi;
	private ResultConsumer _rc;
	private Node[] _key;
	private DerefKeyObserver1 _obs;
	private ArrayList<Var> _vars;
	private ArrayList<Integer> _varsPos;
	private final static AtomicInteger counter = new AtomicInteger(1);

	protected Set<Binding> l_rows = new HashSet<Binding>() ;
	protected Set<Binding> r_rows = new HashSet<Binding>() ;
	private boolean poisonInjected= false;
	private boolean [] _expectResults = new boolean[2];

	private boolean interrupt = false;
	
	/**
	 * @param qI 
	 * @param t
	 * @param _curContext 
	 * @param _webRep
	 * @param _qc
	 */
	public ThreadedWebRepIter(QueryIterator qI, Triple t, WebRepository webRep, LTBQEQueryConfig qc, com.hp.hpl.jena.graph.Node curContext) {
		_qI = qI;
		_id = counter.getAndIncrement();
		_t = t;
		_webRep = webRep;
		_qc = qc;
		_expectResults[0] = true;
		_expectResults[1] = true;
			
		_vars = new ArrayList<Var>();
		_varsPos = new ArrayList<Integer>();

		if(_t.getSubject().isVariable()){
			_vars.add(Var.alloc(_t.getSubject()));
			_varsPos.add(0);	
		}
		if(_t.getPredicate().isVariable()){
			_vars.add(Var.alloc(_t.getPredicate()));
			_varsPos.add(1);
		}
		if(_t.getObject().isVariable()){
			_vars.add(Var.alloc(_t.getObject()));
			_varsPos.add(2);
		}
		if(curContext!=null && curContext.isVariable()){
			_vars.add(Var.alloc(curContext));
			_varsPos.add(3);
		}

		_q = new BindingBlockingQueue();
		_bqi = new BindingBlockingQueueIterator(_q,webRep);

		_obs = new DerefKeyObserver1(this, t,curContext);
		if(_qc.getQueryExecutionBenchmark()!=null)
			_qc.getQueryExecutionBenchmark().initOperator("tp-"+_id, t.toString());
		
		_rc = new ResultConsumer(this, qI, _obs, _webRep);
		_rc.start();
	}
	
	
	public void injectPoisonToken() {
		if(interrupt ) return;
		if(poisonInjected) return;
		
		boolean inject = (!_expectResults[0] && _webRep.idle(true));
		
		StringBuilder sb = new StringBuilder("[tp-");
		sb.append(_id).append("] [POISON] inject:");
		sb.append(inject).append(" /injected:").append(poisonInjected);
		sb.append(" (expl. results: ").append(Arrays.toString(_expectResults));
		sb.append(", webRep_idle:").append(_webRep.idle());
		if(!_webRep.idle()){
			sb.append("[abox:"+ _webRep.aboxTasks()).append(" tbox:").append(_webRep.tboxTasks()).append(" reasoning:").append(_webRep.reasoningCache()).append("]");
		}
		sb.append(")");
		logger.info(sb.toString());
		if(inject){
			if(!poisonInjected){
				_q.add(BindingBlockingQueue.POISON_TOKEN);
				logger.info("[tp-"+_id+"] [INJECT] [POISSON]");
				poisonInjected = true;	
			}
		}
	}


	/**
	 * @param derefKeyObserver
	 */
	public void notifyReceived(KeyObserver dko) {
		injectPoisonToken();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.util.PrintSerializable#output(org.openjena.atlas.io.IndentedWriter, com.hp.hpl.jena.sparql.serializer.SerializationContext)
	 */
	@Override
	public void output(IndentedWriter out, SerializationContext sCxt) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase#hasNextBinding()
	 */
	@Override
	protected boolean hasNextBinding() {
		boolean next = _bqi.hasNext();
		if(!next){
			logger.warning("[tp-"+_id+"] [DONE]");
			if(_qc.getQueryExecutionBenchmark()!=null){
				_qc.getQueryExecutionBenchmark().addQueryIteratorVarBind("tp-"+_id,l_rows.size());
				_qc.getQueryExecutionBenchmark().addQueryIteratorSolBind("tp-"+_id,r_rows.size());
			}
		}
		return next;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase#moveToNextBinding()
	 */
	@Override
	protected Binding moveToNextBinding() {
		Binding next= _bqi.next(); 
		logger.fine("[tp-"+_id+"] [NEXT] => "+next);

		return next;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase#closeIterator()
	 */
	@Override
	protected void closeIterator() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase#requestCancel()
	 */
	@Override
	protected void requestCancel() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param b
	 */
	public void addLeftResult(Binding b) {
		if(b == null || b.isEmpty()){
			//there is no underlying iterator
			_expectResults[0] = false;
			logger.info("[tp-"+_id+"] [RECIEVED] inject poisson [left] expecting right results:"+ !_expectResults[1]);
			if(!_expectResults[1]){
				injectPoisonToken();
			}
		}else if(b.contains(DerefKeyObserver1.INTERRUPTVAR)){
			_expectResults[0] = false;
			interrupt();
		}else{
			synchronized (l_rows) {
				l_rows.add(b);
			}
			//ok, we have a binding and a triple, so we can figure out how it fits
			//			ArrayList<com.hp.hpl.jena.graph.Node> key = new ArrayList<com.hp.hpl.jena.graph.Node>();
			//			for(Var v: _vars){
			//				if(b.contains(v)){
			//					key.add(b.get(v));
			//				}
			//			}
			synchronized (r_rows) {
				for(Binding rb: r_rows){
					Binding res = Algebra.merge(b, rb);
					if(res != null)
						_q.add(res);
				}
				logger.info("[tp-"+_id+"] [left] queue cache size "+_q.cacheSize());
			}
		}
	}
	public void addRightResult(Binding b) {
		//		if root , add result to queue
		if(b==null){
			_expectResults[1] = false;
			logger.info("[tp-"+_id+"] [RECIEVED] inject poisson [right] expecting left results:"+!_expectResults[0]);
			if(!_expectResults[0]){
				injectPoisonToken();
			}
		}else if(b.contains(DerefKeyObserver1.INTERRUPTVAR)){
			_expectResults[1] = false;
			interrupt();
		}else if(!_expectResults[0]&& l_rows.size()==0){
			_q.add(b);
			r_rows.add(b);
		}else{
			synchronized (r_rows) {
				r_rows.add(b);
			}
			synchronized (l_rows) {
				for(Binding lb: l_rows){
					Binding res = Algebra.merge(lb, b);
					if(res != null)
						_q.add(res);
				}
				logger.info("[tp-"+_id+"] [right] queue cache size "+_q.cacheSize());
			}
		}
	}


	public void interrupt() {
		if(!interrupt){
			_q.add(BindingBlockingQueue.POISON_TOKEN);
			logger.info("[tp-"+_id+"] [INTERRUPT] [INJECT] [POISSON]");
			poisonInjected = true;
			interrupt= true;
		}
		
	}
}
