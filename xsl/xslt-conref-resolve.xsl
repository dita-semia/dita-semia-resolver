<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:err	= "http://www.w3.org/2005/xqt-errors"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	xmlns:xcrsr	= "http://www.dita-semia.org/xslt-conref/saxon-resolver"
	xmlns:xcrcp	= "http://www.dita-semia.org/xslt-conref/custom-parameter"
	expand-text					= "yes"
	exclude-result-prefixes		= "xs"
	extension-element-prefixes	= "xcrsr">

    <xsl:mode name="#default" on-no-match="shallow-copy"/>
    
    <xsl:template match="*[@xcr:xsl]">
    	
    	<xsl:try>
	    	<xsl:variable name="Resolved" as="element()">
	    		<xcrsr:resolve select="."/>
	    	</xsl:variable>
    		<xsl:message select="$Resolved"></xsl:message>
	    	
	    	<xsl:if test="name($Resolved) != 'no-content'">
		    	<xsl:copy>
		    		
		    		<!-- keep all attributes not related to xslt-conref -->
		    		<xsl:copy-of select="@* except (@xcr:*, @xcrcp:*)"/>
		    		
		    		<!-- copy all attributes from resolved element that don't exist in the original element and are not in the xc-namespace -->
		    		<xsl:variable name="ExistingAttrNameList" as="xs:string*" select="for $i in @* return name($i)"/>
		    		<xsl:copy-of select="$Resolved/@*[not(name(.) = $ExistingAttrNameList)] except $Resolved/@xcr:*"/>
		
		    		<!-- copy all content -->
		    		<xsl:copy-of select="$Resolved/node()"/>
		    		
		    	</xsl:copy>
	    	</xsl:if>
    		
    		<xsl:catch>
    			<xsl:message>{$err:description}</xsl:message>
    			<xsl:message>(in {base-uri()})</xsl:message>
    		</xsl:catch>
    	</xsl:try>

    </xsl:template>
	
</xsl:stylesheet>