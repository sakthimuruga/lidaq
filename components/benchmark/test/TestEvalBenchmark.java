import ie.deri.urq.lidaq.cli.Main;
import ie.deri.urq.lidaq.log.LIDAQLOGGER;

import java.io.IOException;

import junit.framework.TestCase;

/**
 *
 */

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Apr 8, 2011
 */
public class TestEvalBenchmark extends TestCase   {

	
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		LIDAQLOGGER.setDefaultLogging();
		String [] arg ={
				"BEval"
				,"-bd","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/all.out.1/tmp"
				,"-o","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval/all.out.1.tmp.out/"
		};
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
		
//		String [] arg ={
//				"BEval"
//				,"-bd","/Users/juum/Resources/evaluations/ldquery_reason/swj.queries.eval"
//				,"-o","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/swj.queries.eval.out/"
//		};
		arg[2]="/Users/juum/Resources/evaluations/ldquery_reason/swj.queries.eval";
		arg[4]="/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/swj/eval/webqueries/";
//		Main.main(arg);
		
		arg[2]="/Users/juum/Resources/evaluations/ldquery_reason/fedbench/fedbench_ld.03.03.12";
		arg[4]="/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/fedbench.out-03";
////		Main.main(arg);
//
//		arg[2]="/Users/juum/Resources/evaluations/ldquery_reason/fedbench/fedbench_ld.12.03.2012";
//		arg[4]="/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/fedbench.out-12";
//////		Main.main(arg);
////		
//		arg[2]="/Users/juum/Resources/evaluations/ldquery_reason/fedbench/fedbench_ld.15.03.2012";
//		arg[4]="/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/fedbench.out-15";
//////		Main.main(arg);
////		
//		arg[2]="/Users/juum/Resources/evaluations/ldquery_reason/fedbench/fedbench_ld.20.03.2012";
//		arg[4]="/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/fedbench.out-20";
//////		Main.main(arg);
//		arg[2]="/Users/juum/Resources/evaluations/ldquery_reason/fedbench/fedbench_ld.27.03.2012";
//		arg[4]="/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/fedbench.out-27";
//////		Main.main(arg);
//		
//		arg[2]="/Users/juum/Resources/evaluations/ldquery_reason/dbpedia/12.03.2012";
//		arg[4]="/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/dbpedia/12.03.2012.out";
////		Main.main(arg);
//		
		arg[2]="/Users/juum/Resources/evaluations/ldquery_reason/dbpedia/dbspb.queries.25.eval";
//////		arg[4]="/Users/juum/Resources/evaluations/ldquery_reason/dbpedia/dbspb.queries.25.eval.out";
		arg[4]="/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/dbpedia.out";
		Main.main(arg);
		
//		String [] arg ={
//				"BEval"
////				,"-bd","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/dbpedia/dbspb.queries.25.eval"
//				,"-bd","/Users/juum/Resources/evaluations/ldquery_reason/dbpedia/12.03.2012"
//				,"-o","/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/dbpedia.out"
////				"/Users/juum/Documents/deri-svn/pubs/ldquery_reasoning/eval.swj/dbpedia/dbspb.queries.25.eval.out"
//		};
//		Main.main(arg);
		
		
	}
	
}
