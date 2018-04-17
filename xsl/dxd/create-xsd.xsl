<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:jt 	= "http://saxon.sf.net/java-type"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
	xmlns:ikd	= "http://www.dita-semia.org/implicit-keydef"
	xmlns:dxd	= "http://www.dita-semia.org/dynamic-xml-definition"
	exclude-result-prefixes="xs"
	version="2.0">

	<xsl:param name="dxd:rootType"	as="xs:string"/>
	<xsl:param name="dxd:rootName"	as="xs:string"/>
	<xsl:param name="dxd:mapUrl"	as="xs:anyURI?"/>
	
	<xsl:template match="/">
		
		<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified">
			
			<xs:element name="list">
				<xs:complexType>
					<xs:sequence>
						<xs:element name="codeblock" maxOccurs="unbounded" type="codeblockType"/>
					</xs:sequence>
				</xs:complexType>
			</xs:element>
			
			<xs:complexType name="codeblockType">
				<xs:sequence>
					<xs:element name="{$dxd:rootName}" type="{$dxd:rootType}"/>
				</xs:sequence>
				<xs:attribute name="id" type="xs:ID"/>
			</xs:complexType>
			
			<xsl:call-template name="createTypeDefs">
				<xsl:with-param name="undefinedTypeNames"	select="$dxd:rootType"/>
			</xsl:call-template>
			
		</xs:schema>
	</xsl:template>
	
	
	<xsl:template name="createTypeDefs">
		<xsl:param name="definedTypeNames"		as="xs:string*"/>
		<xsl:param name="undefinedTypeNames"	as="xs:string*"/>

		<xsl:variable name="currentTypeName" 				as="xs:string" 	select="$undefinedTypeNames[1]"/>
		
		<xsl:variable name="keyDef" 	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface?" 	select="if (exists($dxd:mapUrl)) then dxd:getKeyDefByTypeName($currentTypeName, $dxd:mapUrl) else ()"/>
		<xsl:variable name="typeDef" 	as="element()?" 											select="if (exists($keyDef)) then dxd:getTypeDef($keyDef) else ()"/>
		
		<xsl:choose>
			<xsl:when test="empty($keyDef)">
				<!--<xsl:message>ERROR: undefined type name '<xsl:value-of select="$currentTypeName"/>' (unknown key).</xsl:message>-->
				<!--<xs:complexType name="{$currentTypeName}" mixed="true">
					<xs:sequence>
						<xs:any minOccurs="0" maxOccurs="unbounded" processContents="skip"/>
					</xs:sequence>
				</xs:complexType>-->
			</xsl:when>
			<xsl:when test="empty($typeDef)">
				<xsl:message>ERROR: no type definition for type name '<xsl:value-of select="$currentTypeName"/>' (key '<xsl:value-of select="ikd:getRefString($keyDef)"/>' defines no type).</xsl:message>
				<xs:complexType name="{$currentTypeName}"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="$typeDef"></xsl:copy-of>
			</xsl:otherwise>
		</xsl:choose>


		<xsl:variable name="referencedTypeNames" 	as="xs:string*" select="distinct-values($typeDef//xs:element/@type)"/>
		<xsl:variable name="newTypeNames" 			as="xs:string*" select="$referencedTypeNames[not(. = ($definedTypeNames, $undefinedTypeNames))]"/>
		<xsl:variable name="missingTypeNames" 		as="xs:string*" select="($undefinedTypeNames[position() > 1] , $newTypeNames)"/>
		
		<xsl:if test="exists($missingTypeNames)">
			<xsl:call-template name="createTypeDefs">
				<xsl:with-param name="definedTypeNames"		select="($definedTypeNames, $currentTypeName)"/>
				<xsl:with-param name="undefinedTypeNames"	select="$missingTypeNames"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	
</xsl:stylesheet>