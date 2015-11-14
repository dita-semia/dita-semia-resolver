<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
    exclude-result-prefixes	= "#all"
	expand-text				= "yes">
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:include href="draw-sequence-diagram.xsl"/>
	
    <xsl:template match="/">
    	
    	<xsl:processing-instruction name="xml-model">href="urn:dita-ng:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>

        <xsl:call-template name="DrawSequenceDiagram">
        	<xsl:with-param name="callingComponentName" select="reference/refbody/section[1]/p[1]/codeph"/>
        	<xsl:with-param name="callList" 			select="reference/refbody/section[1]/sl[1]/sli"/>
        </xsl:call-template>
    	
    </xsl:template>
	
	
</xsl:stylesheet>
