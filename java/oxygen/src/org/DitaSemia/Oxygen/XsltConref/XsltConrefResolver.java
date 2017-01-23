/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen.XsltConref;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.XsltConref.TempContextException;
import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Base.XsltConref.XsltConref.Parameter;
import org.DitaSemia.Base.XsltConref.XsltConrefCache;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import ro.sync.ecss.dita.DITAAccess;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.ReferenceResolverException;
import ro.sync.ecss.extensions.api.ValidatingReferenceResolverException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class XsltConrefResolver {
	
	private static final Logger logger = Logger.getLogger(XsltConrefResolver.class.getName());


	private static XsltConrefResolver instance			= null;

	protected final XsltConrefCache 	xsltConrefCache;
	protected final List<Parameter> 	frameworkParameters	= new LinkedList<>();
	

	public static final String	NAME_TEMP_CONTEXT 	= "temp-context";


	public static XsltConrefResolver getInstance() {
		if (instance == null) {
			instance = new XsltConrefResolver();
		}
		return instance;
	}
	
	public XsltConrefResolver() {
		xsltConrefCache = BookCacheHandler.getInstance().getXsltConrefCache();
	}

	public void addFrameworkParameter(QName name, Sequence value) {
		frameworkParameters.add(new Parameter(name, value));
	}
	
	public XsltConref xsltConrefFromNode(AuthorNode node, AuthorAccess authorAccess) {
		final XsltConref xsltConref = XsltConref.fromNode(new AuthorNodeWrapper(node, authorAccess), xsltConrefCache);
		return xsltConref;
	}

	public static boolean isXsltConrefAttr(AuthorNode node, String attributeName) {
		final String customParameterPrefix = node.getNamespaceContext().getPrefixForNamespace(XsltConref.NAMESPACE_PARAMETER_URI);
		return (attributeName.equals(XsltConref.ATTR_URI)) ||
				(attributeName.equals(XsltConref.ATTR_XML_SOURCE_URI)) ||
				(attributeName.equals(XsltConref.ATTR_START_TEMPLATE)) ||
				(attributeName.startsWith(customParameterPrefix + ":"));
	}

	
	public static void checkXsltConrefTarget(AuthorNode node, AuthorDocument targetDocument) throws ValidatingReferenceResolverException {
		
		final String rootName = targetDocument.getRootElement().getDisplayName(); 
		if ((rootName.equals(XsltConref.NAME_NO_CONTENT)) || (rootName.equals(NAME_TEMP_CONTEXT))) {
			// <no-content> and <temp-context> are always valid as result element
			return;
		}
		
		String errorMessage = null;
	    
	    final AuthorElement targetElement = targetDocument.getRootElement();
	    if (targetElement != null) {
	        final AttrValue targetClass = targetElement.getAttribute("class");
	        if (targetClass == null) {
	        	errorMessage = "The target does not have a class attribute";
	        } else {
	        	if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
	        		final AttrValue sourceClass = ((AuthorElement)node).getAttribute("class");
	        		if (sourceClass != null) {
	                    final boolean isGeneralization = DITAAccess.isGeneralizationOf(targetClass.getValue(), sourceClass.getValue());
	                    if (!isGeneralization) {
	                    	errorMessage = 
                    			"The target element '" + targetElement.getName() +"' " +
                    			"with class value '" + targetClass.getValue() +"' " +
                    			"is not a generalization of source element '" + node.getName() +"' " + 
                    			"with class value '" + sourceClass.getValue() + "'";
	                    }
	        		} else {
	        			errorMessage = "The source does not have a class attribute";
	        		}
	        	} else {
	        		errorMessage = "The XSLT-Conref source is not an element";
	        	}
	        }
	    }
	    if (errorMessage != null) {
	      throw new ValidatingReferenceResolverException("XSLT-Conref was not expanded:\n" + errorMessage);
	    }
    }
	
	public SAXSource resolveXsltConref(XsltConref xsltConref, AuthorAccess authorAccess) throws ReferenceResolverException {
		String resolvedString;
		try {
			resolvedString = xsltConref.resolveToString(frameworkParameters);
			//logger.info("resolvedString: " + resolvedString);
		} catch (XPathException e) {
			logger.error(e.getMessage(), e);
			throw new ReferenceResolverException(e.getMessage(), true, true);
		} catch (TempContextException e) {
			resolvedString = "<" + NAME_TEMP_CONTEXT + "/>";
		}
		final XMLReader xmlReader = authorAccess.getXMLUtilAccess().newNonValidatingXMLReader();
		final SAXSource	saxSource = new SAXSource(xmlReader, new InputSource(new StringReader(resolvedString)));
		
		return saxSource;
	}

	public XslTransformerCache getTransformerCache() {
		return xsltConrefCache.getTransformerCache();
	}

}
