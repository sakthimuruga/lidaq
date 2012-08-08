package ie.deri.urq.lidaq.source;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import org.semanticweb.nxindex.NodesIndex;
import org.semanticweb.nxindex.block.NodesBlockReaderIO;
import org.semanticweb.nxindex.sparse.SparseIndex;

public interface CrawlTaskFactory {

	
	public DerefTask create(URI uri, SourceLookup sl,
			SourceLookupManager slm);
	
	public static CrawlTaskFactory HTTPFACTORY = new HTTPCrawlTaskFactory(); 
	
	
	
	
	public class HTTPCrawlTaskFactory implements CrawlTaskFactory{
		public DerefTask create(URI uri, SourceLookup sl,
				SourceLookupManager slm) {
			return new HttpDerefTask(uri, sl, slm);
		}
	}

	public static class IDXCrawlTaskFactory implements CrawlTaskFactory{
		
		
//		public static CrawlTaskFactory getIDXFACTORY(){
//			return new IDXCrawlTaskFactory();
//		}
		private static final Logger logger = Logger
		.getLogger(CrawlTaskFactory.IDXCrawlTaskFactory.class.getName());
		private static final String SPOC_IDX= "data-spoc.idx";
		private static final String SPOC_SPI= "data-spoc.spidx";
		private static final String OPSC_IDX= "data-opsc.idx";
		private static final String OPSC_SPI= "data-opsc.spidx";

		int OUT=0,IN=1;
		private NodesIndex[] ni;
		private Redirects rIDX;
		
		public IDXCrawlTaskFactory(File idxDir, File redir) throws IOException {
			 //check for spoc index
			
			File [] idx = new File[2];
			File [] spidx = new File[2];
			idx[OUT] = new File(idxDir,SPOC_IDX);spidx[OUT] = new File(idxDir,SPOC_SPI);int [] spoc = {0,1,2,3};
			idx[IN] = new File(idxDir,OPSC_IDX);spidx[IN] = new File(idxDir,OPSC_SPI);int [] opsc = {2,1,0,3};
			ni= new NodesIndex[2];

			logger.info("Reading: "+idx[OUT]+" "+spidx[OUT]);
			NodesBlockReaderIO nbrOUT = new NodesBlockReaderIO(idx[OUT].getAbsolutePath());
			SparseIndex spi = new SparseIndex(spidx[OUT].getAbsolutePath());
			ni[OUT] = new NodesIndex(nbrOUT, spi);

			logger.info("Reading: "+idx[IN]+" "+spidx[IN]);
			NodesBlockReaderIO nbrIN = new NodesBlockReaderIO(idx[IN].getAbsolutePath());
			SparseIndex spiIN = new SparseIndex(spidx[IN].getAbsolutePath());
			ni[IN] = new NodesIndex(nbrIN, spiIN);

			 rIDX = new Redirects(redir);


		}
		
		
		@Override
		public DerefTask create(URI uri, SourceLookup sl,
				SourceLookupManager slm) {
			return new IDXDerefTask(uri, sl, slm,ni,rIDX);
		}
	
	}
}
