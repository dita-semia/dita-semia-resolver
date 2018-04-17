<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:jt 	= "http://saxon.sf.net/java-type"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:ikd	= "http://www.dita-semia.org/implicit-keydef"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
	xmlns:cba	= "http://www.dita-semia.org/conbat"
	exclude-result-prefixes		= "#all">
	
	<xsl:variable name="OUTPUTCLASS_KEY"				as="xs:string"	select="'key'"/>
	<xsl:variable name="OUTPUTCLASS_KEY_NAME_BRACED"	as="xs:string"	select="'key-name-braced'"/>
	<xsl:variable name="OUTPUTCLASS_KEY_NAME_DASHED"	as="xs:string"	select="'key-dash-name'"/>
	<xsl:variable name="OUTPUTCLASS_NAME"				as="xs:string"	select="'name'"/>
	<xsl:variable name="OUTPUTCLASS_SVG"				as="xs:string"	select="'svg'"/>
	
	
	<xsl:template match="document-node() | element()" mode="resolve-akr">
		<xsl:copy>
			<xsl:apply-templates select="attribute() | node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="attribute() | processing-instruction() | text() | comment()"  mode="resolve-akr">
		<xsl:copy/>
	</xsl:template>
	
	
	<xsl:template match="*[@akr:ref]" mode="resolve-akr">
		<xsl:copy>
			<xsl:apply-templates select="attribute() except (@cba:content | @cba:default-content)" mode="#current"/>
			<xsl:call-template name="CreateKeySpecContent">
				<xsl:with-param name="keyRef"	select="."/>
				<xsl:with-param name="content" 	as="node()*">
					<xsl:call-template name="getCbaResolvedContent"/>
				</xsl:with-param>
			</xsl:call-template>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="CreateKeySpecContent">
		<xsl:param name="keyRef"	as="element()"/>
		<xsl:param name="content"	as="node()*"/>
		<xsl:param name="baseUri"	as="xs:anyURI"	select="base-uri(.)"/>

		<xsl:choose>
			<xsl:when test="$keyRef/@akr:ref">
				<xsl:variable name="outputclass"	as="xs:string?"										select="replace($keyRef/@outputclass, '!$', '')"/>
				<xsl:variable name="jKeyDef"		as="jt:org.DitaSemia.Base.AdvancedKeyref.KeyDef?" 	select="akr:getKeyDefByRefString($keyRef/@akr:ref, $baseUri)"/>
				
				<xsl:variable name="href"			as="xs:string?" 									select="if (exists($jKeyDef)) then ikd:getLocation($jKeyDef, $baseUri) else ()"/>
				<!--<xsl:message>href: <xsl:value-of select="$href"/></xsl:message>-->
				
				<xsl:if test="empty($jKeyDef)">
					<xsl:message>no keydef: <xsl:sequence select="$keyRef"/></xsl:message>
				</xsl:if>
				
				<xsl:variable name="displaySuffix" 	as="xs:string*" select="akr:getDisplaySuffix($keyRef, $jKeyDef)"/>
				<xsl:variable name="isKeyFiltered" 	as="xs:boolean" select="ikd:getIsFilteredKey($jKeyDef)"/>
				<xsl:variable name="isKeyHidden" 	as="xs:boolean" select="ikd:getIsKeyHidden($jKeyDef)"/>
				<xsl:variable name="isKeyNoLink" 	as="xs:boolean" select="ikd:getIsDontLink($jKeyDef)"/>
				<xsl:variable name="isResourceOnly"	as="xs:boolean" select="ikd:getIsResourceOnly($jKeyDef)"/>

				<!--<xsl:message>ref: <xsl:value-of select="@akr:ref"/>, isResourceOnly: <xsl:value-of select="$isResourceOnly"/>, isKeyNoLink: <xsl:value-of select="$isKeyNoLink"/><!-\-, isKeyHidden: <xsl:value-of select="$isKeyHidden"/>-\-></xsl:message>-->

				<xsl:variable name="refContent" as="node()*">
					<xsl:choose>
						<xsl:when test="($outputclass = $OUTPUTCLASS_NAME) or ($isKeyHidden)">
							<!-- no key to be displayed -->
						</xsl:when>
						<xsl:when test="$outputclass = $OUTPUTCLASS_SVG">
							<xsl:copy-of select="$content"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:variable name="keyContent" as="node()*">
								<xsl:call-template name="KeyFormatting">
									<xsl:with-param name="keyNode" select="$keyRef"/>
									<xsl:with-param name="content" select="$content"/>
								</xsl:call-template>
								<xsl:value-of select="$displaySuffix[1]"/>
							</xsl:variable>
							<xsl:choose>
								<xsl:when test="$isKeyFiltered">
									<ph class="{$CP_PH}">
										<xsl:variable name="attrContainer" as="element()?" select="ikd:getKeyFilterAttr($jKeyDef)"/>
										<!--<xsl:message>attrContainer: <xsl:copy-of select="$attrContainer"/></xsl:message>-->
										<xsl:copy-of select="$attrContainer/attribute()"/>
										<xsl:sequence select="$keyContent"/>
									</ph>
								</xsl:when>
								<xsl:otherwise>
									<xsl:sequence select="$keyContent"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:if test="not($outputclass = ($OUTPUTCLASS_KEY, $OUTPUTCLASS_SVG))">
						<xsl:value-of select="$displaySuffix[2]"/>
						<xsl:if test="empty($displaySuffix) and ($outputclass = $OUTPUTCLASS_NAME)">
							<xsl:text>&#xA0;</xsl:text>	<!-- insert some text to avoid generated content from link target -->
						</xsl:if>
					</xsl:if>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$isKeyNoLink or $isResourceOnly">
						<xsl:sequence select="$refContent"/>
					</xsl:when>
					<xsl:when test="exists($href)">
						<xref class="{$CP_XREF}" format="dita" outputclass="advanced-keyref" href="{$href}">
							<xsl:sequence select="$refContent"/>
						</xref>
					</xsl:when>
					<xsl:otherwise>
						<xsl:sequence select="$refContent"/>
						<xsl:if test="empty($jKeyDef) and not(ancestor-or-self::*[tokenize(@cba:flags, '\s+') = $CBA_FLAG_HIDE])">
							<xsl:call-template name="akr:NoLink">
								<xsl:with-param name="keyRef"	select="$keyRef"/>
							</xsl:call-template>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$keyRef/@ikd:key-type">
				<xsl:call-template name="KeyFormatting">
					<xsl:with-param name="keyNode" select="$keyRef"/>
					<xsl:with-param name="content" select="$content"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="$content"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="akr:NoLink">
		<xsl:param name="keyRef"	as="element()"/>
	</xsl:template>
	
	
	<xsl:template match="*[@ikd:key-type][not(@ikd:key) or (@ikd:key = '.')]" mode="resolve-akr">
		<xsl:copy>
			<xsl:apply-templates select="attribute() except (@cba:content | @cba:default-content)" mode="#current"/>
			<xsl:call-template name="KeyFormatting">
				<xsl:with-param name="keyNode" select="."/>
				<xsl:with-param name="content" as="node()*">
					<xsl:call-template name="getCbaResolvedContent"/>
				</xsl:with-param>
			</xsl:call-template>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="getCbaResolvedContent" as="node()*">
		<xsl:value-of select="@cba:content"/>
		<xsl:choose>
			<xsl:when test="exists(node())">
				<xsl:sequence select="node()"/>
			</xsl:when>
			<xsl:when test="@cba:default-content">
				<xsl:value-of select="@cba:default-content"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="@akr:* | @ikd:*" mode="resolve-akr">
		<!-- remove these attributes -->
	</xsl:template>
	
	
	<xsl:template name="KeyFormatting">
		<xsl:param name="keyNode" as="element()"/>
		<xsl:param name="content" as="node()*"/>
		
		<xsl:choose>
			<xsl:when test="$keyNode/@outputclass = $OUTPUTCLASS_SVG">
				<xsl:copy-of select="$content"/>
			</xsl:when>
			<xsl:when test="($keyNode/@akr:ref) or (($keyNode/@ikd:key-type) and not($keyNode/@ikd:key))">
				<xsl:variable name="keyTypeDef"		as="element()" select="akr:getKeyTypeDef($keyNode)"/>
				<xsl:variable name="keyContent" as="node()*">
					<xsl:value-of select="$keyTypeDef/@prefix"/>
					<xsl:copy-of select="$content"/>
					<xsl:value-of select="$keyTypeDef/@suffix"/>
				</xsl:variable>
				<xsl:variable name="italicWrapper" as="node()*">
					<xsl:choose>
						<xsl:when test="xs:boolean($keyTypeDef/@isItalicFont)">
							<i class="{$CP_I}">
								<xsl:sequence select="$keyContent"/>
							</i>
						</xsl:when>
						<xsl:otherwise>
							<xsl:sequence select="$keyContent"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="xs:boolean($keyTypeDef/@isCodeFont)">
						<codeph class="{$CP_CODEPH}">
							<xsl:sequence select="$italicWrapper"/>
						</codeph>
					</xsl:when>
					<xsl:otherwise>
						<xsl:sequence select="$italicWrapper"/>
					</xsl:otherwise>
				</xsl:choose>	
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="$content"/>
			</xsl:otherwise>
		</xsl:choose>	
	</xsl:template>
	
</xsl:stylesheet>
