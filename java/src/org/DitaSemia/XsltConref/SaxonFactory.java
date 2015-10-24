/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.XsltConref;

import net.sf.saxon.style.StyleElement;

import com.saxonica.xsltextn.ExtensionElementFactory;

public class SaxonFactory implements ExtensionElementFactory {
	
	@Override
	public Class<? extends StyleElement> getExtensionClass(String localname) {
		if 		(localname.equals("resolve")) 		return SaxonXsltConrefResolver.class;
		else return null;
	}

}
