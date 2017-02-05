<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	xmlns:ikd	= "http://www.dita-semia.org/implicit-keydef"
	xmlns:akr	= "http://www.dita-semia.org/advanced-keyref"
    exclude-result-prefixes	= "#all">
	
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	
	<xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
    	<ul class="{$CP_UL}">
    		<xsl:for-each select="Data/Entry">
    			<li class="{$CP_LI}" id="id-{position()}" ikd:key-type="Test" ikd:name="'{Name}'">
    				<xsl:value-of select="Key"/>
    			</li>
    		</xsl:for-each>
    	</ul>
    </xsl:template>
	
	
</xsl:stylesheet>
