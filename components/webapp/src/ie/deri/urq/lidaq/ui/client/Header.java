package ie.deri.urq.lidaq.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

public class Header extends Composite{

	
	
	private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);
	
	@UiField Hyperlink lilidaq;
	@UiField Hyperlink hybrid;
	@UiField Hyperlink about;
	
	interface HeaderUiBinder extends UiBinder<Widget, Header> {
	}
	
	
	public Header() {
		
	}


	public Header(LidaqUI hybridUI) {
		initWidget(uiBinder.createAndBindUi(this));
		lilidaq.addClickHandler(hybridUI);
		hybrid.addClickHandler(hybridUI);
		about.addClickHandler(hybridUI);
	}  
}
