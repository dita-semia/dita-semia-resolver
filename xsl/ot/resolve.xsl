<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
	exclude-result-prefixes		= "#all">
	
	<xsl:import href="../common/resolve-akr.xsl"/>	<!-- overwrite error handling -->
	
	<xsl:include href="common.xsl"/>
 	<xsl:include href="xslt-conref.xsl"/>
	<xsl:include href="conbat.xsl"/>
	<!--<xsl:include href="advanced-keyref.xsl"/>-->
	<xsl:include href="outsource-svg.xsl"/>
	<xsl:include href="../dxd/resolve-codeblock.xsl"/>
	<xsl:include href="extension-functions.xsl"/>
	
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	<xsl:include href="urn:dita-semia:xsl:cba-const.xsl"/>
	
	<xsl:param name="basedir"			as="xs:string"/>
	<xsl:param name="outsource-svg"		as="xs:boolean"/>
	<xsl:param name="wrap-cba-ph"		as="xs:boolean"/>
	<xsl:param name="dxd-indent"		as="xs:string"/>
	<xsl:param name="dxd-max-width"		as="xs:integer"/>
	<xsl:param name="dxd-markup"		as="xs:boolean"/>
	
	<xsl:variable name="baseUri" as="xs:anyURI" select="base-uri(.)"/>

	<xsl:template match="document-node()">
		<!--<xsl:message>version: <xsl:value-of select="system-property('xsl:product-version')"/></xsl:message>-->
		<!--<xsl:message>basedir:       <xsl:value-of select="$basedir"/></xsl:message>
		<xsl:message>outsource-svg: <xsl:value-of select="$outsource-svg"/></xsl:message>
		<xsl:message>wrap-cba-ph: 	<xsl:value-of select="$wrap-cba-ph"/></xsl:message>
		<xsl:message>baseUri:       <xsl:value-of select="$baseUri"/></xsl:message>-->
		
		<xsl:variable name="resolved1" as="document-node()">
			<xsl:apply-templates select="." mode="resolve-xcr"/>
		</xsl:variable>
		
		<xsl:variable name="resolved2" as="document-node()">
			<xsl:apply-templates select="$resolved1" mode="resolve-akr"/>
		</xsl:variable>
		
		<xsl:variable name="resolved3" as="document-node()">
			<xsl:apply-templates select="$resolved2" mode="resolve-cba"/>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="$outsource-svg">
				<xsl:apply-templates select="$resolved3" mode="outsource-svg"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="$resolved3"/>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' ds-d/dxd-codeblock ')]" mode="resolve-cba">
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:variable name="resolved" as="element()?">
				<xsl:call-template name="resolve-codeblock">
					<xsl:with-param name="baseUri"	select="ds:getFixedBaseUri(.)"/>
					<xsl:with-param name="coderef"	select="@coderef"/>
					<xsl:with-param name="indent"	select="$dxd-indent"/>
					<xsl:with-param name="maxWidth"	select="$dxd-max-width"/>
					<xsl:with-param name="markup"	select="$dxd-markup"/>
				</xsl:call-template>
			</xsl:variable>
			
			<xsl:choose>
				<xsl:when test="$resolved/self::ERROR">
					<xsl:call-template name="DotXMessage">
						<xsl:with-param name="type"	select="'ERROR'"/>
						<xsl:with-param name="message">Failed to resolve dxd-codeblock: <xsl:value-of select="$resolved"/>.</xsl:with-param>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$resolved/node()"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	
	
	<xsl:template match="document-node() | element()" mode="resolve-xcr resolve-cba outsource-svg">
		<xsl:copy>
			<xsl:apply-templates select="attribute() | node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/*" mode="resolve-xcr resolve-akr resolve-cba outsource-svg">
		<xsl:copy>
			<xsl:call-template name="setBaseUri"/>
			<xsl:apply-templates select="attribute() | node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>

	<!--<xsl:template match="*" mode="resolve-akr" priority="100">
		<xsl:message select="name(.)"/>
		<xsl:next-match/>
	</xsl:template>-->
	
	<xsl:template match="attribute() | processing-instruction() | text() | comment()"  mode="resolve-xcr resolve-cba outsource-svg">
		<xsl:copy/>
	</xsl:template>
	
	<xsl:template name="setBaseUri" as="attribute()?">
		<xsl:if test="parent::document-node()">
			<xsl:attribute name="xml:base" select="base-uri(.)"/>
		</xsl:if>
	</xsl:template>
	
	
	
	
	<xsl:template name="akr:NoLink">
		<xsl:param name="keyRef"	as="element()"/>
		
		<xsl:call-template name="DotXMessage">
			<xsl:with-param name="type"	select="'WARN'"/>
			<xsl:with-param name="message">Failed to resolve advanced-keyref to xref (<xsl:value-of select="$keyRef/@akr:ref"/>).</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>
