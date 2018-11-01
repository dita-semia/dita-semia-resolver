<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:jt 	= "http://saxon.sf.net/java-type"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
	exclude-result-prefixes		= "#all">
	
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	<xsl:include href="urn:dita-semia:xsl:cba-const.xsl"/>
	<xsl:include href="urn:dita-semia:xsl:extract-text.xsl"/>
	
	
	<xsl:function name="ds:resolveEmbeddedXPath" as="xs:string" use-when="not(function-available('ds:resolveEmbeddedXPath'))">
		<xsl:param name="text" 		as="xs:string"/>
		<xsl:param name="context" 	as="node()"/>

		<xsl:sequence select="$text"/>
	</xsl:function>
	
	<xsl:function name="akr:getDisplaySuffix" as="xs:string*" use-when="not(function-available('akr:getDisplaySuffix'))">
		<xsl:param name="keyRef" 	as="element()"/>
		<xsl:param name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?"/>

		<!-- return nothing -->
	</xsl:function>
	
	<xsl:function name="akr:getKeyDefByRefString" as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?" use-when="not(function-available('akr:getKeyDefByRefString'))">
		<xsl:param name="refString" as="xs:string"/>
		<xsl:param name="baseUri" 	as="xs:anyURI"/>
		
		<!-- return nothing -->
	</xsl:function>
	
</xsl:stylesheet>
