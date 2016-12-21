<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet exclude-result-prefixes="ditaarch opentopic ds" version="2.0"
	xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" 
	xmlns:ds="org.dita-semia.resolver"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" 
	xmlns:opentopic="http://www.idiominc.com/opentopic"
	xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	
	
	<xsl:template name="remove-outer-space">
		<xsl:param name="content" 	as="node()*"/>
		<xsl:param name="isFirst"	as="xs:boolean"	select="true()"/>
		<xsl:param name="isLast"	as="xs:boolean"	select="true()"/>
		
		<!-- ignore first and last whitespace text node -->
		<xsl:for-each select="$content[not((position() = (1, last())) and self::text()[matches(., '^\s+$')])]">
			<xsl:apply-templates select="." mode="remove-outer-space">
				<xsl:with-param name="isFirst" 	select="($isFirst) 	and (position() = 1)"/>
				<xsl:with-param name="isLast" 	select="($isLast)	and (position() = last())"/>
			</xsl:apply-templates>
		</xsl:for-each>
	</xsl:template>
	
	
	<xsl:template match="attribute() | node()" mode="remove-outer-space">
		<xsl:param name="isFirst"	as="xs:boolean"/>
		<xsl:param name="isLast"	as="xs:boolean"/>
		
		<xsl:choose>
			<xsl:when test="not($isFirst) and not($isLast)">
				<!-- no further processing required -->
				<xsl:copy-of select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="attribute()" mode="#current">
						<xsl:with-param name="isFirst" 	select="$isFirst"/>
						<xsl:with-param name="isLast" 	select="$isLast"/>
					</xsl:apply-templates>
					<xsl:call-template name="remove-outer-space">
						<xsl:with-param name="content" 	select="node()"/>
						<xsl:with-param name="isFirst" 	select="$isFirst"/>
						<xsl:with-param name="isLast" 	select="$isLast"/>
					</xsl:call-template>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<xsl:template match="@space-before" mode="remove-outer-space">
		<xsl:param name="isFirst"	as="xs:boolean"/>
		<xsl:if test="not($isFirst)">
			<xsl:copy/>
		</xsl:if>
	</xsl:template>
	
	
	<xsl:template match="@space-after" mode="remove-outer-space">
		<xsl:param name="isLast"	as="xs:boolean"/>
		<xsl:if test="not($isLast)">
			<xsl:copy/>
		</xsl:if>
	</xsl:template>
	
	
</xsl:stylesheet>
