<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet exclude-result-prefixes="#all" version="2.0"
	xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" xmlns:ds="org.dita-semia.resolver"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:opentopic="http://www.idiominc.com/opentopic"
	xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	
	<xsl:variable name="DL_OUTPUTCLASS_BULLET_LIST_TITLES" as="xs:string">bullet-list-titles</xsl:variable>
	
	<!-- outputclass "bullet-list-titles" -->
	
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_BULLET_LIST_TITLES]">
		<fo:list-block xsl:use-attribute-sets="ul">
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="dl-bullet-list-titles"/>
		</fo:list-block>
	</xsl:template>

	<xsl:template match="*[contains(@class, ' topic/dlentry ')]" mode="dl-bullet-list-titles">
		<fo:list-item xsl:use-attribute-sets="ul.li">
			<xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="flag-attributes"/>
			<fo:list-item-label xsl:use-attribute-sets="ul.li__label">
				<fo:block xsl:use-attribute-sets="ul.li__label__content">
					<fo:inline>
						<xsl:apply-templates select="@id"/>
						<xsl:call-template name="commonattributes"/>
					</fo:inline>
					<xsl:call-template name="getVariable">
						<xsl:with-param name="id" select="'Unordered List bullet'"/>
					</xsl:call-template>
				</fo:block>
			</fo:list-item-label>
			<fo:list-item-body xsl:use-attribute-sets="ul.li__body">
				<fo:block xsl:use-attribute-sets="ul.li__content">
					<xsl:apply-templates mode="#current"/>
				</fo:block>
			</fo:list-item-body>
		</fo:list-item>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dt ')]" mode="dl-bullet-list-titles">
		
		<fo:block xsl:use-attribute-sets="ds:dt-bullet-list-titles">
			<xsl:if test="exists(following-sibling::*)">
				<xsl:attribute name="keep-with-next.within-column">100</xsl:attribute>
			</xsl:if>
			
			<xsl:call-template name="commonattributes"/>
			
			<xsl:apply-templates mode="#default"/>
			
		</fo:block>
		
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dd ')]" mode="dl-bullet-list-titles">
		
		<fo:block xsl:use-attribute-sets="ds:dd-bullet-list-titles">
			
			<xsl:call-template name="commonattributes"/>
			
			<xsl:apply-templates mode="#default"/>
			
		</fo:block>
		
	</xsl:template>

</xsl:stylesheet>
