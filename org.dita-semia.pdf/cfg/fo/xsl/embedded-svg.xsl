<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:fo	= "http://www.w3.org/1999/XSL/Format"
	xmlns:svg	= "http://www.w3.org/2000/svg"
	exclude-result-prefixes="#all">
	
	<!-- This file contains bug fixes in DITA-OT version 2.2.1 for handling embedded svg -->
	
	<!-- passing svg content to fop -->
	<xsl:template match="*[contains(@class, ' svg-d/svg-container ')]" priority="10">
		<fo:instream-foreign-object>
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates select="svg:svg" mode="fix-svg-namespace"/>
		</fo:instream-foreign-object>
	</xsl:template>
	
	<xsl:template match="svg:svg">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<!-- The preprocess doesn't handle namespaces without prefix correctly. Thus, reset the namespace of all elements within svg:svg. -->
	<xsl:template match="element()[namespace-uri() = '']" mode="fix-svg-namespace">
		<xsl:element name="svg:{local-name()}" namespace="http://www.w3.org/2000/svg">
			<xsl:apply-templates select="attribute(), node()" mode="#current"/>
		</xsl:element>
	</xsl:template>
	
	
	<!-- default patterns: identity transform -->

	<xsl:template match="element()" mode="fix-svg-namespace">
		<xsl:copy>
			<xsl:apply-templates select="attribute(), node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="attribute() | text() | comment() | processing-instruction()" mode="fix-svg-namespace">
		<xsl:copy/>
	</xsl:template>
	
</xsl:stylesheet>