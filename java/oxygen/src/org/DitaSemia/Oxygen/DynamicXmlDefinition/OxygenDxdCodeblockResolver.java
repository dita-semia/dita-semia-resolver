package org.DitaSemia.Oxygen.DynamicXmlDefinition;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.DynamicXmlDefinition.DxdCodeblockResolver;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import net.sf.saxon.trans.XPathException;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.ReferenceResolverException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class OxygenDxdCodeblockResolver extends DxdCodeblockResolver {
	
	private static final Logger logger = Logger.getLogger(OxygenDxdCodeblockResolver.class.getName());

	private static OxygenDxdCodeblockResolver instance			= null;
	
	public static final String	ATTR_CODEREF	= "coderef";
	
	public static final String	INDENT		= "  ";
	public static final int		MAX_WIDTH	= 80;
	
	protected final URIResolver uriResolver; 


	public static OxygenDxdCodeblockResolver getInstance() {
		if (instance == null) {
			instance = new OxygenDxdCodeblockResolver();
		}
		return instance;
	}
	
	protected OxygenDxdCodeblockResolver() {
		super(
			BookCacheHandler.getInstance().getXsltConrefCache().getTransformerCache(),
			BookCacheHandler.getInstance().getXsltConrefCache().getXPathCache());
		
		uriResolver = xslTransformerCache.getConfiguration().getURIResolver();
	}

	public static boolean isDxdCodeblock(AuthorNode node) {
		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
			final AttrValue classAttr = ((AuthorElement)node).getAttribute(DitaUtil.ATTR_CLASS);
			return ((classAttr != null) && (classAttr.getValue().contains(CLASS_DXD_CODEBLOCK)));
		} else {
			return false;
		}
	}

	public String getDisplayName(AuthorNode dxdCodeblockNode) {
		final String coderef = getCoderef(dxdCodeblockNode);
		if (coderef != null) {
			return coderef;
		} else {
			return "dxd-codeblock";
		}
	}

	public String getReferenceSystemId(AuthorNode dxdCodeblockNode) {
		final String baseUri	= dxdCodeblockNode.getXMLBaseURL().toExternalForm();
		final String coderef 		= getCoderef(dxdCodeblockNode);
		if (coderef != null) {
			try {
				final Source source = uriResolver.resolve(coderef, baseUri);
				//logger.info("systemId: " + source.getSystemId());
				return source.getSystemId();
			} catch (TransformerException e) {
				logger.error(e, e);
				return null;
			}
		} else {
			return baseUri;
		}
	}

	public String getUniqueId(AuthorNode dxdCodeblockNode) {
		final String systemId = getReferenceSystemId(dxdCodeblockNode);
		return (systemId == null) ? dxdCodeblockNode.getXMLBaseURL().toExternalForm() : systemId;
	}

	public boolean isDxdCodeblockAttr(AuthorNode node, String attributeName) {
		return (attributeName.equals(ATTR_CODEREF));
	}

	public void checkTarget(AuthorNode node, AuthorDocument targetDocument) {
		/* nothing to check */
	}

	public SAXSource resolve(AuthorNode node, String systemID, AuthorAccess authorAccess, EntityResolver entityResolver) throws ReferenceResolverException {
		String resolvedString;
		try {
			resolvedString = resolve(node.getXMLBaseURL().toURI(), getCoderef(node), INDENT, MAX_WIDTH, true);
			//logger.info("resolvedString: " + resolvedString);
		} catch (XPathException | URISyntaxException e) {
			logger.error(e.getMessage(), e);
			throw new ReferenceResolverException(e.getMessage(), true, true);
		}
		final XMLReader xmlReader = authorAccess.getXMLUtilAccess().newNonValidatingXMLReader();
		final SAXSource	saxSource = new SAXSource(xmlReader, new InputSource(new StringReader(resolvedString)));
		
		return saxSource;
	}
	
	protected static String getCoderef(AuthorNode node) {
		final AttrValue codrefAttr = ((AuthorElement)node).getAttribute(ATTR_CODEREF);
		return (codrefAttr != null) ? codrefAttr.getValue() : null;
	}

	public void removeScripFromCache(AuthorNode dxdCodeblockNode) {
		String scriptXsl = XSL;
		try {
			final URIResolver	uriResolver = xslTransformerCache.getConfiguration().getURIResolver();
			final Source 		xslSource 	= uriResolver.resolve(scriptXsl, dxdCodeblockNode.getXMLBaseURL().toExternalForm());
			xslTransformerCache.removeFromCache(new URL(xslSource.getSystemId()));
		} catch (TransformerException | MalformedURLException e) {
			logger.error(e, e);
		}
	}

	public URIResolver getURIResolver() {
		return uriResolver;
	}

}
