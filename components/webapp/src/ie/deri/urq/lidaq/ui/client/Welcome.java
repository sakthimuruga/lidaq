package ie.deri.urq.lidaq.ui.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Welcome extends Composite {

	Map<String,String> resFormats = new HashMap<String,String>();
	{
		resFormats.put("application/rdf+xml","application/rdf+xml");
		resFormats.put("application/sparql-results+xml","application/sparql-results+xml");
		resFormats.put("application/sparql-results+json","application/sparql-results+json");
		resFormats.put("text/plain","text/plain");
		resFormats.put("text/html","text/html");
	}
	
	
	private static WelcomeUiBinder uiBinder = GWT.create(WelcomeUiBinder.class);

	interface WelcomeUiBinder extends UiBinder<Widget, Welcome> {
	}
	
	
	
	public Welcome() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	
	
	
	
	
//    <option value="application/rdf+xml"></option>
//	<option value="application/sparql-results+xml">application/sparql-results+xml</option>
//	<option value="application/sparql-results+json">application/sparql-results+json</option>
//	<option value="text/plain">text/plain</option>
//	<option value="text/html" selected="selected">text/html</option>
}
