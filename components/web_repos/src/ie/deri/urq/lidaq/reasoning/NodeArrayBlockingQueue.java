package ie.deri.urq.lidaq.reasoning;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.util.thread.BlockingQueueIterator;

public class NodeArrayBlockingQueue extends ArrayBlockingQueue<Node[]> {
	public static final Node[] POISON_TOKEN =  { new Literal("EOS") };
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Variable[] _mask = null;

	public NodeArrayBlockingQueue(int block){
		super(block);
	}
	
	
	public Iterator<Node[]> iterator(){
		
		return new BlockingQueueIterator(this);
	}
	
	public synchronized void setMask(Variable[] mask) throws MaskAlreadySetException{
		if(_mask==null)
			_mask = mask;
		else if(NodeComparator.NC.compare(_mask, mask)==0){
			return;
		} else{
			throw new MaskAlreadySetException();
		}
	}
	
	public synchronized Variable[] getMask(){
		return _mask;
	}
	
	public class MaskAlreadySetException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MaskAlreadySetException(){
			super();
		}
	}
}
