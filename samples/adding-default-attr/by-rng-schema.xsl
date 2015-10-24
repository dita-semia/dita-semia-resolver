<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi	= "http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    xmlns:xc	= "http://www.dita-semia.org/xsl-conref"
    exclude-result-prefixes="xs" >

    <xsl:template match="/">
    	
    	<xsl:processing-instruction name="xml-model">href="urn:dita-ng:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>
    	
    	<section xc:reparse="yes">
    		<title>By assigning an rng schema</title>
    		<p>Just add <codeph>&lt;?xml-model href="urn:dita-ng:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"?&gt;</codeph> result document.</p>
    		<p>Additionally you should set the attribute <codeph>xc:reparse</codeph> to "yes".
    			This will ensure that the result is converted to a string first and be parsed again adding the default attributes.
    			Otherwise the xslt-conref-resolver might skip this step when the two xml representations are compatible.
    		</p>
        </section>
    	
    </xsl:template>

</xsl:stylesheet>
