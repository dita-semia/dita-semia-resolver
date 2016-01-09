<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs" >

    <xsl:template match="/">
        
        <xsl:variable name="invalidValue" as="element()" select="Not-Existing-Element"/>
    	<xsl:sequence select="$invalidValue"/>
    	
	</xsl:template>

</xsl:stylesheet>
