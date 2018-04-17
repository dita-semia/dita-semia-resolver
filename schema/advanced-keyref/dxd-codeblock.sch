<?xml version="1.0" encoding="UTF-8"?>
<sch:schema 
	xmlns:sch	= "http://purl.oclc.org/dsdl/schematron"
	xmlns:sqf	= "http://www.schematron-quickfix.com/validator/process"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	queryBinding="xslt2">
	
	
	<sch:ns uri="http://saxon.sf.net/java-type" 			prefix="jt"/>
	<sch:ns uri="java:org.DitaSemia.Base.FileUtil" 			prefix="jfu"/>
	<sch:ns uri="java:org.DitaSemia.Oxygen.SchematronUtil" 	prefix="jsu"/>
	
	<xsl:variable name="dxdCoderefContentCache" as="item()" select="jsu:createXmlCache()"/>
	
	<sch:pattern id="dxd-codeblock">
		<sch:rule context="*[contains(@class, ' ds-d/dxd-codeblock ')]">
			
			<sch:let name="coderefAttr" 	value="@coderef"/>
			<sch:let name="coderefFileUrl" 	value="jfu:resolveUri(tokenize($coderefAttr, '#')[1], .)"/>
			<sch:let name="coderefId" 		value="tokenize($coderefAttr, '#')[2]"/>
			<sch:let name="coderefContent" 	value="jsu:loadXmlWithValidation($coderefFileUrl, $dxdCoderefContentCache)"/>

			<sch:report test="empty($coderefContent) and not(jfu:fileExists($coderefFileUrl))">
				The referenced file does not exist: '<sch:value-of select="jfu:decodeUrl(string($coderefFileUrl))"/>'.
			</sch:report>
			
			<sch:report test="exists($coderefContent) and (not($coderefContent/list/codeblock/@id = $coderefId))">
				The referenced file does not contain a codeblock with the referenced id '<sch:value-of select="$coderefId"/>' ('<sch:value-of select="jfu:decodeUrl(string($coderefFileUrl))"/>)'.
			</sch:report>
			
		</sch:rule>
	</sch:pattern>

	
</sch:schema>