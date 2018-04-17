<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet exclude-result-prefixes="#all" version="2.0"
	xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" xmlns:ds="org.dita-semia.resolver"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:opentopic="http://www.idiominc.com/opentopic"
	xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	
	<xsl:variable name="DL_OUTPUTCLASS_BULLET_LIST_TITLES" as="xs:string">bullet-list-titles</xsl:variable>
	
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_BULLET_LIST_TITLES]">
		<ul>
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates select="@compact"/>
			<xsl:apply-templates mode="dl-bullet-list-titles"/>
		</ul>
		<xsl:value-of select="$newline"/>
	</xsl:template>

	<xsl:template match="*[contains(@class, ' topic/dlentry ')]" mode="dl-bullet-list-titles">
		<li>
			<xsl:call-template name="commonattributes"/>
			<xsl:call-template name="setidaname"/>
			<xsl:apply-templates mode="#current"/>
		</li>
	</xsl:template>
	
	<xsl:template match="*[contains(@class, ' topic/dt ')] | *[contains(@class, ' topic/dd ')]" mode="dl-bullet-list-titles">
		<div>
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="#default"/>
		</div>
	</xsl:template>
	
	
	<!-- mode: get-output-class -->
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_BULLET_LIST_TITLES]" mode="get-output-class">
		<xsl:text>ul</xsl:text>
		<xsl:next-match/>
	</xsl:template>
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_BULLET_LIST_TITLES]/*[contains(@class, ' topic/dlentry ')]" mode="get-output-class">
		<xsl:text>li</xsl:text>
		<xsl:next-match/>
	</xsl:template>
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_BULLET_LIST_TITLES]/*/*[contains(@class, ' topic/dt ')]" mode="get-output-class">
		<xsl:text>p</xsl:text>
		<xsl:next-match/>
	</xsl:template>
	
</xsl:stylesheet>
