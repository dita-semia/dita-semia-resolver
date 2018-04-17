<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:cba	= "http://www.dita-semia.org/conbat"
	xmlns:svg	= "http://www.w3.org/2000/svg"
	exclude-result-prefixes		= "#all">

	<!-- remove the wrapper in last stage -->
	<xsl:template match="*[contains(@class, $C_TOPIC_CONTAINER)]" mode="resolve-cba">
		<xsl:apply-templates mode="#current"/>
	</xsl:template>

	<!-- dlentry with missing dd wrapper in last stage -->
	<xsl:template match="*[contains(@class, $C_DLENTRY)][*[not(contains(@class, $C_DLENTRY) or contains(@class, $C_DT) or contains(@class, $C_DD))]]" priority="5" mode="resolve-cba">
		<xsl:copy>
			<xsl:apply-templates select="attribute()" mode="#current"/>
			<xsl:for-each-group select="element()" group-adjacent="string(tokenize(@class, '\s+')[2])">
				<xsl:choose>
					<xsl:when test="current-grouping-key() = ('topic/dt', 'topic/dd')">
						<xsl:apply-templates select="current-group()" mode="#current"/>
					</xsl:when>
					<xsl:otherwise>
						<dd class="+ topic/dd ">
							<xsl:apply-templates select="current-group()" mode="#current"/>
						</dd>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each-group>
		</xsl:copy>
	</xsl:template>


	<xsl:template name="DotXMessage">
		<xsl:param name="node"		as="node()?"	select="."/>
		<xsl:param name="type"		as="xs:string?" select="'INFO'"/>
		<xsl:param name="message"/>
		
		<xsl:variable name="pos"	as="xs:string?" select="substring-after($node/@xtrc, ';')"/>
		<xsl:message>
			<xsl:call-template name="getLocation">
				<xsl:with-param name="node" select="$node"/>
			</xsl:call-template>
			<xsl:text>: [DOTX][</xsl:text>
			<xsl:value-of select="$type"/>
			<xsl:text>]:</xsl:text>
			<xsl:copy-of select="$message"/>
		</xsl:message>
	</xsl:template>


	<xsl:template name="getLocation">
		<xsl:param name="node"		as="node()?"	select="."/>
		
		<xsl:choose>
			<xsl:when test="empty($node)"/>	<!-- no location -->
			<xsl:when test="empty(@xtrf) or empty(@xtrf)">
				<xsl:call-template name="getLocation">
					<xsl:with-param name="node" select="$node/parent::node()"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="contains(@xtrf, 't1/t2/t3/t4/t5')">
				<!-- handle fix.external.refs.com.oxygenxml -->
				<xsl:value-of select="concat($basedir, substring-after($node/@xtrf, 't1/t2/t3/t4/t5'))"/>
				<xsl:text>:</xsl:text>
				<xsl:value-of select="substring-after($node/@xtrc, ';')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@xtrf"/>
				<xsl:text>:</xsl:text>
				<xsl:value-of select="substring-after($node/@xtrc, ';')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
