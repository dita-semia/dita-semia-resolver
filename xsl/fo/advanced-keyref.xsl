<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet version="2.0"
	xmlns:ditaarch			= "http://dita.oasis-open.org/architecture/2005/"
	xmlns:fo				= "http://www.w3.org/1999/XSL/Format" 
	xmlns:opentopic			= "http://www.idiominc.com/opentopic"
	xmlns:opentopic-func	= "http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs				= "http://www.w3.org/2001/XMLSchema" 
	xmlns:xsl				= "http://www.w3.org/1999/XSL/Transform"
	exclude-result-prefixes="ditaarch opentopic" >

	<xsl:template match="*[contains(@class,' topic/xref ')][@outputclass = 'advanced-keyref']" priority="10">
		<xsl:variable name="resolved" as="node()*">
			<xsl:next-match/>
		</xsl:variable>
		<xsl:apply-templates select="$resolved" mode="remove-basic-link-styling"/>
	</xsl:template>
	
	<xsl:template match="fo:basic-link" mode="remove-basic-link-styling">
		<xsl:copy>
			<xsl:copy-of select="@internal-destination | @external-destination"/>
			<!-- remove all other attributes -->
			<xsl:copy-of select="node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="attribute() | text() | processing-instruction() | comment()" mode="remove-basic-link-styling">
		<xsl:copy/>
	</xsl:template>
	
	<xsl:template match="element()" mode="remove-basic-link-styling">
		<xsl:copy>
			<xsl:apply-templates select="attribute() | node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
