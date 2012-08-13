
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import com.sun.tools.extcheck.Main;

/**
 *
 */

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Apr 28, 2011
 */
public class CLI extends TestCase{

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

	public void testCLIMain() throws Exception {
		String [] args = {};
		ie.deri.urq.lidaq.cli.Main.main(args);
	}
}
