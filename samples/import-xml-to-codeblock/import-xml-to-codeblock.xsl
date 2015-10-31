<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
    exclude-result-prefixes	= "#all"
	expand-text				= "yes">
	
	<xsl:output method="xml" indent="no"/>
	
	<xsl:include href="xml-serialize.xsl"/>
	
    <xsl:template match="/">
    	
    	<xsl:processing-instruction name="xml-model">href="urn:dita-ng:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>
    	
        <codeblock>
        	<xsl:value-of select="ds:xmlSerialize(*, '    ', 50)"/>
        </codeblock>
    	
    </xsl:template>
	
</xsl:stylesheet>
