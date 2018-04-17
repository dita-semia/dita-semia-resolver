<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	exclude-result-prefixes		= "#all">
	
	<xsl:include href="../class.xsl"/>
	<xsl:include href="../cba-const.xsl"/>
	<xsl:include href="extract-text.xsl"/>
	
	
	<xsl:function name="ds:resolveEmbeddedXPath" as="xs:string" use-when="not(function-available('ds:resolveEmbeddedXPath'))">
		<xsl:param name="text" 		as="xs:string"/>
		<xsl:param name="context" 	as="node()"/>

		<xsl:sequence select="$text"/>
	</xsl:function>
	
</xsl:stylesheet>
