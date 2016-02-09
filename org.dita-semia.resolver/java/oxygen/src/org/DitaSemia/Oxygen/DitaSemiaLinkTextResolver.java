package org.DitaSemia.Oxygen;

import org.DitaSemia.Oxygen.Conbat.ConbatContentResolver;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.link.InvalidLinkException;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.dita.link.DitaLinkTextResolver;

public class DitaSemiaLinkTextResolver extends DitaLinkTextResolver {

	AuthorAccess authorAccess = null;
	
	@Override
	public void activated(AuthorAccess authorAccess) {
		this.authorAccess = authorAccess;
		super.activated(authorAccess);
	}

	@Override
	public String resolveReference(AuthorNode node) throws InvalidLinkException {
		String resolved = ConbatContentResolver.resolveContent(node, authorAccess);
		if (resolved == null) {
			resolved = super.resolveReference(node);
		}
		return resolved;
	}
}
