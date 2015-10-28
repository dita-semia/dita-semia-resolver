<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
	xmlns:cache = "java:org.DitaSemia.JavaBase.XslTransformerCache"
	xmlns:jt 	= "http://saxon.sf.net/java-type"
	exclude-result-prefixes="xs">
	
	<xsl:function name="ds:isScriptValid" as="xs:boolean">
		<xsl:param name="scriptUrl" as="xs:anyURI"/>
		
		<xsl:variable name="cache"			as="jt:org.DitaSemia.JavaBase.XslTransformerCache" 	select="cache:getInstance()"/>
		<xsl:variable name="transformer"	as="jt:javax.xml.transform.Transformer?"			select="cache:getTransformer($cache, $scriptUrl)"/>
		
		<xsl:sequence select="exists($transformer)"/>
		
	</xsl:function>
	
</xsl:stylesheet>
