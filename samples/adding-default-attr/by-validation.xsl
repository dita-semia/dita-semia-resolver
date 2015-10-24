<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs" >
	
	<!-- currently the xslt-conref resolver does not support urns... -->
	<!--<xsl:import-schema schema-location="urn:oasis:names:tc:dita:xsd:topic.xsd"/>-->
	<xsl:import-schema>
		<xs:schema elementFormDefault="qualified">
			
			<xs:element name="section">
				<xs:complexType>
					<xs:sequence>
						<xs:element ref="title"/>
						<xs:choice minOccurs="0" maxOccurs="unbounded">
							<xs:element ref="p"/>
						</xs:choice>
					</xs:sequence>
					<xs:attribute name="class" default="- topic/section "/>
				</xs:complexType>
			</xs:element>
			
			<xs:element name="title">
				<xs:complexType mixed="true">
					<xs:attribute name="class" default="- topic/title "/>
				</xs:complexType>
			</xs:element>
			
			<xs:element name="p">
				<xs:complexType mixed="true">
					<xs:choice minOccurs="0" maxOccurs="unbounded">
						<xs:element ref="codeph"/>
					</xs:choice>
					<xs:attribute name="class" default="- topic/p "/>
				</xs:complexType>
			</xs:element>
			
			<xs:element name="codeph">
				<xs:complexType mixed="true">
					<xs:attribute name="class" default="+ topic/ph pr-d/codeph "/>
				</xs:complexType>
			</xs:element>
			
		</xs:schema>
	</xsl:import-schema>
	
	
    <xsl:template match="/">
    	
    	<section xsl:validation="strict">
    		<title>By using the xsl:validation attribute</title>
    		<p>You need to import an xsd schema and add <codeph>@xsl:validation="strict"</codeph> to your root element.</p>
    		<p>A great advantage is that you get some degree of validation during writing the script.
    			But finding the origin of a validation error in a more complex script can be extremly annoying.
    		</p>
        </section>
    	
    </xsl:template>

</xsl:stylesheet>
