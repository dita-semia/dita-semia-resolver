<?xml version="1.0" encoding="UTF-8"?>
<sch:schema 
	xmlns:sch	= "http://purl.oclc.org/dsdl/schematron"
	xmlns:sqf	= "http://www.schematron-quickfix.com/validator/process"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	queryBinding="xslt2">
	
	<sch:ns uri="java:org.DitaSemia.JavaBase.FileUtil" prefix="java"/>
	
	<sch:pattern>
		<sch:rule context="*[@xslt-conref]">
			
			<sch:let name="scriptAttr" 	value="@xslt-conref"/>
			<sch:let name="scriptUrl" 	value="java:resolveUri($scriptAttr, .)"/>
			<sch:let name="sourceAttr" 	value="@xslt-conref-source"/>
			<sch:let name="sourceUrl" 	value="java:resolveUri($sourceAttr, .)"/>
			
			<sch:report test="not(java:fileExists($scriptUrl))">
				Invalid value for @xslt-conref ('<sch:value-of select="$scriptAttr"/>').
				Not existing file: '<sch:value-of select="java:decodeUrl($scriptUrl)"/>'.
			</sch:report>
			
			<sch:report test="java:fileExists($scriptUrl) and not(java:isValidXsl($scriptUrl))">
				Invalid value for @xslt-conref ('<sch:value-of select="$scriptAttr"/>').
				No valid XSL file: '<sch:value-of select="java:decodeUrl($scriptUrl)"/>'.
			</sch:report>
			
			<sch:report test="(string($sourceAttr) != '') and not(java:fileExists($sourceUrl))">
				Invalid value for @xslt-conref-source ('<sch:value-of select="$sourceAttr"/>').
				Not existing file: '<sch:value-of select="java:decodeUrl($sourceUrl)"/>'.
			</sch:report>
			
			<sch:report test="(string($sourceAttr) != '') and java:fileExists($sourceUrl) and not(doc-available($sourceUrl))">
				Invalid value for @xslt-conref ('<sch:value-of select="$sourceAttr"/>').
				No valid XML file: '<sch:value-of select="java:decodeUrl($sourceUrl)"/>'.
			</sch:report>
			
		</sch:rule>
	</sch:pattern>

	
</sch:schema>