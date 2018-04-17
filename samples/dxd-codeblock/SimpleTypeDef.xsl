<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
	xmlns:dxd	= "http://www.dita-semia.org/dynamic-xml-definition"
	xmlns:jt 	= "http://saxon.sf.net/java-type"
	exclude-result-prefixes="#all"
	version="2.0">

	<xsl:param name="dxd:typeName" 	as="xs:string"/>
	<xsl:param name="dxd:defUrl" 	as="xs:anyURI"/>
	
	<xsl:template match="*">
		<xs:simpleType name="{$dxd:typeName}">
			<xs:restriction base="xs:string">
				<xsl:variable name="maxLength" as="xs:string?" select="dd/ph[1]"/>
				<xsl:choose>
					<xsl:when test="matches($maxLength, '^[0-9]+$')">
						<xs:maxLength value="{$maxLength}"></xs:maxLength>
					</xsl:when>
					<xsl:otherwise>
						<xsl:message>ERROR: invalid max length '<xsl:value-of select="$maxLength"/>' in DXD type <xsl:value-of select="$dxd:typeName"/></xsl:message>
					</xsl:otherwise>
				</xsl:choose>
			</xs:restriction>
		</xs:simpleType>
	</xsl:template>

</xsl:stylesheet>
