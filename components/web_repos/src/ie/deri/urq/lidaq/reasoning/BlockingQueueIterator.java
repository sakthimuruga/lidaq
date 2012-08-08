package ie.deri.urq.lidaq.reasoning;

import ie.deri.urq.lidaq.source.SourceLookup;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


import org.semanticweb.yars.nx.Node;

public class BlockingQueueIterator implements Iterator<Node[]>{
	private BlockingQueue<Node[]> _q;
	private Node[] _current;
	private boolean _done = false;
	private SourceLookup _sl;
	private final static Logger logger = Logger.getLogger(BlockingQueueIterator.class.getName());
	public BlockingQueueIterator(BlockingQueue<Node[]> q, SourceLookup sourceLookup){
		_q = q;
		_sl = sourceLookup;
	}

	public BlockingQueueIterator(NodeArrayBlockingQueue _q2) {
		this(_q2,null);
	}

	public boolean hasNext() {
		if(_current!=null)
			return true;
		else if(_done)
			return false;
		else{
			do{
				try {
					_current = _q.poll(60000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.warning("[WARN] InterruptedException");
					return false;
				}
			}while(_current == null && (_sl == null || !_sl.idle()));
			if(_current == null)
				return false;
			else if(_current.equals(NodeArrayBlockingQueue.POISON_TOKEN)){
				_current = null;
				if(_q.size()!=0){
					logger.warning("Poison token found before end of stream... recovering...");
					return hasNext();
				}
				_done = true;
				return false;
			}
			else return true;
		}
	}

	public Node[] next() {
		Node[] result = new Node[_current.length];
		System.arraycopy(_current, 0, result, 0, _current.length);
		_current = null;
		return result;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public String toString() {
		return "BlockingQueueIterator: queue size "+_q.size();
	}

	public void shutdown() {
		_q.clear();
		_current = null;
		_done=true;
		
		
	}

}
