<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:xsi	= "http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	xmlns:xcrp	= "http://www.dita-semia.org/xslt-conref/custom-parameter"
	exclude-result-prefixes		= "#all">
    
    <xsl:template match="*[@xcr:xsl]" mode="resolve-xcr">
    	<xsl:choose>
    		<xsl:when test="tokenize(@xcr:flags, '\s+') = 'copy'">
    			<xsl:copy>
    				
    				<xsl:call-template name="setBaseUri"/>
    				
    				<!-- copy all other attributes -->
    				<xsl:copy-of select="@* except (@xcr:* | @xcrp:*)"/>
    				
    				<!-- process all content -->
    				<xsl:apply-templates select="node()" mode="#current"/>
    				
    			</xsl:copy>
    		</xsl:when>
    		<xsl:otherwise>
    			<xsl:variable name="resolved" as="element()" select="xcr:resolve(.)"/>
    			<xsl:if test="name($resolved) != 'no-content'">
    				<xsl:choose>
    					<xsl:when test="contains(@class, $C_TOPIC_CONTAINER)">
    						
    						<!-- don't copy wrapper -->
    						
    						<!-- process all content -->
    						<xsl:apply-templates select="$resolved/node()" mode="#current"/>
    						
    					</xsl:when>
    					<xsl:otherwise>
    						<xsl:copy>
    							
    							<xsl:call-template name="setBaseUri"/>
    							
    							<!-- copy all other attributes -->
    							<xsl:copy-of select="@* except (@xcr:* | @xcrp:*)"/>
    							
    							<!-- copy all attributes from resolved element that don't exist in the original element and are not in the xcr- or xsi-namespace -->
    							<xsl:variable name="ExistingAttrNameList" as="xs:string*" select="for $i in @* return name($i)"/>
    							<xsl:copy-of select="$resolved/@*[not(name(.) = $ExistingAttrNameList)] except ($resolved/@xcr:* | $resolved/@xsi:*)"/>
    							
    							<!-- resolve all content -->
    							<xsl:apply-templates select="$resolved/node()" mode="#current"/>
    							
    						</xsl:copy>
    					</xsl:otherwise>
    				</xsl:choose>
    			</xsl:if>
    		</xsl:otherwise>
    	</xsl:choose>
    </xsl:template>
	
</xsl:stylesheet>
