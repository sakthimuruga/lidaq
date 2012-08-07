/**
 *
 */
package ie.deri.urq.lidaq.repos;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.saorr.Statement;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Feb 14, 2011
 */
public class MapTripleStoreTEST {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void testTripleStore(){
		MapTripleStoreWrapper t = new MapTripleStoreWrapper(null);
		
		Node [] match = {new Resource("http://example.org/sub#") ,new Resource("http://example.org/pred"), new Resource("http://example.org/obj#"),new Resource("http://example.org/cntx")};
		Node [] match1 = {new BNode("http://example.com/sub#") ,new Resource("http://example.com/pred"), new Resource("http://example.com/obj#"),new Resource("http://example.com/cntx#")};
		
		Node [] matchIDX= new Node[4];
		Node [] matchIDX1= new Node[4];
		matchIDX1[1]=match1[1];
		matchIDX[1]=match[1];
		int size =0;
		for(int subj =0; subj < 10; subj++){
			matchIDX[0] = new Resource(match[0].toString()+""+subj);
			matchIDX1[0] = new BNode(match1[0].toString()+""+subj);
			
			//entities
			int ctx = 0;
			for(int obj =0; obj < 4; obj++){
				
				if(obj==2) ctx = 1;
				if(obj==3) ctx = 2;
				matchIDX[2] = new Resource(match[2].toString()+""+obj);
				matchIDX1[2] = new Resource(match1[2].toString()+""+obj);
				matchIDX[3] = new Resource(match[3].toString()+""+ctx);
				matchIDX1[3] = new Resource(match1[3].toString()+""+ctx);
				t.setCurrentStatement(new Statement(matchIDX));
				t.indexCurrentStatement();
				t.setCurrentStatement(new Statement(matchIDX1));
				t.indexCurrentStatement();
				size+=2;
			}
			
			
			
		}
		Variable s = new Variable("s");
		Variable p = new Variable("p");
		Variable o = new Variable("o");
		Variable c = new Variable("c");
		Node [] sConst = {new Resource("http://example.org/sub#0") ,p, o,c};
		Node [] pConst = {s ,new Resource("http://example.org/pred"), o,c};
		Node [] oConst = {s ,p, new Resource("http://example.org/obj#1"),c};
		Node [] key2 = {new Resource("http://example.org/sub") ,new Resource("http://example.org/pred") , new Variable("o"),new Resource("http://example.org/cntx2")};
		
		System.out.println(">>>>Matches for key "+Nodes.toN3(sConst));
		System.out.println("__________________________");
		for(Nodes n: t.retrieveStatements(new Statement(sConst))){
			System.out.println("MATCH: "+n);
		}
		System.out.println(">>>>Matches for key "+Nodes.toN3(pConst));
		System.out.println("__________________________");
		for(Nodes n: t.retrieveStatements(new Statement(pConst))){
			System.out.println("MATCH: "+n);
		}
		System.out.println(">>>>Matches for key "+Nodes.toN3(oConst));
		System.out.println("__________________________");
		for(Nodes n: t.retrieveStatements(new Statement(oConst))){
			System.out.println("MATCH: "+n);
		}
//		System.out.println("__________________________");
//		System.out.println(">>>>Matches for key "+Nodes.toN3(key1));
//		System.out.println("__________________________");
//		for(Nodes n: t.retrieveStatements(new Statement(key1))){
//			System.out.println("MATCH: "+n);
//		}
//		System.out.println("__________________________");
//		System.out.println(">>>>Matches for key "+Nodes.toN3(key2));
//		System.out.println("__________________________");
//		for(Nodes n: t.retrieveStatements(new Statement(key2))){
//			System.out.println("MATCH: "+n);
//		}
//		System.out.println("__________________________");
		System.out.println("Inserted: "+size+" stmts: "+t.size());
		
	}
}
