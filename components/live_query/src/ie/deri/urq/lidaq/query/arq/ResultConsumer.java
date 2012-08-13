/**
 *
 */
package ie.deri.urq.lidaq.query.arq;

import ie.deri.urq.lidaq.repos.WebRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Variable;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 11, 2011
 */
public class ResultConsumer extends Thread{

	private final static Logger logger = Logger.getLogger(ResultConsumer.class.getName());
	private ThreadedWebRepIter _qI;
	private WebRepository _webRep;
	private DerefKeyObserver1 _obs;
	private QueryIterator _prod;
	private int _regKeys;

	/**
	 * 
	 */
	public ResultConsumer(ThreadedWebRepIter qI, QueryIterator prod, DerefKeyObserver1 obs, WebRepository webRep) {
		_qI= qI;

		_obs = obs;
		_webRep = webRep;
		_prod = prod;
		_regKeys =0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try{
			while(_prod.hasNext()){
				Binding b = _prod.next();
				
				_qI.addLeftResult(b);
				List<Integer> varPos = new ArrayList<Integer>();
				List<org.semanticweb.yars.nx.Node> varValue = new ArrayList<org.semanticweb.yars.nx.Node>();
				org.semanticweb.yars.nx.Node  [] key;
				
				if(Var.isVar(_obs.getTriple().getSubject())){
					varPos.add(0);
					Node s = substitute(_obs.getTriple().getSubject()  , b) ;
					varValue.add(NodeUtils.convertToNX(s));
				}
				if(Var.isVar(_obs.getTriple().getPredicate())){
					Node p = substitute(_obs.getTriple().getPredicate(), b) ;
					varValue.add(NodeUtils.convertToNX(p));
					varPos.add(1);
				}
				if(Var.isVar(_obs.getTriple().getObject())){
					Node o = substitute(_obs.getTriple().getObject()   , b) ;
					varValue.add(NodeUtils.convertToNX(o));
					varPos.add(2);
				}
				if(_obs.getContext()!=null && Var.isVar(_obs.getContext())){
					Node c = substitute(_obs.getContext()   , b) ;
					varValue.add(NodeUtils.convertToNX(c));
					varPos.add(3);
					key = new org.semanticweb.yars.nx.Node[4];
					key[3] = NodeUtils.convertToNX(_obs.getContext());
				}
				else{
					key = new org.semanticweb.yars.nx.Node[3];
				}
				key[0] = NodeUtils.convertToNX(_obs.getTriple().getSubject());
				key[1] = NodeUtils.convertToNX(_obs.getTriple().getPredicate());
				key[2] = NodeUtils.convertToNX(_obs.getTriple().getObject());
				
				int[] a = new int[varPos.size()];
				org.semanticweb.yars.nx.Node[] n = new org.semanticweb.yars.nx.Node[varValue.size()];
				for(int i=0;i<a.length;i++){
					a[i]=varPos.get(i);
					n[i]=varValue.get(i);
				}
				_webRep.registerKey(key, n, a, null, _obs);
				_regKeys++;
				
			}
			_qI.addLeftResult(null);
			if(_regKeys==0)
				_qI.addRightResult(null);
			logger.info("[rc-"+_qI._id+" [DONE] consumed all results and produced "+_regKeys+" keys");
		}catch(Exception e){
			e.printStackTrace();
			logger.severe("[rc-"+_qI._id+" [SEVERE] "+e.getClass().getSimpleName()+" msg:"+e.getMessage());
			_qI.addLeftResult(null);
			if(_regKeys==0)
				_qI.addRightResult(null);
		}
	}

	private Node substitute(Node node, Binding binding)
	{
		if ( Var.isVar(node) )
		{
			Node x = binding.get(Var.alloc(node)) ;
			if ( x != null )
				return x ;
		}
		return node ;
	}
}
