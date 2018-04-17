package org.DitaSemia.Oxygen.ExtensionBundle;

import org.DitaSemia.Oxygen.MapContextStylesFilter;
import org.DitaSemia.Oxygen.TopicNumStylesFilter;
import org.DitaSemia.Oxygen.AdvancedKeyRef.AdvancedKeyRefStylesFilter;
//import org.DitaSemia.Oxygen.Conbat.ConbatStylesFilter;
import org.DitaSemia.Oxygen.Conbat.ConbatStylesFilter;

import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class DitaSemiaStylesFilter implements StylesFilter {

	private boolean active = true;
	
	@Override
	public String getDescription() {
		return "DITA-Semia styles filter";
	}

	@Override
	public Styles filter(Styles styles, AuthorNode authorNode) { 
		if (active) {
			boolean handled = AdvancedKeyRefStylesFilter.filter(styles, authorNode);
			if (!handled) {
				handled = TopicNumStylesFilter.filter(styles, authorNode);
				if (!handled) {
					handled = MapContextStylesFilter.filter(styles, authorNode);
					if (!handled) {
						handled = ConbatStylesFilter.filter(styles, authorNode);
					}
				}
			}
			return styles;
		} else {
			return styles;
		}
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
}
