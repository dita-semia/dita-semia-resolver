package org.DitaSemia.Oxygen.ExtensionBundle;

import org.DitaSemia.Oxygen.TopicNumStylesFilter;
import org.DitaSemia.Oxygen.AdvancedKeyRef.AdvancedKeyRefStylesFilter;
//import org.DitaSemia.Oxygen.Conbat.ConbatStylesFilter;
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
		boolean handled = AdvancedKeyRefStylesFilter.filter(styles, authorNode);
		if (!handled) {
			handled = TopicNumStylesFilter.filter(styles, authorNode);
			if (!handled) {
				handled = ConbatStylesFilter.filter(styles, authorNode);
			}
		}
		return styles;
	}

}
