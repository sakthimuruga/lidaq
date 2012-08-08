/**
 *
 */
package ie.deri.urq.lidaq.repos;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Mar 25, 2011
 */
public abstract class StringKeyObserver extends KeyObserver<String>{

	/* (non-Javadoc)
	 * @see ie.deri.urq.lidaq.repos.KeyObserver#convert(org.semanticweb.yars.nx.Node[])
	 */
	 public String[] convertTo(Node[] stmt){
		 String [] update = null;
		 if(stmt != null){
			 update = new String[stmt.length];
			 for(int pos = 0; pos < stmt.length; pos++){
				update[pos] = stmt[pos].toN3();
			 }
		 }
		 return update;
	 }

	

	/* (non-Javadoc)
	 * @see ie.deri.urq.lidaq.repos.KeyObserver#convert(T[])
	 */
	@Override
	protected Node[] convertFrom(String[] key) {
		Node [] res = new Node[4];
		for(int pos =0; pos < key.length; pos++){
			try {
				res[pos]= NxParser.parseNode(key[pos]);
			} catch (ParseException e) {
				e.printStackTrace();
			}
//			if(key[pos].startsWith("\""))
//				try {
//					res[pos]= NxParser.parseLiteral(key[pos]);
//				} catch (ParseException e) {
//					res[pos]= new Literal(key[pos]);
//				}
//			else if(key[pos].startsWith("_:"))
//				try {
//					res[pos]= NxParser.parseBNode(key[pos]);
//				} catch (ParseException e) {
//					res[pos]= new BNode(key[pos]);
//				}
//			else if(key[pos].startsWith("?"))
//				try {
//					res[pos]= NxParser.parseVariable(key[pos]);
//				} catch (ParseException e) {
//					res[pos]= new Variable(key[pos]);
//				}
//			else
//				try {
//					res[pos]= NxParser.parseResource(key[pos]);
//				} catch (ParseException e) {
//					res[pos]= new Resource(key[pos]);
//				}
		}
		if(key.length==3)
			res[3] = new Variable("c");
		return res;
	}
	 
	 
	 

}
