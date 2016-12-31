<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:cba	= "http://www.dita-semia.org/conbat"
	exclude-result-prefixes		= "#all">
	
	<xsl:include href="../class.xsl"/>
	
	<xsl:variable name="NUM_DELIMITER" as="xs:string" select="'.'"/>
	
	<xsl:template match="*" priority="10">
		<xsl:variable name="numList" as="xs:string*">
			<xsl:next-match/>
		</xsl:variable>
		<xsl:value-of select="$numList" separator=""/>
	</xsl:template>

	<xsl:template match="/*[contains(@class, $C_TOPIC)]" priority="3">
		<!-- no local number for root topic - number comes from global map context -->
	</xsl:template>
	
	<xsl:template match="*[contains(@class, $C_TOPIC)]" priority="2">
		<xsl:apply-templates select="parent::*"/>
		<xsl:sequence select="$NUM_DELIMITER"/>
		<xsl:value-of select="count(preceding-sibling::*[contains(@class, $C_TOPIC)]) + 1"/>
	</xsl:template>
	
	<xsl:template match="*" priority="1">
		<xsl:apply-templates select="parent::*"/>
	</xsl:template>

</xsl:stylesheet>
