<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"  
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:jt 	= "http://saxon.sf.net/java-type"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
	xmlns:dxd	= "http://www.dita-semia.org/dynamic-xml-definition"
	xmlns:ikd	= "http://www.dita-semia.org/implicit-keydef"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	exclude-result-prefixes	= "#all">
	
	
	<!--	dita-semia	-->
	
	<xsl:function name="ds:resolveEmbeddedXPath" as="xs:string" use-when="not(function-available('ds:resolveEmbeddedXPath'))">
		<xsl:param name="text" 		as="xs:string"/>
		<xsl:param name="context" 	as="node()"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:resolveEmbeddedXPath' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ds:getOutsourcedSvgUri" as="xs:anyURI" use-when="not(function-available('ds:getOutsourcedSvgUri'))">
		<xsl:param name="uri"	as="xs:anyURI"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:getOutsourcedSvgUri' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ds:getFixedBaseUri" as="xs:anyURI" use-when="not(function-available('ds:getFixedBaseUri'))">
		<xsl:param name="node"	as="node()"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:getFixedBaseUri' not registered.</xsl:message>
	</xsl:function>
	
	
	<!--	advanced-keyref -->
	
	<xsl:function name="akr:getKeyDefByRefString" as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?" use-when="not(function-available('akr:getKeyDefByRefString'))">
		<xsl:param name="refString" as="xs:string"/>
		<xsl:param name="baseUri" 	as="xs:anyURI"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'akr:getKeyDefByRefString' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="akr:getKeyTypeDef" as="element()" use-when="not(function-available('akr:getKeyTypeDef'))">
		<xsl:param name="keyRef" 	as="element()"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'akr:getKeyTypeDef' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="akr:getDisplaySuffix" as="xs:string+" use-when="not(function-available('akr:getDisplaySuffix'))">
		<xsl:param name="keyRef" 	as="element()"/>
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'akr:getDisplaySuffix' not registered.</xsl:message>
	</xsl:function>
	
	
	<!--	implicit-keydef -->
	
	<!--<xsl:function name="ikd:getRefString" as="xs:string?" use-when="not(function-available('ikd:getRefString'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getRefString' not registered.</xsl:message>
	</xsl:function>-->
	
	<!--<xsl:function name="ikd:getKey" as="xs:string?" use-when="not(function-available('ikd:getKey'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getKey' not registered.</xsl:message>
	</xsl:function>-->
	
	<!--<xsl:function name="ikd:getName" as="xs:string?" use-when="not(function-available('ikd:getName'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getName' not registered.</xsl:message>
	</xsl:function>-->
	
	<!--<xsl:function name="ikd:getPath" as="xs:string*" use-when="not(function-available('ikd:getPath'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getPath' not registered.</xsl:message>
	</xsl:function>-->
	
	<!--<xsl:function name="ikd:getRoot" as="element()?" use-when="not(function-available('ikd:getRoot'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getRoot' not registered.</xsl:message>
	</xsl:function>-->
	
	<xsl:function name="ikd:getIsDontLink" as="xs:boolean?" use-when="not(function-available('ikd:getIsDontLink'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getIsDontLink' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ikd:getIsFilteredKey" as="xs:boolean?" use-when="not(function-available('ikd:getIsFilteredKey'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getIsFilteredKey' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ikd:getIsKeyHidden" as="xs:boolean?" use-when="not(function-available('ikd:getIsKeyHidden'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getIsKeyHidden' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ikd:getIsOverwritable" as="xs:boolean?" use-when="not(function-available('ikd:getIsOverwritable'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getIsOverwritable' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ikd:getIsResourceOnly" as="xs:boolean?" use-when="not(function-available('ikd:getIsResourceOnly'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getIsResourceOnly' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ikd:getLocation" as="xs:string?" use-when="not(function-available('ikd:getLocation'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:param name="baseUri"	as="xs:anyURI"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getLocation' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ikd:getKeyFilterAttr" as="element()?" use-when="not(function-available('ikd:getKeyFilterAttr'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getKeyFilterAttr' not registered.</xsl:message>
	</xsl:function>
	
	
	<!--	xslt-conref	-->
	
	<xsl:function name="xcr:resolve" as="element()" use-when="not(function-available('xcr:resolve'))">
		<xsl:param name="xcrElement"	as="element()"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'xcr:resolve' not registered.</xsl:message>
	</xsl:function>
	


</xsl:stylesheet>
