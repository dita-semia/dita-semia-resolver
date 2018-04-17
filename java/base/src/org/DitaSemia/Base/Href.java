package org.DitaSemia.Base;

import java.net.MalformedURLException;
import java.net.URL;


public class Href {
	
	protected URL 		refUrl;
	protected String 	refId;
	
	public Href(String href, URL baseUrl) {
		if ((href != null) && (!href.isEmpty()) && (href.contains(DitaUtil.HREF_URL_ID_DELIMITER))) {
			final int 		splitPos 	= href.indexOf(DitaUtil.HREF_URL_ID_DELIMITER);
			final String	hrefUrl 	= href.substring(0, splitPos);
			
			refId 	= href.substring(splitPos + 1);
			
			if (hrefUrl.isEmpty()) {
				refUrl = baseUrl;
			} else {
				try {
					refUrl = new URL(baseUrl, hrefUrl);
				} catch (MalformedURLException e) {
					refUrl = null;
				}
			}
		} else {
			refUrl	= null;
			refId	= null;
		}
	}
	
	public URL getRefUrl() {
		return refUrl;
	}

	public String getRefId() {
		return refId;
	}

}
