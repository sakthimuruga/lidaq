/**
 *
 */
package ie.deri.urq.lidaq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Aug 21, 2010
 */
public class Utils {
	
	private static final Logger logger = Logger.getLogger(Utils.class.getName());
	public final static String NL = System.getProperty("line.separator");
	
	/**
	 * 
	 * @param file
	 * @return - a {@link String} containing the file content
	 * @throws IOException 
	 */
	public static String readFileContent(String file) throws IOException{
		return readFileContent(new File(file));
	}
	
	/**
	 * 
	 * @param file
	 * @return - a {@link String} containing the file content
	 * @throws IOException 
	 */
	public static String readFileContent(File file) throws IOException{
		FileInputStream stream = new FileInputStream(file);
		try {
			FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
	}
}
