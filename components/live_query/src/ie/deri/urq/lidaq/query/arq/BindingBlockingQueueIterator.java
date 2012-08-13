/**
 *
 */
package ie.deri.urq.lidaq.query.arq;

import ie.deri.urq.lidaq.repos.WebRepository;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingBase;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 11, 2011
 */
public class BindingBlockingQueueIterator implements Iterator<Binding>{
	private final static Logger logger = Logger.getLogger(BindingBlockingQueueIterator.class.getName());
	private BindingBlockingQueue _q;
	private Binding _current;
	private boolean _done = false;
	private WebRepository _webRep;
	public static final int REMOTE_THREAD_TIMEOUT_QP = 120*1000; 
	
	
	
	public BindingBlockingQueueIterator(BindingBlockingQueue q, WebRepository webRep) {
		_q =q;
		_webRep=webRep;
	}

	
	public boolean hasNext() {
		if(_current != null)
			return true;
		else if(_done){
			logger.warning(" [DONE] we returned already false");
			return false;
		}
		else{
			//ok, poll a URI as long as 
			do{
				try {
					_current = _q.poll(REMOTE_THREAD_TIMEOUT_QP, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "[DONE] Queue iterator was interruppted ", e);
					return false;
				}
			}while(_current == null && (_webRep == null || !_webRep.idle(false)));
			
			if(_current==null){
				logger.warning(" [DONE] Current result is null -> timeout ");
				return false;
			}
			else if(BindingBase.equals(_current, BindingBlockingQueue.POISON_TOKEN)){
				_current = null;
				if(_q.size()!=0){
					logger.warning("Poison token found before end of stream... recovering...");
					return hasNext();
				}
				_done = true;
				logger.info("[POISON] received ");
//				System.out.println("We found poison token");
				return false;
			}
			else return true;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Binding next() {
		Binding result = _current;
		_current = null;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

}
