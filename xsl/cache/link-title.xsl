<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:cba	= "http://www.dita-semia.org/conbat"
	exclude-result-prefixes		= "#all">
	
	<xsl:include href="../common/extract-text.xsl"/>
	<xsl:include href="../class.xsl"/>
	<xsl:include href="../cba-const.xsl"/>


	<xsl:template match="*[contains(@class, $C_BOOKMAP)]" priority="4">
		<xsl:variable name="mainTitle" as="xs:string?" select="normalize-space(ds:extractText(*[contains(@class, $C_TITLE)]/*[contains(@class, $C_MAINBOOKTITLE)]))"/>
		<xsl:choose>
			<xsl:when test="string($mainTitle) != ''">
				<xsl:sequence select="$mainTitle"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="normalize-space(ds:extractText(*[contains(@class, $C_TITLE)]))"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*[contains(@class, $C_DL)][@outputclass = 'numbered-list-titles']/* | 
						*[contains(@class, $C_OL)]/*" priority="3">
		<xsl:variable name="title" as="xs:string?">
			<xsl:next-match/>
		</xsl:variable>
		<xsl:variable name="number" as="xs:integer" select="count(preceding-sibling::*) + 1"/>
		<xsl:value-of select="string-join((concat($number, '.'), $title), ' ')"/>
	</xsl:template>

	<xsl:template match="*[*[contains(@class, $C_TITLE)]]" priority="2">
		<xsl:sequence select="normalize-space(ds:extractText(*[contains(@class, $C_TITLE)]))"/>
	</xsl:template>
	
	
	<xsl:template match="*[*[contains(@class, $C_DT)]]" priority="2">
		<xsl:sequence select="normalize-space(ds:extractText(*[contains(@class, $C_DT)]))"/>
	</xsl:template>
	
	
	<xsl:template match="*[@cba:title]" priority="1">
		<xsl:value-of select="normalize-space(ds:resolveEmbeddedXPath(@cba:title, .))"/>
	</xsl:template>
	
	
	<xsl:template match="*[@cba:dt]" priority="1">
		<xsl:value-of select="normalize-space(ds:resolveEmbeddedXPath(@cba:dt, .))"/>
	</xsl:template>
	
	<xsl:template match="*">
		<!-- default: no title -->
	</xsl:template>
	
	<xsl:function name="ds:resolveEmbeddedXPath" as="xs:string" use-when="not(function-available('ds:resolveEmbeddedXPath'))">
		<xsl:param name="text" 		as="xs:string"/>
		<xsl:param name="context" 	as="node()"/>
		<xsl:message terminate="yes">ERROR: Custom extension function 'ds:resolveEmbeddedXPath' not registered.</xsl:message>
	</xsl:function>
	
</xsl:stylesheet>
