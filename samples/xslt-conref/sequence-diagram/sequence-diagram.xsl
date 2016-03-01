<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
    exclude-result-prefixes	= "#all">
	
	<xsl:param name="xcr:current" as="element()"/>
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:include href="draw-sequence-diagram.xsl"/>
	
    <xsl:template match="/">
    	
    	<xsl:processing-instruction name="xml-model">href="urn:oasis:names:tc:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>
    	
        <xsl:call-template name="DrawSequenceDiagram">
        	<xsl:with-param name="callingComponentName" select="$xcr:current/preceding-sibling::*/self::p[1]/codeph[1]"/>
        	<xsl:with-param name="callList" 			select="$xcr:current/preceding-sibling::*/self::sl[1]/sli/codeph[1]"/>
        </xsl:call-template>
    	
    </xsl:template>
	
	
</xsl:stylesheet>
