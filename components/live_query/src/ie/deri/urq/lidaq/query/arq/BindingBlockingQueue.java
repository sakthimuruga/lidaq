/**
 *
 */
package ie.deri.urq.lidaq.query.arq;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jun 11, 2011
 */
public class BindingBlockingQueue extends LinkedBlockingQueue <Binding> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Binding POISON_TOKEN = BindingFactory.binding(Var.alloc("poison"), Node.createLiteral("poison"));

	private final Set<Binding> cache;
	
	@Override
	public boolean add(Binding e) {
		
		if(!cache.contains(e)){
			cache.add(e);
			super.add(e);
		}
		return true;
	}
	
	public int cacheSize(){
		return cache.size();
	}
	
	public BindingBlockingQueue() {
		super();
		cache = Collections.newSetFromMap(new ConcurrentHashMap<Binding, Boolean>());
	}
	
	/**
	 * @param capacity
	 */
	public BindingBlockingQueue(int capacity) {
		super(capacity);
		cache = Collections.newSetFromMap(new ConcurrentHashMap<Binding, Boolean>());
	}
}