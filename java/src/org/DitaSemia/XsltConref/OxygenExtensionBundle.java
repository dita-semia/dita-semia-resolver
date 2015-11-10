/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.XsltConref;

import org.DitaSemia.JavaBase.AuthorNodeWrapper;

import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.dita.DITAExtensionsBundle;

public class OxygenExtensionBundle extends DITAExtensionsBundle {

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
	
}
