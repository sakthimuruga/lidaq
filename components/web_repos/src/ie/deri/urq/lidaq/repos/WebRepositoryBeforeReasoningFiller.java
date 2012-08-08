package ie.deri.urq.lidaq.repos;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;

import org.semanticweb.saorr.Statement;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

public class WebRepositoryBeforeReasoningFiller implements Callback{
	

		final private WebRepository _webRepos;

		private int count;

		private long lastReceived =-1L;

		public WebRepositoryBeforeReasoningFiller(final WebRepository webRepos) {
			_webRepos = webRepos;
			lastReceived  = System.currentTimeMillis();
		}

		public void processStatement(Node[] statement) {
			if(statement[1].toString().equals("http://code.google.com/p/ldspider/ns#headerInfo")
					||statement[1].toString().startsWith("http://www.w3.org/2006/http#"))
				return;
			
			lastReceived  = System.currentTimeMillis();
			count++;

			//index statement if we 1) not do reasoning or 2) perform RDFS reasoning
			if(!_webRepos.doReasoning() || _webRepos.getResonerMode().name().contains(REASONING_MODE.RDFS.name())){
//				System.out.println(_webRepos.getResonerMode());
				if(_webRepos.getPredFilter() == null || _webRepos.getPredFilter().contains(statement[1])){
					/*should be fine to apply a pred content filter here, 
					either we do no reasoning, then it is fine, or we do 
					RDFS reasoning and the predicates will be added to the reasoner anyway*/
					_webRepos.getTripleStore().indexStatement(new Statement(statement));
				}
			}
			if(_webRepos.doReasoning()){
				_webRepos.getReasonerFramework().processStatement(statement);
			}
		}

		
		public int getCount(){return count;}
		public void endDocument() { ; }
		public void startDocument() { ; }

		public long lastStmtReceived() {
			return lastReceived;
		}
	
}
