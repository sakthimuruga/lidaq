package ie.deri.urq.lidaq.reasoning;

import org.semanticweb.saorr.Statement;
import org.semanticweb.saorr.engine.unique.UniqueStatementFilter;

public class UniqueQuadFilter extends UniqueStatementFilter {
	/**
	 * Underlying statement filter to which newly created triples
	 * are passed.
	 */
	private UniqueStatementFilter usf;
	
	/**
	 * Constructor.
	 * 
	 * @param usf The underlying statement filter to which triples
	 * will be passed.
	 */
	public UniqueQuadFilter(UniqueStatementFilter usf){
		this.usf = usf;
	}
	
	/**
	 * @return The underlying statement filter
	 */
	public UniqueStatementFilter getUnderylingStatementFilter(){
		return this.usf;
	}

	public boolean addSeen(Statement s) {
		return this.usf.addSeen(s);
	}

	public boolean checkSeen(Statement s) {
		s = new Statement(s.toNodeTriple());
		return this.usf.checkSeen(s);
	}

	public void clear() {
		usf.clear();
	}

	public int size() {
		return usf.size();
	}

	public boolean remove(Statement s) {
		return usf.remove(s);
	}
}
