<?xml version="1.0" encoding="UTF-8"?>
<sch:schema 
	xmlns:sch	= "http://purl.oclc.org/dsdl/schematron"
	xmlns:sqf	= "http://www.schematron-quickfix.com/validator/process"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:ds	= "http://www.dita-semia.org"
	queryBinding="xslt2">
	
	<sch:ns uri="http://www.dita-semia.org" prefix="ds"/>
	
	<xsl:include href="urn:dita-semia:xslt-conref:xsl:check-xslt-conref.xsl"/>

	<sch:pattern>
		<sch:rule context="*[@xslt-conref]">
			<sch:assert test="ds:isScriptValid(resolve-uri(@xslt-conref, base-uri(.)))">
				Invalid value for @xslt-conref: '<sch:value-of select="@xslt-conref"/>'.
				The URL of a valid xsl script is required.
				<sch:value-of select="resolve-uri(@xslt-conref, base-uri(.))"/>
			</sch:assert>
			
		</sch:rule>
	</sch:pattern>
	
</sch:schema>