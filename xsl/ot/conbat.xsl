<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:cba	= "http://www.dita-semia.org/conbat"
	exclude-result-prefixes		= "#all">
	
	
	<xsl:template name="cba-marker" as="processing-instruction()?">
		<xsl:processing-instruction name="CBA"/>
	</xsl:template>


	<!-- @cba:hide-empty -->
	<xsl:template match="*[ds:isHidden(.)]" priority="9">
		<!-- remove -->
	</xsl:template>
	
	<!-- @cba:hide-empty -->
	<!--<xsl:template match="*[() and (@cba:pe-hide-value) and ][empty(node())]" priority="9">
		<!-\- remove -\->
	</xsl:template>-->
	
	<xsl:function name="ds:isHidden" as="xs:boolean">
		<xsl:param name="node" as="node()"/>
		
		<xsl:choose>
			<xsl:when test="empty($node/(text() | element())) and 
							(matches($node/@cba:flags, concat('(^|\s)', $CBA_FLAG_HIDE_EMPTY, '(\s|$)')))">
				<xsl:sequence select="true()"/>
			</xsl:when>
			<xsl:when test="($node/@cba:popup-edit = '#text') and
							(string($node/text()) = string($node/@cba:pe-hide-value))">
				<xsl:sequence select="true()"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="false()"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<!-- paragraph-prefix -->
	<xsl:template match="*[@cba:prefix][contains(@class, $C_UL) or contains(@class, $C_OL) or contains(@class, $C_SL) or contains(@class, $C_CODEBLOCK)]" priority="8">
		<p class="{$CP_P}">
			<xsl:call-template name="copy-filter-attr"/>
			<xsl:call-template name="cba-marker"/>
			<xsl:value-of select="cba:resolveEmbeddedXPath(@cba:prefix)"/>
		</p>
		<xsl:next-match/>
	</xsl:template>
	
	<!-- paragraph-suffix -->
	<xsl:template match="*[@cba:suffix][contains(@class, $C_UL) or contains(@class, $C_OL) or contains(@class, $C_SL)]" priority="7">
		<xsl:next-match/>
		<p class="{$CP_P}">
			<xsl:call-template name="copy-filter-attr"/>
			<xsl:call-template name="cba-marker"/>
			<xsl:value-of select="cba:resolveEmbeddedXPath(@cba:suffix)"/>
		</p>
	</xsl:template>
	
	
	<!-- title -->
	<xsl:template match="*[@cba:title]" priority="6">
		<xsl:variable name="resolved" as="node()*">
			<!-- combination of different resolving-features (e.g. xslt-conref) needs to be supported. -->
			<xsl:next-match/>
		</xsl:variable>
		<xsl:variable name="title" as="element()">
			<title class="{$CP_TITLE}">
				<xsl:call-template name="cba-marker"/>
				<ph class="{$CP_PH}">
					<xsl:call-template name="cba-marker"/>
					<xsl:value-of select="cba:resolveEmbeddedXPath(@cba:title)"/>
				</ph>
			</title>
		</xsl:variable>
		<xsl:for-each select="$resolved">
			<xsl:copy>
				<xsl:sequence select="attribute(), $title, node()"/>
			</xsl:copy>
		</xsl:for-each>
	</xsl:template>


	<!-- inline-codeph-content -->
	<xsl:template match="*[contains(@class, $C_CODEPH)]" priority="6">
		<xsl:sequence select="ds:createCbaPhrase(@cba:prefix)"/>
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:call-template name="insert-csli-prefix"/>
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
	

	<!-- dd-term -->
	<xsl:template match="*[@cba:dt][contains(@class, $C_DD)]" priority="6">
		<dlentry class="- topic/dlentry ">
			<xsl:apply-templates select="attribute() except @class" mode="#current"/>
			<dt class="- topic/dt ">
				<xsl:value-of select="cba:resolveEmbeddedXPath(@cba:dt)"/>
				<xsl:call-template name="cba-marker"/>
			</dt>
			<xsl:next-match/>
		</dlentry>
	</xsl:template>
	
	
	<!-- inline-content -->
	<xsl:template match="*[contains(@class, $C_P) or 
							contains(@class, $C_DT) or 
							contains(@class, $C_PH) or 
							contains(@class, $C_SLI) or 
							contains(@class, $C_STENTRY) or 
							contains(@class, $C_TITLE)]" priority="5">
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:call-template name="insert-csli-prefix"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:prefix)"/>
			<xsl:call-template name="handle-code-flag">
				<xsl:with-param name="content" as="node()*">
					<xsl:choose>
						<xsl:when test="@cba:content">
							<xsl:call-template name="KeyFormatting">
								<xsl:with-param name="content" 	select="ds:createCbaPhrase(@cba:content)"/>
								<xsl:with-param name="keyNode"	select="."/>
							</xsl:call-template>
						</xsl:when>
						<xsl:when test="empty(node()) and exists(@cba:default-content)">
							<xsl:call-template name="KeyFormatting">
								<xsl:with-param name="content" 	select="ds:createCbaPhrase(@cba:default-content)"/>
								<xsl:with-param name="keyNode"	select="."/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="node()" mode="#current"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:with-param>
			</xsl:call-template>
			<xsl:sequence select="ds:createCbaPhrase(@cba:suffix)"/>
			<xsl:call-template name="add-popup-edit-content"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:suffix2)"/>
		</xsl:copy>
	</xsl:template>
	
	
	<!-- dd -->
	<xsl:template match="*[contains(@class, $C_DD)]" priority="5">
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:prefix)"/>
			<xsl:call-template name="handle-code-flag">
				<xsl:with-param name="content" as="node()*">
					<xsl:sequence select="ds:createCbaPhrase(@cba:content)"/>
					<xsl:choose>
						<xsl:when test="empty(node())">
							<xsl:sequence select="ds:createCbaPhrase(@cba:default-content)"/>
						</xsl:when>
						<xsl:when test="exists(*[contains(@class, $C_LI) or contains(@class, $C_SLI)])">
							<!-- handle list items without list container but ignore empty text nodes -->
							<xsl:for-each-group select="node() except text()[matches(., '^\s+$')]" group-adjacent="string(tokenize(@class, '\s+')[2])">
								<xsl:choose>
									<xsl:when test="current-grouping-key() = 'topic/sli'">
										<sl class="+ topic/sl ">
											<xsl:apply-templates select="current-group()"/>
										</sl>
									</xsl:when>
									<xsl:when test="current-grouping-key() = 'topic/li'">
										<ul class="+ topic/ul ">
											<xsl:apply-templates select="current-group()"/>
										</ul>
									</xsl:when>
									<xsl:otherwise>
										<xsl:apply-templates select="current-group()"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each-group>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="node()" mode="#current"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="add-popup-edit-content"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:suffix)"/>
		</xsl:copy>
	</xsl:template>
	
	
	<!-- dl-header -->
	<xsl:template match="*[@cba:header][contains(@class, $C_DL)]" priority="5">
		<xsl:copy>
			<xsl:variable name="resolvedHeader" as="xs:string" select="cba:resolveEmbeddedXPath(@cba:header)"/>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:apply-templates select="*[contains(@class, $C_COLSPEC)]" mode="#current"/>
			<dlhead class="{$CP_DLHEAD}">
				<xsl:call-template name="cba-marker"/>
				<xsl:variable name="headerList" as="xs:string*" select="tokenize($resolvedHeader, '[|]')"/>
				<dthd class="{$CP_DTHD}">
					<xsl:call-template name="cba-marker"/>
					<xsl:value-of select="$headerList[1]"/>
				</dthd>
				<ddhd class="{$CP_DDHD}">
					<xsl:call-template name="cba-marker"/>
					<xsl:value-of select="$headerList[2]"/>
				</ddhd>
			</dlhead>
			<xsl:apply-templates select="node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>


	<!-- table-header -->
	<xsl:template match="*[@cba:header][contains(@class, $C_TGROUP)]" priority="5">
		<xsl:copy>
			<xsl:variable name="resolvedHeader" as="xs:string" select="cba:resolveEmbeddedXPath(@cba:header)"/>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:apply-templates select="*[contains(@class, $C_COLSPEC)]" mode="#current"/>
			<thead class="{$CP_THEAD}">
				<xsl:call-template name="cba-marker"/>
				<row class="{$CP_ROW}">
					<xsl:call-template name="cba-marker"/>
					<xsl:for-each select="tokenize($resolvedHeader, '[|]')">
						<entry class="{$CP_ENTRY}">
							<xsl:call-template name="cba-marker"/>
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
				<xsl:call-template name="cba-marker"/>
				<xsl:for-each select="tokenize($resolvedHeader, '[|]')">
					<stentry class="{$CP_STENTRY}">
						<xsl:call-template name="cba-marker"/>
						<xsl:value-of select="."/>
					</stentry>
				</xsl:for-each>
			</sthead>
			<xsl:apply-templates select="node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="*[@cba:popup-edit = '#text']/text()">
		<xsl:call-template name="create-popup-content">
			<xsl:with-param name="value" 	select="string(.)"/>
			<xsl:with-param name="node"		select="parent::*"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="add-popup-edit-content">
		<xsl:if test="(@cba:popup-edit) and (@cba:popup-edit != '#text')">
			<xsl:call-template name="create-popup-content">
				<xsl:with-param name="value" 	select="string(attribute()[name(.) = current()/@cba:popup-edit])"/>
				<xsl:with-param name="node"		select="."/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	
	<xsl:template name="create-popup-content">
		<xsl:param name="value"	as="xs:string?"/>
		<xsl:param name="node"	as="node()"/>
		
		<xsl:variable name="hideValue" 	as="xs:string?" select="$node/@cba:pe-hide-value"/>
			
		<xsl:if test="not($hideValue = $value)">
			<ph class="{$CP_PH}">
				<xsl:call-template name="cba-marker"/>
				<xsl:variable name="pe-braced" 	as="xs:boolean" select="matches($node/@cba:flags, concat('(^|\s)', $CBA_FLAG_PE_BRACED, '(\s|$)'))"/>
				<xsl:if test="$pe-braced">
					<xsl:text> (</xsl:text>
				</xsl:if>
				<xsl:value-of select="$node/@cba:pe-prefix"/>
				
				<xsl:variable name="values"		as="xs:string*"		select="tokenize($node/@cba:pe-values, ',')"/>
				<xsl:variable name="labels"		as="xs:string*"		select="tokenize($node/@cba:pe-labels, ',')"/>
				<xsl:variable name="valueIndex"	as="xs:integer?"	select="index-of($values, $value)"/>
				<xsl:variable name="label"		as="xs:string?"		select="$labels[$valueIndex]"/>
				<xsl:variable name="output"		as="xs:string?"		select="if ($label) then $label else $value"/>
				
				<xsl:choose>
					<xsl:when test="matches($node/@cba:flags, concat('(^|\s)', $CBA_FLAG_PE_ITALIC, '(\s|$)'))">
						<i class="{$CP_I}">
							<xsl:value-of select="$output"/>
						</i>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$output"/>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="$pe-braced">
					<xsl:text>)</xsl:text>
				</xsl:if>
			</ph>
		</xsl:if>
		
	</xsl:template>


	<!-- remove whitespaces next to content generated by attributes -->
	<xsl:template match="text()[matches(., '^\s+$')]">
		<xsl:choose>
			<xsl:when test="empty(preceding-sibling::node()) and exists(parent::*/@cba:prefix)">
				<!-- first node within an element with a prefix -->
			</xsl:when>
			<xsl:when test="exists(preceding-sibling::node()[1]/@cba:suffix)">
				<!-- following node of an element with a suffix --> 
			</xsl:when>
			<xsl:when test="(tokenize(preceding-sibling::node()[1]/@cba:flags, '\s+') = $CBA_FLAG_CSLI) and
							(tokenize(following-sibling::node()[1]/@cba:flags, '\s+') = $CBA_FLAG_CSLI)">
				<!-- node between two csli elements --> 
			</xsl:when>
			<xsl:when test="exists(following-sibling::node()[1]/@cba:prefix)">
				<!-- preceding node of an element with a prefix -->
			</xsl:when>
			<xsl:when test="empty(following-sibling::node()) and exists(parent::*/@cba:suffix)">
				<!-- last node within an element with a suffix -->
			</xsl:when>
			<xsl:when test="empty(following-sibling::node()) and 
							empty(parent::*/following-sibling::node()[not(self::text()[matches(., '^\s+$')])]) and 
							exists(parent::*/parent::*/@cba:suffix)">
				<!-- last node within the last element within an element with a suffix -->
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<xsl:template name="handle-code-flag">
		<xsl:param name="content" as="node()*"/>
		
		<xsl:choose>
			<xsl:when test="tokenize(@cba:flags, '\s+') = $CBA_FLAG_CODE">
				<codeph class="{$CP_CODEPH}">
					<xsl:call-template name="cba-marker"/>
					<xsl:value-of select="@cba:code-prefix"/>
					<xsl:sequence select="$content"/>
					<xsl:value-of select="@cba:code-suffix"/>
				</codeph>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="$content"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<!-- @cba:flags = "csli" (comma seperated list item) -->
	<xsl:template name="insert-csli-prefix">
		<xsl:variable name="pre" as="node()?" select="preceding-sibling::node()[not(self::text()[matches(., '^\s+$')])][not(ds:isHidden(.))][1]"/>
		<xsl:if test="(tokenize(@cba:flags, '\s+') = $CBA_FLAG_CSLI) and (tokenize($pre/@cba:flags, '\s+') = $CBA_FLAG_CSLI)">
			<ph class="{$CP_PH}">
				<xsl:call-template name="cba-marker"/>
				<xsl:text>, </xsl:text>
			</ph>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="copy-filter-attr">
		<xsl:copy-of select="@audience | @product | @plattform | @props | @otherprops"/>
	</xsl:template>
	

	<xsl:template match="@cba:*">
		<!-- remove these attributes -->
	</xsl:template>


	<xsl:function name="ds:createCbaPhrase">
		<xsl:param name="attribute" as="attribute()?"/>
		
		<xsl:if test="exists($attribute)">
			<ph class="{$CP_PH}">
				<xsl:call-template name="cba-marker"/>
				<xsl:for-each select="$attribute/parent::*">	<!-- set context -->
					<xsl:value-of select="cba:resolveEmbeddedXPath($attribute)"/>
				</xsl:for-each>
			</ph>
		</xsl:if>
	</xsl:function>


	<xsl:function name="cba:resolveEmbeddedXPath" use-when="not(function-available('cba:resolveEmbeddedXPath'))">
		<xsl:param name="xpath" as="xs:string"/>
		<xsl:value-of select="$xpath"/>
	</xsl:function>
	
</xsl:stylesheet>
