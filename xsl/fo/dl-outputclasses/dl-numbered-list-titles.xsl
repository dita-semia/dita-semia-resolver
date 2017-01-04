<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet exclude-result-prefixes="#all" version="2.0"
	xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" xmlns:ds="org.dita-semia.resolver"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:opentopic="http://www.idiominc.com/opentopic"
	xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	
	<xsl:variable name="DL_OUTPUTCLASS_NUMBERED_LIST_TITLES" as="xs:string">numbered-list-titles</xsl:variable>
	
	<!-- outputclass "numbered-list-titles" -->
	
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_NUMBERED_LIST_TITLES]">
		<fo:list-block xsl:use-attribute-sets="ds:dl-numbered-list-titles">
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="dl-numbered-list-titles"/>
		</fo:list-block>
	</xsl:template>

	<xsl:template match="*[contains(@class, ' topic/dlentry ')]" mode="dl-numbered-list-titles">
		<fo:list-item xsl:use-attribute-sets="ol.li">
			<xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="flag-attributes"/>
			<fo:list-item-label xsl:use-attribute-sets="ol.li__label">
				<fo:block xsl:use-attribute-sets="ds:dlentry-numbered-list-titles-label-content">
					<fo:inline>
						<xsl:copy-of select="@id"/>
						<xsl:call-template name="commonattributes"/>
					</fo:inline>
					<xsl:call-template name="getVariable">
						<xsl:with-param name="id" select="'Ordered List Number'"/>
						<xsl:with-param name="params">
							<number>
								<xsl:number/>
							</number>
						</xsl:with-param>
					</xsl:call-template>
				</fo:block>
			</fo:list-item-label>
			<fo:list-item-body xsl:use-attribute-sets="ol.li__body">
				<fo:block xsl:use-attribute-sets="ol.li__content">
					<xsl:apply-templates mode="#current"/>
				</fo:block>
			</fo:list-item-body>
		</fo:list-item>
	</xsl:template>
	
	<xsl:template match="*[contains(@class, ' topic/dt ')]" mode="dl-numbered-list-titles">
		
		<fo:block xsl:use-attribute-sets="ds:dt-numbered-list-titles">
			<xsl:if test="exists(following-sibling::*)">
				<xsl:attribute name="keep-with-next.within-column">100</xsl:attribute>
			</xsl:if>
			
			<xsl:call-template name="commonattributes"/>
			
			<xsl:apply-templates mode="#default"/>
			
		</fo:block>
		
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dd ')]" mode="dl-numbered-list-titles">
		
		<fo:block xsl:use-attribute-sets="ds:dd-numbered-list-titles">
			
			<xsl:call-template name="commonattributes"/>
			
			<xsl:apply-templates mode="#default"/>
			
		</fo:block>
		
	</xsl:template>

</xsl:stylesheet>
