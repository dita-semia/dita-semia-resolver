<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:saxon="http://saxon.sf.net/"
	exclude-result-prefixes="#all" expand-text="yes">

	<xsl:output method="xml" indent="yes"/>

	<xsl:param name="xPathToXsltConref" as="xs:string" select="'/*[1]'"/>

	<xsl:variable name="FIELD_DEF_REFERENCE_ID" as="xs:string" select="'global-field-definitions'"/>

	<xsl:template match="/">

		<xsl:processing-instruction name="xml-model">href="urn:dita-ng:dita:rng:topic.rng" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>

		<xsl:variable name="xsltConref" 	as="element()"	select="saxon:evaluate($xPathToXsltConref)"/>
		<xsl:variable name="fieldName" as="xs:string"
			select="$xsltConref/ancestor::row/entry[1]//codeph"/>
		<xsl:variable name="fieldDefTable" as="element()"
			select="//reference[@id = $FIELD_DEF_REFERENCE_ID]/refbody/table"/>
		<xsl:variable name="fieldDef" as="element()?"
			select="$fieldDefTable/tgroup/tbody/row[entry[1]//codeph = $fieldName]"/>

		<xsl:choose>
			<xsl:when test="exists($fieldDef)">
				<block class="- dita-semia/block ">
					<xsl:copy-of select="$fieldDef/entry[2]/node()"/>
				</block>
			</xsl:when>
			<xsl:otherwise>
				<no-content>(undefined field <codeph>{$fieldName}</codeph>)</no-content>
			</xsl:otherwise>
		</xsl:choose>



	</xsl:template>
</xsl:stylesheet>
