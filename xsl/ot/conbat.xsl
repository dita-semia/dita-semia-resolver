<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:cba	= "http://www.dita-semia.org/conbat"
	exclude-result-prefixes		= "#all">
	
	<xsl:variable name="CBA_MARKER" as="processing-instruction()">
		<xsl:processing-instruction name="CBA"/>
	</xsl:variable>


	<!-- @cba:hide-empty -->
	<xsl:template match="*[xs:boolean(@cba:hide-empty)][empty(node())]" priority="9">
		<!-- remove -->
	</xsl:template>

	<!-- paragraph-prefix -->
	<xsl:template match="*[@cba:prefix][contains(@class, $C_UL) or contains(@class, $C_OL) or contains(@class, $C_SL) or contains(@class, $C_CODEBLOCK)]" priority="8">
		<p class="{$CP_P}">
			<xsl:call-template name="copy-filter-attr"/>
			<xsl:sequence select="$CBA_MARKER"/>
			<xsl:value-of select="cba:resolveEmbeddedXPath(@cba:prefix)"/>
		</p>
		<xsl:next-match/>
	</xsl:template>
	
	<!-- paragraph-suffix -->
	<xsl:template match="*[@cba:suffix][contains(@class, $C_UL) or contains(@class, $C_OL) or contains(@class, $C_SL)]" priority="7">
		<xsl:next-match/>
		<p class="{$CP_P}">
			<xsl:call-template name="copy-filter-attr"/>
			<xsl:sequence select="$CBA_MARKER"/>
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
				<xsl:sequence select="$CBA_MARKER"/>
				<ph class="{$CP_PH}">
					<xsl:sequence select="$CBA_MARKER"/>
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
				<xsl:sequence select="$CBA_MARKER"/>
			</dt>
			<xsl:next-match/>
		</dlentry>
	</xsl:template>
	
	
	<!-- inline-content -->
	<xsl:template match="*[contains(@class, $C_P) or 
							contains(@class, $C_PH) or 
							contains(@class, $C_SLI) or 
							contains(@class, $C_STENTRY) or 
							contains(@class, $C_TITLE)]" priority="5">
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:call-template name="insert-csli-prefix"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:prefix)"/>
			<xsl:call-template name="handle-code-oclass">
				<xsl:with-param name="content" as="node()*">
					<xsl:sequence select="ds:createCbaPhrase(@cba:content)"/>
					<xsl:choose>
						<xsl:when test="empty(node())">
							<xsl:sequence select="ds:createCbaPhrase(@cba:default-content)"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="node()" mode="#current"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:with-param>
			</xsl:call-template>
			<xsl:sequence select="ds:createCbaPhrase(@cba:suffix)"/>
		</xsl:copy>
	</xsl:template>
	
	
	<!-- dd -->
	<xsl:template match="*[contains(@class, $C_DD)]" priority="5">
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:prefix)"/>
			<xsl:call-template name="handle-code-oclass">
				<xsl:with-param name="content" as="node()*">
					<xsl:sequence select="ds:createCbaPhrase(@cba:content)"/>
					<xsl:choose>
						<xsl:when test="empty(node())">
							<xsl:sequence select="ds:createCbaPhrase(@cba:default-content)"/>
						</xsl:when>
						<xsl:otherwise>
							<!-- handle list items without list container -->
							<xsl:for-each-group select="node()" group-adjacent="string(tokenize(@class, '\s+')[2])">
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
						</xsl:otherwise>
					</xsl:choose>
				</xsl:with-param>
			</xsl:call-template>
			
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
				<xsl:sequence select="$CBA_MARKER"/>
				<xsl:variable name="headerList" as="xs:string*" select="tokenize($resolvedHeader, '[|]')"/>
				<dthd class="{$CP_DTHD}">
					<xsl:sequence select="$CBA_MARKER"/>
					<xsl:value-of select="$headerList[1]"/>
				</dthd>
				<ddhd class="{$CP_DDHD}">
					<xsl:sequence select="$CBA_MARKER"/>
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
				<xsl:sequence select="$CBA_MARKER"/>
				<row class="{$CP_ROW}">
					<xsl:sequence select="$CBA_MARKER"/>
					<xsl:for-each select="tokenize($resolvedHeader, '[|]')">
						<entry class="{$CP_ENTRY}">
							<xsl:sequence select="$CBA_MARKER"/>
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
				<xsl:sequence select="$CBA_MARKER"/>
				<xsl:for-each select="tokenize($resolvedHeader, '[|]')">
					<stentry class="{$CP_STENTRY}">
						<xsl:sequence select="$CBA_MARKER"/>
						<xsl:value-of select="."/>
					</stentry>
				</xsl:for-each>
			</sthead>
			<xsl:apply-templates select="node()" mode="#current"/>
		</xsl:copy>
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
			<xsl:when test="(tokenize(preceding-sibling::node()[1]/@cba:o-class, '\s+') = $OCLASS_CSLI) and
							(tokenize(following-sibling::node()[1]/@cba:o-class, '\s+') = $OCLASS_CSLI)">
				<!-- node between two csli elements --> 
			</xsl:when>
			<xsl:when test="exists(following-sibling::node()[1]/@cba:prefix)">
				<!-- preceding node of an element with a prefix -->
			</xsl:when>
			<xsl:when test="empty(following-sibling::node()) and exists(parent::*/@cba:suffix)">
				<!-- last node within an element with a suffix -->
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<xsl:template name="handle-code-oclass">
		<xsl:param name="content" as="node()*"/>
		
		<xsl:choose>
			<xsl:when test="tokenize(@cba:o-class, '\s+') = $OCLASS_CODE">
				<codeph class="{$CP_CODEPH}">
					<xsl:sequence select="$content"/>
				</codeph>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="$content"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<!-- @cba:o-class = "csli" (comma seperated list item) -->
	<xsl:template name="insert-csli-prefix">
		<xsl:variable name="pre" as="node()?" select="preceding-sibling::node()[not(self::text()[matches(., '^\s+$')])][1]"/>
		<xsl:if test="(tokenize(@cba:o-class, '\s+') = $OCLASS_CSLI) and (tokenize($pre/@cba:o-class, '\s+') = $OCLASS_CSLI)">
			<ph class="{$CP_PH}">
				<xsl:sequence select="$CBA_MARKER"/>
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
				<xsl:sequence select="$CBA_MARKER"/>
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
