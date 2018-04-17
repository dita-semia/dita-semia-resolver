<?xml version="1.0" encoding="UTF-8"?>
<sch:schema 
	xmlns:sch	= "http://purl.oclc.org/dsdl/schematron"
	xmlns:sqf	= "http://www.schematron-quickfix.com/validator/process"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:chk	= "http://www.dita-semia.org/checkbat"
	queryBinding="xslt2">
	
	
	<sch:ns uri="http://www.dita-semia.org/checkbat" 		prefix="chk"/>
	<sch:ns uri="java:org.DitaSemia.Oxygen.SchematronUtil" 	prefix="jsu"/>
	
	<sch:pattern id="checkbat">
		<sch:rule context="*[@chk:*]">
			
			<sch:report test="exists(@chk:regex) and not(matches(string(.), @chk:regex, 'm'))">
				Element &lt;<sch:value-of select="name()"/>&gt;: The content '<sch:value-of select="."/>' does not match the required pattern: "<sch:value-of select="@chk:regex"/>" <sch:value-of select="if (@chk:regex-desc) then concat(' (', @chk:regex-desc, ')') else ()"/>.
			</sch:report>

			<sch:report test="(string(@chk:required) != '') and empty(jsu:evaluateXPathToNode(., @chk:required))">
				Element &lt;<sch:value-of select="name()"/>&gt;: The required content <sch:value-of select="@chk:required"/> is missing.
			</sch:report>
			
			<sch:report test="(@flags = 'text-xor-element') and empty(text() | element())">
				Element &lt;<sch:value-of select="name()"/>&gt;: Missing content. Either text or element(s) are expected.
			</sch:report>
			
			<sch:report test="(@flags = 'text-xor-element') and exists(text()) and exists(element())">
				Element &lt;<sch:value-of select="name()"/>&gt;: Excessive content. Only one of text or element is expected.
			</sch:report>

		</sch:rule>
	</sch:pattern>

	
</sch:schema>