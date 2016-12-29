<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:cba	= "http://www.dita-semia.org/conbat"
	exclude-result-prefixes		= "#all">
	
	<xsl:include href="../common/extract-text.xsl"/>
	<xsl:include href="../class.xsl"/>


	<xsl:template match="*[*[contains(@class, $C_TITLE)]]">
		<xsl:sequence select="normalize-space(ds:extractText(*[contains(@class, $C_TITLE)]))"/>
	</xsl:template>
	
	<xsl:template match="*[@cba:title]">
		<xsl:value-of select="normalize-space(cba:resolveEmbeddedXPath(@cba:title))"/>
	</xsl:template>
	
	<xsl:template match="*[*[contains(@class, $C_DT)]]">
		<xsl:sequence select="normalize-space(ds:extractText(*[contains(@class, $C_DT)]))"/>
	</xsl:template>
	
	<xsl:template match="*[@cba:dt]">
		<xsl:value-of select="normalize-space(cba:resolveEmbeddedXPath(@cba:dt))"/>
	</xsl:template>

</xsl:stylesheet>
