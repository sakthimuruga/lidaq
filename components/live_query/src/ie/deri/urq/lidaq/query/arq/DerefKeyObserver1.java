/**
 *
 */
package ie.deri.urq.lidaq.query.arq;

import ie.deri.urq.lidaq.LTBQEQueryEngine;
import ie.deri.urq.lidaq.repos.KeyObserver;
import ie.deri.urq.lidaq.repos.WebRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;

import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 11, 2011
 */
public class DerefKeyObserver1 extends KeyObserver<Node>{

	
	
	public static final Binding INTERRUPTBINDING = new BindingMap();
	public static final Var INTERRUPTVAR = Var.alloc("interrupt");
	static{
		INTERRUPTBINDING.add(INTERRUPTVAR, Node_URI.createLiteral("interrupt"));
	}
	private static final Logger logger = Logger.getLogger(DerefKeyObserver1.class
			.getName());
	private ThreadedWebRepIter _qI;
	private ArrayList<Var> _vars;
	private ArrayList<Integer> _varsPos;
	private Triple _t;

	private final HashSet<Binding> _addedResSet = new HashSet<Binding>();
	private final  String id;
	private com.hp.hpl.jena.graph.Node _curContext;
	
	
	/**
	 * @param threadedWebRepIter
	 * @param t
	 * @param _q
	 */
	public DerefKeyObserver1(ThreadedWebRepIter qI, Triple t) {
		_qI =qI;
		_t = t;
		id = "[ko-"+_qI._id+"]";
		_vars = new ArrayList<Var>();
		_varsPos = new ArrayList<Integer>();

		if(t!=null){
			if(_t.getSubject().isVariable()){
				_vars.add(Var.alloc(_t.getSubject().getName()));
				_varsPos.add(0);	
			}
			if(_t.getPredicate().isVariable()){
				_vars.add(Var.alloc(_t.getPredicate().getName()));
				_varsPos.add(1);
			}
			if(_t.getObject().isVariable()){
				_vars.add(Var.alloc(_t.getObject().getName()));
				_varsPos.add(2);
			}
		}
	}

	public DerefKeyObserver1(ThreadedWebRepIter qI, Triple t,
			com.hp.hpl.jena.graph.Node curContext) {
		_qI =qI;
		_t = t;
		_curContext = curContext;
		id = "[ko-"+_qI._id+"]";
		_vars = new ArrayList<Var>();
		_varsPos = new ArrayList<Integer>();

		if(t!=null){
			if(_t.getSubject().isVariable()){
				_vars.add(Var.alloc(_t.getSubject().getName()));
				_varsPos.add(0);	
			}
			if(_t.getPredicate().isVariable()){
				_vars.add(Var.alloc(_t.getPredicate().getName()));
				_varsPos.add(1);
			}
			if(_t.getObject().isVariable()){
				_vars.add(Var.alloc(_t.getObject().getName()));
				_varsPos.add(2);
			}
			if(_curContext!=null && _curContext.isVariable()){
				_vars.add(Var.alloc(_curContext.getName()));
				_varsPos.add(3);
			}
		}
		
	}

	public Triple getTriple(){
		
		return _t;
	}
	
	

	/* (non-Javadoc)
	 * @see ie.deri.urq.lidaq.repos.KeyObserver#receiveStatement(T[])
	 */
	@Override
	protected void receiveStatement(Node[] statement) {
		if(statement == null){
			_qI.addRightResult(null);
			_qI.notifyReceived(this);
			return;
		}
//		System.out.println("Received "+Nodes.toN3(statement));
		if(statement.length==1 && statement[0].equals(WebRepository.INTERRUPT)){
			_addedResSet.clear();
			_qI.addRightResult(INTERRUPTBINDING);
			_qI.addLeftResult(INTERRUPTBINDING);
			_qI.interrupt();
			logger.info(id+" [INTERRUPT]");
			
			return;
		}
		
		//ok, we create the binding 
		try{
			Binding b = new BindingMap();
			boolean bindNonVar= true;
			for(int i=0; i < _vars.size(); i++){
				com.hp.hpl.jena.graph.Node n = NodeUtils.convertToARQ(statement[_varsPos.get(i)]);
				if(Var.isAllocVar(n) ||  n instanceof Node_Variable){
					bindNonVar = false;
				}
				b.add(_vars.get(i), n);
			}
			if(bindNonVar && ! _addedResSet.contains(b)){
				_qI.addRightResult(b);
				_addedResSet.add(b);
				logger.info(id+" [NOTIFY] new result "+b);	
			}
		}catch(RuntimeException e){
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see ie.deri.urq.lidaq.repos.KeyObserver#convertTo(org.semanticweb.yars.nx.Node[])
	 */
	@Override
	protected Node[] convertTo(Node[] n) {
		return n;
	}

	/* (non-Javadoc)
	 * @see ie.deri.urq.lidaq.repos.KeyObserver#convertFrom(T[])
	 */
	@Override
	protected Node[] convertFrom(Node[] t) {
		return t;
	}

	@Override
	public int compareTo(KeyObserver<Node> o) {
		if(!(o instanceof DerefKeyObserver1)) return -1;
		DerefKeyObserver1 ot = (DerefKeyObserver1) o;
		int diff = -1;
		if(_t != null && ot.getTriple()!=null){
			diff = _t.getSubject().toString().compareTo(ot.getTriple().getSubject().toString());
			if (diff == 0)
				diff =_t.getSubject().toString().compareTo(ot.getTriple().getSubject().toString());
			if (diff == 0)
				diff=_t.getSubject().toString().compareTo(ot.getTriple().getSubject().toString());
		}
		if(_curContext != null && ot._curContext != null)
			if(diff == 0){
				diff = _curContext.toString().compareTo(ot._curContext.toString());
			}
		return diff;
	}

	@Override
	public String getID() {
		return id;
	}

	public com.hp.hpl.jena.graph.Node getContext() {
		return _curContext;
	}

}
