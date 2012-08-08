package ie.deri.urq.lidaq.benchmark;
/**
 *
 */


import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Sep 29, 2010
 */
abstract public class Benchmark extends Properties{

	private static final long serialVersionUID = 1L;
	public Benchmark() {
		super();
	}

	public abstract Map<String,List<String>> getKeyOrder();

	public synchronized String toString() {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, List<String>> ent: getKeyOrder().entrySet()){
			sb.append("___"+ent.getKey()+"___\n");
			for(String s: ent.getValue()){
				sb.append(s).append(" = ").append(getProperty(s)).append("\n");
			}}
		return sb.toString();
	}

	
	
	protected static final String TOTAL = "total";
	protected static final String ABOX  = "abox";
	protected static final String TBOX  = "tbox";
	protected static final String SEP = "_";
	protected static final String SIZE = "size";
	protected static final String SAME_AS = "same_as";
	protected static final String ERRORS = "err";

	@Override
	public synchronized Object put(Object key, Object value) {
//		if(!(value instanceof String) && !(value instanceof Integer)){
//			System.out.println(value.getClass());
//		}else
		return super.put(key, value.toString());
//		return true;
	}
	
	@Override
	public synchronized String get(Object key) {
		return (String)super.get(key.toString());
	}

}
