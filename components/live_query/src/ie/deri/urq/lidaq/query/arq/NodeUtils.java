/**
 *
 */
package ie.deri.urq.lidaq.query.arq;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.NodeFactory;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 11, 2011
 */
public class NodeUtils {

	/**
	 * @param node
	 * @return
	 */
	public static com.hp.hpl.jena.graph.Node convertToARQ(Node n) {
		 if (n instanceof Resource) {
	           return com.hp.hpl.jena.graph.Node.createURI(((Resource)n).toString());
	       } else if (n instanceof Literal) {
	    	   Literal l= (Literal) n;
	    	   String language = l.getLanguageTag();
	    	   String datatype = null;
	    	   if(l.getDatatype()!=null && l.getDatatype().toString().trim().length()!=0)
	    		   datatype = l.getDatatype().toString();
	    	   return NodeFactory.createLiteralNode(l.toString(), language, datatype);
	       } else if (n instanceof BNode) {
	           return com.hp.hpl.jena.graph.Node.createAnon(new AnonId(((BNode)n).toString()));
	       }
	       else if (n instanceof org.semanticweb.yars.nx.Variable) {
	           return Var.createVariable(n.toString());
	       }
	       return null; 
	}

	/**
	 * @param _t
	 * @return
	 */
	public static Node[] convertToNX(Triple t) {
		Node[] n = new Node[3];
		n[0]= convertToNX(t.getSubject());
		n[1]= convertToNX(t.getPredicate());
		n[2]= convertToNX(t.getObject());
		return n;
	}

	/**
	 * @param subject
	 * @return
	 */
	public static Node convertToNX(com.hp.hpl.jena.graph.Node n) {
//		System.out.println(n.getClass());
//		String val = n.getLocalName();
		
		 if (n instanceof Node_URI) {
	           return new Resource(n.toString());
	       } else if (n instanceof Node_Literal) {
	           return new Literal(n.toString());
	       } else if (n instanceof Node_Blank) {
	           return new BNode(n.toString());
	       }else if(n instanceof Var){
	    	   return new org.semanticweb.yars.nx.Variable(n.getName());
	       }
	       return null; 
	}

}
