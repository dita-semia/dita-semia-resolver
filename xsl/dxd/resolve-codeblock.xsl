<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:dxd	= "http://www.dita-semia.org/dynamic-xml-definition"
	exclude-result-prefixes="#all"
	version="2.0">
	
	<xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

	<!-- parameters for resolving from oXygen -->
	<xsl:param name="dxd:baseUri"	as="xs:anyURI?"/>
	<xsl:param name="dxd:coderef"	as="xs:string?"/>
	<xsl:param name="dxd:indent"	as="xs:string?"/>
	<xsl:param name="dxd:maxWidth"	as="xs:integer?"/>
	<xsl:param name="dxd:markup"	as="xs:boolean?"/>
	
	<xsl:template name="resolve-codeblock">
		<xsl:param name="baseUri"	as="xs:anyURI"	select="$dxd:baseUri"/>
		<xsl:param name="coderef"	as="xs:string"	select="$dxd:coderef"/>
		<xsl:param name="indent"	as="xs:string"	select="$dxd:indent"/>
		<xsl:param name="maxWidth"	as="xs:integer"	select="$dxd:maxWidth"/>
		<xsl:param name="markup"	as="xs:boolean" select="$dxd:markup"/>

		<xsl:variable name="uri"			as="xs:string?"			select="tokenize($coderef, '#')[1]"/>
		<xsl:variable name="id"				as="xs:string?" 		select="tokenize($coderef, '#')[2]"/>
		<xsl:variable name="resolvedUri"	as="xs:anyURI?"			select="resolve-uri($uri, $baseUri)"/>
		<xsl:variable name="doc"			as="document-node()?" 	select="if (doc-available($resolvedUri)) then doc($resolvedUri) else ()"/>
		<xsl:variable name="codeblock"		as="element()?" 		select="($doc/list/codeblock[@id = $id])[1]"/>

		<xsl:choose>
			<xsl:when test="$coderef = ''">
				<ERROR>Missing coderef attribute.</ERROR>
			</xsl:when>
			<xsl:when test="empty($doc)">
				<ERROR>Referenced file could not be loaded ('<xsl:value-of select="$resolvedUri"/>').</ERROR>
			</xsl:when>
			<xsl:when test="empty($codeblock)">
				<ERROR>Referenced file contains no codeblock with id '<xsl:value-of select="$id"/>' ('<xsl:value-of select="$resolvedUri"/>').</ERROR>
			</xsl:when>
			<xsl:when test="empty($codeblock/*)">
				<ERROR>no content</ERROR>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="resolved" as="node()*">
					<xsl:apply-templates select="$codeblock" mode="serialize">
						<xsl:with-param name="single-indent"	select="$indent"/>
					</xsl:apply-templates>
				</xsl:variable>
				<codeblock>
					<xsl:choose>
						<xsl:when test="$dxd:markup">
							<xsl:sequence select="$resolved"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="$resolved" mode="remove-markup"/>
						</xsl:otherwise>
					</xsl:choose>
				</codeblock>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<xsl:template match="/list/codeblock" mode="serialize" as="node()*">
		<xsl:param name="single-indent"	as="xs:string"/>
		
		<xsl:for-each select="*">
			<xsl:if test="position() > 1">
				<xsl:text>&#x0A;</xsl:text>
			</xsl:if>
			<xsl:apply-templates select="." mode="#current">
				<xsl:with-param name="single-indent"	select="$single-indent"/>
			</xsl:apply-templates>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="element()" mode="serialize" as="node()*">
		<xsl:param name="indent" 		as="xs:string?"/>
		<xsl:param name="single-indent"	as="xs:string"/>
		
		<xsl:variable name="attributes" as="node()*">
			<xsl:apply-templates select="attribute()" mode="#current"/> <!-- TODO: handle line breaks -->
		</xsl:variable>
		
		<xsl:variable name="content" as="node()*">
			<xsl:apply-templates select="node()" mode="#current">
				<xsl:with-param name="indent" 			select="concat($indent, $single-indent)"/>
				<xsl:with-param name="single-indent"	select="$single-indent"/>
			</xsl:apply-templates>
		</xsl:variable>	
		
		<xsl:variable name="attributesLen" 	as="xs:integer" select="string-length(string-join($attributes//text(), ''))"/>
		<xsl:variable name="contentLen" 	as="xs:integer" select="string-length(string-join($content//text(), ''))"/>

		<xsl:variable name="singleLineWidth" as="xs:integer" select="(2 * string-length(name(.))) + 5 +  $attributesLen + $contentLen"/>
		
		<xsl:choose>
			<xsl:when test="empty($content)">
				
				<element>
					<xsl:text>&lt;</xsl:text>
					<xsl:value-of select="name(.)"/>
					<xsl:sequence select="$attributes"/>
					<xsl:text>/&gt;</xsl:text>
				</element>
				
			</xsl:when>
			<xsl:when test="exists(element()) or exists($content/descendant-or-self::text()[contains(., '&#x0A;')]) or ($singleLineWidth gt $dxd:maxWidth)">
				
				<!-- content in seperate line(s) -->
				
				<element>
					<xsl:text>&lt;</xsl:text>
					<xsl:value-of select="name(.)"/>
					<xsl:sequence select="$attributes"/>
					<xsl:text>&gt;</xsl:text>
				</element>
				
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="concat($indent, $single-indent)"/>
				
				<xsl:sequence select="$content"/>
				
				<xsl:text>&#x0A;</xsl:text>
				<xsl:value-of select="$indent"/>
				
				<element>
					<xsl:text>&lt;/</xsl:text>
					<xsl:value-of select="name(.)"/>
					<xsl:text>&gt;</xsl:text>
				</element>
				
			</xsl:when>
			<xsl:otherwise>
				
				<!-- content in single line -->
				<element>
					<xsl:text>&lt;</xsl:text>
					<xsl:value-of select="name(.)"/>
					<xsl:sequence select="$attributes"/>
					<xsl:text>&gt;</xsl:text>
				</element>
				
				<xsl:sequence select="$content"/>
				
				<element>
					<xsl:text>&lt;/</xsl:text>
					<xsl:value-of select="name(.)"/>
					<xsl:text>&gt;</xsl:text>
				</element>
				
			</xsl:otherwise>
		</xsl:choose>
		
		<xsl:if test="following-sibling::element()">
			<xsl:text>&#x0A;</xsl:text>
			<xsl:value-of select="$indent"/>
		</xsl:if>
		
	</xsl:template>
	
	<xsl:template match="attribute()" mode="serialize" as="node()*">
		<xsl:text> </xsl:text>
		<attribute>
			<xsl:value-of select="name(.)"/>
			<xsl:text>="</xsl:text>
			<attribute-value>
				<xsl:value-of select="."/>
			</attribute-value>
			<xsl:text>"</xsl:text>
		</attribute>
	</xsl:template>
	
	<xsl:template match="text()[matches(., '^\s+$')]" mode="serialize" as="node()*">
		<!-- ignore -->
	</xsl:template>
	
	<xsl:template match="text()" mode="serialize" as="node()*">
		<text>
			<xsl:value-of select="."/>
		</text>
	</xsl:template>
	
	<xsl:template match="comment()" mode="serialize" as="node()*">
		<comment>
			<xsl:text>&lt;!--</xsl:text>
			<xsl:value-of select="."/>
			<xsl:text>--&gt;</xsl:text>
		</comment>
	</xsl:template>

	<xsl:template match="processing-instruction()" mode="serialize" as="node()*">
		<processing-instruction>
			<xsl:text>&lt;?</xsl:text>
			<xsl:value-of select="name(.)"/>
			<xsl:value-of select="if (string(.) != '') then concat(' ', .) else ()"/>
			<xsl:text>?&gt;</xsl:text>
		</processing-instruction>
	</xsl:template>
	
	
	<xsl:template match="element()" mode="remove-markup">
		<xsl:apply-templates select="node()" mode="#current"/>
	</xsl:template>
	
	<xsl:template match="text()" mode="remove-markup">
		<xsl:copy/>
	</xsl:template>
	
</xsl:stylesheet>
