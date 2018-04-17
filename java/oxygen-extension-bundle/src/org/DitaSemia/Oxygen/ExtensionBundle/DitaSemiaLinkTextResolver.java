package org.DitaSemia.Oxygen.ExtensionBundle;

import org.DitaSemia.Oxygen.XRefLinkTextResolver;
import org.DitaSemia.Oxygen.AdvancedKeyRef.AdvancedKeyRefContentResolver;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.link.InvalidLinkException;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.dita.link.DitaLinkTextResolver;

public class DitaSemiaLinkTextResolver extends DitaLinkTextResolver {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DitaSemiaLinkTextResolver.class.getName());
	
	private boolean active = true;
	
	AuthorAccess authorAccess = null;
	
	@Override
	public void activated(AuthorAccess authorAccess) {
		this.authorAccess = authorAccess;
		super.activated(authorAccess);
	}

	@Override
	public String resolveReference(AuthorNode node) throws InvalidLinkException {
		if (active) {
			String resolved = XRefLinkTextResolver.resolveReference(node, authorAccess);
			if (resolved == null) {
				resolved = AdvancedKeyRefContentResolver.resolveContent(node, authorAccess);
				if (resolved == null) {
					resolved = super.resolveReference(node);
				}
			}
			return resolved;
		} else {
			return "";
		}		
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
}
