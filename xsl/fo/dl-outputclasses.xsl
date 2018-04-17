<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet version="2.0"
	xmlns:xs				= "http://www.w3.org/2001/XMLSchema" 
	xmlns:xsl				= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo				= "http://www.w3.org/1999/XSL/Format"
	xmlns:ds				= "http://www.dita-semia.org"
	exclude-result-prefixes	= "#all">

	<xsl:include href="dl-outputclasses/dl-tree.xsl"/>
	<xsl:include href="dl-outputclasses/dl-bullet-list-titles.xsl"/>
	<xsl:include href="dl-outputclasses/dl-numbered-list-titles.xsl"/>
	<xsl:include href="dl-outputclasses/dl-bullet-list-dashes.xsl"/>
	<xsl:include href="dl-outputclasses/dl-header-table.xsl"/>
	<xsl:include href="dl-outputclasses/dl-parameter-table.xsl"/>
	<xsl:include href="dl-outputclasses/dl-table.xsl"/>
	
	<xsl:template name="ds:dt-toc-id">
		<xsl:param name="inline" as="xs:boolean?" select="false()"/>
		<xsl:element name="{if ($inline) then 'fo:inline' else 'fo:block'}">
			<xsl:attribute name="id">
				<xsl:call-template name="generate-toc-id">
					<xsl:with-param name="element" select="parent::*"/>
				</xsl:call-template>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
