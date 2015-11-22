<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:err	= "http://www.w3.org/2005/xqt-errors"
	xmlns:jfile	= "java:org.DitaSemia.JavaBase.FileUtil"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	xmlns:xcrsr	= "http://www.dita-semia.org/xslt-conref/saxon-resolver"
	xmlns:xcrcp	= "http://www.dita-semia.org/xslt-conref/custom-parameter"
	expand-text					= "yes"
	exclude-result-prefixes		= "#all"
	extension-element-prefixes	= "xcrsr">
	
	<xsl:variable name="RESOLVED_FILE_SUFFIX" 	as="xs:string" select="'-RESOLVED'"/>
	<xsl:variable name="CONREF_FILE_SUFFIX" 	as="xs:string" select="'-CONREF'"/>
	<xsl:variable name="CONREF_TOPIC_ID" 		as="xs:string" select="'conref-topic'"/>

    <xsl:mode name="#default" on-no-match="fail"/>
	<xsl:mode name="Resolved" on-no-match="shallow-copy"/>
	


    <!-- new file(s) for each document -->
    <xsl:template match="/">
    	
    	<xsl:variable name="resolvedList" as="element()*">
    		<xsl:call-template name="GetResolvedXsltConrefs"/>
    	</xsl:variable>

    	<xsl:variable name="resolvedPath" as="xs:string"	select="ds:addFileSuffix(base-uri(), $RESOLVED_FILE_SUFFIX)"/>
    	<xsl:message>creating resolved file '{$resolvedPath}'</xsl:message>
    	<xsl:result-document href="{$resolvedPath}">
    		<xsl:apply-templates select="node()">
    			<xsl:with-param name="resolvedList" select="$resolvedList"/>
    		</xsl:apply-templates>
    	</xsl:result-document>
    	
    	<xsl:if test="exists($resolvedList)">
    		<xsl:call-template name="CreateConrefFile">
    			<xsl:with-param name="resolvedList" 	select="$resolvedList"/>
    			<xsl:with-param name="resolvedFilename"	select="tokenize($resolvedPath, '[\\/]')[last()]"/>
    		</xsl:call-template>
    	</xsl:if>

    </xsl:template>


	<!-- recurse into topic refs -->
	<xsl:template match="*[contains(@class, 'map/topicref')]">

		<xsl:apply-templates select="jfile:loadXml(@href, .)" mode="#current"/>
		
		<xsl:next-match/>
		
	</xsl:template>
	
	
	<!-- add the filename suffix to the reference -->
	<xsl:template match="*[contains(@class, 'map/topicref')]/@href">
		<xsl:attribute name="href" select="ds:addFileSuffix(., $RESOLVED_FILE_SUFFIX)"/>
	</xsl:template>


	<!-- convert xslt-conref to conref -->
	<xsl:template match="*[@xslt-conref]">
		<xsl:param name="resolvedList" as="element()*"/>

		<xsl:variable name="conrefId"	as="xs:string"	select="ds:getConrefId(.)"/>
		<xsl:variable name="resolved" 	as="element()" 	select="$resolvedList[@id = $conrefId]"/>

		<!-- convert to resolved element to make sure the conref can be resolved -->
		<xsl:element name="{name($resolved)}">
			<xsl:attribute name="class" 	select="$resolved/@class"/>
			<xsl:attribute name="conref"	select="ds:getConref(., $resolved)"/>
			<xsl:apply-templates select="attribute() except (@class, @xslt-conref, @xslt-conref-source,  @xslt-conref-start,  @xcrcp:*), node()" mode="#current"/>
		</xsl:element>
	</xsl:template>
	
	
	<!-- identity transform with passing resolvedList -->
	<xsl:template match="element()">
		<xsl:param name="resolvedList" as="element()*"/>
		
		<xsl:copy>
			<xsl:apply-templates select="attribute(), node()">
				<xsl:with-param name="resolvedList" select="$resolvedList"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="attribute() | processing-instruction() | text() | comment()">
		<xsl:copy/>
	</xsl:template>


	<!-- ========== Tempalte: GetResolvedXsltConrefs ========== -->
	<xsl:template name="GetResolvedXsltConrefs" as="element()*">
		<xsl:for-each select="//*[@xslt-conref]">
			<xsl:variable name="resolved" as="element()">
				<xcrsr:resolve select="."/>	
			</xsl:variable>
			<xsl:variable name="conrefId" as="xs:string" select="ds:getConrefId(.)"/>
			<xsl:for-each select="$resolved">
				<xsl:choose>
					<xsl:when test="self::no-content">
						<draft-comment class="- topic/draft-comment " id="{$conrefId}">
							<xsl:sequence select="node()"/>
						</draft-comment>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy>
							<xsl:attribute name="id" select="$conrefId"/>
							<xsl:sequence select="attribute(), node()"/>
						</xsl:copy>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>


	<!-- ========== Tempalte: CreateConrefFile ========== -->
	<xsl:template name="CreateConrefFile">
		<xsl:param name="resolvedList" 		as="element()+"/>
		<xsl:param name="resolvedFilename"	as="xs:string"/>

		<xsl:variable name="conrefPath" as="xs:string"	select="ds:addFileSuffix(base-uri(), $CONREF_FILE_SUFFIX)"/>
		<xsl:message>creating conref file '{$conrefPath}'</xsl:message>
		<xsl:result-document href="{$conrefPath}">
			
			<xsl:variable name="xsltConrefTopicList" as="element()*" select="$resolvedList[contains(@class, ' topic/topic ')]"/>
			<xsl:variable name="xsltConrefBlockList" as="element()*" select="$resolvedList except $xsltConrefTopicList"/>
			
			<topic id="{$CONREF_TOPIC_ID}" class="- topic/topic ">
				<xsl:copy-of select="*/@domains, */@ditaarch:DITAArchVersion" xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/"/>
				<title class="- topic/title ">resolved XSLT-Conref elements for file {tokenize(base-uri(), '[\\/]')[last()]}</title>
				
				<xsl:if test="exists($xsltConrefBlockList)">
					<body class="- topic/body ">
						<xsl:apply-templates select="$xsltConrefBlockList" mode="Resolved">
							<xsl:with-param name="resolvedFilename" select="$resolvedFilename" tunnel="yes"/>
						</xsl:apply-templates>
					</body>
				</xsl:if>
				<xsl:apply-templates select="$xsltConrefTopicList" mode="Resolved">
					<xsl:with-param name="resolvedFilename" select="$resolvedFilename" tunnel="yes"/>
				</xsl:apply-templates>
				
			</topic>
			
		</xsl:result-document>	
	</xsl:template>
	
	
	<!-- fix references to lokal file -->
	<xsl:template match="@href[matches(., '^#')]" mode="Resolved">
		<xsl:param name="resolvedFilename" as="xs:string" tunnel="yes"/>
		
		<xsl:attribute name="href" select="concat($resolvedFilename, .)"/>
	</xsl:template>
	

	<!-- ========== Function: ds:getConref ========== -->
	<xsl:function name="ds:getConref" as="xs:string">
		<xsl:param name="xsltConrefElement"	as="element()"/>
		<xsl:param name="resolvedElement"	as="element()"/>
		
		<xsl:variable name="inputFilename"	as="xs:string"	select="tokenize(base-uri($xsltConrefElement), '[\\/]')[last()]"/>
		<xsl:variable name="conrefFilename"	as="xs:string"	select="ds:addFileSuffix($inputFilename, $CONREF_FILE_SUFFIX)"/>
		<xsl:variable name="topicIdPrefix"	as="xs:string?"	select="if (contains($resolvedElement/@class, ' topic/topic ')) then () else concat($CONREF_TOPIC_ID, '/')"/>
		<xsl:variable name="conrefId"		as="xs:string"	select="ds:getConrefId($xsltConrefElement)"/>
			
		<xsl:sequence select="concat($conrefFilename, '#', $topicIdPrefix, $conrefId)"/>
	</xsl:function>
	

	<!-- ========== Function: ds:getConrefId ========== -->
	<xsl:function name="ds:getConrefId" as="xs:string">
		<xsl:param name="xsltConrefElement"	as="element()"/>
		
		<xsl:sequence select="string-join(for $i in $xsltConrefElement/ancestor-or-self::* return string(count($i/preceding-sibling::*)), '-')"/>
	</xsl:function>

	<!-- ========== Function: ds:addFileSuffix ========== -->
	<xsl:function name="ds:addFileSuffix" as="xs:string">
		<xsl:param name="path"		as="xs:string"/>
		<xsl:param name="suffix"	as="xs:string"/>
		
		<xsl:variable name="pathWithoutExtension"	as="xs:string"	select="replace($path, '[.][^.]+$', '')"/>
		<xsl:variable name="pathExtension"			as="xs:string?"	select="substring($path, string-length($pathWithoutExtension) + 1)"/>
		
		<xsl:sequence select="concat($pathWithoutExtension, $suffix, $pathExtension)"/>
	</xsl:function>

</xsl:stylesheet>