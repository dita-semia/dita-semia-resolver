package org.DitaSemia.Base.DynamicXmlDefinition;

import java.io.StringWriter;
import java.net.URI;

import org.DitaSemia.Base.XPathCache;
import org.DitaSemia.Base.XslTransformerCache;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.StringValue;

public class DxdCodeblockResolver {
	

	protected static final String	XSL		= "plugin:org.dita-semia.resolver:xsl/dxd/resolve-codeblock.xsl";
	
	public static final QName INIT_TEMPLATE		= new QName("resolve-codeblock");
	
	public static final QName PARAM_BASE_URI	= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "baseUri");
	public static final QName PARAM_CODEREF		= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "coderef");
	public static final QName PARAM_INDENT		= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "indent");
	public static final QName PARAM_MAX_WIDTH	= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "maxWidth");
	public static final QName PARAM_MARKUP		= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "markup");

	public static final QName PARAM_TYPE_NAME 	= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "typeName");
	public static final QName PARAM_DEF_URL		= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "defUrl");
	
	public static final String CLASS_DXD_CODEBLOCK	= " ds-d/dxd-codeblock ";

	protected final XslTransformerCache xslTransformerCache;
	protected final XPathCache 			xPathCache;
	
	public DxdCodeblockResolver(XslTransformerCache xslTransformerCache, XPathCache xPathCache) {
		this.xslTransformerCache 	= xslTransformerCache;
		this.xPathCache				= xPathCache;
	}

	public String resolve(URI baseUri, String coderef, String indent, int maxWidth, boolean markup) throws XPathException {
		final XsltExecutable 	executable 		= xslTransformerCache.getExecutable(XSL);
		final XsltTransformer 	xslTransformer 	= executable.load();

		try {
			xslTransformer.setInitialTemplate(INIT_TEMPLATE);
			
			final Processor		processor	= new Processor(xslTransformerCache.getConfiguration());
			final StringWriter 	writer 		= new StringWriter();
			final Serializer 	serializer	= processor.newSerializer(writer);
			
			xslTransformer.setDestination(serializer);
	
			xslTransformer.setParameter(PARAM_BASE_URI, 	XdmValue.wrap(new AnyURIValue(baseUri.toString())));
			xslTransformer.setParameter(PARAM_CODEREF, 		XdmValue.wrap(new StringValue(coderef)));
			xslTransformer.setParameter(PARAM_INDENT, 		XdmValue.wrap(new StringValue(indent)));
			xslTransformer.setParameter(PARAM_MAX_WIDTH, 	XdmValue.wrap(new Int64Value(maxWidth)));
			xslTransformer.setParameter(PARAM_MARKUP, 		XdmValue.wrap((markup ? BooleanValue.TRUE : BooleanValue.FALSE)));
			
			xslTransformer.transform();
			
			writer.flush();
			
			return writer.toString();
		} catch (SaxonApiException e) {
			throw new XPathException(e.getMessage(), e);
		}
	}


	public XslTransformerCache getTransformerCache() {
		return xslTransformerCache;
	}


	public XPathCache getXPathCache() {
		return xPathCache;
	}
}
