package ie.deri.urq.lidaq.query.arq;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib;

/**
 *
 */

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 10, 2011
 */
public class TransformBGPToJoin extends TransformCopy {

	
	private OpBGP deepLeft = null;
	
	
	public OpBGP getDeepLeft(){return deepLeft;}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.TransformCopy#transform(com.hp.hpl.jena.sparql.algebra.op.OpBGP)
	 */
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.TransformCopy#transform(com.hp.hpl.jena.sparql.algebra.op.OpJoin, com.hp.hpl.jena.sparql.algebra.Op, com.hp.hpl.jena.sparql.algebra.Op)
	 */
	@Override
	public Op transform(OpJoin opJoin, Op left, Op right) {
		// TODO Auto-generated method stub
		return super.transform(opJoin, left, right);
	}
	@Override
	public Op transform(OpBGP opBGP) {
		BasicPattern p = opBGP.getPattern();
		p = ReorderLib.fixed().reorder(p);
		System.out.println("reordered "+p);
		Op cur = null;
		List<Op> rem = new ArrayList<Op>();
		int c =0;
		for(Triple t:p)
		{
			Op tOp = new OpTriple(t).asBGP();
			if(cur == null){
				cur = tOp;
				deepLeft = (OpBGP) tOp;
			}
			else if(isJoin(tOp,cur))
				cur = OpJoin.create(cur, tOp);
			else{
				rem.add(tOp);
			}
		}
		if(rem.size()!=0) System.err.println("AUTSCH");
		return cur;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.TransformCopy#transform(com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern)
	 */
	@Override
	public Op transform(OpQuadPattern opQuadPattern) {
		System.out.println("QUAD: "+opQuadPattern);
		return super.transform(opQuadPattern);
	}
	/**
	 * @param tOp
	 * @param cur
	 * @return
	 */
	private boolean isJoin(Op tOp, Op cur) {
		Set<Var> vars = OpVars.allVars(cur);
		for(Var v:OpVars.allVars(tOp)){
			if(vars.contains(v)) return true;
		}
		return false;
		
		
	}
}
