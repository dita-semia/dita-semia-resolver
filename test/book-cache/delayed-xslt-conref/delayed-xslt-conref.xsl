<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	xmlns:ikd	= "http://www.dita-semia.org/implicit-keydef"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
    exclude-result-prefixes	= "#all">
	
	<xsl:param name="xcr:current-uri" as="xs:anyURI"/>
	
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	
	<xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
    	
    	<xsl:variable name="keyRefStrings" as="xs:string*" select="akr:getMatchingKeyDefs('Test', (), $xcr:current-uri)"/>
    	
    	<ul class="{$CP_UL}">
    		<xsl:for-each select="$keyRefStrings">
    			<xsl:variable name="path"	as="xs:string" select="substring-after(., ':')"/>
    			<li class="{$CP_LI}" id="id-{position()}">
    				<ph class="{$CP_PH}" ikd:key-type="Test-Child" ikd:root="parent::li" ikd:namespace="'{$path}'">
    					<xsl:text>Child</xsl:text>
    				</ph>
    				<xsl:text/> of <xsl:value-of select="$path"/><xsl:text/>
    			</li>
    		</xsl:for-each>
    	</ul>
    </xsl:template>
	
	
</xsl:stylesheet>
