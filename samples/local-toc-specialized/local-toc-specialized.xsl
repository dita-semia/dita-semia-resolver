<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:saxon = "http://saxon.sf.net/"
    exclude-result-prefixes="#all">
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:param name="xPathToXsltConref" as="xs:string" select="'/*[1]'"/>
	
	<xsl:include href="../../xsl/class.xsl"/>
	
    <xsl:template match="/">
    	
    	<xsl:variable name="xsltConref" as="element()" select="saxon:evaluate($xPathToXsltConref)"/>
    	<xsl:variable name="baseTopic"	as="element()?" select="($xsltConref/ancestor::reference)[1]"/>

        <local-toc class="+ topic/ul dita-semia/local-toc ">
        	<xsl:for-each select="$baseTopic/reference">
                <li class="{$CLASS_LI}">
                	<title class="{$CLASS_TITLE}">
                    	<xsl:value-of select="title"/>
                    </title>
                    <xsl:if test="exists(shortdesc)">
                    	<p class="{$CLASS_P}">
                    		<xsl:copy-of select="shortdesc/node()"/>
                    	</p>
                    </xsl:if>
                    <xsl:if test="exists(@id)">
                    	<p class="{$CLASS_P}">
                    		<xsl:text>(Details: </xsl:text>
                    		<xref href="#{@id}" format="dita" class="{$CLASS_XREF}">
                    			<xsl:value-of select="title"/>	<!-- The DITA link text resolver can't handle the referenced topics!? -->
                    		</xref>
                    		<xsl:text>)</xsl:text>
                    	</p>
                    </xsl:if>
                </li>
            </xsl:for-each>
        </local-toc>
    	
    </xsl:template>
</xsl:stylesheet>
