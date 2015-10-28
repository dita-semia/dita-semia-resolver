<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	exclude-result-prefixes	= "xs"
	expand-text				= "yes">
	
	<xsl:include href="urn:dita-semia:xslt-conref:xsl:check-xslt-conref.xsl"/>
	
	<xsl:template match="/">
		<xsl:call-template name="CheckUrl">
			<xsl:with-param name="url" select="'invalid url'"/>
		</xsl:call-template>
		<xsl:call-template name="CheckUrl">
			<xsl:with-param name="url" select="'invalid-script.xsl'"/>
		</xsl:call-template>
		<xsl:call-template name="CheckUrl">
			<xsl:with-param name="url" select="'valid-script.xsl'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="CheckUrl">
		<xsl:param name="url" as="xs:string"/>
		
		<xsl:message>check url '{$url}': {ds:isScriptValid($url, base-uri())}</xsl:message>
	</xsl:template>
	
	
</xsl:stylesheet>
