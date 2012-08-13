package ie.deri.urq.lodq;

import java.util.ArrayList;
import java.util.HashSet;

import ie.deri.urq.lidaq.query.arq.DerefKeyObserver1;
import ie.deri.urq.lidaq.repos.PatternIndex;
import ie.deri.urq.lidaq.repos.TriplePattern;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

public class TriplePatternIndexTEST {

	
	
	public static void main(String[] args) {
		PatternIndex pidx = new PatternIndex();
		
		Variable c = new Variable("c");
		Variable o = new Variable("o");
		Variable ctx = new Variable("ctx");
		
		Node [] key = {new Resource("test"), c, o, ctx};
		ArrayList<Variable> vars = new ArrayList<Variable>();
		vars.add(c);
		vars.add(o);
		int [] pos = {1,2};
		int [] pos1 = {1};
		HashSet<String> src = new HashSet<String>();
		DerefKeyObserver1 a = new DerefKeyObserver1(null, null);
		DerefKeyObserver1 aa = new DerefKeyObserver1(null, null);
		TriplePattern tp = new TriplePattern(key, vars, pos, src, a);
		TriplePattern tp2 = new TriplePattern(key, vars, pos, src, aa);
		
		pidx.indexPattern(tp);
		pidx.indexPattern(tp2);
		
//		System.out.println(tp);
		for(TriplePattern tp1: pidx.getRelevantPattern(key)){
			System.out.println(tp1);
		}
//		System.out.println(tp2);
	}
}
