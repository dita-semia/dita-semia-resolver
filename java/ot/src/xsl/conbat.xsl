<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:cba	= "http://www.dita-semia.org/conbat"
	exclude-result-prefixes		= "#all">
	
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	
	<xsl:variable name="CBA_MARKER" as="processing-instruction()">
		<xsl:processing-instruction name="CBA"/>
	</xsl:variable>


	<!-- paragraph-prefix -->
	<xsl:template match="*[@cba:prefix][contains(@class, $C_UL) or contains(@class, $C_OL) or contains(@class, $C_SL)]" priority="8">
		<p class="{$CP_P}">
			<xsl:sequence select="$CBA_MARKER"/>
			<xsl:value-of select="cba:resolveEmbeddedXPath(@cba:prefix)"/>
		</p>
		<xsl:next-match/>
	</xsl:template>
	

	<!-- paragraph-suffix -->
	<xsl:template match="*[@cba:suffix][contains(@class, $C_UL) or contains(@class, $C_OL) or contains(@class, $C_SL)]" priority="7">
		<xsl:next-match/>
		<p class="{$CP_P}">
			<xsl:sequence select="$CBA_MARKER"/>
			<xsl:value-of select="cba:resolveEmbeddedXPath(@cba:suffix)"/>
		</p>
	</xsl:template>
	
	
	<!-- title -->
	<xsl:template match="*[@cba:title]" priority="6">
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<title class="{$CP_TITLE}">
				<xsl:sequence select="$CBA_MARKER"/>
				<ph class="{$CP_PH}">
					<xsl:sequence select="$CBA_MARKER"/>
					<xsl:value-of select="cba:resolveEmbeddedXPath(@cba:title)"/>
				</ph>
			</title>
			<xsl:apply-templates select="node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>


	<!-- inline-codeph-content -->
	<xsl:template match="*[contains(@class, $C_CODEPH)]" priority="6">
		<xsl:sequence select="ds:createCbaPhrase(@cba:prefix)"/>
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:code-prefix)"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:content)"/>
			<xsl:choose>
				<xsl:when test="empty(node())">
					<xsl:sequence select="ds:createCbaPhrase(@cba:default-content)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="node()" mode="#current"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:sequence select="ds:createCbaPhrase(@cba:code-suffix)"/>
		</xsl:copy>
		<xsl:sequence select="ds:createCbaPhrase(@cba:suffix)"/>
	</xsl:template>
	
	
	<!-- inline-content -->
	<xsl:template match="*[contains(@class, $C_P) or contains(@class, $C_PH) or contains(@class, $C_SLI) or contains(@class, $C_STENTRY) or contains(@class, $C_TITLE)]" priority="5">
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:prefix)"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:content)"/>
			<xsl:choose>
				<xsl:when test="empty(node())">
					<xsl:sequence select="ds:createCbaPhrase(@cba:default-content)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="node()" mode="#current"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:sequence select="ds:createCbaPhrase(@cba:suffix)"/>
		</xsl:copy>
	</xsl:template>


	<!-- table-header -->
	<xsl:template match="*[@cba:header][contains(@class, $C_TGROUP)]" priority="5">
		<xsl:copy>
			<xsl:variable name="resolvedHeader" as="xs:string" select="cba:resolveEmbeddedXPath(@cba:header)"/>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:apply-templates select="*[contains(@class, $C_COLSPEC)]" mode="#current"/>
			<thead class="{$CP_THEAD}">
				<xsl:sequence select="$CBA_MARKER"/>
				<row class="{$CP_ROW}">
					<xsl:sequence select="$CBA_MARKER"/>
					<xsl:for-each select="tokenize($resolvedHeader, '[|]')">
						<entry class="{$CP_ENTRY}">
							<xsl:sequence select="$CBA_MARKER"/>
							<xsl:value-of select="."/>
						</entry>
					</xsl:for-each>
				</row>
			</thead>
			<xsl:apply-templates select="node() except *[contains(@class, $C_COLSPEC)]" mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
	
	<!-- simpletable-header -->
	<xsl:template match="*[@cba:header][contains(@class, $C_SIMPLETABLE)]" priority="5">
		<xsl:copy>
			<xsl:variable name="resolvedHeader" as="xs:string" select="cba:resolveEmbeddedXPath(@cba:header)"/>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<sthead class="{$CP_STHEAD}">
				<xsl:sequence select="$CBA_MARKER"/>
				<xsl:for-each select="tokenize($resolvedHeader, '[|]')">
					<stentry class="{$CP_STENTRY}">
						<xsl:sequence select="$CBA_MARKER"/>
						<xsl:value-of select="."/>
					</stentry>
				</xsl:for-each>
			</sthead>
			<xsl:apply-templates select="node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>




	<xsl:function name="ds:createCbaPhrase">
		<xsl:param name="attribute" as="attribute()?"/>
		
		<xsl:if test="exists($attribute)">
			<ph class="{$CP_PH}">
				<xsl:sequence select="$CBA_MARKER"/>
				<xsl:for-each select="$attribute/parent::*">	<!-- set context -->
					<xsl:value-of select="cba:resolveEmbeddedXPath($attribute)"/>
				</xsl:for-each>
			</ph>
		</xsl:if>
	</xsl:function>
	
</xsl:stylesheet>
