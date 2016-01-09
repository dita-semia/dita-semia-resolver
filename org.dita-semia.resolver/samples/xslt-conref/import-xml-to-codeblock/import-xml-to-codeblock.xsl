<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
    exclude-result-prefixes	= "#all">
	
	<xsl:param name="maxWidth" as="xs:integer" select="60"/>
	
	<xsl:output method="xml" indent="no"/>
	
	<xsl:include href="urn:dita-semia:xslt-conref:xsl:class.xsl"/>
	
	<xsl:include href="xml-serialize.xsl"/>
	
    <xsl:template match="/">
    	
        <codeblock class="{$CP_CODEBLOCK}">
        	<xsl:value-of select="ds:xmlSerialize(*, '    ', $maxWidth)"/>
        </codeblock>
    	
    </xsl:template>
	
</xsl:stylesheet>
