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
		<xs:complexType name="{$dxd:typeName}">
			<xs:sequence>
				<xsl:for-each select="dd/sl/sli/key-xref">
					<xs:element name="{.}">
						<xsl:variable name="keyDef" 	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?" select="akr:getKeyDefByRefString(@akr:ref, $dxd:defUrl)"/>
						<xsl:variable name="typeName" 	as="xs:string?" select="if (exists($keyDef)) then dxd:getTypeName($keyDef) else ()"/>
						<xsl:choose>
							<xsl:when test="empty($keyDef)">
								<xsl:message>ERROR: No key definition for '<xsl:value-of select="."/>' in DXD type <xsl:value-of select="$dxd:typeName"/>.</xsl:message>
							</xsl:when>
							<xsl:when test="empty($typeName)">
								<xsl:message>FEHLER: No DXD type definition for '<xsl:value-of select="."/>' in DXD type <xsl:value-of select="$dxd:typeName"/>.</xsl:message>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="type" select="$typeName"/>
							</xsl:otherwise>
						</xsl:choose>
					</xs:element>
				</xsl:for-each>
			</xs:sequence>
		</xs:complexType>
	</xsl:template>

</xsl:stylesheet>
