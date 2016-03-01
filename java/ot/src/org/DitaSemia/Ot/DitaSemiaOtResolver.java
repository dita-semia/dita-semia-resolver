package org.DitaSemia.Ot;

import static org.apache.commons.io.FileUtils.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Ot.Conbat.ResolveEmbeddedXPathDef;
import org.DitaSemia.Ot.XsltConref.ResolveXsltConrefDef;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.util.Job.FileInfo;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;

public class DitaSemiaOtResolver extends AbstractPipelineModuleImpl {

	protected static final String FILE_EXTENSION_TEMP	= ".dita-semia.temp";

	protected static final String XSLT_FILE_URL 		= "/xsl/resolve.xsl";

	protected static final String NEEDS_RESOLVER_XPATH 	= "exists(//@xcr:xsl | //@cba:*)";
	protected static final String BASE_URI_XPATH 		= "/*/@xtrf";
	
	
	// <TEMP>
	// required until DITA-OT 2.3 (#2177 Mark AbstractPipelineModuleImpl members as protected...)  
	
	protected DITAOTLogger logger;
    protected Job job;

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }
    
    @Override
    public void setJob(final Job job) {
        this.job = job;
    }
    // </TEMP>
    
    

	protected Processor 			resolverProcessor			= null;
	protected XsltExecutable 		resolverExecutable			= null;
	protected DocumentBuilder 		resolverDocBuilder			= null;
	protected XPathExecutable 		needsResolveXPath			= null;
	protected XPathExecutable 		baseUriXPath				= null;
	
	protected XslTransformerCache	xsltConrefTransformerCache	= null;
	protected XMLReader				xsltConrefXmlReader			= null;

	protected XPathCache			xPathCache					= null;
	
	protected URL					currentBaseUri				= null;

	protected void init() throws DITAOTException {

		// xslt-conref
		Configuration xsltConrefConfiguration;
		try {
			xsltConrefConfiguration = XsltConref.createConfiguration();
		} catch (XPathException e) {
			throw new DITAOTException(e.getMessage(), e);
		}
		xsltConrefConfiguration.setURIResolver(CatalogUtils.getCatalogResolver());
		
		xsltConrefTransformerCache	= new XslTransformerCache(xsltConrefConfiguration);
		xPathCache					= new XPathCache(xsltConrefConfiguration);
		try {
			xsltConrefXmlReader		= XMLUtils.getXMLReader();
		} catch (SAXException e) {
			throw new DITAOTException(e.getMessage(), e);
		}
		
		xsltConrefXmlReader.setEntityResolver(CatalogUtils.getCatalogResolver());
		
		// general resolver
		Configuration resolverConfiguration;
		try {
			resolverConfiguration = XsltConref.loadBaseConfiguration();
		} catch (XPathException e) {
			throw new DITAOTException(e.getMessage(), e);
		}
		
		// ensure that both configurations are compatible
		resolverConfiguration.setNamePool(xsltConrefConfiguration.getNamePool());
		resolverConfiguration.setDocumentNumberAllocator(xsltConrefConfiguration.getDocumentNumberAllocator());

		resolverConfiguration.setURIResolver(CatalogUtils.getCatalogResolver());
		resolverConfiguration.registerExtensionFunction(new ResolveXsltConrefDef(this));
		resolverConfiguration.registerExtensionFunction(new ResolveEmbeddedXPathDef(this));

		resolverProcessor 		= new Processor(resolverConfiguration);
		resolverDocBuilder 		= resolverProcessor.newDocumentBuilder();
		
		final XPathCompiler xPathCompiler 	= resolverProcessor.newXPathCompiler();
		xPathCompiler.declareNamespace(XsltConref.NAMESPACE_PREFIX, XsltConref.NAMESPACE_URI);
		xPathCompiler.declareNamespace(ResolveEmbeddedXPathDef.NAMESPACE_PREFIX, ResolveEmbeddedXPathDef.NAMESPACE_URI);
		try {
			needsResolveXPath	= xPathCompiler.compile(NEEDS_RESOLVER_XPATH);
			baseUriXPath		= xPathCompiler.compile(BASE_URI_XPATH);
		} catch (SaxonApiException e) {
			throw new DITAOTException(e.getMessage(), e);
		}
	}
	

	@Override
	public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
		
		init();

		final Collection<FileInfo> fis = job.getFileInfo();
		for (final FileInfo fi: fis) {
			final File file = new File(job.tempDir, fi.file.getPath());
			logger.info("Processing " + file.getAbsolutePath());

			final XdmNode sourceNode;
			try {
				sourceNode = resolverDocBuilder.build(new SAXSource(new InputSource(file.getAbsolutePath())));
			} catch (SaxonApiException e) {
				throw new DITAOTException("Failed to load file '" + file.getName() + "': " + e.getMessage(), e);
			}
			
			try {
				final XPathSelector selector = needsResolveXPath.load();
				selector.setContextItem(sourceNode);

				if (selector.effectiveBooleanValue()) {
					logger.debug("  resolving...");
					resolve(sourceNode, file);
				} else {
					logger.debug("  Contains no XSLT-conref -> skipped!");
				}
			} catch (SaxonApiException e) {
				throw new DITAOTException(e.getMessage(), e);
			}
		}

		return null;
	}
	

	protected void resolve(XdmNode sourceNode, File inputFile) throws DITAOTException {
		
		if (resolverExecutable == null) {
			// initialize at first call (avoid compilation when not required)
			final XsltCompiler 	compiler 		= resolverProcessor.newXsltCompiler();
			final URL 			resoveXslUrl 	= getClass().getResource(XSLT_FILE_URL);
			final SAXSource		xslSource		= new SAXSource(new InputSource(resoveXslUrl.toExternalForm())); 
			try {
				resolverExecutable = compiler.compile(xslSource);
			} catch (SaxonApiException e) {
				throw new DITAOTException("Failed to compile resolver stylesheet: " + e.getMessage(), e);
			}
		}
		
		final XPathSelector baseUriSelector = baseUriXPath.load();
		try {
			baseUriSelector.setContextItem(sourceNode);
			currentBaseUri = new URL(baseUriSelector.evaluateSingle().getStringValue());
		} catch (SaxonApiException e) {
			throw new DITAOTException("Failed to determine base URI of file '" + inputFile.getName() + "': " + e.getMessage(), e);
		} catch (MalformedURLException e) {
			throw new DITAOTException(e.getMessage(), e);
		}
		
		// transform into temporary file
		final XsltTransformer resolverTransformer = resolverExecutable.load();
		
		resolverTransformer.setInitialContextNode(sourceNode);
		
		final File 			outputFile = new File(inputFile.getAbsolutePath() + FILE_EXTENSION_TEMP);
		final Serializer 	serializer = resolverProcessor.newSerializer(outputFile);

		resolverTransformer.setDestination(serializer);
		
		try {
			resolverTransformer.transform();
		} catch (SaxonApiException e) {
			throw new DITAOTException("Failed to resolve file '" + inputFile + "': " + e.getMessage(), e);
		}
		
		currentBaseUri = null;

		// move/copy temporary file to input file
		try {
			deleteQuietly(inputFile);
			moveFile(outputFile, inputFile);
			//copyFile(outputFile, inputFile);
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			throw new DITAOTException("Failed to replace file '" + inputFile + "': " + e.getMessage(), e);
		}
	}

	public DocumentBuilder getDocumentBuilder() {
		return resolverDocBuilder;
	}
	
	public XMLReader getXsltConrefXmlReader() {
		return xsltConrefXmlReader;
	}

	public XPathCache getXPathCache() {
		return xPathCache;
	}


	public XslTransformerCache getXsltConrefTransformerCache() {
		return xsltConrefTransformerCache;
	}
	
	public URL getCurrentBaseUri() {
		return currentBaseUri;
	}


	public DITAOTLogger getOtLogger() {
		return logger;
	}

}
