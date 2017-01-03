<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"  
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes	= "#all">
	
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
	<xsl:variable name="C_DIV"				as="xs:string"> topic/div </xsl:variable>
	<xsl:variable name="C_FOREIGN"			as="xs:string"> topic/foreign </xsl:variable>
	<xsl:variable name="C_PH"				as="xs:string"> topic/ph </xsl:variable>
	<xsl:variable name="C_I"				as="xs:string"> hi-d/i </xsl:variable>
	
	<xsl:variable name="C_UL"				as="xs:string"> topic/ul </xsl:variable>
	<xsl:variable name="C_OL"				as="xs:string"> topic/ol </xsl:variable>
	<xsl:variable name="C_LI"				as="xs:string"> topic/li </xsl:variable>
	<xsl:variable name="C_SL"				as="xs:string"> topic/sl </xsl:variable>
	<xsl:variable name="C_SLI"				as="xs:string"> topic/sli </xsl:variable>
	
	<xsl:variable name="C_DL"				as="xs:string"> topic/dl </xsl:variable>
	<xsl:variable name="C_DLENTRY"			as="xs:string"> topic/dlentry </xsl:variable>
	<xsl:variable name="C_DT"				as="xs:string"> topic/dt </xsl:variable>
	<xsl:variable name="C_DD"				as="xs:string"> topic/dd </xsl:variable>
	<xsl:variable name="C_DLHEAD"			as="xs:string"> topic/dlhead </xsl:variable>
	<xsl:variable name="C_DTHD"				as="xs:string"> topic/dthd </xsl:variable>
	<xsl:variable name="C_DDHD"				as="xs:string"> topic/ddhd </xsl:variable>
	
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
	
	<xsl:variable name="C_SVG_CONTAINER"	as="xs:string"> svg-d/svg-container </xsl:variable>
	
	<xsl:variable name="C_KEY_XREF"			as="xs:string"> akr-d/key-xref </xsl:variable>
	
	
	<!-- classpaths to be used for creating elements with class="{$CP_xxx}" -->
	
	<xsl:variable name="CP_TOPIC"			as="xs:string"	select="concat('-', $C_TOPIC)"/>
	<xsl:variable name="CP_PROLOG"			as="xs:string"	select="concat('-', $C_PROLOG)"/>
	<xsl:variable name="CP_BODY"			as="xs:string"	select="concat('-', $C_BODY)"/>
	<xsl:variable name="CP_SECTION"			as="xs:string"	select="concat('-', $C_SECTION)"/>
	<xsl:variable name="CP_FIG"				as="xs:string"	select="concat('-', $C_FIG)"/>
	<xsl:variable name="CP_NOTE"			as="xs:string"	select="concat('-', $C_NOTE)"/>
	<xsl:variable name="CP_TITLE"			as="xs:string"	select="concat('-', $C_TITLE)"/>
	<xsl:variable name="CP_P"				as="xs:string"	select="concat('-', $C_P)"/>
	<xsl:variable name="CP_IMAGE"			as="xs:string"	select="concat('-', $C_IMAGE)"/>
	<xsl:variable name="CP_DRAFT_COMMENT"	as="xs:string"	select="concat('-', $C_DRAFT_COMMENT)"/>
	<xsl:variable name="CP_RELATED_LINKS"	as="xs:string"	select="concat('-', $C_RELATED_LINKS)"/>
	<xsl:variable name="CP_XREF"			as="xs:string"	select="concat('-', $C_XREF)"/>
	<xsl:variable name="CP_DIV"				as="xs:string"	select="concat('-', $C_DIV)"/>
	<xsl:variable name="CP_FOREIGN"			as="xs:string"	select="concat('-', $C_FOREIGN)"/>
	<xsl:variable name="CP_PH"				as="xs:string"	select="concat('-', $C_PH)"/>
	<xsl:variable name="CP_I"				as="xs:string"	select="concat('+ topic/ph', $C_I)"/>
	
	<xsl:variable name="CP_UL"				as="xs:string"	select="concat('-', $C_UL)"/>
	<xsl:variable name="CP_OL"				as="xs:string"	select="concat('-', $C_OL)"/>
	<xsl:variable name="CP_LI"				as="xs:string"	select="concat('-', $C_LI)"/>
	<xsl:variable name="CP_SL"				as="xs:string"	select="concat('-', $C_SL)"/>
	<xsl:variable name="CP_SLI"				as="xs:string"	select="concat('-', $C_SLI)"/>
	
	<xsl:variable name="CP_DL"				as="xs:string"	select="concat('-', $C_DL)"/>
	<xsl:variable name="CP_DLENTRY"			as="xs:string"	select="concat('-', $C_DLENTRY)"/>
	<xsl:variable name="CP_DT"				as="xs:string"	select="concat('-', $C_DT)"/>
	<xsl:variable name="CP_DD"				as="xs:string"	select="concat('-', $C_DD)"/>
	<xsl:variable name="CP_DLHEAD"			as="xs:string"	select="concat('-', $C_DLHEAD)"/>
	<xsl:variable name="CP_DTHD"			as="xs:string"	select="concat('-', $C_DTHD)"/>
	<xsl:variable name="CP_DDHD"			as="xs:string"	select="concat('-', $C_DDHD)"/>
	
	<xsl:variable name="CP_TABLE"			as="xs:string"	select="concat('-', $C_TABLE)"/>
	<xsl:variable name="CP_TGROUP"			as="xs:string"	select="concat('-', $C_TGROUP)"/>
	<xsl:variable name="CP_COLSPEC"			as="xs:string"	select="concat('-', $C_COLSPEC)"/>
	<xsl:variable name="CP_THEAD"			as="xs:string"	select="concat('-', $C_THEAD)"/>
	<xsl:variable name="CP_TBODY"			as="xs:string"	select="concat('-', $C_TBODY)"/>
	<xsl:variable name="CP_ROW"				as="xs:string"	select="concat('-', $C_ROW)"/>
	<xsl:variable name="CP_ENTRY"			as="xs:string"	select="concat('-', $C_ENTRY)"/>
	
	<xsl:variable name="CP_SIMPLETABLE"		as="xs:string"	select="concat('-', $C_SIMPLETABLE)"/>
	<xsl:variable name="CP_STHEAD"			as="xs:string"	select="concat('-', $C_STHEAD)"/>
	<xsl:variable name="CP_STBODY"			as="xs:string"	select="concat('-', $C_STBODY)"/>
	<xsl:variable name="CP_STROW"			as="xs:string"	select="concat('-', $C_STROW)"/>
	<xsl:variable name="CP_STENTRY"			as="xs:string"	select="concat('-', $C_STENTRY)"/>
	
	<xsl:variable name="CP_CODEBLOCK"		as="xs:string"	select="concat('+ topic/pre', 	$C_CODEBLOCK)"/>
	<xsl:variable name="CP_CODEPH"			as="xs:string"	select="concat('+ topic/ph', 	$C_CODEPH)"/>
	
	<xsl:variable name="CP_SVG_CONTAINER"	as="xs:string" select="concat('+ topic/foreign', $C_SVG_CONTAINER)"/>
	
	<xsl:variable name="CP_KEY_XREF"		as="xs:string" select="concat('+ topic/ph', $C_KEY_XREF)"/>




	<xsl:variable name="OCLASS_CSLI"	as="xs:string"	select="'csli'"/>
	<xsl:variable name="OCLASS_CODE"	as="xs:string"	select="'code'"/>
	
	
</xsl:stylesheet>
