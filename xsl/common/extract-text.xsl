<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:jt 	= "http://saxon.sf.net/java-type"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:cba	= "http://www.dita-semia.org/conbat"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
	exclude-result-prefixes		= "#all">
	
	
	<xsl:variable name="NEWLINE" as="xs:string" select="'&#x0A;'"/>
	
	<xsl:function name="ds:extractTextFromNode" as="xs:string?">
		<xsl:param name="node" as="node()?"/>
		
		<xsl:variable name="text" as="xs:string*">
			<xsl:apply-templates select="$node" mode="ExtractText"/>
		</xsl:variable>
		<xsl:sequence select="string-join($text, '')"/>
	</xsl:function>
	
	
	<xsl:function name="ds:extractContentTextFromNode" as="xs:string?">
		<xsl:param name="node" as="node()?"/>
		
		<xsl:variable name="text" as="xs:string*">
			<xsl:apply-templates select="$node" mode="ExtractText">
				<xsl:with-param name="contentOnly" select="true()"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:sequence select="string-join($text, '')"/>
	</xsl:function>
	
	
	<xsl:template match="node()" as="text()*" mode="ExtractContentText">
		<xsl:apply-templates select="." mode="ExtractText">
			<xsl:with-param name="contentOnly" select="true()"/>
		</xsl:apply-templates>
	</xsl:template>


	<!-- @cba:hide-empty -->
	<xsl:template match="*[xs:boolean(@cba:hide-empty)][empty(node())]" as="text()*" mode="ExtractText" priority="9">
		<!-- remove -->
	</xsl:template>

	<!-- paragraph-prefix -->
	<xsl:template match="*[@cba:prefix][contains(@class, $C_UL) or contains(@class, $C_OL) or contains(@class, $C_SL) or contains(@class, $C_CODEBLOCK)]" as="text()*" priority="8" mode="ExtractText">
		<xsl:param name="contentOnly" as="xs:boolean" select="false()"/>
		
		<xsl:if test="not($contentOnly)">
			<xsl:value-of select="ds:resolveEmbeddedXPath(@cba:prefix, .)"/>
			<xsl:value-of select="$NEWLINE"/>
		</xsl:if>
		<xsl:next-match>
			<xsl:with-param name="contentOnly" select="$contentOnly"/>
		</xsl:next-match>
	</xsl:template>
	
	<!-- paragraph-suffix -->
	<xsl:template match="*[@cba:suffix][contains(@class, $C_UL) or contains(@class, $C_OL) or contains(@class, $C_SL)]" as="text()*" priority="7" mode="ExtractText">
		<xsl:param name="contentOnly" as="xs:boolean" select="false()"/>
		
		<xsl:next-match>
			<xsl:with-param name="contentOnly" select="$contentOnly"/>
		</xsl:next-match>
		<xsl:if test="not($contentOnly)">
			<xsl:value-of select="$NEWLINE"/>
			<xsl:value-of select="ds:resolveEmbeddedXPath(@cba:suffix, .)"/>
		</xsl:if>
	</xsl:template>
	
	
	<!-- title -->
	<xsl:template match="*[@cba:title]" as="text()*" mode="ExtractText" priority="6">
		<xsl:param name="contentOnly" as="xs:boolean" select="false()"/>
		
		<xsl:if test="not($contentOnly)">
			<xsl:value-of select="ds:resolveEmbeddedXPath(@cba:title, .)"/>
			<xsl:value-of select="$NEWLINE"/>
		</xsl:if>
		<xsl:next-match>
			<xsl:with-param name="contentOnly" select="$contentOnly"/>
		</xsl:next-match>
	</xsl:template>

	<!-- dd-term -->
	<xsl:template match="*[@cba:dt][contains(@class, $C_DD)]" as="text()*" mode="ExtractText" priority="6">
		<xsl:param name="contentOnly" as="xs:boolean" select="false()"/>
		
		<xsl:if test="not($contentOnly)">
			<xsl:value-of select="ds:resolveEmbeddedXPath(@cba:dt, .)"/>
			<xsl:value-of select="$NEWLINE"/>
		</xsl:if>
		<xsl:next-match>
			<xsl:with-param name="contentOnly" select="$contentOnly"/>
		</xsl:next-match>
	</xsl:template>
	
	
	<!-- inline-content -->
	<xsl:template match="*[contains(@class, $C_P) or 
							contains(@class, $C_PH) or 
							contains(@class, $C_SLI) or 
							contains(@class, $C_STENTRY) or 
							contains(@class, $C_TITLE) or
							contains(@class, $C_CODEPH) or
							contains(@class, $C_DT) or
							contains(@class, $C_DD)]" as="text()*" mode="ExtractText" priority="5">
		<xsl:param name="contentOnly" as="xs:boolean" select="false()"/>
		
		<xsl:if test="not($contentOnly)">
			<xsl:call-template name="insert-csli-prefix"/>
			<xsl:value-of select="ds:getCbaText(@cba:prefix)"/>
		</xsl:if>
		
		<xsl:sequence select="ds:getCbaText(@cba:content)"/>
		<xsl:choose>
			<xsl:when test="empty(node())">
				<xsl:value-of select="ds:getCbaText(@cba:default-content)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
		
		<xsl:if test="not($contentOnly)">
			<xsl:value-of select="ds:getCbaText(@cba:suffix)"/>
			<xsl:call-template name="popup-edit-content"/>
			<xsl:value-of select="ds:getCbaText(@cba:suffix2)"/>
		</xsl:if>
	</xsl:template>
	

	<!-- table-header -->
	<xsl:template match="*[@cba:header][contains(@class, $C_TGROUP)]" as="text()*" mode="ExtractText" priority="5">
		<xsl:param name="contentOnly" as="xs:boolean" select="false()"/>
		
		<!-- not handled yet -->
	</xsl:template>
	
	
	<!-- simpletable-header -->
	<xsl:template match="*[@cba:header][contains(@class, $C_SIMPLETABLE)]" as="text()*" mode="ExtractText" priority="5">
		<xsl:param name="contentOnly" as="xs:boolean" select="false()"/>
		
		<!-- not handled yet -->
	</xsl:template>


	<!-- lists -->
	<xsl:template match="*[contains(@class, $C_UL) or contains(@class, $C_SL)]" as="text()*" mode="ExtractText" priority="4">
		<xsl:for-each select="*">
			<xsl:if test="position() gt 1">
				<xsl:value-of select="'&#x0A;'"/>
			</xsl:if>
			<xsl:value-of select="' - '"/>
			<xsl:apply-templates select="." mode="#current"/>
		</xsl:for-each>
	</xsl:template>
	
	
	<!-- block-container -->
	<xsl:template match="*[*[contains(@class, $C_P) or contains(@class, $C_UL) or contains(@class, $C_SL) or contains(@class, $C_DL)]]" as="text()*" mode="ExtractText" priority="4">
		<xsl:for-each select="*">
			<xsl:if test="position() gt 1">
				<xsl:value-of select="'&#x0A;'"/>
			</xsl:if>
			<xsl:apply-templates select="." mode="#current"/>
		</xsl:for-each>
	</xsl:template>
	
	
	<!-- key-xref -->
	<xsl:template match="*[@akr:ref]" as="text()*" mode="ExtractText" priority="3">
		<xsl:next-match/>
		<xsl:variable name="jKeyDef"	as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDef?" 	select="akr:getKeyDefByRefString(@akr:ref, base-uri())"/>
		<xsl:value-of select="akr:getDisplaySuffix(., $jKeyDef)" separator=""/>
	</xsl:template>
	

	<!-- remove whitespaces next to content generated by attributes or for indention only -->
	<xsl:template match="text()[matches(., '^\s+$')]" as="text()*" mode="ExtractText" priority="2">
		<xsl:choose>
			<xsl:when test="tokenize(parent::*/@cba:flags, '\s+') = $CBA_NO_TEXT">
				<!-- parent contains only no-text nodes -->
			</xsl:when>
			<xsl:when test="empty(preceding-sibling::node()) and exists(parent::*/@cba:prefix)">
				<!-- first node within an element with a prefix -->
				<!--<xsl:message>ignore whitespace (1): <xsl:sequence select="parent::*"/></xsl:message>-->
			</xsl:when>
			<xsl:when test="exists(preceding-sibling::node()[1]/@cba:suffix)">
				<!-- following node of an element with a suffix --> 
				<!--<xsl:message>ignore whitespace (2): <xsl:sequence select="parent::*"/></xsl:message>-->
			</xsl:when>
			<xsl:when test="(tokenize(preceding-sibling::node()[1]/@cba:flags, '\s+') = $CBA_FLAG_CSLI) and
				(tokenize(following-sibling::node()[1]/@cba:flags, '\s+') = $CBA_FLAG_CSLI)">
				<!-- node between two csli elements --> 
				<!--<xsl:message>ignore whitespace (3): <xsl:sequence select="parent::*"/></xsl:message>-->
			</xsl:when>
			<xsl:when test="exists(following-sibling::node()[1]/@cba:prefix)">
				<!-- preceding node of an element with a prefix -->
				<!--<xsl:message>ignore whitespace (4): <xsl:sequence select="parent::*"/></xsl:message>-->
			</xsl:when>
			<xsl:when test="empty(following-sibling::node()) and exists(parent::*/@cba:suffix)">
				<!-- last node within an element with a suffix -->
				<!--<xsl:message>ignore whitespace (5): <xsl:sequence select="parent::*"/></xsl:message>-->
			</xsl:when>
			<xsl:when test="(ancestor-or-self::*/@xml:space = 'preserve') or exists(parent::*/text()[not(matches(., '^\s+$'))])">
				<!-- within space-preserve mode and non-whitespace-text content -->
				<!--<xsl:message>keep whitespace: <xsl:sequence select="parent::*"/></xsl:message>-->
				<xsl:next-match/>
			</xsl:when>
			<xsl:when test="exists(preceding-sibling::node()) and exists(following-sibling::node())">
				<!-- collapse whitespaces -->
				<!--<xsl:message>collapse whitespace: <xsl:sequence select="parent::*"/></xsl:message>-->
				<xsl:value-of select="' '"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- ignore whitespaces -->
				<!--<xsl:message>ignore whitespace: <xsl:sequence select="parent::*"/></xsl:message>-->
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<!-- collapse whitespaces -->
	<xsl:template match="text()[not(ancestor::*/@xml:space = 'preserve')]" as="text()" mode="ExtractText" priority="1">
		<xsl:value-of select="replace(., '\s\s+', ' ')"/>
	</xsl:template>
		
		
	<xsl:template match="*[@cba:popup-edit = '#text']/text()" as="text()*" mode="ExtractText">
		<xsl:call-template name="create-popup-content">
			<xsl:with-param name="value" 	select="string(.)"/>
			<xsl:with-param name="element"	select="parent::*"/>
		</xsl:call-template>
	</xsl:template>
	
	
	<!-- @cba:o-class = "csli" (comma seperated list item) -->
	<xsl:template name="insert-csli-prefix" as="text()?">
		<xsl:variable name="pre" as="node()?" select="preceding-sibling::node()[not(self::text()[matches(., '^\s+$')])][1]"/>
		<xsl:if test="(tokenize(@cba:flags, '\s+') = $CBA_FLAG_CSLI) and (tokenize($pre/@cba:flags, '\s+') = $CBA_FLAG_CSLI)">
			<xsl:value-of select="', '"/>
		</xsl:if>
	</xsl:template>
	

	<xsl:function name="ds:getCbaText" as="text()?">
		<xsl:param name="attribute" as="attribute()?"/>
		
		<xsl:if test="exists($attribute)">
			<xsl:value-of select="ds:resolveEmbeddedXPath($attribute, $attribute/parent::*)"/>
		</xsl:if>
	</xsl:function>
	
	
	<xsl:template name="popup-edit-content" as="text()*">
		<xsl:if test="(@cba:popup-edit) and (@cba:popup-edit != '#text')">
			<xsl:call-template name="create-popup-content">
				<xsl:with-param name="value" 	select="string(attribute()[name(.) = current()/@cba:popup-edit])"/>
				<xsl:with-param name="element"	select="."/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	
	<xsl:template name="create-popup-content" as="text()*">
		<xsl:param name="value"		as="xs:string?"/>
		<xsl:param name="element"	as="element()"/>
		
		<xsl:variable name="hideValue" 	as="xs:string?" select="$element/@cba:pe-hide-value"/>
		
		<xsl:if test="not($hideValue = $value)">
			<xsl:variable name="flags" 	as="xs:string*" select="tokenize($element/@cba:flags, '\s+')"/>
			<xsl:if test="$flags = $CBA_FLAG_PE_BRACED">
				<xsl:value-of select="' ('"/>
			</xsl:if>
			<xsl:value-of select="$element/@cba:pe-prefix"/>
			
			<xsl:variable name="values"		as="xs:string*"		select="tokenize($element/@cba:pe-values, ',')"/>
			<xsl:variable name="labels"		as="xs:string*"		select="tokenize($element/@cba:pe-labels, ',')"/>
			<xsl:variable name="valueIndex"	as="xs:integer?"	select="index-of($values, $value)"/>
			<xsl:variable name="label"		as="xs:string?"		select="$labels[$valueIndex]"/>
			
			<xsl:value-of select="if ($label) then $label else $value"/>

			<xsl:if test="$flags = $CBA_FLAG_PE_BRACED">
				<xsl:value-of select="')'"/>
			</xsl:if>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
