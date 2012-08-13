package ie.deri.urq.lidaq.query.arq;
import ie.deri.urq.lidaq.query.LTBQEQueryConfig;
import ie.deri.urq.lidaq.repos.WebRepository;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 9, 2011
 */
public class LIDAQOpExecutor extends OpExecutor {

	private WebRepository _webRep;
	private LTBQEQueryConfig _qc;
	private Node _curContext;

	
	@Override
	public QueryIterator executeOp(Op op, QueryIterator input) {
//		System.out.println(op);
		return super.executeOp(op, input);
	}
	
	
	@Override
	protected QueryIterator execute(OpGraph opGraph, QueryIterator input) {
		_curContext = opGraph.getNode();
		return executeOp(opGraph.getSubOp(), input);
	}
	
	/**
	 * @param execCxt
	 * @param deepLeft 
	 */
	public LIDAQOpExecutor(ExecutionContext execCxt, WebRepository webRep, LTBQEQueryConfig qc) {
		super(execCxt);
		_webRep = webRep;
		_qc = qc;
	}

	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.main.OpExecutor#execute(com.hp.hpl.jena.sparql.algebra.op.OpBGP, com.hp.hpl.jena.sparql.engine.QueryIterator)
	 */
	@Override
	protected QueryIterator execute(OpBGP opBGP, QueryIterator input) {
		BasicPattern p =opBGP.getPattern();
		p = ReorderLib.fixed().reorder(p);
		QueryIterator qI = input;
		for ( Triple t : p ) {
			qI = new ThreadedWebRepIter(qI, t, _webRep, _qc, _curContext);
		}
		return qI;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.main.OpExecutor#execute(com.hp.hpl.jena.sparql.algebra.op.OpJoin, com.hp.hpl.jena.sparql.engine.QueryIterator)
	 */
	@Override
	protected QueryIterator execute(OpJoin opJoin, QueryIterator input) {
//		System.out.println("OpJoin left:"+opJoin.getLeft()+" right:"+opJoin.getRight());
////		QueryIterator l = executeOp(opJoin.getLeft(), input);
////		QueryIterator r = executeOp(opJoin.getRight(), input);
		
		return super.execute(opJoin, input);
	}
	
	protected QueryIterator execute(OpLeftJoin opLeftJoin, QueryIterator input) {
//		System.out.println("OpLeftJoin left:"+opLeftJoin.getLeft()+" right:"+opLeftJoin.getRight());
//		QueryIterator l = executeOp(opLeftJoin.getLeft(), input);
//		QueryIterator r = executeOp(opLeftJoin.getRight(), input);
		
		return super.execute(opLeftJoin, input);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.main.OpExecutor#execute(com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern, com.hp.hpl.jena.sparql.engine.QueryIterator)
	 */
	@Override
	protected QueryIterator execute(OpQuadPattern quadPattern,
			QueryIterator input) {
		return super.execute(quadPattern, input);
	}
}
