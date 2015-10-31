<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="#all">
	
    <xsl:template match="/">
    	
    	<!-- add schema to set the class attributes automatically as default attributes -->
    	<xsl:processing-instruction name="xml-model">href="urn:dita-ng:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>

        <ul>
            <xsl:for-each select="*/reference">
                <li>
                    <title>
                    	<xsl:value-of select="title"/>
                    </title>
                    <xsl:if test="exists(shortdesc)">
                    	<p>
                    		<xsl:copy-of select="shortdesc/node()"/>
                    	</p>
                    </xsl:if>
                    <xsl:if test="exists(@id)">
                    	<p>
                    		<xsl:text>(Details: </xsl:text>
                    		<xref href="#{@id}" format="dita">
                    			<xsl:value-of select="title"/>	<!-- The DITA link text resolver can't handle the referenced topics!? -->
                    		</xref>
                    		<xsl:text>)</xsl:text>
                    	</p>
                    </xsl:if>
                </li>
            </xsl:for-each>
        </ul>
    </xsl:template>
</xsl:stylesheet>
