<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:saxon = "http://saxon.sf.net/"
    exclude-result-prefixes="#all">
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:param name="xPathToXsltConref" as="xs:string" select="'/*[1]'"/>
	
	<xsl:include href="urn:dita-semia:xslt-conref:xsl:class.xsl"/>
	
    <xsl:template match="/">

    	<xsl:variable name="xsltConref" as="element()" select="saxon:evaluate($xPathToXsltConref)"/>
    	<xsl:variable name="baseTopic"	as="element()?" select="($xsltConref/ancestor::*[contains(@class, $C_TOPIC)])[last()]"/>

        <local-toc class="+ topic/ul dita-semia/local-toc ">
        	<xsl:for-each select="$baseTopic/*[contains(@class, $C_TOPIC)]">
                <li class="{$CP_LI}">
                	<title class="{$CP_TITLE}">
                    	<xsl:value-of select="title"/>
                    </title>
                    <xsl:if test="exists(shortdesc)">
                    	<p class="{$CP_P}">
                    		<xsl:copy-of select="shortdesc/node()"/>
                    	</p>
                    </xsl:if>
                    <xsl:if test="exists(@id)">
                    	<p class="{$CP_P}">
                    		<xsl:text>(Details: </xsl:text>
                    		<xref href="#{@id}" format="dita" class="{$CP_XREF}"/>
                    		<xsl:text>)</xsl:text>
                    	</p>
                    </xsl:if>
                </li>
            </xsl:for-each>
        </local-toc>
    	
    </xsl:template>
</xsl:stylesheet>
