package ie.deri.urq.lidaq.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class About extends Composite {

	
	
	private static AboutUiBinder uiBinder = GWT.create(AboutUiBinder.class);

	interface AboutUiBinder extends UiBinder<Widget, About> {
	}
	
	
	
	public About() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	
	
	
	
	
//    <option value="application/rdf+xml"></option>
//	<option value="application/sparql-results+xml">application/sparql-results+xml</option>
//	<option value="application/sparql-results+json">application/sparql-results+json</option>
//	<option value="text/plain">text/plain</option>
//	<option value="text/html" selected="selected">text/html</option>
}
