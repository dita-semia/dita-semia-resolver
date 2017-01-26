<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"  
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes	= "#all">
	
	
	
	<xsl:variable name="CBA_FLAG_HIDE"				as="xs:string"	select="'hide'"/>
	<xsl:variable name="CBA_FLAG_HIDE_EMPTY"		as="xs:string"	select="'hide-empty'"/>
	<xsl:variable name="CBA_FLAG_CSLI"				as="xs:string"	select="'csli'"/>
	<xsl:variable name="CBA_FLAG_CODE"				as="xs:string"	select="'code'"/>
	<xsl:variable name="CBA_FLAG_PE_BRACED"			as="xs:string"	select="'pe-braced'"/>
	<xsl:variable name="CBA_FLAG_PE_ITALIC"			as="xs:string"	select="'pe-italic'"/>
	<xsl:variable name="CBA_FLAG_DEFAULT_ITALIC"	as="xs:string"	select="'default-italic'"/>


</xsl:stylesheet>
