<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	xmlns:ikd	= "http://www.dita-semia.org/implicit-keydef"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
    exclude-result-prefixes	= "#all">
	
	<xsl:param name="csvFile" as="xs:string"/>
	
	<xsl:include href="urn:dita-semia:xsl:csv-to-xml.xsl"/>
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	
	<xsl:output method="xml" indent="yes"/>
	
    <xsl:template name="ImportCsv">
    	
    	<xsl:variable name="csvCode" 	as="xs:string" 	select="unparsed-text(resolve-uri($csvFile))"/>
    	<xsl:variable name="rowList" 	as="element()+" select="ds:csvToXml($csvCode)"/>
    	<xsl:variable name="colCount" 	as="xs:integer" select="max(for $i in $rowList return count($i/*))"/>
    	
        <simpletable class="{$CP_SIMPLETABLE}" relcolwidth="{for $i in 1 to $colCount return '1.0*'}">
        	<sthead class="{$CP_STHEAD}">
        		<stentry class="{$CP_STENTRY}">Key</stentry>
        		<stentry class="{$CP_STENTRY}">Name</stentry>
        		<stentry class="{$CP_STENTRY}">Description</stentry>
        	</sthead>
        	<xsl:for-each select="$rowList">
        		<xsl:variable name="colList" as="element()*" select="*"/>
        		<strow class="{$CP_STROW}">
        			<stentry class="{$CP_STENTRY}">
        				<ph class="{$CP_PH}" id="key-{position()}" ikd:key-type="CSV" ikd:ref-node="stentry[1]/ph" ikd:root="ancestor::strow" ikd:name="stentry[2]" ikd:desc="stentry[3]">
        					<xsl:value-of select="$colList[1]"/>
        				</ph>
        			</stentry>
        			<stentry class="{$CP_STENTRY}">
        				<xsl:value-of select="$colList[2]"/>
        			</stentry>
        			<stentry class="{$CP_STENTRY}">
        				<xsl:value-of select="$colList[3]"/>
        			</stentry>
        		</strow>
        	</xsl:for-each>
        </simpletable>
    </xsl:template>
	
	
</xsl:stylesheet>
