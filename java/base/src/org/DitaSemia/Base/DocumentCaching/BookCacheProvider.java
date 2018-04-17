package org.DitaSemia.Base.DocumentCaching;

import java.net.URL;

public interface BookCacheProvider {
	
	public BookCache getBookCache(URL url);

	public URL getBookCacheRootUrl(URL url);

}
