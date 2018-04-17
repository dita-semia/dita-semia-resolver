<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
    exclude-result-prefixes	= "#all">
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:template match="/">
		
		<xsl:variable name="rowList" 	as="element()+" select="root/sheet[1]/row"/>
		<xsl:variable name="colCount" 	as="xs:integer" select="count($rowList[1]/column)"/>
		
		<!--<xsl:message select="$colCount"/>-->
		
		
		<xsl:processing-instruction name="xml-model">href="urn:oasis:names:tc:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>
		
		<simpletable relcolwidth="{for $i in 1 to $colCount return '1.0*'}" xcr:reparse="yes">
			<xsl:for-each select="$rowList">
				<xsl:variable name="colList" as="element()*" select="*"/>
				<xsl:element name="{if (position() = 1) then 'sthead' else 'strow'}">
					<xsl:for-each select="1 to $colCount">
						<xsl:variable name="colIndex" 	as="xs:integer"	select="."/>
						<xsl:variable name="content" 	as="xs:string?" select="$colList[$colIndex]"/>
						<stentry>
							<xsl:if test="contains($content, '&#x0A;')">
								<xsl:attribute name="xml:space" select="'preserve'"/>
							</xsl:if>
							<xsl:value-of select="$content"/>
						</stentry>
					</xsl:for-each>
				</xsl:element>
			</xsl:for-each>
		</simpletable>
	</xsl:template>
	
</xsl:stylesheet>
