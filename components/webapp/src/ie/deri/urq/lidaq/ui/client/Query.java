package ie.deri.urq.lidaq.ui.client;

import ie.deri.urq.lidaq.ui.shared.Bindings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class Query extends Composite implements ChangeHandler, SubmitHandler, SubmitCompleteHandler {

	private static final Logger logger = Logger.getLogger(Query.class.getName());
	
	Map<String,String> resFormats = new LinkedHashMap<String,String>();
	{
		resFormats.put("application/rdf+xml","application/rdf+xml");
		resFormats.put("application/sparql-results+xml","application/sparql-results+xml");
		resFormats.put("application/sparql-results+json","application/sparql-results+json");
		resFormats.put("text/plain","text/plain");
		resFormats.put("text/html","text/html");
	}
	
	Map<String,String> sparqlEps = new LinkedHashMap<String,String>();
	{
		sparqlEps.put("Sindice", "http://sparql.sindice.com/sparql");
		sparqlEps.put("OpenLink", "http://lod.openlinksw.com/sparql");
	}
	Map<String,String> srcSelMap = new LinkedHashMap<String,String>();
	{
		srcSelMap.put("select", "smart");
		srcSelMap.put("all URIs", "all");
		srcSelMap.put("only query entities", "onlySrc");
		srcSelMap.put("subject/object", "so");
	}
	Map<String,String> linksMap = new LinkedHashMap<String,String>();
	{
		linksMap.put("off", "off");
		linksMap.put("seeAlso", "seeAlso");
		linksMap.put("owl", "owl");
		linksMap.put("both", "all");
	}
	Map<String,String> reasoningMap = new LinkedHashMap<String,String>();
	{
		reasoningMap.put("off", "off");
		reasoningMap.put("rdfs", "rdfs");
	}
	Map<String,String> anyMap = new LinkedHashMap<String,String>();
	{
		anyMap.put("disable", "off");
		anyMap.put("enable", "on");
	}
	
	static {
		logger.info("static init");
	}
	private static QueryUiBinder uiBinder = GWT.create(QueryUiBinder.class);
	interface QueryUiBinder extends UiBinder<Widget, Query> {
	}
	
	@UiField FormPanel qForm;
	@UiField FlowPanel queryForm;
	@UiField VerticalPanel resultPanel;
	@UiField VerticalPanel debugPanel;
	@UiField VerticalPanel resultTbl;
	@UiField VerticalPanel errorPanel;
	@UiField HorizontalPanel sparqlEP;
	@UiField TextArea query;
	@UiField TextArea debugQuery;
	@UiField HTMLPanel errorTxt;
	@UiField Label errorCls;
	@UiField Button execBtn;
	@UiField ListBox resFormat;
	@UiField ListBox tmplQuery;
	@UiField ListBox srcSel;
	@UiField ListBox links;
	@UiField ListBox reasoning;
	@UiField ListBox ep;
	@UiField ListBox any23;
	@UiField FlexTable debugTbl;
	
	private ListBox visibResSize;
	private  CellTable<Bindings> results;
	private ListDataProvider<Bindings> bindings;
	private ArrayList<String> _rVars;
	private LiLiDaqServiceAsync _s = null; 

	String[] debugValues ={
			"2XX","3XX","4XX","5XX","6XX","Retrieved","Infered"
	};
//
	int staticResult=0,liveResult=0;


	private long _start;
//	
//	/**
//	 * This is the entry point method.
//	 */
//
	private HashMap<String, String> tmplQueriesMap;
	
	
	
	
	
	public Query() {
		if(_s==null){
			_s = GWT.create(LiLiDaqService.class);
//			((ServiceDefTarget) _s).setServiceEntryPoint("query");
			
		}
		logger.info("init service at  "+((ServiceDefTarget) _s).getServiceEntryPoint());
		initWidget(uiBinder.createAndBindUi(this));
		
		visibResSize = new ListBox();
		
		qForm.setEncoding("utf-8");
		qForm.setAction("sparql?");
		qForm.setMethod(FormPanel.METHOD_GET);
		
		logger.info("We are here 1");
		for(Entry<String,String> ent: resFormats.entrySet()){
			resFormat.addItem(ent.getKey(), ent.getValue());
		}
		resFormat.setItemSelected(resFormat.getItemCount()-1,true);
		for(Entry<String,String> ent: srcSelMap.entrySet()){
			srcSel.addItem(ent.getKey(), ent.getValue());
		}
		srcSel.setItemSelected(0,true);
		for(Entry<String,String> ent: linksMap.entrySet()){
			links.addItem(ent.getKey(), ent.getValue());
		}
		links.setItemSelected(0,true);
		for(Entry<String,String> ent: reasoningMap.entrySet()){
			reasoning.addItem(ent.getKey(), ent.getValue());
		}
		reasoning.setItemSelected(0,true);
		
		for(Entry<String,String> ent: anyMap.entrySet()){
			any23.addItem(ent.getKey(), ent.getValue());
		}
//		any23.setItemSelected(0,true);
//		
		for(Entry<String,String> ent: sparqlEps.entrySet()){
			ep.addItem(ent.getKey(), ent.getValue());
		}
		sparqlEP.getElement().addClassName("hide");
		ep = null;
		
		queryForm.getElement().setId("queryForm");
		query.getElement().setId("query");
		execBtn.getElement().setId("execBtn");
		
		logger.info("Button is enable "+execBtn.isEnabled()+" "+execBtn.isAttached());
		resultPanel.getElement().setId("results");
		debugPanel.getElement().setId("debug");
		logger.info("We are here 2");
		
		query.setText("SELECT ?o\nWHERE{\n\t<http://umbrich.net/foaf.rdf#me> ?p ?o .\n}");
		
		tmplQuery.addChangeHandler(this);
		resFormat.addChangeHandler(this);
		logger.info("We are here 3");
		
		_s.getTemplateQueries(new AsyncCallback<HashMap<String,String>>() {
			public void onFailure(Throwable caught) {
				switchToErrorView(caught);
			}
			public void onSuccess(HashMap<String, String> result) {
				for(Entry<String,String> ent: result.entrySet()){
					tmplQuery.addItem(ent.getKey(), ent.getKey());
				}
				tmplQuery.setItemSelected(0, false);
				for(int i =0; i < tmplQuery.getItemCount();i++){
					if(tmplQuery.getItemText(i).equalsIgnoreCase("empty"))
						tmplQuery.setItemSelected(i, true); 
				}
				tmplQueriesMap=result;
				
			}
		});
		logger.info("Init Query View Done");
	}

	public void resetView() {
		logger.info("Reseting view");
		results = new CellTable<Bindings>();
		resultTbl.clear();
		resultTbl.add(results);
		
		
		
		queryForm.getElement().removeClassName("hide");
		resultPanel.getElement().addClassName("hide");
		debugPanel.getElement().addClassName("hide");
		errorPanel.getElement().addClassName("hide");
	}
	
	private void resultView() {
		logger.info("Switch to result view");
		queryForm.getElement().addClassName("hide");
		resultPanel.getElement().removeClassName("hide");
		debugPanel.getElement().removeClassName("hide");
		errorPanel.getElement().addClassName("hide");
	}
	
	private void switchToErrorView(Throwable caught) {
		queryForm.getElement().addClassName("hide");
		resultPanel.getElement().addClassName("hide");
		debugPanel.getElement().addClassName("hide");
		errorPanel.getElement().removeClassName("hide");
		
		StringBuilder sb = new StringBuilder();
		if(caught instanceof QueryException){
			sb.append(((QueryException)caught).toString());
		}
		else{
		
		for(StackTraceElement e:caught.getStackTrace()){
			sb.append(e.toString()).append("\n");
		}
		}
		int code = 0;
		if(caught instanceof StatusCodeException){
			code = ((StatusCodeException)caught).getStatusCode();
		}
		errorTxt.setTitle(caught.getClass().getName()+":"+code);
		errorCls.getElement().setInnerHTML(caught.getMessage()+"\n"+ sb.toString());		
	}
	
	
	
	@UiHandler("execBtn")
	void buttonClick(ClickEvent event) {
		logger.info("Execute the query");
		query();
	}
	
	/**
	 * 
	 */
	private void query() {
		try{
		HashMap<String, String> eps = new HashMap<String, String>();
		if(ep != null){
			for(int i=0; i <ep.getItemCount(); i++ ){
				if(ep.isItemSelected(i)){
					String v_sparqlEp = ep.getValue(i);
					eps.put(ep.getItemText(i), ep.getValue(i));
				}
			}
		}
		logger.info("Selected endpoints: "+eps);
		String v_format = resFormat.getValue(resFormat.getSelectedIndex());
		logger.info("Selected v_format: "+v_format);
		boolean submit = (eps.size() == 0 && !v_format.equalsIgnoreCase("text/html"));
		logger.info("Execute query via servlet: "+submit);
		if(submit){
//			qForm.
			qForm.setEncoding("multipart/form-data; charset=UTF-8");
			Element elFp = qForm.getElement();
			elFp.setAttribute("target", "_blank");
			qForm.addSubmitHandler(this);
			qForm.addSubmitCompleteHandler(this);
			logger.info("Form: "+qForm.getAction()+" method:"+qForm.getMethod());
			qForm.submit();
		}else{
			switchToResultView();

			String sparqlQuery = query.getText();

			String v_srcSel = srcSel.getValue(srcSel.getSelectedIndex());;
			String v_links = links.getValue(links.getSelectedIndex());;
			String v_rMode = reasoning.getValue(reasoning.getSelectedIndex());;

			if(v_rMode.equalsIgnoreCase("rdfs") && (v_links.equalsIgnoreCase("all") || v_links.equalsIgnoreCase("owl"))){
				v_rMode = "ALL";
			}else if (v_rMode.equalsIgnoreCase("rdfs")){
				v_rMode = "RDFS";
			}else if ((v_links.equalsIgnoreCase("all") || v_links.equalsIgnoreCase("owl"))){
				v_rMode = "OWL";
			}else{
				v_rMode = "OFF";
			}

			boolean any23On = any23.getValue(any23.getSelectedIndex()).equalsIgnoreCase("on");
			boolean seeAlso = v_links.equalsIgnoreCase("all") || v_links.equalsIgnoreCase("seeAlso");
			
			logger.info("[EXECUTE] srcSel:"+v_srcSel+" seeAlso:"+seeAlso+" rMode:"+v_rMode+" any23:"+any23On);
			_start = System.currentTimeMillis();
			_s.executeQuery(sparqlQuery, v_srcSel, seeAlso, v_rMode, eps,any23On, new AsyncCallback<String[]>() {
				public void onFailure(Throwable caught) {
					switchToErrorView(caught);
				}
				
				@Override
				public void onSuccess(String[] result) {
					String queryID = result[0];
					_rVars = new ArrayList<String>();
					// Create a data provider.
					bindings = new ListDataProvider<Bindings>();
					// Connect the table to the data provider.
					bindings.addDataDisplay(results);

					for(int i =1; i < result.length;i++){
						SpecificTextColumn a = new SpecificTextColumn(result[i]);
						a.setSortable(true);
						results.addColumn(a, result[i]);
						//						results.setText(0, i-1, result[i]);
						//						results.getCellFormatter().addStyleName(0,i-1,"th");
						_rVars.add(result[i]);
						ListHandler sortHandler = new ListHandler(bindings.getList());
						sortHandler.setComparator(a, new SpecifiBindingComparator(result[i]));
						results.addColumnSortHandler(sortHandler);
					}
					//					results.setVisibleRange(0,10);
					//					results.set
					updateResultView(queryID);
				}
			});
		}
		}catch(Exception e){
			switchToErrorView(e.getCause());
		}
	}
	/**
	 * 
	 */
	private void switchToResultView() {
		resultView();	
		
		for(int i=0; i< debugValues.length;i++){
			debugTbl.setText(0,i,debugValues[i]);
			debugTbl.getCellFormatter().addStyleName(0,i,"th");
			debugTbl.setText(1,i,"0");
		}
		debugQuery.setText(query.getText());
		debugQuery.setEnabled(false);
		debugQuery.setHeight(""+(countIndexOf(query.getText(), "\n")*20)+"px");

		SimplePager.Resources pagerResources = 
			GWT.create(SimplePager.Resources.class); 
		SimplePager pager = new SimplePager(TextLocation.CENTER, 
				pagerResources, false, 0, true); 
		pager.setDisplay(results); 
		pager.setPageSize(10); 
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(pager);hp.add(visibResSize);
		resultTbl.add(hp);
		pager.setPageSize(10);
		results.setPageSize(10);
		visibResSize.addItem("10", "10");
		visibResSize.addChangeHandler(this);
	}
	public static int countIndexOf(String content, String search) {
		int ctr = -1;
		int total = 0;
		while (true) {
			if (ctr == -1) ctr = content.indexOf(search);
			else ctr = content.indexOf(search, ctr);
			if (ctr == -1) {
				break;
			} else {
				total++;
				ctr += search.length();
			}
		}
		return total;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event.dom.client.ChangeEvent)
	 */
	@Override
	public void onChange(ChangeEvent event) {
		if(ep!=null && event.getSource().equals(ep)){
			if(!ep.getValue(ep.getSelectedIndex()).equals("off")){
				resFormat.setItemSelected(4, true);
			}
		}else if(event.getSource().equals(resFormat)){
			if(!resFormat.getValue(resFormat.getSelectedIndex()).equals("text/html")&& ep!=null)
				ep.setItemSelected(0,true);
		}else if(event.getSource().equals(tmplQuery)){
			query.setText(tmplQueriesMap.get(tmplQuery.getValue(tmplQuery.getSelectedIndex())));
		}else if(event.getSource().equals(visibResSize)){
			int visibSize = Integer.valueOf(visibResSize.getValue(visibResSize.getSelectedIndex()));
			results.setVisibleRange(0, visibSize);
			DOM.getElementById("results").getStyle().setHeight(Math.min((60+30*(bindings.getList().size()+5)),(60+30*(results.getVisibleItemCount()+5))), Unit.PX);
		}
	}

	private void updateResultView(final String queryID) {
		_s.getNewResults(queryID, new AsyncCallback<List<Bindings>>() {

			@Override
			public void onFailure(Throwable caught) {
				switchToErrorView(caught);
				
			}

			@Override
			public void onSuccess(List<Bindings> result) {
				//first binding contains the debug statistics
				//lets update them
				DOM.getElementById("resultH2").setInnerText(bindings.getList().size()+" Results");
				DOM.getElementById("processH2").setInnerText("LiDaQ - Processing ["+(System.currentTimeMillis()-_start)+" ms]");
				if(result.size()==0){
					//there must be an error
				}
				else if(result.size()>=1){
					Bindings debB = result.get(0);
					for(int i=0; i< debugValues.length;i++){
						debugTbl.setText(1,i,debB.get(debugValues[i]));
					}
				}
				List<Bindings> b = bindings.getList();
				DOM.getElementById("resultH2").setInnerText(b.size()+" Results");
				for(int i =1; i<result.size();i++){
					Bindings rbw = result.get(i);
					if(rbw.equals(Bindings.doneBinding)){
						return;
					}
					int row = results.getRowCount();
					if(b.contains(rbw)){
						Bindings exist = b.get(b.indexOf(rbw));
						String curStatus = rbw.get("status");
						String existStatus = exist.get("status");

						if(!curStatus.equals(existStatus)){
							if(!existStatus.contains(curStatus))
								existStatus+=","+curStatus;
							//							results.setText(row, _rVars.size()-2, tblTXT);
							exist.put("time",rbw.get("time"));
							if(existStatus.contains("lidaq")){
								//								results.getRowElement(row).getStylgetRowFormatter().addStyleName(row,"update");
							}
							exist.put("status",existStatus);

							if(rbw.get("status").equals("lidaq")) liveResult++;
							else  staticResult++;

						}
						//						row = bindings.indexOf(rbw)+1;
					}else{
						b.add(rbw);
						if(rbw.get("status").equals("lidaq")) liveResult++;
						else  staticResult++;
					}
				}
				DOM.getElementById("results").getStyle().setHeight(Math.min((60+30*(5+b.size())),(60+30*(results.getVisibleItemCount()+5))), Unit.PX);
				//show results
				results.setRowCount(b.size(), true);
				if(b.size()>25 && visibResSize.getItemCount()<2)
					visibResSize.addItem("25","25");
				if(b.size()>50 && visibResSize.getItemCount()<3)
					visibResSize.addItem("50","50");
				//				results.setVisibleRange(0, b.size());
				results.redraw();
				Timer t = new Timer() {
					public void run() {
						updateResultView(queryID);				
					}
				};
				t.schedule(500);
			}
		});
	}

	private  class SpecificTextColumn extends TextColumn<Bindings>{

		private final  String _key;

		/**
		 * 
		 */
		public SpecificTextColumn(String key) {
			_key = key;
		}

		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(Bindings object) {
			String value = object.get(_key);
			if(value == null) value = "-";
			return value;
		}
	}
	private class SpecifiBindingComparator implements Comparator<Bindings>{
		private String _key;
		public SpecifiBindingComparator(String key) {
			_key = key;
		}
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Bindings arg0, Bindings arg1) {
			String s0 = arg0.get(_key);
			String s1 = arg1.get(_key);
			if(s0 == null) return -1;
			else if(s1 == null) return 1;
			else 
				return s0.compareTo(s1);
		}
	}
	
	@Override
	public void onSubmit(SubmitEvent event) {
		logger.info(event.toDebugString()+" canceled: "+event.isCanceled());
		
	}

	@Override
	public void onSubmitComplete(SubmitCompleteEvent event) {
		logger.info("complete event: "+event.toDebugString()+" res: "+event.getResults());
		
	}
}