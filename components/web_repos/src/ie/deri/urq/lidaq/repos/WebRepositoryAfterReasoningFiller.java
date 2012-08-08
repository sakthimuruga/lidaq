package ie.deri.urq.lidaq.repos;

import ie.deri.urq.lidaq.CONSTANTS.REASONING_MODE;

import org.semanticweb.saorr.Statement;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

public class WebRepositoryAfterReasoningFiller implements Callback{

	final private WebRepository _webRepos;
	private int count;

	public WebRepositoryAfterReasoningFiller(final WebRepository webRepos) {
		_webRepos = webRepos;
	}

	public void processStatement(Node[] statement) {
		if(statement[1].toString().equals("http://code.google.com/p/ldspider/ns#headerInfo")
				||statement[1].toString().startsWith("http://www.w3.org/2006/http#"))
			return;
		_webRepos.notify(statement);
		count++;

		if(_webRepos.doReasoning() && _webRepos.getResonerMode().name().contains(REASONING_MODE.RDFS.name())){
			_webRepos.getTripleStore().indexStatement(new Statement(statement));
		}
	}

	public int getCount(){return count;}
	public void endDocument() { ; }
	public void startDocument() { ; }

}
