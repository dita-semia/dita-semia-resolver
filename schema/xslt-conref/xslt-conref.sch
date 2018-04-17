<?xml version="1.0" encoding="UTF-8"?>
<sch:schema 
	xmlns:sch	= "http://purl.oclc.org/dsdl/schematron"
	xmlns:sqf	= "http://www.schematron-quickfix.com/validator/process"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	queryBinding="xslt2">
	
	<sch:ns uri="java:org.DitaSemia.Base.FileUtil" 								prefix="jfu"/>
	<sch:ns uri="java:org.DitaSemia.Oxygen.XsltConref.XsltConrefSchematronUtil" prefix="jxcr"/>
	<sch:ns uri="java:org.DitaSemia.Oxygen.SchematronUtil" 						prefix="jsu"/>
	<sch:ns uri="http://www.dita-semia.org/xslt-conref/custom-parameter" 		prefix="xcrcp"/>
	<sch:ns uri="http://www.dita-semia.org/xslt-conref" 						prefix="xcr"/>
	
	<sch:pattern id="xslt-conref">
		<sch:rule context="*[@xcr:xsl]">
			
			<sch:let name="scriptAttr" 	value="@xcr:xsl"/>
			<sch:let name="scriptUrl" 	value="jfu:resolveUri($scriptAttr, .)"/>
			<sch:let name="sourceAttr" 	value="@xcr:source"/>
			<sch:let name="sourceType" 	value="@xcr:source-type"/>
			<sch:let name="sourceUrl" 	value="jfu:resolveUri($sourceAttr, .)"/>
			
			<sch:report test="not(jfu:fileExists($scriptUrl))">
				Invalid value for @xcr:xsl ('<sch:value-of select="$scriptAttr"/>').
				Not existing file: '<sch:value-of select="jfu:decodeUrl(string($scriptUrl))"/>'.
			</sch:report>
			<sch:report test="jfu:fileExists($scriptUrl) and not(jxcr:isValidXsl($scriptUrl))">
				Invalid value for @xcr:xsl ('<sch:value-of select="$scriptAttr"/>').
				No valid XSL file: '<sch:value-of select="jfu:decodeUrl(string($scriptUrl))"/>'.
			</sch:report>
			
			<sch:report test="(string($sourceAttr) != '') and not(jfu:fileExists($sourceUrl))">
				Invalid value for @xcr:source ('<sch:value-of select="$sourceAttr"/>').
				Not existing file: '<sch:value-of select="jfu:decodeUrl(string($sourceUrl))"/>'.
			</sch:report>
			
			<sch:report test="(string($sourceAttr) != '') and ((string($sourceType) = 'xml') or (string($sourceType) = ''))and jfu:fileExists($sourceUrl) and not(jsu:isValidXml($sourceUrl, true()))">
				Invalid value for @xcr:source ('<sch:value-of select="$sourceAttr"/>').
				No valid XML file: '<sch:value-of select="jfu:decodeUrl(string($sourceUrl))"/>'.
			</sch:report>
		</sch:rule>
		<sch:rule context="*[@xcr:xsl]/@xcrcp:*">
			
			<sch:let name="scriptAttr" 	value="parent::*/@xcr:xsl"/>
			<sch:let name="scriptUrl" 	value="jfu:resolveUri($scriptAttr, parent::*)"/>
			
			<sch:report test="jxcr:isXslParameterUndefined($scriptUrl, local-name())" role="warn">
				The parameter '<sch:value-of select="local-name()"/>' is undefined.
			</sch:report>
		</sch:rule>
	</sch:pattern>

	
</sch:schema>