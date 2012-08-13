package ie.deri.urq.lodq;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;

public class BlockingQueueTests {

	public static void main(String[] args) {
//		LinkedBlockingQueue<String> lbq = new  LinkedBlockingQueue<String>();
//		PriorityBlockingQueue<String> pbq = new PriorityBlockingQueue<String>(10, new Comparator<String>() {
//
//			@Override
//			public int compare(String o1, String o2) {
//				System.out.println(o1.compareTo(o2));
//				return o1.compareTo(o2);
//			}
//		});
//		ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<String>(10);
//		
////		String test = "aa";
//		testQueue(lbq);
//		
//		testQueue(pbq);
//		testQueue(abq);
		
		
		 Binding POISON_TOKEN = BindingFactory.binding(Var.alloc("poison"), Node.createLiteral("poison"));
		 Binding POISON_TOKEN2 = BindingFactory.binding(Var.alloc("poison1"), Node.createLiteral("poison"));
		 Set<Binding> cache = Collections.newSetFromMap(new ConcurrentHashMap<Binding, Boolean>());
		 cache.add(POISON_TOKEN);
		 System.out.println(cache.contains(POISON_TOKEN2));

		
	}

	private static void testQueue(BlockingQueue<String> q) {
		System.out.println(q.getClass().getSimpleName());
		String test = "test";
		System.out.println("  adding \""+test+"\"");
		q.add(test);
		System.out.println("  queue size: "+q.size());
		System.out.println("  adding \""+test+"\"");
		q.add(test);
		System.out.println("  queue size: "+q.size());
		for(String s: q){
			System.out.println("  >> "+s);
		}
	}
}
