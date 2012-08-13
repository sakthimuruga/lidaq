package org.semanticweb.yars2.query;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars2.query.iter.ManualQueryIterator;
import org.semanticweb.yars2.query.iter.local.DistinctIterator;


public class DistinctTest extends TestCase { 
	public void testDistinct() throws FileNotFoundException, ParseException, IOException {
		NxParser nxp = new NxParser(new FileInputStream("test/joinindex/query.1"));
		
		Variable[] spoc = { new Variable("s"), new Variable("p"), new Variable("o"),new Variable("c") };
		DistinctIterator dit = new DistinctIterator(new ManualQueryIterator(nxp, spoc));
		
		int i=0;
		
		while (dit.hasNext()) {
			dit.next();
			i++;
		}
		
		System.out.println("count; " + i);
	}
}
