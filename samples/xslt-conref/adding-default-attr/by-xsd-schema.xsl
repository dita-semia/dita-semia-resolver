<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi	= "http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
    exclude-result-prefixes="#all">

    <xsl:template match="/">
    	
    	<section xsi:noNamespaceSchemaLocation="urn:oasis:names:tc:dita:xsd:topic.xsd:1.2">
    		<title>By assigning an XSD schema</title>
    		<p>Just add <codeph>xsi:noNamespaceSchemaLocation="urn:oasis:names:tc:dita:xsd:topic.xsd"</codeph> to the root element.</p>
    		<p>When the returned root element does not have a class attribute this will automatically trigger a reparsing of the content to ad the default attributes from the schema.</p>
        </section>
    	
    </xsl:template>

</xsl:stylesheet>
