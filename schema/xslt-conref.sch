<?xml version="1.0" encoding="UTF-8"?>
<sch:schema 
	xmlns:sch	= "http://purl.oclc.org/dsdl/schematron"
	xmlns:sqf	= "http://www.schematron-quickfix.com/validator/process"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	queryBinding="xslt2">
	
	<sch:ns uri="java:org.DitaSemia.JavaBase.FileUtil" 						prefix="java"/>
	<sch:ns uri="http://www.dita-semia.org/xslt-conref/custom-parameter" 	prefix="xcrcp"/>
	<sch:ns uri="http://www.dita-semia.org/xslt-conref" 					prefix="xcr"/>
	
	<sch:pattern>
		<sch:rule context="*[@xcr:xsl]">
			
			<sch:let name="scriptAttr" 	value="@xcr:xsl"/>
			<sch:let name="scriptUrl" 	value="java:resolveUri($scriptAttr, .)"/>
			<sch:let name="sourceAttr" 	value="@xcr:source"/>
			<sch:let name="sourceUrl" 	value="java:resolveUri($sourceAttr, .)"/>
			
			<sch:report test="not(java:fileExists($scriptUrl))">
				Invalid value for @xcr:xsl ('<sch:value-of select="$scriptAttr"/>').
				Not existing file: '<sch:value-of select="java:decodeUrl($scriptUrl)"/>'.
			</sch:report>
			
			<sch:report test="java:fileExists($scriptUrl) and not(java:isValidXsl($scriptUrl))">
				Invalid value for @xcr:xsl ('<sch:value-of select="$scriptAttr"/>').
				No valid XSL file: '<sch:value-of select="java:decodeUrl($scriptUrl)"/>'.
			</sch:report>
			
			<sch:report test="(string($sourceAttr) != '') and not(java:fileExists($sourceUrl))">
				Invalid value for @xcr:source ('<sch:value-of select="$sourceAttr"/>').
				Not existing file: '<sch:value-of select="java:decodeUrl($sourceUrl)"/>'.
			</sch:report>
			
			<sch:report test="(string($sourceAttr) != '') and java:fileExists($sourceUrl) and not(doc-available($sourceUrl))">
				Invalid value for @xcr:source ('<sch:value-of select="$sourceAttr"/>').
				No valid XML file: '<sch:value-of select="java:decodeUrl($sourceUrl)"/>'.
			</sch:report>
		</sch:rule>
		<sch:rule context="*[@xcr:xsl]/@xcrcp:*">
			
			<sch:let name="scriptAttr" 	value="parent::*/@xcr:xsl"/>
			<sch:let name="scriptUrl" 	value="java:resolveUri($scriptAttr, parent::*)"/>
			
			<sch:report test="java:isXslParameterUndefined($scriptUrl, local-name())" role="warn">
				The parameter '<sch:value-of select="local-name()"/>' is undefined.
			</sch:report>
		</sch:rule>
	</sch:pattern>

	
</sch:schema>