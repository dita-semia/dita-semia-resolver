package org.DitaSemia.Base.AdvancedKeyref;

import java.util.Collection;
import java.util.HashSet;

import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.apache.log4j.Logger;

public class KeyTypeDef {

	private static final Logger logger = Logger.getLogger(KeyTypeDef.class.getName());

	protected final String 			name;
	protected final boolean 		isCodeFont;
	protected final boolean 		isItalicFont;
	protected final String 			prefix;
	protected final String 			suffix;
	protected final int 			selectPriority;
	protected final String			pathDelimiter;
	protected final HashSet<String>	ancestorTypes;

	public static final String ELEMENT				= "KeyTypeDef";
	public static final String ATTR_NAME			= "name";
	public static final String ATTR_IS_CODE_FONT	= "isCodeFont";
	public static final String ATTR_IS_ITALIC_FONT	= "isItalicFont";
	public static final String ATTR_PREFIX			= "prefix";
	public static final String ATTR_SUFFIX			= "suffix";
	public static final String ATTR_SELECT_PRIORITY	= "selectPriority";
	public static final String ATTR_PATH_DELIMITER	= "path-delimiter";
	
	public static final KeyTypeDef DEFAULT = new KeyTypeDef(null, false, true, "", "", 1, "/", null);

	public static KeyTypeDef fromNode(SaxonNodeWrapper saxonNodeWrapper, KeyTypeDef parent) {
		final String 			name 			= saxonNodeWrapper.getAttribute(ATTR_NAME, null);
		final boolean 			isCodeFont		= getBoolean	(saxonNodeWrapper, ATTR_IS_CODE_FONT, 		parent.isCodeFont());
		final boolean 			isItalicFont	= getBoolean	(saxonNodeWrapper, ATTR_IS_ITALIC_FONT, 	parent.isItalicFont());
		final String 			prefix			= getString		(saxonNodeWrapper, ATTR_PREFIX, 			parent.getPrefix());
		final String 			suffix			= getString		(saxonNodeWrapper, ATTR_SUFFIX, 			parent.getSuffix());
		final int 				selectPriority	= getInt		(saxonNodeWrapper, ATTR_SELECT_PRIORITY, 	parent.getSelectPriority());
		final String 			pathDelimiter	= getString		(saxonNodeWrapper, ATTR_PATH_DELIMITER, 	parent.getPathDelimiter());

		final HashSet<String>	ancestorTypes	= parent.getAncestorTypes();
		if (parent.getName() != null) {
			ancestorTypes.add(parent.getName());
		}
		
		return new KeyTypeDef(name, isCodeFont, isItalicFont, prefix, suffix, selectPriority, pathDelimiter, ancestorTypes);
	}

	private static boolean getBoolean(NodeWrapper node, String attrName, boolean defaultValue) {
		final String attrValue = node.getAttribute(attrName, null);
		if (attrValue == null) {
			return defaultValue;
		} else {
			return (attrValue.equals("1") || attrValue.equals("true"));
		}
	}
	
	private static String getString(NodeWrapper node, String attrName, String defaultValue) {
		final String attrValue = node.getAttribute(attrName, null);
		if (attrValue == null) {
			return defaultValue;
		} else {
			return attrValue;
		}
	}
	
	private static int getInt(NodeWrapper node, String attrName, int defaultValue) {
		final String attrValue = node.getAttribute(attrName, null);
		if (attrValue == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(attrValue);
		}
	}

	public KeyTypeDef(String name, boolean isCodeFont, boolean isItalicFont, String prefix, String suffix, int selectPriority, String pathDelimiter, Collection<String> ancestorTypes) {
		this.name			= name;
		this.isCodeFont		= isCodeFont;
		this.isItalicFont	= isItalicFont;
		this.prefix			= prefix;
		this.suffix			= suffix;
		this.selectPriority	= selectPriority;
		this.pathDelimiter	= pathDelimiter;
		this.ancestorTypes	= (ancestorTypes == null) ? new HashSet<>() : new HashSet<>(ancestorTypes);

		//logger.info("new KeyTypeDef: " + toString());
	}

	public String getName() {
		return name;
	}

	public boolean isCodeFont() {
		return isCodeFont;
	}

	public boolean isItalicFont() {
		return isItalicFont;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public int getSelectPriority() {
		return selectPriority;
	}

	public String getPathDelimiter() {
		return pathDelimiter;
	}
	
	public HashSet<String> getAncestorTypes() {
		return new HashSet<String>(ancestorTypes);
	}

	@Override
	public String toString() {
		return 
			"name: " + name + 
			", isCodeFont: " + isCodeFont + 
			", isItalicFont: " + isItalicFont + 
			", prefix: '" + prefix + "'" + 
			", suffix: '" + suffix + "'" + 
			", selectPriority: " + selectPriority +
			", pathDelimiter: " + pathDelimiter + 
			", ancestorTypes: " + (((ancestorTypes == null) || (ancestorTypes.isEmpty())) ? '-' : String.join("/", ancestorTypes));
	}
}
