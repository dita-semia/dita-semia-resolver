package org.DitaSemia.Oxygen;

import org.DitaSemia.Oxygen.Conbat.ConbatStylesFilter;

import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class DitaSemiaStylesFilter implements StylesFilter {

	@Override
	public String getDescription() {
		return "DITA-Semia styles filter";
	}

	@Override
	public Styles filter(Styles styles, AuthorNode authorNode) {
		ConbatStylesFilter.filter(styles, authorNode);
		return styles;
	}

}
