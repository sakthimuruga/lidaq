/**
 *
 */
package ie.deri.urq.lidaq.ui.shared;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jul 3, 2011
 */
public class Bindings  implements Serializable,Comparable<Bindings>{

	
	LinkedHashMap<String, String> bindings = new LinkedHashMap<String, String>();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final Bindings doneBinding = new Bindings();
	static{
		doneBinding.put("done", "done");
	}
	/**
	 * @param next
	 * @param string
	 */
	public Bindings() {
		
	}




	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Bindings o) {
		int comp = bindings.size()-o.bindings.size();
		if(comp != 0) return comp;
		
		for(Map.Entry<String, String> ent: o.bindings.entrySet()){
			if(ent.getKey().equals("status")||ent.getKey().equals("time")) continue;
			if(bindings.containsKey(ent.getKey())){
				comp = bindings.get(ent.getKey()).compareTo(ent.getValue());
			}else return -1;
			if(comp != 0) return comp;
		}
		for(Map.Entry<String, String> ent: bindings.entrySet()){
			if(ent.getKey().equals("status")||ent.getKey().equals("time")) continue;
			if(o.bindings.containsKey(ent.getKey())){
				comp = o.bindings.get(ent.getKey()).compareTo(ent.getValue());
			}else return -1;
			if(comp != 0) return comp;
		}
		
		return 0;
	}

	
	
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return this.compareTo((Bindings)o)==0;
	}




	/**
	 * @param var
	 * @param string
	 */
	public void put(String var, String string) {
		bindings.put(var,string);
	}


/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Override
public String toString() {
	return bindings.toString();
}

	/**
	 * @param string
	 * @return
	 */
	public String get(String string) {
		return bindings.get(string);
	}
}
