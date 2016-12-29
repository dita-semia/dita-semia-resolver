<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
    xmlns:dc	= "http://www.dita-semia.org/document-cache"
    exclude-result-prefixes="#all">
	
    <xsl:template match="/">
    	
    	<!-- add schema to set the class attributes automatically as default attributes -->
    	<xsl:processing-instruction name="xml-model">href="urn:oasis:names:tc:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>

    	<xsl:variable name="childTopics" as="element()*" select="dc:getChildTopics(*)"/>
    	<ul xcr:reparse="yes">
    		<xsl:for-each select="$childTopics/self::reference">
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
                    		<xref href="{base-uri(.)}#{@id}" format="dita"/>
                    		<xsl:text>)</xsl:text>
                    	</p>
                    </xsl:if>
                </li>
            </xsl:for-each>
        </ul>
    </xsl:template>

</xsl:stylesheet>
