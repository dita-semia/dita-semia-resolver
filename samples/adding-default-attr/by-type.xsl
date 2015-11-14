<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="#all">
	
	<xsl:import-schema>
		<xs:schema>
			<xs:include schemaLocation="urn:oasis:names:tc:dita:xsd:topic.xsd"/>
			<xs:complexType name="section.type">
				<xs:complexContent>
					<xs:extension base="section.class">
						<xs:attribute ref="class" default="- topic/section "/>
					</xs:extension>
				</xs:complexContent>
			</xs:complexType>
		</xs:schema>
	</xsl:import-schema>
	
    <xsl:template match="/">
    	<section xsl:type="section.type">
    		<title>By using the xsl:type attribute</title>
    		<p>You need to import an xsd schema and add @xsl:type="xxx" to your root element.</p>
    		<p>A great advantage is that you get some degree of validation during writing the script.</p>
    		<p>But finding the origin of a validation error in a more complex script can be extremly annoying.</p>
        </section>
    </xsl:template>

</xsl:stylesheet>
