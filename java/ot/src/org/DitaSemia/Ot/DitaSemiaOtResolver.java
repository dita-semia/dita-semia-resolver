package org.DitaSemia.Ot;

import static org.apache.commons.io.FileUtils.*;
import static org.dita.dost.util.Constants.FEATURE_NAMESPACE;
import static org.dita.dost.util.Constants.FEATURE_VALIDATION;
import static org.dita.dost.util.Constants.FEATURE_VALIDATION_SCHEMA;
import static org.dita.dost.util.URLUtils.*;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.Base.ConbatResolver;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.Log4jErrorListener;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonDocumentBuilder;
import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.XslTransformerCacheProvider;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetDisplaySuffixDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetIsDontLinkDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetIsFilteredKeyDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetIsKeyHiddenDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetIsOverwritableDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetIsResourceOnlyDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyDefByRefStringDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyFilterAttrDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetKeyTypeDefDef;
import org.DitaSemia.Base.AdvancedKeyref.ExtensionFunctions.GetLocationDef;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Base.DocumentCaching.BookCacheProvider;
import org.DitaSemia.Base.ExtensionFunctions.ResolveEmbeddedXPathDef;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Base.XsltConref.XsltConrefCache;
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
import net.sf.saxon.lib.Initializer;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.StringValue;

@SuppressWarnings("deprecation")
public class DitaSemiaOtResolver extends AbstractPipelineModuleImpl implements BookCacheProvider, XslTransformerCacheProvider {

	public static final String	TRUE	= "true";
	
	protected static final String FILE_EXTENSION_TEMP	= ".dita-semia.temp";
	
	public static final String ANT_INVOKER_PARAM_XSL 					= "xsl";
	public static final String ANT_INVOKER_PARAM_KEY_TYPE_DEF_LIST_URI	= "key-type-def-list-uri";
	public static final String ANT_INVOKER_PARAM_OUTSOURCE_SVG			= "outsource-svg";
	public static final String ANT_INVOKER_PARAM_WRAP_CBA_PH			= "wrap-cba-ph";
	public static final String ANT_INVOKER_PARAM_BASEDIR				= "basedir";
	public static final String ANT_INVOKER_PARAM_DXD_INDENT				= "dxd-indent";
	public static final String ANT_INVOKER_PARAM_DXD_MAX_WIDTH			= "dxd-max-width";
	public static final String ANT_INVOKER_PARAM_DXD_MARKUP				= "dxd-markup";
	
	public static final String ANT_INVOKER_PARAM_OT_URL					= "ditadir";
	public static final String ANT_INVOKER_PARAM_LANGUAGE				= "language";

	//protected static final String XSLT_FILE_URL 		= "/xsl/resolve.xsl";
	//protected static final String XSLT_FILE_URL 		= "plugin:org.dita-semia.resolver:java/ot/src/xsl/resolve.xsl";
	
	protected static final String ATTR_OT_FILE			= "xtrf";
	protected static final String ATTR_OT_POS			= "xtrc";
	
	protected static final String WARN_MARKER			= ": [DOTX][WARN]: ";

	protected static final String NEEDS_RESOLVER_XPATH 	= "exists(//@xcr:xsl | //@cba:* | //@akr:ref | //@ikd:key-type)";
	protected static final String BASE_URI_XPATH 		= "/*/@xtrf";


	protected Processor 			resolverProcessor			= null;
	protected XsltExecutable 		resolverExecutable			= null;
	protected DocumentBuilder 		resolverDocBuilder			= null;
	protected XPathExecutable 		needsResolveXPath			= null;
	protected XPathExecutable 		baseUriXPath				= null;
	
	protected XMLReader				xsltConrefXmlReader			= null;
	protected BookCache				bookCache					= null;
	protected boolean				bookCacheInitialized		= false;
	protected XsltConrefCache		xsltConrefCache				= null;
	
	protected URL					currentBaseUrl				= null;
	
	protected URI					outDirUri					= null;
	protected URI					tempDirUri					= null;
	protected String				basedir						= null;
	protected boolean				outsourceSvg				= false;
	protected boolean				wrapCbaPh					= true;
	protected String				dxdIndent					= "  ";
	protected int					dxdMaxWidth					= 80;
	protected boolean				dxdMarkup					= false;
	
	protected List<URI>				outsourcedSvgList			= new ArrayList<>();
	

	protected void init(AbstractPipelineInput input) throws DITAOTException {

		//logger.info("job.getOutputDir(): " + job.getOutputDir());
		outDirUri		= job.getOutputDir().toURI();
		tempDirUri		= job.tempDir.toURI();
		basedir			= FileUtil.encodeUrl(input.getAttribute(ANT_INVOKER_PARAM_BASEDIR));
		outsourceSvg 	= (TRUE.equals(input.getAttribute(ANT_INVOKER_PARAM_OUTSOURCE_SVG)));
		wrapCbaPh 		= (TRUE.equals(input.getAttribute(ANT_INVOKER_PARAM_WRAP_CBA_PH)));
		
		System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
		
		// xslt-conref
		//final Configuration xsltConrefConfiguration = XsltConref.createConfiguration(this);
		//xsltConrefConfiguration.;
		
		SaxonDocumentBuilder documentBuilder = new SaxonDocumentBuilder(CatalogUtils.getCatalogResolver(), CatalogUtils.getCatalogResolver());
		
		final Initializer configurationInitializer = new Initializer() {
			@Override
			public void initialize(Configuration configuration) {
				configuration.setURIResolver(CatalogUtils.getCatalogResolver());
			}};
		xsltConrefCache = new XsltConrefCache(this, configurationInitializer, documentBuilder, null, basedir);
		
		try {
			xsltConrefXmlReader	= XMLUtils.getXMLReader();
			xsltConrefXmlReader.setFeature(FEATURE_VALIDATION, 			true);
			xsltConrefXmlReader.setFeature(FEATURE_VALIDATION_SCHEMA, 	true);
			xsltConrefXmlReader.setFeature(FEATURE_NAMESPACE, 			true);
		} catch (SAXException e) {
			throw new DITAOTException(e.getMessage(), e);
		}
		
		xsltConrefXmlReader.setEntityResolver(CatalogUtils.getCatalogResolver());
		
		// general resolver
		final Configuration resolverConfiguration = XsltConrefCache.createConfiguration(
				XsltConrefCache.class.getResource(XsltConrefCache.CONFIG_FILE_URL), 
				this, 
				this,
				documentBuilder);
		resolverConfiguration.setErrorListener(new Log4jErrorListener(Logger.getLogger("OT-Resolver")));

		// ensure that both configurations are compatible
		final Configuration xsltConrefConfiguration = xsltConrefCache.getConfiguration();
		resolverConfiguration.setNamePool(xsltConrefConfiguration.getNamePool());
		resolverConfiguration.setDocumentNumberAllocator(xsltConrefConfiguration.getDocumentNumberAllocator());

		resolverConfiguration.setURIResolver(CatalogUtils.getCatalogResolver());

		// dita-semia
		resolverConfiguration.registerExtensionFunction(new ResolveEmbeddedXPathDef());
		resolverConfiguration.registerExtensionFunction(new GetOutsourcedSvgUriDef(this));
		resolverConfiguration.registerExtensionFunction(new IsUrlInBookCacheDef(bookCache));
		resolverConfiguration.registerExtensionFunction(new GetFixedBaseUriDef(this));
		
		// advanced-keyref
		resolverConfiguration.registerExtensionFunction(new GetKeyDefByRefStringDef(this));
		resolverConfiguration.registerExtensionFunction(new GetKeyTypeDefDef(this));
		resolverConfiguration.registerExtensionFunction(new GetDisplaySuffixDef(this));
		
		// implicit-keydef
		resolverConfiguration.registerExtensionFunction(new GetLocationDef());
		resolverConfiguration.registerExtensionFunction(new GetIsFilteredKeyDef());
		resolverConfiguration.registerExtensionFunction(new GetIsKeyHiddenDef());
		resolverConfiguration.registerExtensionFunction(new GetIsDontLinkDef());
		resolverConfiguration.registerExtensionFunction(new GetIsOverwritableDef());
		resolverConfiguration.registerExtensionFunction(new GetIsResourceOnlyDef());
		resolverConfiguration.registerExtensionFunction(new GetKeyFilterAttrDef());

		// xslt-conref
		resolverConfiguration.registerExtensionFunction(new ResolveXsltConrefDef(this));
		

		resolverProcessor 		= new Processor(resolverConfiguration);
		resolverDocBuilder 		= resolverProcessor.newDocumentBuilder();
		
		final XPathCompiler xPathCompiler 	= resolverProcessor.newXPathCompiler();
		xPathCompiler.declareNamespace(XsltConref.NAMESPACE_PREFIX, 		XsltConref.NAMESPACE_URI);
		xPathCompiler.declareNamespace(ConbatResolver.NAMESPACE_PREFIX, 	ConbatResolver.NAMESPACE_URI);
		xPathCompiler.declareNamespace(KeyRef.NAMESPACE_PREFIX, 			KeyRef.NAMESPACE_URI);
		xPathCompiler.declareNamespace(KeyDef.NAMESPACE_PREFIX, 			KeyDef.NAMESPACE_URI);
		try {
			needsResolveXPath	= xPathCompiler.compile(NEEDS_RESOLVER_XPATH);
			baseUriXPath		= xPathCompiler.compile(BASE_URI_XPATH);
		} catch (SaxonApiException e) {
			throw new DITAOTException(e.getMessage(), e);
		}
		
		final Configuration extractConfiguration = new Configuration();
		SaxonDocumentBuilder.makeConfigurationCompatible(extractConfiguration);
		extractConfiguration.setURIResolver(CatalogUtils.getCatalogResolver());
		BookCache.registerExtractTextExtensionFunctions(extractConfiguration, this);
		final XslTransformerCache	extractTransformerCache = new XslTransformerCache(extractConfiguration);
		
		try {
			
			final String dxdIndentParam = input.getAttribute(ANT_INVOKER_PARAM_DXD_INDENT);
			if (dxdIndentParam != null) {
				dxdIndent = dxdIndentParam;
			}

			final String dxdMaxWidthParam = input.getAttribute(ANT_INVOKER_PARAM_DXD_MAX_WIDTH);
			if (dxdMaxWidthParam != null) {
				dxdMaxWidth = Integer.parseInt(dxdMaxWidthParam);
			}

			final String dxdMarkupParam = input.getAttribute(ANT_INVOKER_PARAM_DXD_MARKUP);
			if (dxdMarkupParam != null) {
				dxdMarkup = TRUE.equals(dxdMarkupParam);
			}
			
			final String	inputUrl			= job.getInputMap().toString();
			final URL		rootUrl				= new URL(tempDirUri.toURL(), inputUrl);
			final URL		ditaOtUrl			= new File(input.getAttribute(ANT_INVOKER_PARAM_OT_URL)).toURI().toURL();
			final String	keyTypeDefListParam	= input.getAttribute(ANT_INVOKER_PARAM_KEY_TYPE_DEF_LIST_URI);
			final URL		keyTypeDefListUrl	= (keyTypeDefListParam == null) ? null : new File(keyTypeDefListParam).toURI().toURL();
			final String	language			= input.getAttribute(ANT_INVOKER_PARAM_LANGUAGE);
			
			
			//logger.info("ditaOtUrl: " + ditaOtUrl);
			//logger.info("Build bookCache for file: " + rootUrl);
			//logger.info("rootUrl: " + rootUrl);
			bookCache = new BookCache(rootUrl, configurationInitializer, xsltConrefCache, documentBuilder, extractTransformerCache, false, true, ditaOtUrl, keyTypeDefListUrl, null, language);
			
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
				return (fi.format.equals("dita") || fi.format.equals("ditamap"));
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
		
		checkKeyRefExpected();

		if ((outsourceSvg) && (!outsourcedSvgList.isEmpty())) {
			// add outsourced images to image.list file
			try {
				final URI 				imageListUri 	= tempDirUri.resolve("image.list");
				//logger.info("image.list: " + imageListUri.toURL().getFile());
				final FileWriter 		fileWriter 		= new FileWriter(imageListUri.toURL().getFile(), true);
				final PrintWriter 		out 			= new PrintWriter(new BufferedWriter(fileWriter));
				for (URI uri : outsourcedSvgList) {
					//logger.info(" - " + uri);
					out.print("\n" + uri);
				}
				out.flush();
				fileWriter.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
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
				throw new DITAOTException("Failed to read resolver stylesheet: " + e.getMessage());
			} catch (SaxonApiException e) {
				throw new DITAOTException("Failed to compile resolver stylesheet: " + e.getMessage());
			}
		}
		
		final XPathSelector baseUriSelector = baseUriXPath.load();
		try {
			baseUriSelector.setContextItem(sourceNode);
			currentBaseUrl = new URL(fixTempUrl(baseUriSelector.evaluateSingle().getStringValue()));
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

		resolverTransformer.setParameter(new QName(ANT_INVOKER_PARAM_BASEDIR), 			XdmValue.wrap(new StringValue(basedir)));
		resolverTransformer.setParameter(new QName(ANT_INVOKER_PARAM_OUTSOURCE_SVG), 	XdmValue.wrap(BooleanValue.get(outsourceSvg)));
		resolverTransformer.setParameter(new QName(ANT_INVOKER_PARAM_WRAP_CBA_PH), 		XdmValue.wrap(BooleanValue.get(wrapCbaPh)));
		resolverTransformer.setParameter(new QName(ANT_INVOKER_PARAM_DXD_MAX_WIDTH), 	XdmValue.wrap(new Int64Value(dxdMaxWidth)));
		resolverTransformer.setParameter(new QName(ANT_INVOKER_PARAM_DXD_INDENT), 		XdmValue.wrap(new StringValue(dxdIndent)));
		resolverTransformer.setParameter(new QName(ANT_INVOKER_PARAM_DXD_MARKUP), 		XdmValue.wrap(BooleanValue.get(dxdMarkup)));
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
	
	public void checkKeyRefExpected() {
		final Collection<KeyDefInterface> 	keyDefs 				= bookCache.getKeyDefs();
		final Set<String>					referencedKeyDefRefList	= bookCache.getReferencedKeyDefRefList();
		//logger.info("referencedKeyDefRefList size: " + referencedKeyDefRefList.size());
		for (KeyDefInterface keyDef : keyDefs) {
			if ((keyDef.isRefExpected()) && (!referencedKeyDefRefList.contains(keyDef.getRefString()))) {
				final NodeWrapper 	node 	= bookCache.getNodeByLocation(keyDef.getDefLocation());
				final String location = getLocation(node);
				logger.warn(location + WARN_MARKER + " No reference to implicit key-def '" + keyDef.getRefString() + "'.");
			}
		}
	}
	
	public String getLocation(NodeWrapper node) {
		if (node == null) {
			return "";
		} else {
			final String file 	= node.getAttribute(ATTR_OT_FILE, null);
			final String pos 	= node.getAttribute(ATTR_OT_POS, null);
			
			if ((file == null) || (file.isEmpty()) || (pos == null) || (pos.isEmpty())) {
				return getLocation(node.getParent());
			} else {
				return fixTempUrl(file) + ":" + pos.replaceFirst("^.*;", "");
			}
		}
	}
	
	public String fixTempUrl(String url) {
		
		return url;
	}

	public DocumentBuilder getDocumentBuilder() {
		return resolverDocBuilder;
	}
	
	public XMLReader getXsltConrefXmlReader() {
		return xsltConrefXmlReader;
	}
	
	public URL getCurrentBaseUrl() {
		return currentBaseUrl;
	}


	public DITAOTLogger getOtLogger() {
		return logger;
	}
	
	public BookCache getBookCache() {
		if (!bookCacheInitialized) {
			bookCacheInitialized = true;
			bookCache.fillCache(null);			
		}
		return bookCache;
	}

	@Override
	public BookCache getBookCache(URL url) {
		return getBookCache();
	}


	@Override
	public URL getBookCacheRootUrl(URL url) {
		final String inputFileName 	= job.getInputFile().toString().replace(job.getInputDir().toString(), "");
		final String inputPath		= FileUtil.decodeUrl(basedir) + "/" + inputFileName;
		try {
			return new File(inputPath).toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public XslTransformerCache getXslTransformerCache() {
		return getBookCache().getXsltConrefCache().getTransformerCache();
	}


	public XPathCache getXPathCache() {
		return xsltConrefCache.getXPathCache();
	}


	public XsltConrefCache getXsltConrefCache() {
		return xsltConrefCache;
	}

	public URI getOutsourcedSvgUri(String filename) {
		try {
			final URI absUri	= new URI(filename);
			final URI relUri	= tempDirUri.relativize(absUri);
			final URI outUri	= outDirUri.resolve(relUri);
			/*logger.info("absUri: " + absUri);
			logger.info("relUri: " + relUri);
			logger.info("outUri: " + outUri);*/
			outsourcedSvgList.add(relUri);
			return outUri;
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
			return null;
		} 
	}


}
