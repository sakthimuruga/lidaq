/**
 *
 */
package ie.deri.urq.lodq;

import ie.deri.urq.lidaq.repos.QueryBasedSourceSelection;
import ie.deri.urq.lidaq.repos.QueryBasedSourceSelectionStrategies;
import ie.deri.urq.lidaq.repos.TriplePattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;



/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Apr 4, 2011
 */
public class SourceSelectionTEST extends TestCase{

	
	
	public void testLookup() throws Exception {
		QueryBasedSourceSelection sl = new QueryBasedSourceSelection(QueryBasedSourceSelectionStrategies.SMART);
		
		Variable p = new Variable("p");
		Variable o = new Variable("o");
		/** QUERY 
		 * SELECT ?o
		 * WHERE{
		 * 	<http://example.org/person#me> ?p ?o .
		 * }
		 * **/
		Node [] tp1 = {new Resource("http://example.org/person#me"),p,o };

		Set<Variable> joinVars = new HashSet<Variable>();
		ArrayList<Variable> selVars = new ArrayList<Variable>();
		selVars.add(o);
		sl.setResultVariable(selVars);
		sl.setJoinVariable(joinVars);
		ArrayList<Variable> vars = new ArrayList<Variable>();
		vars.add(p);vars.add(o);
		int [] varpos = {1,2};
	

 		TriplePattern pt = new TriplePattern(tp1, vars, varpos, null, null);
		
 		//submit pattern
 		assertTrue(sl.selectURIs(pt, 0).size()==0);
		
		Node [] tp2 = {new Resource("http://example.org/person#me"),new Resource("http://example.org/pred"),new Resource("http://example.org/obj") };
		assertTrue(sl.selectURIs(tp2,pt).size()==0);
		
		
		/** QUERY 
		 * SELECT ?p
		 * WHERE{
		 * 	<http://example.org/person#me> ?p ?o .
		 * }
		 * **/
		selVars = new ArrayList<Variable>();
		selVars.add(p);
		sl.setResultVariable(selVars);
		
 		pt = new TriplePattern(tp1, vars, varpos, null, null);
		
 		//submit pattern
 		assertTrue((sl.selectURIs(pt, 0).size()==1) && (sl.selectURIs(pt, 0).contains(new Resource("http://example.org/person#me"))));
		
 		//TODO
//		assertTrue((sl.selectURIs(tp2,pt).size()==1) && (sl.selectURIs(tp2,pt).contains(new Resource("http://example.org/obj"))));

 		
 		/** QUERY 
		 * SELECT ?p ?o
		 * WHERE{
		 * 	<http://example.org/person#me> ?p ?o .
		 * }
		 * **/
		selVars = new ArrayList<Variable>();
		selVars.add(p);selVars.add(o);
		sl.setResultVariable(selVars);
		
 		pt = new TriplePattern(tp1, vars, varpos, null, null);
		
 		//submit pattern
 		assertTrue((sl.selectURIs(pt, 0).size()==1) && (sl.selectURIs(pt, 0).contains(new Resource("http://example.org/person#me"))));
		
 		//TODO
		assertTrue((sl.selectURIs(tp2,pt).size()==1) && (sl.selectURIs(tp2,pt).contains(new Resource("http://example.org/obj"))));
		
	}
	
	public void testSimpleJoin() throws Exception {
		QueryBasedSourceSelection sl = new QueryBasedSourceSelection(QueryBasedSourceSelectionStrategies.SMART);
		
		Variable p = new Variable("p");
		Variable o = new Variable("o");
		Variable p2 = new Variable("p2");
		Variable o2 = new Variable("o2");
		
		/** QUERY 
		 * WHERE{
		 * 	<http://example.org/person#me> ?p ?o .
		 * ?o ?p2 ?o2 .
		 * }
		 * **/
		Node [] tp1 = {new Resource("http://example.org/person#me"),p,o };
		Node [] tp2 = {o,p2,o2 };
		int [] tp1_arpos = {1,2};
		int [] tp2_arpos = {0,1,2};
		
		Set<Variable> joinVars = new HashSet<Variable>();
		joinVars.add(o);
		sl.setJoinVariable(joinVars);
		
		ArrayList<Variable> vars1 = new ArrayList<Variable>();
		vars1.add(o);vars1.add(p);
		
		ArrayList<Variable> vars2 = new ArrayList<Variable>();
		vars2.add(o);vars2.add(p2);vars2.add(o2);
		
		TriplePattern pt1 = new TriplePattern(tp1, vars1, tp1_arpos, null, null);
 		
 		tp2[0] = new Resource("http://example.org/obj");
 		TriplePattern pt2 = new TriplePattern(tp2, vars2, tp2_arpos, null, null);
		
 		
 		Node [] tp1In = {new Resource("http://example.org/person#me"),new Resource("http://example.org/pred"),new Resource("http://example.org/obj") };
 		Node [] tp2In = {new Resource("http://example.org/obj"),new Resource("http://example.org/pred2"),new Resource("http://example.org/obj2") };
 		
 		/*************
		 * SELECT ?o  
		 *************/
		ArrayList<Variable> selVars = new ArrayList<Variable>();
		selVars.add(o);
		sl.setResultVariable(selVars);
		
		test(sl,pt1,pt2, tp1In,tp2In,0,1,0,0,0);
 		
		/*************
		 * SELECT ?o ?p 
		 *************/
		selVars = new ArrayList<Variable>();
		selVars.add(o);selVars.add(p);
		sl.setResultVariable(selVars);
		
		
		test(sl,pt1,pt2, tp1In,tp2In,1,1,0,1,0);
		
		/*************
		 * SELECT ?o2 
		 *************/
		selVars = new ArrayList<Variable>();
		selVars.add(o2);
		sl.setResultVariable(selVars);
		
		test(sl,pt1,pt2, tp1In, tp2In, 0,1,1,0,0);
		
		/*************
		 * SELECT ?o2 ?o
		 *************/
		selVars = new ArrayList<Variable>();
		selVars.add(o);selVars.add(o2);
		sl.setResultVariable(selVars);
		
		test(sl,pt1,pt2, tp1In, tp2In, 0,1,1,0,0);
		
		/*************
		 * SELECT ?p2 ?o
		 *************/
		selVars = new ArrayList<Variable>();
		selVars.add(o);selVars.add(p2);
		sl.setResultVariable(selVars);
		
		test(sl,pt1,pt2, tp1In, tp2In, 0,1,1,0,1);
		
		/*************
		 * SELECT ?p2 ?p
		 *************/
		selVars = new ArrayList<Variable>();
		selVars.add(p);selVars.add(p2);
		sl.setResultVariable(selVars);
		
		test(sl,pt1,pt2, tp1In, tp2In, 1,1,1,1,1);
	}

	public void testTwoJoin() throws Exception {
		QueryBasedSourceSelection sl = new QueryBasedSourceSelection(QueryBasedSourceSelectionStrategies.SMART);
		
		Variable p = new Variable("p");
		Variable o = new Variable("o");
		Variable p2 = new Variable("p2");
		Variable o2 = new Variable("o2");
		Variable p3 = new Variable("p3");
		Variable o3 = new Variable("o3");
		
		/** QUERY 
		 * WHERE{
		 * 	<http://example.org/person#me> ?p ?o .
		 * ?o ?p2 ?o2 .
		 * ?o2 ?p3 ?o3 .
		 * }
		 * **/
		Node [] tp1 = {new Resource("http://example.org/person#me"),p,o };
		Node [] tp2 = {o,p2,o2 };
		Node [] tp3 = {o2,p3,o3 };
		int [] tp1_arpos = {1,2};
		int [] tp2_arpos = {0,1,2};
		int [] tp3_arpos = {0,1,2};
		
		Set<Variable> joinVars = new HashSet<Variable>();
		joinVars.add(o);joinVars.add(o2);
		sl.setJoinVariable(joinVars);
		
		ArrayList<Variable> vars1 = new ArrayList<Variable>();
		vars1.add(o);vars1.add(p);
		
		ArrayList<Variable> vars2 = new ArrayList<Variable>();
		vars2.add(o);vars2.add(p2);vars2.add(o2);
		
		ArrayList<Variable> vars3 = new ArrayList<Variable>();
		vars3.add(o2);vars3.add(p3);vars3.add(o3);

		TriplePattern pt1 = new TriplePattern(tp1, vars1, tp1_arpos, null, null);
 		
 		tp2[0] = new Resource("http://example.org/obj");
 		TriplePattern pt2 = new TriplePattern(tp2, vars2, tp2_arpos, null, null);
		
 		tp3[0] = new Resource("http://example.org/obj2");
 		TriplePattern pt3 = new TriplePattern(tp3, vars3, tp3_arpos, null, null);
 		
 		
 		Node [] tp1In = {new Resource("http://example.org/person#me"),new Resource("http://example.org/pred"),new Resource("http://example.org/obj") };
 		Node [] tp2In = {new Resource("http://example.org/obj"),new Resource("http://example.org/pred2"),new Resource("http://example.org/obj2") };
 		Node [] tp3In = {new Resource("http://example.org/obj2"),new Resource("http://example.org/pred3"),new Resource("http://example.org/obj3") };
 		
 		/*************
		 * SELECT ?o  
		 *************/
		ArrayList<Variable> selVars = new ArrayList<Variable>();
		selVars.add(o);
		sl.setResultVariable(selVars);
		
			
		int tp1NC= 0;
		int tp2NC= 1; 
		int tp2C = 1;
		int tp3NC = 1; 
		int tp3C= 0; 
		int tp1InS= 0;
		int tp2InS= 0;
		int tp3InS= 0;
		test(sl,pt1,pt2,pt3, tp1In,tp2In,tp3In,tp1NC,tp2NC,tp2C,tp3NC,tp3C,tp1InS,tp2InS,tp3InS);
 		
//		/*************
//		 * SELECT ?o ?p 
//		 *************/
//		selVars = new ArrayList<Variable>();
//		selVars.add(o);selVars.add(p);
//		sl.setResultVariable(selVars);
//		
//		
//		test(sl,pt1,pt2, tp1In,tp2In,1,1,0,1,0);
//		
//		/*************
//		 * SELECT ?o2 
//		 *************/
//		selVars = new ArrayList<Variable>();
//		selVars.add(o2);
//		sl.setResultVariable(selVars);
//		
//		test(sl,pt1,pt2, tp1In, tp2In, 0,1,1,0,0);
//		
//		/*************
//		 * SELECT ?o2 ?o
//		 *************/
//		selVars = new ArrayList<Variable>();
//		selVars.add(o);selVars.add(o2);
//		sl.setResultVariable(selVars);
//		
//		test(sl,pt1,pt2, tp1In, tp2In, 0,1,1,0,0);
//		
//		/*************
//		 * SELECT ?p2 ?o
//		 *************/
//		selVars = new ArrayList<Variable>();
//		selVars.add(o);selVars.add(p2);
//		sl.setResultVariable(selVars);
//		
//		test(sl,pt1,pt2, tp1In, tp2In, 0,1,1,0,1);
//		
//		/*************
//		 * SELECT ?p2 ?p
//		 *************/
//		selVars = new ArrayList<Variable>();
//		selVars.add(p);selVars.add(p2);
//		sl.setResultVariable(selVars);
//		
//		test(sl,pt1,pt2, tp1In, tp2In, 1,1,1,1,1);
	}
	
	/**
	 * 
	 * @param sl
	 * @param pt1
	 * @param pt2
	 * @param pt3
	 * @param tp1In
	 * @param tp2In
	 * @param tp3In
	 * @param tp1NC
	 * @param tp2NC
	 * @param tp2C
	 * @param tp3NC
	 * @param tp3C
	 * @param tp1InS
	 * @param tp2InS
	 * @param tp3InS
	 */
	private void test(QueryBasedSourceSelection sl, TriplePattern pt1,
			TriplePattern pt2, TriplePattern pt3, Node[] tp1In, Node[] tp2In,
			Node[] tp3In, int tp1NC, int tp2NC, int tp2C,int tp3NC, int tp3C, int tp1InS,int tp2InS,int tp3InS) {

		//submit pattern
 		assertTrue(sl.selectURIs(pt1, 0).size()==tp1NC);
 		assertTrue(sl.selectURIs(pt2, 1).size()==tp2C);
 		assertTrue(sl.selectURIs(pt2, 0).size()==tp2NC);
// 		System.out.println(sl.selectURIs(pt3, 1));
 		assertTrue(sl.selectURIs(pt3, 1).size()==tp3C);
// 		System.out.println(sl.selectURIs(pt3, 0));
 		assertTrue(sl.selectURIs(pt3, 0).size()==tp3NC);
 		
		assertTrue(sl.selectURIs(tp1In,pt1).size()==tp1InS);
		assertTrue(sl.selectURIs(tp2In,pt2).size()==tp2InS);
		assertTrue(sl.selectURIs(tp3In,pt3).size()==tp3InS);
		
		
	}

	/**
	 * @param sl
	 * @param pt1
	 * @param pt2
	 * @param tp2In 
	 * @param tp1In 
	 */
	private void test(QueryBasedSourceSelection sl, TriplePattern pt1, TriplePattern pt2, Node[] tp1In, Node[] tp2In, int tp1NC, int tp2NC, int tp2C, int tp1InS,int tp2InS) {

		//submit pattern
 		assertTrue(sl.selectURIs(pt1, 0).size()==tp1NC);
 		assertTrue(sl.selectURIs(pt2, 1).size()==tp2C);
 		assertTrue(sl.selectURIs(pt2, 0).size()==tp2NC);
 		
		assertTrue(sl.selectURIs(tp1In,pt1).size()==tp1InS);
		assertTrue(sl.selectURIs(tp2In,pt2).size()==tp2InS);
		
		
	}
}
