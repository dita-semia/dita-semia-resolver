<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet version="2.0"
	xmlns:ditaarch			= "http://dita.oasis-open.org/architecture/2005/"
	xmlns:fo				= "http://www.w3.org/1999/XSL/Format" 
	xmlns:opentopic			= "http://www.idiominc.com/opentopic"
	xmlns:opentopic-func	= "http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs				= "http://www.w3.org/2001/XMLSchema" 
	xmlns:xsl				= "http://www.w3.org/1999/XSL/Transform"
	xmlns:ds				= "org.dita-semia.resolver"
	exclude-result-prefixes="#all" >

	<xsl:include href="dl-outputclasses/dl-tree.xsl"/>
	<xsl:include href="dl-outputclasses/dl-bullet-list-titles.xsl"/>
	<xsl:include href="dl-outputclasses/dl-numbered-list-titles.xsl"/>
	<xsl:include href="dl-outputclasses/dl-bullet-list-dashes.xsl"/>
	<xsl:include href="dl-outputclasses/dl-header-table.xsl"/>
	<xsl:include href="dl-outputclasses/dl-parameter-table.xsl"/>
	<xsl:include href="dl-outputclasses/dl-table.xsl"/>

	<xsl:template name="ds:dl-width">
		<xsl:variable name="outputclasses" 	as="xs:string*" select="tokenize(@outputclass, '\s+')"/>
		<xsl:variable name="widthClass"		as="xs:string*"	select="$outputclasses[starts-with(., 'width-')]"/>
		<xsl:if test="exists($widthClass)">
			<xsl:attribute name="style">
				<xsl:text>width: </xsl:text>
				<xsl:value-of select="substring-after($widthClass, 'width-')"/>
				<xsl:text>;</xsl:text>
			</xsl:attribute>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>
