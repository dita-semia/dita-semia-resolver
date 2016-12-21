<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet exclude-result-prefixes="ditaarch opentopic ds" version="2.0"
	xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" xmlns:ds="org.dita-semia.resolver"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:opentopic="http://www.idiominc.com/opentopic"
	xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	
	<xsl:variable name="DL_OUTPUTCLASS_TREE" as="xs:string">tree</xsl:variable>
	
	<!-- outputclass "tree" -->
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_TREE]">
		<fo:block xsl:use-attribute-sets="ds:dl-tree">
			
			<xsl:call-template name="commonattributes"/>
			
			<xsl:apply-templates mode="dl-tree"/>
			
		</fo:block>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dlentry ')]" mode="dl-tree">
		
		<xsl:variable name="allEntries"			as="element()*"	select="ancestor::*[contains(@class, ' topic/dl ')]//*[contains(@class, ' topic/dlentry ')]"/>
		<xsl:variable name="countPreEntries"	as="xs:integer"	select="count($allEntries intersect (preceding::* | ancestor::*))"/>
		<xsl:variable name="countPostEntries"	as="xs:integer"	select="count($allEntries intersect (following::*))"/>
		<xsl:variable name="level" 				as="xs:integer" select="count(ancestor::*[contains(@class, ' topic/dlentry ')]) + 1"/>
		
		<!--<xsl:message>Name: '<xsl:value-of select="Name"/>', vor: <xsl:value-of select="$ElementeVor"/>, nach: <xsl:value-of select="$ElementeNach"/></xsl:message>-->
		
		<fo:block xsl:use-attribute-sets="ds:dlentry-tree">
			
			<xsl:call-template name="commonattributes"/>
			
			<xsl:if test="not(contains(parent::*/@class, ' topic/dl '))">
				<xsl:if test="exists(preceding-sibling::*)">
					<xsl:attribute name="border-top-style" select="'solid'"/>
				</xsl:if>
				<xsl:attribute name="margin-left" 		select="$DL_TREE_INDENT"/>
				<xsl:attribute name="border-left-style" select="'solid'"/>
			</xsl:if>

			<xsl:if test="($DL_TREE_WIDOWS > $countPreEntries)">
				<xsl:attribute name="keep-with-previous.within-column" select="100"/>
			</xsl:if>
			<xsl:if test="($DL_TREE_ORPHANS > $countPostEntries) and ($countPostEntries > 0)">
				<xsl:attribute name="keep-with-next.within-column" select="100"/>
			</xsl:if>
			
			<xsl:choose>
				<xsl:when test="exists(*[contains(@class, ' topic/dd ')]/node())">
					
					<!-- Formatierung als Tabelle -->
					<fo:table>
						<fo:table-column column-number="1" column-width="70mm - ({$level} * ({$DL_TREE_INDENT} + {$DL_TREE_BORDER_WIDTH}))"/>
						<fo:table-column column-number="2" column-width="95mm"/>
						
						<fo:table-body>
							<fo:table-row keep-together.within-column	= "100">
								
								<fo:table-cell margin-left = "0"> 
									<!-- margin-left muss explizit auf 0 gesetzt werden, da es sich anderenfalls aufsummiert -->
									<xsl:apply-templates select="*[contains(@class, ' topic/dt ')]" mode="#current"/>
								</fo:table-cell>
								<fo:table-cell margin-left="0" padding-right="1.5mm">									
									<!-- margin-left muss explizit auf 0 gesetzt werden, da es sich anderenfalls aufsummiert -->
									<xsl:apply-templates select="*[contains(@class, ' topic/dd ')]" mode="#current"/>
								</fo:table-cell>
							</fo:table-row>
						</fo:table-body>
					</fo:table>
					<xsl:apply-templates select="*[contains(@class, ' topic/dlentry ')]" mode="#current"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="*[contains(@class, ' topic/dt ') or contains(@class, ' topic/dlentry ')]" mode="#current"/>
				</xsl:otherwise>
			</xsl:choose>
			
		</fo:block>
		
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dt ')]" mode="dl-tree">
		
		<fo:block xsl:use-attribute-sets="ds:dt-tree">
			<xsl:if test="exists(following-sibling::*)">
				<xsl:attribute name="keep-with-next.within-column">100</xsl:attribute>
			</xsl:if>
			
			<xsl:call-template name="commonattributes"/>
			
			<xsl:apply-templates mode="#default"/>
			
		</fo:block>
		
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dd ')]" mode="dl-tree">
		
		<fo:block xsl:use-attribute-sets="ds:dd-tree">
			
			<xsl:call-template name="commonattributes"/>
			
			<xsl:apply-templates mode="#default"/>
			
		</fo:block>
		
	</xsl:template>

</xsl:stylesheet>
