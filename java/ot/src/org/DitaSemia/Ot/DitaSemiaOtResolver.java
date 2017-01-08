package org.DitaSemia.Ot;

import static org.apache.commons.io.FileUtils.*;
import static org.dita.dost.util.URLUtils.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.Base.BookCache;
import org.DitaSemia.Base.BookCacheInitializer;
import org.DitaSemia.Base.BookCacheProvider;
import org.DitaSemia.Base.Log4jErrorListener;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Ot.AdvancedKeyRef.GetKeyDefLocationDef;
import org.DitaSemia.Ot.AdvancedKeyRef.GetKeyRefDisplaySuffixDef;
import org.DitaSemia.Ot.AdvancedKeyRef.GetKeyTypeDefDef;
import org.DitaSemia.Ot.AdvancedKeyRef.GetReferencedKeyDefDef;
import org.DitaSemia.Ot.Conbat.ResolveEmbeddedXPathDef;
import org.DitaSemia.Ot.XsltConref.ResolveXsltConrefDef;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Job.FileInfo.Filter;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.util.Job.FileInfo;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Item;
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
import net.sf.saxon.value.ObjectValue;

public class DitaSemiaOtResolver extends AbstractPipelineModuleImpl implements BookCacheProvider, BookCacheInitializer {
	
	protected static final String FILE_EXTENSION_TEMP	= ".dita-semia.temp";
	
	public static final String ANT_INVOKER_PARAM_XSL 					= "xsl";
	public static final String ANT_INVOKER_PARAM_KEY_TYPE_DEF_LIST_URI	= "key-type-def-list-uri";
	public static final String ANT_INVOKER_PARAM_OT_URL					= "ditadir";
	public static final String ANT_INVOKER_PARAM_LANGUAGE				= "language";

	//protected static final String XSLT_FILE_URL 		= "/xsl/resolve.xsl";
	//protected static final String XSLT_FILE_URL 		= "plugin:org.dita-semia.resolver:java/ot/src/xsl/resolve.xsl";

	protected static final String NEEDS_RESOLVER_XPATH 	= "exists(//@xcr:xsl | //@cba:* | //@akr:ref | //@ikd:key-type)";
	protected static final String BASE_URI_XPATH 		= "/*/@xtrf";

	protected Processor 			resolverProcessor			= null;
	protected XsltExecutable 		resolverExecutable			= null;
	protected DocumentBuilder 		resolverDocBuilder			= null;
	protected XPathExecutable 		needsResolveXPath			= null;
	protected XPathExecutable 		baseUriXPath				= null;
	
	protected XslTransformerCache	xsltConrefTransformerCache	= null;
	protected XMLReader				xsltConrefXmlReader			= null;

	protected BookCache				bookCache					= null;
	protected XPathCache			xPathCache					= null;
	
	protected URL					currentBaseUrl				= null;
	
	protected File					keyTypeDefListFile			= null;

	protected void init(AbstractPipelineInput input) throws DITAOTException {

		// xslt-conref
		final Configuration xsltConrefConfiguration = XsltConref.createConfiguration(this);
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
		final Configuration resolverConfiguration = XsltConref.createConfiguration(this);
		resolverConfiguration.setErrorListener(new Log4jErrorListener(Logger.getLogger("OT-Resolver")));

		// ensure that both configurations are compatible
		resolverConfiguration.setNamePool(xsltConrefConfiguration.getNamePool());
		resolverConfiguration.setDocumentNumberAllocator(xsltConrefConfiguration.getDocumentNumberAllocator());

		resolverConfiguration.setURIResolver(CatalogUtils.getCatalogResolver());

		resolverConfiguration.registerExtensionFunction(new ResolveXsltConrefDef(this));
		resolverConfiguration.registerExtensionFunction(new ResolveEmbeddedXPathDef(this));
		resolverConfiguration.registerExtensionFunction(new GetReferencedKeyDefDef(this));
		resolverConfiguration.registerExtensionFunction(new GetKeyDefLocationDef(this));
		resolverConfiguration.registerExtensionFunction(new GetKeyTypeDefDef(this));
		resolverConfiguration.registerExtensionFunction(new GetKeyRefDisplaySuffixDef(this));

		resolverProcessor 		= new Processor(resolverConfiguration);
		resolverDocBuilder 		= resolverProcessor.newDocumentBuilder();
		
		final XPathCompiler xPathCompiler 	= resolverProcessor.newXPathCompiler();
		xPathCompiler.declareNamespace(XsltConref.NAMESPACE_PREFIX, 				XsltConref.NAMESPACE_URI);
		xPathCompiler.declareNamespace(ResolveEmbeddedXPathDef.NAMESPACE_PREFIX, 	ResolveEmbeddedXPathDef.NAMESPACE_URI);
		xPathCompiler.declareNamespace(KeyRef.NAMESPACE_PREFIX, 					KeyRef.NAMESPACE_URI);
		xPathCompiler.declareNamespace(KeyDef.NAMESPACE_PREFIX, 					KeyDef.NAMESPACE_URI);
		try {
			needsResolveXPath	= xPathCompiler.compile(NEEDS_RESOLVER_XPATH);
			baseUriXPath		= xPathCompiler.compile(BASE_URI_XPATH);
		} catch (SaxonApiException e) {
			throw new DITAOTException(e.getMessage(), e);
		}
		
		keyTypeDefListFile	= toFile(input.getAttribute(ANT_INVOKER_PARAM_KEY_TYPE_DEF_LIST_URI));
		
		try {
			final String	inputUrl	= job.getInputMap().toString();
			final URL		tempDirUrl	= job.tempDir.toURI().toURL();
			final URL		rootUrl		= new URL(tempDirUrl, inputUrl);
			final URL		ditaOtUrl	= new File(input.getAttribute(ANT_INVOKER_PARAM_OT_URL)).toURI().toURL();
			final String	language	= input.getAttribute(ANT_INVOKER_PARAM_LANGUAGE);
			//logger.info("ditaOtUrl: " + ditaOtUrl);
			//logger.info("Build bookCache for file: " + rootUrl);
			bookCache = new BookCache(rootUrl, this, resolverConfiguration, ditaOtUrl, language);
			bookCache.fillCache(null);
			//logger.info("  done! KeyDefs: " + documentCache.getKeyDefs().size());
		} catch (MalformedURLException e) {
			logger.error("Error initializing the OT-Resolver: " + e.getMessage(), e);
		}
	}
	

	@Override
	public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
		
		init(input);
		
		final Collection<FileInfo> fis = job.getFileInfo(new Filter<FileInfo>() {
			@Override
			public boolean accept(FileInfo fi) {
				return fi.format.equals("dita");
			}});

		for (final FileInfo fi: fis) {
			final File file = new File(job.tempDir, fi.file.getPath());

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
					logger.info("Processing " + file.getAbsolutePath());
					resolve(sourceNode, file, input);
				} else {
					logger.info("Skipping " + file.getAbsolutePath());
				}
			} catch (SaxonApiException e) {
				throw new DITAOTException(e.getMessage(), e);
			}
		}

		return null;
	}
	

	protected void resolve(XdmNode sourceNode, File inputFile, AbstractPipelineInput input) throws DITAOTException {
		
		if (resolverExecutable == null) {
			// initialize at first call (avoid compilation when not required)
			final XsltCompiler 	compiler 	= resolverProcessor.newXsltCompiler();
			final File			xslFile		= toFile(input.getAttribute(ANT_INVOKER_PARAM_XSL));
			try {
				final Source xslSource	= CatalogUtils.getCatalogResolver().resolve(xslFile.toURI().toString(), "");
				resolverExecutable = compiler.compile(xslSource);
			} catch (TransformerException e) {
				throw new DITAOTException("Failed to lead resolver stylesheet: " + e.getMessage());
			} catch (SaxonApiException e) {
				throw new DITAOTException("Failed to compile resolver stylesheet: " + e.getMessage());
			}
		}
		
		final XPathSelector baseUriSelector = baseUriXPath.load();
		try {
			baseUriSelector.setContextItem(sourceNode);
			currentBaseUrl = new URL(baseUriSelector.evaluateSingle().getStringValue());
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
		
		currentBaseUrl = null;

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
	
	public URL getCurrentBaseUrl() {
		return currentBaseUrl;
	}


	public DITAOTLogger getOtLogger() {
		return logger;
	}
	
	public BookCache getBookCache() {
		return bookCache;
	}

	public static KeyDefInterface getKeyDefFromItem(Item item) throws XPathException {
		if ((!(item instanceof ObjectValue<?>)) || (!(((ObjectValue<?>)item).getObject() instanceof KeyDefInterface))) {
			throw new XPathException("Supplied item  needs to be an instance of " + KeyDefInterface.class.getTypeName() + ".");
		}
		return (KeyDefInterface)(((ObjectValue<?>)item).getObject());
	}


	@Override
	public BookCache getBookCache(URL url) {
		return bookCache;
	}


	@Override
	public void initBookCache(BookCache bookCache) {
		if (keyTypeDefListFile != null) {
			try {
				bookCache.parseKeyTypeDefFile(keyTypeDefListFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

}
