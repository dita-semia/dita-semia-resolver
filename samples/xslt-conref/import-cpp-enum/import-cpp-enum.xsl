<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
    exclude-result-prefixes	= "#all">
	
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:template match="text()">
    	
    	<xsl:variable name="cppCode" as="xs:string" select="."/>
    	
    	<xsl:processing-instruction name="xml-model">href="urn:oasis:names:tc:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>
    	
		<ul xcr:reparse="yes">
        	<xsl:analyze-string select="$cppCode" regex="enum\s+([a-zA-Z0-0_]+)\s*\{{([^\}}]+)\}}">
        		<xsl:matching-substring>
        			<li>
        				<title>
        					<xsl:text>Enumeration </xsl:text>
        					<codeph> 
        						<xsl:value-of select="regex-group(1)"/>
        					</codeph>
        				</title>
        				<sl>
        					<xsl:call-template name="CreateEnum">
        						<xsl:with-param name="enumList" select="tokenize(regex-group(2), ',')"/>
        					</xsl:call-template>
        				</sl>
        			</li>		
        		</xsl:matching-substring>
        	</xsl:analyze-string>
        	
        </ul>
    </xsl:template>
	
	<xsl:template name="CreateEnum" as="element(sli)*">
		<xsl:param name="enumList"	as="xs:string+"/>
		<xsl:param name="prevValue"	as="xs:integer"	select="-1"/>
		
		<xsl:variable name="enum"	as="xs:string" select="replace($enumList[1], '\s+', '')"/>
		<xsl:variable name="name"	as="xs:string" select="replace($enum, '=.*$', '')"/>
		<xsl:variable name="value"	as="xs:integer">
			<xsl:analyze-string select="$enum" regex="^[^=]+=([-+]?[0-9]+)$">
				<xsl:matching-substring>
					<xsl:sequence select="xs:integer(regex-group(1))"/>
				</xsl:matching-substring>
				<xsl:non-matching-substring>
					<xsl:sequence select="$prevValue + 1"/>
				</xsl:non-matching-substring>
			</xsl:analyze-string>
		</xsl:variable>
		
		<sli>
			<xsl:value-of select="$value"/>
			<xsl:text> – </xsl:text>
			<codeph>
				<xsl:value-of select="$name"/>
			</codeph>
		</sli>
		
		<xsl:if test="count($enumList) > 1">
			<xsl:call-template name="CreateEnum">
				<xsl:with-param name="enumList" 	select="$enumList[position() > 1]"/>
				<xsl:with-param name="prevValue"	select="$value"/>
			</xsl:call-template>
		</xsl:if>
		
	</xsl:template>
	
</xsl:stylesheet>
