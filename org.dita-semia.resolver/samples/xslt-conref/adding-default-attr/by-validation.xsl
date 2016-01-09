<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="#all">

	<xsl:import-schema schema-location="urn:oasis:names:tc:dita:xsd:topic.xsd"/>

    <xsl:template match="/">

    	<section xsl:validation="strict">
    		<title>By using the <codeph>xsl:validation</codeph> attribute</title>
    		<p>You need to import an XSD schema and add <codeph>@xsl:validation="strict"</codeph> to your root element.</p>
    		<p>A great advantage is that you get some degree of validation during writing the script.
    			But finding the origin of a validation error in a more complex script can be extremely annoying.
    		</p>
        </section>
    	
    </xsl:template>

</xsl:stylesheet>
