<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
	exclude-result-prefixes="#all">
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:param name="xcr:current" as="element()"/>
	
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	
	
	<xsl:template match="/">
		
		<xsl:variable name="fieldRef" 	as="element()?"	select="$xcr:current/ancestor::row/entry[1]//key-xref"/>
		<xsl:variable name="fieldDef" 	as="element()?" select="akr:getKeyDefRoot($fieldRef)"/>
		<xsl:message select="$fieldDef"></xsl:message>
		
		<xsl:choose>
			<xsl:when test="exists($fieldDef)">
				<div class="{$CP_DIV}">
					<xsl:variable name="typeInfo" as="element()*" select="$fieldDef/entry[2]/element()"/>
					<xsl:choose>
						<xsl:when test="contains($typeInfo[1]/@class, ' topic/p ')">
							<xsl:for-each select="$typeInfo[1]">
								<xsl:copy>
									<xsl:copy-of select="attribute()"/>
									<xsl:text>Type: </xsl:text>
									<xsl:copy-of select="node()"/>
								</xsl:copy>
							</xsl:for-each>
							<xsl:copy-of select="$typeInfo[position() > 1]"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:copy-of select="$fieldDef/entry[2]/node()"/>
						</xsl:otherwise>
					</xsl:choose>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<no-content>(undefined field <codeph class="{$CP_CODEPH}"><xsl:value-of select="$fieldRef"/></codeph>)</no-content>
			</xsl:otherwise>
		</xsl:choose>
		
		
		
	</xsl:template>
</xsl:stylesheet>