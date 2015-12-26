<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:saxon = "http://saxon.sf.net/"
	xmlns:ds	= "http://www.dita-semia.org"
    exclude-result-prefixes	= "#all"
	expand-text				= "yes">
	
	<xsl:param name="xPathToXsltConref" as="xs:string" select="'/*[1]'"/>
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:include href="draw-sequence-diagram.xsl"/>
	
    <xsl:template match="/">
    	
    	<xsl:processing-instruction name="xml-model">href="urn:dita-ng:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>
    	
    	<xsl:variable name="xsltConref" as="element()" select="saxon:evaluate($xPathToXsltConref)"/>

        <xsl:call-template name="DrawSequenceDiagram">
        	<xsl:with-param name="callingComponentName" select="$xsltConref/preceding-sibling::*/self::p[1]/codeph[1]"/>
        	<xsl:with-param name="callList" 			select="$xsltConref/preceding-sibling::*/self::sl[1]/sli/codeph[1]"/>
        </xsl:call-template>
    	
    </xsl:template>
	
	
</xsl:stylesheet>
