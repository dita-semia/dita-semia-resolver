<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:cba	= "http://www.dita-semia.org/conbat"
	exclude-result-prefixes		= "#all">
	
	<xsl:variable name="FILTER_ATTR_LIST" as="xs:string+" select="('audience', 'product', 'plattform', 'props', 'otherprops')"/>
	
	<xsl:template name="cba-marker" as="processing-instruction()?">
		<xsl:processing-instruction name="CBA"/>
	</xsl:template>
	
	<!-- resolve xpath expressions in first stage with unmodified xml structure -->
	<xsl:template match="@cba:title | @cba:prefix | @cba:code-prefix | @cba:suffix | @cba:code-suffix | @cba:content | @cba:default-content | @cba:dt | @cba:header" mode="resolve-xcr">
		<xsl:variable name="attribute" as="attribute()" select="."/>
		<xsl:for-each select="parent::*">	<!-- set context -->
			<xsl:attribute name="{name($attribute)}" select="ds:resolveEmbeddedXPath($attribute, .)"/>
		</xsl:for-each>
	</xsl:template>


	<!-- @cba:hide-empty -->
	<xsl:template match="*[ds:isHidden(.)]" priority="9" mode="resolve-cba">
		<!-- remove -->
	</xsl:template>
	
	<xsl:function name="ds:isHidden" as="xs:boolean">
		<xsl:param name="node" as="node()"/>
		
		<xsl:variable name="flags" as="xs:string*" select="ds:getCbaFlags($node)"/>
		<xsl:choose>
			<xsl:when test="$flags = $CBA_FLAG_HIDE">
				<xsl:sequence select="true()"/>
			</xsl:when>
			<xsl:when test="(name($node/*[1]) = $node/@cba:hide-on-content)">
				<xsl:sequence select="true()"/>
			</xsl:when>
			<xsl:when test="empty($node/(text()[not(matches(., '^\s+$'))] | element()[not(ds:isHidden(.))])) and 
							($flags = $CBA_FLAG_HIDE_EMPTY)">
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
	
	
	<!-- title -->
	<xsl:template match="*[@cba:title]" priority="8" mode="resolve-cba">
		<xsl:variable name="title" as="element()">
			<title class="{$CP_TITLE}">
				<xsl:call-template name="cba-marker"/>
				<xsl:call-template name="cba-ph">
					<xsl:with-param name="content" as="node()">
						<xsl:value-of select="@cba:title"/>
					</xsl:with-param>
				</xsl:call-template>
			</title>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="contains(parent::*/@class, $C_BODY) and not(contains(@class, $C_SECTION))">
				<!-- within a body create a section wrapper -->
				<section class="{$CP_SECTION}">
					<xsl:sequence select="$title"/>
					<xsl:copy>
						<xsl:apply-templates select="attribute() | node()" mode="#current"/>
					</xsl:copy>
				</section>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="attribute()" mode="#current"/>
					<xsl:sequence select="$title"/>
					<xsl:apply-templates select="node()" mode="#current"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	<xsl:function name="ds:getCbaFlags" as="xs:string*">
		<xsl:param name="node" as="node()?"/>
		<xsl:sequence select="tokenize($node/@cba:flags, '\s+')"/>
	</xsl:function>

	<!-- paragraph-prefix -->
	<xsl:template match="*[@cba:prefix][contains(@class, $C_UL) or contains(@class, $C_OL) or contains(@class, $C_SL) or contains(@class, $C_DL) or contains(@class, $C_CODEBLOCK)]" priority="8" mode="resolve-cba">
		<p class="{$CP_P}">
			<xsl:call-template name="copy-filter-attr"/>
			<xsl:call-template name="cba-marker"/>
			<xsl:value-of select="@cba:prefix"/>
		</p>
		<xsl:next-match/>
	</xsl:template>
	
	<!-- paragraph-suffix -->
	<xsl:template match="*[@cba:suffix][contains(@class, $C_UL) or contains(@class, $C_OL) or contains(@class, $C_SL)]" priority="7" mode="resolve-cba">
		<xsl:next-match/>
		<p class="{$CP_P}">
			<xsl:call-template name="copy-filter-attr"/>
			<xsl:call-template name="cba-marker"/>
			<xsl:value-of select="@cba:suffix"/>
		</p>
	</xsl:template>


	<!-- inline-codeph-content -->
	<xsl:template match="*[contains(@class, $C_CODEPH)]" priority="6" mode="resolve-cba">
		<xsl:call-template name="insert-csli-prefix"/>
		<xsl:sequence select="ds:createCbaPhrase(@cba:prefix)"/>
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
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
	<xsl:template match="*[@cba:dt][contains(@class, $C_DD)]" priority="6" mode="resolve-cba">
		<dlentry class="- topic/dlentry ">
			<xsl:apply-templates select="@audience | @product | @platform | @props | @otherprops" mode="#current"/>
			<dt class="- topic/dt ">
				<xsl:value-of select="@cba:dt"/>
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
							contains(@class, $C_TITLE) or 
							contains(@class, $C_DATA)]" priority="5" mode="resolve-cba">
		<xsl:call-template name="insert-csli-prefix"/>
		
		<xsl:variable name="flags" as="xs:string*" select="ds:getCbaFlags(.)"/>
		<xsl:variable name="content" as="node()*">
			<xsl:sequence select="ds:createCbaPhrase(@cba:prefix, ($flags = $CBA_FLAG_PREFIX_ITALIC))"/>
			<xsl:choose>
				<xsl:when test="@cba:content">
					<xsl:call-template name="handle-style-flags">
						<xsl:with-param name="content" select="ds:createCbaPhrase(@cba:content)"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="empty(node()) and exists(@cba:default-content)">
					<xsl:call-template name="handle-style-flags">
						<xsl:with-param name="flags"	select="$flags"/>
						<xsl:with-param name="content" select="ds:createCbaPhrase(@cba:default-content)"/>
						<xsl:with-param name="isDefaultContent" select="true()"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="handle-style-flags">
						<xsl:with-param name="flags"	select="$flags"/>
						<xsl:with-param name="content" as="node()*">
							<xsl:apply-templates select="node()" mode="#current"/>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:sequence select="ds:createCbaPhrase(@cba:suffix)"/>
			<xsl:call-template name="add-popup-edit-content"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:suffix2)"/>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="($flags = $CBA_FLAG_UNWRAP) and empty(@audience | @product | @platform | @props | @otherprops)">
				<xsl:sequence select="$content"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="attribute()" mode="#current"/>
					<xsl:sequence select="$content"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose> 
	</xsl:template>
	
	
	<!-- dd -->
	<xsl:template match="*[contains(@class, $C_DD)]" priority="5" mode="resolve-cba">
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:prefix)"/>
			<xsl:choose>
				<xsl:when test="@cba:content">
					<xsl:call-template name="handle-style-flags">
						<xsl:with-param name="content" as="node()*">
							<xsl:sequence select="ds:createCbaPhrase(@cba:content)"/>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="empty(node())">
					<xsl:call-template name="handle-style-flags">
						<xsl:with-param name="content" as="node()*">
							<xsl:sequence select="ds:createCbaPhrase(@cba:default-content)"/>
						</xsl:with-param>
						<xsl:with-param name="isDefaultContent" select="true()"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="exists(*[contains(@class, $C_LI) or contains(@class, $C_SLI)])">
					<!-- handle list items without list container but ignore empty text nodes -->
					<xsl:for-each-group select="node() except text()[matches(., '^\s+$')]" group-adjacent="string(tokenize(@class, '\s+')[2])">
						<xsl:choose>
							<xsl:when test="current-grouping-key() = 'topic/sli'">
								<sl class="+ topic/sl ">
									<xsl:call-template name="merge-filter-attr-for-wrapper">
										<xsl:with-param name="elements" select="current-group()"/>
									</xsl:call-template>
									<xsl:apply-templates select="current-group()" mode="#current"/>
								</sl>
							</xsl:when>
							<xsl:when test="current-grouping-key() = 'topic/li'">
								<ul class="+ topic/ul ">
									<xsl:call-template name="merge-filter-attr-for-wrapper">
										<xsl:with-param name="elements" select="current-group()"/>
									</xsl:call-template>
									<xsl:apply-templates select="current-group()" mode="#current"/>
								</ul>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="current-group()" mode="#current"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each-group>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="handle-style-flags">
						<xsl:with-param name="content" as="node()*">
							<xsl:apply-templates select="node()" mode="#current"/>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:sequence select="ds:createCbaPhrase(@cba:suffix)"/>
			<xsl:call-template name="add-popup-edit-content"/>
			<xsl:sequence select="ds:createCbaPhrase(@cba:suffix2)"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- dl-header -->
	<xsl:template match="*[@cba:header][contains(@class, $C_DL)]" priority="5" mode="resolve-cba">
		<xsl:copy>
			<xsl:variable name="resolvedHeader" as="xs:string" select="@cba:header"/>
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
	<xsl:template match="*[@cba:header][contains(@class, $C_TGROUP)]" priority="5" mode="resolve-cba">
		<xsl:copy>
			<xsl:variable name="resolvedHeader" as="xs:string" select="@cba:header"/>
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
	<xsl:template match="*[@cba:header][contains(@class, $C_SIMPLETABLE)]" priority="5" mode="resolve-cba">
		<xsl:copy>
			<xsl:variable name="resolvedHeader" as="xs:string" select="@cba:header"/>
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
	
	<xsl:template match="*[@cba:popup-edit = '#text']/text()" mode="resolve-cba">
		<xsl:call-template name="create-popup-content">
			<xsl:with-param name="value" 	select="string(.)"/>
			<xsl:with-param name="element"	select="parent::*"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="add-popup-edit-content">
		<xsl:if test="(@cba:popup-edit) and (@cba:popup-edit != '#text')">
			<xsl:call-template name="create-popup-content">
				<xsl:with-param name="value" 	select="string(attribute()[name(.) = current()/@cba:popup-edit])"/>
				<xsl:with-param name="element"	select="."/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	
	<xsl:template name="create-popup-content">
		<xsl:param name="value"		as="xs:string?"/>
		<xsl:param name="element"	as="element()"/>
		
		<xsl:variable name="hideValue" 	as="xs:string?" select="$element/@cba:pe-hide-value"/>
			
		<xsl:if test="not($hideValue = $value)">
			<xsl:call-template name="cba-ph">
				<xsl:with-param name="content" as="node()*">
					<xsl:variable name="flags" 	as="xs:string*" select="ds:getCbaFlags($element)"/>
					<xsl:if test="$flags = $CBA_FLAG_PE_BRACED">
						<xsl:text> (</xsl:text>
					</xsl:if>
					<xsl:value-of select="$element/@cba:pe-prefix"/>
					
					<xsl:variable name="values"		as="xs:string*"		select="tokenize($element/@cba:pe-values, ',')"/>
					<xsl:variable name="labels"		as="xs:string*"		select="tokenize($element/@cba:pe-labels, ',')"/>
					<xsl:variable name="valueIndex"	as="xs:integer?"	select="index-of($values, $value)"/>
					<xsl:variable name="label"		as="xs:string?"		select="$labels[$valueIndex]"/>
					<xsl:variable name="output"		as="xs:string?"		select="if ($label) then $label else $value"/>
					
					<xsl:choose>
						<xsl:when test="$flags = $CBA_FLAG_PE_ITALIC">
							<i class="{$CP_I}">
								<xsl:value-of select="$output"/>
							</i>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$output"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:if test="$flags = $CBA_FLAG_PE_BRACED">
						<xsl:text>)</xsl:text>
					</xsl:if>
				</xsl:with-param>
				<xsl:with-param name="baseElement" select="$element"/>
			</xsl:call-template>
		</xsl:if>
		
	</xsl:template>


	<!-- remove whitespaces next to content generated by attributes -->
	<xsl:template match="text()[matches(., '^\s+$')]" mode="resolve-cba">
		
		<xsl:choose>
			<xsl:when test="ds:getCbaFlags(parent::*) = $CBA_NO_TEXT">
				<!-- parent contains only no-text nodes -->
			</xsl:when>
			<xsl:when test="empty(preceding-sibling::node()) and exists(parent::*/@cba:prefix)">
				<!-- first node within an element with a prefix -->
			</xsl:when>
			<xsl:when test="exists(preceding-sibling::node()[1]/@cba:suffix)">
				<!-- following node of an element with a suffix --> 
			</xsl:when>
			<xsl:when test="(ds:getCbaFlags(preceding-sibling::node()[1]) = $CBA_FLAG_CSLI) and
							(ds:getCbaFlags(following-sibling::node()[1]) = $CBA_FLAG_CSLI)">
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
			<xsl:when test="empty(following-sibling::node()) and 
				empty(parent::*/following-sibling::node()[not(self::text()[matches(., '^\s+$')])]) and
				empty(parent::*/parent::*/following-sibling::node()[not(self::text()[matches(., '^\s+$')])]) and
				exists(parent::*/parent::*/parent::*/@cba:suffix)">
				<!-- last node within the last element within the last element within an element with a suffix -->
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<xsl:template name="handle-style-flags">
		<xsl:param name="flags" 			as="xs:string*" select="ds:getCbaFlags(.)"/>
		<xsl:param name="content" 			as="node()*"/>
		<xsl:param name="isDefaultContent"	as="xs:boolean"	select="false()"/>
		
		<xsl:choose>
			<xsl:when test="($isDefaultContent) and ($flags = $CBA_FLAG_DEFAULT_ITALIC)">
				<i class="{$CP_I}">
					<xsl:call-template name="cba-marker"/>
					<xsl:sequence select="$content"/>
				</i>
			</xsl:when>
			<xsl:when test="$flags = $CBA_FLAG_CODE">
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
		<xsl:variable name="prev" as="node()*" select="preceding-sibling::node()[not(self::text()[matches(., '^\s+$')])][not(ds:isHidden(.))]"/>
		<xsl:if test="(ds:getCbaFlags(.) = $CBA_FLAG_CSLI) and (ds:getCbaFlags($prev[last()]) = $CBA_FLAG_CSLI)">
			<xsl:call-template name="cba-ph">
				<xsl:with-param name="content" as="node()">
					<xsl:text>, </xsl:text>
				</xsl:with-param>
				<xsl:with-param name="delimiterPrev" select="$prev[ds:getCbaFlags(.) = $CBA_FLAG_CSLI]"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="copy-filter-attr" as="attribute()*">
		<xsl:copy-of select="@audience | @product | @plattform | @props | @otherprops"/>
	</xsl:template>
	
	
	<xsl:template name="merge-filter-attr-for-delimiter" as="attribute()*">
		<xsl:param name="baseElement" 	as="element()"/>
		<xsl:param name="delimiterPrev" as="element()+"/>

		<!-- element needs to remain, when base-element and at least one previous element is present -->
		
		<xsl:variable name="prevAttr" as="attribute()*">
			<xsl:call-template name="merge-filter-attr-any">
				<xsl:with-param name="elements" select="$delimiterPrev"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:for-each select="$FILTER_ATTR_LIST">
			<xsl:variable name="vals1" as="xs:string*" select="tokenize($baseElement/@*[name(.) = current()], '\s+')"/>
			<xsl:variable name="vals2" as="xs:string*" select="tokenize($prevAttr[name(.) = current()], '\s+')"/>
			<xsl:choose>
				<xsl:when test="empty($vals1) and empty($vals2)">
					<!-- no values -->
				</xsl:when>
				<xsl:when test="empty($vals2)">
					<xsl:attribute name="{.}" select="string-join($vals1, ' ')"/>
				</xsl:when>
				<xsl:when test="empty($vals1)">
					<xsl:attribute name="{.}" select="string-join($vals2, ' ')"/>
				</xsl:when>
				<xsl:otherwise>
					<!-- take intersection of both lists -->
					<xsl:attribute name="{.}" select="string-join($vals1[. = $vals2], ' ')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	
	<xsl:template name="merge-filter-attr-for-wrapper" as="attribute()*">
		<xsl:param name="elements" as="element()+"/>
		<!-- element needs to remain, when any of the elements are present -->
		
		<xsl:call-template name="merge-filter-attr-any">
			<xsl:with-param name="elements" select="$elements"/>
		</xsl:call-template>
	</xsl:template>
	
	
	<xsl:template name="merge-filter-attr-any" as="attribute()*">
		<xsl:param name="elements" as="element()+"/>
		
		<xsl:for-each select="$FILTER_ATTR_LIST">
			<xsl:choose>
				<xsl:when test="$elements[string(@*[name(.) = current()]) = '']">
					<!-- no values -->
				</xsl:when>
				<xsl:otherwise>
					<!-- take intersection of alls lists -->
					<xsl:attribute name="{.}" select="string-join(distinct-values(for $i in $elements return tokenize($i/@*[name(.) = current()], '\s+')), ' ')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	

	<xsl:template match="@cba:*" mode="resolve-cba">
		<!-- remove these attributes -->
	</xsl:template>
	

	<xsl:function name="ds:createCbaPhrase">
		<xsl:param name="attribute" 	as="attribute()?"/>

		<xsl:sequence select="ds:createCbaPhrase($attribute, false())"/>
	</xsl:function>
	
	<xsl:function name="ds:createCbaPhrase">
		<xsl:param name="attribute" 	as="attribute()?"/>
		<xsl:param name="italic" 		as="xs:boolean"/>
		
		<xsl:if test="exists($attribute)">
			<xsl:call-template name="cba-ph">
				<xsl:with-param name="content" as="node()*">
					<xsl:for-each select="$attribute/parent::*">	<!-- set context -->
						<xsl:value-of select="$attribute"/>
					</xsl:for-each>
				</xsl:with-param>
				<xsl:with-param name="baseElement"	select="$attribute/parent::*"/>
				<xsl:with-param name="italic" 		select="$italic"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:function>

	<xsl:template name="cba-ph">
		<xsl:param name="content" 		as="node()*"/>
		<xsl:param name="baseElement" 	as="element()"	select="."/>
		<xsl:param name="delimiterPrev" as="element()*"/>
		<xsl:param name="italic" 		as="xs:boolean" select="false()"/>
		
		<xsl:variable name="filterAttr" as="attribute()*">
			<xsl:choose>
				<xsl:when test="exists($delimiterPrev)">
					<xsl:call-template name="merge-filter-attr-for-delimiter">
						<xsl:with-param name="baseElement" 		select="$baseElement"/>
						<xsl:with-param name="delimiterPrev" 	select="$delimiterPrev"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="$baseElement">
						<xsl:call-template name="copy-filter-attr"/>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="$italic">
				<i class="{$CP_I}">
					<xsl:sequence select="$filterAttr"/>
					<xsl:call-template name="cba-marker"/>
					<xsl:sequence select="$content"/>
				</i>
			</xsl:when>
			<xsl:when test="($wrap-cba-ph) or exists($filterAttr)">
				<ph class="{$CP_PH}">
					<xsl:sequence select="$filterAttr"/>
					<xsl:call-template name="cba-marker"/>
					<xsl:sequence select="$content"/>
				</ph>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="$content"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
		
</xsl:stylesheet>
