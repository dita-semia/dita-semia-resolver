<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="#all">
	
	<xsl:include href="urn:dita-semia:xslt-conref:xsl:class.xsl"/>
	
	<xsl:variable name="ATTR_P" as="attribute()">
		<xsl:attribute name="class" select="$CP_P"/>
	</xsl:variable>
	
	<xsl:variable name="ATTR_HTML_XREF" as="attribute()+">
		<xsl:attribute name="class" 	select="$CP_XREF"/>
		<xsl:attribute name="format"	select="'html'"/>
		<xsl:attribute name="scope" 	select="'external'"/>
	</xsl:variable>


    <xsl:template match="/">
    	
    	<section class="- topic/section ">
    		<title class="- topic/title ">Explicitly</title>
    		<p class="- topic/p ">The class attribute is set explicitly for each created element.</p>
    		<p class="{$CP_P}">You can use constants within you attributes for this as well to avoid the risk of typos.</p>
    		<p class="{$CP_P}">There is a file class.xsl that defines constants for (probably) the most common classes.</p>
    		<p>
    			<xsl:copy-of select="$ATTR_P"/>
    			<xsl:text>Furthermore you can define constants for the complete attribute or even for multiple attributes when appropriate. </xsl:text>
    			<xsl:text>E.g. for HTML references: </xsl:text>
    			<xref>
    				<xsl:copy-of select="$ATTR_HTML_XREF"/>
    				<xsl:attribute name="href" select="'www.dita-semia.org'"/>
    			</xref>
    		</p>
    		<ThisIsACustomPElement class="{$CP_P}">Note that with this approach the actual name of your elements usually doesn't matter...</ThisIsACustomPElement>
        </section>
    	
    </xsl:template>

</xsl:stylesheet>
