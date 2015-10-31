<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"  
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes	= "#all"
	expand-text				= "yes">
	
	<!-- classes to be used for checking elements with *[contains(@class, 'C_xxx')] -->
	
	<xsl:variable name="C_TOPIC"			as="xs:string"> topic/topic </xsl:variable>
	<xsl:variable name="C_PROLOG"			as="xs:string"> topic/prolog </xsl:variable>
	<xsl:variable name="C_BODY"				as="xs:string"> topic/body </xsl:variable>
	<xsl:variable name="C_SECTION"			as="xs:string"> topic/section </xsl:variable>
	<xsl:variable name="C_FIG"				as="xs:string"> topic/fig </xsl:variable>
	<xsl:variable name="C_NOTE"				as="xs:string"> topic/note </xsl:variable>
	<xsl:variable name="C_TITLE"			as="xs:string"> topic/title </xsl:variable>
	<xsl:variable name="C_P"				as="xs:string"> topic/p </xsl:variable>
	<xsl:variable name="C_IMAGE"			as="xs:string"> topic/image </xsl:variable>
	<xsl:variable name="C_DRAFT_COMMENT"	as="xs:string"> topic/draft-comment </xsl:variable>
	<xsl:variable name="C_RELATED_LINKS"	as="xs:string"> topic/related-links </xsl:variable>
	<xsl:variable name="C_XREF"				as="xs:string"> topic/xref </xsl:variable>
	
	<xsl:variable name="C_UL"				as="xs:string"> topic/ul </xsl:variable>
	<xsl:variable name="C_OL"				as="xs:string"> topic/ol </xsl:variable>
	<xsl:variable name="C_LI"				as="xs:string"> topic/li </xsl:variable>
	<xsl:variable name="C_SL"				as="xs:string"> topic/sl </xsl:variable>
	<xsl:variable name="C_SLI"				as="xs:string"> topic/sli </xsl:variable>
	
	<xsl:variable name="C_DL"				as="xs:string"> topic/dl </xsl:variable>
	<xsl:variable name="C_DLENTRY"			as="xs:string"> topic/dlentry </xsl:variable>
	<xsl:variable name="C_DT"				as="xs:string"> topic/dt </xsl:variable>
	<xsl:variable name="C_DD"				as="xs:string"> topic/dd </xsl:variable>
	
	<xsl:variable name="C_TABLE"			as="xs:string"> topic/table </xsl:variable>
	<xsl:variable name="C_TGROUP"			as="xs:string"> topic/tgroup </xsl:variable>
	<xsl:variable name="C_COLSPEC"			as="xs:string"> topic/colspec </xsl:variable>
	<xsl:variable name="C_THEAD"			as="xs:string"> topic/thead </xsl:variable>
	<xsl:variable name="C_TBODY"			as="xs:string"> topic/tbody </xsl:variable>
	<xsl:variable name="C_ROW"				as="xs:string"> topic/row </xsl:variable>
	<xsl:variable name="C_ENTRY"			as="xs:string"> topic/entry </xsl:variable>
	
	<xsl:variable name="C_SIMPLETABLE"		as="xs:string"> topic/simpletable </xsl:variable>
	<xsl:variable name="C_STHEAD"			as="xs:string"> topic/sthead </xsl:variable>
	<xsl:variable name="C_STBODY"			as="xs:string"> topic/stbody </xsl:variable>
	<xsl:variable name="C_STROW"			as="xs:string"> topic/strow </xsl:variable>
	<xsl:variable name="C_STENTRY"			as="xs:string"> topic/stentry </xsl:variable>
	
	<xsl:variable name="C_CODEBLOCK"		as="xs:string"> pr-d/codeblock </xsl:variable>
	<xsl:variable name="C_CODEPH"			as="xs:string"> pr-d/codeph </xsl:variable>
	
	
	
	<!-- classpaths to be used for creating elements with class="{$CP_xxx}" -->
	
	<xsl:variable name="CP_TOPIC"			as="xs:string">-{$C_TOPIC}</xsl:variable>
	<xsl:variable name="CP_PROLOG"			as="xs:string">-{$C_PROLOG}</xsl:variable>
	<xsl:variable name="CP_BODY"			as="xs:string">-{$C_BODY}</xsl:variable>
	<xsl:variable name="CP_SECTION"			as="xs:string">-{$C_SECTION}</xsl:variable>
	<xsl:variable name="CP_FIG"				as="xs:string">-{$C_FIG}</xsl:variable>
	<xsl:variable name="CP_NOTE"			as="xs:string">-{$C_NOTE}</xsl:variable>
	<xsl:variable name="CP_TITLE"			as="xs:string">-{$C_TITLE}</xsl:variable>
	<xsl:variable name="CP_P"				as="xs:string">-{$C_P}</xsl:variable>
	<xsl:variable name="CP_IMAGE"			as="xs:string">-{$C_IMAGE}</xsl:variable>
	<xsl:variable name="CP_DRAFT_COMMENT"	as="xs:string">-{$C_DRAFT_COMMENT}</xsl:variable>
	<xsl:variable name="CP_RELATED_LINKS"	as="xs:string">-{$C_RELATED_LINKS}</xsl:variable>
	<xsl:variable name="CP_XREF"			as="xs:string">-{$C_XREF}</xsl:variable>
	
	<xsl:variable name="CP_UL"				as="xs:string">-{$C_UL}</xsl:variable>
	<xsl:variable name="CP_OL"				as="xs:string">-{$C_OL}</xsl:variable>
	<xsl:variable name="CP_LI"				as="xs:string">-{$C_LI}</xsl:variable>
	<xsl:variable name="CP_SL"				as="xs:string">-{$C_SL}</xsl:variable>
	<xsl:variable name="CP_SLI"				as="xs:string">-{$C_SLI}</xsl:variable>
	
	<xsl:variable name="CP_DL"				as="xs:string">-{$C_DL}</xsl:variable>
	<xsl:variable name="CP_DLENTRY"			as="xs:string">-{$C_DLENTRY}</xsl:variable>
	<xsl:variable name="CP_DT"				as="xs:string">-{$C_DT}</xsl:variable>
	<xsl:variable name="CP_DD"				as="xs:string">-{$C_DD}</xsl:variable>
	
	<xsl:variable name="CP_TABLE"			as="xs:string">-{$C_TABLE}</xsl:variable>
	<xsl:variable name="CP_TGROUP"			as="xs:string">-{$C_TGROUP}</xsl:variable>
	<xsl:variable name="CP_COLSPEC"			as="xs:string">-{$C_COLSPEC}</xsl:variable>
	<xsl:variable name="CP_THEAD"			as="xs:string">-{$C_THEAD}</xsl:variable>
	<xsl:variable name="CP_TBODY"			as="xs:string">-{$C_TBODY}</xsl:variable>
	<xsl:variable name="CP_ROW"				as="xs:string">-{$C_ROW}</xsl:variable>
	<xsl:variable name="CP_ENTRY"			as="xs:string">-{$C_ENTRY}</xsl:variable>
	
	<xsl:variable name="CP_SIMPLETABLE"		as="xs:string">-{$C_SIMPLETABLE}</xsl:variable>
	<xsl:variable name="CP_STHEAD"			as="xs:string">-{$C_STHEAD}</xsl:variable>
	<xsl:variable name="CP_STBODY"			as="xs:string">-{$C_STBODY}</xsl:variable>
	<xsl:variable name="CP_STROW"			as="xs:string">-{$C_STROW}</xsl:variable>
	<xsl:variable name="CP_STENTRY"			as="xs:string">-{$C_STENTRY}</xsl:variable>
	
	<xsl:variable name="CP_CODEBLOCK"		as="xs:string">+ topic/pre {$C_CODEBLOCK}</xsl:variable>
	<xsl:variable name="CP_CODEPH"			as="xs:string">+ topic/ph {$C_CODEPH}</xsl:variable>
	
</xsl:stylesheet>
