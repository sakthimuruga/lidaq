import ie.deri.urq.lidaq.cli.Main;

import java.io.IOException;

import junit.framework.TestCase;

/**
 *
 */

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Apr 8, 2011
 */
public class CopyOfTestEvalBenchmark extends TestCase   {

	
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		
//		String [] arg ={
//				"BEval"
//				,"-bd","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/all.out.1/tmp"
//				,"-o","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/all.out.1.tmp.out/"
//		};
//		Main.main(arg);
//		
//		String [] arg ={
//				"BEval"
//				,"-bd","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/dbpsb.eval"
//				,"-o","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/dbpsb.eval.out/"
//		};
//		Main.main(arg);
//		
		
		
		
//		String [] arg ={
//				"BEval"
//				,"-bd","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/queries-200.eval.old/"
//				,"-o","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/queries-200.eval.old.out/"
//		};
//		Main.main(arg);
//		
//		
//		String [] arg1 ={
//				"BEval"
//	
//				,"-bd","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/queries-200.eval/"
//				,"-o","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/queries-200.eval.out/"
//
//		};
//		Main.main(arg1);
//		
//		String [] arg2 ={
//				"BEval"
//	
//				,"-bd","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/update/"
////				,"-bd","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/update"
////				,"-o","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/update.eval"
//				,"-o","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/update.out/"
////						"archives/jueumb/pubs/ldquery_reasoning/eval/queries-200.eval.36.out"
//		};
//		Main.main(arg2);
		
		String [] arg ={
				"BEval"
				,"-bd","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.btc2011/shape_queries.50.eval/"
				,"-o","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.btc2011/shape_queries.50.eval.out/"
		};
		Main.main(arg);
	}
	
}
