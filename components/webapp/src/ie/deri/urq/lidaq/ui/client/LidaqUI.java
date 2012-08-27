package ie.deri.urq.lidaq.ui.client;






import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class LidaqUI implements EntryPoint,ValueChangeHandler<String>, ClickHandler {
	private static final Logger logger = Logger.getLogger(LidaqUI.class
			.getName());
	
	Welcome welcome;
	Header  header;
	About about;
	Query query;
	
	public void onModuleLoad() {
		String value = com.google.gwt.user.client.Window.Location.getHref();
		logger.info("onModuleLoad: "+value);
		History.addValueChangeHandler(this);
		header = new Header(this);
		
		RootPanel.get("header").add(header);
		
		welcome = new Welcome();
		about = new About();
		query = new Query();
		logger.info("Init HybridUI");
		
		String initToken = History.getToken();
		if (initToken.length() == 0)
		initToken = "About";

		// onHistoryChanged() is not called when the application first runs. Call
		// it now in order to reflect the initial state.
		changeContentView(initToken);
		
//		changeContent(welcome);
	}
    	
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String value = event.getValue();
		
		
		logger.info("onValueChange("+value+")");
		changeContentView(value);
		
	}



	private void changeContentView(String value) {
		if(value.equalsIgnoreCase("About")){
			changeContent(about);
		}
		else if(value.equalsIgnoreCase("LiLiDaQ")){
			logger.info("switchToView Lilidaq");
			query.resetView();
			changeContent(query);
			
		}
		else if(value.equalsIgnoreCase("Hybrid")){
			logger.info("switchToView Hybrid");
			query.resetView();
			changeContent(query);
		}
		else{
			
		}
		
	}

	private void changeContent(Composite view) {
		RootPanel.get("content").clear();
		RootPanel.get("content").add(view);
	}

	@Override
	public void onClick(ClickEvent event) {
		String value = ((Hyperlink) event.getSource()).getTargetHistoryToken();
		logger.info("onClick("+value+")");
		changeContentView(value);
		
	}

}
