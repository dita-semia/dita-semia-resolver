<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes		= "#all">
    
 	<xsl:include href="xslt-conref.xsl"/>
	<xsl:include href="conbat.xsl"/>
	<xsl:include href="advanced-keyref.xsl"/>
	
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	
	<xsl:template match="element() | document-node()">
		<xsl:copy>
			<xsl:apply-templates select="attribute() | node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="attribute() | processing-instruction() | text() | comment()">
		<xsl:copy/>
	</xsl:template>
	
</xsl:stylesheet>
