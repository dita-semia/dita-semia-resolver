<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="2.0" 
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs	="http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	exclude-result-prefixes="#all">
	
	
	<!-- ========== Function: ds:xmlSerialize ========== -->
	<xsl:function name="ds:xmlSerialize" as="xs:string">
		<xsl:param name="xml"					as="node()*"/>
		<xsl:param name="singlelIndentString"	as="xs:string"/>
		<xsl:param name="maxLineLen"			as="xs:integer?"/>

		<xsl:variable name="Value">
			<xsl:for-each select="$xml">
				<xsl:if test="position() > 1">
					<xsl:text>&#x0A;</xsl:text>
				</xsl:if>
				<xsl:apply-templates select="." mode="XmlSerialize">
					<xsl:with-param name="singlelIndentString" 	select="$singlelIndentString"/>
					<xsl:with-param name="maxLineLen" 			select="$maxLineLen"/>
				</xsl:apply-templates>
			</xsl:for-each>
		</xsl:variable>
		<xsl:value-of select="$Value"/>
	</xsl:function>
	
	
	<!-- ===================================================== -->
	<!-- ========== Templates for mode XmlSerialize ========== -->	

	<xsl:template match="element()" mode="XmlSerialize">
		<xsl:param name="singlelIndentString" 		as="xs:string"/>
		<xsl:param name="fullIndentString" 			as="xs:string?"/>
		<xsl:param name="maxLineLen"				as="xs:integer?"/>
		
		<xsl:variable name="quot">"</xsl:variable>
		
		<xsl:variable name="elementString">
			<xsl:value-of select="concat('&lt;', name())"/>
			<xsl:for-each select="@*">
				<xsl:value-of select="concat(' ', name(), '=', $quot, ., $quot)"/>
			</xsl:for-each>
		</xsl:variable>
		
		<xsl:variable name="childFullIndentString" select="concat($singlelIndentString, $fullIndentString)"/>
		
		<xsl:variable name="content">
			<xsl:choose>
				<xsl:when test="element()">
					<xsl:for-each select="element() | text()[not(matches(., '^[\s]+$'))]">
						<xsl:text>&#xA;</xsl:text>
						<xsl:value-of select="$childFullIndentString"/>
						<xsl:apply-templates select="." mode="XmlSerialize">
							<xsl:with-param name="singlelIndentString" 	select="$singlelIndentString"/>
							<xsl:with-param name="fullIndentString" 	select="$childFullIndentString"/>
							<xsl:with-param name="maxLineLen" 			select="$maxLineLen"/>
						</xsl:apply-templates>
					</xsl:for-each>
					<xsl:text>&#xA;</xsl:text>
					<xsl:value-of select="$fullIndentString"/>
				</xsl:when>
				<xsl:when test="not(text())">
					<!-- no content -> no output -->
				</xsl:when>
				<xsl:otherwise>
					
					<xsl:variable name="singleLineString" 	as="xs:string" select="concat($fullIndentString, $elementString, '&gt;', text(), '&lt;/', name(), '&gt;')"/>
					<xsl:variable name="outputString"		as="xs:string" select="ds:xmlQuote(text())"/>
					
					<xsl:choose>
						<xsl:when test="exists($maxLineLen) and (string-length($singleLineString) > $maxLineLen)">
							<xsl:value-of select="concat('&#xA;', $childFullIndentString, $outputString, '&#xA;', $fullIndentString)"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$outputString"/>
						</xsl:otherwise>
					</xsl:choose>
					
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="$content/node()">
				<xsl:value-of select="concat($elementString, '&gt;')"/>
				<xsl:sequence select="$content"/>
				<xsl:value-of select="concat('&lt;/', name(), '&gt;')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat($elementString, '/&gt;')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- No indention for root element -->
	<xsl:template match="/" mode="XmlSerialize">
		<xsl:param name="singlelIndentString" 	as="xs:string"/>
		<xsl:param name="maxLineLen"			as="xs:integer?"/>
		
		<xsl:for-each select="element()">
			<xsl:if test="position() > 0">
				<xsl:text>&#xA;</xsl:text>
			</xsl:if>
			<xsl:apply-templates select="." mode="XmlSerialize">
				<xsl:with-param name="singlelIndentString" 	select="$singlelIndentString"/>
				<xsl:with-param name="maxLineLen" 			select="$maxLineLen"/>
			</xsl:apply-templates>
		</xsl:for-each>
	</xsl:template>
	
	<!-- Copy text content unless it consists of only whilespaces -->
	<xsl:template match="text()" mode="XmlSerialize">
		<xsl:if test="not(matches(., '^[\s]+$'))">
			<xsl:value-of select="."/>
		</xsl:if>
	</xsl:template>
	
	<!-- ignore these items -->
	<xsl:template match="attribute() | comment() | processing-instruction()" mode="XmlSerialize"/>


	<!-- ========== Funktion: ds:xmlQuote ========== -->
	<xsl:function name="ds:xmlQuote" as="xs:string?">
		<xsl:param name="string" as="xs:string"/>

		<xsl:variable name="apos" as="xs:string">'</xsl:variable>
		<xsl:variable name="quot" as="xs:string">"</xsl:variable>
		<xsl:value-of select="replace(replace(replace(replace($string, '&gt;', '&amp;gt;'), '&lt;', '&amp;lt;'), $quot, '&amp;quot;'), $apos, '&amp;apos;')"/>
	</xsl:function>
	
</xsl:transform>
