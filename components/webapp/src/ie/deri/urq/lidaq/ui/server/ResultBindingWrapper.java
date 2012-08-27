/**
 *
 */
package ie.deri.urq.lidaq.ui.server;

import java.util.Iterator;

import com.hp.hpl.jena.query.QuerySolution;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jul 2, 2011
 */
public class ResultBindingWrapper implements Comparable<ResultBindingWrapper> {



	QuerySolution _qs;

	/**
	 * @param next
	 */
	public ResultBindingWrapper(QuerySolution qs) {
		_qs = qs;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ResultBindingWrapper o2) {
		Iterator<String> iter = _qs.varNames(); 
		int res = 0;
		while(iter.hasNext()){
			String var = iter.next();
			if(_qs.contains(var)&& o2._qs.contains(var)){
				int c = _qs.get(var).toString().compareTo(o2._qs.get(var).toString());
				if(c != 0) {res= c; break;}
			}else
				res =-1;
		}

//		System.out.println(_qs+" vs "+o2._qs+" = "+res);
		return res;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return _qs.toString();
	}
}
