<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet version="2.0"
	xmlns:xs				= "http://www.w3.org/2001/XMLSchema" 
	xmlns:xsl				= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo				= "http://www.w3.org/1999/XSL/Format"
	xmlns:ds				= "http://www.dita-semia.org"
	exclude-result-prefixes	= "#all">
	
	
	<xsl:variable name="DL_OUTPUTCLASS_BULLET_LIST_DASHES" as="xs:string">bullet-list-dashes</xsl:variable>
	
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_BULLET_LIST_DASHES]">
		<fo:list-block xsl:use-attribute-sets="ul">
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="dl-bullet-list-dashes"/>
		</fo:list-block>
	</xsl:template>

	<xsl:template match="*[contains(@class, ' topic/dlentry ')]" mode="dl-bullet-list-dashes">
		<fo:list-item xsl:use-attribute-sets="ul.li">
			<xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="flag-attributes"/>
			<fo:list-item-label xsl:use-attribute-sets="ul.li__label">
				<fo:block xsl:use-attribute-sets="ul.li__label__content">
					<fo:inline>
						<xsl:copy-of select="@id"/>
						<xsl:call-template name="commonattributes"/>
					</fo:inline>
					<xsl:call-template name="getVariable">
						<xsl:with-param name="id" select="'Unordered List bullet'"/>
					</xsl:call-template>
				</fo:block>
			</fo:list-item-label>
			<fo:list-item-body xsl:use-attribute-sets="ul.li__body">
				<fo:block xsl:use-attribute-sets="ul.li__content">
					<xsl:apply-templates select="*[contains(@class, ' topic/dt ')]" mode="#current"/>
					<fo:inline> &#x2013; </fo:inline>
					<xsl:apply-templates select="*[contains(@class, ' topic/dd ')]" mode="#current"/>
				</fo:block>
			</fo:list-item-body>
		</fo:list-item>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dt ')]" mode="dl-bullet-list-dashes">
		
		<fo:inline xsl:use-attribute-sets="ds:dt-bullet-list-dashes">
			
			<xsl:call-template name="commonattributes"/>
			
			<xsl:apply-templates mode="#default"/>
			
		</fo:inline>
		
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dd ')]" mode="dl-bullet-list-dashes">
		
		<fo:inline xsl:use-attribute-sets="ds:dd-bullet-list-dashes">
			
			<xsl:call-template name="commonattributes"/>
			
			<xsl:apply-templates mode="#default"/>
			
		</fo:inline>
		
	</xsl:template>

</xsl:stylesheet>
