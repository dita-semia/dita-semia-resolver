package org.DitaSemia.Oxygen.ExtensionBundle;

import org.DitaSemia.Oxygen.XRefLinkTextResolver;
import org.DitaSemia.Oxygen.AdvancedKeyRef.AdvancedKeyRefContentResolver;
import org.DitaSemia.Oxygen.Conbat.ConbatContentResolver;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.link.InvalidLinkException;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.dita.link.DitaLinkTextResolver;

public class DitaSemiaLinkTextResolver extends DitaLinkTextResolver {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DitaSemiaLinkTextResolver.class.getName());
	
	AuthorAccess authorAccess = null;
	
	@Override
	public void activated(AuthorAccess authorAccess) {
		this.authorAccess = authorAccess;
		super.activated(authorAccess);
	}

	@Override
	public String resolveReference(AuthorNode node) throws InvalidLinkException {
		String resolved = XRefLinkTextResolver.resolveReference(node, authorAccess);
		if (resolved == null) {
			resolved = AdvancedKeyRefContentResolver.resolveContent(node, authorAccess);
			if (resolved == null) {
				/*resolved = ConbatContentResolver.resolveContent(node, authorAccess);
				if (resolved == null) {*/
					resolved = super.resolveReference(node);
				/*}*/
			}
		}
		return resolved;
	}
}
