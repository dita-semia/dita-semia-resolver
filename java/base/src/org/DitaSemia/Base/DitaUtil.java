package org.DitaSemia.Base;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import org.apache.log4j.Logger;

public class DitaUtil {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DitaUtil.class.getName());

	public static final String	ATTR_ID					= "id";
	public static final String	ATTR_CLASS				= "class";
	public static final String	ATTR_PROCESSING_ROLE	= "processing-role";
	public static final String	ATTR_HREF				= "href";
	public static final String	ATTR_NAME				= "name";
	public static final String	ATTR_VALUE				= "value";
	public static final String	ATTR_XTRF				= "xtrf";

	public static final String	CLASS_TOPIC_REF			= " map/topicref ";
	public static final String	CLASS_APPENDIX			= " bookmap/appendix ";
	public static final String	CLASS_CHAPTER			= " bookmap/chapter ";
	public static final String	CLASS_FRONTMATTER		= " bookmap/frontmatter ";
	public static final String	CLASS_BACKMATTER		= " bookmap/backmatter ";
	public static final String	CLASS_DATA				= " topic/data ";
	public static final String	CLASS_TOPIC				= " topic/topic ";
	public static final String	CLASS_TITLE				= " topic/title ";
	public static final String	CLASS_MAP				= " map/map ";

	public static final String	ROLE_RESOURCE_ONLY		= "resource-only";
	
	public static final String 	URL_ID_DELIMITER		= "#";

	public static final String	TOPIC_NUM_DELIMITER		= ".";



	public static String decodeUrl(URL url) {
		return decodeUrl(url.toString());
	}

	public static String decodeUrl(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static String getNodeLocation(URL url, String id) {
		return decodeUrl(url) + URL_ID_DELIMITER + id;
	}

	public static String numToLetter(int num) {
		if (num <= 0) {
			return "";
		} else {
			final int quot 	= num / 26;
			final int rem 	= num % 26;
			final char letter = (char)((int)'A' + rem - 1);
			return numToLetter(quot) + letter;
		}
	}
}
