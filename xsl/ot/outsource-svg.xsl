<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:svg	= "http://www.w3.org/2000/svg"
	exclude-result-prefixes		= "#all">


	
	<xsl:template match="*[contains(@class, $C_SVG_CONTAINER)][svg:svg]" mode="outsource-svg">
		<xsl:choose>
			<xsl:when test="$outsource-svg">
				<xsl:for-each select="svg:svg">
					<xsl:variable name="filename" as="xs:string?" select="ds:getSvgFilename(.)"/>
					<xsl:choose>
						<xsl:when test="$filename">
							<image class="{$CP_IMAGE}" href="{$filename}"/>
							<xsl:result-document href="{ds:getOutsourcedSvgUri(resolve-uri($filename, $baseUri))}" method="xml" indent="no">
								<xsl:copy-of select="."/>
							</xsl:result-document>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="DotXMessage">
								<xsl:with-param name="type"	select="'WARN'"/>
								<xsl:with-param name="message">Could not outsource SVG since no filename is specified.</xsl:with-param>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<xsl:function name="ds:getSvgFilename">
		<xsl:param name="svgElement" as="element()"/>
		<xsl:value-of select="$svgElement/processing-instruction('filename')"/>
	</xsl:function>
	
</xsl:stylesheet>
