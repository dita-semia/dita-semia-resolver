<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:xsi	= "http://www.w3.org/2001/XMLSchema-instance"
	xmlns:err	= "http://www.w3.org/2005/xqt-errors"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	xmlns:xcrcp	= "http://www.dita-semia.org/xslt-conref/custom-parameter"
	exclude-result-prefixes		= "#all">
    
    <xsl:template match="*[@xcr:xsl]">
    	<xsl:variable name="resolved" as="element()" select="xcr:resolve(.)"/>
    	<xsl:if test="name($resolved) != 'no-content'">
	    	<xsl:copy>
	    		
	    		<!-- keep all attributes -->
	    		<xsl:copy-of select="@*"/>

	    		<!-- copy all attributes from resolved element that don't exist in the original element and are not in the xcr- or xsi-namespace -->
	    		<xsl:variable name="ExistingAttrNameList" as="xs:string*" select="for $i in @* return name($i)"/>
	    		<xsl:copy-of select="$resolved/@*[not(name(.) = $ExistingAttrNameList)] except ($resolved/@xcr:* | $resolved/@xsi:*)"/>
	
	    		<!-- copy all content -->
	    		<xsl:copy-of select="$resolved/node()"/>
	    		
	    	</xsl:copy>
    	</xsl:if>
    </xsl:template>
	
	<xsl:template match="element()">
		<xsl:copy>
			<xsl:apply-templates select="attribute() | node()" mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="attribute() | processing-instruction() | text() | comment()">
		<xsl:copy/>
	</xsl:template>
	
</xsl:stylesheet>
