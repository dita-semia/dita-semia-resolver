<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet version="2.0"
	xmlns:ditaarch			= "http://dita.oasis-open.org/architecture/2005/"
	xmlns:fo				= "http://www.w3.org/1999/XSL/Format" 
	xmlns:opentopic			= "http://www.idiominc.com/opentopic"
	xmlns:opentopic-func	= "http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs				= "http://www.w3.org/2001/XMLSchema" 
	xmlns:xsl				= "http://www.w3.org/1999/XSL/Transform"
	exclude-result-prefixes="ditaarch opentopic" >

	<xsl:include href="dl-outputclasses/dl-tree.xsl"/>
	<xsl:include href="dl-outputclasses/dl-bullet-list-titles.xsl"/>
	<xsl:include href="dl-outputclasses/dl-numbered-list-titles.xsl"/>
	<xsl:include href="dl-outputclasses/dl-bullet-list-dashes.xsl"/>
	<xsl:include href="dl-outputclasses/dl-header-table.xsl"/>
	<xsl:include href="dl-outputclasses/dl-parameter-table.xsl"/>

	<xsl:variable name="NO_TABLE_DL_OUTPUTCLASSES" as="xs:string*" select="($DL_OUTPUTCLASS_TREE, $DL_OUTPUTCLASS_HEADER_TABLE, $DL_OUTPUTCLASS_BULLET_LIST_TITLES)"/>
	
	<!-- override special handling for FOP -->
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $NO_TABLE_DL_OUTPUTCLASSES]/*[contains(@class,' topic/dlentry ')]/@id" priority="10">
		<xsl:copy/>
	</xsl:template>
	
</xsl:stylesheet>
