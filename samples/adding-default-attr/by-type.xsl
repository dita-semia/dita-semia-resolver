<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="#all" >
	
	<!-- currently the xslt-conref resolver does not support urns... -->
	<!--<xsl:import-schema schema-location="urn:oasis:names:tc:dita:xsd:topic.xsd"/>-->
	<xsl:import-schema>
		<xs:schema elementFormDefault="qualified">
			
			<xs:complexType name="section.class">
				<xs:sequence>
					<xs:element ref="title" minOccurs="0"/>
					<xs:choice minOccurs="0" maxOccurs="unbounded">
						<xs:element ref="p"/>
					</xs:choice>
				</xs:sequence>
				<xs:attribute name="class" default="- topic/section "/>
			</xs:complexType>
			
			<xs:element name="title">
				<xs:complexType mixed="true">
					<xs:attribute name="class" default="- topic/title "/>
				</xs:complexType>
			</xs:element>
			
			<xs:element name="p">
				<xs:complexType mixed="true">
					<xs:attribute name="class" default="- topic/p "/>
				</xs:complexType>
			</xs:element>
			
		</xs:schema>
	</xsl:import-schema>
	
	
    <xsl:template match="/">
    	
    	<section xsl:type="section.class">
    		<title>By using the xsl:type attribute</title>
    		<p>You need to import an xsd schema and add @xsl:type="xxx" to your root element.</p>
    		<p>A great advantage is that you get some degree of validation during writing the script.</p>
    		<p>But finding the origin of a validation error in a more complex script can be extremly annoying.</p>
        </section>
    	
    </xsl:template>

</xsl:stylesheet>
