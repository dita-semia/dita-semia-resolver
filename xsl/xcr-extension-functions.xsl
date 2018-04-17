<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"  
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:jt 	= "http://saxon.sf.net/java-type"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
	xmlns:dxd	= "http://www.dita-semia.org/dynamic-xml-definition"
	xmlns:ikd	= "http://www.dita-semia.org/implicit-keydef"
	exclude-result-prefixes	= "#all">
	
	
	<!--	dita-semia	-->
	
	<xsl:function name="ds:getChildTopics" as="node()*" use-when="not(function-available('ds:getChildTopics'))">
		<xsl:param name="topic" as="node()"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:getChildTopics' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ds:getTopicNum" as="xs:string?" use-when="not(function-available('ds:getTopicNum'))">
		<xsl:param name="topicId" 	as="xs:string"/>
		<xsl:param name="uri" 		as="xs:anyURI"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:getTopicNum' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ds:getTextWidth" as="xs:double" use-when="not(function-available('ds:getTextWidth'))">
		<xsl:param name="text" 		as="xs:string"/>
		<xsl:param name="font" 		as="xs:string"/>
		<xsl:param name="isBold" 	as="xs:boolean"/>
		<xsl:param name="fontSize" 	as="xs:double"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:getTextWidth' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ds:hyphenateWord" as="xs:string" use-when="not(function-available('ds:hyphenateWord'))">
		<xsl:param name="word" 		as="xs:string"/>
		<xsl:param name="delimiter" as="xs:string"/>
		<xsl:param name="lang" 		as="xs:string"/>
		<xsl:param name="country" 	as="xs:string?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:hyphenateWord' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ds:evaluateXPath" as="item()*" use-when="not(function-available('ds:evaluateXPath'))">
		<xsl:param name="xPath" 	as="xs:string"/>
		<xsl:param name="context" 	as="node()"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:evaluateXPath' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ds:resolveEmbeddedXPath" as="xs:string" use-when="not(function-available('ds:resolveEmbeddedXPath'))">
		<xsl:param name="text" 		as="xs:string"/>
		<xsl:param name="context" 	as="node()"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:resolveEmbeddedXPath' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ds:executeXslt" as="document-node()" use-when="not(function-available('ds:executeXslt'))">
		<xsl:param name="xmlUri" as="xs:anyURI"/>
		<xsl:param name="xslUri" as="xs:anyURI"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:executeXslt' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ds:loadXmlFile" as="document-node()?" use-when="not(function-available('ds:loadXmlFile'))">
		<xsl:param name="uri" 						as="xs:string"/>
		<xsl:param name="expandAttributDefaults" 	as="xs:boolean"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:loadXmlFile' not registered.</xsl:message>
	</xsl:function>
	
	
	<!--	advanced-keyref -->
	
	<xsl:function name="akr:getAncestorKeyDef" as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?" use-when="not(function-available('akr:getAncestorKeyDef'))">
		<xsl:param name="node" 		as="node()"/>
		<xsl:param name="keyType" 	as="xs:string"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'akr:getAncestorKeyDef' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="akr:getMatchingKeyDefs" as="xs:string*" use-when="not(function-available('akr:getMatchingKeyDefs'))">
		<xsl:param name="typeFilter" 		as="xs:string*"/>
		<xsl:param name="namespaceFilter" 	as="xs:string*"/>
		<xsl:param name="baseUri" 			as="xs:anyURI"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'akr:getMatchingKeyDefs' not registered.</xsl:message>
	</xsl:function>
	
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
	
	
	<!--	dynamic-xml-definition	-->
	
	<xsl:function name="dxd:getKeyDefByTypeName" as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?" use-when="not(function-available('dxd:getKeyDefByTypeName'))">
		<xsl:param name="typeName" 			as="xs:string"/>
		<xsl:param name="baseUri" 			as="xs:anyURI"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'dxd:getKeyDefByTypeName' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="dxd:getTypeDef" as="element()?" use-when="not(function-available('dxd:getTypeDef'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'dxd:getTypeDef' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="dxd:getTypeName" as="xs:string?" use-when="not(function-available('dxd:getTypeName'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'dxd:getTypeName' not registered.</xsl:message>
	</xsl:function>
	
	
	<!--	implicit-keydef -->
	
	<xsl:function name="ikd:getRefString" as="xs:string?" use-when="not(function-available('ikd:getRefString'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getRefString' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ikd:getKey" as="xs:string?" use-when="not(function-available('ikd:getKey'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getKey' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ikd:getName" as="xs:string?" use-when="not(function-available('ikd:getName'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getName' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ikd:getPath" as="xs:string*" use-when="not(function-available('ikd:getPath'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getPath' not registered.</xsl:message>
	</xsl:function>
	
	<xsl:function name="ikd:getRoot" as="element()?" use-when="not(function-available('ikd:getRoot'))">
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ikd:getRoot' not registered.</xsl:message>
	</xsl:function>
	
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
	
</xsl:stylesheet>
