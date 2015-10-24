<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"  
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes	= "#all">
	
	<xsl:variable name="CLASS_TOPIC"			as="xs:string">- topic/topic </xsl:variable>
	<xsl:variable name="CLASS_PROLOG"			as="xs:string">- topic/prolog </xsl:variable>
	<xsl:variable name="CLASS_BODY"				as="xs:string">- topic/body </xsl:variable>
	<xsl:variable name="CLASS_SECTION"			as="xs:string">- topic/section </xsl:variable>
	<xsl:variable name="CLASS_FIG"				as="xs:string">- topic/fig </xsl:variable>
	<xsl:variable name="CLASS_NOTE"				as="xs:string">- topic/note </xsl:variable>
	<xsl:variable name="CLASS_TITLE"			as="xs:string">- topic/title </xsl:variable>
	<xsl:variable name="CLASS_P"				as="xs:string">- topic/p </xsl:variable>
	<xsl:variable name="CLASS_IMAGE"			as="xs:string">- topic/image </xsl:variable>
	<xsl:variable name="CLASS_DRAFTCOMMENT"		as="xs:string">- topic/draft-comment </xsl:variable>
	<xsl:variable name="CLASS_RELATED_LINKS"	as="xs:string">- topic/related-links </xsl:variable>
	<xsl:variable name="CLASS_XREF"				as="xs:string">- topic/xref </xsl:variable>

	<xsl:variable name="CLASS_UL"				as="xs:string">- topic/ul </xsl:variable>
	<xsl:variable name="CLASS_OL"				as="xs:string">- topic/ol </xsl:variable>
	<xsl:variable name="CLASS_LI"				as="xs:string">- topic/li </xsl:variable>
	<xsl:variable name="CLASS_SL"				as="xs:string">- topic/sl </xsl:variable>
	<xsl:variable name="CLASS_SLI"				as="xs:string">- topic/sli </xsl:variable>

	<xsl:variable name="CLASS_DL"				as="xs:string">- topic/dl </xsl:variable>
	<xsl:variable name="CLASS_DLENTRY"			as="xs:string">- topic/dlentry </xsl:variable>
	<xsl:variable name="CLASS_DT"				as="xs:string">- topic/dt </xsl:variable>
	<xsl:variable name="CLASS_DD"				as="xs:string">- topic/dd </xsl:variable>
	
	<xsl:variable name="CLASS_TABLE"			as="xs:string">- topic/table </xsl:variable>
	<xsl:variable name="CLASS_TGROUP"			as="xs:string">- topic/tgroup </xsl:variable>
	<xsl:variable name="CLASS_COLSPEC"			as="xs:string">- topic/colspec </xsl:variable>
	<xsl:variable name="CLASS_THEAD"			as="xs:string">- topic/thead </xsl:variable>
	<xsl:variable name="CLASS_TBODY"			as="xs:string">- topic/tbody </xsl:variable>
	<xsl:variable name="CLASS_ROW"				as="xs:string">- topic/row </xsl:variable>
	<xsl:variable name="CLASS_ENTRY"			as="xs:string">- topic/entry </xsl:variable>
	
	<xsl:variable name="CLASS_SIMPLETABLE"		as="xs:string">- topic/simpletable </xsl:variable>
	<xsl:variable name="CLASS_STHEAD"			as="xs:string">- topic/sthead </xsl:variable>
	<xsl:variable name="CLASS_STBODY"			as="xs:string">- topic/stbody </xsl:variable>
	<xsl:variable name="CLASS_STROW"			as="xs:string">- topic/strow </xsl:variable>
	<xsl:variable name="CLASS_STENTRY"			as="xs:string">- topic/stentry </xsl:variable>
	
	<xsl:variable name="CLASS_CODEBLOCK"		as="xs:string">+ topic/pre pr-d/codeblock </xsl:variable>
	<xsl:variable name="CLASS_CODEPH"			as="xs:string">+ topic/ph pr-d/codeph </xsl:variable>
	
</xsl:stylesheet>
