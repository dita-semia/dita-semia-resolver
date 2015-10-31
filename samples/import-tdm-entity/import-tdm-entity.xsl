<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
    exclude-result-prefixes	= "#all"
	expand-text				= "yes">
	
	<xsl:param name="entityName" as="xs:string?"/>
	
	<xsl:include href="urn:dita-semia:xslt-conref:xsl:class.xsl"/>
	
	<xsl:output method="xml" indent="yes"/>
	
    <xsl:template match="/">


    	<xsl:variable name="entityDef" as="element()?" select="*/Entities/*[Name = $entityName]"/>
    	
    	<xsl:processing-instruction name="xml-model">href="urn:dita-ng:dita:rng:reference.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>
    	
    	<xsl:choose>
    		<xsl:when test="exists($entityDef)">
    			
    			<xsl:variable name="pkId" 				as="xs:string?" select="$entityDef/PK/Id"/>
    			<xsl:variable name="pkAttributeIdList" 	as="xs:string*" select="$entityDef/Keys/*[Id = $pkId]/KeyItems/*/Attribute/Id"/>
    			
    			<reference>
    				<title>Entity: <codeph>{$entityName}</codeph></title>
    				<refbody>
    					<table>
    						<tgroup cols="2">
    							<colspec colname="c1" colnum="1" colwidth="1.0*"/>
    							<colspec colname="c2" colnum="2" colwidth="2.0*"/>
    							<thead>
    								<row>
    									<entry>
    										<p>Column</p>
    									</entry>
    									<entry>
    										<p>Description</p>
    									</entry>
    								</row>
    							</thead>
    							<tbody>
    								<xsl:for-each select="$entityDef/Attributes/*">
    									<xsl:call-template name="CreateAttributeDefinition">
    										<xsl:with-param name="pkAttributeIdList" select="$pkAttributeIdList"/>
    									</xsl:call-template>
    								</xsl:for-each>
    							</tbody>
    						</tgroup>
    					</table>
    				</refbody>
    			</reference>
    			
    		</xsl:when>
    		<xsl:otherwise>
    			<!-- class-attribute is required in this case to be properly displayed in oXygen -->
    			<no-content class="+ topic/topic topic/reference ">No entity with name '{$entityName}' found in data model '{base-uri()}'.</no-content>
    		</xsl:otherwise>
    	</xsl:choose>
    	
    </xsl:template>
	
	
	<xsl:template name="CreateAttributeDefinition">
		<xsl:param name="pkAttributeIdList" as="xs:string*"/>

		<row>
			<entry>
				<p>
					<codeph>{Name}</codeph>
				</p>
				<p>
					<xsl:variable name="propertyList" as="xs:string*">
						
						<!-- Datatype -->
						<xsl:value-of select="ds:getTdmDataTypeName(.)"/>
						
						<!-- Primary-Key index -->
						<xsl:variable name="pkIndex" as="xs:integer?" select="index-of($pkAttributeIdList, Id)"/>
						<xsl:if test="$pkIndex">PK-{$pkIndex}</xsl:if>
						
						<!-- Not-Null-Constraint -->
						<xsl:if test="NotNull = '1'">NN</xsl:if>
						
					</xsl:variable>
					
					<xsl:text>({string-join($propertyList, ', ')})</xsl:text>
				</p>
			</entry>
			<entry>
				<p>{Comments}</p>
			</entry>
		</row>
	</xsl:template>
	
	
	<xsl:function name="ds:getTdmDataTypeName" as="xs:string?">
		<xsl:param name="attribute" as="element()"/>
		<xsl:choose>
			<xsl:when
				test="$attribute/DataType/Id = ('{815A3856-57FA-46D2-8970-F8EE7155A7DF}', '{BBFB1737-C6F1-4CBB-8C6D-5D5AB9D6AB62}')">
				<xsl:value-of select="'Integer'"/>
			</xsl:when>
			<xsl:when
				test="$attribute/DataType/Id = ('{AFC276D3-C902-4173-9A79-50450B59BA55}', '{E08B7501-7E55-45B2-8AD4-C7DAA0D733EE}', '{DAFC6D07-DE9A-493B-8BB1-A0CD1A921047}')">
				<xsl:value-of select="'Date'"/>
			</xsl:when>
			<xsl:when
				test="$attribute/DataType/Id = ('{DC51FDE1-D7B0-428E-A7BE-BEC8590B2728}', '{5D1A1991-9D10-4025-BC17-465FD4168632}')">
				<xsl:value-of select="concat('Varchar2(', $attribute/DataTypeParam1, ')')"/>
			</xsl:when>
			<xsl:when
				test="$attribute/DataType/Id = ('{414D5D15-0528-46ED-87FF-4289F9388981}', '{256483BC-E92D-43CA-A6B1-F87D1314E701}')">
				<xsl:value-of select="concat('Char(', $attribute/DataTypeParam1, ')')"/>
			</xsl:when>
			<xsl:when
				test="$attribute/DataType/Id = ('{7600CED9-B2EE-4545-A810-6673262A98EE}', '{C8855472-5055-41A3-AD96-E5EF19EFB6D0}')">
				<xsl:value-of select="concat('Timestamp(', $attribute/DataTypeParam1, ')')"/>
			</xsl:when>
			<xsl:when
				test="$attribute/DataType/Id = ('{803316E4-EE78-40A3-B9F7-2BCB6FEEC2A0}', '{580A4E46-9EE4-44CF-962F-0182748C71C9}')">
				<xsl:value-of select="'Clob'"/>
			</xsl:when>
			<xsl:when test="$attribute/DataType/Id = ('{88E4D8D8-31C5-409F-AB4D-A24CC6D8AF83}')">
				<xsl:value-of select="'Blob'"/>
			</xsl:when>
			<xsl:when test="($attribute/DataType/Id = ('{2418B677-05B1-4A57-B578-9FE46DC48D3C}', '{3A22E4F9-EE24-4A39-835D-62C3EF76CAA4}', '{71878A50-2C20-4C76-BC9B-314FA444D2B6}'))">
				<xsl:choose>
					<xsl:when test="string($attribute/DataTypeParam2) = ('0', '')">
						<xsl:value-of select="concat('Number(', $attribute/DataTypeParam1, ')')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat('Number(', $attribute/DataTypeParam1, ',', $attribute/DataTypeParam2, ')')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$attribute/DataType/Id = ('{7B430245-0252-45B4-9277-3AAE6AE9EDEC}')">
				<xsl:value-of select="'Number'"/>
			</xsl:when>
			<xsl:when test="$attribute/DataType/Id = ('{D20A9267-A876-4626-B49F-C0C023C458BE}', '{CBEF261C-5AD0-4E2B-9874-E94CFC394547}')">
				<xsl:value-of select="'Float'"/>
			</xsl:when>
			<xsl:when test="$attribute/DataType/Id = ('{1F606032-4339-4379-AB0F-2F6C8DCB9214}')">
				<xsl:value-of select="'AnyType'"/>
			</xsl:when>
			<xsl:when test="$attribute/DataType/Id = ('{1169186B-F79A-491A-B34A-E9A535D9A8D0}')">
				<xsl:choose>
					<xsl:when test="exists($attribute/DataTypeParam1/text())">
						<xsl:value-of select="concat('Raw(', $attribute/DataTypeParam1, ')')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="'Raw'"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:message>ERROR: Unkown datentyp with ID '<xsl:value-of select="$attribute/DataType/Id"/>' in file '<xsl:value-of select="base-uri($attribute)"/>'</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
</xsl:stylesheet>
