<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi	= "http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:dxd	= "http://www.dita-semia.org/dynamic-xml-definition"
	exclude-result-prefixes="#all"
	version="2.0">
	
	<xsl:param name="dxd:rootType"	as="xs:string"/>
	<xsl:param name="dxd:rootName"	as="xs:string"/>
	<xsl:param name="dxd:newId"		as="xs:string"/>
	<xsl:param name="dxd:schema"	as="document-node()"/>
	
	<xsl:key name="type" match="/xs:schema/xs:complexType | /xs:schema/xs:simpleType" use="@name"/>
	
	<xsl:output method="xml" indent="no"/>

	<xsl:template name="create-codeblock">
		<codeblock id="{$dxd:newId}">
			<xsl:call-template name="createElement">
				<xsl:with-param name="name"		select="$dxd:rootName"/>
				<xsl:with-param name="typeName"	select="$dxd:rootType"/>
			</xsl:call-template>
		</codeblock>
	</xsl:template>
	
	<xsl:template name="createElement">
		<xsl:param name="typePath"	as="xs:string*"/>
		<xsl:param name="name"		as="xs:string?"/>
		<xsl:param name="typeName"	as="xs:string?"/>
		<xsl:param name="typeDef"	as="element()?"/>
		
		<xsl:if test="$name">
			<xsl:element name="{$name}">
				<xsl:apply-templates select="if ($typeDef) then $typeDef else if ($typeName) then key('type', $typeName, $dxd:schema) else ()" mode="initContent"/>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	
	<xsl:template match="xs:simpleType" mode="initContent">
		<xsl:value-of select="processing-instruction('dxd-init')"/>
	</xsl:template>
	
	
	<xsl:template match="xs:complexType" mode="initContent">
		<xsl:param name="typePath"	as="xs:string*"/>

		<xsl:if test="not(@name = $typePath)">	<!-- avoid recursion -->
			<!-- handle attributes first -->
			<xsl:apply-templates select="xs:attribute, xs:* except xs:attribute" mode="#current">
				<xsl:with-param name="typePath" select="($typePath, @name)"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>
	
	
	<xsl:template match="xs:sequence | xs:all" mode="initContent">
		<xsl:param name="typePath"	as="xs:string*"/>
		
		<xsl:apply-templates select="xs:*" mode="#current">
			<xsl:with-param name="typePath" select="$typePath"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="xs:any" mode="initContent">
		<any-content/>
	</xsl:template>
	
	<xsl:template match="xs:element" mode="initContent">
		<xsl:param name="typePath"	as="xs:string*"/>
		
		<xsl:call-template name="createElement">
			<xsl:with-param name="name"		select="@name"/>	<!-- @ref not supported yet -->
			<xsl:with-param name="typeName"	select="@type"/>
			<xsl:with-param name="typeDef"	select="xs:simpleType | xs:complexType"/>
		</xsl:call-template>
	</xsl:template>
	
	
	<xsl:template match="*" mode="initContent">
		<xsl:message>WARNING: initContent not implemented for element '<xsl:value-of select="name(.)"/>'.</xsl:message>
	</xsl:template>
	
</xsl:stylesheet>
