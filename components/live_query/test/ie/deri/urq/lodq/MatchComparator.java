/**
 *
 */
package ie.deri.urq.lodq;

import java.io.Serializable;
import java.util.Comparator;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Variable;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jan 26, 2011
 */
public class MatchComparator implements Comparator<Node[]>, Serializable{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Node[] n1, Node[] n2) {
		System.out.println("Compare "+Nodes.toN3(n1)+" "+Nodes.toN3(n2));
		if(n1==n2){
			return 0;
		}
		if(n1.length!=n2.length) return n1.length-n2.length;
		
		
		int diff = 0;
		for (int i = 0; i < n1.length; i++) {
			if(n1[i] instanceof Variable || n2[i] instanceof Variable) continue;
			else{
				diff = n1[i].compareTo(n2[i]);
				if(diff!=0) return diff;
			}
			
		}
		return diff;
		
	}

}
