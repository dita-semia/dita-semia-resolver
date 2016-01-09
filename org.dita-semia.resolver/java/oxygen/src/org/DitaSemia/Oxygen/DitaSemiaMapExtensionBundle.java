/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Oxygen;

import org.DitaSemia.Base.XsltConref.XsltConref;
import org.DitaSemia.Oxygen.XsltConref.DitaSemiaMapReferenceResolver;

import ro.sync.ecss.extensions.api.AuthorReferenceResolver;
import ro.sync.ecss.extensions.api.ExtensionsBundle;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class DitaSemiaMapExtensionBundle extends ExtensionsBundle {

	@Override
	public boolean isContentReference(AuthorNode node) {
		boolean isContentReference = false;
		if (XsltConref.isXsltConref(new AuthorNodeWrapper(node, null))) {
			isContentReference = true;
		} else {
			isContentReference = super.isContentReference(node);
		}
		return isContentReference;
	}
	
	@Override
	public AuthorReferenceResolver createAuthorReferenceResolver() {
		return new DitaSemiaMapReferenceResolver();
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentTypeID() {
		// TODO Auto-generated method stub
		return null;
	}

}
