import java.io.IOException;

import ie.deri.urq.lidaq.cli.Main;

import org.junit.Test;


public class TestASKWBenchGet {

	
	@Test
	public void test() throws ClassNotFoundException, IOException{
//		String[]args = 
//			{""};
		String [] args = {"AKSWBenchGet","-id","/Users/juum/Documents/deri-svn/resources/queries/dbspb/Queries.txt","-o","/Users/juum/Documents/deri-svn/resources/queries/dbspb.queries"};
		ie.deri.urq.lidaq.cli.Main.main(args);	
	}	
}
