/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Base.XsltConref;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.EmbeddedXPathResolver;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.apache.log4j.Logger;

public class XsltConref {
	
	private static final Logger logger = Logger.getLogger(XsltConref.class.getName());
	
	public static final String 	ATTR_URI 					= "xsl";
	public static final String 	ATTR_XML_SOURCE_URI			= "source";
	public static final String 	ATTR_SOURCE_TYPE			= "source-type";
	public static final String 	ATTR_START_TEMPLATE			= "start-template";
	public static final String 	ATTR_STAGE					= "stage";
	public static final String 	ATTR_FLAGS					= "flags";
	

	public static final String 	FLAG_REPARSE				= "reparse";
	public static final String 	FLAG_SINGLE_SOURCE			= "single-source";
	
	public static final int		STAGE_DISPLAY				= -1;
	public static final int		STAGE_IMMEDIATELY			= 0;
	public static final int		STAGE_DELAYED				= 1;
	
	public static final String	YES							= "yes";
	
	public static final String 	PARAM_CURRENT_NODE			= "current";
	public static final String 	PARAM_CURRENT_URI			= "current-uri";
	public static final String 	NAMESPACE_PARAMETER_URI		= "http://www.dita-semia.org/xslt-conref/custom-parameter";
	public static final String 	NAMESPACE_URI				= "http://www.dita-semia.org/xslt-conref";
	public static final String 	NAMESPACE_PARAMETER_PREFIX	= "xcp";
	public static final String 	NAMESPACE_PREFIX			= "xcr";

	public static final String 	NAME_NO_CONTENT				= "no-content";
	
	public static final String 	EMPTY_SOURCE_XML			= "<?xml version=\"1.0\"?><dummy/>";

	protected final NodeWrapper 			node;
	protected final XsltConrefCache			xsltConrefCache;
	protected 		URL 					baseUrl;				

	
	public static class Parameter {
		
		protected final QName 		name;
		protected final Sequence 	value;
		
		public Parameter(QName name, Sequence value) {
			this.name	= name;
			this.value	= value;
		}
	}

	public static XsltConref fromNode(NodeWrapper node, XsltConrefCache xsltConrefCache)	{
		if (isXsltConref(node)) {
			return new XsltConref(node, xsltConrefCache);
		}
		return null;
	}

	
	public static boolean isXsltConref(NodeWrapper node) {
		boolean isXsltConref = ((node != null) && 
								(node.isElement()) && 
								(node.getAttribute(XsltConref.ATTR_URI, NAMESPACE_URI) != null) &&
								(!node.getAttribute(XsltConref.ATTR_URI, NAMESPACE_URI).isEmpty()));
		return isXsltConref;
	}
	
	
	private XsltConref(NodeWrapper node, XsltConrefCache xsltConrefCache) {
		this.node 				= node;
		this.xsltConrefCache	= xsltConrefCache;
		this.baseUrl			= node.getBaseUrl();
		
		// set original base-url of file when processed by DITA-OT
		final String xtrfString = node.getAttribute("xtrf", null);
		if (xtrfString != null) {
			try {
				baseUrl = new URL(xtrfString);
			} catch (MalformedURLException e) {
				logger.error(e, e);
			}
		}
	}
	
	
	public void setBaseUrl(final URL baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public String resolveToString(List<Parameter> frameworkParameters) throws XPathException, TempContextException {
		//logger.info("resolveToString: " + getScriptName());
		final NodeInfo 	resolvedNode 	= resolve(frameworkParameters);
		final String resolvedString = SaxonNodeWrapper.serializeNode(resolvedNode);
		//logger.info(resolvedString);
		return resolvedString;
	}

	public NodeInfo resolveToNode(List<Parameter> frameworkParameters) throws XPathException, TempContextException {
		//logger.info("resolveToNode: " + getScriptName());
		final NodeInfo 	resolvedNode 	= resolve(frameworkParameters);
		NodeInfo 		resolvedElement = resolvedNode.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();
		//logger.info("needsReparse: " + XsltConref.needsReparse(resolvedElement));
		if (XsltConref.needsReparse(resolvedElement)) {
			final String 	serialized 		= SaxonNodeWrapper.serializeNode(resolvedNode);
			try {
				//logger.info("reparsing...");
				//logger.info(serialized);
				final XdmNode 	reparsedNode 	= xsltConrefCache.getDocumentBuilder().buildFromString(serialized, true);
				
				resolvedElement = reparsedNode.getUnderlyingNode().iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();
				//logger.info("---");
				//logger.info(SaxonNodeWrapper.serializeNode(reparsedElement));
			} catch (SaxonApiException e) {
				logger.info("------");
				logger.info("serialized node:");
				logger.info(serialized);
				logger.info("------");
				throw new XPathException("Failed to reparse resolved xslt-conref ('" + getScriptName() + "'): " + e.getMessage());
			}
		} 
		resolvedElement.setSystemId(node.getBaseUrl().toString());
		return resolvedElement;
	}
	
	protected NodeInfo resolve(List<Parameter> frameworkParameters) throws XPathException, TempContextException {
		//logger.info("resolve()");
		final URL 				scriptUrl 		= getScriptUrl();
		String 					sourceType		= node.getAttribute(ATTR_SOURCE_TYPE, null);
		if (sourceType == null) {
			sourceType = "";
		}
		final XsltExecutable 	xsltExecutable 	= xsltConrefCache.getTransformerCache().getExecutable(scriptUrl, xsltConrefCache.getUriResolver());
		final XsltTransformer 	xsltTransformer = xsltExecutable.load();
		//logger.info("xsltTransformer: " + xsltTransformer);

		
		if (getStartTemplate() != null) {
			try {
				xsltTransformer.setInitialTemplate(new QName(getStartTemplate()));
			} catch (SaxonApiException e) {
				//TODO
				throw new XPathException(e.getMessage());
			}
		} else if (sourceType.isEmpty() || sourceType.equals("xml")) {
			Source 	xmlSource 	= getXmlSource();
			if ((xmlSource == null) && (node instanceof SaxonNodeWrapper)) {
				//logger.info("Source is root.");
				
				// use current root document as input
				final NodeInfo nodeInfo = ((SaxonNodeWrapper)node).getNodeInfo();
				xsltTransformer.setInitialContextNode(new XdmNode(nodeInfo.getRoot()));
				
				// set "xcr:current"
				xsltTransformer.setParameter(new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_NODE), new XdmNode(nodeInfo));
				
			} else {
				boolean xmlSourceIsBaseUrl	= false;
				if (xmlSource == null) {
					//logger.info("Source is base uri.");
					
					// 	use current base uri as input
					final String baseUrl = node.getBaseUrl().toExternalForm();
					try {
						xmlSource 			= xsltConrefCache.getUriResolver().resolve(baseUrl, "");;
						xmlSourceIsBaseUrl 	= true;
					} catch (TransformerException e) {
						throw new XPathException("Error resolving the source URL: '" + FileUtil.decodeUrl(baseUrl) + "': " + e.getMessage());
					}
				} else if ((xmlSource.getSystemId() != null) && (!FileUtil.fileExists(xmlSource.getSystemId()))) {
					// dedicated error message for this scenario
					throw new XPathException("Input source could not be found. (URL: '" + FileUtil.decodeUrl(xmlSource.getSystemId()) + "')");
				}
				
				// remove the provided xml reader to force saxon creating its own one using the configuration and, thus, expanding the attribute defaults
				if (xmlSource instanceof SAXSource) {
					((SAXSource)xmlSource).setXMLReader(null);
				}
				
				try {
					final Processor 		processor 	= new Processor(xsltConrefCache.getTransformerCache().getConfiguration());
					final DocumentBuilder 	builder 	= processor.newDocumentBuilder();
					final XdmNode 			context 	= builder.build(xmlSource);
					
					if (xmlSourceIsBaseUrl) {
						// set "xcr:current"
						final String 			xPathString	= createXPathToElement(node);
						final XPathSelector  	xPathSel	= xsltConrefCache.getXPathCache().getXPathSelector(xPathString, context);
						try {
							xPathSel.setContextItem(context);
							xsltTransformer.setParameter(
									new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_NODE), 
									xPathSel.evaluateSingle());
						} catch (SaxonApiException e) {
							throw new XPathException(e.getMessage(), e);
						}
					}
					
					xsltTransformer.setInitialContextNode(context);
				} catch (SaxonApiException e) {
					/* try to read input with standard configuration to print it */ 
					final Processor 		processor 	= new Processor(new Configuration());
					final DocumentBuilder 	builder 	= processor.newDocumentBuilder();
					try {
						final XdmNode node	= builder.build(xmlSource);
						logger.error("Input: " + SaxonNodeWrapper.serializeNode(node.getUnderlyingNode()));
					} catch (SaxonApiException e1) {
						// keep original message
					}
					Throwable[] suppressed =  e.getSuppressed();
					if (suppressed.length > 0) {
						logger.error("Suppresses exceptions:");
						for (int i = 0; i < suppressed.length; ++i) {
							logger.error(" - " + suppressed[i].getMessage());
						}
					}
					throw new XPathException("Error reading input source ('" + FileUtil.decodeUrl(xmlSource.getSystemId()) + "'): " + e.getMessage());
				}
			}
		} else if (sourceType.equals("text")) {
			//TODO: pass source as unparsed text
			if (!FileUtil.fileExists(node.getAttribute(ATTR_XML_SOURCE_URI, NAMESPACE_URI))) {
				// dedicated error message for this scenario
				throw new XPathException("Input source could not be found. (URL: '" + ATTR_XML_SOURCE_URI + "')");
			} else {
				//NodeInfo from File? XdmNode?
				// complete document as context node
				
			}
		} else {
			throw new XPathException("invalid value for source-type attribute ('" + sourceType + "')");
		}
		
		// set xcr:current-uri
		xsltTransformer.setParameter(
				new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_URI), 
				XdmValue.wrap(new AnyURIValue(baseUrl.toString())));

		setCustomParameters(xsltTransformer);
		
		// set framework parameters
		if (frameworkParameters != null) {
			for (Parameter parameter : frameworkParameters) {
				xsltTransformer.setParameter(parameter.name, XdmValue.wrap(parameter.value));	
			}
		}

		//logger.info("scriptUrl: " + scriptUrl);
		try {
			
			final XdmDestination destination = new XdmDestination();
			xsltTransformer.setDestination(destination);
			xsltTransformer.transform();
			final NodeInfo resolved = destination.getXdmNode().getUnderlyingNode();
			resolved.setSystemId(FileUtil.decodeUrl(node.getBaseUrl()));
			return resolved;
			
		} catch (SaxonApiException e) {
			throw new XPathException("Runtime Error. " + e.getMessage());
		} 
	}
	
	public URL getScriptUrl() throws XPathException {
		final URIResolver 	uriResolver 	= xsltConrefCache.getUriResolver();
		final String 		scriptString 	= node.getAttribute(ATTR_URI, NAMESPACE_URI);
		final String 		baseUrlString	= baseUrl.toExternalForm();
		//logger.info("getScriptUrl:");
		//logger.info("  uriResolver: " + uriResolver);
		//logger.info("  scriptString:" + scriptString);
		//logger.info("  baseUrlString: " + baseUrlString);
		try {
			return new URL(uriResolver.resolve(scriptString, baseUrlString).getSystemId());
		} catch (TransformerException | MalformedURLException e) {
			throw new XPathException("Failed to resolve script URI '" + scriptString + "' with base-url '" + baseUrlString + "': " + e.getMessage());
		}
	}
	
	public String getUniqueId() {
		URL scriptUrl = null;
		try {
			scriptUrl = getScriptUrl();
		} catch (XPathException e) {
			logger.error(e, e);
		}
		return (scriptUrl == null) ? node.getAttribute(ATTR_URI, NAMESPACE_URI) : scriptUrl.toExternalForm();
	}

	public String getScriptName() {
		return node.getAttribute(ATTR_URI, NAMESPACE_URI);
	}


	public String getScriptSystemId() {
		final URIResolver 	uriResolver 	= xsltConrefCache.getUriResolver();
		final String 		scriptString 	= node.getAttribute(ATTR_URI, NAMESPACE_URI);
		final String 		baseUrlString	= baseUrl.toExternalForm();
		try {
			return uriResolver.resolve(scriptString, baseUrlString).getSystemId();
		} catch (TransformerException e) {
			return null;
		}
	}
	
	public Source getXmlSource() {
		final String attrValue = node.getAttribute(ATTR_XML_SOURCE_URI, NAMESPACE_URI);
		if (attrValue == null) {
			return null;
		} else if (attrValue.isEmpty()) {
			return new StreamSource(new StringReader(EMPTY_SOURCE_XML));
		} else {
			final URIResolver uriResolver = xsltConrefCache.getUriResolver();
			try {
				return uriResolver.resolve(attrValue, baseUrl.toExternalForm());
			} catch (TransformerException e) {
				logger.error(e, e);
				return null;
			}
		}
	}
	
	public URL getXmlSourceUrl() {
		final Source source = getXmlSource();
		if (source != null) {
			try {
				return new URL(source.getSystemId());
			} catch (MalformedURLException e) {
				logger.error(e, e);
			}
		}
		return null;
	}

	public String getStartTemplate() {
		return node.getAttribute(ATTR_START_TEMPLATE, NAMESPACE_URI);
	}
	
	private static boolean needsReparse(final NodeInfo resolvedElement) {
		if (resolvedElement.getDisplayName() == NAME_NO_CONTENT) {
			return false;
		} else {
			final String classAttr = resolvedElement.getAttributeValue(null, DitaUtil.ATTR_CLASS);
			return ((classAttr == null) || (classAttr.isEmpty()));
		}
		//final String reparseString = resolvedElement.getAttributeValue(NAMESPACE_URI, ATTR_REPARSE);
		//logger.info("reparseString: " + reparseString);
		//return ((reparseString != null) && (reparseString.equals(YES)));
	}
	
	private void setCustomParameters(XsltTransformer xsltTransformer) throws XPathException {
		//logger.info("setCustomParameters()");
		final URL 				scriptUrl 		= getScriptUrl();
		final XsltExecutable 	xsltExecutable 	= xsltConrefCache.getTransformerCache().getExecutable(scriptUrl, xsltConrefCache.getUriResolver());
		final List<String> attrNameList = node.getAttributeNamesOfNamespace(NAMESPACE_PARAMETER_URI);
		for (String attrName : attrNameList) {
			//logger.info("attribute: " + attrName);
			final QName 	paramName 	= new QName(attrName.replaceAll("(^[^\\{\\}]*:)|(^\\{.*\\})", ""));
			final String 	paramValue	= node.getAttribute(attrName, NAMESPACE_PARAMETER_URI);
			//logger.info("set custom parameter: " + paramName + " = '" + paramValue + "'");
			if (paramValue != null) {
				if (xsltExecutable.getGlobalParameters().containsKey(paramName)) {
					try {
						xsltTransformer.setParameter(paramName, new XdmAtomicValue(EmbeddedXPathResolver.resolve(paramValue, node), ItemType.UNTYPED_ATOMIC));
						//logger.info("parameters set. ");
					} catch (SaxonApiException e) {
						logger.error(e, e);
					}
				} else {
					//logger.error("Parameter '" + paramName + "' not defined in script.");
				}
			}
		}
	}
	
	private static String createXPathToElement(NodeWrapper node) throws TempContextException {
		final URL baseUrl = node.getBaseUrl();
		String createXPathToElement = "";
		
		while ((node != null) && (node.getParent() != null) && (node.getParent().getBaseUrl().equals(baseUrl))) {
			final int index = node.getChildElementIndexWithinParent();
			if (index <= 0) {
				throw new TempContextException();
			}
			createXPathToElement = "/*[" + index + "]" + createXPathToElement;
			node = node.getParent();
		}
		
		//logger.info("createXPathToElement: result = " + createXPathToElement);
		return createXPathToElement;
	}


	public int getStage() {
		final String stageString = node.getAttribute(ATTR_STAGE, NAMESPACE_URI);
		if ((stageString != null) && (!stageString.isEmpty())) {
			try {
				return Integer.parseInt(stageString);
			} catch (NumberFormatException e) {
				// no special handling
			}
		}
		return STAGE_DISPLAY;
	}


	public boolean isSingleSource() {
		final String flags = node.getAttribute(ATTR_FLAGS, NAMESPACE_URI);
		if (flags != null) {
			return flags.contains(FLAG_SINGLE_SOURCE);
		} else {
			return false;
		}
	}


	public String getSourceSystemId() {
		final Source source = getXmlSource();
		if (source != null) {
			return source.getSystemId();
		} else {
			return null;
		}
	}

}
