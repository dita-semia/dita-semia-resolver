<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet version="2.0"
	xmlns:xs				= "http://www.w3.org/2001/XMLSchema" 
	xmlns:xsl				= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo				= "http://www.w3.org/1999/XSL/Format"
	xmlns:ds				= "http://www.dita-semia.org"
	exclude-result-prefixes	= "#all">
	
	
	<xsl:variable name="DL_OUTPUTCLASS_HEADER_TABLE" 		as="xs:string">header-table</xsl:variable>
	
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][tokenize(@outputclass, '\s+') = $DL_OUTPUTCLASS_HEADER_TABLE]">
		<fo:block xsl:use-attribute-sets="ds:dl-header-table">
			
			<xsl:call-template name="commonattributes"/>
			
			<fo:table table-layout="fixed" width="100%">
				
				<fo:table-column column-number	= "1" column-width	= "proportional-column-width(25)"/>
				<fo:table-column column-number	= "2" column-width	= "proportional-column-width(75)"/>
				
				<fo:table-body>
					<xsl:apply-templates mode="dl-header-table"/>
				</fo:table-body>
				
			</fo:table>
			
		</fo:block>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dlentry ')]" mode="dl-header-table">
		
		<xsl:variable name="isFirstEntry"	as="xs:boolean" select="empty(preceding-sibling::*)"/>
		<xsl:variable name="isLastEntry"	as="xs:boolean" select="empty(following-sibling::*)"/>
		
		<xsl:variable name="cellAttributes" as="attribute()*">
			<xsl:attribute name="border-top-style"		select="'solid'"/>
			<xsl:attribute name="border-bottom-style"	select="'solid'"/>
			<xsl:attribute name="border-top-width"		select="if ($isFirstEntry)	then $DL_HEADER_TABLE_OUTER_BORDER_WIDTH else $DL_HEADER_TABLE_INNER_BORDER_WIDTH"/>
			<xsl:attribute name="border-bottom-width"	select="if ($isLastEntry)	then $DL_HEADER_TABLE_OUTER_BORDER_WIDTH else $DL_HEADER_TABLE_INNER_BORDER_WIDTH"/>
			<xsl:attribute name="margin-left"			select="0"/>	<!-- needs to be set explicitly to 0 (whyever) -->
		</xsl:variable>

		<xsl:variable name="countPreEntries"	as="xs:integer"	select="count(preceding-sibling::*)"/>
		<xsl:variable name="countPostEntries"	as="xs:integer"	select="count(following-sibling::*)"/>
		
		<!--<xsl:message>Name: '<xsl:value-of select="Name"/>', vor: <xsl:value-of select="$ElementeVor"/>, nach: <xsl:value-of select="$ElementeNach"/></xsl:message>-->
		
		<fo:table-row xsl:use-attribute-sets="ds:dlentry-header-table">
			
			<xsl:call-template name="commonattributes"/>

			<!--<xsl:if test="($DL_HEADER_TABLE_WIDOWS > $countPreEntries)">
				<xsl:attribute name="keep-with-previous.within-column" select="$KEEP_TABLE_ROW_VALUE"/>
			</xsl:if>
			<xsl:if test="($DL_HEADER_TABLE_ORPHANS > $countPostEntries) and ($countPostEntries > 0)">
				<xsl:attribute name="keep-with-next.within-column" select="$KEEP_TABLE_ROW_VALUE"/>
			</xsl:if>-->

			<fo:table-cell xsl:use-attribute-sets="ds:dl-common-table-cell">
				<xsl:copy-of select="$cellAttributes"/>
				<xsl:apply-templates select="*[contains(@class, ' topic/dt ')]" mode="#current"/>
			</fo:table-cell>
			<fo:table-cell xsl:use-attribute-sets="ds:dl-common-table-cell">
				<xsl:copy-of select="$cellAttributes"/>
				<xsl:apply-templates select="*[contains(@class, ' topic/dd ')]" mode="#current"/>
			</fo:table-cell>
		</fo:table-row>
		
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dt ')]" mode="dl-header-table">
		
		<fo:block xsl:use-attribute-sets="ds:dt-header-table">
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="#default"/>
		</fo:block>
		
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dd ')]" mode="dl-header-table">
		
		<fo:block xsl:use-attribute-sets="ds:dd-header-table">
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="#default"/>
		</fo:block>
		
	</xsl:template>
	
		
</xsl:stylesheet>
